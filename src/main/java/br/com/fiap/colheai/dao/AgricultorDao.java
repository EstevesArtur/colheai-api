/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.dao;

import br.com.fiap.colheai.exception.EntidadeNaoEncontradaException;
import br.com.fiap.colheai.model.Agricultor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Acesso ao banco da tabela tb_agricultor (CRUD do agricultor familiar).
@Repository
public class AgricultorDao {

    private final DataSource dataSource;

    private static final String INSERT_SQL = "insert into tb_agricultor (cd_agricultor, nm_agricultor, nr_caf, " +
            "ds_tipo_fornecedor, qt_familia_caf, nm_municipio, cd_uf, nm_distrito, in_organico, nr_telefone, dt_cadastro) " +
            "values (sq_tb_agricultor.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_SQL = "select * from tb_agricultor order by nm_agricultor";
    private static final String SELECT_BY_ID_SQL = "select * from tb_agricultor where cd_agricultor = ?";
    // conta quantos OUTROS agricultores tem o mesmo CAF (no PUT, o proprio fica de fora)
    private static final String COUNT_CAF_SQL = "select count(*) from tb_agricultor where nr_caf = ? and cd_agricultor <> ?";
    private static final String UPDATE_SQL = "update tb_agricultor set nm_agricultor = ?, nr_caf = ?, " +
            "ds_tipo_fornecedor = ?, qt_familia_caf = ?, nm_municipio = ?, cd_uf = ?, nm_distrito = ?, " +
            "in_organico = ?, nr_telefone = ? where cd_agricultor = ?";
    private static final String DELETE_SQL = "delete from tb_agricultor where cd_agricultor = ?";

    public AgricultorDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void cadastrar(Agricultor agricultor) throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(INSERT_SQL, new String[] {"cd_agricultor"})) {
            stmt.setString(1, agricultor.getNome());
            stmt.setString(2, agricultor.getCaf());
            stmt.setString(3, agricultor.getTipoFornecedor());
            stmt.setInt(4, agricultor.getQuantidadeFamilias());
            stmt.setString(5, agricultor.getMunicipio());
            stmt.setString(6, agricultor.getUf());
            stmt.setString(7, agricultor.getDistrito());
            // no banco o indicador e CHAR(1) 'S'/'N'; no Java, boolean
            stmt.setString(8, agricultor.isOrganico() ? "S" : "N");
            stmt.setString(9, agricultor.getTelefone());
            stmt.setObject(10, agricultor.getDataCadastro());
            stmt.executeUpdate();
            ResultSet resultSet = stmt.getGeneratedKeys();
            if (resultSet.next())
                agricultor.setId(resultSet.getInt(1));
        }
    }

    public List<Agricultor> listar() throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SELECT_SQL)) {
            ResultSet resultSet = stmt.executeQuery();
            List<Agricultor> lista = new ArrayList<>();
            while (resultSet.next())
                lista.add(getAgricultor(resultSet));
            return lista;
        }
    }

    public Agricultor buscar(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setInt(1, id);
            ResultSet resultSet = stmt.executeQuery();
            if (!resultSet.next())
                throw new EntidadeNaoEncontradaException("Agricultor não encontrado");
            return getAgricultor(resultSet);
        }
    }

    // Usado antes de cadastrar ou atualizar: o CAF e unico, porque o limite de
    // venda do PNAE e contado por CAF. idIgnorado = 0 no cadastro.
    public boolean existeCaf(String caf, int idIgnorado) throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(COUNT_CAF_SQL)) {
            stmt.setString(1, caf);
            stmt.setInt(2, idIgnorado);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) > 0;
        }
    }

    // A data de cadastro nao muda no PUT: e o dia em que o agricultor entrou no ColheAi
    public void atualizar(Agricultor agricultor) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, agricultor.getNome());
            stmt.setString(2, agricultor.getCaf());
            stmt.setString(3, agricultor.getTipoFornecedor());
            stmt.setInt(4, agricultor.getQuantidadeFamilias());
            stmt.setString(5, agricultor.getMunicipio());
            stmt.setString(6, agricultor.getUf());
            stmt.setString(7, agricultor.getDistrito());
            stmt.setString(8, agricultor.isOrganico() ? "S" : "N");
            stmt.setString(9, agricultor.getTelefone());
            stmt.setInt(10, agricultor.getId());
            if (stmt.executeUpdate() == 0)
                throw new EntidadeNaoEncontradaException("Agricultor não encontrado");
        }
    }

    public void remover(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0)
                throw new EntidadeNaoEncontradaException("Agricultor não encontrado");
        }
    }

    private Agricultor getAgricultor(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("cd_agricultor");
        String nome = resultSet.getString("nm_agricultor");
        String caf = resultSet.getString("nr_caf");
        String tipoFornecedor = resultSet.getString("ds_tipo_fornecedor");
        int quantidadeFamilias = resultSet.getInt("qt_familia_caf");
        String municipio = resultSet.getString("nm_municipio");
        String uf = resultSet.getString("cd_uf");
        String distrito = resultSet.getString("nm_distrito");
        boolean organico = "S".equals(resultSet.getString("in_organico"));
        String telefone = resultSet.getString("nr_telefone");
        LocalDate dataCadastro = resultSet.getObject("dt_cadastro", LocalDate.class);
        return new Agricultor(id, nome, caf, tipoFornecedor, quantidadeFamilias, municipio, uf, distrito,
                organico, telefone, dataCadastro);
    }

}
