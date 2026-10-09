package pagamentos.modelo;

/**
 * Tipos de chave PIX, cada um com sua expressão regular de validação de formato.
 */
public enum TipoChavePix {
    CPF("CPF", "\\d{11}"),
    CNPJ("CNPJ", "\\d{14}"),
    EMAIL("E-mail", "^[\\w.+-]+@[\\w-]+\\.[\\w.]+$"),
    TELEFONE("Telefone", "^\\+55\\d{10,11}$"),
    ALEATORIA("Chave aleatória", "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

    private final String descricao;
    private final String regex;

    TipoChavePix(String descricao, String regex) {
        this.descricao = descricao;
        this.regex = regex;
    }

    public boolean formatoValido(String chave) {
        return chave != null && chave.matches(regex);
    }

    public String getDescricao() {
        return descricao;
    }
}
