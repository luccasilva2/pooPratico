package pagamentos.interfaces;

/**
 * Contrato para pagamentos que podem enviar uma notificação ao cliente.
 *
 * <p>Cada classe define o canal (SMS, e-mail...) e o conteúdo da mensagem,
 * de acordo com o tipo de pagamento e a situação ocorrida (aprovado, recusado...).</p>
 */
public interface Notificavel {

    /** Monta a mensagem adequada à situação atual do pagamento. */
    String gerarMensagemNotificacao();

    /** Envia (simula o envio) da notificação ao cliente. */
    void notificar();
}
