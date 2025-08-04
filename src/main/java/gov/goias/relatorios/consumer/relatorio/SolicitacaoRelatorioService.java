// br.gov.go.financeiro.ipof.service.SolicitacaoRelatorioService
package gov.goias.relatorios.consumer.relatorio;

import gov.goias.relatorios.consumer.dto.NotificationMessage;
import gov.goias.relatorios.consumer.dto.NotificationType;
import gov.goias.relatorios.consumer.entity.SolicitacaoRelatorio;
import gov.goias.relatorios.consumer.enuns.StatusRelatorio;
import gov.goias.relatorios.consumer.producer.KafkaProducer;
import gov.goias.relatorios.consumer.repository.SolicitacaoRelatorioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class SolicitacaoRelatorioService {

    private final KafkaProducer producer;
    private final SolicitacaoRelatorioRepository repository;

    @Value("${kafka.topic.relatorio.notificacao}")
    private String topicNotificacao;


    public SolicitacaoRelatorio atualizarStatus(SolicitacaoRelatorio solicitacao) {

        switch (solicitacao.getStatus()) {
            case AGENDADO -> {
                solicitacao.setStatus(StatusRelatorio.EM_EXECUCAO);
                solicitacao.setDataInicioExecucao(LocalDateTime.now());
            }
            case EM_EXECUCAO -> {
                if (solicitacao.getDataInicioExecucao() == null) {
                    solicitacao.setDataInicioExecucao(LocalDateTime.now());
                }
            }
            case CONCLUIDO, FALHA, CANCELADO -> {
                if (solicitacao.getDataConclusao() == null) {
                    solicitacao.setDataConclusao(LocalDateTime.now());
                }
            }
        }

        var atualizada = repository.save(solicitacao);
        this.notificar(atualizada);

        return solicitacao;
    }

    public void notificar(SolicitacaoRelatorio relatorio) {
        log.info("Recebido evento de relatório concluído para matrícula: {}", relatorio.getUsuario());

        try {
            NotificationMessage notification = NotificationMessage.builder()
                    .id(UUID.randomUUID().toString())
                    .codgUsuario(relatorio.getUsuario())
                    .type(NotificationType.REPORT_COMPLETED)
                    .title("Relatório Concluído")
                    .message(String.format("O relatório %s foi gerado com sucesso!", relatorio.getTipoRelatorio().getDescricao()))
                    .build();

            this.producer.publicar(this.topicNotificacao, notification);

        } catch (Exception e) {
            log.error("Erro ao processar notificação de relatório concluído: {}", e.getMessage(), e);
            NotificationMessage notification = NotificationMessage.builder()
                    .id(UUID.randomUUID().toString())
                    .codgUsuario(relatorio.getUsuario())
                    .type(NotificationType.REPORT_FAILED)
                    .title("Falha ao gerar Relatorio")
                    .message(String.format("O relatório %s não foi gerado!", relatorio.getTipoRelatorio().getDescricao()))
                    .build();

            this.producer.publicar(this.topicNotificacao, notification);
        }
    }
}