/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Dados para cadastrar ou atualizar um produto. O id so e usado quando o
// produto e informado dentro de um item: {"produto": {"id": 6}}.
public class ProdutoRequestDto {

    private int id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 60, message = "Nome deve ter no máximo 60 caracteres")
    private String nome;

    @NotBlank(message = "Categoria é obrigatória")
    @Pattern(regexp = "HORTALICA|FRUTA|RAIZ_TUBERCULO|GRAO|ORIGEM_ANIMAL|PROCESSADO", message = "Categoria deve ser HORTALICA, FRUTA, RAIZ_TUBERCULO, GRAO, ORIGEM_ANIMAL ou PROCESSADO")
    private String categoria;

    @NotBlank(message = "Unidade de medida é obrigatória")
    @Pattern(regexp = "KG|MACO|UNIDADE|DUZIA|LITRO", message = "Unidade deve ser KG, MACO, UNIDADE, DUZIA ou LITRO")
    private String unidadeMedida;

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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

}
