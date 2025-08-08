package gov.goias.relatorios.consumer.solicitacaoRelatorio.entity.enuns;

import lombok.Getter;

@Getter
public enum StatusRelatorio {
    EM_FILA("Em Fila", "Relatorio está em fila de execução"),
    AGENDADO("Agendado", "Relatório foi agendado para processamento"),
    EM_EXECUCAO("Em Execução", "Relatório está sendo processado"),
    CONCLUIDO("Concluído", "Relatório foi gerado com sucesso"),
    FALHA("Falha", "Erro durante a geração do relatório"),
    CANCELADO("Cancelado", "Relatório foi cancelado");

    private final String descricao;
    private final String detalhamento;

    StatusRelatorio(String descricao, String detalhamento) {
        this.descricao = descricao;
        this.detalhamento = detalhamento;
    }

    /**
     * Verifica se o status atual pode transicionar para o próximo status
     */
    public boolean podeTransicionarPara(StatusRelatorio proximoStatus) {
        return switch (this) {
            case AGENDADO, EM_FILA -> proximoStatus == EM_EXECUCAO || proximoStatus == CANCELADO;
            case EM_EXECUCAO -> proximoStatus == CONCLUIDO || proximoStatus == FALHA || proximoStatus == CANCELADO;
            case CONCLUIDO, FALHA, CANCELADO -> false; // Status finais
        };
    }

    /**
     * Verifica se é um status final (sem próximas transições)
     */
    public boolean isFinal() {
        return this == CONCLUIDO || this == FALHA || this == CANCELADO;
    }

    /**
     * Obtém o próximo status na sequência normal
     */
    public StatusRelatorio proximoStatus() {
        return switch (this) {
            case AGENDADO, EM_FILA -> EM_EXECUCAO;
            case EM_EXECUCAO -> CONCLUIDO;
            case CONCLUIDO, FALHA, CANCELADO -> null; // Sem próximo status
        };
    }
}