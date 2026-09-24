import { useEffect, useMemo, useRef, useState } from 'react';
import type { Responsible } from '../types/project';

interface ResponsibleMultiSelectProps {
  responsibles: Responsible[];
  selectedIds: number[];
  onChange: (selectedIds: number[]) => void;
}

function matchesSearch(responsible: Responsible, normalizedSearch: string): boolean {
  const searchableText = [responsible.name, responsible.email, responsible.role, responsible.department]
    .join(' ')
    .toLocaleLowerCase('pt-BR');
  return searchableText.includes(normalizedSearch);
}

export function ResponsibleMultiSelect({ responsibles, selectedIds, onChange }: ResponsibleMultiSelectProps) {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState('');
  const containerRef = useRef<HTMLDivElement>(null);
  const selectedResponsibles = responsibles.filter((responsible) => selectedIds.includes(responsible.id));
  const filteredResponsibles = useMemo(() => {
    const normalizedSearch = search.trim().toLocaleLowerCase('pt-BR');
    if (!normalizedSearch) {
      return responsibles;
    }
    return responsibles.filter((responsible) => matchesSearch(responsible, normalizedSearch));
  }, [responsibles, search]);

  useEffect(() => {
    function closeOnOutsideClick(event: MouseEvent) {
      if (!containerRef.current?.contains(event.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', closeOnOutsideClick);
    return () => document.removeEventListener('mousedown', closeOnOutsideClick);
  }, []);

  function toggleResponsible(responsibleId: number) {
    if (selectedIds.includes(responsibleId)) {
      onChange(selectedIds.filter((id) => id !== responsibleId));
      return;
    }
    onChange([...selectedIds, responsibleId]);
  }

  return (
    <div className="responsible-select" ref={containerRef}>
      <div className="responsible-select__control" onClick={() => setOpen(true)}>
        {selectedResponsibles.map((responsible) => (
          <span className="responsible-chip" key={responsible.id}>
            {responsible.name}
            <button
              type="button"
              aria-label={`Remover ${responsible.name}`}
              onClick={(event) => {
                event.stopPropagation();
                toggleResponsible(responsible.id);
              }}
            >
              ×
            </button>
          </span>
        ))}
        <input
          aria-label="Buscar responsáveis"
          role="combobox"
          aria-expanded={open}
          aria-controls="responsible-options"
          value={search}
          placeholder={selectedIds.length === 0 ? 'Digite um nome para buscar…' : 'Adicionar…'}
          onFocus={() => setOpen(true)}
          onChange={(event) => {
            setSearch(event.target.value);
            setOpen(true);
          }}
          onKeyDown={(event) => {
            if (event.key === 'Escape' && open) {
              event.stopPropagation();
              setOpen(false);
            }
          }}
        />
      </div>
      {open && (
        <div className="responsible-select__menu" id="responsible-options" role="listbox" aria-multiselectable="true">
          {filteredResponsibles.map((responsible) => {
            const selected = selectedIds.includes(responsible.id);
            return (
              <button
                type="button"
                role="option"
                aria-selected={selected}
                className={selected ? 'selected' : ''}
                key={responsible.id}
                onClick={() => toggleResponsible(responsible.id)}
              >
                <span className="responsible-select__check" aria-hidden="true">{selected ? '✓' : ''}</span>
                <span>
                  <strong>{responsible.name}</strong>
                  <small>{responsible.email} · {responsible.department} · {responsible.role}</small>
                </span>
              </button>
            );
          })}
          {filteredResponsibles.length === 0 && <p>Nenhum responsável encontrado.</p>}
        </div>
      )}
    </div>
  );
}
