/* ── Retrospect Page ────────────────────────────────────── */
const { useState: useSt5, useEffect: useEf5 } = React;

function StatBlock({ label, value, color }) {
  return React.createElement('div', { className: 'text-center' },
    React.createElement('div', { className: `font-mono-num text-3xl font-bold ${color || 'text-white'}` }, value ?? '—'),
    React.createElement('div', { className: 'text-xs text-white/40 mt-1 font-semibold uppercase tracking-wider' }, label)
  );
}

function RetrospectCard({ data, title }) {
  if (!data) return null;
  return React.createElement('div', { className: 'bg-[#14171E] rounded-xl border border-white/[0.06] overflow-hidden' },
    React.createElement('div', { className: 'px-5 py-4 border-b border-white/[0.06]' },
      React.createElement('h3', { className: 'font-bold text-base' }, title || 'Retrospecto')
    ),
    React.createElement('div', { className: 'p-6' },
      React.createElement('div', { className: 'grid grid-cols-2 sm:grid-cols-4 gap-6 mb-6' },
        React.createElement(StatBlock, { label: 'Partidas', value: data.totalMatches ?? ((data.victories ?? 0) + (data.draws ?? 0) + (data.losses ?? 0)), color: 'text-white' }),
        React.createElement(StatBlock, { label: 'Vitórias', value: data.victories, color: 'text-emerald-400' }),
        React.createElement(StatBlock, { label: 'Empates', value: data.draws, color: 'text-white/50' }),
        React.createElement(StatBlock, { label: 'Derrotas', value: data.losses, color: 'text-red-400' }),
      ),
      React.createElement('div', { className: 'grid grid-cols-2 sm:grid-cols-3 gap-6 pt-4 border-t border-white/[0.06]' },
        React.createElement(StatBlock, { label: 'Gols Marcados', value: data.goalsScored, color: 'text-[#FFE600]' }),
        React.createElement(StatBlock, { label: 'Gols Sofridos', value: data.goalsConceded, color: 'text-white/60' }),
        React.createElement(StatBlock, {
          label: 'Saldo de Gols', color: (data.goalDifference ?? 0) >= 0 ? 'text-emerald-400' : 'text-red-400',
          value: (() => {
            const b = data.goalDifference ?? 0;
            return b >= 0 ? `+${b}` : b;
          })()
        }),
      ),
      /* Win rate bar */
      (() => {
        const total = data.totalMatches ?? ((data.victories ?? 0) + (data.draws ?? 0) + (data.losses ?? 0));
        return data.victories != null && total > 0 &&
        React.createElement('div', { className: 'mt-6' },
          React.createElement('div', { className: 'flex items-center justify-between text-xs text-white/40 mb-2' },
            React.createElement('span', null, 'Aproveitamento'),
            React.createElement('span', { className: 'font-mono-num text-[#FFE600] font-bold' },
              `${Math.round((data.victories / total) * 100)}%`
            )
          ),
          React.createElement('div', { className: 'h-2 bg-white/[0.06] rounded-full overflow-hidden flex' },
            React.createElement('div', {
              className: 'h-full bg-emerald-500 rounded-l-full',
              style: { width: `${(data.victories / total) * 100}%` }
            }),
            React.createElement('div', {
              className: 'h-full bg-white/20',
              style: { width: `${(data.draws / total) * 100}%` }
            }),
            React.createElement('div', {
              className: 'h-full bg-red-500 rounded-r-full',
              style: { width: `${(data.losses / total) * 100}%` }
            }),
          ),
          React.createElement('div', { className: 'flex justify-between text-[10px] text-white/30 mt-1.5' },
            React.createElement('span', null, 'V ', data.victories),
            React.createElement('span', null, 'E ', data.draws),
            React.createElement('span', null, 'D ', data.losses),
          )
        );
      })()
    )
  );
}

