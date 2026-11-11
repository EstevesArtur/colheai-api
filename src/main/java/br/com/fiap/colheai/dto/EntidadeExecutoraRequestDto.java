/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// Dados que o cliente envia para cadastrar ou atualizar uma EEx (Entidade Executora).
public class EntidadeExecutoraRequestDto {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
    private String nome;

    @NotBlank(message = "Município é obrigatório")
    @Size(max = 60, message = "Município deve ter no máximo 60 caracteres")
    private String municipio;

    @NotBlank(message = "UF é obrigatória")
    @Pattern(regexp = "[A-Z]{2}", message = "UF deve ter 2 letras maiúsculas (ex.: SP)")
    private String uf;

    // codigo do municipio no IBGE: liga a EEx aos dados publicos do FNDE
    @Min(value = 1000000, message = "Código IBGE deve ter 7 dígitos")
    @Max(value = 9999999, message = "Código IBGE deve ter 7 dígitos")
    private int codigoIbge;

    @Positive(message = "Repasse anual deve ser maior que zero")
    private double repasseAnual;

    @Min(value = 2026, message = "A meta de 45% vale a partir de 2026")
    @Max(value = 2099, message = "Ano do repasse inválido")
    private int anoRepasse;

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
