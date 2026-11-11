/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.exception;

// Lancada quando o pedido e valido, mas fere uma regra do PNAE ou do ColheAi
// (ex.: proposta para chamada que ja fechou, CAF repetido).
// O handler transforma em 409 (Conflict).
public class RegraNegocioException extends Exception {

    public RegraNegocioException(String msg) {
        super(msg);
    }

}
