# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**AI_MindVault** is a medical/healthcare intelligent consultation assistant powered by AI agent **A.R.I.A** (Advanced Responsive Intelligent Advisor). It provides AI chat, medication consultation, disease knowledge, symptom self-check, health records, appointment booking, community Q&A, and an admin panel.

## Build & Run Commands

### Backend (Spring Boot, Java 21, Maven)
```bash
cd major_ai
mvn spring-boot:run                  # run dev server on :8080
mvn clean package                    # build JAR
mvn test                             # run tests (single test: mvn test -Dtest=MajorAiApplicationTests)
```
First-time setup: copy `src/main/resources/application.yml.example` → `application.yml` and fill in MySQL, Redis, and DashScope API key credentials. Initialize DB with `schema.sql`.

### Frontend (Vue 3, Vite)
```bash
cd ai-frontend
npm install
npm run dev                          # dev server on :5173
npm run build                        # production build → dist/
npm run preview                      # preview production build
```

## Architecture

```
ai-frontend/ (Vue 3 + Vite)  →  WebSocket /ws/chat  →  major_ai/ (Spring Boot)
                                                             ↓
                                                    LangChain4j → Qwen (DashScope)
                                                             ↓
                                                    Redis (RAG vector store + chat memory)
                                                             ↓
                                                    MySQL (persistent data)
```

### Backend (`major_ai/src/main/java/org/example/major_ai/`)

| Layer | Key Files | Notes |
|-------|-----------|-------|
| AI Service | `aiservice/ConsultantService.java` | LangChain4j `@AiService` interface |
| RAG Config | `config/CommonConfig.java` | Document loading, embedding ingestion, content retriever, chat memory |
| WebSocket | `config/WebSocketConfig.java`, `handler/ChatWebSocketHandler.java` | Registers `/ws/chat`; handler parses JSON, builds scenario prompts, streams responses |
| REST API | `controller/ChatController.java` | Streaming endpoints (Flux\<String\>) for chat, medicine, disease, symptom |
| Auth | `controller/AuthController.java`, `security/` | JWT-based auth with Spring Security; 3 roles: `ROOT_ADMIN`, `DOCTOR`, `USER` |
| Admin | `controller/Admin*.java` (5 files) | Dashboard, user, department, consultation, appointment management |
| Data | `entity/` (12), `mapper/` (12), `service/` (11) | MyBatis-Plus ORM layer |
| DTOs | `dto/` | Login, Register, MedicineQuery, SymptomCheck request/response objects |

**Config files:**
- `resources/system.txt` — AI system prompt defining A.R.I.A's persona and capabilities
- `resources/content/` — 5 RAG knowledge base documents (disease, drug interaction, medicine guide, special population, symptom guide)
- `resources/schema.sql` — DDL for 11 tables + seed data (10 departments, admin user)

**AI provider:** Alibaba DashScope (OpenAI-compatible API at `dashscope.aliyuncs.com/compatible-mode/v1`). Models: `qwen-plus` (chat), `text-embedding-v4` (embeddings).

### Frontend (`ai-frontend/src/`)

| Layer | Key Files | Notes |
|-------|-----------|-------|
| Router | `router/index.js` | 17 routes with auth guards; admin routes require `ROOT_ADMIN` role |
| State | `stores/auth.js` | Pinia store for JWT token management, login/register/logout |
| HTTP | `utils/api.js` | Axios instance with JWT interceptor; base URL `http://localhost:8080` |
| WebSocket | `utils/websocket.js` | Singleton client with auto-reconnect |
| Streaming | `composables/useStreamingText.js` | Word-by-word text animation composable |
| Views | `views/` (15+ pages), `views/admin/` (5 pages) | Page-level components |
| Components | `components/` | ChatInterface, HealthProfileModal, robot avatar variants (3D/Animated/SVG), MedicalPoster |

## Key Conventions

- Backend uses **MyBatis-Plus** (not JPA/Hibernate) — mapper interfaces extend `BaseMapper<T>`, services extend `ServiceImpl`
- All AI responses are **streamed** — both REST (`Flux<String>`) and WebSocket paths exist; frontend uses WebSocket primarily
- Chat memory is **Redis-backed** with 7-day TTL and 20-message window per session
- RAG knowledge documents in `resources/content/` are chunked (500 tokens, 100 overlap) and embedded into Redis on startup
- Frontend uses **Composition API** exclusively (`<script setup>`)
- Config uses **environment variable overrides** with defaults: `${VAR:default}` pattern in `application.yml`
