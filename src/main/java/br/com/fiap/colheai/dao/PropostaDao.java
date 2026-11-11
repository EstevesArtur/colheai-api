/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.dao;

import br.com.fiap.colheai.exception.EntidadeNaoEncontradaException;
import br.com.fiap.colheai.model.Agricultor;
import br.com.fiap.colheai.model.ChamadaPublica;
import br.com.fiap.colheai.model.ItemChamada;
import br.com.fiap.colheai.model.Produto;
import br.com.fiap.colheai.model.Proposta;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Acesso ao banco da tabela tb_proposta. Alem do CRUD, faz as duas somas que
// a API precisa: o comprometido do agricultor (regra dos R$ 40 mil) e o
// contratado da EEx (painel da meta de 45%). As somas ficam no SQL porque o
// NUMBER do Oracle e decimal exato, sem o erro de arredondamento do double.
@Repository
public class PropostaDao {

    private final DataSource dataSource;

    private static final String INSERT_SQL = "insert into tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, " +
            "qt_ofertada, dt_envio, st_proposta, dt_contrato) values (sq_tb_proposta.nextval, ?, ?, ?, ?, ?, ?)";
    // JOIN com item, produto e agricultor: a proposta volta pronta para a tela
    // (produto, preco para calcular o valor e dados do agricultor)
    private static final String SELECT_SQL = "select p.*, i.cd_chamada, i.cd_produto, i.qt_demandada, i.vl_preco_unitario, " +
            "pr.nm_produto, pr.ds_categoria, pr.ds_unidade_medida, a.nm_agricultor, a.nr_caf, a.ds_tipo_fornecedor, " +
            "a.qt_familia_caf, a.nm_municipio, a.cd_uf, a.nm_distrito, a.in_organico, a.nr_telefone, a.dt_cadastro " +
            "from tb_proposta p " +
            "inner join tb_item_chamada i on i.cd_item_chamada = p.cd_item_chamada " +
            "inner join tb_produto pr on pr.cd_produto = i.cd_produto " +
            "inner join tb_agricultor a on a.cd_agricultor = p.cd_agricultor";
    private static final String SELECT_ALL_SQL = SELECT_SQL + " order by p.cd_proposta";
    private static final String SELECT_BY_ID_SQL = SELECT_SQL + " where p.cd_proposta = ?";
    private static final String SELECT_BY_ITEM_SQL = SELECT_SQL + " where p.cd_item_chamada = ? order by p.cd_proposta";
    private static final String SELECT_BY_AGRICULTOR_SQL = SELECT_SQL + " where p.cd_agricultor = ? order by p.dt_envio, p.cd_proposta";
    // item e agricultor nao mudam no PUT: muda a quantidade e o andamento (status e contrato)
    private static final String UPDATE_SQL = "update tb_proposta set qt_ofertada = ?, st_proposta = ?, dt_contrato = ? " +
            "where cd_proposta = ?";
    private static final String DELETE_SQL = "delete from tb_proposta where cd_proposta = ?";

    // Regra dos R$ 40 mil (a mesma conta da consulta C05 do BD): soma o valor das
    // propostas ENVIADAS ou CONTRATADAS do agricultor em chamadas da mesma EEx, no
    // mesmo ano civil. A proposta que esta sendo alterada fica fora da soma.
    private static final String SOMA_COMPROMETIDO_SQL = "select nvl(sum(p.qt_ofertada * i.vl_preco_unitario), 0) " +
            "from tb_proposta p " +
            "inner join tb_item_chamada i on i.cd_item_chamada = p.cd_item_chamada " +
            "inner join tb_chamada_publica c on c.cd_chamada = i.cd_chamada " +
            "where p.cd_agricultor = ? and c.cd_entidade = ? and p.st_proposta in ('ENVIADA', 'CONTRATADA') " +
            "and extract(year from p.dt_envio) = ? and p.cd_proposta <> ?";

    // Painel da meta (a mesma conta da consulta C02 do BD): soma o valor das
    // propostas CONTRATADAS em chamadas da EEx, pelo ano do contrato.
    private static final String SOMA_CONTRATADO_SQL = "select nvl(sum(p.qt_ofertada * i.vl_preco_unitario), 0) " +
            "from tb_proposta p " +
            "inner join tb_item_chamada i on i.cd_item_chamada = p.cd_item_chamada " +
            "inner join tb_chamada_publica c on c.cd_chamada = i.cd_chamada " +
            "where c.cd_entidade = ? and p.st_proposta = 'CONTRATADA' and extract(year from p.dt_contrato) = ?";

