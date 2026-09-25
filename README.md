# FinPilot - Personal Finance Aggregator

FinPilot is an intelligent personal finance aggregator application that allows users to manage financial accounts, track budgets, analyze transactions, and leverage a private, local AI assistant for financial insights.

---

## Local AI with Docker + Ollama

FinPilot uses [Ollama](https://ollama.com/) running **`qwen2.5-coder:7b`** entirely locally in Docker. This ensures 100% financial privacy: no financial context or sensitive user data is ever transmitted to cloud LLM providers.

### Architecture Overview

```
┌─────────────────────┐
│   React Frontend    │ (Runs locally on localhost:5173)
└──────────┬──────────┘
           │ HTTP (/api/ai/*)
           ▼
┌─────────────────────┐
│ Spring Boot Backend │ (Port 8080: Gateway & Auth Source of Truth)
└──────────┬──────────┘
           │ HTTP (POST /api/chat, GET /api/tags)
           ▼
┌─────────────────────┐
│ finpilot-ollama     │ (Port 11434: Docker Container)
│  qwen2.5-coder:7b   │
└─────────────────────┘
```

> **Security Note:** The React frontend never connects to Ollama directly. All AI queries pass through Spring Boot, enforcing JWT authentication, user-scoped data access, and strict prompt sanitization.

---

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (v24+ recommended with Docker Compose v2)
- Java 21 & Maven (for running backend locally)
- Node.js 18+ (for frontend)

---

### Step-by-Step Setup

#### 1. Start the Dockerized Ollama Service

Start the `finpilot-ollama` container in the background:

```bash
docker compose up -d ollama
```

Verify the container is running:

```bash
docker ps
```

#### 2. Download the AI Model (`qwen2.5-coder:7b`)

Pull the `qwen2.5-coder:7b` model into the container:

```bash
docker exec -it finpilot-ollama ollama pull qwen2.5-coder:7b
```

> **Model Persistence:** Models are persisted inside the named Docker volume `ollama_data` (`/root/.ollama`). They will survive container restarts (`docker compose down && docker compose up -d`) without needing to be re-downloaded. **Do NOT run `docker compose down -v`** as that removes the named volume.

#### 3. Verify the Model Installation

Verify that the model is listed inside the container:

```bash
docker exec -it finpilot-ollama ollama list
```

You should see output similar to:

```
NAME                    ID              SIZE      MODIFIED
qwen2.5-coder:7b        2b049651e52d    4.7 GB    seconds ago
```

#### 4. Test the Ollama REST API Directly

You can query the Ollama tags endpoint from your terminal:

```bash
curl http://localhost:11434/api/tags
```

---

### Running the Application

FinPilot supports two development workflows without requiring source-code modifications:

#### MODE 1: Spring Boot Runs Locally on Host (Default for Development)

When running Spring Boot directly via IntelliJ, VS Code, or Maven:
- Backend communicates with Ollama via `http://localhost:11434`.
- Default values in `application.properties`:
  ```properties
  ollama.base-url=${OLLAMA_BASE_URL:http://localhost:11434}
  ollama.model=${OLLAMA_MODEL:qwen2.5-coder:7b}
  ollama.timeout-seconds=${OLLAMA_TIMEOUT_SECONDS:120}
  ```

Run the backend:
```bash
cd Backend
./mvnw spring-boot:run
```

#### MODE 2: Spring Boot Runs Inside Docker Compose

When running the backend container inside Docker Compose:
- Backend must reach Ollama using the Docker Compose service DNS name `http://ollama:11434` instead of `localhost`.
- Configured in `docker-compose.yml`:
  ```yaml
  environment:
    OLLAMA_BASE_URL: http://ollama:11434
    OLLAMA_MODEL: qwen2.5-coder:7b
  ```

Start the full stack:
```bash
docker compose up -d
```

---

### Docker Networking: `localhost` vs `ollama`

| Context | Target URL | Reason |
|---------|-----------|--------|
| **Host Machine / Browser / IDE** | `http://localhost:11434` | Port 11434 is mapped to the host (`11434:11434`). |
| **Inside Docker Containers** | `http://ollama:11434` | Docker's internal DNS resolves service names (`ollama`) on the bridge network. `localhost` inside a container refers to the container itself. |

---

### Backend AI Endpoints

The Spring Boot backend exposes guarded AI endpoints:

- **Health Check**: `GET /api/ai/health`
  - When available:
    ```json
    {
      "available": true,
      "model": "qwen2.5-coder:7b"
    }
    ```
  - When Ollama is stopped or unavailable:
    ```json
    {
      "available": false,
      "model": "qwen2.5-coder:7b",
      "message": "Ollama is unavailable"
    }
    ```
- **Chat**: `POST /api/ai/chat` (Requires Bearer token)
  ```json
  {
    "message": "Give me a short summary of my spending this month."
  }
  ```
- **Spending Insights**: `GET /api/ai/insights/spending`
- **Budget Insights**: `GET /api/ai/insights/budget`
- **Recurring Insights**: `GET /api/ai/insights/recurring`
- **Monthly Summary**: `GET /api/ai/summary/monthly`
- **Category Suggestion**: `POST /api/ai/suggest-category`

---

### Troubleshooting

1. **Check container status:**
   ```bash
   docker ps
   ```

2. **Inspect Ollama container logs:**
   ```bash
   docker logs finpilot-ollama
   ```

3. **Verify model list inside container:**
   ```bash
   docker exec -it finpilot-ollama ollama list
   ```

4. **Restart Ollama container:**
   ```bash
   docker compose restart ollama
   ```

---

### Optional GPU Acceleration (NVIDIA)

If your system is equipped with an NVIDIA GPU and you have installed the [NVIDIA Container Toolkit](https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/install-guide.html), you can enable GPU acceleration by adding the `deploy` reservation to the `ollama` service in `docker-compose.yml`:

```yaml
  ollama:
    image: ollama/ollama:latest
    container_name: finpilot-ollama
    ports:
      - "11434:11434"
    volumes:
      - ollama_data:/root/.ollama
    deploy:
      resources:
        reservations:
          devices:
            - driver: nvidia
              count: all
              capabilities: [gpu]
```

*(If NVIDIA Docker support is not present, Ollama runs seamlessly on standard CPU mode.)*
