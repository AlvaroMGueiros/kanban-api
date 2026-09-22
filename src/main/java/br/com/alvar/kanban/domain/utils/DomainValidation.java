package br.com.alvar.kanban.domain.utils;

import br.com.alvar.kanban.domain.exception.BusinessRuleException;

public final class DomainValidation {
    private DomainValidation() {
    }

    public static String requireText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException(field + " deve ser preenchido.");
        }
        String normalized = value.strip();
        if (normalized.length() > maxLength) {
            throw new BusinessRuleException(field + " deve ter no máximo " + maxLength + " caracteres.");
        }
        return normalized;
    }
}
