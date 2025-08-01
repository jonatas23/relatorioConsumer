package gov.goias.relatorios.consumer.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<Object, Object> template;

    public <T> void publicar(String topico, T dados) {
        template.send(topico, dados);
    }

}