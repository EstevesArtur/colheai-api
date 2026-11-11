/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.model;

// Item de uma chamada: qual produto a EEx quer, quanto e por qual preco.
// O preco e publicado pela EEx; o agricultor nao escolhe preco, so a quantidade.
// Uma chamada tem varios itens (1:N). Tabela tb_item_chamada.
public class ItemChamada {

    private int id;
    private double quantidadeDemandada;
    private double precoUnitario;
    private ChamadaPublica chamada;
    private Produto produto;

    public ItemChamada() {}

    public ItemChamada(double quantidadeDemandada, double precoUnitario, ChamadaPublica chamada, Produto produto) {
        this.quantidadeDemandada = quantidadeDemandada;
        this.precoUnitario = precoUnitario;
        this.chamada = chamada;
        this.produto = produto;
    }

    public ItemChamada(int id, double quantidadeDemandada, double precoUnitario, ChamadaPublica chamada,
                       Produto produto) {
        this.id = id;
        this.quantidadeDemandada = quantidadeDemandada;
        this.precoUnitario = precoUnitario;
        this.chamada = chamada;
        this.produto = produto;
    }

    public ItemChamada(int id) {
        this.id = id;
    }

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

    public ChamadaPublica getChamada() {
        return chamada;
    }

    public void setChamada(ChamadaPublica chamada) {
        this.chamada = chamada;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }
}
