/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Dados para cadastrar ou atualizar um agricultor. O id so e usado quando o
// agricultor e informado dentro de uma proposta: {"agricultor": {"id": 9}}.
public class AgricultorRequestDto {

    private int id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
    private String nome;

    // Cadastro Nacional da Agricultura Familiar: sem ele nao se vende para o PNAE
    @NotBlank(message = "CAF é obrigatório")
    @Size(max = 25, message = "CAF deve ter no máximo 25 caracteres")
    private String caf;

    @NotBlank(message = "Tipo de fornecedor é obrigatório")
    @Pattern(regexp = "INDIVIDUAL|GRUPO_INFORMAL|GRUPO_FORMAL", message = "Tipo deve ser INDIVIDUAL, GRUPO_INFORMAL ou GRUPO_FORMAL")
    private String tipoFornecedor;

    @Min(value = 1, message = "Quantidade de famílias deve ser no mínimo 1")
    @Max(value = 9999, message = "Quantidade de famílias deve ser no máximo 9999")
    private int quantidadeFamilias;

    @NotBlank(message = "Município é obrigatório")
    @Size(max = 60, message = "Município deve ter no máximo 60 caracteres")
    private String municipio;

    @NotBlank(message = "UF é obrigatória")
    @Pattern(regexp = "[A-Z]{2}", message = "UF deve ter 2 letras maiúsculas (ex.: SP)")
    private String uf;

    // opcional
    @Size(max = 60, message = "Distrito deve ter no máximo 60 caracteres")
    private String distrito;

    private boolean organico;

    // opcional: so serve para o alerta de chamada aberta (LGPD: o minimo necessario)
    @Pattern(regexp = "\\d{10,11}", message = "Telefone deve ter 10 ou 11 dígitos, só números, com DDD")
    private String telefone;

    // Individual tem 1 familia; grupo (formal ou informal) tem 2 ou mais.
    // E a mesma regra do CHECK ck_agricultor_familia do banco, validada antes
    // para o cliente receber uma mensagem clara (400) em vez de um erro do Oracle.
    @AssertTrue(message = "INDIVIDUAL tem 1 família; grupo formal ou informal tem 2 ou mais")
    public boolean isFamiliasCoerentes() {
        if (tipoFornecedor == null)
            return true;
        if (tipoFornecedor.equals("INDIVIDUAL"))
            return quantidadeFamilias == 1;
        return quantidadeFamilias >= 2;
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

    public String getCaf() {
        return caf;
    }

    public void setCaf(String caf) {
        this.caf = caf;
    }

    public String getTipoFornecedor() {
        return tipoFornecedor;
    }

    public void setTipoFornecedor(String tipoFornecedor) {
        this.tipoFornecedor = tipoFornecedor;
    }

    public int getQuantidadeFamilias() {
        return quantidadeFamilias;
    }

    public void setQuantidadeFamilias(int quantidadeFamilias) {
        this.quantidadeFamilias = quantidadeFamilias;
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

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public boolean isOrganico() {
        return organico;
    }

    public void setOrganico(boolean organico) {
        this.organico = organico;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

}
