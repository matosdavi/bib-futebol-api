/* ── Matches Page ───────────────────────────────────────── */
const { useState: useSt4, useEffect: useEf4, useCallback: useCb4 } = React;

function MatchForm({ match, onClose, onSaved }) {
  const toast = useToast();
  const [clubs, setClubs] = useSt4([]);
  const [stadiums, setStadiums] = useSt4([]);
  const [form, setForm] = useSt4({
    homeClubId: match?.homeClub?.id || '',
    awayClubId: match?.awayClub?.id || '',
    stadiumId: match?.stadium?.id || '',
    matchDateTime: match?.matchDateTime ? match.matchDateTime.slice(0, 16) : '',
    homeClubGoals: match?.homeClubGoals ?? '',
    awayClubGoals: match?.awayClubGoals ?? '',
  });
  const [saving, setSaving] = useSt4(false);
  const isEdit = !!match?.id;

  useEf4(() => {
    Promise.allSettled([api.getActiveClubs(), api.getActiveStadiums()])
      .then(([c, s]) => {
        if (c.status === 'fulfilled') setClubs(c.value?.content || (Array.isArray(c.value) ? c.value : []));
        if (s.status === 'fulfilled') setStadiums(s.value?.content || (Array.isArray(s.value) ? s.value : []));
      });
  }, []);

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const submit = async (e) => {
    e.preventDefault();
    if (!form.homeClubId) { toast.error('Selecione o clube mandante.'); return; }
    if (!form.awayClubId) { toast.error('Selecione o clube visitante.'); return; }
    if (String(form.homeClubId) === String(form.awayClubId)) { toast.error('Mandante e visitante devem ser diferentes.'); return; }
    if (!form.stadiumId) { toast.error('Selecione o estádio.'); return; }
    if (!form.matchDateTime) { toast.error('Data/hora é obrigatória.'); return; }

    const payload = {
      homeClubId: form.homeClubId,
      awayClubId: form.awayClubId,
      stadiumId: form.stadiumId,
      matchDateTime: form.matchDateTime,
      homeClubGoals: form.homeClubGoals !== '' ? Number(form.homeClubGoals) : 0,
      awayClubGoals: form.awayClubGoals !== '' ? Number(form.awayClubGoals) : 0,
    };
    setSaving(true);
    try {
      if (isEdit) {
        await api.updateMatch(match.id, payload);
        toast.success('Partida atualizada.');
      } else {
        await api.createMatch(payload);
        toast.success('Partida criada.');
      }
      onSaved();
    } catch (err) {
      toast.error(err.messages || ['Erro ao salvar partida.']);
    }
    setSaving(false);
  };

  const selClass = 'w-full bg-[#1A1E27] border border-white/[0.08] rounded-lg px-3 py-2.5 text-sm';

  return React.createElement('form', { onSubmit: submit, className: 'flex flex-col gap-4' },
    React.createElement('div', { className: 'grid grid-cols-2 gap-4' },
      React.createElement(FormField, { label: 'Mandante', required: true },
        React.createElement('select', {
          value: form.homeClubId, onChange: e => set('homeClubId', e.target.value), className: selClass
        },
          React.createElement('option', { value: '' }, 'Selecione…'),
          clubs.map(c => React.createElement('option', { key: c.id, value: c.id }, c.name))
        )
      ),
      React.createElement(FormField, { label: 'Visitante', required: true },
        React.createElement('select', {
          value: form.awayClubId, onChange: e => set('awayClubId', e.target.value), className: selClass
        },
          React.createElement('option', { value: '' }, 'Selecione…'),
          clubs.filter(c => String(c.id) !== String(form.homeClubId)).map(c =>
            React.createElement('option', { key: c.id, value: c.id }, c.name)
          )
        )
      ),
    ),
    React.createElement(FormField, { label: 'Estádio', required: true },
      React.createElement('select', {
        value: form.stadiumId, onChange: e => set('stadiumId', e.target.value), className: selClass
      },
        React.createElement('option', { value: '' }, 'Selecione…'),
        stadiums.map(s => React.createElement('option', { key: s.id, value: s.id }, `${s.name} — ${s.city || s.state}`))
      )
    ),
    React.createElement(FormField, { label: 'Data e Hora', required: true },
      React.createElement('input', {
        type: 'datetime-local', value: form.matchDateTime,
        max: nowStr(),
        onChange: e => set('matchDateTime', e.target.value),
        className: selClass,
      })
    ),
    React.createElement('div', { className: 'grid grid-cols-2 gap-4' },
      React.createElement(FormField, { label: 'Gols Mandante' },
        React.createElement('input', {
          type: 'number', min: 0, max: 99, value: form.homeClubGoals,
          onChange: e => set('homeClubGoals', e.target.value),
          className: selClass, placeholder: '0'
        })
      ),
      React.createElement(FormField, { label: 'Gols Visitante' },
        React.createElement('input', {
          type: 'number', min: 0, max: 99, value: form.awayClubGoals,
          onChange: e => set('awayClubGoals', e.target.value),
          className: selClass, placeholder: '0'
        })
      ),
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

/* ── Blowouts Section ──────────────────────────────────── */
function BlowoutsSection({ blowouts }) {
  if (!blowouts || blowouts.length === 0) return null;

  return React.createElement('div', { className: 'bg-[#14171E] rounded-xl border border-white/[0.06] overflow-hidden' },
    React.createElement('div', { className: 'px-5 py-4 border-b border-white/[0.06] flex items-center gap-3' },
      React.createElement('span', { className: 'hi text-sm' }, 'GOLEADAS'),
      React.createElement('span', { className: 'text-xs text-white/40' }, `${blowouts.length} partida${blowouts.length > 1 ? 's' : ''} com 3+ gols`)
    ),
    React.createElement('div', { className: 'divide-y divide-white/[0.04]' },
      blowouts.map((m, i) => {
        const totalGoals = (m.homeClubGoals || 0) + (m.awayClubGoals || 0);
        return React.createElement('div', { key: m.id || i, className: 'px-5 py-3.5 flex items-center gap-4' },
          React.createElement('span', { className: 'font-mono-num text-xs text-white/25 w-16 shrink-0' },
            m.matchDateTime ? formatDateTime(m.matchDateTime).split(' ')[0] : '—'
          ),
          React.createElement('div', { className: 'flex items-center gap-3 flex-1 min-w-0' },
            React.createElement('span', { className: `text-sm font-semibold text-right flex-1 truncate ${(m.homeClubGoals || 0) > (m.awayClubGoals || 0) ? 'text-[#FFE600]' : ''}` },
              m.homeClub?.name || '—'
            ),
            React.createElement('div', { className: 'flex items-center gap-1.5 shrink-0 bg-[#FFE600]/10 border border-[#FFE600]/20 rounded-lg px-3 py-1.5' },
              React.createElement('span', { className: 'font-mono-num font-bold text-lg w-5 text-center text-[#FFE600]' }, m.homeClubGoals ?? 0),
              React.createElement('span', { className: 'text-[#FFE600]/40 text-xs' }, '×'),
              React.createElement('span', { className: 'font-mono-num font-bold text-lg w-5 text-center text-[#FFE600]' }, m.awayClubGoals ?? 0),
            ),
            React.createElement('span', { className: `text-sm font-semibold flex-1 truncate ${(m.awayClubGoals || 0) > (m.homeClubGoals || 0) ? 'text-[#FFE600]' : ''}` },
              m.awayClub?.name || '—'
            ),
          ),
          React.createElement('span', { className: 'font-mono-num text-xs bg-[#FFE600]/10 text-[#FFE600] px-2 py-0.5 rounded-full font-bold shrink-0' },
            `${totalGoals} gols`
          ),
        );
      })
    )
  );
}

/* ── Matches Page ──────────────────────────────────────── */
function MatchesPage() {
  const toast = useToast();
  const [data, setData] = useSt4({ content: [], totalPages: 0, totalElements: 0 });
  const [blowouts, setBlowouts] = useSt4([]);
  const [loading, setLoading] = useSt4(true);
  const [page, setPage] = useSt4(0);
  const [modal, setModal] = useSt4({ open: false, match: null });
  const [confirm, setConfirm] = useSt4({ open: false, match: null });
  const [tab, setTab] = useSt4('list'); // 'list' | 'blowouts'
  const size = 10;

  const loadList = useCb4(async () => {
    setLoading(true);
    try {
      const res = await api.getMatches({ page, size, sort: 'matchDateTime,desc' });
      setData(res);
    } catch (err) {
      toast.error(err.messages || ['Erro ao carregar partidas.']);
    }
    setLoading(false);
  }, [page]);

  const loadBlowouts = useCb4(async () => {
    try {
      const res = await api.getBlowouts();
      setBlowouts(Array.isArray(res) ? res : res?.content || []);
    } catch (err) { /* silent — optional section */ }
  }, []);

  useEf4(() => { loadList(); loadBlowouts(); }, [loadList]);

  const handleDelete = async () => {
    try {
      await api.deleteMatch(confirm.match.id);
      toast.success('Partida excluída.');
      setConfirm({ open: false, match: null });
      loadList();
      loadBlowouts();
    } catch (err) {
      toast.error(err.messages || ['Erro ao excluir partida.']);
    }
  };

  const tabBtn = (id, label) => React.createElement('button', {
    key: id, onClick: () => setTab(id),
    className: `px-4 py-2 text-sm font-bold rounded-lg transition-colors ${
      tab === id ? 'bg-[#FFE600] text-[#111315]' : 'text-white/50 hover:text-white hover:bg-white/[0.06]'
    }`
  }, label);

  return React.createElement('div', { className: 'anim-fade' },
    React.createElement(PageHeader, {
      title: 'Partidas', subtitle: `${data.totalElements || 0} registros`,
      action: React.createElement(BtnPrimary, {
        onClick: () => setModal({ open: true, match: null })
      }, Icons.plus(), ' Nova Partida')
    }),

    /* Tabs */
    React.createElement('div', { className: 'flex gap-2 mb-5' },
      tabBtn('list', 'Todas as Partidas'),
      tabBtn('blowouts', `Goleadas (${blowouts.length})`),
    ),

    tab === 'blowouts'
      ? React.createElement(BlowoutsSection, { blowouts })
      : React.createElement(React.Fragment, null,
        /* Table */
        React.createElement('div', { className: 'bg-[#14171E] rounded-xl border border-white/[0.06] overflow-hidden' },
          loading ? React.createElement(Loading) :
          (!data.content || data.content.length === 0) ? React.createElement(EmptyState) :
          React.createElement('div', { className: 'overflow-x-auto' },
            React.createElement('table', null,
              React.createElement('thead', null,
                React.createElement('tr', null,
                  ['ID','Data','Mandante','','Placar','','Visitante','Estádio','Ações'].map(h =>
                    React.createElement('th', { key: h + Math.random(), className: h === 'Placar' ? 'text-center' : '' }, h)
                  )
                )
              ),
              React.createElement('tbody', null,
                data.content.map(m => {
                  const hw = (m.homeClubGoals ?? 0) > (m.awayClubGoals ?? 0);
                  const aw = (m.awayClubGoals ?? 0) > (m.homeClubGoals ?? 0);
                  return React.createElement('tr', { key: m.id },
                    React.createElement('td', { className: 'font-mono-num text-white/40 text-xs' }, m.id),
                    React.createElement('td', { className: 'font-mono-num text-white/50 text-xs whitespace-nowrap' },
                      formatDateTime(m.matchDateTime)
                    ),
                    React.createElement('td', { className: `font-semibold text-sm ${hw ? 'text-[#FFE600]' : ''}` },
                      m.homeClub?.name || '—'
                    ),
                    React.createElement('td', { className: 'font-mono-num font-bold text-center text-base w-8' }, m.homeClubGoals ?? '–'),
                    React.createElement('td', { className: 'text-white/20 text-center text-xs w-4' }, '×'),
                    React.createElement('td', { className: 'font-mono-num font-bold text-center text-base w-8' }, m.awayClubGoals ?? '–'),
                    React.createElement('td', { className: `font-semibold text-sm ${aw ? 'text-[#FFE600]' : ''}` },
                      m.awayClub?.name || '—'
                    ),
                    React.createElement('td', { className: 'text-white/40 text-xs' }, m.stadium?.name || '—'),
                    React.createElement('td', null,
                      React.createElement('div', { className: 'flex items-center gap-1' },
                        React.createElement(IconBtn, {
                          onClick: () => setModal({ open: true, match: m }), title: 'Editar'
                        }, Icons.edit()),
                        React.createElement(IconBtn, {
                          onClick: () => setConfirm({ open: true, match: m }),
                          title: 'Excluir', variant: 'danger'
                        }, Icons.trash()),
                      )
                    )
                  );
                })
              )
            )
          ),
          React.createElement('div', { className: 'px-4 pb-3' },
            React.createElement(Pagination, { page, totalPages: data.totalPages || 0, onPageChange: setPage })
          )
        )
      ),

    React.createElement(Modal, {
      open: modal.open,
      onClose: () => setModal({ open: false, match: null }),
      title: modal.match?.id ? 'Editar Partida' : 'Nova Partida',
      width: 'w-full max-w-xl'
    },
      React.createElement(MatchForm, {
        match: modal.match,
        onClose: () => setModal({ open: false, match: null }),
        onSaved: () => { setModal({ open: false, match: null }); loadList(); loadBlowouts(); }
      })
    ),

    React.createElement(ConfirmModal, {
      open: confirm.open,
      onClose: () => setConfirm({ open: false, match: null }),
      onConfirm: handleDelete,
      title: 'Excluir Partida',
      message: `Deseja realmente excluir esta partida?`
    })
  );
}

window.MatchesPage = MatchesPage;
