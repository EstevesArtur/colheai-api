/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.dto;

import java.util.Map;

// Formato unico de erro da API. Todo erro (400, 404, 405, 409, 415, 500) volta
// com "erro" (o tipo) e "detalhe" (a explicacao). Na validacao (400), vem
// tambem "campos", com a mensagem de cada campo errado. Quando "campos" e nulo,
// ele nao aparece no JSON (spring.jackson.default-property-inclusion=non_null).
public class ErroResponseDto {

    private String erro;
    private String detalhe;
    private Map<String, String> campos;

    public ErroResponseDto(String erro, String detalhe) {
        this.erro = erro;
        this.detalhe = detalhe;
    }

    public ErroResponseDto(String erro, String detalhe, Map<String, String> campos) {
        this.erro = erro;
        this.detalhe = detalhe;
        this.campos = campos;
    }

    public String getErro() {
        return erro;
    }

    public void setErro(String erro) {
        this.erro = erro;
    }

    public String getDetalhe() {
        return detalhe;
    }

    public void setDetalhe(String detalhe) {
        this.detalhe = detalhe;
    }

    public Map<String, String> getCampos() {
        return campos;
    }

    public void setCampos(Map<String, String> campos) {
        this.campos = campos;
    }
}
