CREATE INDEX "responsiblesByDepartment"
    ON kanban.responsibles (lower(department));

CREATE INDEX "projectsByPlannedStartDate"
    ON kanban.projects ("plannedStartDate");

CREATE INDEX "projectsByPlannedEndDate"
    ON kanban.projects ("plannedEndDate");

CREATE INDEX "projectsByActualStartDate"
    ON kanban.projects ("actualStartDate");

CREATE INDEX "projectsByActualEndDate"
    ON kanban.projects ("actualEndDate");
