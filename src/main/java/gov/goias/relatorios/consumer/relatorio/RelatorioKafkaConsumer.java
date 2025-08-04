package gov.goias.relatorios.consumer.relatorio;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.goias.relatorios.consumer.entity.SolicitacaoRelatorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RelatorioKafkaConsumer {

    private final SolicitacaoRelatorioService service;

    @KafkaListener(topics = "${kafka.topic.relatorio.solicitacao}", groupId = "relatorio-group")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2), exclude = {JsonProcessingException.class})
    public void consumir(@Payload SolicitacaoRelatorio solicitacaoRelatorio,
                         @Header(KafkaHeaders.RECEIVED_KEY) String key,
                         @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition) {

        log.info("Chave = {}", key);
        log.info("Particao = {}", partition);
        log.info("Evento Recebido = {}", solicitacaoRelatorio);

        this.service.atualizarStatus(solicitacaoRelatorio);
    }
}