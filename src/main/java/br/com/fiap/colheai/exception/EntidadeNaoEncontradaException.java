/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.exception;

// Lancada pelo DAO quando o id pesquisado, atualizado ou removido nao existe.
// O handler transforma em 404 (Not Found).
public class EntidadeNaoEncontradaException extends Exception {

    public EntidadeNaoEncontradaException(String msg) {
        super(msg);
    }

}
