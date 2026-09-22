package br.com.alvar.kanban.domain.exception;

public class TransitionNotAllowedException extends RuntimeException {
    public TransitionNotAllowedException(String message) {
        super(message);
    }
}
