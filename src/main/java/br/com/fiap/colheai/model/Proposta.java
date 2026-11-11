/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.model;

import java.time.LocalDate;

// Proposta do agricultor para um item da chamada (uma linha do projeto de venda).
// Quando a EEx contrata, o status vira CONTRATADA e a data do contrato e preenchida.
// Um item tem varias propostas e um agricultor tem varias propostas (dois 1:N).
// Tabela tb_proposta.
public class Proposta {

    private int id;
    private double quantidade;
    private LocalDate dataEnvio;
    private String status;
    private LocalDate dataContrato;
    private ItemChamada item;
    private Agricultor agricultor;

    public Proposta() {}

    public Proposta(double quantidade, LocalDate dataEnvio, String status, LocalDate dataContrato,
                    ItemChamada item, Agricultor agricultor) {
        this.quantidade = quantidade;
        this.dataEnvio = dataEnvio;
        this.status = status;
        this.dataContrato = dataContrato;
        this.item = item;
        this.agricultor = agricultor;
    }

    public Proposta(int id, double quantidade, LocalDate dataEnvio, String status, LocalDate dataContrato,
                    ItemChamada item, Agricultor agricultor) {
        this.id = id;
        this.quantidade = quantidade;
        this.dataEnvio = dataEnvio;
        this.status = status;
        this.dataContrato = dataContrato;
        this.item = item;
        this.agricultor = agricultor;
    }

    public Proposta(int id) {
        this.id = id;
    }

    // O valor nao fica guardado no banco (seria um dado derivado, fora da 3FN):
    // e sempre quantidade x preco publicado no item, arredondado em centavos.
    public double calcularValor() {
        return Math.round(quantidade * item.getPrecoUnitario() * 100) / 100.0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public LocalDate getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDate dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDataContrato() {
        return dataContrato;
    }

    public void setDataContrato(LocalDate dataContrato) {
        this.dataContrato = dataContrato;
    }

    public ItemChamada getItem() {
        return item;
    }

    public void setItem(ItemChamada item) {
        this.item = item;
    }

    public Agricultor getAgricultor() {
        return agricultor;
    }

    public void setAgricultor(Agricultor agricultor) {
        this.agricultor = agricultor;
    }
}
