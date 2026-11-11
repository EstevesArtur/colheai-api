/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.controller;

import br.com.fiap.colheai.dao.AgricultorDao;
import br.com.fiap.colheai.dao.ChamadaPublicaDao;
import br.com.fiap.colheai.dao.PropostaDao;
import br.com.fiap.colheai.dto.PropostaAtualizacaoDto;
import br.com.fiap.colheai.dto.PropostaResponseDto;
import br.com.fiap.colheai.exception.EntidadeNaoEncontradaException;
import br.com.fiap.colheai.exception.RegraNegocioException;
import br.com.fiap.colheai.model.Agricultor;
import br.com.fiap.colheai.model.ChamadaPublica;
import br.com.fiap.colheai.model.Proposta;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

// Rotas da proposta: listar, buscar, atualizar (quantidade e andamento) e remover.
// O envio da proposta fica em POST /itens/{id}/propostas, porque toda proposta e para um item.
@RestController
@RequestMapping("/propostas")
public class PropostaController {

    private PropostaDao dao;
    private ChamadaPublicaDao chamadaDao;
    private AgricultorDao agricultorDao;
    private ModelMapper mapper;

    public PropostaController(PropostaDao dao, ChamadaPublicaDao chamadaDao, AgricultorDao agricultorDao,
                              ModelMapper mapper) {
        this.dao = dao;
        this.chamadaDao = chamadaDao;
        this.agricultorDao = agricultorDao;
        this.mapper = mapper;
    }

    // Converte para DTO e preenche o valor, que nao existe no banco (e calculado).
    // E static para os outros controllers que devolvem propostas usarem tambem.
    public static PropostaResponseDto paraDto(Proposta proposta, ModelMapper mapper) {
        PropostaResponseDto dto = mapper.map(proposta, PropostaResponseDto.class);
        dto.setValor(proposta.calcularValor());
        return dto;
    }

    @GetMapping
    public List<PropostaResponseDto> listar() throws SQLException {
        return dao.listar().stream()
                .map(p -> paraDto(p, mapper)).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropostaResponseDto> buscar(@PathVariable int id)
            throws SQLException, EntidadeNaoEncontradaException {
        return ResponseEntity.ok(paraDto(dao.buscar(id), mapper));
    }

    // Atualiza a quantidade e o andamento. Se a proposta continua valendo
    // (ENVIADA ou CONTRATADA), a regra dos R$ 40 mil e conferida de novo,
    // com a propria proposta fora da soma do que ja estava comprometido.
    @PutMapping("/{id}")
    public ResponseEntity<PropostaResponseDto> atualizar(@PathVariable int id,
                                                         @Valid @RequestBody PropostaAtualizacaoDto dto)
            throws SQLException, EntidadeNaoEncontradaException, RegraNegocioException {
        Proposta proposta = dao.buscar(id);
        proposta.setQuantidade(dto.getQuantidade());
        proposta.setStatus(dto.getStatus());

        if (!dto.getStatus().equals("RECUSADA")) {
            ChamadaPublica chamada = chamadaDao.buscar(proposta.getItem().getChamada().getId());
            Agricultor agricultor = agricultorDao.buscar(proposta.getAgricultor().getId());
            double comprometido = dao.somarComprometido(agricultor.getId(), chamada.getEntidade().getId(),
                    proposta.getDataEnvio().getYear(), proposta.getId());
            agricultor.verificarLimite(comprometido, proposta.calcularValor());
        }

        // contrato fundido na proposta: CONTRATADA tem data de contrato; os outros status nao
        if (dto.getStatus().equals("CONTRATADA")) {
            if (proposta.getDataContrato() == null)
                proposta.setDataContrato(LocalDate.now());
        } else {
            proposta.setDataContrato(null);
        }

        dao.atualizar(proposta);
        return ResponseEntity.ok(paraDto(proposta, mapper));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable int id) throws SQLException, EntidadeNaoEncontradaException {
        dao.remover(id);
        return ResponseEntity.noContent().build();
    }

}
