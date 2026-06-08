# BiB — Basket is Better 🏟️

> API REST para gerenciamento de partidas de futebol desenvolvida como projeto do **Neocamp Brasil** (Mercado Livre).

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
- [Modelo de dados](#modelo-de-dados)
- [Endpoints](#endpoints)
- [Regras de negócio](#regras-de-negócio)
- [Respostas de erro](#respostas-de-erro)
- [Como executar](#como-executar)
- [Configuração do MySQL](#configuração-do-mysql)
- [Testes](#testes)

---

## Visão geral

O **BiB** expõe uma API para cadastrar clubes, estádios e partidas de futebol, além de calcular retrospecto geral e confrontos diretos entre clubes e gerar rankings por pontos e gols.

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

---

## Arquitetura

O projeto segue a arquitetura em camadas padrão do Spring Boot:

```
┌──────────────────────────────────────────────────┐
│                   HTTP Client                    │
└─────────────────────┬────────────────────────────┘
                      │
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
│   Queries nativas para rankings                  │
└─────────────────────┬────────────────────────────┘
                      │
┌─────────────────────▼────────────────────────────┐
│              Banco de dados                      │
│   H2 (dev/test)  │  MySQL (produção)             │
└──────────────────────────────────────────────────┘
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
| `GET` | `/api/clubs/ranking/points` | Ranking de clubes por pontos | `200` |
| `GET` | `/api/clubs/ranking/goals` | Ranking de clubes por gols marcados | `200` |

**Parâmetros de filtro — `GET /api/clubs`**

| Parâmetro | Tipo | Padrão | Descrição |
|-----------|------|--------|-----------|
| `name` | `string` | — | Filtra por nome (parcial) |
| `state` | `StateEnum` | — | Filtra por UF |
| `active` | `boolean` | `true` | Filtra por status |
| `page` / `size` / `sort` | Pageable | `0 / 20` | Paginação |

**Payload — `POST / PUT /api/clubs`**
```json
{
  "name": "Flamengo",
  "state": "RJ",
  "foundationDate": "1895-11-15"
}
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

**Payload — `POST / PUT /api/stadiums`**
```json
{
  "name": "Maracanã",
  "city": "Rio de Janeiro",
  "state": "RJ"
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

**Payload — `POST / PUT /api/matches`**
```json
{
  "homeClubId": "uuid-do-clube-mandante",
  "awayClubId": "uuid-do-clube-visitante",
  "stadiumId": "uuid-do-estadio",
  "matchDateTime": "2024-06-08T16:00:00",
  "homeClubGoals": 2,
  "awayClubGoals": 1
}
```

---

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

## Regras de negócio

### Clubes
- Nome único por UF — tentativa de duplicata retorna `409 Conflict`
- `foundationDate` não pode ser no futuro (`@PastOrPresent`)
- Exclusão é **lógica** (soft delete via campo `active`)
- Apenas clubes ativos aparecem nos filtros padrão e podem participar de partidas

### Estádios
- Nome único globalmente — tentativa de duplicata retorna `409 Conflict`
- Exclusão é **lógica** (soft delete)

### Partidas
- Clube mandante e visitante não podem ser o mesmo (`422`)
- Ambos os clubes precisam estar **ativos** — caso contrário `409 Conflict`
- A data de fundação de cada clube deve ser anterior à data da partida (`409`)
- `matchDateTime` não pode ser no futuro (`@PastOrPresent`)
- Gols não podem ser negativos
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

### Desenvolvimento (H2 in-memory)

A configuração padrão já usa H2, sem dependências externas:

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

Console do H2 disponível em `http://localhost:8080/h2-console`:

| Campo | Valor |
|-------|-------|
| JDBC URL | `jdbc:h2:mem:bibdb` |
| User | `sa` |
| Password | `password` |

---

## Configuração do MySQL

Para usar MySQL em produção, crie o banco e configure o `application.yaml` (ou use variáveis de ambiente):

```sql
CREATE DATABASE bib CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'bib_user'@'%' IDENTIFIED BY 'sua_senha';
GRANT ALL PRIVILEGES ON bib.* TO 'bib_user'@'%';
FLUSH PRIVILEGES;
```

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bib?useSSL=false&serverTimezone=UTC
    driverClassName: com.mysql.cj.jdbc.Driver
    username: bib_user
    password: sua_senha
  jpa:
    database-platform: org.hibernate.dialect.MySQLDialect
    hibernate:
      ddl-auto: update
```

> O schema é gerado automaticamente pelo Hibernate (`ddl-auto: update`). Nenhum script SQL manual é necessário.

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
