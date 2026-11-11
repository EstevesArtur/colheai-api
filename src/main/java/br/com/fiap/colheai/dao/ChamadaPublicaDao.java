/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.dao;

import br.com.fiap.colheai.exception.EntidadeNaoEncontradaException;
import br.com.fiap.colheai.model.ChamadaPublica;
import br.com.fiap.colheai.model.EntidadeExecutora;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Acesso ao banco da tabela tb_chamada_publica. As consultas fazem JOIN com a
// EEx para a chamada ja voltar com os dados da prefeitura (o front mostra o nome).
@Repository
public class ChamadaPublicaDao {

    private final DataSource dataSource;

    private static final String INSERT_SQL = "insert into tb_chamada_publica (cd_chamada, cd_entidade, nr_edital, " +
            "ds_objeto, dt_abertura, dt_encerramento, st_chamada) values (sq_tb_chamada_publica.nextval, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_SQL = "select c.*, e.nm_entidade, e.nm_municipio, e.cd_uf, e.cd_ibge_municipio, " +
            "e.vl_repasse_anual, e.nr_ano_repasse from tb_chamada_publica c " +
            "inner join tb_entidade_executora e on e.cd_entidade = c.cd_entidade";
    private static final String SELECT_ALL_SQL = SELECT_SQL + " order by c.dt_encerramento, c.cd_chamada";
    private static final String SELECT_BY_ID_SQL = SELECT_SQL + " where c.cd_chamada = ?";
    private static final String SELECT_BY_STATUS_SQL = SELECT_SQL + " where c.st_chamada = ? order by c.dt_encerramento, c.cd_chamada";
    private static final String SELECT_BY_ENTIDADE_SQL = SELECT_SQL + " where c.cd_entidade = ? order by c.dt_abertura, c.cd_chamada";
    // a EEx dona da chamada nao muda no PUT
    private static final String UPDATE_SQL = "update tb_chamada_publica set nr_edital = ?, ds_objeto = ?, " +
            "dt_abertura = ?, dt_encerramento = ?, st_chamada = ? where cd_chamada = ?";
    private static final String DELETE_SQL = "delete from tb_chamada_publica where cd_chamada = ?";

    public ChamadaPublicaDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void cadastrar(ChamadaPublica chamada) throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(INSERT_SQL, new String[] {"cd_chamada"})) {
            stmt.setInt(1, chamada.getEntidade().getId());
            stmt.setString(2, chamada.getNumeroEdital());
            stmt.setString(3, chamada.getObjeto());
            stmt.setObject(4, chamada.getDataAbertura());
            stmt.setObject(5, chamada.getDataEncerramento());
            stmt.setString(6, chamada.getStatus());
            stmt.executeUpdate();
            ResultSet resultSet = stmt.getGeneratedKeys();
            if (resultSet.next())
                chamada.setId(resultSet.getInt(1));
        }
    }

    public List<ChamadaPublica> listar() throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SELECT_ALL_SQL)) {
            return getLista(stmt.executeQuery());
        }
    }

    // Filtro usado pelo front para mostrar so as chamadas ABERTAS
    public List<ChamadaPublica> listarPorStatus(String status) throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SELECT_BY_STATUS_SQL)) {
            stmt.setString(1, status);
            return getLista(stmt.executeQuery());
        }
    }

    // Rota aninhada do 1:N: GET /entidades/{id}/chamadas
    public List<ChamadaPublica> buscarPorEntidade(int idEntidade) throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SELECT_BY_ENTIDADE_SQL)) {
            stmt.setInt(1, idEntidade);
            return getLista(stmt.executeQuery());
        }
    }

    public ChamadaPublica buscar(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setInt(1, id);
            ResultSet resultSet = stmt.executeQuery();
            if (!resultSet.next())
                throw new EntidadeNaoEncontradaException("Chamada pública não encontrada");
            return getChamada(resultSet);
        }
    }

    public void atualizar(ChamadaPublica chamada) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, chamada.getNumeroEdital());
            stmt.setString(2, chamada.getObjeto());
            stmt.setObject(3, chamada.getDataAbertura());
            stmt.setObject(4, chamada.getDataEncerramento());
            stmt.setString(5, chamada.getStatus());
            stmt.setInt(6, chamada.getId());
            if (stmt.executeUpdate() == 0)
                throw new EntidadeNaoEncontradaException("Chamada pública não encontrada");
        }
    }

    public void remover(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0)
                throw new EntidadeNaoEncontradaException("Chamada pública não encontrada");
        }
    }

    private List<ChamadaPublica> getLista(ResultSet resultSet) throws SQLException {
        List<ChamadaPublica> lista = new ArrayList<>();
        while (resultSet.next())
            lista.add(getChamada(resultSet));
        return lista;
    }

    private ChamadaPublica getChamada(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("cd_chamada");
        String numeroEdital = resultSet.getString("nr_edital");
        String objeto = resultSet.getString("ds_objeto");
        LocalDate dataAbertura = resultSet.getObject("dt_abertura", LocalDate.class);
        LocalDate dataEncerramento = resultSet.getObject("dt_encerramento", LocalDate.class);
        String status = resultSet.getString("st_chamada");
        // dados da EEx que vieram do JOIN
        EntidadeExecutora entidade = new EntidadeExecutora(resultSet.getInt("cd_entidade"),
                resultSet.getString("nm_entidade"), resultSet.getString("nm_municipio"),
                resultSet.getString("cd_uf"), resultSet.getInt("cd_ibge_municipio"),
                resultSet.getDouble("vl_repasse_anual"), resultSet.getInt("nr_ano_repasse"));
        return new ChamadaPublica(id, numeroEdital, objeto, dataAbertura, dataEncerramento, status, entidade);
    }

}
