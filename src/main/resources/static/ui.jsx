/* ── Shared UI Components ───────────────────────────────── */
const { useState, useEffect, useRef, useCallback, createContext, useContext } = React;

/* ── Constants ─────────────────────────────────────────── */
const UF_STATES = [
  'AC','AL','AM','AP','BA','CE','DF','ES','GO','MA','MG','MS','MT',
  'PA','PB','PE','PI','PR','RJ','RN','RO','RR','RS','SC','SE','SP','TO'
];
window.UF_STATES = UF_STATES;

const todayStr = () => new Date().toISOString().slice(0, 10);
const nowStr = () => { const d = new Date(); d.setMinutes(d.getMinutes() - d.getTimezoneOffset()); return d.toISOString().slice(0, 16); };
window.todayStr = todayStr;
window.nowStr = nowStr;

/* ── Toast System ──────────────────────────────────────── */
const ToastCtx = createContext();

function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  const add = useCallback((type, messages) => {
    const id = Date.now() + Math.random();
    const msgs = Array.isArray(messages) ? messages : [messages];
    setToasts(t => [...t, { id, type, msgs }]);
    setTimeout(() => setToasts(t => t.filter(x => x.id !== id)), 5000);
  }, []);
  const toast = {
    success: (m) => add('success', m),
    error:   (m) => add('error', m),
    info:    (m) => add('info', m),
  };
  return React.createElement(ToastCtx.Provider, { value: toast },
    children,
    React.createElement('div', { className: 'fixed top-4 right-4 z-[100] flex flex-col gap-2 max-w-sm' },
      toasts.map(t =>
        React.createElement('div', {
          key: t.id,
          className: `anim-toast rounded-lg px-4 py-3 text-sm font-semibold shadow-lg border ${
            t.type === 'error'   ? 'bg-red-500/15 border-red-500/30 text-red-300' :
            t.type === 'success' ? 'bg-emerald-500/15 border-emerald-500/30 text-emerald-300' :
                                   'bg-blue-500/15 border-blue-500/30 text-blue-300'
          }`
        }, t.msgs.map((m, i) => React.createElement('div', { key: i }, m)))
      )
    )
  );
}

function useToast() { return useContext(ToastCtx); }

/* ── Modal ─────────────────────────────────────────────── */
function Modal({ open, onClose, title, children, width }) {
  if (!open) return null;
  return React.createElement('div', {
    className: 'fixed inset-0 z-50 flex items-center justify-center p-4',
    onClick: (e) => { if (e.target === e.currentTarget) onClose(); }
  },
    React.createElement('div', { className: 'absolute inset-0 bg-black/60 backdrop-blur-sm' }),
    React.createElement('div', {
      className: `anim-slide relative bg-[#14171E] border border-white/[0.06] rounded-xl ${width || 'w-full max-w-lg'} max-h-[90vh] overflow-y-auto`
    },
      React.createElement('div', { className: 'flex items-center justify-between px-6 py-4 border-b border-white/[0.06]' },
        React.createElement('h3', { className: 'text-lg font-bold' }, title),
        React.createElement('button', { onClick: onClose, className: 'text-white/40 hover:text-white/80 text-xl leading-none p-1' }, '✕')
      ),
      React.createElement('div', { className: 'px-6 py-5' }, children)
    )
  );
}

