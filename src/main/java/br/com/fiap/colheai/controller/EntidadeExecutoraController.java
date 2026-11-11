/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.controller;

import br.com.fiap.colheai.dao.ChamadaPublicaDao;
import br.com.fiap.colheai.dao.EntidadeExecutoraDao;
import br.com.fiap.colheai.dao.PropostaDao;
import br.com.fiap.colheai.dto.ChamadaPublicaRequestDto;
import br.com.fiap.colheai.dto.ChamadaPublicaResponseDto;
import br.com.fiap.colheai.dto.EntidadeExecutoraRequestDto;
import br.com.fiap.colheai.dto.EntidadeExecutoraResponseDto;
import br.com.fiap.colheai.dto.MetaResponseDto;
import br.com.fiap.colheai.exception.EntidadeNaoEncontradaException;
import br.com.fiap.colheai.model.ChamadaPublica;
import br.com.fiap.colheai.model.EntidadeExecutora;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

// Rotas da EEx (Entidade Executora): CRUD, as chamadas dela (1:N) e o painel da meta de 45%.
@RestController
@RequestMapping("/entidades")
public class EntidadeExecutoraController {

    private EntidadeExecutoraDao dao;
    private ChamadaPublicaDao chamadaDao;
    private PropostaDao propostaDao;
    private ModelMapper mapper;

    public EntidadeExecutoraController(EntidadeExecutoraDao dao, ChamadaPublicaDao chamadaDao,
                                       PropostaDao propostaDao, ModelMapper mapper) {
        this.dao = dao;
        this.chamadaDao = chamadaDao;
        this.propostaDao = propostaDao;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<EntidadeExecutoraResponseDto> cadastrar(@Valid @RequestBody EntidadeExecutoraRequestDto dto,
                                                                  UriComponentsBuilder builder) throws SQLException {
        EntidadeExecutora entidade = mapper.map(dto, EntidadeExecutora.class);
        dao.cadastrar(entidade);
        // 201 + Location com a URL do recurso criado
        URI uri = builder.path("/entidades/{id}").buildAndExpand(entidade.getId()).toUri();
        return ResponseEntity.created(uri).body(mapper.map(entidade, EntidadeExecutoraResponseDto.class));
    }

    @GetMapping
    public List<EntidadeExecutoraResponseDto> listar() throws SQLException {
        return dao.listar().stream()
                .map(e -> mapper.map(e, EntidadeExecutoraResponseDto.class)).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntidadeExecutoraResponseDto> buscar(@PathVariable int id)
            throws SQLException, EntidadeNaoEncontradaException {
        EntidadeExecutora entidade = dao.buscar(id);
        return ResponseEntity.ok(mapper.map(entidade, EntidadeExecutoraResponseDto.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntidadeExecutoraResponseDto> atualizar(@PathVariable int id,
                                                                  @Valid @RequestBody EntidadeExecutoraRequestDto dto)
            throws SQLException, EntidadeNaoEncontradaException {
        EntidadeExecutora entidade = mapper.map(dto, EntidadeExecutora.class);
        entidade.setId(id);
        dao.atualizar(entidade);
        return ResponseEntity.ok(mapper.map(entidade, EntidadeExecutoraResponseDto.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable int id) throws SQLException, EntidadeNaoEncontradaException {
        dao.remover(id);
        return ResponseEntity.noContent().build();
    }

    // Rota aninhada do 1:N: chamadas publicas desta EEx
    @GetMapping("/{id}/chamadas")
    public List<ChamadaPublicaResponseDto> listarChamadas(@PathVariable int id)
            throws SQLException, EntidadeNaoEncontradaException {
        dao.buscar(id); // 404 se a EEx nao existe (em vez de lista vazia)
        return chamadaDao.buscarPorEntidade(id).stream()
                .map(c -> mapper.map(c, ChamadaPublicaResponseDto.class)).toList();
    }

    // Cadastro do filho pela rota aninhada: a EEx dona da chamada vem do caminho
    @PostMapping("/{id}/chamadas")
    public ResponseEntity<ChamadaPublicaResponseDto> cadastrarChamada(@PathVariable int id,
                                                                      @Valid @RequestBody ChamadaPublicaRequestDto dto,
                                                                      UriComponentsBuilder builder)
            throws SQLException, EntidadeNaoEncontradaException {
        EntidadeExecutora entidade = dao.buscar(id);
        ChamadaPublica chamada = mapper.map(dto, ChamadaPublica.class);
        chamada.setEntidade(entidade);
        chamadaDao.cadastrar(chamada);
        URI uri = builder.path("/chamadas/{id}").buildAndExpand(chamada.getId()).toUri();
        return ResponseEntity.created(uri).body(mapper.map(chamada, ChamadaPublicaResponseDto.class));
    }

    // Painel da meta de 45%: soma o que a EEx ja contratou no ano do repasse
    @GetMapping("/{id}/meta")
    public ResponseEntity<MetaResponseDto> meta(@PathVariable int id)
            throws SQLException, EntidadeNaoEncontradaException {
        EntidadeExecutora entidade = dao.buscar(id);
        double contratado = propostaDao.somarContratado(id, entidade.getAnoRepasse());
        double meta = entidade.calcularMeta();

        MetaResponseDto dto = new MetaResponseDto();
        dto.setEntidade(mapper.map(entidade, EntidadeExecutoraResponseDto.class));
        dto.setAno(entidade.getAnoRepasse());
        dto.setRepasse(entidade.getRepasseAnual());
        dto.setMeta(meta);
        dto.setContratado(contratado);
        dto.setPercentualContratado(Math.round(contratado / entidade.getRepasseAnual() * 10000) / 100.0);
        dto.setFalta(Math.max(Math.round((meta - contratado) * 100) / 100.0, 0));
        dto.setMetaAtingida(contratado >= meta);
        return ResponseEntity.ok(dto);
    }

}
