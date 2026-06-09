/* ── Clubs Page ─────────────────────────────────────────── */
const { useState: useSt2, useEffect: useEf2, useCallback: useCb2 } = React;

function ClubForm({ club, onClose, onSaved }) {
  const toast = useToast();
  const [form, setForm] = useSt2({
    name: club?.name || '',
    state: club?.state || '',
    foundationDate: club?.foundationDate || '',
    active: club?.active ?? true,
  });
  const [saving, setSaving] = useSt2(false);
  const isEdit = !!club?.id;

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const submit = async (e) => {
    e.preventDefault();
    if (!form.name.trim()) { toast.error('Nome é obrigatório.'); return; }
    if (!form.state) { toast.error('Estado é obrigatório.'); return; }
    setSaving(true);
    try {
      if (isEdit) {
        await api.updateClub(club.id, form);
        toast.success('Clube atualizado.');
      } else {
        await api.createClub(form);
        toast.success('Clube criado.');
      }
      onSaved();
    } catch (err) {
      toast.error(err.messages || ['Erro ao salvar clube.']);
    }
    setSaving(false);
  };

  return React.createElement('form', { onSubmit: submit, className: 'flex flex-col gap-4' },
    React.createElement(FormField, { label: 'Nome', required: true },
      React.createElement('input', {
        type: 'text', value: form.name, maxLength: 100,
        onChange: e => set('name', e.target.value),
        className: 'w-full bg-[#1A1E27] border border-white/[0.08] rounded-lg px-3 py-2.5 text-sm',
        placeholder: 'Ex: Flamengo'
      })
    ),
    React.createElement(FormField, { label: 'Estado (UF)', required: true },
      React.createElement(StateSelect, { value: form.state, onChange: e => set('state', e.target.value) })
    ),
    React.createElement(FormField, { label: 'Data de Fundação' },
      React.createElement('input', {
        type: 'date', value: form.foundationDate || '',
        max: todayStr(),
        onChange: e => set('foundationDate', e.target.value),
        className: 'w-full bg-[#1A1E27] border border-white/[0.08] rounded-lg px-3 py-2.5 text-sm',
      })
    ),
    isEdit && React.createElement(FormField, { label: 'Status' },
      React.createElement('label', { className: 'flex items-center gap-3 cursor-pointer' },
        React.createElement('div', {
          onClick: () => set('active', !form.active),
          className: `w-10 h-5 rounded-full transition-colors cursor-pointer ${form.active ? 'bg-emerald-500' : 'bg-white/10'}`,
          style: { position: 'relative' }
        },
          React.createElement('div', {
            className: 'absolute top-0.5 w-4 h-4 rounded-full bg-white shadow transition-transform',
            style: { left: form.active ? '22px' : '2px' }
          })
        ),
        React.createElement('span', { className: 'text-sm text-white/60' }, form.active ? 'Ativo' : 'Inativo')
      )
    ),
    React.createElement('div', { className: 'flex gap-3 justify-end pt-2' },
      React.createElement('button', {
        type: 'button', onClick: onClose,
        className: 'px-4 py-2 rounded-lg text-sm font-semibold bg-white/[0.06] text-white/60 hover:bg-white/[0.1]'
      }, 'Cancelar'),
      React.createElement(BtnPrimary, { disabled: saving, onClick: submit },
        saving ? 'Salvando…' : (isEdit ? 'Atualizar' : 'Criar')
      )
    )
  );
}