    public PropostaDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void cadastrar(Proposta proposta) throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(INSERT_SQL, new String[] {"cd_proposta"})) {
            stmt.setInt(1, proposta.getItem().getId());
            stmt.setInt(2, proposta.getAgricultor().getId());
            stmt.setDouble(3, proposta.getQuantidade());
            stmt.setObject(4, proposta.getDataEnvio());
            stmt.setString(5, proposta.getStatus());
            stmt.setObject(6, proposta.getDataContrato());
            stmt.executeUpdate();
            ResultSet resultSet = stmt.getGeneratedKeys();
            if (resultSet.next())
                proposta.setId(resultSet.getInt(1));
        }
    }

    public List<Proposta> listar() throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SELECT_ALL_SQL)) {
            return getLista(stmt.executeQuery());
        }
    }

    // Rota aninhada do 1:N: GET /itens/{id}/propostas
    public List<Proposta> buscarPorItem(int idItem) throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SELECT_BY_ITEM_SQL)) {
            stmt.setInt(1, idItem);
            return getLista(stmt.executeQuery());
        }
    }

    // Rota aninhada do 1:N: GET /agricultores/{id}/propostas
    public List<Proposta> buscarPorAgricultor(int idAgricultor) throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SELECT_BY_AGRICULTOR_SQL)) {
            stmt.setInt(1, idAgricultor);
            return getLista(stmt.executeQuery());
        }
    }

    public Proposta buscar(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setInt(1, id);
            ResultSet resultSet = stmt.executeQuery();
            if (!resultSet.next())
                throw new EntidadeNaoEncontradaException("Proposta não encontrada");
            return getProposta(resultSet);
        }
    }

    public void atualizar(Proposta proposta) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(UPDATE_SQL)) {
            stmt.setDouble(1, proposta.getQuantidade());
            stmt.setString(2, proposta.getStatus());
            stmt.setObject(3, proposta.getDataContrato());
            stmt.setInt(4, proposta.getId());
            if (stmt.executeUpdate() == 0)
                throw new EntidadeNaoEncontradaException("Proposta não encontrada");
        }
    }

    public void remover(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0)
                throw new EntidadeNaoEncontradaException("Proposta não encontrada");
        }
    }

    // idPropostaIgnorada = 0 no cadastro (nenhuma proposta tem codigo 0)
    public double somarComprometido(int idAgricultor, int idEntidade, int ano, int idPropostaIgnorada)
            throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SOMA_COMPROMETIDO_SQL)) {
            stmt.setInt(1, idAgricultor);
            stmt.setInt(2, idEntidade);
            stmt.setInt(3, ano);
            stmt.setInt(4, idPropostaIgnorada);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            return resultSet.getDouble(1);
        }
    }

    public double somarContratado(int idEntidade, int ano) throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SOMA_CONTRATADO_SQL)) {
            stmt.setInt(1, idEntidade);
            stmt.setInt(2, ano);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            return resultSet.getDouble(1);
        }
    }

    private List<Proposta> getLista(ResultSet resultSet) throws SQLException {
        List<Proposta> lista = new ArrayList<>();
        while (resultSet.next())
            lista.add(getProposta(resultSet));
        return lista;
    }

    private Proposta getProposta(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("cd_proposta");
        double quantidade = resultSet.getDouble("qt_ofertada");
        LocalDate dataEnvio = resultSet.getObject("dt_envio", LocalDate.class);
        String status = resultSet.getString("st_proposta");
        LocalDate dataContrato = resultSet.getObject("dt_contrato", LocalDate.class);

        // item e agricultor completos, vindos do JOIN; da chamada basta o codigo
        Produto produto = new Produto(resultSet.getInt("cd_produto"), resultSet.getString("nm_produto"),
                resultSet.getString("ds_categoria"), resultSet.getString("ds_unidade_medida"));
        ItemChamada item = new ItemChamada(resultSet.getInt("cd_item_chamada"), resultSet.getDouble("qt_demandada"),
                resultSet.getDouble("vl_preco_unitario"), new ChamadaPublica(resultSet.getInt("cd_chamada")), produto);
        Agricultor agricultor = new Agricultor(resultSet.getInt("cd_agricultor"), resultSet.getString("nm_agricultor"),
                resultSet.getString("nr_caf"), resultSet.getString("ds_tipo_fornecedor"),
                resultSet.getInt("qt_familia_caf"), resultSet.getString("nm_municipio"), resultSet.getString("cd_uf"),
                resultSet.getString("nm_distrito"), "S".equals(resultSet.getString("in_organico")),
                resultSet.getString("nr_telefone"), resultSet.getObject("dt_cadastro", LocalDate.class));

        return new Proposta(id, quantidade, dataEnvio, status, dataContrato, item, agricultor);
    }

}
