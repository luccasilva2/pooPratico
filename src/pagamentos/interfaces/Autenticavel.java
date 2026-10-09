package pagamentos.interfaces;

/**
 * Contrato para pagamentos que precisam de autenticação/validação
 * ANTES de serem processados.
 *
 * <p>Cada classe que implementa define SUA forma de autenticar
 * (ex.: cartão valida número/CVV/validade; PIX valida a chave;
 * carteira digital valida o token do dispositivo).</p>
 */
public interface Autenticavel {

    /**
     * Executa a autenticação do pagamento.
     *
     * @return {@code true} se autenticado com sucesso; {@code false} caso contrário.
     */
    boolean autenticar();
}
