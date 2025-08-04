package gov.goias.relatorios.consumer.enuns;

import lombok.Getter;

@Getter
public enum TipoRelatorio {
    FOLHA_PAGAMENTO("FOLHA_PAGAMENTO", "Folha de Pagamento"),
    DEMONSTRATIVO_FINANCEIRO("DEMONSTRATIVO_FINANCEIRO", "Demonstrativo Financeiro"),
    BALANCETE("BALANCETE", "Balancete"),
    RELATORIO_ORCAMENTARIO("RELATORIO_ORCAMENTARIO", "Relatório Orçamentário"),
    PRESTACAO_CONTAS("PRESTACAO_CONTAS", "Prestação de Contas"),
    RELATORIO_PATRIMONIAL("RELATORIO_PATRIMONIAL", "Relatório Patrimonial");

    private final String codigo;
    private final String descricao;

    TipoRelatorio(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public static TipoRelatorio fromCodigo(String codigo) {
        for (TipoRelatorio tipo : values()) {
            if (tipo.codigo.equals(codigo)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de relatório não encontrado: " + codigo);
    }
}