package pagamentos.modelo;

/**
 * Ciclo de vida de um pagamento.
 */
public enum StatusPagamento {
    PENDENTE("Pendente"),
    AUTENTICADO("Autenticado"),
    APROVADO("Aprovado"),
    RECUSADO("Recusado");

    private final String descricao;

    StatusPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
