package gov.goias.relatorios.consumer.relatorio;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gov.goias.relatorios.consumer.producer.KafkaProducer;
import gov.goias.relatorios.consumer.dto.NotificationMessage;
import gov.goias.relatorios.consumer.dto.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RelatorioKafkaConsumer {

    private final KafkaProducer producer;

    @Value("${kafka.topic.notificacao}")
    private String topicNotificacao;

    @KafkaListener(topics = "${kafka.topic.relatorio}", groupId = "relatorio-group")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2), exclude = {JsonProcessingException.class})
    public void consumir(ConsumerRecord<String, String> record) {
        log.info("Chave = {}", record.key());
        log.info("Cabecalho = {}", record.headers());
        log.info("Particao = {}", record.partition());

        String strDados = record.value();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        RelatorioDTO relatorio;

        try {
            relatorio = mapper.readValue(strDados, RelatorioDTO.class);
        } catch (JsonProcessingException ex) {
            log.error("Falha converter evento [dado={}}]", strDados, ex);
            return;
        }

        log.info("Evento Recebido = {}", relatorio);

        this.gravar(relatorio);
        this.notificar(relatorio);
    }

    private void gravar(RelatorioDTO relatorio) {
        try {
            System.out.println("Iniciando processamento...");

            // Pausa a execução por 10 segundos (10.000 milissegundos)
            Thread.sleep(10000);

            System.out.println("Processamento concluído após 10 segundos.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaura o estado de interrupção
        }
    }

    public void notificar(RelatorioDTO relatorio) {
        log.info("Recebido evento de relatório concluído para matrícula: {}", relatorio.codgUsuario());

        try {
            NotificationMessage notification = NotificationMessage.builder()
                    .id(UUID.randomUUID().toString())
                    .codgUsuario(relatorio.codgUsuario())
                    .type(NotificationType.REPORT_COMPLETED)
                    .title("Relatório Concluído")
                    .message(String.format("O relatório '%s' foi gerado com sucesso!", relatorio.nome()))
                    .build();

            this.producer.publicar(this.topicNotificacao, notification);

        } catch (Exception e) {
            log.error("Erro ao processar notificação de relatório concluído: {}", e.getMessage(), e);
        }
    }
}