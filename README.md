# Sistema de Processamento de Pagamentos — POO

Trabalho prático de Programação Orientada a Objetos (compatível com Java 11 a 21).

**Grupo:** Lucca Silva Oliveira, Gustavo Lange do Nascimento, Guilherme Agnes, Pedro Henrique Marciniak

## Como executar

**Windows:** clique duas vezes em `executar.bat`.

**Terminal (qualquer SO):**
```bash
javac -encoding UTF-8 -d out src/pagamentos/*.java src/pagamentos/*/*.java
java -cp out pagamentos.Main
```

## Entregáveis

| Item | Arquivo |
|---|---|
| Código-fonte | [`src/pagamentos`](src/pagamentos) |
| Diagrama UML | [`docs/UML.md`](docs/UML.md) (Mermaid) e [`docs/diagrama-classes.puml`](docs/diagrama-classes.puml) (PlantUML) |
| Relatório | [`docs/RELATORIO.md`](docs/RELATORIO.md) |

## Estrutura

```
src/pagamentos/
├── Main.java
├── interfaces/   Autenticavel, Notificavel
├── modelo/       Pagamento (abstrata), CartaoCredito, Pix, Boleto, CarteiraDigital,
│                 Cliente, StatusPagamento, TipoChavePix
└── servico/      ProcessadorPagamentos
```
