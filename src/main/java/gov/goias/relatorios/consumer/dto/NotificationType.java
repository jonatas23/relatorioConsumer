package gov.goias.relatorios.consumer.dto;

import lombok.Getter;

@Getter
public enum NotificationType {
    REPORT_SCHEDULED("REPORT_SCHEDULED", "Relatório Agendado"),
    REPORT_PROCESSING("REPORT_PROCESSING", "Relatório em Processamento"),
    REPORT_COMPLETED("REPORT_COMPLETED", "Relatório Concluído"),
    REPORT_FAILED("REPORT_FAILED", "Falha no Relatório"),
    REPORT_CANCELLED("REPORT_CANCELLED", "Relatório Cancelado"),
    SYSTEM_ERROR("SYSTEM_ERROR", "Erro do Sistema");

    private final String code;
    private final String description;

    NotificationType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}