package gov.goias.relatorios.consumer.solicitacaoRelatorio;

import gov.goias.relatorios.consumer.notificacao.NotificacaoService;
import gov.goias.relatorios.consumer.solicitacaoRelatorio.entity.SolicitacaoRelatorio;
import gov.goias.relatorios.consumer.solicitacaoRelatorio.entity.enuns.StatusRelatorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class SolicitacaoRelatorioService {

    private final NotificacaoService notificacaoService;
    private final SolicitacaoRelatorioRepository repository;
    private final Random random = new Random();

    @Value("${relatorio.simulation.error-rate:0.2}")
    private double errorRate;

    @Value("${relatorio.simulation.processing-delay:5000}")
    private long processingDelay;

    public SolicitacaoRelatorio processarRelatorio(SolicitacaoRelatorio solicitacao) {
        log.info("Iniciando processamento para solicitação: {} - Status atual: {}",
                solicitacao.getIdSolicitacao(), solicitacao.getStatus());

        // Busca a solicitação atual no banco de dados
        Optional<SolicitacaoRelatorio> solicitacaoExistente = repository.findById(solicitacao.getIdSolicitacao());

        SolicitacaoRelatorio solicitacaoAtualizada;

        if (solicitacaoExistente.isPresent()) {
            // Atualiza a solicitação existente
            solicitacaoAtualizada = solicitacaoExistente.get();
            StatusRelatorio statusAtual = solicitacaoAtualizada.getStatus();

            log.info("Solicitação encontrada no banco - Status atual: {}", statusAtual);

            // Processa baseado no status atual
            solicitacaoAtualizada = processarProximaEtapa(solicitacaoAtualizada);

        } else {
            // Primeira vez processando esta solicitação
            log.info("Nova solicitação sendo processada pela primeira vez");

            // Se vier como AGENDADO, inicia o processamento
            if (solicitacao.getStatus() == StatusRelatorio.AGENDADO) {
                solicitacao = iniciarProcessamento(solicitacao);
                log.info("Nova solicitação iniciada: AGENDADO -> EM_EXECUCAO");
            } else {
                // Salva no estado atual se não for AGENDADO
                solicitacao = repository.save(solicitacao);
            }
            solicitacaoAtualizada = solicitacao;
        }

        solicitacaoAtualizada = processarProximaEtapa(solicitacaoAtualizada);

        // Envia notificação após a atualização
        this.notificacaoService.notificar(solicitacaoAtualizada);

        return solicitacaoAtualizada;
    }

    private SolicitacaoRelatorio processarProximaEtapa(SolicitacaoRelatorio solicitacao) {
        StatusRelatorio statusAtual = solicitacao.getStatus();

        switch (statusAtual) {
            case AGENDADO, EM_FILA -> {
                return iniciarProcessamento(solicitacao);
            }
            case EM_EXECUCAO -> {
                return finalizarProcessamento(solicitacao);
            }
            case CONCLUIDO, FALHA, CANCELADO -> {
                log.info("Solicitação {} já está em status final: {}",
                        solicitacao.getIdSolicitacao(), statusAtual);
                return solicitacao;
            }
            default -> {
                log.warn("Status desconhecido para solicitação {}: {}",
                        solicitacao.getIdSolicitacao(), statusAtual);
                return solicitacao;
            }
        }
    }

    private SolicitacaoRelatorio iniciarProcessamento(SolicitacaoRelatorio solicitacao) {
        log.info("Iniciando processamento da solicitação: {}", solicitacao.getIdSolicitacao());

        // Atualiza para EM_EXECUCAO
        solicitacao.setStatus(StatusRelatorio.EM_EXECUCAO);
        solicitacao.setDataInicioExecucao(LocalDateTime.now());
        solicitacao.setProgresso(0);
        solicitacao.setMensagemStatus("Iniciando processamento do relatório...");

        solicitacao = repository.save(solicitacao);

        log.info("Solicitação {} atualizada para EM_EXECUCAO", solicitacao.getIdSolicitacao());

        return solicitacao;
    }

    private SolicitacaoRelatorio finalizarProcessamento(SolicitacaoRelatorio solicitacao) {
        log.info("Finalizando processamento da solicitação: {}", solicitacao.getIdSolicitacao());

        try {
            // Simula tempo de processamento
            simularProcessamento(solicitacao);

            // Determina se o processamento será bem-sucedido ou falhará
            boolean processamentoComSucesso = determinarSucessoProcessamento(solicitacao);

            if (processamentoComSucesso) {
                return concluirComSucesso(solicitacao);
            } else {
                return concluirComFalha(solicitacao);
            }

        } catch (Exception e) {
            log.error("Erro inesperado durante processamento da solicitação: {}",
                    solicitacao.getIdSolicitacao(), e);
            return concluirComFalha(solicitacao, "Erro inesperado: " + e.getMessage());
        }
    }

    private void simularProcessamento(SolicitacaoRelatorio solicitacao) {
        log.info("Simulando processamento do relatório: {}", solicitacao.getIdSolicitacao());

        try {
            // Simula progresso gradual
            for (int progresso = 10; progresso <= 90; progresso += 20) {
                Thread.sleep(processingDelay / 5); // Divide o delay em etapas

                solicitacao.setProgresso(progresso);
                solicitacao.setMensagemStatus(String.format("Processando relatório... %d%%", progresso));
                repository.save(solicitacao);

                log.debug("Progresso da solicitação {}: {}%", solicitacao.getIdSolicitacao(), progresso);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processamento interrompido", e);
        }
    }

    private boolean determinarSucessoProcessamento(SolicitacaoRelatorio solicitacao) {
        // Fatores que podem influenciar o sucesso
        boolean sucessoAleatorio = random.nextDouble() > errorRate;

        // Fatores específicos que podem causar falha
        boolean falhaEspecifica = verificarFalhasEspecificas(solicitacao);

        boolean sucesso = sucessoAleatorio && !falhaEspecifica;

        log.info("Determinação de sucesso para solicitação {}: {} (taxa de erro: {})",
                solicitacao.getIdSolicitacao(), sucesso ? "SUCESSO" : "FALHA", errorRate);

        return sucesso;
    }

    private boolean verificarFalhasEspecificas(SolicitacaoRelatorio solicitacao) {
        // Simula condições específicas que podem causar falha

        // Falha se o ID contém "error" (para testes)
        if (solicitacao.getIdSolicitacao().toLowerCase().contains("error")) {
            log.warn("Forçando falha para solicitação com ID contendo 'error': {}",
                    solicitacao.getIdSolicitacao());
            return true;
        }

        // Falha se o tipo de relatório é específico (simulação)
        if (solicitacao.getTipoRelatorio() != null &&
                solicitacao.getTipoRelatorio().getCodigo().equals("TIPO_PROBLEMATICO")) {
            log.warn("Falha devido ao tipo de relatório problemático: {}",
                    solicitacao.getTipoRelatorio());
            return true;
        }

        // Falha se processamento demorou muito (timeout simulado)
        if (solicitacao.getDataInicioExecucao() != null) {
            long minutosProcessamento = java.time.Duration.between(
                    solicitacao.getDataInicioExecucao(), LocalDateTime.now()).toMinutes();

            if (minutosProcessamento > 30) { // Timeout de 30 minutos
                log.warn("Timeout no processamento da solicitação: {} ({}min)",
                        solicitacao.getIdSolicitacao(), minutosProcessamento);
                return true;
            }
        }

        return false;
    }

    private SolicitacaoRelatorio concluirComSucesso(SolicitacaoRelatorio solicitacao) {
        log.info("Concluindo com sucesso a solicitação: {}", solicitacao.getIdSolicitacao());

        solicitacao.setStatus(StatusRelatorio.CONCLUIDO);
        solicitacao.setDataConclusao(LocalDateTime.now());
        solicitacao.setProgresso(100);
        solicitacao.setMensagemStatus("Relatório gerado com sucesso!");

        // Simula informações do arquivo gerado
        solicitacao.setCaminhoArquivo(gerarCaminhoArquivo(solicitacao));
        solicitacao.setTamanhoArquivo(gerarTamanhoArquivo());

        solicitacao = repository.save(solicitacao);

        log.info("Solicitação {} concluída com sucesso em {}",
                solicitacao.getIdSolicitacao(), solicitacao.getDataConclusao());

        return solicitacao;
    }

    private SolicitacaoRelatorio concluirComFalha(SolicitacaoRelatorio solicitacao) {
        return concluirComFalha(solicitacao, null);
    }

    private SolicitacaoRelatorio concluirComFalha(SolicitacaoRelatorio solicitacao, String motivoEspecifico) {
        log.warn("Concluindo com falha a solicitação: {} - Motivo: {}",
                solicitacao.getIdSolicitacao(), motivoEspecifico);

        solicitacao.setStatus(StatusRelatorio.FALHA);
        solicitacao.setDataConclusao(LocalDateTime.now());

        // Define mensagem de erro apropriada
        String mensagemErro = motivoEspecifico != null ? motivoEspecifico : gerarMensagemErroAleatoria();
        solicitacao.setMensagemStatus(mensagemErro);

        // Limpa informações de arquivo em caso de falha
        solicitacao.setCaminhoArquivo(null);
        solicitacao.setTamanhoArquivo(null);

        solicitacao = repository.save(solicitacao);

        log.warn("Solicitação {} falhou em {} - Erro: {}",
                solicitacao.getIdSolicitacao(), solicitacao.getDataConclusao(), mensagemErro);

        return solicitacao;
    }

    private String gerarCaminhoArquivo(SolicitacaoRelatorio solicitacao) {
        String timestamp = LocalDateTime.now().toString().replaceAll("[:-]", "").substring(0, 15);
        return String.format("/relatorios/%s/%s_%s.pdf",
                solicitacao.getTipoRelatorio().getCodigo(),
                solicitacao.getIdSolicitacao(),
                timestamp);
    }

    private Long gerarTamanhoArquivo() {
        // Gera tamanho aleatório entre 100KB e 5MB
        return (long) (random.nextInt(4900) + 100) * 1024;
    }

    private String gerarMensagemErroAleatoria() {
        String[] mensagensErro = {
                "Falha na conexão com o banco de dados",
                "Timeout durante geração do relatório",
                "Memória insuficiente para processar o relatório",
                "Erro na validação dos dados de entrada",
                "Falha na escrita do arquivo de saída",
                "Serviço de relatórios temporariamente indisponível",
                "Erro interno do sistema",
                "Falha na comunicação com serviços externos"
        };

        return mensagensErro[random.nextInt(mensagensErro.length)];
    }

    /**
     * Método para forçar transição para um status específico (usar com cuidado)
     */
    public SolicitacaoRelatorio forcarStatus(String idSolicitacao, StatusRelatorio novoStatus, String motivo) {
        log.warn("Forçando mudança de status para solicitação: {} -> Status: {} - Motivo: {}",
                idSolicitacao, novoStatus, motivo);

        Optional<SolicitacaoRelatorio> solicitacaoOpt = repository.findById(idSolicitacao);

        if (solicitacaoOpt.isPresent()) {
            SolicitacaoRelatorio solicitacao = solicitacaoOpt.get();
            StatusRelatorio statusAnterior = solicitacao.getStatus();

            solicitacao.setStatus(novoStatus);
            solicitacao.setMensagemStatus(motivo);

            if (novoStatus.isFinal()) {
                solicitacao.setDataConclusao(LocalDateTime.now());
            }

            repository.save(solicitacao);
            this.notificacaoService.notificar(solicitacao);

            log.info("Status forçado com sucesso: {} -> {} para solicitação: {}",
                    statusAnterior, novoStatus, idSolicitacao);

            return solicitacao;
        } else {
            throw new IllegalArgumentException("Solicitação não encontrada: " + idSolicitacao);
        }
    }

    /**
     * Método para simular falha em solicitação específica (para testes)
     */
    public SolicitacaoRelatorio simularFalha(String idSolicitacao, String motivoFalha) {
        return forcarStatus(idSolicitacao, StatusRelatorio.FALHA, motivoFalha);
    }

    /**
     * Método para cancelar solicitação
     */
    public SolicitacaoRelatorio cancelarSolicitacao(String idSolicitacao, String motivo) {
        return forcarStatus(idSolicitacao, StatusRelatorio.CANCELADO, motivo);
    }
}