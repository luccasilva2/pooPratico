package pagamentos.modelo;

/**
 * Cliente que realiza o pagamento. Seus dados de contato são usados nas notificações.
 */
public class Cliente {

    private final String nome;
    private final String email;
    private final String telefone;

    public Cliente(String nome, String email, String telefone) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do cliente é obrigatório.");
        }
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    @Override
    public String toString() {
        return nome;
    }
}
