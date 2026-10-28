# SecureBank — Full Java Full-Stack Project

**Spring Boot 3 REST API + React 18 + Vite + Tailwind CSS + MySQL**

---

## 🗂️ Project Structure

```
securebank-light/
├── backend/                    ← Spring Boot (Java)
│   ├── pom.xml
│   └── src/main/java/com/securebank/
│       ├── SecureBankApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java     ← JWT + CORS + Role guards
│       │   └── DataSeeder.java         ← Auto-seeds demo accounts
│       ├── controller/
│       │   ├── AuthController.java
│       │   ├── TransactionController.java
│       │   ├── UserController.java
│       │   ├── AuditController.java
│       │   ├── SimulatorController.java
│       │   ├── EvaluationController.java
│       │   └── GlobalExceptionHandler.java
│       ├── dto/                        ← Request/Response shapes
│       ├── entity/                     ← JPA entities (MySQL tables)
│       ├── repository/                 ← Spring Data JPA
│       ├── security/                   ← JWT filter, provider, entry point
│       └── service/                    ← Business logic + fraud engine
│
└── securebank/                 ← React + Vite frontend (unchanged)
    └── src/
        ├── services/api.js     ← Already points to localhost:8080
        └── ...
```

---

## 🚀 Quick Start

### Prerequisites
- **Java 17+** (check: `java -version`)
- **Maven 3.8+** (check: `mvn -version`)
- **MySQL 8+** running locally
- **Node.js 18+** for the frontend

### 1. Set up MySQL

Make sure MySQL is running. Update credentials in `backend/src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=root   ← change to your MySQL password
```

The database `securebank` will be **created automatically** on first startup.

### 2. Start the Backend

```powershell
cd backend
mvn spring-boot:run
```

The API will start at **http://localhost:8080**

On first run, DataSeeder auto-creates:
- **jai / user123** — Customer account (₹1,25,350 balance)
- **admin / admin123** — Security Admin account

### 3. Start the Frontend

```powershell
cd securebank
npm install
npm run dev
```

Open **http://localhost:5173** in your browser.

---

## 🔌 REST API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/auth/login` | Public | Login → returns `{ token, user }` |
| `POST` | `/api/auth/logout` | Auth | Logout (audit log only) |
| `GET` | `/api/auth/me` | Auth | Current user from token |
| `GET` | `/api/users/me` | Auth | User profile |
| `GET` | `/api/users` | Admin | All users |
| `POST` | `/api/users/{id}/freeze` | Admin | Toggle account freeze |
| `GET` | `/api/transactions` | Auth | User's own / all (admin) |
| `GET` | `/api/transactions/flagged` | Auth | Flagged MEDIUM/HIGH transactions |
| `POST` | `/api/transactions` | Auth | Create transaction (fraud engine runs) |
| `PATCH` | `/api/transactions/{id}/status` | Auth | Update status (mark safe etc.) |
| `GET` | `/api/audit` | Auth | All audit logs |
| `POST` | `/api/simulator/generate` | Admin | Generate 1 synthetic transaction |
| `DELETE` | `/api/simulator/purge` | Admin | Delete all simulated transactions |
| `GET` | `/api/evaluation/metrics` | Admin | Confusion matrix + F1/Precision/Recall |
| `GET` | `/api/evaluation/settings` | Admin | Fraud engine weight settings |
| `PUT` | `/api/evaluation/settings` | Admin | Update fraud engine weights |

---

## 🧠 Fraud Engine (Java Port)

The JavaScript heuristic engine has been fully ported to Java as `FraudEngineService.java`:

| Rule | Weight (default) | Trigger |
|------|-----------------|---------|
| Amount > 6x average | 30 | Extreme amount anomaly |
| Amount > 3x average | 18 | Moderate amount deviation |
| Amount ≥ ₹1,00,000 | 20 | Absolute high-value outflow |
| 01:00–05:00 AM IST | 15 | Off-hours temporal anomaly |
| Unknown city | 20 | Geographic mismatch |
| Unknown device | 20 | Unrecognized client signature |
| ≥ 4 txns in 10 min | 35 | Velocity burst |
| 2–3 txns in 10 min | 17 | Elevated velocity |

**Score ≥ 71** → `HIGH` risk → Transaction BLOCKED + account auto-frozen  
**Score 41–70** → `MEDIUM` risk → `PENDING_REVIEW`  
**Score < 41** → `LOW` risk → `COMPLETED`

---

## 🔐 Security

- **BCrypt** password hashing (no plain text passwords)
- **JWT** (HMAC-SHA256, 8-hour expiry) — configurable in `application.properties`
- **Stateless** sessions — no server-side session state
- **Role-based** endpoint guards (`ROLE_USER`, `ROLE_ADMIN`)

---

## ⚙️ Configuration

Edit `backend/src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/securebank?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

# JWT (use a 64+ char random string in production!)
app.jwt.secret=SecureBankSuperSecretJwtSigningKey2024XYZ...
app.jwt.expiration-ms=28800000  # 8 hours

# CORS (add production domain here)
app.cors.allowed-origins=http://localhost:5173,http://localhost:3000
```
