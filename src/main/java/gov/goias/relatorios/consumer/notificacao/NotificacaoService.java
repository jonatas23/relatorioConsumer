package gov.goias.relatorios.consumer.notificacao;

import gov.goias.relatorios.consumer.commons.KafkaProducer;
import gov.goias.relatorios.consumer.notificacao.dto.NotificationMessage;
import gov.goias.relatorios.consumer.notificacao.dto.NotificationType;
import gov.goias.relatorios.consumer.solicitacaoRelatorio.entity.SolicitacaoRelatorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificacaoService {

    private final KafkaProducer producer;
    @Value("${kafka.topic.relatorio.notificacao}")
    private String topicNotificacao;


    public void notificar(SolicitacaoRelatorio relatorio) {
        log.info("Enviando notificação para usuário: {} - Status: {}",
                relatorio.getUsuario(), relatorio.getStatus());

        try {
            NotificationMessage notification = criarNotificacao(relatorio);
            this.producer.publicar(this.topicNotificacao, notification);
            log.info("Notificação enviada com sucesso para usuário: {}", relatorio.getUsuario());

        } catch (Exception e) {
            log.error("Erro ao processar notificação: {}", e.getMessage(), e);

            // Envia notificação de erro como fallback
            try {
                NotificationMessage errorNotification = NotificationMessage.builder()
                        .id(UUID.randomUUID().toString())
                        .codgUsuario(relatorio.getUsuario())
                        .type(NotificationType.REPORT_FAILED)
                        .title("Erro no Sistema")
                        .message("Houve um erro interno. Tente novamente mais tarde.")
                        .build();

                this.producer.publicar(this.topicNotificacao, errorNotification);
            } catch (Exception fallbackError) {
                log.error("Erro crítico: falha ao enviar notificação de erro: {}", fallbackError.getMessage());
            }
        }
    }

    private NotificationMessage criarNotificacao(SolicitacaoRelatorio relatorio) {
        String titulo;
        String mensagem;
        NotificationType tipo;

        switch (relatorio.getStatus()) {
            case EM_EXECUCAO -> {
                tipo = NotificationType.REPORT_PROCESSING;
                titulo = "Relatório em Processamento";
                mensagem = String.format("O relatório %s está sendo gerado. Progresso: %d%%",
                        relatorio.getTipoRelatorio().getDescricao(),
                        relatorio.getProgresso() != null ? relatorio.getProgresso() : 0);
            }
            case CONCLUIDO -> {
                tipo = NotificationType.REPORT_COMPLETED;
                titulo = "Relatório Concluído";
                mensagem = String.format("O relatório %s foi gerado com sucesso e está disponível para download! Tamanho: %s",
                        relatorio.getTipoRelatorio().getDescricao(),
                        formatarTamanhoArquivo(relatorio.getTamanhoArquivo()));
            }
            case FALHA -> {
                tipo = NotificationType.REPORT_FAILED;
                titulo = "Falha na Geração do Relatório";
                mensagem = String.format("Houve um erro ao gerar o relatório %s: %s",
                        relatorio.getTipoRelatorio().getDescricao(),
                        relatorio.getMensagemStatus());
            }
            case CANCELADO -> {
                tipo = NotificationType.REPORT_CANCELLED;
                titulo = "Relatório Cancelado";
                mensagem = String.format("O relatório %s foi cancelado.",
                        relatorio.getTipoRelatorio().getDescricao());
            }
            default -> {
                tipo = NotificationType.REPORT_PROCESSING;
                titulo = "Status do Relatório";
                mensagem = String.format("Status do relatório %s atualizado para: %s",
                        relatorio.getTipoRelatorio().getDescricao(),
                        relatorio.getStatus().getDescricao());
            }
        }

        return NotificationMessage.builder()
                .id(UUID.randomUUID().toString())
                .codgUsuario(relatorio.getUsuario())
                .type(tipo)
                .title(titulo)
                .message(mensagem)
                .build();
    }

    private String formatarTamanhoArquivo(Long tamanhoBytes) {
        if (tamanhoBytes == null) return "N/A";

        if (tamanhoBytes < 1024) return tamanhoBytes + " B";
        if (tamanhoBytes < 1024 * 1024) return String.format("%.1f KB", tamanhoBytes / 1024.0);
        return String.format("%.1f MB", tamanhoBytes / (1024.0 * 1024.0));
    }

}