function RetrospectPage() {
  const toast = useToast();
  const [clubs, setClubs] = useSt5([]);
  const [clubId, setClubId] = useSt5('');
  const [advId, setAdvId] = useSt5('');
  const [retro, setRetro] = useSt5(null);
  const [h2h, setH2h] = useSt5(null);
  const [loading, setLoading] = useSt5(false);
  const [loadingH2h, setLoadingH2h] = useSt5(false);

  useEf5(() => {
    api.getAllClubs().then(res => {
      setClubs(res?.content || (Array.isArray(res) ? res : []));
    }).catch(() => {});
  }, []);

  /* Load general retrospect */
  useEf5(() => {
    if (!clubId) { setRetro(null); return; }
    let cancelled = false;
    setLoading(true);
    api.getRetrospect(clubId)
      .then(r => { if (!cancelled) setRetro(r); })
      .catch(err => { if (!cancelled) toast.error(err.messages || ['Erro ao buscar retrospecto.']); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [clubId]);

  /* Load head-to-head */
  useEf5(() => {
    if (!clubId || !advId) { setH2h(null); return; }
    let cancelled = false;
    setLoadingH2h(true);
    api.getHeadToHead(clubId, advId)
      .then(r => { if (!cancelled) setH2h(r); })
      .catch(err => { if (!cancelled) toast.error(err.messages || ['Erro ao buscar confronto.']); })
      .finally(() => { if (!cancelled) setLoadingH2h(false); });
    return () => { cancelled = true; };
  }, [clubId, advId]);

  const selClass = 'bg-[#1A1E27] border border-white/[0.08] rounded-lg px-3 py-2.5 text-sm';
  const selectedClub = clubs.find(c => String(c.id) === String(clubId));
  const selectedAdv = clubs.find(c => String(c.id) === String(advId));

  return React.createElement('div', { className: 'anim-fade' },
    React.createElement(PageHeader, {
      title: 'Retrospecto',
      subtitle: 'Histórico geral e confronto direto entre clubes'
    }),

    /* Club selectors */
    React.createElement('div', { className: 'bg-[#14171E] rounded-xl border border-white/[0.06] p-5 mb-6' },
      React.createElement('div', { className: 'grid grid-cols-1 sm:grid-cols-2 gap-4' },
        React.createElement(FormField, { label: 'Clube' },
          React.createElement('select', {
            value: clubId,
            onChange: e => { setClubId(e.target.value); setAdvId(''); },
            className: `w-full ${selClass}`
          },
            React.createElement('option', { value: '' }, 'Selecione um clube…'),
            clubs.map(c => React.createElement('option', { key: c.id, value: c.id },
              `${c.name}${!c.active ? ' (inativo)' : ''}`
            ))
          )
        ),
        React.createElement(FormField, { label: 'Adversário (opcional)' },
          React.createElement('select', {
            value: advId,
            onChange: e => setAdvId(e.target.value),
            disabled: !clubId,
            className: `w-full ${selClass} ${!clubId ? 'opacity-40 cursor-not-allowed' : ''}`
          },
            React.createElement('option', { value: '' }, 'Todos os adversários'),
            clubs.filter(c => String(c.id) !== String(clubId)).map(c =>
              React.createElement('option', { key: c.id, value: c.id },
                `${c.name}${!c.active ? ' (inativo)' : ''}`
              )
            )
          )
        ),
      ),
    ),

    /* Results */
    !clubId && React.createElement('div', { className: 'text-center py-16 text-white/25' },
      React.createElement('div', { className: 'text-5xl mb-3 opacity-30' }, '⏱'),
      React.createElement('p', { className: 'font-semibold' }, 'Selecione um clube para ver o retrospecto.')
    ),

    loading && React.createElement(Loading),

    !loading && retro && React.createElement('div', { className: 'space-y-6' },
      React.createElement(RetrospectCard, {
        data: retro,
        title: `Retrospecto Geral — ${selectedClub?.name || 'Clube'}`
      }),

      loadingH2h && React.createElement(Loading),

      !loadingH2h && h2h && React.createElement('div', null,
        React.createElement('h3', { className: 'text-lg font-bold mb-4 flex items-center gap-3' },
          React.createElement('span', null, selectedClub?.name || 'Clube'),
          React.createElement('span', { className: 'text-white/20 text-sm' }, 'vs'),
          React.createElement('span', null, selectedAdv?.name || 'Adversário'),
        ),
        React.createElement(RetrospectCard, {
          data: h2h,
          title: `Confronto Direto`
        }),
      )
    )
  );
}

window.RetrospectPage = RetrospectPage;
