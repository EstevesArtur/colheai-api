/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.handler;

import br.com.fiap.colheai.dto.ErroResponseDto;
import br.com.fiap.colheai.exception.EntidadeNaoEncontradaException;
import br.com.fiap.colheai.exception.RegraNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

// Tratamento global de erros: toda excecao que sai de um controller chega aqui
// e vira um JSON no mesmo formato (ErroResponseDto), com o status HTTP certo.
// Nunca devolvemos stack trace nem a mensagem do Oracle (que mostra SQL e nomes
// de tabelas): o detalhe tecnico vai so para o log do servidor.
@RestControllerAdvice
public class ApiExceptionHandler {

    // Codigos de erro do Oracle que tem significado para o cliente
    private static final int ORA_REGISTRO_DUPLICADO = 1;      // ORA-00001: UNIQUE violado
    private static final int ORA_CHECK_VIOLADO = 2290;        // ORA-02290: CHECK violado
    private static final int ORA_PAI_NAO_EXISTE = 2291;       // ORA-02291: FK aponta para registro inexistente
    private static final int ORA_REGISTRO_COM_FILHOS = 2292;  // ORA-02292: remover pai que tem filhos

    // 404: id que nao existe no banco
    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<ErroResponseDto> naoEncontrado(EntidadeNaoEncontradaException e) {
        return resposta(HttpStatus.NOT_FOUND, new ErroResponseDto("Não encontrado", e.getMessage()));
    }

    // 409: regra de negocio violada (limite de R$ 40 mil, chamada fechada, CAF repetido)
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponseDto> regraNegocio(RegraNegocioException e) {
        return resposta(HttpStatus.CONFLICT, new ErroResponseDto("Regra de negócio violada", e.getMessage()));
    }

    // 400: Bean Validation dos DTOs (@NotBlank, @Positive...), com a mensagem de cada campo
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDto> validacao(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError field : e.getFieldErrors())
            campos.put(field.getField(), field.getDefaultMessage());
        return resposta(HttpStatus.BAD_REQUEST,
                new ErroResponseDto("Dados inválidos", "Corrija os campos indicados e envie de novo", campos));
    }

    // 400: corpo que nao e um JSON valido, ou campo com tipo errado (texto no lugar de numero, data fora do formato)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDto> jsonInvalido(HttpMessageNotReadableException e) {
        return resposta(HttpStatus.BAD_REQUEST, new ErroResponseDto("JSON inválido",
                "O corpo da requisição não pôde ser lido. Confira o formato do JSON, os tipos dos campos e as datas (AAAA-MM-DD)"));
    }

    // 400: parametro da URL com tipo errado, ex.: /chamadas/abc
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDto> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return resposta(HttpStatus.BAD_REQUEST, new ErroResponseDto("Parâmetro inválido",
                "O valor informado para '" + e.getName() + "' não é válido"));
    }

    // 404: rota que nao existe na API (em JSON, nunca a pagina HTML padrao)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponseDto> rotaInexistente(NoResourceFoundException e) {
        return resposta(HttpStatus.NOT_FOUND, new ErroResponseDto("Rota não encontrada",
                "A rota /" + e.getResourcePath() + " não existe nesta API"));
    }

    // 405: rota existe, mas nao aceita o metodo usado (ex.: PATCH /chamadas/1)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponseDto> metodoNaoPermitido(HttpRequestMethodNotSupportedException e) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, new ErroResponseDto("Método não permitido",
                "O método " + e.getMethod() + " não é aceito nesta rota"));
    }

    // 415: corpo enviado sem Content-Type application/json
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponseDto> tipoNaoSuportado(HttpMediaTypeNotSupportedException e) {
        return resposta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, new ErroResponseDto("Formato não suportado",
                "Envie o corpo como JSON, com o cabeçalho Content-Type: application/json"));
    }

    // 406: cliente pediu um formato que a API nao produz (ela so responde JSON)
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErroResponseDto> formatoNaoAceito(HttpMediaTypeNotAcceptableException e) {
        return resposta(HttpStatus.NOT_ACCEPTABLE, new ErroResponseDto("Formato não aceito",
                "Esta API responde somente em JSON (application/json)"));
    }

    // Erros do banco. Os de restricao (UNIQUE, CHECK, FK) viram respostas claras;
    // qualquer outro (banco fora do ar, SQL com problema) vira 500 generico.
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErroResponseDto> erroBanco(SQLException e) {
        switch (e.getErrorCode()) {
            case ORA_REGISTRO_DUPLICADO:
                return resposta(HttpStatus.CONFLICT, new ErroResponseDto("Registro duplicado",
                        "Já existe um cadastro com esses dados (um campo que não pode se repetir foi repetido)"));
            case ORA_REGISTRO_COM_FILHOS:
                return resposta(HttpStatus.CONFLICT, new ErroResponseDto("Registro em uso",
                        "Não é possível remover: existem outros registros ligados a este"));
            case ORA_PAI_NAO_EXISTE:
                return resposta(HttpStatus.NOT_FOUND, new ErroResponseDto("Não encontrado",
                        "Um registro relacionado informado não existe"));
            case ORA_CHECK_VIOLADO:
                return resposta(HttpStatus.BAD_REQUEST, new ErroResponseDto("Dados inválidos",
                        "Um dos valores está fora das regras do cadastro"));
            default:
                System.err.println("Erro de banco (ORA-" + e.getErrorCode() + "): " + e.getMessage());
                return erroInterno();
        }
    }

    // 500: qualquer erro inesperado. A mensagem e generica de proposito.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDto> erroInesperado(Exception e) {
        System.err.println("Erro inesperado: " + e);
        return erroInterno();
    }

    // Monta a resposta de erro sempre como JSON. O Content-Type fixo faz o Spring
    // ignorar o Accept do cliente: mesmo um navegador pedindo HTML recebe o JSON
    // (o edital proibe erro devolvido como pagina HTML).
    private ResponseEntity<ErroResponseDto> resposta(HttpStatus status, ErroResponseDto erro) {
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(erro);
    }

    private ResponseEntity<ErroResponseDto> erroInterno() {
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, new ErroResponseDto("Erro interno",
                "Ocorreu um erro inesperado. Tente novamente em instantes ou procure o administrador"));
    }

}
