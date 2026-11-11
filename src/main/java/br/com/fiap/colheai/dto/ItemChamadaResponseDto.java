/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.dto;

// Dados do item da chamada que a API devolve, com o produto completo.
public class ItemChamadaResponseDto {

    private int id;

    private double quantidadeDemandada;

    private double precoUnitario;

    private ChamadaPublicaResponseDto chamada;

    private ProdutoResponseDto produto;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public ChamadaPublicaResponseDto getChamada() {
        return chamada;
    }

    public void setChamada(ChamadaPublicaResponseDto chamada) {
        this.chamada = chamada;
    }

    public ProdutoResponseDto getProduto() {
        return produto;
    }

    public void setProduto(ProdutoResponseDto produto) {
        this.produto = produto;
    }

}
