/* ── Stadiums Page ──────────────────────────────────────── */
const { useState: useSt3, useEffect: useEf3, useCallback: useCb3 } = React;

function StadiumForm({ stadium, onClose, onSaved }) {
  const toast = useToast();
  const [form, setForm] = useSt3({
    name: stadium?.name || '',
    city: stadium?.city || '',
    state: stadium?.state || '',
    active: stadium?.active ?? true,
  });
  const [saving, setSaving] = useSt3(false);
  const isEdit = !!stadium?.id;

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const submit = async (e) => {
    e.preventDefault();
    if (!form.name.trim()) { toast.error('Nome é obrigatório.'); return; }
    if (!form.state) { toast.error('Estado é obrigatório.'); return; }
    setSaving(true);
    try {
      if (isEdit) {
        await api.updateStadium(stadium.id, form);
        toast.success('Estádio atualizado.');
      } else {
        await api.createStadium(form);
        toast.success('Estádio criado.');
      }
      onSaved();
    } catch (err) {
      toast.error(err.messages || ['Erro ao salvar estádio.']);
    }
    setSaving(false);
  };

  return React.createElement('form', { onSubmit: submit, className: 'flex flex-col gap-4' },
    React.createElement(FormField, { label: 'Nome', required: true },
      React.createElement('input', {
        type: 'text', value: form.name, maxLength: 120,
        onChange: e => set('name', e.target.value),
        className: 'w-full bg-[#1A1E27] border border-white/[0.08] rounded-lg px-3 py-2.5 text-sm',
        placeholder: 'Ex: Maracanã'
      })
    ),
    React.createElement(FormField, { label: 'Cidade' },
      React.createElement('input', {
        type: 'text', value: form.city, maxLength: 80,
        onChange: e => set('city', e.target.value),
        className: 'w-full bg-[#1A1E27] border border-white/[0.08] rounded-lg px-3 py-2.5 text-sm',
        placeholder: 'Ex: Rio de Janeiro'
      })
    ),
    React.createElement(FormField, { label: 'Estado (UF)', required: true },
      React.createElement(StateSelect, { value: form.state, onChange: e => set('state', e.target.value) })
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

function StadiumsPage() {
  const toast = useToast();
  const [data, setData] = useSt3({ content: [], totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useSt3(true);
  const [page, setPage] = useSt3(0);
  const [filters, setFilters] = useSt3({ name: '', state: '', active: '' });
  const [modal, setModal] = useSt3({ open: false, stadium: null });
  const size = 10;

  const load = useCb3(async () => {
    setLoading(true);
    try {
      const res = await api.getStadiums({ page, size, sort: 'name,asc', ...filters });
      setData(res);
    } catch (err) {
      toast.error(err.messages || ['Erro ao carregar estádios.']);
    }
    setLoading(false);
  }, [page, filters]);

  useEf3(() => { load(); }, [load]);

  const toggleActive = async (s) => {
    try {
      await api.updateStadium(s.id, { ...s, active: !s.active });
      toast.success(s.active ? 'Estádio desativado.' : 'Estádio reativado.');
      load();
    } catch (err) {
      toast.error(err.messages || ['Erro ao alterar status.']);
    }
  };

  return React.createElement('div', { className: 'anim-fade' },
    React.createElement(PageHeader, {
      title: 'Estádios', subtitle: `${data.totalElements || 0} registros`,
      action: React.createElement(BtnPrimary, {
        onClick: () => setModal({ open: true, stadium: null })
      }, Icons.plus(), ' Novo Estádio')
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
              ['ID','Nome','Cidade','UF','Status','Ações'].map(h =>
                React.createElement('th', { key: h }, h)
              )
            )
          ),
          React.createElement('tbody', null,
            data.content.map(s =>
              React.createElement('tr', {
                key: s.id,
                className: s.active ? '' : 'row-inactive'
              },
                React.createElement('td', { className: 'font-mono-num text-white/40 text-xs' }, s.id),
                React.createElement('td', { className: 'font-semibold' }, s.name),
                React.createElement('td', { className: 'text-white/50 text-sm' }, s.city || '—'),
                React.createElement('td', { className: 'font-mono-num text-white/50' }, s.state),
                React.createElement('td', null, React.createElement(Badge, { active: s.active })),
                React.createElement('td', null,
                  React.createElement('div', { className: 'flex items-center gap-1' },
                    React.createElement(IconBtn, {
                      onClick: () => setModal({ open: true, stadium: s }),
                      title: 'Editar'
                    }, Icons.edit()),
                    React.createElement(IconBtn, {
                      onClick: () => toggleActive(s),
                      title: s.active ? 'Desativar' : 'Reativar',
                      variant: s.active ? 'danger' : 'success'
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

    React.createElement(Modal, {
      open: modal.open,
      onClose: () => setModal({ open: false, stadium: null }),
      title: modal.stadium?.id ? 'Editar Estádio' : 'Novo Estádio'
    },
      React.createElement(StadiumForm, {
        stadium: modal.stadium,
        onClose: () => setModal({ open: false, stadium: null }),
        onSaved: () => { setModal({ open: false, stadium: null }); load(); }
      })
    )
  );
}

window.StadiumsPage = StadiumsPage;
