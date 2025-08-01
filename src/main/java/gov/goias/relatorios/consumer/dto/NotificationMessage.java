package gov.goias.relatorios.consumer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private String id;
    private String codgUsuario;
    private NotificationType type;
    private String title;
    private String message;
    private Object data;
}

