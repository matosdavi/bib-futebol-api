/* ── Dashboard Page ─────────────────────────────────────── */
const { useState: useSt, useEffect: useEf } = React;

function RankingTable({ data, title, subtitle, type }) {
  if (!data || data.length === 0) return React.createElement(EmptyState, { message: 'Sem dados de ranking.' });

  const isGoals = type === 'goals';
  const headers = isGoals
    ? ['#','Clube','PJ','Gols','SG']
    : ['#','Clube','PJ','V','E','D','Pts'];

  return React.createElement('div', { className: 'bg-[#14171E] rounded-xl border border-white/[0.06] overflow-hidden' },
    React.createElement('div', { className: 'px-5 py-4 border-b border-white/[0.06]' },
      React.createElement('h3', { className: 'font-bold text-base' }, title),
      subtitle && React.createElement('p', { className: 'text-xs text-white/40 mt-0.5' }, subtitle)
    ),
    React.createElement('div', { className: 'overflow-x-auto' },
      React.createElement('table', null,
        React.createElement('thead', null,
          React.createElement('tr', null,
            headers.map(h => React.createElement('th', { key: h, className: h === '#' ? 'w-12 text-center' : '' }, h))
          )
        ),
        React.createElement('tbody', null,
          data.slice(0, 10).map((r, i) => {
            const pos = i + 1;
            const posClass = pos <= 3 ? 'hi text-xs' : 'font-mono-num text-white/40 text-xs';
            return React.createElement('tr', { key: r.clubId || i },
              React.createElement('td', { className: 'text-center' },
                React.createElement('span', { className: posClass },
                  String(pos).padStart(2, '0')
                )
              ),
              React.createElement('td', { className: 'font-semibold' }, r.clubName || r.name || '—'),
              React.createElement('td', { className: 'font-mono-num text-white/60' }, r.totalMatches ?? r.matches ?? '—'),
              ...(isGoals ? [
                React.createElement('td', { key: 'g', className: 'font-mono-num text-[#FFE600] font-bold' }, r.goalsFor ?? '—'),
                React.createElement('td', { key: 'sg', className: 'font-mono-num ' + ((r.goalBalance ?? 0) >= 0 ? 'text-emerald-400' : 'text-red-400') },
                  (r.goalBalance ?? 0) >= 0 ? `+${r.goalBalance ?? 0}` : r.goalBalance
                ),
              ] : [
                React.createElement('td', { key: 'w', className: 'font-mono-num text-emerald-400' }, r.wins ?? '—'),
                React.createElement('td', { key: 'd', className: 'font-mono-num text-white/50' }, r.draws ?? '—'),
                React.createElement('td', { key: 'l', className: 'font-mono-num text-red-400' }, r.losses ?? '—'),
                React.createElement('td', { key: 'p', className: 'font-mono-num text-[#FFE600] font-bold text-base' }, r.points ?? '—'),
              ])
            );
          })
        )
      )
    )
  );
}

function RecentMatches({ matches }) {
  if (!matches || matches.length === 0) return React.createElement(EmptyState, { message: 'Nenhuma partida recente.' });

  return React.createElement('div', { className: 'bg-[#14171E] rounded-xl border border-white/[0.06] overflow-hidden' },
    React.createElement('div', { className: 'px-5 py-4 border-b border-white/[0.06]' },
      React.createElement('h3', { className: 'font-bold text-base' }, 'Últimas Partidas')
    ),
    React.createElement('div', { className: 'divide-y divide-white/[0.04]' },
      matches.slice(0, 8).map((m, i) =>
        React.createElement('div', { key: m.id || i, className: 'px-5 py-3 flex items-center gap-4' },
          React.createElement('span', { className: 'font-mono-num text-xs text-white/30 w-16 shrink-0' },
            m.matchDateTime ? formatDateTime(m.matchDateTime).split(' ')[0] : '—'
          ),
          React.createElement('div', { className: 'flex items-center gap-3 flex-1 min-w-0' },
            React.createElement('span', { className: 'text-sm font-semibold text-right flex-1 truncate' }, m.homeClub?.name || '—'),
            React.createElement('div', { className: 'flex items-center gap-1.5 shrink-0 bg-white/[0.04] rounded-lg px-3 py-1' },
              React.createElement('span', { className: 'font-mono-num font-bold text-base w-5 text-center' }, m.homeClubGoals ?? '–'),
              React.createElement('span', { className: 'text-white/20 text-xs' }, '×'),
              React.createElement('span', { className: 'font-mono-num font-bold text-base w-5 text-center' }, m.awayClubGoals ?? '–'),
            ),
            React.createElement('span', { className: 'text-sm font-semibold flex-1 truncate' }, m.awayClub?.name || '—'),
          ),
          React.createElement('span', { className: 'text-xs text-white/25 truncate max-w-[120px] shrink-0 hidden sm:block' }, m.stadium?.name || '')
        )
      )
    )
  );
}

function Dashboard() {
  const toast = useToast();
  const [loading, setLoading] = useSt(true);
  const [rankPoints, setRankPoints] = useSt([]);
  const [rankGoals, setRankGoals] = useSt([]);
  const [matches, setMatches] = useSt([]);

  useEf(() => {
    let mounted = true;
    async function load() {
      setLoading(true);
      try {
        const [rp, rg, m] = await Promise.allSettled([
          api.getRankingPoints(),
          api.getRankingGoals(),
          api.getMatches({ page: 0, size: 8, sort: 'matchDateTime,desc' }),
        ]);
        if (!mounted) return;
        if (rp.status === 'fulfilled') setRankPoints(Array.isArray(rp.value) ? rp.value : rp.value?.content || []);
        if (rg.status === 'fulfilled') setRankGoals(Array.isArray(rg.value) ? rg.value : rg.value?.content || []);
        if (m.status === 'fulfilled') setMatches(m.value?.content || (Array.isArray(m.value) ? m.value : []));
        const failed = [rp, rg, m].filter(r => r.status === 'rejected');
        if (failed.length === 3) toast.error('Não foi possível conectar ao servidor.');
      } catch (e) {
        toast.error(e.messages || ['Erro ao carregar dashboard.']);
      }
      if (mounted) setLoading(false);
    }
    load();
    return () => { mounted = false; };
  }, []);

  if (loading) return React.createElement(Loading);

  return React.createElement('div', { className: 'anim-fade' },
    React.createElement(PageHeader, { title: 'Dashboard', subtitle: 'Rankings e resultados recentes' }),
    React.createElement('div', { className: 'grid grid-cols-1 lg:grid-cols-2 gap-5 mb-6' },
      React.createElement(RankingTable, { data: rankPoints, title: 'Ranking por Pontos', subtitle: 'Classificação geral', type: 'points' }),
      React.createElement(RankingTable, { data: rankGoals, title: 'Ranking por Gols', subtitle: 'Artilharia por clube', type: 'goals' }),
    ),
    React.createElement(RecentMatches, { matches })
  );
}

window.Dashboard = Dashboard;
