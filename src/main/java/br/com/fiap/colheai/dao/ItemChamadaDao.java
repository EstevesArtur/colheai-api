/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.dao;

import br.com.fiap.colheai.exception.EntidadeNaoEncontradaException;
import br.com.fiap.colheai.model.ChamadaPublica;
import br.com.fiap.colheai.model.ItemChamada;
import br.com.fiap.colheai.model.Produto;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Acesso ao banco da tabela tb_item_chamada. As consultas fazem JOIN com o
// produto, porque o detalhe da chamada mostra o nome e a unidade do alimento.
@Repository
public class ItemChamadaDao {

    private final DataSource dataSource;

    private static final String INSERT_SQL = "insert into tb_item_chamada (cd_item_chamada, cd_chamada, cd_produto, " +
            "qt_demandada, vl_preco_unitario) values (sq_tb_item_chamada.nextval, ?, ?, ?, ?)";
    private static final String SELECT_SQL = "select i.*, p.nm_produto, p.ds_categoria, p.ds_unidade_medida " +
            "from tb_item_chamada i inner join tb_produto p on p.cd_produto = i.cd_produto";
    private static final String SELECT_ALL_SQL = SELECT_SQL + " order by i.cd_item_chamada";
    private static final String SELECT_BY_ID_SQL = SELECT_SQL + " where i.cd_item_chamada = ?";
    private static final String SELECT_BY_CHAMADA_SQL = SELECT_SQL + " where i.cd_chamada = ? order by p.nm_produto";
    // a chamada do item nao muda no PUT
    private static final String UPDATE_SQL = "update tb_item_chamada set cd_produto = ?, qt_demandada = ?, " +
            "vl_preco_unitario = ? where cd_item_chamada = ?";
    private static final String DELETE_SQL = "delete from tb_item_chamada where cd_item_chamada = ?";

    public ItemChamadaDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void cadastrar(ItemChamada item) throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(INSERT_SQL, new String[] {"cd_item_chamada"})) {
            stmt.setInt(1, item.getChamada().getId());
            stmt.setInt(2, item.getProduto().getId());
            stmt.setDouble(3, item.getQuantidadeDemandada());
            stmt.setDouble(4, item.getPrecoUnitario());
            stmt.executeUpdate();
            ResultSet resultSet = stmt.getGeneratedKeys();
            if (resultSet.next())
                item.setId(resultSet.getInt(1));
        }
    }

    public List<ItemChamada> listar() throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SELECT_ALL_SQL)) {
            return getLista(stmt.executeQuery());
        }
    }

    // Rota aninhada do 1:N: GET /chamadas/{id}/itens (detalhe da chamada no front)
    public List<ItemChamada> buscarPorChamada(int idChamada) throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SELECT_BY_CHAMADA_SQL)) {
            stmt.setInt(1, idChamada);
            return getLista(stmt.executeQuery());
        }
    }

    public ItemChamada buscar(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setInt(1, id);
            ResultSet resultSet = stmt.executeQuery();
            if (!resultSet.next())
                throw new EntidadeNaoEncontradaException("Item da chamada não encontrado");
            return getItem(resultSet);
        }
    }

    public void atualizar(ItemChamada item) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(UPDATE_SQL)) {
            stmt.setInt(1, item.getProduto().getId());
            stmt.setDouble(2, item.getQuantidadeDemandada());
            stmt.setDouble(3, item.getPrecoUnitario());
            stmt.setInt(4, item.getId());
            if (stmt.executeUpdate() == 0)
                throw new EntidadeNaoEncontradaException("Item da chamada não encontrado");
        }
    }

    public void remover(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0)
                throw new EntidadeNaoEncontradaException("Item da chamada não encontrado");
        }
    }

    private List<ItemChamada> getLista(ResultSet resultSet) throws SQLException {
        List<ItemChamada> lista = new ArrayList<>();
        while (resultSet.next())
            lista.add(getItem(resultSet));
        return lista;
    }

    private ItemChamada getItem(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("cd_item_chamada");
        double quantidadeDemandada = resultSet.getDouble("qt_demandada");
        double precoUnitario = resultSet.getDouble("vl_preco_unitario");
        // da chamada basta o codigo (como o new Condominio(id) da aula);
        // o produto vem completo do JOIN
        ChamadaPublica chamada = new ChamadaPublica(resultSet.getInt("cd_chamada"));
        Produto produto = new Produto(resultSet.getInt("cd_produto"), resultSet.getString("nm_produto"),
                resultSet.getString("ds_categoria"), resultSet.getString("ds_unidade_medida"));
        return new ItemChamada(id, quantidadeDemandada, precoUnitario, chamada, produto);
    }

}
