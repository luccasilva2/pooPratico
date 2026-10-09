package pagamentos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import pagamentos.interfaces.Autenticavel;
import pagamentos.interfaces.Notificavel;
import pagamentos.modelo.Boleto;
import pagamentos.modelo.CartaoCredito;
import pagamentos.modelo.CarteiraDigital;
import pagamentos.modelo.Cliente;
import pagamentos.modelo.Pagamento;
import pagamentos.modelo.Pix;
import pagamentos.modelo.TipoChavePix;
import pagamentos.servico.ProcessadorPagamentos;

/**
 * Programa principal interativo com menu e switch.
 *
 * <p>Permite ao usuário escolher quais pagamentos cadastrar e processar,
 * ver relatório ou rodar a demonstração completa.</p>
 *
 * <p>Grupo: Lucca Silva Oliveira, Gustavo Lange do Nascimento,
 * Guilherme Agnes e Pedro Henrique Marciniak.</p>
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final ProcessadorPagamentos processador = new ProcessadorPagamentos();
    private static Cliente clienteAtual = new Cliente("Lucca Silva Oliveira", "lucca@email.com", "(41) 99999-0001");

    public static void main(String[] args) {
        boolean rodando = true;

        while (rodando) {
            exibirMenu();
            System.out.print("Escolha uma opção: ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String opcao = scanner.nextLine().trim();
            if (opcao.isEmpty()) {
                continue;
            }

            switch (opcao) {
                case "1":
                    criarCartaoCredito();
                    break;
                case "2":
                    criarPix();
                    break;
                case "3":
                    criarBoleto();
                    break;
                case "4":
                    criarCarteiraDigital();
                    break;
                case "5":
                    processarDemonstracaoCompleta();
                    break;
                case "6":
                    demonstrarInterfaces(processador.getHistorico());
                    break;
                case "7":
                    processador.exibirRelatorio();
                    break;
                case "8":
                    alterarCliente();
                    break;
                case "0":
                    System.out.println("\nEncerrando o sistema. Até logo!");
                    rodando = false;
                    break;
                default:
                    System.out.println("\n[!] Opção inválida! Escolha um número do menu.");
                    break;
            }

            if (rodando) {
                System.out.println("\nPressione ENTER para continuar...");
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }
            }
        }
    }

    private static void exibirMenu() {
        System.out.println();
        System.out.println("====================================================================================");
        System.out.println("             SISTEMA DE PROCESSAMENTO DE PAGAMENTOS — MENU PRINCIPAL                ");
        System.out.println("====================================================================================");
        System.out.println(" Cliente atual: " + clienteAtual.getNome() + " (" + clienteAtual.getEmail() + ")");
        System.out.println("------------------------------------------------------------------------------------");
        System.out.println(" 1 - Processar Cartão de Crédito");
        System.out.println(" 2 - Processar PIX");
        System.out.println(" 3 - Processar Boleto Bancário");
        System.out.println(" 4 - Processar Carteira Digital (Novo Tipo)");
        System.out.println(" 5 - Executar Cenário de Demonstração Completo (11 casos de teste pré-definidos)");
        System.out.println(" 6 - Demonstrar Interfaces (Autenticavel e Notificavel no histórico)");
        System.out.println(" 7 - Exibir Relatório Consolidado de Pagamentos");
        System.out.println(" 8 - Alterar Dados do Cliente");
        System.out.println(" 0 - Sair");
        System.out.println("====================================================================================");
    }

    // -------------------------------------------------------------------------
    // CADASTRO E PROCESSAMENTO INDIVIDUAL
    // -------------------------------------------------------------------------

    private static void criarCartaoCredito() {
        titulo("NOVO PAGAMENTO - CARTÃO DE CRÉDITO");
        try {
            double valor = lerDouble("Valor da compra (ex: 250.00): R$ ");
            System.out.print("Número do cartão (16 dígitos, ex: 4111111111111111): ");
            String numero = scanner.nextLine().trim();
            System.out.print("Nome no cartão: ");
            String titular = scanner.nextLine().trim();
            YearMonth validade = lerValidadeCartao();
            System.out.print("CVV (3 dígitos): ");
            String cvv = scanner.nextLine().trim();
            int parcelas = lerInt("Quantidade de parcelas (1 a " + CartaoCredito.MAX_PARCELAS + "): ", 1, CartaoCredito.MAX_PARCELAS);
            double limite = lerDouble("Limite disponível no cartão: R$ ");

            Pagamento cartao = new CartaoCredito(clienteAtual, valor, numero, titular, validade, cvv, parcelas, limite);
            processador.processarLote(Collections.singletonList(cartao));
        } catch (Exception e) {
            System.out.println("[!] Erro ao criar pagamento: " + e.getMessage());
        }
    }

    private static void criarPix() {
        titulo("NOVO PAGAMENTO - PIX");
        try {
            double valor = lerDouble("Valor do PIX: R$ ");

            System.out.println("\nTipos de chave:");
            System.out.println("  1 - CPF (11 dígitos)");
            System.out.println("  2 - CNPJ (14 dígitos)");
            System.out.println("  3 - E-mail");
            System.out.println("  4 - Telefone (+55...)");
            System.out.println("  5 - Chave Aleatória");
            System.out.print("Escolha o tipo da chave: ");
            String opChave = scanner.nextLine().trim();

            TipoChavePix tipo;
            switch (opChave) {
                case "1":
                    tipo = TipoChavePix.CPF;
                    break;
                case "2":
                    tipo = TipoChavePix.CNPJ;
                    break;
                case "3":
                    tipo = TipoChavePix.EMAIL;
                    break;
                case "4":
                    tipo = TipoChavePix.TELEFONE;
                    break;
                default:
                    tipo = TipoChavePix.ALEATORIA;
                    break;
            }

            System.out.print("Informe a chave (" + tipo.getDescricao() + "): ");
            String chave = scanner.nextLine().trim();

            Pagamento pix = new Pix(clienteAtual, valor, chave, tipo, LocalTime.now());
            processador.processarLote(Collections.singletonList(pix));
        } catch (Exception e) {
            System.out.println("[!] Erro ao criar pagamento: " + e.getMessage());
        }
    }

    private static void criarBoleto() {
        titulo("NOVO PAGAMENTO - BOLETO BANCÁRIO");
        try {
            double valor = lerDouble("Valor do boleto: R$ ");
            System.out.print("Linha digitável (47 dígitos): ");
            String linha = scanner.nextLine().trim();
            if (linha.isBlank()) {
                // Sugere uma válida se der ENTER em branco
                linha = "34191.79001 01043.510047 91020.150008 1 91070000045000";
                System.out.println("  (usando linha digitável padrão de teste: " + linha + ")");
            }

            int diasVencimento = lerInt("Vence em quantos dias a partir de hoje? (digite negativo se já venceu): ", -60, 60);
            LocalDate vencimento = LocalDate.now().plusDays(diasVencimento);

            Pagamento boleto = new Boleto(clienteAtual, valor, linha, vencimento);
            processador.processarLote(Collections.singletonList(boleto));
        } catch (Exception e) {
            System.out.println("[!] Erro ao criar pagamento: " + e.getMessage());
        }
    }

    private static void criarCarteiraDigital() {
        titulo("NOVO PAGAMENTO - CARTEIRA DIGITAL (NOVO TIPO)");
        try {
            double valor = lerDouble("Valor da transferência: R$ ");
            System.out.print("Provedor (ex: PicPay, Mercado Pago, Google Pay): ");
            String provedor = scanner.nextLine().trim();
            if (provedor.isBlank()) provedor = "PicPay";

            System.out.print("Identificador da conta/usuário (ex: @usuario): ");
            String idConta = scanner.nextLine().trim();
            if (idConta.isBlank()) idConta = "@" + clienteAtual.getNome().toLowerCase().replace(" ", ".");

            double saldo = lerDouble("Saldo disponível na carteira: R$ ");

            System.out.print("Cadastre o token do dispositivo (6 dígitos, ex: 123456): ");
            String tokenDisp = scanner.nextLine().trim();
            if (tokenDisp.isBlank()) tokenDisp = "123456";

            System.out.print("Informe o token para autenticar (6 dígitos): ");
            String tokenInf = scanner.nextLine().trim();

            Pagamento carteira = new CarteiraDigital(clienteAtual, valor, provedor, idConta, saldo, tokenDisp, tokenInf);
            processador.processarLote(Collections.singletonList(carteira));
        } catch (Exception e) {
            System.out.println("[!] Erro ao criar pagamento: " + e.getMessage());
        }
    }

    private static void alterarCliente() {
        titulo("ALTERAR CLIENTE ATUAL");
        System.out.print("Nome (" + clienteAtual.getNome() + "): ");
        String nome = scanner.nextLine().trim();
        if (nome.isBlank()) nome = clienteAtual.getNome();

        System.out.print("E-mail (" + clienteAtual.getEmail() + "): ");
        String email = scanner.nextLine().trim();
        if (email.isBlank()) email = clienteAtual.getEmail();

        System.out.print("Telefone (" + clienteAtual.getTelefone() + "): ");
        String tel = scanner.nextLine().trim();
        if (tel.isBlank()) tel = clienteAtual.getTelefone();

        clienteAtual = new Cliente(nome, email, tel);
        System.out.println("\n[✓] Cliente atualizado para: " + clienteAtual.getNome());
    }

    // -------------------------------------------------------------------------
    // CENÁRIO COMPLETO PRÉ-DEFINIDO (O ORIGINAL DO TRABALHO)
    // -------------------------------------------------------------------------

    private static void processarDemonstracaoCompleta() {
        Cliente lucca = new Cliente("Lucca Silva Oliveira", "lucca@email.com", "(41) 99999-0001");
        Cliente gustavo = new Cliente("Gustavo Lange do Nascimento", "gustavo@email.com", "(41) 99999-0002");
        Cliente guilherme = new Cliente("Guilherme Agnes", "guilherme@email.com", "(41) 99999-0003");
        Cliente pedro = new Cliente("Pedro Henrique Marciniak", "pedro@email.com", "(41) 99999-0004");

        titulo("ETAPA 1 - Cartão de Crédito, PIX e Boleto (Cenário Automático)");
        List<Pagamento> lote1 = new ArrayList<>();
        lote1.add(new CartaoCredito(lucca, 1200.00, "4111 1111 1111 1111", "LUCCA S OLIVEIRA",
                YearMonth.now().plusYears(3), "123", 6, 5000.00));
        lote1.add(new CartaoCredito(gustavo, 350.00, "4111 1111 1111 1112", "GUSTAVO L NASCIMENTO",
                YearMonth.now().plusYears(2), "456", 1, 2000.00));
        lote1.add(new CartaoCredito(guilherme, 3000.00, "5555 5555 5555 4444", "GUILHERME AGNES",
                YearMonth.now().plusYears(1), "789", 12, 3000.00));
        lote1.add(new Pix(guilherme, 80.00, "12345678901", TipoChavePix.CPF, LocalTime.of(10, 0)));
        lote1.add(new Pix(pedro, 2500.00, "loja@empresa.com.br", TipoChavePix.EMAIL, LocalTime.of(14, 30)));
        lote1.add(new Pix(lucca, 1500.00, "+5541999990001", TipoChavePix.TELEFONE, LocalTime.of(22, 15)));
        lote1.add(new Boleto(gustavo, 450.00, "34191.79001 01043.510047 91020.150008 1 91070000045000",
                LocalDate.now().plusDays(5)));
        lote1.add(new Boleto(guilherme, 800.00, "23793.38128 60000.000003 00000.000400 1 84340000080000",
                LocalDate.now().minusDays(10)));
        processador.processarLote(lote1);

        titulo("ETAPA 2 - Novo tipo: Carteira Digital (Cenário Automático)");
        List<Pagamento> lote2 = new ArrayList<>();
        lote2.add(new CarteiraDigital(pedro, 199.90, "PicPay", "@pedro.marciniak", 500.00, "482913", "482913"));
        lote2.add(new CarteiraDigital(lucca, 30.00, "Mercado Pago", "lucca@email.com", 10.00, "731045", "731045"));
        lote2.add(new CarteiraDigital(gustavo, 75.00, "Google Pay", "gustavo@gmail.com", 300.00, "120934", "999999"));
        processador.processarLote(lote2);

        System.out.println("\n[✓] Todos os 11 casos de teste foram processados com sucesso!");
    }

    // -------------------------------------------------------------------------
    // DEMONSTRAÇÃO DE INTERFACES
    // -------------------------------------------------------------------------

    private static void demonstrarInterfaces(List<Pagamento> pagamentos) {
        titulo("DEMONSTRAÇÃO DAS INTERFACES");
        if (pagamentos.isEmpty()) {
            System.out.println("Nenhum pagamento processado até o momento. Processe algum pagamento ou execute a opção 5!");
            return;
        }

        List<Autenticavel> autenticaveis = new ArrayList<>();
        List<Notificavel> notificaveis = new ArrayList<>();

        for (Pagamento p : pagamentos) {
            if (p instanceof Autenticavel) {
                autenticaveis.add((Autenticavel) p);
            }
            if (p instanceof Notificavel) {
                notificaveis.add((Notificavel) p);
            }
        }

        System.out.println("Objetos que implementam Autenticavel (" + autenticaveis.size() + "):");
        for (Autenticavel a : autenticaveis) {
            System.out.println("  - " + a);
        }

        System.out.println("\nObjetos que implementam Notificavel (" + notificaveis.size() + ") e suas mensagens:");
        for (Notificavel n : notificaveis) {
            System.out.println("  - " + n + ": " + n.gerarMensagemNotificacao());
        }
    }

    // -------------------------------------------------------------------------
    // UTILITÁRIOS DE ENTRADA
    // -------------------------------------------------------------------------

    private static double lerDouble(String label) {
        while (true) {
            System.out.print(label);
            String input = scanner.nextLine().trim().replace(",", ".");
            try {
                double val = Double.parseDouble(input);
                if (val <= 0) {
                    System.out.println("[!] O valor deve ser maior que zero.");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("[!] Valor numérico inválido. Tente novamente.");
            }
        }
    }

    private static int lerInt(String label, int min, int max) {
        while (true) {
            System.out.print(label);
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val < min || val > max) {
                    System.out.println("[!] Digite um número entre " + min + " e " + max + ".");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("[!] Número inválido. Tente novamente.");
            }
        }
    }

    private static YearMonth lerValidadeCartao() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yyyy");
        while (true) {
            System.out.print("Validade (MM/yyyy, ex: 12/2028): ");
            String input = scanner.nextLine().trim();
            try {
                return YearMonth.parse(input, fmt);
            } catch (DateTimeParseException e) {
                System.out.println("[!] Formato de validade inválido. Use MM/yyyy.");
            }
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("#".repeat(90));
        System.out.println("# " + texto);
        System.out.println("#".repeat(90));
    }
}
