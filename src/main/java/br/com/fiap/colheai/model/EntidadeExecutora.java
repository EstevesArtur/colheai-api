/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.model;

// EEx (Entidade Executora): a prefeitura que recebe o repasse federal do PNAE
// e compra a merenda. Tabela tb_entidade_executora.
public class EntidadeExecutora {

    // Desde 2026 (Lei 15.226/2025), 45% do repasse tem de ir para a agricultura familiar
    private static final double PERCENTUAL_META = 0.45;

    private int id;
    private String nome;
    private String municipio;
    private String uf;
    private int codigoIbge;
    private double repasseAnual;
    private int anoRepasse;

    public EntidadeExecutora() {}

    public EntidadeExecutora(String nome, String municipio, String uf, int codigoIbge,
                             double repasseAnual, int anoRepasse) {
        this.nome = nome;
        this.municipio = municipio;
        this.uf = uf;
        this.codigoIbge = codigoIbge;
        this.repasseAnual = repasseAnual;
        this.anoRepasse = anoRepasse;
    }

    public EntidadeExecutora(int id, String nome, String municipio, String uf, int codigoIbge,
                             double repasseAnual, int anoRepasse) {
        this.id = id;
        this.nome = nome;
        this.municipio = municipio;
        this.uf = uf;
        this.codigoIbge = codigoIbge;
        this.repasseAnual = repasseAnual;
        this.anoRepasse = anoRepasse;
    }

    public EntidadeExecutora(int id) {
        this.id = id;
    }

    // Quanto a EEx precisa contratar da agricultura familiar no ano (45% do repasse)
    public double calcularMeta() {
        return Math.round(repasseAnual * PERCENTUAL_META * 100) / 100.0;
    }

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
