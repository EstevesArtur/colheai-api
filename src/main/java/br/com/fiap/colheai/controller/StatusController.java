/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Rota de saude da API. O Render chama /status para saber se o servico subiu,
// e o grupo abre essa URL antes da demonstracao para "acordar" a API
// (no plano gratuito ela dorme depois de um tempo sem uso).
// Nao consulta o banco de proposito: se o Oracle estiver lento, o Render nao
// deve achar que a API caiu.
@RestController
public class StatusController {

    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of("status", "UP", "api", "ColheAi");
    }

}
