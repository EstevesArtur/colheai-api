/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.controller;

import br.com.fiap.colheai.dao.ProdutoDao;
import br.com.fiap.colheai.dto.ProdutoRequestDto;
import br.com.fiap.colheai.dto.ProdutoResponseDto;
import br.com.fiap.colheai.exception.EntidadeNaoEncontradaException;
import br.com.fiap.colheai.model.Produto;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

// Rotas do catalogo de produtos (alimentos que as chamadas compram).
@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private ProdutoDao dao;
    private ModelMapper mapper;

    public ProdutoController(ProdutoDao dao, ModelMapper mapper) {
        this.dao = dao;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<ProdutoResponseDto> cadastrar(@Valid @RequestBody ProdutoRequestDto dto,
                                                        UriComponentsBuilder builder) throws SQLException {
        Produto produto = mapper.map(dto, Produto.class);
        dao.cadastrar(produto);
        URI uri = builder.path("/produtos/{id}").buildAndExpand(produto.getId()).toUri();
        return ResponseEntity.created(uri).body(mapper.map(produto, ProdutoResponseDto.class));
    }

    @GetMapping
    public List<ProdutoResponseDto> listar() throws SQLException {
        return dao.listar().stream()
                .map(p -> mapper.map(p, ProdutoResponseDto.class)).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDto> buscar(@PathVariable int id)
            throws SQLException, EntidadeNaoEncontradaException {
        return ResponseEntity.ok(mapper.map(dao.buscar(id), ProdutoResponseDto.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDto> atualizar(@PathVariable int id, @Valid @RequestBody ProdutoRequestDto dto)
            throws SQLException, EntidadeNaoEncontradaException {
        Produto produto = mapper.map(dto, Produto.class);
        produto.setId(id);
        dao.atualizar(produto);
        return ResponseEntity.ok(mapper.map(produto, ProdutoResponseDto.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable int id) throws SQLException, EntidadeNaoEncontradaException {
        dao.remover(id);
        return ResponseEntity.noContent().build();
    }

}
