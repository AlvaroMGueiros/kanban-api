package br.com.alvar.kanban.presentation.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageRequests {
    private PageRequests() {
    }

    public static Pageable create(int page, int size, List<String> sort, Set<String> allowedFields) {
        if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Paginação inválida: page >= 0, size entre 1 e 100 e deslocamento até 2147483647.");
        }
        List<Sort.Order> orders = new ArrayList<>();
        boolean sortsById = false;
        for (String expression : sort) {
            String[] parts = expression.split(",", -1);
            if (parts.length > 2 || !allowedFields.contains(parts[0])) {
                throw new IllegalArgumentException("Ordenação inválida: " + expression + ". Campos permitidos: " + allowedFields + ".");
            }
            Sort.Direction direction = Sort.Direction.ASC;
            if (parts.length == 2) {
                try {
                    direction = Sort.Direction.fromString(parts[1]);
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException("Direção de ordenação deve ser asc ou desc.");
                }
            }
            orders.add(new Sort.Order(direction, parts[0]));
            sortsById |= parts[0].equals("id");
        }
        if (!sortsById) {
            orders.add(Sort.Order.asc("id"));
        }
        return PageRequest.of(page, size, Sort.by(orders));
    }
}
