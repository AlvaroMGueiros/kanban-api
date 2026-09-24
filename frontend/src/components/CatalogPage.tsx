import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { catalogApi } from '../api/catalogApi';
import type { Department, Responsible, ResponsibleRequest } from '../types/project';
import { Alert } from './Alert';
import { ConfirmDeleteModal } from './ConfirmDeleteModal';

type CatalogMode = 'responsibles' | 'departments';
type CatalogRecord = Responsible | Department;

interface CatalogPageProps {
  mode: CatalogMode;
}

const emptyResponsible: ResponsibleRequest = { name: '', email: '', role: '', department: '' };

function getErrorMessage(error: unknown): string {
  return error instanceof Error ? error.message : 'Não foi possível concluir a operação.';
}

export function CatalogPage({ mode }: CatalogPageProps) {
  const [records, setRecords] = useState<CatalogRecord[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [responsibleForm, setResponsibleForm] = useState<ResponsibleRequest>(emptyResponsible);
  const [departmentName, setDepartmentName] = useState('');
  const [message, setMessage] = useState<{ tone: 'danger' | 'success'; text: string } | null>(null);
  const [recordToDelete, setRecordToDelete] = useState<CatalogRecord | null>(null);
  const [busy, setBusy] = useState(false);

  const loadRecords = useCallback(async () => {
    try {
      if (mode === 'responsibles') {
        const [responsibles, departmentList] = await Promise.all([
          catalogApi.listResponsibles(),
          catalogApi.listDepartments(),
        ]);
        setRecords(responsibles);
        setDepartments(departmentList);
      } else {
        setRecords(await catalogApi.listDepartments());
      }
    } catch (error) {
      setMessage({ tone: 'danger', text: getErrorMessage(error) });
    }
  }, [mode]);

  useEffect(() => {
    clearForm();
    void loadRecords();
  }, [loadRecords]);

  function clearForm() {
    setEditingId(null);
    setResponsibleForm(emptyResponsible);
    setDepartmentName('');
  }

  async function submitForm(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setMessage(null);
    try {
      if (mode === 'responsibles') {
        if (editingId) {
          await catalogApi.updateResponsible(editingId, responsibleForm);
        } else {
          await catalogApi.createResponsible(responsibleForm);
        }
        setResponsibleForm(emptyResponsible);
      } else {
        if (editingId) {
          await catalogApi.updateDepartment(editingId, departmentName);
        } else {
          await catalogApi.createDepartment(departmentName);
        }
        setDepartmentName('');
      }
      clearForm();
      setMessage({ tone: 'success', text: 'Cadastro salvo com sucesso.' });
      await loadRecords();
    } catch (error) {
      setMessage({ tone: 'danger', text: getErrorMessage(error) });
    } finally {
      setBusy(false);
    }
  }

  function editRecord(record: CatalogRecord) {
    setEditingId(record.id);
    if (mode === 'responsibles') {
      const responsible = record as Responsible;
      setResponsibleForm({
        name: responsible.name,
        email: responsible.email,
        role: responsible.role,
        department: responsible.department,
      });
    } else {
      setDepartmentName(record.name);
    }
  }

  async function confirmDeletion() {
    if (!recordToDelete) {
      return;
    }
    setBusy(true);
    try {
      if (mode === 'responsibles') {
        await catalogApi.deleteResponsible(recordToDelete.id);
      } else {
        await catalogApi.deleteDepartment(recordToDelete.id);
      }
      setRecordToDelete(null);
      setMessage({ tone: 'success', text: 'Cadastro excluído com sucesso.' });
      await loadRecords();
    } catch (error) {
      setMessage({ tone: 'danger', text: getErrorMessage(error) });
    } finally {
      setBusy(false);
    }
  }

  const title = mode === 'responsibles' ? 'Responsáveis' : 'Secretarias';

  return (
    <div className="catalog-page">
      <header className="page-header">
        <div><span className="eyebrow">CADASTROS</span><h1>{title}</h1><p>Gerencie os dados utilizados pelos projetos.</p></div>
      </header>
      {message && <Alert message={message.text} tone={message.tone} onClose={() => setMessage(null)} />}
      <section className="catalog-panel">
        <form className="catalog-form" onSubmit={submitForm}>
          <h2>{editingId ? 'Editar cadastro' : `Nova ${mode === 'responsibles' ? 'pessoa responsável' : 'secretaria'}`}</h2>
          {mode === 'responsibles' ? (
            <div className="catalog-form__grid">
              <input aria-label="Nome" required placeholder="Nome" value={responsibleForm.name} onChange={(event) => setResponsibleForm((current) => ({ ...current, name: event.target.value }))} />
              <input aria-label="E-mail" required type="email" placeholder="E-mail" value={responsibleForm.email} onChange={(event) => setResponsibleForm((current) => ({ ...current, email: event.target.value }))} />
              <input aria-label="Cargo" required placeholder="Cargo" value={responsibleForm.role} onChange={(event) => setResponsibleForm((current) => ({ ...current, role: event.target.value }))} />
              <select aria-label="Secretaria" required value={responsibleForm.department} onChange={(event) => setResponsibleForm((current) => ({ ...current, department: event.target.value }))}>
                <option value="">Selecione a secretaria</option>
                {departments.map((department) => <option key={department.id} value={department.name}>{department.name}</option>)}
              </select>
            </div>
          ) : (
            <input aria-label="Nome da secretaria" required maxLength={120} placeholder="Nome da secretaria" value={departmentName} onChange={(event) => setDepartmentName(event.target.value)} />
          )}
          <div className="catalog-form__actions">
            {editingId && <button type="button" className="secondary-button" onClick={clearForm}>Cancelar</button>}
            <button type="submit" className="primary-button" disabled={busy}>Salvar</button>
          </div>
        </form>
        <div className="catalog-list">
          {records.map((record) => (
            <article key={record.id} className="catalog-row">
              <div><strong>{record.name}</strong>{mode === 'responsibles' && <span>{(record as Responsible).email} · {(record as Responsible).role} · {(record as Responsible).department}</span>}</div>
              <div><button type="button" onClick={() => editRecord(record)}>Editar</button><button type="button" className="delete-button" onClick={() => setRecordToDelete(record)}>Excluir</button></div>
            </article>
          ))}
          {records.length === 0 && <p className="empty-column">Nenhum cadastro encontrado.</p>}
        </div>
      </section>
      {recordToDelete && <ConfirmDeleteModal subjectName={recordToDelete.name} subjectLabel={mode === 'responsibles' ? 'responsável' : 'secretaria'} deleting={busy} onCancel={() => setRecordToDelete(null)} onConfirm={() => void confirmDeletion()} />}
    </div>
  );
}
