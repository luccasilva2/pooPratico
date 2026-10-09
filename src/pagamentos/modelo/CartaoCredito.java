package pagamentos.modelo;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import pagamentos.interfaces.Autenticavel;
import pagamentos.interfaces.Notificavel;

/**
 * Pagamento com cartão de crédito, com suporte a parcelamento.
 *
 * <p><b>Regra de taxa:</b> 2,99% sobre o valor + 1,49% por parcela adicional
 * (ou seja, à vista = 2,99%; 2x = 4,48%; ... 12x = 19,38%).</p>
 *
 * <p><b>Autenticação:</b> número do cartão válido (algoritmo de Luhn),
 * cartão dentro da validade e CVV com 3 dígitos.</p>
 *
 * <p><b>Processamento:</b> o valor total (com taxa) deve caber no limite disponível;
 * ao aprovar, o limite é reduzido.</p>
 *
 * <p><b>Notificação:</b> SMS para o telefone do cliente.</p>
 */
public class CartaoCredito extends Pagamento implements Autenticavel, Notificavel {

    public static final int MAX_PARCELAS = 12;
    private static final double TAXA_BASE = 0.0299;
    private static final double TAXA_POR_PARCELA_ADICIONAL = 0.0149;
    private static final DateTimeFormatter FORMATO_VALIDADE = DateTimeFormatter.ofPattern("MM/yyyy");

    private final String numeroCartao;
    private final String nomeTitular;
    private final YearMonth validade;
    private final String cvv;
    private final int parcelas;
    private double limiteDisponivel;

    public CartaoCredito(Cliente cliente, double valor, String numeroCartao, String nomeTitular,
                         YearMonth validade, String cvv, int parcelas, double limiteDisponivel) {
        super(cliente, valor);
        if (parcelas < 1 || parcelas > MAX_PARCELAS) {
            throw new IllegalArgumentException("A quantidade de parcelas deve estar entre 1 e " + MAX_PARCELAS + ".");
        }
        this.numeroCartao = numeroCartao == null ? "" : numeroCartao.replaceAll("\\D", "");
        this.nomeTitular = nomeTitular;
        this.validade = validade;
        this.cvv = cvv;
        this.parcelas = parcelas;
        this.limiteDisponivel = limiteDisponivel;
    }

    // ------------------------- Métodos abstratos implementados -------------------------

    @Override
    public String getTipo() {
        return "Cartão de Crédito";
    }

    @Override
    public double calcularTaxa() {
        double percentual = TAXA_BASE + TAXA_POR_PARCELA_ADICIONAL * (parcelas - 1);
        return arredondar(getValor() * percentual);
    }

    @Override
    protected String validarRegrasEspecificas() {
        if (!isAutenticado()) {
            return "Cartão não autenticado.";
        }
        if (getValorTotal() > limiteDisponivel) {
            return "Limite insuficiente (disponível: " + formatarMoeda(limiteDisponivel) + ").";
        }
        return null;
    }

    @Override
    protected void aoAprovar() {
        limiteDisponivel = arredondar(limiteDisponivel - getValorTotal());
    }

    @Override
    protected String getDetalhesEspecificos() {
        return String.format("Cartão: %s | Titular: %s | Validade: %s | %dx de %s",
                getNumeroMascarado(), nomeTitular, validade.format(FORMATO_VALIDADE),
                parcelas, formatarMoeda(getValorParcela()));
    }

    // ---------------------------------- Autenticavel ----------------------------------

    @Override
    public boolean autenticar() {
        if (!numeroValidoPorLuhn()) {
            recusar("Número do cartão inválido (falha na verificação de Luhn).");
            return false;
        }
        if (validade == null || validade.isBefore(YearMonth.now())) {
            recusar("Cartão vencido.");
            return false;
        }
        if (cvv == null || !cvv.matches("\\d{3}")) {
            recusar("CVV inválido.");
            return false;
        }
        marcarComoAutenticado();
        return true;
    }

    // ---------------------------------- Notificavel ----------------------------------

    @Override
    public String gerarMensagemNotificacao() {
        switch (getStatus()) {
            case APROVADO:
                return String.format("Compra de %s aprovada no cartão %s em %dx de %s.",
                        formatarMoeda(getValorTotal()), getNumeroMascarado(), parcelas, formatarMoeda(getValorParcela()));
            case RECUSADO:
                return String.format("Compra no cartão %s RECUSADA. Motivo: %s",
                        getNumeroMascarado(), getMotivoRecusa());
            default:
                return "Sua compra no cartão " + getNumeroMascarado() + " está em análise.";
        }
    }

    @Override
    public void notificar() {
        System.out.println("  [SMS -> " + getCliente().getTelefone() + "] " + gerarMensagemNotificacao());
    }

    // ----------------------------------- Auxiliares -----------------------------------

    public double getValorParcela() {
        return arredondar(getValorTotal() / parcelas);
    }

    public String getNumeroMascarado() {
        if (numeroCartao.length() < 4) {
            return "****";
        }
        return "**** **** **** " + numeroCartao.substring(numeroCartao.length() - 4);
    }

    /** Algoritmo de Luhn: verificação padrão de números de cartão. */
    private boolean numeroValidoPorLuhn() {
        if (!numeroCartao.matches("\\d{13,19}")) {
            return false;
        }
        int soma = 0;
        boolean dobrar = false;
        for (int i = numeroCartao.length() - 1; i >= 0; i--) {
            int digito = numeroCartao.charAt(i) - '0';
            if (dobrar) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }
            soma += digito;
            dobrar = !dobrar;
        }
        return soma % 10 == 0;
    }

    public int getParcelas() {
        return parcelas;
    }

    public String getNomeTitular() {
        return nomeTitular;
    }

    public YearMonth getValidade() {
        return validade;
    }

    public double getLimiteDisponivel() {
        return limiteDisponivel;
    }
}
