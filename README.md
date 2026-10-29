# PengeOverblik

## Vision

PengeOverblik er en personlig økonomiapplikation, der hjælper brugere med at få et enkelt og struktureret overblik over deres daglige økonomi.

Systemet gør det muligt at oprette en bruger og logge ind, registrere indtægter og udgifter, oprette månedlige budgetter, administrere faste betalinger, organisere transaktioner i kategorier og underkategorier samt få et samlet overblik over månedens økonomi.

---

## Links

Deployed applikation:  
http://165.22.66.250

Kildekode repository:  
https://github.com/Viktoria-Rad/PengeOverblik


---

# Arkitektur

## Systemoversigt

PengeOverblik er bygget som en lagdelt Java-backendapplikation.

Projektet er struktureret omkring:
- **Routes** – definerer de tilgængelige endpoints
- **Controllers** – håndterer HTTP-requests og responses
- **Services** – indeholder forretningslogik og beregninger
- **DAO-lag** – håndterer adgang til databasen
- **Entities** – repræsenterer domænemodellen og databasetabellerne
- **Database** – gemmer brugere, transaktioner, budgetter, kategorier og faste betalinger

De vigtigste teknologier:
- Java 17
- Javalin
- Hibernate ORM
- PostgreSQL
- Maven
- JWT authentication
- BCrypt password hashing
- HikariCP
- Thymeleaf
- Docker / Docker Compose
- JUnit
- REST Assured
- Testcontainers

---

## Arkitekturdiagram

Client
  ↓
Javalin Routes
  ↓
Controllers
  ↓
Services / DAO
  ↓
Hibernate ORM
  ↓
PostgreSQL
```

##Authentication flow:

Bruger logger ind
  ↓
SecurityController
  ↓
SecurityDAO
  ↓
BCrypt password-verifikation
  ↓
JWT token
  ↓
Beskyttede routes
```

---

## Vigtige designvalg

### Lagdelt struktur

Projektet er opdelt i routes, controllers, services og DAOs.

Det gør applikationen lettere at vedligeholde, fordi hvert lag har et klart ansvar.

### Hibernate ORM

Hibernate bruges som ORM-framework.

Applikationen arbejder med Java-entities som `User`, `Transaction`, `Budget`, `Category`, `Subcategory` og `RecurringPayment`, mens Hibernate håndterer kommunikationen med PostgreSQL.

Projektet bruger Hibernate `SessionFactory` og `Session` direkte til databaseadgang.

### Authentication

Authentication er implementeret med JWT tokens.

Når en bruger logger ind korrekt, oprettes der et JWT token. Tokenet indeholder brugerens ID og roller.

Beskyttede routes validerer tokenet, før brugeren får adgang.

Passwords gemmes aldrig som plain text. De hashes med BCrypt, før de gemmes i databasen.

### Brugerspecifikke data

Økonomiske data er knyttet til den bruger, der er logget ind.

Det betyder, at en bruger kun kan få adgang til sine egne:

- transaktioner
- budgetter
- kategorier
- underkategorier
- faste betalinger
- dashboard-data

### Fejlhåndtering

REST API'et bruger HTTP-statuskoder til at beskrive resultatet af en request.

Eksempler:

- `200 OK` – request gennemført korrekt
- `201 Created` – resource oprettet
- `204 No Content` – resource slettet
- `400 Bad Request` – ugyldigt input
- `401 Unauthorized` – manglende eller ugyldig authentication
- `404 Not Found` – resource blev ikke fundet
- `409 Conflict` – resource kan ikke oprettes eller slettes på grund af eksisterende data

---

# Datamodel

## ERD

Tilføj projektets ERD her.

```text
User
 ├── Category
 │    └── Subcategory
 ├── Transaction
 ├── Budget
 └── RecurringPayment
```

Vigtige relationer:

- En bruger kan have mange kategorier
- En kategori kan have mange underkategorier
- En transaktion tilhører en bruger og en underkategori
- Et budget tilhører en bruger og en underkategori
- En fast betaling tilhører en bruger og en underkategori

---

## Vigtige entities

### User

Repræsenterer en registreret bruger i systemet.

Vigtige felter:

- id
- name
- email
- password
- roles

### Category

Repræsenterer en overordnet økonomisk kategori.

Vigtige felter:

- id
- name
- type
- user

En kategori kan være enten `EXPENSE` eller `INCOME`.

### Subcategory

Repræsenterer en mere specifik kategori under en hovedkategori.

Vigtige felter:

- id
- name
- category

### Transaction

Repræsenterer en indtægt eller udgift.

Vigtige felter:

- id
- amount
- date
- type
- description
- user
- subcategory

### Budget

Repræsenterer et månedligt budget for en bestemt underkategori.

Vigtige felter:

- id
- amount
- month
- year
- user
- subcategory

### RecurringPayment

Repræsenterer en betaling, der gentages med faste intervaller.

Vigtige felter:

- id
- amount
- description
- frequency
- user
- subcategory

---

# API-dokumentation

De fleste økonomiske API-endpoints kræver authentication.

JWT-tokenet kan sendes på denne måde:

```http
Authorization: Bearer <token>
```

## Authentication

### Opret bruger

```http
POST /auth/register
```

Eksempel på request:

```json
{
  "name": "Anna",
  "email": "anna@test.dk",
  "password": "secret"
}
```

Succesfuldt svar:

```text
201 Created
```

### Login