/* ── Pagination ────────────────────────────────────────── */
function Pagination({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null;
  const pages = [];
  const start = Math.max(0, page - 2);
  const end = Math.min(totalPages - 1, page + 2);
  for (let i = start; i <= end; i++) pages.push(i);

  const btn = (label, pg, disabled, active) =>
    React.createElement('button', {
      key: label,
      disabled,
      onClick: () => onPageChange(pg),
      className: `px-3 py-1.5 rounded-lg text-sm font-semibold transition-colors ${
        active ? 'bg-[#FFE600] text-[#111315]' :
        disabled ? 'text-white/20 cursor-not-allowed' :
        'text-white/60 hover:bg-white/[0.06] hover:text-white'
      }`
    }, label);

  return React.createElement('div', { className: 'flex items-center justify-center gap-1 pt-4' },
    btn('‹', page - 1, page === 0, false),
    ...pages.map(p => btn(String(p + 1), p, false, p === page)),
    btn('›', page + 1, page >= totalPages - 1, false)
  );
}

/* ── Badge ─────────────────────────────────────────────── */
function Badge({ active }) {
  return React.createElement('span', {
    className: `inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-bold ${
      active ? 'bg-emerald-500/15 text-emerald-400' : 'bg-white/[0.06] text-white/30'
    }`
  },
    React.createElement('span', { className: `w-1.5 h-1.5 rounded-full ${active ? 'bg-emerald-400' : 'bg-white/20'}` }),
    active ? 'Ativo' : 'Inativo'
  );
}

/* ── Spinner ───────────────────────────────────────────── */
function Spinner({ size }) {
  return React.createElement('div', {
    className: `animate-spin rounded-full border-2 border-white/10 border-t-[#FFE600] ${size || 'w-6 h-6'}`
  });
}

/* ── Loading ───────────────────────────────────────────── */
function Loading() {
  return React.createElement('div', { className: 'flex items-center justify-center py-20' },
    React.createElement(Spinner, { size: 'w-8 h-8' })
  );
}

/* ── Empty State ───────────────────────────────────────── */
function EmptyState({ message }) {
  return React.createElement('div', { className: 'text-center py-16 text-white/30' },
    React.createElement('div', { className: 'text-4xl mb-3 opacity-40' }, '⌀'),
    React.createElement('p', { className: 'font-semibold' }, message || 'Nenhum registro encontrado.')
  );
}

/* ── Form Field ────────────────────────────────────────── */
function FormField({ label, required, children }) {
  return React.createElement('div', null,
    React.createElement('label', { className: 'block text-sm font-semibold text-white/70 mb-1.5' },
      label, required && React.createElement('span', { className: 'text-red-400 ml-0.5' }, '*')
    ),
    children
  );
}

/* ── State Select ──────────────────────────────────────── */
function StateSelect({ value, onChange, placeholder, ...rest }) {
  return React.createElement('select', {
    value: value || '', onChange, ...rest,
    className: 'w-full bg-[#1A1E27] border border-white/[0.08] rounded-lg px-3 py-2.5 text-sm'
  },
    React.createElement('option', { value: '' }, placeholder || 'Selecione o estado'),
    UF_STATES.map(uf => React.createElement('option', { key: uf, value: uf }, uf))
  );
}

/* ── Confirm action ────────────────────────────────────── */
function ConfirmModal({ open, onClose, onConfirm, title, message }) {
  return React.createElement(Modal, { open, onClose, title: title || 'Confirmar' },
    React.createElement('p', { className: 'text-white/60 mb-6' }, message),
    React.createElement('div', { className: 'flex gap-3 justify-end' },
      React.createElement('button', {
        onClick: onClose,
        className: 'px-4 py-2 rounded-lg text-sm font-semibold bg-white/[0.06] text-white/60 hover:bg-white/[0.1]'
      }, 'Cancelar'),
      React.createElement('button', {
        onClick: onConfirm,
        className: 'px-4 py-2 rounded-lg text-sm font-bold bg-red-500/20 text-red-400 hover:bg-red-500/30'
      }, 'Confirmar')
    )
  );
}

/* ── Page Header ───────────────────────────────────────── */
function PageHeader({ title, subtitle, action }) {
  return React.createElement('div', { className: 'flex items-start justify-between mb-6' },
    React.createElement('div', null,
      React.createElement('h1', { className: 'text-2xl font-extrabold tracking-tight' }, title),
      subtitle && React.createElement('p', { className: 'text-sm text-white/40 mt-1' }, subtitle)
    ),
    action
  );
}

/* ── Primary Button ────────────────────────────────────── */
function BtnPrimary({ children, onClick, disabled, small }) {
  return React.createElement('button', {
    onClick, disabled,
    className: `inline-flex items-center gap-2 font-bold rounded-lg transition-colors
      ${small ? 'px-3 py-1.5 text-xs' : 'px-5 py-2.5 text-sm'}
      ${disabled ? 'bg-[#FFE600]/30 text-[#111315]/60 cursor-not-allowed' : 'bg-[#FFE600] text-[#111315] hover:bg-[#FFE600]/85'}`
  }, children);
}

/* ── Icon Button ───────────────────────────────────────── */
function IconBtn({ onClick, title, children, variant }) {
  const cls = variant === 'danger'
    ? 'text-red-400/60 hover:text-red-400 hover:bg-red-400/10'
    : variant === 'success'
    ? 'text-emerald-400/60 hover:text-emerald-400 hover:bg-emerald-400/10'
    : 'text-white/30 hover:text-white/70 hover:bg-white/[0.06]';
  return React.createElement('button', {
    onClick, title,
    className: `p-1.5 rounded-lg transition-colors ${cls}`
  }, children);
}

/* ── SVG Icons (monoline, square caps) ─────────────────── */
const icon = (d, size = 20) => React.createElement('svg', {
  width: size, height: size, viewBox: '0 0 24 24', fill: 'none',
  stroke: 'currentColor', strokeWidth: 2, strokeLinecap: 'square', strokeLinejoin: 'miter'
}, typeof d === 'string'
  ? React.createElement('path', { d })
  : d
);

const Icons = {
  dashboard: () => icon('M3 12h4v9H3zM10 3h4v18h-4zM17 8h4v13h-4z'),
  clubs:     () => icon([
    React.createElement('path', { key: 'a', d: 'M12 2L3 7v6c0 5.25 3.75 10.15 9 11.25C17.25 23.15 21 18.25 21 13V7l-9-5z' }),
  ]),
  stadiums:  () => icon([
    React.createElement('path', { key: 'a', d: 'M2 20h20M4 20V10M20 20V10M2 10l10-7 10 7M8 14h2v6H8zM14 14h2v6h-2z' }),
  ]),
  matches:   () => icon('M8 2v4M16 2v4M3 10h18M5 4h14a2 2 0 012 2v14a2 2 0 01-2 2H5a2 2 0 01-2-2V6a2 2 0 012-2z'),
  retro:     () => icon('M12 8v4l3 3M3 12a9 9 0 1018 0 9 9 0 00-18 0z'),
  edit:      () => icon('M11 4H4v16h16v-7M18.5 2.5a2.12 2.12 0 013 3L12 15l-4 1 1-4 9.5-9.5z', 16),
  trash:     () => icon('M3 6h18M8 6V4h8v2M5 6v14a2 2 0 002 2h10a2 2 0 002-2V6M10 11v6M14 11v6', 16),
  plus:      () => icon('M12 5v14M5 12h14', 16),
  check:     () => icon('M20 6L9 17l-5-5', 16),
  x:         () => icon('M18 6L6 18M6 6l12 12', 16),
  chevDown:  () => icon('M6 9l6 6 6-6', 14),
  power:     () => icon('M12 2v6M18.36 6.64A9 9 0 0112 22a9 9 0 01-6.36-15.36', 16),
};
window.Icons = Icons;

/* ── Logo ──────────────────────────────────────────────── */
function BibLogo({ size }) {
  const s = size || 48;
  return React.createElement('img', {
    src: 'assets/bib-logo.png',
    alt: 'BIB',
    width: s, height: s,
    style: { borderRadius: '50%', objectFit: 'cover', display: 'block' }
  });
}

/* ── Format helpers ────────────────────────────────────── */
function formatDate(d) {
  if (!d) return '—';
  return new Date(d + 'T00:00:00').toLocaleDateString('pt-BR');
}
function formatDateTime(d) {
  if (!d) return '—';
  const dt = new Date(d);
  return dt.toLocaleDateString('pt-BR') + ' ' + dt.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
}

/* ── Export all ─────────────────────────────────────────── */
Object.assign(window, {
  ToastProvider, useToast, Modal, Pagination, Badge, Spinner, Loading,
  EmptyState, FormField, StateSelect, ConfirmModal, PageHeader,
  BtnPrimary, IconBtn, BibLogo, formatDate, formatDateTime, icon,
});
