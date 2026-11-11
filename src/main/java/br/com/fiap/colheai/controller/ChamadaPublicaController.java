/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.controller;

import br.com.fiap.colheai.dao.ChamadaPublicaDao;
import br.com.fiap.colheai.dao.ItemChamadaDao;
import br.com.fiap.colheai.dao.ProdutoDao;
import br.com.fiap.colheai.dto.ChamadaPublicaRequestDto;
import br.com.fiap.colheai.dto.ChamadaPublicaResponseDto;
import br.com.fiap.colheai.dto.ItemChamadaRequestDto;
import br.com.fiap.colheai.dto.ItemChamadaResponseDto;
import br.com.fiap.colheai.exception.EntidadeNaoEncontradaException;
import br.com.fiap.colheai.exception.RegraNegocioException;
import br.com.fiap.colheai.model.ChamadaPublica;
import br.com.fiap.colheai.model.ItemChamada;
import br.com.fiap.colheai.model.Produto;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

// Rotas da chamada publica: listar (com filtro de status), buscar, atualizar,
// remover e os itens da chamada (1:N). O cadastro da chamada fica em
// POST /entidades/{id}/chamadas, porque toda chamada pertence a uma EEx.
@RestController
@RequestMapping("/chamadas")
public class ChamadaPublicaController {

    private ChamadaPublicaDao dao;
    private ItemChamadaDao itemDao;
    private ProdutoDao produtoDao;
    private ModelMapper mapper;

    public ChamadaPublicaController(ChamadaPublicaDao dao, ItemChamadaDao itemDao, ProdutoDao produtoDao,
                                    ModelMapper mapper) {
        this.dao = dao;
        this.itemDao = itemDao;
        this.produtoDao = produtoDao;
        this.mapper = mapper;
    }

    // GET /chamadas lista todas; GET /chamadas?status=ABERTA so as abertas (tela do agricultor)
    @GetMapping
    public List<ChamadaPublicaResponseDto> listar(@RequestParam(required = false) String status) throws SQLException {
        List<ChamadaPublica> lista;
        if (status == null)
            lista = dao.listar();
        else
            lista = dao.listarPorStatus(status.toUpperCase());
        return lista.stream()
                .map(c -> mapper.map(c, ChamadaPublicaResponseDto.class)).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChamadaPublicaResponseDto> buscar(@PathVariable int id)
            throws SQLException, EntidadeNaoEncontradaException {
        ChamadaPublica chamada = dao.buscar(id);
        return ResponseEntity.ok(mapper.map(chamada, ChamadaPublicaResponseDto.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChamadaPublicaResponseDto> atualizar(@PathVariable int id,
                                                               @Valid @RequestBody ChamadaPublicaRequestDto dto)
            throws SQLException, EntidadeNaoEncontradaException {
        ChamadaPublica chamada = mapper.map(dto, ChamadaPublica.class);
        chamada.setId(id);
        dao.atualizar(chamada);
        // busca de novo para devolver a chamada com os dados da EEx
        return ResponseEntity.ok(mapper.map(dao.buscar(id), ChamadaPublicaResponseDto.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable int id) throws SQLException, EntidadeNaoEncontradaException {
        dao.remover(id);
        return ResponseEntity.noContent().build();
    }

    // Rota aninhada do 1:N: itens da chamada (detalhe da chamada no front)
    @GetMapping("/{id}/itens")
    public List<ItemChamadaResponseDto> listarItens(@PathVariable int id)
            throws SQLException, EntidadeNaoEncontradaException {
        dao.buscar(id); // 404 se a chamada nao existe
        return itemDao.buscarPorChamada(id).stream()
                .map(i -> mapper.map(i, ItemChamadaResponseDto.class)).toList();
    }

    // Inclui um item na chamada. So pode enquanto a chamada esta ABERTA.
    @PostMapping("/{id}/itens")
    public ResponseEntity<ItemChamadaResponseDto> cadastrarItem(@PathVariable int id,
                                                                @Valid @RequestBody ItemChamadaRequestDto dto,
                                                                UriComponentsBuilder builder)
            throws SQLException, EntidadeNaoEncontradaException, RegraNegocioException {
        ChamadaPublica chamada = dao.buscar(id);
        chamada.verificarAberta();
        Produto produto = produtoDao.buscar(dto.getProduto().getId());

        ItemChamada item = new ItemChamada(dto.getQuantidadeDemandada(), dto.getPrecoUnitario(), chamada, produto);
        itemDao.cadastrar(item);
        URI uri = builder.path("/itens/{id}").buildAndExpand(item.getId()).toUri();
        return ResponseEntity.created(uri).body(mapper.map(itemDao.buscar(item.getId()), ItemChamadaResponseDto.class));
    }

}
