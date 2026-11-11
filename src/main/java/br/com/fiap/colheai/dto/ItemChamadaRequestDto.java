/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Dados para incluir (POST /chamadas/{id}/itens) ou atualizar (PUT /itens/{id})
// um item da chamada. O produto vai como objeto: {"produto": {"id": 6}}.
public class ItemChamadaRequestDto {

    @NotNull(message = "Produto é obrigatório")
    private ProdutoRequestDto produto;

    @Positive(message = "Quantidade demandada deve ser maior que zero")
    private double quantidadeDemandada;

    // preco de aquisicao publicado pela EEx: o agricultor nao escolhe preco
    @Positive(message = "Preço unitário deve ser maior que zero")
    private double precoUnitario;

    public ProdutoRequestDto getProduto() {
        return produto;
    }

    public void setProduto(ProdutoRequestDto produto) {
        this.produto = produto;
    }

    public double getQuantidadeDemandada() {
        return quantidadeDemandada;
    }

    public void setQuantidadeDemandada(double quantidadeDemandada) {
        this.quantidadeDemandada = quantidadeDemandada;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(double precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

}
