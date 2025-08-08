package gov.goias.relatorios.consumer.solicitacaoRelatorio.entity.enuns;

import lombok.Getter;

@Getter
public enum TipoRelatorio {
    FOLHA_PAGAMENTO("FOLHA_PAGAMENTO", "Folha de Pagamento"),
    DESPESAS_ORCAMENTARIAS("DESPESAS_ORCAMENTARIAS", "Despesas Orçamentárias"),
    RECEITAS_TRIBUTARIAS("RECEITAS_TRIBUTARIAS", "Receitas Tributárias"),
    BALANCO_PATRIMONIAL("BALANCO_PATRIMONIAL", "Balanço Patrimonial"),
    DEMONSTRATIVO_RESULTADOS("DEMONSTRATIVO_RESULTADOS", "Demonstrativo de Resultados"),
    EXECUCAO_ORCAMENTARIA("EXECUCAO_ORCAMENTARIA", "Execução Orçamentária"),
    POSICAO_FINANCEIRA("POSICAO_FINANCEIRA", "Posição Financeira");

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
