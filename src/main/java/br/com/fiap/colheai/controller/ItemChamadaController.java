/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.controller;

import br.com.fiap.colheai.dao.AgricultorDao;
import br.com.fiap.colheai.dao.ChamadaPublicaDao;
import br.com.fiap.colheai.dao.ItemChamadaDao;
import br.com.fiap.colheai.dao.ProdutoDao;
import br.com.fiap.colheai.dao.PropostaDao;
import br.com.fiap.colheai.dto.ItemChamadaRequestDto;
import br.com.fiap.colheai.dto.ItemChamadaResponseDto;
import br.com.fiap.colheai.dto.PropostaRequestDto;
import br.com.fiap.colheai.dto.PropostaResponseDto;
import br.com.fiap.colheai.exception.EntidadeNaoEncontradaException;
import br.com.fiap.colheai.exception.RegraNegocioException;
import br.com.fiap.colheai.model.Agricultor;
import br.com.fiap.colheai.model.ChamadaPublica;
import br.com.fiap.colheai.model.ItemChamada;
import br.com.fiap.colheai.model.Produto;
import br.com.fiap.colheai.model.Proposta;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

// Rotas do item da chamada e das propostas para o item (1:N).
// O POST /itens/{id}/propostas e onde a regra dos R$ 40 mil e aplicada.
@RestController
@RequestMapping("/itens")
public class ItemChamadaController {

    private ItemChamadaDao dao;
    private ChamadaPublicaDao chamadaDao;
    private ProdutoDao produtoDao;
    private AgricultorDao agricultorDao;
    private PropostaDao propostaDao;
    private ModelMapper mapper;

    public ItemChamadaController(ItemChamadaDao dao, ChamadaPublicaDao chamadaDao, ProdutoDao produtoDao,
                                 AgricultorDao agricultorDao, PropostaDao propostaDao, ModelMapper mapper) {
        this.dao = dao;
        this.chamadaDao = chamadaDao;
        this.produtoDao = produtoDao;
        this.agricultorDao = agricultorDao;
        this.propostaDao = propostaDao;
        this.mapper = mapper;
    }

    @GetMapping
    public List<ItemChamadaResponseDto> listar() throws SQLException {
        return dao.listar().stream()
                .map(i -> mapper.map(i, ItemChamadaResponseDto.class)).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemChamadaResponseDto> buscar(@PathVariable int id)
            throws SQLException, EntidadeNaoEncontradaException {
        return ResponseEntity.ok(mapper.map(dao.buscar(id), ItemChamadaResponseDto.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemChamadaResponseDto> atualizar(@PathVariable int id,
                                                            @Valid @RequestBody ItemChamadaRequestDto dto)
            throws SQLException, EntidadeNaoEncontradaException {
        ItemChamada item = dao.buscar(id);
        Produto produto = produtoDao.buscar(dto.getProduto().getId());
        item.setProduto(produto);
        item.setQuantidadeDemandada(dto.getQuantidadeDemandada());
        item.setPrecoUnitario(dto.getPrecoUnitario());
        dao.atualizar(item);
        return ResponseEntity.ok(mapper.map(item, ItemChamadaResponseDto.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable int id) throws SQLException, EntidadeNaoEncontradaException {
        dao.remover(id);
        return ResponseEntity.noContent().build();
    }

    // Rota aninhada do 1:N: propostas enviadas para este item
    @GetMapping("/{id}/propostas")
    public List<PropostaResponseDto> listarPropostas(@PathVariable int id)
            throws SQLException, EntidadeNaoEncontradaException {
        dao.buscar(id); // 404 se o item nao existe
        return propostaDao.buscarPorItem(id).stream()
                .map(p -> PropostaController.paraDto(p, mapper)).toList();
    }

    // Agricultor envia uma proposta para o item. Antes de gravar, duas regras:
    // 1) a chamada do item precisa estar ABERTA;
    // 2) o CAF nao pode passar do teto de venda no ano para esta EEx (R$ 40 mil por familia).
    // As duas lancam excecao propria e viram 409.
    @PostMapping("/{id}/propostas")
    public ResponseEntity<PropostaResponseDto> cadastrarProposta(@PathVariable int id,
                                                                 @Valid @RequestBody PropostaRequestDto dto,
                                                                 UriComponentsBuilder builder)
            throws SQLException, EntidadeNaoEncontradaException, RegraNegocioException {
        ItemChamada item = dao.buscar(id);
        ChamadaPublica chamada = chamadaDao.buscar(item.getChamada().getId());
        chamada.verificarAberta();
        Agricultor agricultor = agricultorDao.buscar(dto.getAgricultor().getId());

        // a data de envio e o status inicial sao definidos aqui, nao pelo cliente
        Proposta proposta = new Proposta(dto.getQuantidade(), LocalDate.now(), "ENVIADA", null, item, agricultor);

        // regra dos R$ 40 mil: soma o que o CAF ja comprometeu com esta EEx no ano
        double comprometido = propostaDao.somarComprometido(agricultor.getId(), chamada.getEntidade().getId(),
                proposta.getDataEnvio().getYear(), 0);
        agricultor.verificarLimite(comprometido, proposta.calcularValor());

        propostaDao.cadastrar(proposta);
        URI uri = builder.path("/propostas/{id}").buildAndExpand(proposta.getId()).toUri();
        return ResponseEntity.created(uri).body(PropostaController.paraDto(propostaDao.buscar(proposta.getId()), mapper));
    }

}
