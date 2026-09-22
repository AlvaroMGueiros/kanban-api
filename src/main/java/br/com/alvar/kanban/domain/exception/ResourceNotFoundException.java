package br.com.alvar.kanban.domain.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " não encontrado: " + id + ".");
    }
}
