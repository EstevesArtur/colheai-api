/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Dados para enviar uma proposta (POST /itens/{id}/propostas). O item vem do
// caminho da URL; o agricultor vai como objeto: {"agricultor": {"id": 9}}.
// Data de envio e status (ENVIADA) sao definidos pela API.
public class PropostaRequestDto {

    @NotNull(message = "Agricultor é obrigatório")
    private AgricultorRequestDto agricultor;

    @Positive(message = "Quantidade deve ser maior que zero")
    private double quantidade;

    public AgricultorRequestDto getAgricultor() {
        return agricultor;
    }

    public void setAgricultor(AgricultorRequestDto agricultor) {
        this.agricultor = agricultor;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

}
