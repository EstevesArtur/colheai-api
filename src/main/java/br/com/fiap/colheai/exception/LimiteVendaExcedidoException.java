/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.exception;

// Regra principal do ColheAi: pelo PNAE, cada CAF vende no maximo R$ 40 mil
// por ano civil para a mesma EEx (Entidade Executora); para grupo, o teto e
// R$ 40 mil vezes o numero de familias. E uma regra de negocio, entao herda de
// RegraNegocioException e tambem vira 409.
public class LimiteVendaExcedidoException extends RegraNegocioException {

    public LimiteVendaExcedidoException(String msg) {
        super(msg);
    }

}
