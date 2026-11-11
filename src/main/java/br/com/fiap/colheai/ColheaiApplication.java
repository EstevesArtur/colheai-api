/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Classe que sobe a API. O Spring encontra sozinho os controllers, DAOs e
// configuracoes que estao nos subpacotes de br.com.fiap.colheai.
@SpringBootApplication
public class ColheaiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ColheaiApplication.class, args);
	}

}
