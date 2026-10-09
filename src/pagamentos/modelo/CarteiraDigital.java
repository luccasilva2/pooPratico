package pagamentos.modelo;

import pagamentos.interfaces.Autenticavel;

/**
 * NOVO TIPO DE PAGAMENTO: Carteira Digital (PicPay, Mercado Pago, Google Pay...).
 *
 * <p>Foi adicionado DEPOIS dos demais sem alterar nenhuma linha da classe
 * {@link Pagamento}, das interfaces, do processador ou dos outros tipos.</p>
 *
 * <p><b>Regra de taxa:</b> 1,49% do valor, com taxa mínima de R$ 0,50.</p>
 *
 * <p><b>Autenticação:</b> o token informado pelo usuário (6 dígitos) deve ser igual
 * ao token gerado no dispositivo cadastrado (autenticação em dois fatores).</p>
 *
 * <p><b>Processamento:</b> o saldo da carteira precisa cobrir o valor total;
 * ao aprovar, o saldo é debitado.</p>
 *
 * <p><b>NÃO implementa {@code Notificavel}:</b> o próprio aplicativo da carteira
 * (provedor) já envia push ao usuário a cada transação; notificar novamente
 * pelo nosso sistema geraria mensagens duplicadas.</p>
 */
public class CarteiraDigital extends Pagamento implements Autenticavel {

    private static final double PERCENTUAL_TAXA = 0.0149;
    private static final double TAXA_MINIMA = 0.50;

    private final String provedor;
    private final String identificadorConta;
    private double saldo;
    private final String tokenDispositivo;
    private final String tokenInformado;

    public CarteiraDigital(Cliente cliente, double valor, String provedor, String identificadorConta,
                           double saldo, String tokenDispositivo, String tokenInformado) {
        super(cliente, valor);
        if (provedor == null || provedor.isBlank()) {
            throw new IllegalArgumentException("O provedor da carteira digital é obrigatório.");
        }
        this.provedor = provedor;
        this.identificadorConta = identificadorConta;
        this.saldo = saldo;
        this.tokenDispositivo = tokenDispositivo;
        this.tokenInformado = tokenInformado;
    }

    // ------------------------- Métodos abstratos implementados -------------------------

    @Override
    public String getTipo() {
        return "Carteira Digital";
    }

    @Override
    public double calcularTaxa() {
        return arredondar(Math.max(getValor() * PERCENTUAL_TAXA, TAXA_MINIMA));
    }

    @Override
    protected String validarRegrasEspecificas() {
        if (!isAutenticado()) {
            return "Carteira digital não autenticada.";
        }
        if (getValorTotal() > saldo) {
            return "Saldo insuficiente na carteira " + provedor + " (saldo: " + formatarMoeda(saldo) + ").";
        }
        return null;
    }

    @Override
    protected void aoAprovar() {
        saldo = arredondar(saldo - getValorTotal());
    }

    @Override
    protected String getDetalhesEspecificos() {
        return String.format("Provedor: %s | Conta: %s | Saldo atual: %s",
                provedor, identificadorConta, formatarMoeda(saldo));
    }

    // ---------------------------------- Autenticavel ----------------------------------

    @Override
    public boolean autenticar() {
        if (tokenInformado == null || !tokenInformado.matches("\\d{6}")) {
            recusar("Token de autenticação em formato inválido (esperado: 6 dígitos).");
            return false;
        }
        if (!tokenInformado.equals(tokenDispositivo)) {
            recusar("Token de autenticação incorreto.");
            return false;
        }
        marcarComoAutenticado();
        return true;
    }

    // ----------------------------------- Getters -----------------------------------

    public String getProvedor() {
        return provedor;
    }

    public String getIdentificadorConta() {
        return identificadorConta;
    }

    public double getSaldo() {
        return saldo;
    }
}
