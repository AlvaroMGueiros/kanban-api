package br.com.alvar.kanban.infrastructure.config;

import br.com.alvar.kanban.presentation.controller.KanbanController;
import br.com.alvar.kanban.presentation.controller.ProjectController;
import br.com.alvar.kanban.presentation.controller.ResponsibleController;
import br.com.alvar.kanban.presentation.exception.ApiError;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {
    @Bean
    OpenAPI kanbanOpenApi() {
        Components components = new Components();
        ModelConverters.getInstance().read(ApiError.class).forEach(components::addSchemas);
        return new OpenAPI()
                .components(components)
                .info(new Info()
                        .title("Kanban API")
                        .version("1.0.0")
                        .description("Gerenciamento de projetos, responsáveis e transições de Kanban.")
                        .contact(new Contact().name("Álvaro Miguel")));
    }

    @Bean
    OperationCustomizer documentedErrors() {
        return (operation, handlerMethod) -> {
            Class<?> controller = handlerMethod.getBeanType();
            if (controller == ResponsibleController.class) {
                addError(operation, "400", "Requisição inválida");
                addError(operation, "404", "Responsável não encontrado");
                addError(operation, "409", "E-mail duplicado ou responsável vinculado");
            } else if (controller == ProjectController.class) {
                addError(operation, "400", "Requisição inválida");
                addError(operation, "404", "Projeto ou responsável não encontrado");
                addError(operation, "422", "Datas ou regra de negócio inválida");
            } else if (controller == KanbanController.class) {
                addError(operation, "400", "Status ou paginação inválida");
                addError(operation, "404", "Projeto não encontrado");
                addError(operation, "422", "Transição incompatível com as datas");
            }
            return operation;
        };
    }

    private void addError(io.swagger.v3.oas.models.Operation operation, String status, String description) {
        Schema<?> schema = new Schema<>().$ref("#/components/schemas/ApiError");
        operation.getResponses().addApiResponse(status, new ApiResponse().description(description)
                .content(new Content().addMediaType("application/json", new MediaType().schema(schema))));
    }
}
