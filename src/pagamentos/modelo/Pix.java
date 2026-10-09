package pagamentos.modelo;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import pagamentos.interfaces.Autenticavel;
import pagamentos.interfaces.Notificavel;

/**
 * Pagamento via PIX.
 *
 * <p><b>Regra de taxa:</b> isento até R$ 100,00; acima disso, 0,99% do valor
 * com teto de R$ 10,00.</p>
 *
 * <p><b>Autenticação:</b> a chave PIX precisa ter formato válido para o seu tipo
 * (CPF, CNPJ, e-mail, telefone ou chave aleatória).</p>
 *
 * <p><b>Processamento:</b> limite noturno (regra do Banco Central) — entre 20h e 6h
 * o valor máximo por transação é de R$ 1.000,00. Ao aprovar, gera-se um
 * identificador de transação (End-to-End ID).</p>
 *
 * <p><b>Notificação:</b> comprovante enviado por e-mail.</p>
 */
public class Pix extends Pagamento implements Autenticavel, Notificavel {

    private static final double VALOR_ISENCAO = 100.00;
    private static final double PERCENTUAL_TAXA = 0.0099;
    private static final double TETO_TAXA = 10.00;
    private static final double LIMITE_NOTURNO = 1000.00;
    private static final LocalTime INICIO_PERIODO_NOTURNO = LocalTime.of(20, 0);
    private static final LocalTime FIM_PERIODO_NOTURNO = LocalTime.of(6, 0);
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final String chave;
    private final TipoChavePix tipoChave;
    private final LocalTime horario;
    private String codigoTransacao;

    /** PIX realizado agora. */
    public Pix(Cliente cliente, double valor, String chave, TipoChavePix tipoChave) {
        this(cliente, valor, chave, tipoChave, LocalTime.now());
    }

    /** PIX em um horário específico (útil para simulações). */
    public Pix(Cliente cliente, double valor, String chave, TipoChavePix tipoChave, LocalTime horario) {
        super(cliente, valor);
        if (tipoChave == null) {
            throw new IllegalArgumentException("O tipo da chave PIX é obrigatório.");
        }
        this.chave = chave;
        this.tipoChave = tipoChave;
        this.horario = horario;
    }

    // ------------------------- Métodos abstratos implementados -------------------------

    @Override
    public String getTipo() {
        return "PIX";
    }

    @Override
    public double calcularTaxa() {
        if (getValor() <= VALOR_ISENCAO) {
            return 0.0;
        }
        return arredondar(Math.min(getValor() * PERCENTUAL_TAXA, TETO_TAXA));
    }

    @Override
    protected String validarRegrasEspecificas() {
        if (!isAutenticado()) {
            return "PIX não autenticado.";
        }
        if (isPeriodoNoturno() && getValor() > LIMITE_NOTURNO) {
            return "Valor acima do limite noturno de " + formatarMoeda(LIMITE_NOTURNO)
                    + " (PIX às " + horario.format(FORMATO_HORA) + ").";
        }
        return null;
    }

    @Override
    protected void aoAprovar() {
        codigoTransacao = "E" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }

    @Override
    protected String getDetalhesEspecificos() {
        return String.format("Chave (%s): %s | Horário: %s%s",
                tipoChave.getDescricao(), chave, horario.format(FORMATO_HORA),
                codigoTransacao != null ? " | ID transação: " + codigoTransacao : "");
    }

    // ---------------------------------- Autenticavel ----------------------------------

    @Override
    public boolean autenticar() {
        if (!tipoChave.formatoValido(chave)) {
            recusar("Chave PIX inválida para o tipo " + tipoChave.getDescricao() + ".");
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
                return String.format("Comprovante PIX: %s enviado para a chave %s. ID: %s",
                        formatarMoeda(getValor()), chave, codigoTransacao);
            case RECUSADO:
                return "Seu PIX de " + formatarMoeda(getValor()) + " NÃO foi realizado. Motivo: " + getMotivoRecusa();
            default:
                return "Seu PIX está sendo processado.";
        }
    }

    @Override
    public void notificar() {
        System.out.println("  [E-MAIL -> " + getCliente().getEmail() + "] " + gerarMensagemNotificacao());
    }

    // ----------------------------------- Auxiliares -----------------------------------

    private boolean isPeriodoNoturno() {
        return !horario.isBefore(INICIO_PERIODO_NOTURNO) || horario.isBefore(FIM_PERIODO_NOTURNO);
    }

    public String getChave() {
        return chave;
    }

    public TipoChavePix getTipoChave() {
        return tipoChave;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public String getCodigoTransacao() {
        return codigoTransacao;
    }
}
