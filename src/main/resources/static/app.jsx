/* ── App Shell ──────────────────────────────────────────── */
const {useState: useSt6, useEffect: useEf6} = React;

const NAV_ITEMS = [
    {id: 'dashboard', label: 'Dashboard', icon: () => Icons.dashboard()},
    {id: 'clubs', label: 'Clubes', icon: () => Icons.clubs()},
    {id: 'stadiums', label: 'Estádios', icon: () => Icons.stadiums()},
    {id: 'matches', label: 'Partidas', icon: () => Icons.matches()},
    {id: 'retrospect', label: 'Retrospecto', icon: () => Icons.retro()},
];

function Sidebar({page, onNavigate}) {
    return React.createElement('aside', {
            className: 'w-[220px] shrink-0 h-full bg-[#14171E] border-r border-white/[0.06] flex flex-col'
        },
        /* Logo */
        React.createElement('div', {className: 'px-5 py-5 flex items-center gap-3 border-b border-white/[0.06]'},
            React.createElement(BibLogo, {size: 40}),
            React.createElement('div', null,
                React.createElement('div', {className: 'font-extrabold text-base tracking-tight leading-none'}, 'BIB'),
                React.createElement('div', {className: 'text-[10px] font-semibold text-white/30 tracking-widest uppercase mt-0.5'}, 'Basket is Better'),
            )
        ),

        /* Nav */
        React.createElement('nav', {className: 'flex-1 py-3 px-3 flex flex-col gap-0.5'},
            NAV_ITEMS.map(item =>
                React.createElement('button', {
                        key: item.id,
                        onClick: () => onNavigate(item.id),
                        className: `w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-semibold transition-colors text-left ${
                            page === item.id
                                ? 'bg-[#FFE600] text-[#111315]'
                                : 'text-white/50 hover:text-white hover:bg-white/[0.04]'
                        }`
                    },
                    React.createElement('span', {className: 'shrink-0 opacity-80'}, item.icon()),
                    item.label
                )
            )
        ),

        /* Footer */
        React.createElement('div', {className: 'px-5 py-4 border-t border-white/[0.06]'},
            React.createElement('div', {className: 'text-[10px] text-white/20 font-semibold tracking-wider uppercase'},
                'BIB · Football Manager'
            )
        )
    );
}

function App() {
    const getPage = () => {
        const h = window.location.hash.replace('#/', '').replace('#', '');
        return NAV_ITEMS.some(n => n.id === h) ? h : 'dashboard';
    };

    const [page, setPage] = useSt6(getPage);

    useEf6(() => {
        const handler = () => setPage(getPage());
        window.addEventListener('hashchange', handler);
        return () => window.removeEventListener('hashchange', handler);
    }, []);

    const navigate = (p) => {
        window.location.hash = `/${p}`;
    };

    const renderPage = () => {
        switch (page) {
            case 'dashboard':
                return React.createElement(Dashboard);
            case 'clubs':
                return React.createElement(ClubsPage);
            case 'stadiums':
                return React.createElement(StadiumsPage);
            case 'matches':
                return React.createElement(MatchesPage);
            case 'retrospect':
                return React.createElement(RetrospectPage);
            default:
                return React.createElement(Dashboard);
        }
    };

    return React.createElement(ToastProvider, null,
        React.createElement('div', {className: 'flex h-full'},
            React.createElement(Sidebar, {page, onNavigate: navigate}),
            React.createElement('main', {className: 'flex-1 overflow-y-auto'},
                React.createElement('div', {className: 'max-w-6xl mx-auto px-6 py-6'},
                    renderPage()
                )
            )
        )
    );
}

window.App = App;