```http
POST /auth/login
```

Eksempel på request:

```json
{
  "email": "anna@test.dk",
  "password": "secret"
}
```

Responsen indeholder et JWT token og grundlæggende information om brugeren.

### Aktuel bruger

```http
GET /auth/me
```

Returnerer information om den bruger, der er logget ind.

---

# Transaktioner

### Hent alle transaktioner

```http
GET /api/transactions
```

Returnerer transaktioner, der tilhører den bruger, som er logget ind.

### Hent transaktion via ID

```http
GET /api/transactions/{id}
```

### Opret transaktion

```http
POST /api/transactions
```

### Opdater transaktion

```http
PUT /api/transactions/{id}
```

### Slet transaktion

```http
DELETE /api/transactions/{id}
```

---

# Budgetter

### Hent alle budgetter

```http
GET /api/budgets
```

### Hent budget via ID

```http
GET /api/budgets/{id}
```

### Opret budget

```http
POST /api/budgets
```

### Opdater budget

```http
PUT /api/budgets/{id}
```

### Slet budget

```http
DELETE /api/budgets/{id}
```

---

# Kategorier

### Hent alle kategorier

```http
GET /api/categories
```

### Hent kategori via ID

```http
GET /api/categories/{id}
```

### Opret kategori

```http
POST /api/categories
```

### Opdater kategori

```http
PUT /api/categories/{id}
```

### Slet kategori

```http
DELETE /api/categories/{id}
```

---

# Underkategorier

### Hent alle underkategorier

```http
GET /api/subcategories
```

### Hent underkategori via ID

```http
GET /api/subcategories/{id}
```

### Opret underkategori

```http
POST /api/subcategories
```

### Opdater underkategori

```http
PUT /api/subcategories/{id}
```

### Slet underkategori

```http
DELETE /api/subcategories/{id}
```

---

# Faste betalinger

### Hent alle faste betalinger

```http
GET /api/recurring-payments
```

### Hent fast betaling via ID

```http
GET /api/recurring-payments/{id}
```

### Opret fast betaling

```http
POST /api/recurring-payments
```

### Opdater fast betaling

```http
PUT /api/recurring-payments/{id}
```

### Slet fast betaling

```http
DELETE /api/recurring-payments/{id}
```

---

# Dashboard

### Hent dashboard-data

```http
GET /api/dashboard
```

Returnerer et økonomisk overblik for den bruger, der er logget ind, blandt andet oplysninger om indtægter, udgifter, budgetter og faste betalinger.

---

# Valuta

### Hent valutakurs

```http
GET /api/currency
```

Projektet bruger et eksternt valuta-API til at hente oplysninger om valutakurser.

---

# User Stories

- Som bruger vil jeg kunne oprette en konto, så jeg kan bruge applikationen.
- Som bruger vil jeg kunne logge sikkert ind, så mine økonomiske data er beskyttet.
- Som bruger vil jeg kunne tilføje indtægter og udgifter, så jeg kan holde styr på min økonomi.
- Som bruger vil jeg kunne redigere og slette transaktioner.
- Som bruger vil jeg kunne organisere transaktioner med kategorier og underkategorier.
- Som bruger vil jeg kunne oprette månedlige budgetter, så jeg kan styre mit forbrug.
- Som bruger vil jeg kunne administrere faste betalinger.
- Som bruger vil jeg kunne se et dashboard med et samlet overblik over min økonomi.
- Som bruger vil jeg kun kunne se mine egne økonomiske oplysninger.
- Som bruger vil jeg kunne sammenligne mit forbrug med mit budget.

---

# Test

Projektet indeholder automatiserede tests af både API'et og databaselaget.

Testene omfatter:

- Security API-tests
- Transaction API-tests
- Budget API-tests
- Category API-tests
- Subcategory API-tests
- RecurringPayment API-tests
- Dashboard API-tests
- DAO-tests
- Hibernate configuration-tests

Testene bruger:

- JUnit
- REST Assured
- Testcontainers

Aktuelt testresultat:

```text
Tests run: 22
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

---

# Deployment

Applikationen er containeriseret med Docker.

Deployment-setup'et bruger:

- Docker
- Docker Compose
- PostgreSQL-container
- Caddy reverse proxy
- Watchtower til automatiske image-opdateringer
- DigitalOcean server
- Docker Hub

Applikationscontaineren kører på port `7070`.

---

# Udviklingsnoter

Nogle af de vigtigste udfordringer og læringspunkter under udviklingen har været:

- design af entity-relationer med Hibernate
- implementering af sikker authentication med JWT og BCrypt
- beskyttelse af routes og brugerspecifikke data
- opdeling af page routes og REST API routes
- implementering af CRUD-operationer for flere entities
- sikring af, at brugere ikke kan få adgang til andre brugeres data
- håndtering af konflikter ved sletning af data, der stadig er i brug
- test af REST-endpoints med REST Assured
- brug af Testcontainers til databasetest
- deployment med Docker og Docker Compose
- arbejde med Hibernate `SessionFactory` og forståelse af forskellen mellem Hibernate `Session` og JPA `EntityManager`

---

# Fremtidig udvikling

Mulige fremtidige forbedringer:

- React frontend
- forbedret UI og mobilvenligt design
- flere økonomiske statistikker og grafer
- mere fleksible intervaller for faste betalinger
- forbedret understøttelse af valuta
- flere brugerroller og administrationsfunktioner
