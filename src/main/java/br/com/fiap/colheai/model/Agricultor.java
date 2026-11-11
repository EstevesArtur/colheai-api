/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.model;

import br.com.fiap.colheai.exception.LimiteVendaExcedidoException;

import java.time.LocalDate;
import java.util.Locale;

// Fornecedor da agricultura familiar, identificado pelo CAF (Cadastro Nacional
// da Agricultura Familiar). Pode ser individual ou grupo. Tabela tb_agricultor.
public class Agricultor {

    // Limite do PNAE por CAF, por ano civil, por EEx (Res. CD/FNDE 4/2026)
    private static final double LIMITE_POR_FAMILIA = 40000.0;

    private int id;
    private String nome;
    private String caf;
    private String tipoFornecedor;
    private int quantidadeFamilias;
    private String municipio;
    private String uf;
    private String distrito;
    private boolean organico;
    private String telefone;
    private LocalDate dataCadastro;

    public Agricultor() {}

    public Agricultor(String nome, String caf, String tipoFornecedor, int quantidadeFamilias, String municipio,
                      String uf, String distrito, boolean organico, String telefone, LocalDate dataCadastro) {
        this.nome = nome;
        this.caf = caf;
        this.tipoFornecedor = tipoFornecedor;
        this.quantidadeFamilias = quantidadeFamilias;
        this.municipio = municipio;
        this.uf = uf;
        this.distrito = distrito;
        this.organico = organico;
        this.telefone = telefone;
        this.dataCadastro = dataCadastro;
    }

    public Agricultor(int id, String nome, String caf, String tipoFornecedor, int quantidadeFamilias, String municipio,
                      String uf, String distrito, boolean organico, String telefone, LocalDate dataCadastro) {
        this.id = id;
        this.nome = nome;
        this.caf = caf;
        this.tipoFornecedor = tipoFornecedor;
        this.quantidadeFamilias = quantidadeFamilias;
        this.municipio = municipio;
        this.uf = uf;
        this.distrito = distrito;
        this.organico = organico;
        this.telefone = telefone;
        this.dataCadastro = dataCadastro;
    }

    public Agricultor(int id) {
        this.id = id;
    }

    // Teto de venda no ano para uma EEx: R$ 40 mil por familia
    // (individual = 1 familia; grupo formal ou informal = N familias)
    public double calcularTeto() {
        return LIMITE_POR_FAMILIA * quantidadeFamilias;
    }

    // Regra de negocio dos R$ 40 mil. Recebe o que o agricultor ja comprometeu
    // com a EEx no ano (propostas enviadas ou contratadas) e o valor da proposta
    // nova. Chegar exatamente no teto e permitido; passar dele, nao.
    // A comparacao e feita em centavos (long) porque somar double pode deixar
    // "sobras" como 40000.000000001 e recusar uma proposta que esta no limite.
    public void verificarLimite(double comprometido, double valorNovo) throws LimiteVendaExcedidoException {
        long totalCentavos = Math.round((comprometido + valorNovo) * 100);
        long tetoCentavos = Math.round(calcularTeto() * 100);
        if (totalCentavos > tetoCentavos) {
            double disponivel = Math.max(calcularTeto() - comprometido, 0);
            Locale br = Locale.of("pt", "BR");
            throw new LimiteVendaExcedidoException(String.format(br,
                    "Limite do PNAE excedido para o CAF %s: o teto é R$ %,.2f por ano nesta EEx, "
                            + "já estão comprometidos R$ %,.2f e restam R$ %,.2f. Esta proposta vale R$ %,.2f.",
                    caf, calcularTeto(), comprometido, disponivel, valorNovo));
        }
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

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}
