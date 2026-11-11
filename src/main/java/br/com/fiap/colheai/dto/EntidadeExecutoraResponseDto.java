/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.dto;

// Dados da EEx (Entidade Executora) que a API devolve.
public class EntidadeExecutoraResponseDto {

    private int id;

    private String nome;

    private String municipio;

    private String uf;

    private int codigoIbge;

    private double repasseAnual;

    private int anoRepasse;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public int getCodigoIbge() {
        return codigoIbge;
    }

    public void setCodigoIbge(int codigoIbge) {
        this.codigoIbge = codigoIbge;
    }

    public double getRepasseAnual() {
        return repasseAnual;
    }

    public void setRepasseAnual(double repasseAnual) {
        this.repasseAnual = repasseAnual;
    }

    public int getAnoRepasse() {
        return anoRepasse;
    }

    public void setAnoRepasse(int anoRepasse) {
        this.anoRepasse = anoRepasse;
    }

}
