/*
 * ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
 * Since Young - 1TDSPV-2026
 * Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
 */
package br.com.fiap.colheai.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Cria um unico ModelMapper para a aplicacao inteira. Os controllers recebem
// esse objeto pelo construtor e usam para converter DTO <-> model.
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper getModelMapper() {
        return new ModelMapper();
    }

}
