# Plan-A - A video about this project
https://vimeo.com/manage/videos/1098556514
# Plan-A Deployment and Development Guide

This repository provides a flexible setup for running the **Plan-A** application in development, end-to-end (E2E) testing, and production environments. The system uses Docker Compose to orchestrate services like the backend, frontend, Keycloak (authentication), and NGINX (reverse proxy).

---

## 📁 Docker Compose Configurations

There are **three** Compose files:

- `docker-compose.dev.yml` – for local development
- `docker-compose.e2e.yml` – for E2E testing
- `docker-compose.prod.yml` – for production deployment

---

## 🚀 Development Setup

### 🔧 Run the App in Dev Mode (Web)

1. **Start services**:
   ```bash
   docker compose -f docker-compose.dev.yml up -d
   ```

2. **Start the frontend** (hot-reload enabled):
   ```bash
   cd dev/Frontend
   npm install
   npm run start
   ```

This starts the React frontend and backend in containers. The backend does **not** support hot reload by default.

---

### 🐞 Backend Debug Mode (Run Backend Manually)

1. **Start only Keycloak**:
   ```bash
   docker compose -f docker-compose.dev.yml up -d keycloak
   ```

2. **Run backend in your IDE**:
   ```bash
   cd dev/Backend
   mvn install
   ```
   Then open `src/main/java/groupId/Main.java` in your IDE and run it in debug mode (e.g., F5 in IntelliJ or Eclipse).

3. If you're developing for the **desktop app**, set:
   ```json
   {
     "IS_DESKTOP": true
   }
   ```
   in `dev/Frontend/public/config.json`.

---

### 🐳 Develop Inside a Container

1. **Build the image**:
   ```bash
   docker build -t plan-a-dev .
   ```

2. **Run the container**:
   ```bash
   docker run -it -p 4000:4000 --mount type=bind,src=${PWD},dst=/Plan-A plan-a-dev /bin/bash
   ```

3. **Inside the container**:
   ```bash
   cd dev/Backend
   mvn spring-boot:run
   ```

Alternatively, connect your IDE to the container and run `Main.java` directly.

---

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

- This includes:
  - NGINX with self-signed TLS
  - Keycloak with two test users:
    - `max` / `1234`
    - `alice` / `1234`
- You can also manually test at [https://localhost](https://localhost)

---

## ☁️ Production Deployment

### 🧭 Steps

1. **Clone the repository** to your production server.
2. **Obtain a valid TLS certificate** and key:
   - Save them as:
     - `certs/prod/server.crt`
     - `certs/prod/server.key`
3. **Update NGINX config**:
   - Replace all instances of `localhost` in `nginx.conf` with your **public domain**.
4. **Configure environment variables**:
   - Edit `env.prod.template`:
     ```dotenv
     PUBLIC_URL=https://<your-domain>
     KEYCLOAK_ADMIN_PASSWORD=<secure-password>
     ```
5. **Run the application**:
   ```bash
   docker compose --env-file env.prod.template -f docker-compose.prod.yml up -d
   ```

6. **Access Keycloak admin** (~30s after startup):
   ```
   https://<your-domain>/auth
   ```

7. **Login** with:
   - Username: `admin`
   - Password: (the value set in `env.prod.template`)

8. **Configure Keycloak Client**:
   - In the admin console:
     - Select the `plan-a` realm
     - Go to **Clients** → click on `spring-gateway`
     - Under **Settings**:
       - Set **Valid Redirect URIs**:
         ```
         https://<your-domain>/*
         ```
       - Set **Web Origins**:
         ```
         +
         ```
     - Save changes.

You're done! Access your app at:
```
https://<your-domain>/
```

---

### 💻 Package to Desktop App

To build a standalone desktop application:

- **For Windows:**
  ```bash
  scripts\buildDesktopAppWindows.bat
  ```

- **For Linux (Debian-based distros):**
  ```bash
  scripts/buildDesktopApp.sh
  ```

This creates an installable `.exe` or `.deb` file respectively.

---

## 🛠 Quick Commands

| Action                      | Command                                                                 |
|-----------------------------|-------------------------------------------------------------------------|
| Start dev environment       | `docker compose -f docker-compose.dev.yml up -d`                        |
| Start frontend with hot-reload | `cd dev/Frontend && npm install && npm run start`                   |
| Run backend tests           | `cd dev/Backend && mvn clean compile && mvn test`                      |
| Build Docker image          | `docker build -t <image-name> .`                                       |
| Run E2E environment         | `docker compose -f docker-compose.e2e.yml up -d`                        |
| Run E2E tests               | `cd dev/Frontend && npx playwright test`                                |
| Run production              | `docker compose --env-file env.prod.template -f docker-compose.prod.yml up -d` |

---


## 💬 Questions?

Feel free to open an issue or discussion if you have feedback, feature requests, or questions.
