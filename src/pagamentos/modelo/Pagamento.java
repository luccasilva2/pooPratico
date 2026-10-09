package pagamentos.modelo;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Representa a ideia GENÉRICA de um pagamento (abstração).
 *
 * <p>Não pode ser instanciada diretamente: ninguém paga com "um pagamento",
 * paga-se com cartão, PIX, boleto, carteira digital...</p>
 *
 * <p>Contém:</p>
 * <ul>
 *   <li>Dados comuns a todos os pagamentos (id, cliente, valor, data, status);</li>
 *   <li>Comportamentos comuns (processar, calcular valor total, exibir resumo);</li>
 *   <li>Comportamentos abstratos que cada tipo concreto implementa à sua maneira
 *       (calcularTaxa, getTipo, validarRegrasEspecificas, getDetalhesEspecificos).</li>
 * </ul>
 */
public abstract class Pagamento {

    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static int contadorIds = 0;

    // ---------- Dados comuns (encapsulados: private + getters, sem setters) ----------
    private final int id;
    private final Cliente cliente;
    private final double valor;
    private final LocalDateTime dataCriacao;
    private StatusPagamento status;
    private String motivoRecusa;

    protected Pagamento(Cliente cliente, double valor) {
        if (cliente == null) {
            throw new IllegalArgumentException("O cliente é obrigatório.");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor do pagamento deve ser maior que zero.");
        }
        this.id = ++contadorIds;
        this.cliente = cliente;
        this.valor = valor;
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusPagamento.PENDENTE;
    }

    // ======================= COMPORTAMENTOS ABSTRATOS =======================

    /** Nome legível do tipo de pagamento. */
    public abstract String getTipo();

    /** Cada modalidade possui sua própria regra de cálculo de taxa. */
    public abstract double calcularTaxa();

    /**
     * Regras de negócio específicas da modalidade, verificadas no processamento.
     *
     * @return {@code null} se o pagamento pode ser aprovado, ou o motivo da recusa.
     */
    protected abstract String validarRegrasEspecificas();

    /** Informações específicas do tipo, usadas no resumo. */
    protected abstract String getDetalhesEspecificos();

    // ========================= COMPORTAMENTOS COMUNS =========================

    /**
     * Fluxo de processamento comum a TODOS os pagamentos (Template Method).
     * O "esqueleto" é fixo (final); os passos variáveis são delegados às subclasses.
     */
    public final void processar() {
        if (status == StatusPagamento.APROVADO || status == StatusPagamento.RECUSADO) {
            return; // pagamento já finalizado, não processa duas vezes
        }
        String motivo = validarRegrasEspecificas();
        if (motivo != null) {
            recusar(motivo);
            return;
        }
        aoAprovar();
        this.status = StatusPagamento.APROVADO;
    }

    /**
     * Gancho (hook) executado quando o pagamento é aprovado.
     * Por padrão não faz nada; subclasses podem sobrescrever (ex.: debitar saldo/limite).
     */
    protected void aoAprovar() {
        // comportamento padrão vazio
    }

    /** Valor final pago pelo cliente: valor + taxa. */
    public double getValorTotal() {
        return arredondar(valor + calcularTaxa());
    }

    /** Resumo do pagamento: parte comum + parte específica (polimórfica). */
    public String exibirResumo() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  #%d | %s | Cliente: %s | Criado em: %s%n",
                id, getTipo(), cliente.getNome(), dataCriacao.format(FORMATO_DATA)));
        sb.append(String.format("  Valor: %s | Taxa: %s | Total: %s%n",
                formatarMoeda(valor), formatarMoeda(calcularTaxa()), formatarMoeda(getValorTotal())));
        sb.append("  ").append(getDetalhesEspecificos()).append(System.lineSeparator());
        sb.append("  Status: ").append(status.getDescricao());
        if (status == StatusPagamento.RECUSADO) {
            sb.append(" -> Motivo: ").append(motivoRecusa);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("%s #%d (%s)", getTipo(), id, status.getDescricao());
    }

    // ============ Alteração de estado: só a própria hierarquia pode fazer ============

    protected final void marcarComoAutenticado() {
        if (status == StatusPagamento.PENDENTE) {
            this.status = StatusPagamento.AUTENTICADO;
        }
    }

    protected final void recusar(String motivo) {
        this.status = StatusPagamento.RECUSADO;
        this.motivoRecusa = motivo;
    }

    protected final boolean isAutenticado() {
        return status == StatusPagamento.AUTENTICADO;
    }

    // ================================ Utilitários ================================

    protected static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    public static String formatarMoeda(double valor) {
        // O formato pt-BR usa espaço não separável (U+00A0) após "R$", que aparece
        // como "R$?" em alguns consoles do Windows; trocamos por espaço comum.
        return MOEDA.format(valor).replace('\u00A0', ' ').replace('\u202F', ' ');
    }

    // ================================== Getters ==================================

    public int getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public double getValor() {
        return valor;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public String getMotivoRecusa() {
        return motivoRecusa;
    }
}
