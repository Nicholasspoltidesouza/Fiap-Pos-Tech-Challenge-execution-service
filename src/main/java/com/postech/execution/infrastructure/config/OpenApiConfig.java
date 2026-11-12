package com.postech.execution.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI executionOpenApi() {
        return new OpenAPI()
                .addServersItem(new Server().url("http://localhost:8083").description("Local"))
                .info(new Info()
                        .title("Execution Service API")
                        .description("""
                                Execução e Produção (FIAP Fase 4).

                                ## Banco
                                PostgreSQL execution_db — tabela de jobs da oficina.

                                ## Exemplos de execução (após pagamento aprovado)
                                1. GET /api/execucoes/por-ordem/{ordemServicoId}
                                2. POST /api/execucoes/{id}/iniciar-diagnostico  {"notas":"Correia danificada"}
                                3. POST /api/execucoes/{id}/iniciar-reparo       {"notas":"Troca da correia"}
                                4. POST /api/execucoes/{id}/finalizar            {"notas":"Serviço concluído"}
                                5. OS Service recebe execucao.finalizada e marca FINALIZADA
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("FIAP Pós Tech").url("https://github.com/Nicholasspoltidesouza")));
    }
}
