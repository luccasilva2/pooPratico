package pagamentos.servico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pagamentos.interfaces.Autenticavel;
import pagamentos.interfaces.Notificavel;
import pagamentos.modelo.Pagamento;
import pagamentos.modelo.StatusPagamento;

/**
 * Processa qualquer pagamento de forma UNIFORME, usando polimorfismo.
 *
 * <p>Esta classe NUNCA pergunta se o pagamento é Cartão, PIX, Boleto ou Carteira.
 * Ela só conhece:</p>
 * <ul>
 *   <li>a abstração {@link Pagamento} (o que todo pagamento sabe fazer);</li>
 *   <li>as capacidades {@link Autenticavel} e {@link Notificavel}
 *       (o que ALGUNS pagamentos sabem fazer).</li>
 * </ul>
 * <p>Assim, novos tipos de pagamento funcionam aqui sem nenhuma alteração.</p>
 */
public class ProcessadorPagamentos {

    private static final String LINHA = "-".repeat(100);

    private final List<Pagamento> historico = new ArrayList<>();

    /** Processa um lote de pagamentos com um ÚNICO laço, sem verificar o tipo concreto. */
    public void processarLote(List<Pagamento> lote) {
        for (Pagamento pagamento : lote) {
            System.out.println(LINHA);
            System.out.println("Processando: " + pagamento.getTipo() + " #" + pagamento.getId());

            // 1) Autenticação — só para quem possui essa capacidade
            if (pagamento instanceof Autenticavel) {
                Autenticavel autenticavel = (Autenticavel) pagamento;
                boolean autenticado = autenticavel.autenticar(); // cada classe autentica do seu jeito
                System.out.println("  Autenticação: " + (autenticado ? "OK" : "FALHOU"));
            } else {
                System.out.println("  Autenticação: não exigida para este tipo de pagamento");
            }

            // 2) Processamento — polimorfismo: taxa e regras dependem do tipo concreto
            pagamento.processar();
            System.out.println(pagamento.exibirResumo());

            // 3) Notificação — só para quem possui essa capacidade
            if (pagamento instanceof Notificavel) {
                Notificavel notificavel = (Notificavel) pagamento;
                notificavel.notificar(); // cada classe escolhe canal e mensagem
            }

            historico.add(pagamento);
        }
        System.out.println(LINHA);
    }

    /** Relatório consolidado de tudo que já foi processado. */
    public void exibirRelatorio() {
        System.out.println();
        System.out.println("=".repeat(100));
        System.out.println("RELATÓRIO FINAL");
        System.out.println("=".repeat(100));
        System.out.printf("%-4s %-20s %-24s %14s %12s %14s  %-10s%n",
                "ID", "Tipo", "Cliente", "Valor", "Taxa", "Total", "Status");

        double totalAprovado = 0;
        double totalTaxas = 0;
        int aprovados = 0;

        for (Pagamento p : historico) {
            System.out.printf("%-4d %-20s %-24s %14s %12s %14s  %-10s%n",
                    p.getId(), p.getTipo(), abreviar(p.getCliente().getNome(), 24),
                    Pagamento.formatarMoeda(p.getValor()),
                    Pagamento.formatarMoeda(p.calcularTaxa()),
                    Pagamento.formatarMoeda(p.getValorTotal()),
                    p.getStatus().getDescricao());

            if (p.getStatus() == StatusPagamento.APROVADO) {
                aprovados++;
                totalAprovado += p.getValorTotal();
                totalTaxas += p.calcularTaxa();
            }
        }

        System.out.println("-".repeat(100));
        System.out.printf("Pagamentos aprovados: %d de %d%n", aprovados, historico.size());
        System.out.println("Total movimentado (aprovados): " + Pagamento.formatarMoeda(totalAprovado));
        System.out.println("Total arrecadado em taxas:     " + Pagamento.formatarMoeda(totalTaxas));
        System.out.println("=".repeat(100));
    }

    public List<Pagamento> getHistorico() {
        return Collections.unmodifiableList(historico);
    }

    private static String abreviar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max - 3) + "...";
    }
}
