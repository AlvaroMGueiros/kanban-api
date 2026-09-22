package br.com.alvar.kanban.domain.service;

import java.time.LocalDate;

import br.com.alvar.kanban.domain.exception.TransitionNotAllowedException;
import br.com.alvar.kanban.domain.model.ProjectSchedule;
import br.com.alvar.kanban.domain.model.ProjectStatus;

public final class KanbanTransitionService {

    private KanbanTransitionService() {
    }

    public static ProjectSchedule transition(ProjectSchedule schedule, ProjectStatus target, LocalDate today) {
        ProjectStatus current = ProjectStatusPolicy.calculate(schedule, today);

        if (current == target) {
            return schedule;
        }

        return switch (current) {
            case A_INICIAR -> fromAIniciar(schedule, target, today);
            case EM_ANDAMENTO -> fromEmAndamento(schedule, target, today);
            case ATRASADO -> fromAtrasado(schedule, target, today);
            case CONCLUIDO -> fromConcluido(schedule, target, today);
        };
    }

    private static ProjectSchedule fromAIniciar(
            ProjectSchedule schedule, ProjectStatus target, LocalDate today) {
        return switch (target) {
            case EM_ANDAMENTO -> {
                ProjectSchedule updated = schedule.withActualStartDate(today);
                requireStatus(updated, ProjectStatus.EM_ANDAMENTO, today,
                        "Para iniciar o projeto, o término previsto deve ser posterior a hoje "
                        + "ou estar sem data de término prevista.");
                yield updated;
            }
            case ATRASADO -> {
                requireStatus(schedule, ProjectStatus.ATRASADO, today,
                        "O projeto não está atrasado. Ajuste 'inicioPrevisto' para antes de hoje "
                        + "ou 'terminoPrevisto' para antes de hoje para que o status fique ATRASADO.");
                yield schedule;
            }
            case CONCLUIDO -> {
                ProjectSchedule updated = schedule.withActualEndDate(today);
                requireStatus(updated, ProjectStatus.CONCLUIDO, today,
                        "Não foi possível concluir o projeto diretamente.");
                yield updated;
            }
            default -> throw new TransitionNotAllowedException(
                    "Transição de A_INICIAR para " + target + " não é permitida.");
        };
    }

    private static ProjectSchedule fromEmAndamento(
            ProjectSchedule schedule, ProjectStatus target, LocalDate today) {
        return switch (target) {
            case A_INICIAR -> {
                ProjectSchedule updated = schedule.withActualStartDate(null);
                requireStatus(updated, ProjectStatus.A_INICIAR, today,
                        "Ao remover o início realizado, o projeto ficaria ATRASADO. "
                        + "Ajuste 'inicioPrevisto' e 'terminoPrevisto' antes de retornar a A_INICIAR.");
                yield updated;
            }
            case ATRASADO -> {
                requireStatus(schedule, ProjectStatus.ATRASADO, today,
                        "O projeto não está atrasado. Para forçar o status ATRASADO, ajuste "
                        + "'terminoPrevisto' para antes de hoje.");
                yield schedule;
            }
            case CONCLUIDO -> {
                ProjectSchedule updated = schedule.withActualEndDate(today);
                requireStatus(updated, ProjectStatus.CONCLUIDO, today,
                        "Não foi possível concluir o projeto.");
                yield updated;
            }
            default -> throw new TransitionNotAllowedException(
                    "Transição de EM_ANDAMENTO para " + target + " não é permitida.");
        };
    }

    private static ProjectSchedule fromAtrasado(
            ProjectSchedule schedule, ProjectStatus target, LocalDate today) {
        return switch (target) {
            case A_INICIAR -> {
                requireStatus(schedule, ProjectStatus.A_INICIAR, today,
                        "O projeto ainda está atrasado com as datas atuais. Ajuste 'inicioPrevisto' "
                        + "para hoje ou depois e 'terminoPrevisto' para hoje ou depois para que "
                        + "o status seja A_INICIAR.");
                yield schedule;
            }
            case EM_ANDAMENTO -> {
                requireStatus(schedule, ProjectStatus.EM_ANDAMENTO, today,
                        "O projeto não pode ficar EM_ANDAMENTO com as datas atuais. Verifique se "
                        + "'inicioRealizado' está preenchido e 'terminoPrevisto' é hoje ou depois.");
                yield schedule;
            }
            case CONCLUIDO -> {
                ProjectSchedule updated = schedule.withActualEndDate(today);
                requireStatus(updated, ProjectStatus.CONCLUIDO, today,
                        "Não foi possível concluir o projeto atrasado.");
                yield updated;
            }
            default -> throw new TransitionNotAllowedException(
                    "Transição de ATRASADO para " + target + " não é permitida.");
        };
    }

    private static ProjectSchedule fromConcluido(
            ProjectSchedule schedule, ProjectStatus target, LocalDate today) {
        return switch (target) {
            case A_INICIAR -> {
                ProjectSchedule updated = schedule.withActualEndDate(null);
                requireStatus(updated, ProjectStatus.A_INICIAR, today,
                        "Ao reabrir o projeto, ele ficaria ATRASADO com as datas atuais. "
                        + "Ajuste 'inicioPrevisto' e 'terminoPrevisto' antes de retornar a A_INICIAR.");
                yield updated;
            }
            case EM_ANDAMENTO -> {
                ProjectSchedule updated = schedule.withActualEndDate(null);
                requireStatus(updated, ProjectStatus.EM_ANDAMENTO, today,
                        "Ao reabrir o projeto, ele ficaria ATRASADO com as datas atuais. "
                        + "Ajuste 'terminoPrevisto' para hoje ou depois antes de retornar a EM_ANDAMENTO.");
                yield updated;
            }
            case ATRASADO -> {
                ProjectSchedule updated = schedule.withActualEndDate(null);
                requireStatus(updated, ProjectStatus.ATRASADO, today,
                        "Ao reabrir o projeto, ele não ficaria ATRASADO com as datas atuais. "
                        + "Ajuste 'terminoPrevisto' para antes de hoje ou 'inicioPrevisto' para antes "
                        + "de hoje (sem 'inicioRealizado') para que o status seja ATRASADO.");
                yield updated;
            }
            default -> throw new TransitionNotAllowedException(
                    "Transição de CONCLUIDO para " + target + " não é permitida.");
        };
    }

    private static void requireStatus(
            ProjectSchedule schedule, ProjectStatus expected, LocalDate today, String guidance) {
        ProjectStatus actual = ProjectStatusPolicy.calculate(schedule, today);
        if (actual != expected) {
            throw new TransitionNotAllowedException(guidance);
        }
    }
}
