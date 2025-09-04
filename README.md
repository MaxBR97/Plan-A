
# 🚀✨ Plan-A
**Modeling & solving real-world constraint and optimization problems**  

<!-- PROJECT POSTER -->
![plan-a poster v5](https://github.com/user-attachments/assets/1d168c5d-5b3f-4e0f-a442-7b75056541b0)


---

## 📚 Table of Contents
- [Introduction](#introduction)  
- [Highlights & Features](#highlights--features)  
- [Architecture (high level)](#architecture-high-level)  
- [Getting started — Quickstart (demo)](#getting-started---quickstart-demo)  
- [Development & Deployment (full guide)](#development--deployment-full-guide)  
  - [Compose configurations](#compose-configurations)  
  - [Development (web)](#development-web)  
  - [Backend debug (IDE)](#backend-debug-ide)  
  - [Develop inside a container](#develop-inside-a-container)  
  - [📌 Development Notes](#-development-notes)  
- [🧪 E2E Testing Setup](#-e2e-testing-setup)  
- [🚢 Production deployment](#production-deployment)  
- [⚡ Quick commands](#quick-commands)  
- [🧩 Use cases (detailed examples)](#use-cases-detailed-examples)  
- [🙏 Credits & Acknowledgements](#credits--acknowledgements)  
- [📖 The story behind Plan-A](#the-story-behind-plan-a)  
- [✉️ Contact](#contact)

---

<a id="introduction"></a>
## 🎬 Introduction
**Intro video:**  
https://vimeo.com/manage/videos/1098556514

Plan-A was built to let users **quickly model constraint and optimization problems** and then solve concrete instances by supplying inputs. Modeling is *declarative*: you describe the problem data, the rules (constraints), and how to score solutions (objective), and the solver (e.g., SCIP) handles *how* to find the solution.

Typical problems you can model with Plan-A:
- 🕒 **Shift scheduling** (workforce rostering)  
- 🎓 **Course scheduling** (students / universities)  
- 🧩 **Chip/module placement** (area, wirelength, thermal constraints)  
- 🗺️ **Route planning** (e.g., traveling salesman)  
- 🔢 **Parity learning** or similar combinatorial tasks  
- ♟️ **Board/game search problems** (e.g., best moves under heuristics)  
- 🧠 **Feature selection for ML classifiers** — select a subset of features that maximizes classifier performance under a budget constraint (a common combinatorial optimization formulation)

This repository contains the full stack: frontend (React; optional Electron), backend (Spring Boot microservices), solver integration, and deployment tooling.

---

<a id="highlights--features"></a>
## ✨ Highlights & Features
- 🖼️ **Images**: a reusable artifact representing a named optimization model (model + metadata + interactive input schema).  
- 🧾 **Declarative modeling** with a ZIMPL-like syntax (ANTLR grammar: `Formulation.g4`).  
- 🧩 **Three input layers for images**:
  - 📂 **Domain** — core data structures (sets, parameters) users supply.  
  - ⚖️ **Constraints** — toggles and editable parameters to adapt rules.  
  - 🎯 **Optimization goals** — how to grade solutions and trade-off objectives.  
- ⚙️ **Solver presets & dynamic solver settings** (accuracy, fast approximations, feasibility search, time limits).  
- ⏱️ **Timeouts & intermediate best-solution handling** for long-running solves.  
- 🏷️ **Labelled, human-friendly inputs/outputs** controlled by the image author.  
- 📊 **Dynamic recursive tables** for multi-dimensional variables (reorder dimensions on-the-fly to analyze results).  
- 📌 **Pin & re-optimize**: lock partial solution tuples (e.g., fixed assignments) and resolve the remaining variables.  
- 🔎 **Publish / discover images**: mark images public or private; search for reusable images.

---

<a id="architecture-high-level"></a>
## 🏛️ Architecture (high level)
<img width="940" height="542" alt="image" src="https://github.com/user-attachments/assets/61394b1e-82c0-4648-9d35-227429f694cf" />

Plan-A supports two deployment modes:

**1. Web (microservices)**  
- 🌐 NGINX (API Gateway / TLS termination)  
- 🔐 Keycloak (authentication & authorization)  
- 🗂️ Image Service — parse/validate images, manage metadata, prepare models.  
- 🧮 Solver Service — manage SCIP instances, compile & run solves, enforce time limits.  
- 📨 Kafka — request/response messaging between Image Service and Solver Service (decoupled, asynchronous).  

**2. [Deprecated] Desktop (monolith)**  
- 🧩 All services packaged together; Image Service and Solver run in-process; no Kafka.  
- 💻 Simplified local deployment; ideal for offline or single-user usage.

**Component responsibilities (summary)**:
- 🖥️ **Frontend**: React + optional Electron; image authoring, input forms, solver UI, recursive tables.  
- 🗃️ **Image Service**: parse/validate models (ANTLR + `Formulation.g4`), image storage & metadata.  
- 🛠️ **Solver Service**: SCIP orchestration, settings, logs, solution streaming.  
- 🔁 **Messaging / Orchestration**: Kafka (web) or direct calls (desktop).  
- 🔐 **Auth & Gateway**: Keycloak & NGINX.

---

<a id="getting-started---quickstart-demo"></a>
## 🧭 Getting started — Quickstart (demo)
A minimal path to run a local demo, given you already have Docker Desktop:

```bash
# from repo root
docker compose -f docker-compose.e2e.yml up -d
```
then visit: 👉 [https://localhost](https://localhost)

---

<a id="development--deployment-full-guide"></a>
## 🛠️ Development & Deployment (full guide)
_Complete developer and ops instructions (dev / E2E / prod)._

<a id="compose-configurations"></a>
### 🧩 Compose configurations
Three main compose files:
- `docker-compose.dev.yml` — development  
- `docker-compose.e2e.yml` — E2E testing  
- `docker-compose.prod.yml` — production

<a id="development-web"></a>
### 🧑‍💻 Development (web)
1. Start dev stack:
```bash
docker compose -f docker-compose.dev.yml up -d
```
2. Start frontend:
```bash
cd dev/Frontend
npm install
npm run start
```
3. Backend:
- Backend runs in a container by default. To run locally with hot debugging, see Backend Debug.

<a id="backend-debug-ide"></a>
### 🐞 Backend debug (IDE)
1. Start Keycloak only:
```bash
docker compose -f docker-compose.dev.yml up -d keycloak
```
2. Build & run backend:
```bash
cd dev/Backend
mvn install
# run Main.java in your IDE (IntelliJ/Eclipse) in debug mode
```
3. For desktop development, set `IS_DESKTOP: true` in `dev/Frontend/public/config.json`.

<a id="develop-inside-a-container"></a>
### 🐳 Develop inside a container
1. Build dev image:
```bash
docker build -t plan-a-dev .
```
2. Run container (bind mount repo):
```bash
docker run -it -p 4000:4000 --mount type=bind,src=${PWD},dst=/Plan-A plan-a-dev /bin/bash
```
3. Inside container:
```bash
cd dev/Backend
mvn spring-boot:run
```

<a id="-development-notes"></a>
### 📌 Development Notes
- ✅ **Keycloak** is **required even in development**, as authentication is integral to the system.  
- ✅ To run backend tests:
  ```bash
  cd dev/Backend
  mvn clean compile
  mvn test
  ```
- ✅ Backend supports many **modular profiles**:
  - `application-H2.yml` — persistent H2 database  
  - `application-H2mem.yml` — in-memory DB (volatile)  
  - `application-S3.yml` — S3 document storage  
  - Default — local file system as document store  
  - Profiles for:
    - Running the solver as a separate service via Kafka  
    - Running the solver embedded in the monolith (useful for desktop)
- ✅ After development, build a production-ready image:
  ```bash
  docker build -t <your-image-name> .
  ```
- ✅ Update the image name in all Compose files by replacing:
  ```yaml
  image: maxbr1/plan-a-dev
  ```
  with your chosen image name.
- ✅ Optionally include an **initial DB**:
  - Copy contents of `dev/Backend/initialData` to `dev/Backend/data` **before building** the image.  
  - These files will be baked into the image.

---

<a id="-e2e-testing-setup"></a>
## 🧪 E2E Testing Setup
1. **Spin up test environment**:
   ```bash
   docker compose -f docker-compose.e2e.yml up -d
   ```
2. **Run tests**:
   ```bash
   cd dev/Frontend
   npx playwright test
   ```
Includes:
- 🔐 NGINX with self-signed TLS  
- 👥 Keycloak with two test users:
  - `max` / `1234`  
  - `alice` / `1234`  
You can also manually test at: 👉 [https://localhost](https://localhost)

---

<a id="production-deployment"></a>
## 🚢 Production deployment
1. Clone repository on server.  
2. Provide TLS certificate & key at:
- `certs/prod/server.crt`  
- `certs/prod/server.key`  
3. Update `nginx.conf` (replace `localhost` with your public domain).  
4. Edit `env.prod.template`:
```dotenv
PUBLIC_URL=https://<your-domain>
KEYCLOAK_ADMIN_PASSWORD=<secure-password>
```
5. Start production:
```bash
docker compose --env-file env.prod.template -f docker-compose.prod.yml up -d
```
6. Keycloak admin:
```
https://<your-domain>/auth
Username: admin
Password: <value from env.prod.template>
```
7. Configure Keycloak client (`plan-a` realm → Clients → `spring-gateway`):
- Valid Redirect URIs: `https://<your-domain>/*`  
- Web Origins: `+`

---

<a id="quick-commands"></a>
## ⚡ Quick commands
| Action                      | Command                                                                 |
|-----------------------------|-------------------------------------------------------------------------|
| Start dev environment       | `docker compose -f docker-compose.dev.yml up -d`                        |
| Start frontend (dev)        | `cd dev/Frontend && npm install && npm run start`                       |
| Run backend tests           | `cd dev/Backend && mvn clean compile && mvn test`                      |
| Build Docker image          | `docker build -t <image-name> .`                                       |
| Run E2E environment         | `docker compose -f docker-compose.e2e.yml up -d`                        |
| Run E2E tests               | `cd dev/Frontend && npx playwright test`                                |
| Run production              | `docker compose --env-file env.prod.template -f docker-compose.prod.yml up -d` |

---

<a id="use-cases-detailed-examples"></a>
## 🧩 Use cases (detailed examples)
- 🕒 **Shift scheduling** — ensure coverage, minimize payroll, balance workload and night/weekend shifts.  
- 🎓 **Course scheduling** — resolve conflicts, ensure required courses are included, maximize free days and early finish times.  
- 🧩 **Chip design (placement & thermal)** — non-overlap constraints, thermal limits, minimize area and wirelength.  
- 🗺️ **Travelling Salesman / route planning** — minimize total travel distance for visiting a set of nodes.  
- 🧩 **Sudoku** — generate puzzles and validate solutions.  
- ♟️ **Chess** — encode board state & heuristics to search for promising moves (with pruning).  
- 🧠 **Feature selection (ML)** — choose a subset of features that maximizes classifier performance under a budget.

---

<a id="credits--acknowledgements"></a>
## 🙏 Credits & Acknowledgements
Thanks & acknowledgements to the projects that made Plan-A possible:
- **ZIMPL** (inspiration for modeling language)  
- **SCIP** (solver)  
- **ANTLR** (parser generator) & the custom `Formulation.g4` grammar  
- **Apache Kafka** (messaging)  
- **Spring Boot** (backend)  
- **React** & **Electron** (frontend)  
- **Keycloak** (authentication)  
- **NGINX** (gateway & TLS)  
- **Docker / Docker Compose** (deployment)  
- **Playwright** (E2E testing)  
- Tooling: **Maven**, **npm / Node.js**

---

<a id="the-story-behind-plan-a"></a>
## 📖 The story behind Plan-A
The idea came from my experience in the IDF after the 7/10 events. My unit needed to assign manpower to missions under dynamic, changing constraints. Tasks appeared or disappeared, personnel were fatigued, and small changes could force the whole plan to be redone. I initially implemented a scheduler in C++ but found it too rigid. I needed a declarative, flexible approach that could generalize across domains (army scheduling, workplace rotas, course scheduling). That led me to Integer Linear Programming and tools like SCIP and ZIMPL, and ultimately to building Plan-A as a reusable, model-driven optimization platform.

---

<a id="contact"></a>
## ✉️ Contact
Questions and thoughts:
- Open an issue in this repository.  
- Or contact: linmaxi@gmail.com

