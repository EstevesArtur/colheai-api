/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.dao;

import br.com.fiap.colheai.exception.EntidadeNaoEncontradaException;
import br.com.fiap.colheai.model.Produto;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Acesso ao banco da tabela tb_produto (CRUD do catalogo de alimentos).
@Repository
public class ProdutoDao {

    private final DataSource dataSource;

    private static final String INSERT_SQL = "insert into tb_produto (cd_produto, nm_produto, ds_categoria, " +
            "ds_unidade_medida) values (sq_tb_produto.nextval, ?, ?, ?)";
    private static final String SELECT_SQL = "select * from tb_produto order by nm_produto";
    private static final String SELECT_BY_ID_SQL = "select * from tb_produto where cd_produto = ?";
    private static final String UPDATE_SQL = "update tb_produto set nm_produto = ?, ds_categoria = ?, " +
            "ds_unidade_medida = ? where cd_produto = ?";
    private static final String DELETE_SQL = "delete from tb_produto where cd_produto = ?";

    public ProdutoDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void cadastrar(Produto produto) throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(INSERT_SQL, new String[] {"cd_produto"})) {
            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getCategoria());
            stmt.setString(3, produto.getUnidadeMedida());
            stmt.executeUpdate();
            ResultSet resultSet = stmt.getGeneratedKeys();
            if (resultSet.next())
                produto.setId(resultSet.getInt(1));
        }
    }

    public List<Produto> listar() throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SELECT_SQL)) {
            ResultSet resultSet = stmt.executeQuery();
            List<Produto> lista = new ArrayList<>();
            while (resultSet.next())
                lista.add(getProduto(resultSet));
            return lista;
        }
    }

    public Produto buscar(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setInt(1, id);
            ResultSet resultSet = stmt.executeQuery();
            if (!resultSet.next())
                throw new EntidadeNaoEncontradaException("Produto não encontrado");
            return getProduto(resultSet);
        }
    }

    public void atualizar(Produto produto) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getCategoria());
            stmt.setString(3, produto.getUnidadeMedida());
            stmt.setInt(4, produto.getId());
            if (stmt.executeUpdate() == 0)
                throw new EntidadeNaoEncontradaException("Produto não encontrado");
        }
    }

    public void remover(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0)
                throw new EntidadeNaoEncontradaException("Produto não encontrado");
        }
    }

    private Produto getProduto(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("cd_produto");
        String nome = resultSet.getString("nm_produto");
        String categoria = resultSet.getString("ds_categoria");
        String unidadeMedida = resultSet.getString("ds_unidade_medida");
        return new Produto(id, nome, categoria, unidadeMedida);
    }

}
