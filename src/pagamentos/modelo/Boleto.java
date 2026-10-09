package pagamentos.modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import pagamentos.interfaces.Notificavel;

/**
 * Pagamento via boleto bancário.
 *
 * <p><b>Regra de taxa:</b> tarifa fixa de emissão de R$ 3,50. Se pago após o
 * vencimento: + multa de 2% + juros de 0,033% ao dia de atraso (≈ 1% ao mês).</p>
 *
 * <p><b>Processamento:</b> a linha digitável deve ter 47 dígitos e o boleto não pode
 * estar vencido há mais de 30 dias (nesse caso, é preciso emitir um novo).</p>
 *
 * <p><b>NÃO implementa {@code Autenticavel}:</b> um boleto é um documento de cobrança
 * que qualquer pessoa pode pagar (inclusive terceiros); não há credencial do pagador
 * a ser validada — a conferência é feita pelo banco na compensação.</p>
 *
 * <p><b>Notificação:</b> e-mail com a confirmação e o prazo de compensação.</p>
 */
public class Boleto extends Pagamento implements Notificavel {

    private static final double TARIFA_EMISSAO = 3.50;
    private static final double MULTA_ATRASO = 0.02;
    private static final double JUROS_AO_DIA = 0.00033;
    private static final int PRAZO_MAXIMO_APOS_VENCIMENTO = 30;
    private static final int TAMANHO_LINHA_DIGITAVEL = 47;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String linhaDigitavel;
    private final LocalDate vencimento;
    private final LocalDate dataPagamento;

    /** Boleto pago hoje. */
    public Boleto(Cliente cliente, double valor, String linhaDigitavel, LocalDate vencimento) {
        this(cliente, valor, linhaDigitavel, vencimento, LocalDate.now());
    }

    /** Boleto pago em uma data específica (útil para simulações). */
    public Boleto(Cliente cliente, double valor, String linhaDigitavel, LocalDate vencimento, LocalDate dataPagamento) {
        super(cliente, valor);
        if (vencimento == null || dataPagamento == null) {
            throw new IllegalArgumentException("Vencimento e data de pagamento são obrigatórios.");
        }
        this.linhaDigitavel = linhaDigitavel == null ? "" : linhaDigitavel.replaceAll("\\D", "");
        this.vencimento = vencimento;
        this.dataPagamento = dataPagamento;
    }

    // ------------------------- Métodos abstratos implementados -------------------------

    @Override
    public String getTipo() {
        return "Boleto";
    }

    @Override
    public double calcularTaxa() {
        double taxa = TARIFA_EMISSAO + calcularEncargosAtraso();
        return arredondar(taxa);
    }

    @Override
    protected String validarRegrasEspecificas() {
        if (linhaDigitavel.length() != TAMANHO_LINHA_DIGITAVEL) {
            return "Linha digitável inválida (deve conter " + TAMANHO_LINHA_DIGITAVEL + " dígitos).";
        }
        if (getDiasAtraso() > PRAZO_MAXIMO_APOS_VENCIMENTO) {
            return "Boleto vencido há mais de " + PRAZO_MAXIMO_APOS_VENCIMENTO + " dias. Solicite a emissão de um novo boleto.";
        }
        return null;
    }

    @Override
    protected String getDetalhesEspecificos() {
        String situacao = isVencido() ? "VENCIDO há " + getDiasAtraso() + " dia(s)" : "em dia";
        return String.format("Código: %s | Vencimento: %s (%s) | Encargos de atraso: %s",
                getLinhaDigitavelFormatada(), vencimento.format(FORMATO_DATA), situacao,
                formatarMoeda(calcularEncargosAtraso()));
    }

    // ---------------------------------- Notificavel ----------------------------------

    @Override
    public String gerarMensagemNotificacao() {
        switch (getStatus()) {
            case APROVADO:
                String msg = "Pagamento do boleto de " + formatarMoeda(getValorTotal())
                        + " registrado. Compensação em até 3 dias úteis.";
                if (isVencido()) {
                    msg += " Atenção: foram cobrados " + formatarMoeda(calcularEncargosAtraso())
                            + " de multa/juros por " + getDiasAtraso() + " dia(s) de atraso.";
                }
                return msg;
            case RECUSADO:
                return "Não foi possível pagar o boleto com vencimento em "
                        + vencimento.format(FORMATO_DATA) + ". Motivo: " + getMotivoRecusa();
            default:
                return "Lembrete: seu boleto vence em " + vencimento.format(FORMATO_DATA) + ".";
        }
    }

    @Override
    public void notificar() {
        System.out.println("  [E-MAIL -> " + getCliente().getEmail() + "] " + gerarMensagemNotificacao());
    }

    // ----------------------------------- Auxiliares -----------------------------------

    public long getDiasAtraso() {
        return Math.max(0, ChronoUnit.DAYS.between(vencimento, dataPagamento));
    }

    public boolean isVencido() {
        return getDiasAtraso() > 0;
    }

    private double calcularEncargosAtraso() {
        if (!isVencido()) {
            return 0.0;
        }
        double multa = getValor() * MULTA_ATRASO;
        double juros = getValor() * JUROS_AO_DIA * getDiasAtraso();
        return arredondar(multa + juros);
    }

    private String getLinhaDigitavelFormatada() {
        if (linhaDigitavel.length() != TAMANHO_LINHA_DIGITAVEL) {
            return linhaDigitavel;
        }
        String l = linhaDigitavel;
        return l.substring(0, 5) + "." + l.substring(5, 10) + " "
                + l.substring(10, 15) + "." + l.substring(15, 21) + " "
                + l.substring(21, 26) + "." + l.substring(26, 32) + " "
                + l.substring(32, 33) + " " + l.substring(33);
    }

    public String getLinhaDigitavel() {
        return linhaDigitavel;
    }

    public LocalDate getVencimento() {
        return vencimento;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }
}
