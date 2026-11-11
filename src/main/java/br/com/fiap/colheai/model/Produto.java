/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.model;

// Alimento comprado nas chamadas (alface, banana, ovo caipira...). Tabela tb_produto.
public class Produto {

    private int id;
    private String nome;
    private String categoria;
    private String unidadeMedida;

    public Produto() {}

    public Produto(String nome, String categoria, String unidadeMedida) {
        this.nome = nome;
        this.categoria = categoria;
        this.unidadeMedida = unidadeMedida;
    }

    public Produto(int id, String nome, String categoria, String unidadeMedida) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.unidadeMedida = unidadeMedida;
    }

    public Produto(int id) {
        this.id = id;
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
