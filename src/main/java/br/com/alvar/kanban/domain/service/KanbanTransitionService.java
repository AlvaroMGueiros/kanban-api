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
                if (schedule.getPlannedEndDate() == null) {
                    throw new TransitionNotAllowedException(
                            "Não é possível iniciar este projeto porque o término previsto não foi informado. "
                            + "Abra Editar, informe um término previsto para hoje ou uma data futura e tente novamente.");
                }
                ProjectSchedule updated = schedule.withActualStartDate(today);
                requireStatus(updated, ProjectStatus.EM_ANDAMENTO, today,
                        "Não é possível iniciar este projeto porque o prazo previsto já venceu. "
                        + "Abra Editar, ajuste o término previsto para hoje ou uma data futura e tente novamente.");
                yield updated;
            }
            case ATRASADO -> {
                requireStatus(schedule, ProjectStatus.ATRASADO, today,
                        "Este projeto ainda está dentro do prazo e não pode ser movido para Atrasado. "
                        + "O início ou o término previsto precisa ser anterior a hoje.");
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
                        "Não é possível retornar para A iniciar porque o projeto continuaria atrasado. "
                        + "Abra Editar e ajuste as datas previstas para datas futuras.");
                yield updated;
            }
            case ATRASADO -> {
                requireStatus(schedule, ProjectStatus.ATRASADO, today,
                        "Este projeto ainda está dentro do prazo e não pode ser movido para Atrasado. "
                        + "Abra Editar e ajuste o início ou o término previsto para uma data anterior a hoje.");
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
                        "Não é possível mover para A iniciar porque as datas ainda indicam atraso. "
                        + "Abra Editar, remova o início realizado e ajuste as datas previstas para datas futuras.");
                yield schedule;
            }
            case EM_ANDAMENTO -> {
                requireStatus(schedule, ProjectStatus.EM_ANDAMENTO, today,
                        "Não é possível mover para Em andamento porque as datas ainda indicam atraso. "
                        + "Abra Editar, informe o início realizado e ajuste o término previsto para hoje "
                        + "ou uma data futura.");
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
                throw new TransitionNotAllowedException(
                        "Não é possível mover um projeto concluído diretamente para A iniciar. "
                        + "Abra Editar, remova o término e o início realizados e ajuste as datas previstas "
                        + "para datas futuras.");
            }
            case EM_ANDAMENTO -> {
                ProjectSchedule updated = schedule.withActualEndDate(null);
                requireStatus(updated, ProjectStatus.EM_ANDAMENTO, today,
                        "Não é possível reabrir em Em andamento porque o prazo previsto já venceu. "
                        + "Abra Editar, ajuste o término previsto para hoje ou uma data futura e tente novamente.");
                yield updated;
            }
            case ATRASADO -> {
                ProjectSchedule updated = schedule.withActualEndDate(null);
                requireStatus(updated, ProjectStatus.ATRASADO, today,
                        "Não é possível reabrir como Atrasado porque as datas ainda estão dentro do prazo. "
                        + "Abra Editar e informe um término previsto anterior a hoje ou remova o início realizado "
                        + "e informe um início previsto anterior a hoje.");
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
