/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.dao;

import br.com.fiap.colheai.exception.EntidadeNaoEncontradaException;
import br.com.fiap.colheai.model.EntidadeExecutora;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Acesso ao banco da tabela tb_entidade_executora (CRUD da EEx).
@Repository
public class EntidadeExecutoraDao {

    private final DataSource dataSource;

    private static final String INSERT_SQL = "insert into tb_entidade_executora (cd_entidade, nm_entidade, nm_municipio, " +
            "cd_uf, cd_ibge_municipio, vl_repasse_anual, nr_ano_repasse) " +
            "values (sq_tb_entidade_executora.nextval, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_SQL = "select * from tb_entidade_executora order by cd_entidade";
    private static final String SELECT_BY_ID_SQL = "select * from tb_entidade_executora where cd_entidade = ?";
    private static final String UPDATE_SQL = "update tb_entidade_executora set nm_entidade = ?, nm_municipio = ?, " +
            "cd_uf = ?, cd_ibge_municipio = ?, vl_repasse_anual = ?, nr_ano_repasse = ? where cd_entidade = ?";
    private static final String DELETE_SQL = "delete from tb_entidade_executora where cd_entidade = ?";

    public EntidadeExecutoraDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void cadastrar(EntidadeExecutora entidade) throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(INSERT_SQL, new String[] {"cd_entidade"})) {
            stmt.setString(1, entidade.getNome());
            stmt.setString(2, entidade.getMunicipio());
            stmt.setString(3, entidade.getUf());
            stmt.setInt(4, entidade.getCodigoIbge());
            stmt.setDouble(5, entidade.getRepasseAnual());
            stmt.setInt(6, entidade.getAnoRepasse());
            stmt.executeUpdate();
            // recupera o codigo gerado pela sequence para devolver no 201
            ResultSet resultSet = stmt.getGeneratedKeys();
            if (resultSet.next())
                entidade.setId(resultSet.getInt(1));
        }
    }

    public List<EntidadeExecutora> listar() throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SELECT_SQL)) {
            ResultSet resultSet = stmt.executeQuery();
            List<EntidadeExecutora> lista = new ArrayList<>();
            while (resultSet.next())
                lista.add(getEntidade(resultSet));
            return lista;
        }
    }

    public EntidadeExecutora buscar(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setInt(1, id);
            ResultSet resultSet = stmt.executeQuery();
            if (!resultSet.next())
                throw new EntidadeNaoEncontradaException("Entidade executora não encontrada");
            return getEntidade(resultSet);
        }
    }

    public void atualizar(EntidadeExecutora entidade) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, entidade.getNome());
            stmt.setString(2, entidade.getMunicipio());
            stmt.setString(3, entidade.getUf());
            stmt.setInt(4, entidade.getCodigoIbge());
            stmt.setDouble(5, entidade.getRepasseAnual());
            stmt.setInt(6, entidade.getAnoRepasse());
            stmt.setInt(7, entidade.getId());
            if (stmt.executeUpdate() == 0)
                throw new EntidadeNaoEncontradaException("Entidade executora não encontrada");
        }
    }

    public void remover(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0)
                throw new EntidadeNaoEncontradaException("Entidade executora não encontrada");
        }
    }

    // Monta o objeto a partir da linha atual do resultado
    private EntidadeExecutora getEntidade(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("cd_entidade");
        String nome = resultSet.getString("nm_entidade");
        String municipio = resultSet.getString("nm_municipio");
        String uf = resultSet.getString("cd_uf");
        int codigoIbge = resultSet.getInt("cd_ibge_municipio");
        double repasseAnual = resultSet.getDouble("vl_repasse_anual");
        int anoRepasse = resultSet.getInt("nr_ano_repasse");
        return new EntidadeExecutora(id, nome, municipio, uf, codigoIbge, repasseAnual, anoRepasse);
    }

}
