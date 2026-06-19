# Glossary AI Assistant

A Spring Boot service that auto-generates draft translations for government glossary terms across 14 languages using an LLM API, with duplicate-term detection — built to explore AI-assisted translation workflows beyond what was achievable in a low-code Power Apps environment.

## Why this exists

At work, I built the production **EDD Glossary Manager** — a 14-language multilingual glossary system — as a Power Apps canvas app with a SharePoint backend. Power Apps was the right tool for that project (fast to build, easy for non-developers to maintain, tightly integrated with SharePoint security and the rest of the agency's Microsoft 365 environment).

But Power Apps' AI Builder and Power Automate HTTP connectors only support simple request/response calls — there's no native way to:

- Send one structured prompt and get back a typed, multi-language JSON response for 14 languages at once
- Validate and parse that response against a defined schema
- Handle retries, fallbacks, or multi-step orchestration around the AI call
- Run custom similarity/duplicate-detection logic over existing glossary entries

So I built this as a personal project: a Spring Boot service that does what the low-code platform couldn't, while keeping the same domain (a government glossary system) so the comparison is apples-to-apples.

**This is a prototype, not a production system.** It exists to demonstrate the technical approach, not to replace the EDD Glossary Manager.

## What it does

1. **Batch AI translation** — given one English term, generates draft translations across all 14 supported languages in a single structured LLM call (via Groq's free-tier OpenAI-compatible API), returned as validated JSON.
2. **Duplicate detection** — before generating translations, checks the existing glossary for near-duplicate terms using normalized Levenshtein similarity, flagging likely duplicates before they're saved.
3. **Review workflow** — new terms are saved with status `AI_SUGGESTED`; a human reviewer can promote them to `HUMAN_APPROVED` via a dedicated endpoint, mirroring the human-in-the-loop review process used in the production glossary system.

## Stack

- **Backend:** Java 17, Spring Boot 3.3, Spring Data JPA
- **Database:** PostgreSQL (Dockerized)
- **AI:** Groq API (Llama 3.3 70B, OpenAI-compatible chat completions endpoint)
- **Frontend:** Plain HTML/JS demo UI (no build step required)

## Architecture

```
Browser (frontend/index.html)
        │
        ▼
GlossaryController  ─── REST endpoints (/api/glossary/**)
        │
        ▼
GlossaryService  ─── orchestrates similarity check + AI generation + persistence
        │
        ├──► SimilarityService ─── Levenshtein-based duplicate detection
        │
        └──► LlmTranslationClient ─── builds structured prompt, calls Groq API,
                                       parses JSON response, falls back to mock
                                       mode if no API key is configured
        │
        ▼
PostgreSQL (glossary_terms + glossary_translations tables)
```

## Running it locally

### 1. Start PostgreSQL

```bash
docker-compose up -d
```

### 2. (Optional) Add a Groq API key

Get a free key at [console.groq.com](https://console.groq.com), then:

```bash
export GROQ_API_KEY=your_key_here
```

Without a key, the service runs in **mock mode** — it returns clearly-labeled placeholder translations (`[MOCK-ES] ...`) so the full flow (duplicate detection → save → review) still works end-to-end for demo purposes.

### 3. Run the Spring Boot app

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080/api/glossary`.

### 4. Open the demo UI

Open `frontend/index.html` directly in a browser (or serve it with any static file server). It talks to the API at `localhost:8080`.

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET`  | `/api/glossary/terms` | List all glossary terms, newest first |
| `POST` | `/api/glossary/terms/translate` | Generate AI translations for a new term (also runs duplicate check) |
| `GET`  | `/api/glossary/terms/{term}/similar` | Check for similar existing terms |
| `PUT`  | `/api/glossary/terms/{id}/approve` | Mark a term as human-approved |
| `DELETE` | `/api/glossary/terms/{id}` | Remove a term |

### Example request

```bash
curl -X POST http://localhost:8080/api/glossary/terms/translate \
  -H "Content-Type: application/json" \
  -d '{
    "englishTerm": "Disability Insurance Elective Coverage",
    "definition": "Optional disability coverage available to self-employed individuals"
  }'
```

## Known limitations / next steps

This is intentionally scoped as a focused prototype:

- **Similarity detection** uses Levenshtein edit distance, not true semantic vector embeddings. A production version would use sentence embeddings (e.g. via a vector database like pgvector) to catch semantically similar terms that are worded differently — this is the natural next step.
- **No authentication/authorization** — the production EDD Glossary Manager has role-based access via SharePoint; this prototype is single-user/local-only by design.
- **No translation quality scoring** — a production version would likely have a confidence score or back-translation check per language.

## Related work

The production system this project was inspired by is the **EDD Glossary Manager**, a Power Apps canvas application with a SharePoint backend supporting the same 14 languages, built for California's Employment Development Department Vital Documents Translation Project. That system handles the full production workflow (role-based access, versioning, audit logging) that Power Apps is well-suited for — this project explores the AI/backend orchestration piece that it isn't.
