# BIB — Basket is Better 🏟️

> API REST + interface web para gerenciamento de partidas de futebol desenvolvida como projeto do **Neocamp Brasil** (Mercado Livre).

![Java](https://img.shields.io/badge/Java-26-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.6-brightgreen?style=flat-square&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-produção-blue?style=flat-square&logo=mysql)
![H2](https://img.shields.io/badge/H2-desenvolvimento-lightgrey?style=flat-square)
![JaCoCo](https://img.shields.io/badge/cobertura-JaCoCo-yellow?style=flat-square)

---

## Sumário

- [Visão geral](#visão-geral)
- [Stack tecnológica](#stack-tecnológica)
- [Arquitetura](#arquitetura)
- [Interface web](#interface-web)
- [Modelo de dados](#modelo-de-dados)
- [Endpoints](#endpoints)
- [Respostas paginadas](#respostas-paginadas)
- [Regras de negócio](#regras-de-negócio)
- [Respostas de erro](#respostas-de-erro)
- [Como executar](#como-executar)
- [Testes](#testes)

---

## Visão geral

O **BIB** é uma aplicação full-stack que expõe uma API REST para cadastrar clubes, estádios e partidas de futebol, além de calcular retrospecto geral e confrontos diretos entre clubes e gerar rankings por pontos e gols. A interface web integrada consome a própria API e está disponível diretamente em `http://localhost:8080`.

---

## Stack tecnológica

| Camada | Tecnologia |
|--------|-----------|
| Linguagem | Java 26 |
| Framework web | Spring Boot 4.0.6 (Spring MVC) |
| Persistência | Spring Data JPA + Hibernate |
| Validação | Jakarta Bean Validation |
| BD produção | MySQL |
| BD desenvolvimento | H2 (in-memory) |
| Boilerplate | Lombok |
| Cobertura de testes | JaCoCo |
| Frontend | React 18 (CDN) + Tailwind CSS (CDN) |

---

## Arquitetura

O projeto segue a arquitetura em camadas padrão do Spring Boot:

```
┌──────────────────────────────────────────────────┐
│          Browser  (React SPA — /static)          │
└─────────────────────┬────────────────────────────┘
                      │ HTTP /api/*
┌─────────────────────▼────────────────────────────┐
│              Controller  (@RestController)        │
│   ClubController  │  StadiumController           │
│                    MatchController                │
│                                                  │
│   Request DTOs (record + Bean Validation)        │
│   Response DTOs (record + static factory)        │
│   GlobalExceptionHandler (@RestControllerAdvice) │
└─────────────────────┬────────────────────────────┘
                      │
┌─────────────────────▼────────────────────────────┐
│              Service  (@Service)                  │
│   ClubService  │  StadiumService  │ MatchService  │
│                                                  │
│   Regras de negócio e validações de domínio      │
└─────────────────────┬────────────────────────────┘
                      │
┌─────────────────────▼────────────────────────────┐
│           Repository  (Spring Data JPA)          │
│   ClubRepository  │  StadiumRepository           │
│                    MatchRepository                │
│                                                  │
│   Specifications para filtros dinâmicos          │
│   JPQL/SQL nativo para rankings e retrospecto    │
└─────────────────────┬────────────────────────────┘
                      │
┌─────────────────────▼────────────────────────────┐
│              Banco de dados                      │
│   H2 (dev/test)  │  MySQL (produção)             │
└──────────────────────────────────────────────────┘
```

---

## Interface web

A aplicação inclui uma SPA (Single Page Application) servida pelo próprio Spring Boot em `http://localhost:8080`. Não requer build step — os arquivos estáticos ficam em `src/main/resources/static/` e são carregados pelo navegador via CDN (React 18 + Tailwind CSS).

### Páginas disponíveis

| Página | Funcionalidade |
|--------|---------------|
| **Dashboard** | Rankings por pontos e gols + últimas partidas |
| **Clubes** | CRUD completo, filtros, ativar/desativar |
| **Estádios** | CRUD completo, filtros, ativar/desativar |
| **Partidas** | CRUD completo, paginação, seção de goleadas |
| **Retrospecto** | Histórico geral por clube + confronto direto (H2H) |

### Estrutura dos arquivos frontend

```
src/main/resources/static/
├── index.html          # Ponto de entrada, carrega React via CDN
├── app.jsx             # Shell da aplicação (layout, roteamento)
├── api.jsx             # Client HTTP centralizado (fetch + error handling)
├── ui.jsx              # Componentes reutilizáveis (Modal, Badge, Pagination…)
├── styles.css          # Estilos globais e customizações Tailwind
├── pages/
│   ├── dashboard.jsx   # Rankings e últimas partidas
│   ├── clubs.jsx       # Gestão de clubes
│   ├── stadiums.jsx    # Gestão de estádios
│   ├── matches.jsx     # Gestão de partidas + goleadas
│   └── retrospect.jsx  # Retrospecto e H2H
├── assets/
│   └── bib-logo.png
└── fonts/              # Proxima Nova (Regular, Semibold, Bold, Extrabold)
```

---

## Modelo de dados

```
┌──────────────────────┐         ┌──────────────────────┐
│        Club          │         │       Stadium         │
├──────────────────────┤         ├──────────────────────┤
│ id          UUID  PK │         │ id          UUID  PK │
│ name        VARCHAR  │         │ name        VARCHAR  │
│ state       ENUM     │         │ city        VARCHAR  │
│ foundationDate DATE  │         │ state       ENUM     │
│ active      BOOLEAN  │         │ active      BOOLEAN  │
└──────┬───────────────┘         └──────────┬───────────┘
       │                                    │
       │  home_club_id                      │ stadium_id
       │  away_club_id                      │
       │              ┌─────────────────────┘
       └──────────────►──────────────────────┐
                      │        Match         │
                      ├──────────────────────┤
                      │ id           UUID PK │
                      │ home_club_id  FK     │
                      │ away_club_id  FK     │
                      │ stadium_id    FK     │
                      │ matchDateTime        │
                      │ homeClubGoals INT    │
                      │ awayClubGoals INT    │
                      └──────────────────────┘
```

> **StateEnum** — aceita todas as 27 UFs brasileiras: `AC`, `AL`, `AM`, `AP`, `BA`, `CE`, `DF`, `ES`, `GO`, `MA`, `MG`, `MS`, `MT`, `PA`, `PB`, `PE`, `PI`, `PR`, `RJ`, `RN`, `RO`, `RR`, `RS`, `SC`, `SE`, `SP`, `TO`.

---

## Endpoints

### Clubes — `/api/clubs`

| Método | Rota | Descrição | Status |
|--------|------|-----------|--------|
| `POST` | `/api/clubs` | Cadastra um clube | `201` |
| `GET` | `/api/clubs` | Lista clubes (paginado, filtros) | `200` |
| `GET` | `/api/clubs/{id}` | Busca clube por ID | `200` |
| `PUT` | `/api/clubs/{id}` | Atualiza um clube | `200` |
| `DELETE` | `/api/clubs/{id}` | Desativa um clube (soft delete) | `204` |
| `GET` | `/api/clubs/{id}/retrospect` | Retrospecto geral do clube | `200` |
| `GET` | `/api/clubs/{id}/retrospect/{adversaryId}` | Confronto direto entre dois clubes | `200` |
| `GET` | `/api/clubs/ranking/points` | Ranking por pontos (com stats) | `200` |
| `GET` | `/api/clubs/ranking/goals` | Ranking por gols marcados (com stats) | `200` |

**Parâmetros de filtro — `GET /api/clubs`**

| Parâmetro | Tipo | Padrão | Descrição |
|-----------|------|--------|-----------|
| `name` | `string` | — | Filtra por nome (parcial) |
| `state` | `StateEnum` | — | Filtra por UF |
| `active` | `boolean` | _(sem padrão — retorna todos)_ | `true` = apenas ativos, `false` = apenas inativos |
| `page` / `size` / `sort` | Pageable | `0 / 20` | Paginação |

**Payload — `POST /api/clubs`**
```json
{
  "name": "Flamengo",
  "state": "RJ",
  "foundationDate": "1895-11-15"
}
```

**Payload — `PUT /api/clubs/{id}`** _(o campo `active` permite ativar/desativar via edição)_
```json
{
  "name": "Flamengo",
  "state": "RJ",
  "foundationDate": "1895-11-15",
  "active": false
}
```

**Resposta — `GET /api/clubs/ranking/points`**
```json
[
  {
    "clubId": "...",
    "clubName": "Flamengo",
    "totalMatches": 10,
    "wins": 7,
    "draws": 2,
    "losses": 1,
    "points": 23,
    "goalsFor": 20,
    "goalsAgainst": 8,
    "goalBalance": 12
  }
]
```

---

### Estádios — `/api/stadiums`

| Método | Rota | Descrição | Status |
|--------|------|-----------|--------|
| `POST` | `/api/stadiums` | Cadastra um estádio | `201` |
| `GET` | `/api/stadiums` | Lista estádios (paginado, filtros) | `200` |
| `GET` | `/api/stadiums/{id}` | Busca estádio por ID | `200` |
| `PUT` | `/api/stadiums/{id}` | Atualiza um estádio | `200` |
| `DELETE` | `/api/stadiums/{id}` | Desativa um estádio (soft delete) | `204` |

**Parâmetros de filtro — `GET /api/stadiums`**

| Parâmetro | Tipo | Padrão | Descrição |
|-----------|------|--------|-----------|
| `name` | `string` | — | Filtra por nome (parcial) |
| `city` | `string` | — | Filtra por cidade |
| `state` | `StateEnum` | — | Filtra por UF |
| `active` | `boolean` | _(sem padrão — retorna todos)_ | `true` = apenas ativos, `false` = apenas inativos |
| `page` / `size` / `sort` | Pageable | `0 / 20` | Paginação |

**Payload — `POST /api/stadiums`**
```json
{
  "name": "Maracanã",
  "city": "Rio de Janeiro",
  "state": "RJ"
}
```

**Payload — `PUT /api/stadiums/{id}`**
```json
{
  "name": "Maracanã",
  "city": "Rio de Janeiro",
  "state": "RJ",
  "active": true
}
```

---

### Partidas — `/api/matches`

| Método | Rota | Descrição | Status |
|--------|------|-----------|--------|
| `POST` | `/api/matches` | Registra uma partida | `201` |
| `GET` | `/api/matches` | Lista partidas (paginado, filtros) | `200` |
| `GET` | `/api/matches/{id}` | Busca partida por ID | `200` |
| `PUT` | `/api/matches/{id}` | Atualiza uma partida | `200` |
| `DELETE` | `/api/matches/{id}` | Remove uma partida | `204` |
| `GET` | `/api/matches/blowouts` | Partidas com diferença ≥ 3 gols | `200` |

**Parâmetros de filtro — `GET /api/matches`**

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `clubId` | `UUID` | Filtra partidas de um clube |
| `stadiumId` | `UUID` | Filtra partidas de um estádio |
| `page` / `size` / `sort` | Pageable | Paginação |

**Payload — `POST / PUT /api/matches`**
```json
{
  "homeClubId": "uuid-do-clube-mandante",
  "awayClubId": "uuid-do-clube-visitante",
  "stadiumId":  "uuid-do-estadio",
  "matchDateTime": "2024-06-08T16:00:00",
  "homeClubGoals": 2,
  "awayClubGoals": 1
}
```

**Resposta — `GET /api/matches/{id}`**
```json
{
  "id": "...",
  "homeClub": { "id": "...", "name": "Flamengo", "state": "RJ", "active": true },
  "awayClub": { "id": "...", "name": "Fluminense", "state": "RJ", "active": true },
  "stadium":  { "id": "...", "name": "Maracanã", "city": "Rio de Janeiro", "state": "RJ", "active": true },
  "homeClubGoals": 2,
  "awayClubGoals": 1,
  "matchDateTime": "2024-06-08T16:00:00"
}
```

---

### Exemplo de resposta — Retrospecto geral

`GET /api/clubs/{id}/retrospect`

```json
{
  "clubId": "...",
  "clubName": "Flamengo",
  "victories": 7,
  "draws": 2,
  "losses": 1,
  "goalsScored": 20,
  "goalsConceded": 8,
  "goalDifference": 12
}
```

### Exemplo de resposta — Confronto direto

`GET /api/clubs/{id}/retrospect/{adversaryId}`

```json
{
  "clubId": "...",
  "clubName": "Flamengo",
  "adversaryClubId": "...",
  "adversaryClubName": "Fluminense",
  "victories": 5,
  "draws": 3,
  "losses": 2,
  "goalsScored": 18,
  "goalsConceded": 12,
  "goalDifference": 6,
  "totalMatches": 10,
  "matches": [ ... ]
}
```

---

## Respostas paginadas

Os endpoints de listagem (`GET /api/clubs`, `GET /api/stadiums`, `GET /api/matches`) retornam um envelope de paginação:

```json
{
  "content": [ ... ],
  "totalElements": 42,
  "totalPages": 5,
  "number": 0
}
```

Os endpoints de ranking (`/ranking/points`, `/ranking/goals`) retornam um array simples (sem paginação), ordenado pelo critério correspondente.

---

## Regras de negócio

### Clubes
- Nome único por UF — tentativa de duplicata retorna `409 Conflict`
- `foundationDate` não pode ser no futuro (`@PastOrPresent`)
- Exclusão é **lógica** (soft delete via campo `active`) — o registro nunca é removido do banco
- O filtro `active` é opcional; sem ele, a listagem retorna ativos e inativos
- Apenas clubes ativos podem participar de partidas
- O campo `active` pode ser alterado via `PUT` ou pelo botão Desativar/Reativar na interface

### Estádios
- Nome único globalmente — tentativa de duplicata retorna `409 Conflict`
- Exclusão é **lógica** (soft delete) — o registro nunca é removido do banco
- O filtro `active` é opcional; sem ele, a listagem retorna ativos e inativos
- O campo `active` pode ser alterado via `PUT` ou pelo botão Desativar/Reativar na interface

### Partidas
- Clube mandante e visitante não podem ser o mesmo (`422`)
- Ambos os clubes precisam estar **ativos** — caso contrário `409 Conflict`
- A data de fundação de cada clube deve ser anterior à data da partida (`409`)
- `matchDateTime` não pode ser no futuro (`@PastOrPresent`)
- `homeClubGoals` e `awayClubGoals` são obrigatórios e não podem ser negativos
- Cada clube só pode ter uma partida a cada **48 horas** (`422`)
- Um estádio só pode sediar **uma partida por dia** (`422`)

---

## Respostas de erro

Todos os erros seguem o mesmo contrato:

```json
{
  "timestamp": "2024-06-08T16:00:00",
  "status": 422,
  "error": "Business Rule Violation",
  "messages": ["Club already has a match scheduled within 48 hours of this date."],
  "path": "/api/matches"
}
```

| Status | Situação |
|--------|----------|
| `400 Bad Request` | Payload inválido ou JSON malformado |
| `404 Not Found` | Recurso não encontrado |
| `409 Conflict` | Duplicata de nome ou clube inativo |
| `422 Unprocessable Entity` | Violação de regra de negócio |
| `500 Internal Server Error` | Erro inesperado |

---

## Como executar

### Pré-requisitos

- Java 26
- Maven (ou use o wrapper `./mvnw`)
- Docker (para rodar com MySQL)

### Desenvolvimento (H2 in-memory)

A configuração padrão já usa H2, sem dependências externas:

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.  
A interface web está disponível na mesma URL, sem configuração adicional.

Console do H2 disponível em `http://localhost:8080/h2-console`:

| Campo | Valor |
|-------|-------|
| JDBC URL | `jdbc:h2:mem:bibdb` |
| User | `sa` |
| Password | `password` |

### Produção (MySQL via Docker)

Suba o banco com Docker e inicie a aplicação:

```bash
docker compose up -d
./mvnw spring-boot:run
```

Na primeira inicialização, o Hibernate cria o schema e o Spring executa automaticamente o `data.sql`, populando o banco com clubes, estádios e partidas de exemplo. Nas reinicializações seguintes os dados já existentes são preservados.

Para resetar o banco e reinserir os dados do zero:

```bash
docker compose down -v
docker compose up -d
./mvnw spring-boot:run
```

| Configuração | Valor |
|---|---|
| Host | `localhost:3306` |
| Database | `bib_database` |
| User | `root` |
| Password | `rootpassword` |

---

## Testes

```bash
# Executar todos os testes
./mvnw test

# Executar testes e gerar relatório de cobertura (JaCoCo)
./mvnw verify
```

O relatório de cobertura é gerado em `target/site/jacoco/index.html`.

A configuração do JaCoCo exclui entidades, DTOs, enums e a classe principal, focando a métrica nas camadas de **controller**, **service** e **repository**.

### Cobertura por camada

| Camada | O que é testado |
|--------|----------------|
| Controllers | Status HTTP, payload de resposta, tratamento de erros via MockMvc |
| Services | Regras de negócio, exceções, persistência via mocks Mockito |
| Repositories | Queries JPQL/nativas cobertas indiretamente pelos testes de serviço |
