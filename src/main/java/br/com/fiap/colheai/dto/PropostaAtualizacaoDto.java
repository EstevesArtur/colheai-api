/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

// Dados para atualizar uma proposta (PUT /propostas/{id}). Item e agricultor nao
// mudam: muda a quantidade e o andamento (CONTRATADA quando a EEx contrata,
// RECUSADA quando nao contrata). Por isso e um DTO separado do de cadastro.
public class PropostaAtualizacaoDto {

    @Positive(message = "Quantidade deve ser maior que zero")
    private double quantidade;

    @NotBlank(message = "Status é obrigatório")
    @Pattern(regexp = "ENVIADA|CONTRATADA|RECUSADA", message = "Status deve ser ENVIADA, CONTRATADA ou RECUSADA")
    private String status;

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
