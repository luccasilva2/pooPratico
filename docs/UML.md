# Diagrama de Classes UML

> Versão Mermaid (renderiza direto no GitHub/VS Code). Versão PlantUML equivalente em [`diagrama-classes.puml`](diagrama-classes.puml) — pode ser exportada como PNG em https://www.plantuml.com/plantuml.

**Legenda de visibilidade:** `+` público · `-` privado · `#` protegido · `*` abstrato · `$` estático

```mermaid
classDiagram
    direction TB

    class Pagamento {
        <<abstract>>
        -int id
        -Cliente cliente
        -double valor
        -LocalDateTime dataCriacao
        -StatusPagamento status
        -String motivoRecusa
        +getTipo()* String
        +calcularTaxa()* double
        #validarRegrasEspecificas()* String
        #getDetalhesEspecificos()* String
        +processar() void
        #aoAprovar() void
        +getValorTotal() double
        +exibirResumo() String
        #marcarComoAutenticado() void
        #recusar(String motivo) void
        #isAutenticado() boolean
    }

    class Autenticavel {
        <<interface>>
        +autenticar() boolean
    }

    class Notificavel {
        <<interface>>
        +gerarMensagemNotificacao() String
        +notificar() void
    }

    class CartaoCredito {
        -String numeroCartao
        -String nomeTitular
        -YearMonth validade
        -String cvv
        -int parcelas
        -double limiteDisponivel
        +calcularTaxa() double
        +autenticar() boolean
        +notificar() void
        +getValorParcela() double
        -numeroValidoPorLuhn() boolean
    }

    class Pix {
        -String chave
        -TipoChavePix tipoChave
        -LocalTime horario
        -String codigoTransacao
        +calcularTaxa() double
        +autenticar() boolean
        +notificar() void
        -isPeriodoNoturno() boolean
    }

    class Boleto {
        -String linhaDigitavel
        -LocalDate vencimento
        -LocalDate dataPagamento
        +calcularTaxa() double
        +notificar() void
        +getDiasAtraso() long
        +isVencido() boolean
    }

    class CarteiraDigital {
        <<novo tipo>>
        -String provedor
        -String identificadorConta
        -double saldo
        -String tokenDispositivo
        -String tokenInformado
        +calcularTaxa() double
        +autenticar() boolean
    }

    class Cliente {
        -String nome
        -String email
        -String telefone
    }

    class StatusPagamento {
        <<enumeration>>
        PENDENTE
        AUTENTICADO
        APROVADO
        RECUSADO
    }

    class TipoChavePix {
        <<enumeration>>
        CPF
        CNPJ
        EMAIL
        TELEFONE
        ALEATORIA
        +formatoValido(String chave) boolean
    }

    class ProcessadorPagamentos {
        -List~Pagamento~ historico
        +processarLote(List~Pagamento~ lote) void
        +exibirRelatorio() void
    }

    class Main {
        +main(String[] args)$ void
    }

    Pagamento <|-- CartaoCredito
    Pagamento <|-- Pix
    Pagamento <|-- Boleto
    Pagamento <|-- CarteiraDigital

    Autenticavel <|.. CartaoCredito
    Autenticavel <|.. Pix
    Autenticavel <|.. CarteiraDigital
    Notificavel <|.. CartaoCredito
    Notificavel <|.. Pix
    Notificavel <|.. Boleto

    Pagamento "*" --> "1" Cliente : pertence a
    Pagamento --> StatusPagamento
    Pix --> TipoChavePix
    ProcessadorPagamentos o--> "*" Pagamento : historico
    ProcessadorPagamentos ..> Autenticavel : usa
    ProcessadorPagamentos ..> Notificavel : usa
    Main ..> ProcessadorPagamentos : usa
```

## Resumo dos relacionamentos

| Relacionamento | Tipo UML | Classes |
|---|---|---|
| Herança | Generalização (seta cheia) | `CartaoCredito`, `Pix`, `Boleto`, `CarteiraDigital` → `Pagamento` |
| Implementação | Realização (seta tracejada) | `CartaoCredito`, `Pix`, `CarteiraDigital` → `Autenticavel` |
| Implementação | Realização (seta tracejada) | `CartaoCredito`, `Pix`, `Boleto` → `Notificavel` |
| Associação | `Pagamento` → `Cliente` | Todo pagamento pertence a um cliente |
| Agregação | `ProcessadorPagamentos` ◇→ `Pagamento` | O processador guarda o histórico de pagamentos |
| Dependência | `ProcessadorPagamentos` ⇢ interfaces | Usa as interfaces para autenticar/notificar |

## Matriz de interfaces

| Tipo | Autenticavel | Notificavel |
|---|:---:|:---:|
| Cartão de Crédito | ✅ | ✅ |
| PIX | ✅ | ✅ |
| Boleto | ❌ | ✅ |
| Carteira Digital | ✅ | ❌ |
