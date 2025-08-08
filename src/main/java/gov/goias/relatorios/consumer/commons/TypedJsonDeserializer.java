package gov.goias.relatorios.consumer.commons;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.support.serializer.JsonDeserializer;

public class TypedJsonDeserializer<T> extends JsonDeserializer<T> {
    public TypedJsonDeserializer(Class<T> targetType) {
        super(targetType, new ObjectMapper());
        this.addTrustedPackages("gov.goias.relatorios.consumer");
        this.setRemoveTypeHeaders(true);
        this.setUseTypeHeaders(false);
    }
}
