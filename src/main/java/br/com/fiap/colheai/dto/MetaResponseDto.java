/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.dto;

// Painel da meta de 45% (GET /entidades/{id}/meta): quanto a EEx ja contratou
// da agricultura familiar no ano e quanto falta para cumprir a Lei 15.226/2025.
public class MetaResponseDto {

    private EntidadeExecutoraResponseDto entidade;

    private int ano;

    private double repasse;

    // meta = 45% do repasse
    private double meta;

    private double contratado;

    // percentual do repasse ja contratado da agricultura familiar
    private double percentualContratado;

    private double falta;

    private boolean metaAtingida;

    public EntidadeExecutoraResponseDto getEntidade() {
        return entidade;
    }

    public void setEntidade(EntidadeExecutoraResponseDto entidade) {
        this.entidade = entidade;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public double getRepasse() {
        return repasse;
    }

    public void setRepasse(double repasse) {
        this.repasse = repasse;
    }

    public double getMeta() {
        return meta;
    }

    public void setMeta(double meta) {
        this.meta = meta;
    }

    public double getContratado() {
        return contratado;
    }

    public void setContratado(double contratado) {
        this.contratado = contratado;
    }

    public double getPercentualContratado() {
        return percentualContratado;
    }

    public void setPercentualContratado(double percentualContratado) {
        this.percentualContratado = percentualContratado;
    }

    public double getFalta() {
        return falta;
    }

    public void setFalta(double falta) {
        this.falta = falta;
    }

    public boolean isMetaAtingida() {
        return metaAtingida;
    }

    public void setMetaAtingida(boolean metaAtingida) {
        this.metaAtingida = metaAtingida;
    }

}
