/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.dto;

import java.time.LocalDate;

// Dados da chamada que a API devolve, com a EEx (Entidade Executora) dona dela.
// Dentro de um item, a chamada vem so com o id (os campos nulos nao aparecem no JSON).
public class ChamadaPublicaResponseDto {

    private int id;

    private String numeroEdital;

    private String objeto;

    private LocalDate dataAbertura;

    private LocalDate dataEncerramento;

    private String status;

    private EntidadeExecutoraResponseDto entidade;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumeroEdital() {
        return numeroEdital;
    }

    public void setNumeroEdital(String numeroEdital) {
        this.numeroEdital = numeroEdital;
    }

    public String getObjeto() {
        return objeto;
    }

    public void setObjeto(String objeto) {
        this.objeto = objeto;
    }

    public LocalDate getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDate dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public LocalDate getDataEncerramento() {
        return dataEncerramento;
    }

    public void setDataEncerramento(LocalDate dataEncerramento) {
        this.dataEncerramento = dataEncerramento;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public EntidadeExecutoraResponseDto getEntidade() {
        return entidade;
    }

    public void setEntidade(EntidadeExecutoraResponseDto entidade) {
        this.entidade = entidade;
    }

}
