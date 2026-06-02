package pk.jl.pasir_lebda_jan.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolationException;
import org.jspecify.annotations.NonNull;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GraphQLExceptionHandler implements DataFetcherExceptionResolver {

    @Override
    public @NonNull Mono<List<GraphQLError>> resolveException(
            @NonNull Throwable ex, @NonNull DataFetchingEnvironment env) {
        if (ex instanceof ConstraintViolationException validationEx) {
            List<GraphQLError> errors = validationEx.getConstraintViolations().stream()
                    .map(violation -> GraphqlErrorBuilder.newError(env)
                            .message("Błąd walidacji: " + violation.getMessage())
                            .build())
                    .collect(Collectors.toList());

            return Mono.just(errors);
        }

        // Fallback - unknown error
        GraphQLError error = GraphqlErrorBuilder.newError(env)
                .message("Wystąpił błąd: " + ex.getMessage())
                .build();

        return Mono.just(List.of(error));
    }
}