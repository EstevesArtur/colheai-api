/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.controller;

import br.com.fiap.colheai.dao.AgricultorDao;
import br.com.fiap.colheai.dao.PropostaDao;
import br.com.fiap.colheai.dto.AgricultorRequestDto;
import br.com.fiap.colheai.dto.AgricultorResponseDto;
import br.com.fiap.colheai.dto.PropostaResponseDto;
import br.com.fiap.colheai.exception.EntidadeNaoEncontradaException;
import br.com.fiap.colheai.exception.RegraNegocioException;
import br.com.fiap.colheai.model.Agricultor;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

// Rotas do agricultor familiar: CRUD e as propostas dele (1:N).
@RestController
@RequestMapping("/agricultores")
public class AgricultorController {

    private AgricultorDao dao;
    private PropostaDao propostaDao;
    private ModelMapper mapper;

    public AgricultorController(AgricultorDao dao, PropostaDao propostaDao, ModelMapper mapper) {
        this.dao = dao;
        this.propostaDao = propostaDao;
        this.mapper = mapper;
    }

    // O CAF e unico: o limite de venda do PNAE e contado por CAF.
    // CAF repetido e regra de negocio violada (409), com mensagem clara.
    @PostMapping
    public ResponseEntity<AgricultorResponseDto> cadastrar(@Valid @RequestBody AgricultorRequestDto dto,
                                                           UriComponentsBuilder builder)
            throws SQLException, RegraNegocioException {
        if (dao.existeCaf(dto.getCaf(), 0))
            throw new RegraNegocioException("Já existe um agricultor cadastrado com o CAF " + dto.getCaf());
        Agricultor agricultor = mapper.map(dto, Agricultor.class);
        agricultor.setDataCadastro(LocalDate.now());
        dao.cadastrar(agricultor);
        URI uri = builder.path("/agricultores/{id}").buildAndExpand(agricultor.getId()).toUri();
        return ResponseEntity.created(uri).body(mapper.map(agricultor, AgricultorResponseDto.class));
    }

    @GetMapping
    public List<AgricultorResponseDto> listar() throws SQLException {
        return dao.listar().stream()
                .map(a -> mapper.map(a, AgricultorResponseDto.class)).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgricultorResponseDto> buscar(@PathVariable int id)
            throws SQLException, EntidadeNaoEncontradaException {
        return ResponseEntity.ok(mapper.map(dao.buscar(id), AgricultorResponseDto.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgricultorResponseDto> atualizar(@PathVariable int id,
                                                           @Valid @RequestBody AgricultorRequestDto dto)
            throws SQLException, EntidadeNaoEncontradaException, RegraNegocioException {
        Agricultor atual = dao.buscar(id); // 404 antes de checar o CAF
        if (dao.existeCaf(dto.getCaf(), id))
            throw new RegraNegocioException("Já existe outro agricultor cadastrado com o CAF " + dto.getCaf());
        Agricultor agricultor = mapper.map(dto, Agricultor.class);
        agricultor.setId(id);
        agricultor.setDataCadastro(atual.getDataCadastro());
        dao.atualizar(agricultor);
        return ResponseEntity.ok(mapper.map(agricultor, AgricultorResponseDto.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable int id) throws SQLException, EntidadeNaoEncontradaException {
        dao.remover(id);
        return ResponseEntity.noContent().build();
    }

    // Rota aninhada do 1:N: propostas deste agricultor (acompanhar o que enviou e o que foi contratado)
    @GetMapping("/{id}/propostas")
    public List<PropostaResponseDto> listarPropostas(@PathVariable int id)
            throws SQLException, EntidadeNaoEncontradaException {
        dao.buscar(id); // 404 se o agricultor nao existe
        return propostaDao.buscarPorAgricultor(id).stream()
                .map(p -> PropostaController.paraDto(p, mapper)).toList();
    }

}