function ClubsPage() {
  const toast = useToast();
  const [data, setData] = useSt2({ content: [], totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useSt2(true);
  const [page, setPage] = useSt2(0);
  const [filters, setFilters] = useSt2({ name: '', state: '', active: '' });
  const [modal, setModal] = useSt2({ open: false, club: null });
  const size = 10;

  const load = useCb2(async () => {
    setLoading(true);
    try {
      const res = await api.getClubs({ page, size, sort: 'name,asc', ...filters });
      setData(res);
    } catch (err) {
      toast.error(err.messages || ['Erro ao carregar clubes.']);
    }
    setLoading(false);
  }, [page, filters]);

  useEf2(() => { load(); }, [load]);

  const toggleActive = async (club) => {
    try {
      await api.updateClub(club.id, { ...club, active: !club.active });
      toast.success(club.active ? 'Clube desativado.' : 'Clube reativado.');
      load();
    } catch (err) {
      toast.error(err.messages || ['Erro ao alterar status.']);
    }
  };

  return React.createElement('div', { className: 'anim-fade' },
    React.createElement(PageHeader, {
      title: 'Clubes', subtitle: `${data.totalElements || 0} registros`,
      action: React.createElement(BtnPrimary, {
        onClick: () => setModal({ open: true, club: null })
      }, Icons.plus(), ' Novo Clube')
    }),

    /* Filters */
    React.createElement('div', { className: 'flex flex-wrap gap-3 mb-5' },
      React.createElement('input', {
        type: 'text', placeholder: 'Buscar por nome…',
        value: filters.name,
        onChange: e => { setFilters(f => ({ ...f, name: e.target.value })); setPage(0); },
        className: 'bg-[#1A1E27] border border-white/[0.08] rounded-lg px-3 py-2 text-sm w-56'
      }),
      React.createElement('select', {
        value: filters.state,
        onChange: e => { setFilters(f => ({ ...f, state: e.target.value })); setPage(0); },
        className: 'bg-[#1A1E27] border border-white/[0.08] rounded-lg px-3 py-2 text-sm w-28'
      },
        React.createElement('option', { value: '' }, 'UF'),
        UF_STATES.map(uf => React.createElement('option', { key: uf, value: uf }, uf))
      ),
      React.createElement('select', {
        value: filters.active,
        onChange: e => { setFilters(f => ({ ...f, active: e.target.value })); setPage(0); },
        className: 'bg-[#1A1E27] border border-white/[0.08] rounded-lg px-3 py-2 text-sm w-32'
      },
        React.createElement('option', { value: '' }, 'Todos'),
        React.createElement('option', { value: 'true' }, 'Ativos'),
        React.createElement('option', { value: 'false' }, 'Inativos'),
      ),
    ),

    /* Table */
    React.createElement('div', { className: 'bg-[#14171E] rounded-xl border border-white/[0.06] overflow-hidden' },
      loading ? React.createElement(Loading) :
      (!data.content || data.content.length === 0) ? React.createElement(EmptyState) :
      React.createElement('div', { className: 'overflow-x-auto' },
        React.createElement('table', null,
          React.createElement('thead', null,
            React.createElement('tr', null,
              ['ID','Nome','UF','Fundação','Status','Ações'].map(h =>
                React.createElement('th', { key: h }, h)
              )
            )
          ),
          React.createElement('tbody', null,
            data.content.map(c =>
              React.createElement('tr', {
                key: c.id,
                className: c.active ? '' : 'row-inactive'
              },
                React.createElement('td', { className: 'font-mono-num text-white/40 text-xs' }, c.id),
                React.createElement('td', { className: 'font-semibold' }, c.name),
                React.createElement('td', { className: 'font-mono-num text-white/50' }, c.state),
                React.createElement('td', { className: 'text-white/50 text-sm' }, formatDate(c.foundationDate)),
                React.createElement('td', null, React.createElement(Badge, { active: c.active })),
                React.createElement('td', null,
                  React.createElement('div', { className: 'flex items-center gap-1' },
                    React.createElement(IconBtn, {
                      onClick: () => setModal({ open: true, club: c }),
                      title: 'Editar'
                    }, Icons.edit()),
                    React.createElement(IconBtn, {
                      onClick: () => toggleActive(c),
                      title: c.active ? 'Desativar' : 'Reativar',
                      variant: c.active ? 'danger' : 'success'
                    }, Icons.power()),
                  )
                )
              )
            )
          )
        )
      ),
      React.createElement('div', { className: 'px-4 pb-3' },
        React.createElement(Pagination, { page, totalPages: data.totalPages || 0, onPageChange: setPage })
      )
    ),

    /* Modal */
    React.createElement(Modal, {
      open: modal.open,
      onClose: () => setModal({ open: false, club: null }),
      title: modal.club?.id ? 'Editar Clube' : 'Novo Clube'
    },
      React.createElement(ClubForm, {
        club: modal.club,
        onClose: () => setModal({ open: false, club: null }),
        onSaved: () => { setModal({ open: false, club: null }); load(); }
      })
    )
  );
}

window.ClubsPage = ClubsPage;
