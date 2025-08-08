package gov.goias.relatorios.consumer.commons;

import gov.goias.relatorios.consumer.solicitacaoRelatorio.entity.SolicitacaoRelatorio;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;

@Configuration
public class KafkaConsumerConfig {

    // ========== Solicitacao ==========
    @Bean
    public ConsumerFactory<String, SolicitacaoRelatorio> solicitacaoConsumerFactory(KafkaProperties kafkaProperties) {
        var props = new HashMap<String, Object>();
        props.putAll(kafkaProperties.buildConsumerProperties());

        var keyDeserializer = new StringDeserializer();
        var valueDeserializer = new ErrorHandlingDeserializer<>(new TypedJsonDeserializer<>(SolicitacaoRelatorio.class));

        return new DefaultKafkaConsumerFactory<>(props, keyDeserializer, valueDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SolicitacaoRelatorio> solicitacaoKafkaListenerFactory(
            ConsumerFactory<String, SolicitacaoRelatorio> consumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, SolicitacaoRelatorio>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 3)));
        factory.setConcurrency(3);
        return factory;
    }


}
