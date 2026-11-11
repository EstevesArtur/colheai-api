/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.model;

import br.com.fiap.colheai.exception.RegraNegocioException;

import java.time.LocalDate;

// Edital de chamada publica: o jeito do PNAE comprar direto da agricultura
// familiar. Uma EEx tem varias chamadas (1:N). Tabela tb_chamada_publica.
public class ChamadaPublica {

    private int id;
    private String numeroEdital;
    private String objeto;
    private LocalDate dataAbertura;
    private LocalDate dataEncerramento;
    private String status;
    private EntidadeExecutora entidade;

    public ChamadaPublica() {}

    public ChamadaPublica(String numeroEdital, String objeto, LocalDate dataAbertura, LocalDate dataEncerramento,
                          String status, EntidadeExecutora entidade) {
        this.numeroEdital = numeroEdital;
        this.objeto = objeto;
        this.dataAbertura = dataAbertura;
        this.dataEncerramento = dataEncerramento;
        this.status = status;
        this.entidade = entidade;
    }

    public ChamadaPublica(int id, String numeroEdital, String objeto, LocalDate dataAbertura,
                          LocalDate dataEncerramento, String status, EntidadeExecutora entidade) {
        this.id = id;
        this.numeroEdital = numeroEdital;
        this.objeto = objeto;
        this.dataAbertura = dataAbertura;
        this.dataEncerramento = dataEncerramento;
        this.status = status;
        this.entidade = entidade;
    }

    public ChamadaPublica(int id) {
        this.id = id;
    }

    // So da para receber proposta (ou incluir item) enquanto a chamada esta ABERTA.
    // Depois disso ela esta em analise, homologada ou fracassada.
    public void verificarAberta() throws RegraNegocioException {
        if (!"ABERTA".equals(status))
            throw new RegraNegocioException("A chamada " + numeroEdital + " não está aberta (status: " + status + ")");
    }

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

    public EntidadeExecutora getEntidade() {
        return entidade;
    }

    public void setEntidade(EntidadeExecutora entidade) {
        this.entidade = entidade;
    }
}
