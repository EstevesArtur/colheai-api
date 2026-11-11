/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.dto;

import java.time.LocalDate;

// Dados da proposta que a API devolve. O valor nao existe no banco (seria um
// dado derivado): o controller preenche com Proposta.calcularValor().
public class PropostaResponseDto {

    private int id;

    private double quantidade;

    private double valor;

    private LocalDate dataEnvio;

    private String status;

    private LocalDate dataContrato;

    private ItemChamadaResponseDto item;

    private AgricultorResponseDto agricultor;

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

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
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

    public ItemChamadaResponseDto getItem() {
        return item;
    }

    public void setItem(ItemChamadaResponseDto item) {
        this.item = item;
    }

    public AgricultorResponseDto getAgricultor() {
        return agricultor;
    }

    public void setAgricultor(AgricultorResponseDto agricultor) {
        this.agricultor = agricultor;
    }

}
