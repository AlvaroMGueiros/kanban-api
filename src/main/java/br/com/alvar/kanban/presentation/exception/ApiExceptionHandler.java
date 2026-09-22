package br.com.alvar.kanban.presentation.exception;

import java.time.Clock;
import java.util.stream.Collectors;

import br.com.alvar.kanban.domain.exception.BusinessRuleException;
import br.com.alvar.kanban.domain.exception.ConflictException;
import br.com.alvar.kanban.domain.exception.ResourceNotFoundException;
import br.com.alvar.kanban.domain.exception.TransitionNotAllowedException;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private final Clock clock;

    public ApiExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiError> conflict(ConflictException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "RESOURCE_CONFLICT", exception.getMessage(), request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    ResponseEntity<ApiError> businessRule(BusinessRuleException exception, HttpServletRequest request) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE_VIOLATION", exception.getMessage(), request);
    }

    @ExceptionHandler(TransitionNotAllowedException.class)
    ResponseEntity<ApiError> transitionNotAllowed(TransitionNotAllowedException exception, HttpServletRequest request) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "TRANSITION_NOT_ALLOWED", exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .sorted().collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> invalidArgument(IllegalArgumentException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> invalidType(MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST",
                "Valor inválido para o parâmetro " + exception.getName() + ".", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> unreadableRequest(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST",
                "JSON inválido: confira os campos, tipos, status e datas no formato yyyy-MM-dd.", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> integrity(DataIntegrityViolationException exception, HttpServletRequest request) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                if ("responsibleEmailUnique".equals(violation.getConstraintName())) {
                    return error(HttpStatus.CONFLICT, "RESOURCE_CONFLICT",
                            "Já existe um responsável com este e-mail.", request);
                }
                if ("projectResponsibleResponsibleFk".equals(violation.getConstraintName())) {
                    return error(HttpStatus.CONFLICT, "RESOURCE_CONFLICT",
                            "Responsável vinculado a projeto. Substitua ou remova o vínculo antes de excluir.", request);
                }
            }
            cause = cause.getCause();
        }
        logger.error("Falha de integridade em {}", request.getRequestURI(), exception);
        return error(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION",
                "A alteração viola uma restrição de integridade dos dados.", request);
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiError(clock.instant(), status.value(),
                code, message, request.getRequestURI()));
    }
}
