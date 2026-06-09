/* ── API Client ─────────────────────────────────────────── */
const API_BASE = '/api';

const api = {
  async request(url, options = {}) {
    try {
      const res = await fetch(`${API_BASE}${url}`, {
        headers: { 'Content-Type': 'application/json', ...options.headers },
        ...options,
      });
      if (!res.ok) {
        const body = await res.json().catch(() => ({ messages: ['Erro inesperado do servidor.'] }));
        throw { status: res.status, messages: body.messages || [`Erro ${res.status}`] };
      }
      if (res.status === 204) return null;
      return res.json();
    } catch (err) {
      if (err.messages) throw err;
      throw { status: 0, messages: ['Servidor indisponível. Verifique se o backend está rodando em localhost:8080.'] };
    }
  },

  buildQuery(params) {
    const clean = {};
    Object.entries(params).forEach(([k, v]) => {
      if (v !== '' && v !== null && v !== undefined) clean[k] = v;
    });
    return new URLSearchParams(clean).toString();
  },

  /* ── Clubs ──────────────────────────────────────────── */
  getClubs:     (p) => api.request(`/clubs?${api.buildQuery(p)}`),
  getClub:      (id) => api.request(`/clubs/${id}`),
  createClub:   (d) => api.request('/clubs', { method: 'POST', body: JSON.stringify(d) }),
  updateClub:   (id, d) => api.request(`/clubs/${id}`, { method: 'PUT', body: JSON.stringify(d) }),
  deleteClub:   (id) => api.request(`/clubs/${id}`, { method: 'DELETE' }),

  /* ── Rankings ───────────────────────────────────────── */
  getRankingPoints: () => api.request('/clubs/ranking/points'),
  getRankingGoals:  () => api.request('/clubs/ranking/goals'),

  /* ── Retrospect ─────────────────────────────────────── */
  getRetrospect:  (id) => api.request(`/clubs/${id}/retrospect`),
  getHeadToHead:  (id, advId) => api.request(`/clubs/${id}/retrospect/${advId}`),

  /* ── Stadiums ───────────────────────────────────────── */
  getStadiums:     (p) => api.request(`/stadiums?${api.buildQuery(p)}`),
  getStadium:      (id) => api.request(`/stadiums/${id}`),
  createStadium:   (d) => api.request('/stadiums', { method: 'POST', body: JSON.stringify(d) }),
  updateStadium:   (id, d) => api.request(`/stadiums/${id}`, { method: 'PUT', body: JSON.stringify(d) }),
  deleteStadium:   (id) => api.request(`/stadiums/${id}`, { method: 'DELETE' }),

  /* ── Matches ────────────────────────────────────────── */
  getMatches:    (p) => api.request(`/matches?${api.buildQuery(p)}`),
  getMatch:      (id) => api.request(`/matches/${id}`),
  createMatch:   (d) => api.request('/matches', { method: 'POST', body: JSON.stringify(d) }),
  updateMatch:   (id, d) => api.request(`/matches/${id}`, { method: 'PUT', body: JSON.stringify(d) }),
  deleteMatch:   (id) => api.request(`/matches/${id}`, { method: 'DELETE' }),
  getBlowouts:   () => api.request('/matches/blowouts'),

  /* ── Lookups (active only, for selects) ─────────────── */
  getActiveClubs:    () => api.request('/clubs?active=true&size=200&sort=name,asc'),
  getActiveStadiums: () => api.request('/stadiums?active=true&size=200&sort=name,asc'),
  getAllClubs:        () => api.request('/clubs?size=200&sort=name,asc'),
};

window.api = api;
