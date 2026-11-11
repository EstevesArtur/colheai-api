/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

// Dados para cadastrar (POST /entidades/{id}/chamadas) ou atualizar
// (PUT /chamadas/{id}) uma chamada publica. A EEx vem do caminho da URL.
public class ChamadaPublicaRequestDto {

    @NotBlank(message = "Número do edital é obrigatório")
    @Size(max = 20, message = "Número do edital deve ter no máximo 20 caracteres")
    private String numeroEdital;

    @NotBlank(message = "Objeto da chamada é obrigatório")
    @Size(max = 200, message = "Objeto deve ter no máximo 200 caracteres")
    private String objeto;

    @NotNull(message = "Data de abertura é obrigatória")
    private LocalDate dataAbertura;

    @NotNull(message = "Data de encerramento é obrigatória")
    private LocalDate dataEncerramento;

    @NotBlank(message = "Status é obrigatório")
    @Pattern(regexp = "ABERTA|EM_ANALISE|HOMOLOGADA|FRACASSADA", message = "Status deve ser ABERTA, EM_ANALISE, HOMOLOGADA ou FRACASSADA")
    private String status;

    // O encerramento precisa ser depois da abertura (mesma regra do CHECK ck_chamada_periodo)
    @AssertTrue(message = "A data de encerramento deve ser depois da data de abertura")
    public boolean isPeriodoValido() {
        if (dataAbertura == null || dataEncerramento == null)
            return true;
        return dataEncerramento.isAfter(dataAbertura);
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

}
