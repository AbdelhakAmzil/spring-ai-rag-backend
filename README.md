# spring-ai-rag-demo

A minimal, working Spring Boot project implementing Retrieval Augmented Generation (RAG)
with **Spring AI**, following the official reference:
https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html

Stack:
- **Spring Boot 4.1.0** / Java 21 (Spring Framework 7)
- **Spring AI 2.0.0** (GA, stable as of June 2026)
- **Ollama** — local LLM (`llama3.1`) for chat + local embeddings (`nomic-embed-text`)
- **PGVector** (Postgres + pgvector extension) as the vector store
- `RetrievalAugmentationAdvisor` (modular RAG) as the main flow, with a `QuestionAnswerAdvisor`
  bean kept alongside for comparison (the "naive RAG" flow from the docs)

## Project layout

```
src/main/java/com/abdelhak/ragdemo/
├── RagDemoApplication.java
├── config/RagConfig.java            # ChatClient beans + RetrievalAugmentationAdvisor wiring
├── controller/ChatController.java   # POST /api/chat        -> ask a question (RAG)
├── controller/DocumentController.java # POST /api/documents/upload | /api/documents/text -> ETL ingestion
├── service/DocumentIngestionService.java # Extract (Tika) -> Transform (chunk) -> Load (vector store)
└── dto/                              # request/response records
src/main/resources/application.yml    # Ollama + PGVector config
docker-compose.yml                    # Postgres+pgvector and Ollama containers
```

## 1. Start infrastructure

```bash
docker compose up -d
docker exec -it rag-ollama ollama pull llama3.1
docker exec -it rag-ollama ollama pull nomic-embed-text
```

> `nomic-embed-text` outputs 768-dim vectors, which is why `application.yml` sets
> `spring.ai.vectorstore.pgvector.dimensions: 768`. If you swap embedding models,
> update this value to match.

## 2. Run the app

```bash
./mvnw spring-boot:run
```

The app auto-creates the `vector_store` table in Postgres on first boot
(`initialize-schema: true`).

## 3. Ingest some documents

Upload a file (PDF, DOCX, TXT, HTML — parsed via Apache Tika):

```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@/path/to/your/document.pdf"
```

Or push raw text directly:

```bash
curl -X POST http://localhost:8080/api/documents/text \
  -H "Content-Type: application/json" \
  -d '{"source":"note-1","text":"Spring AI supports RAG through a modular Advisor API..."}'
```

## 4. Ask questions (RAG)

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question":"What advisor does Spring AI provide for naive RAG?"}'
```

The `RetrievalAugmentationAdvisor` (see `RagConfig`) will:
1. Run `VectorStoreDocumentRetriever` (similarity threshold 0.50, top 6) against PGVector.
2. Augment the prompt with the retrieved chunks via `ContextualQueryAugmenter`.
3. Send the augmented prompt to the Ollama chat model.

## Extending it

The doc's modular RAG architecture maps directly onto `RagConfig`:

- **Query transformation** — add `.queryTransformers(RewriteQueryTransformer.builder()...)` or
  `CompressionQueryTransformer` (useful once you add multi-turn chat memory) to the
  `RetrievalAugmentationAdvisor` builder.
- **Query expansion** — add a `MultiQueryExpander` to search with several reformulated queries.
- **Filtering by source/tenant** — `VectorStoreDocumentRetriever.filterExpression(...)`; each
  ingested chunk already carries a `source` metadata field for this.
- **Document post-processing (re-ranking, compression)** — implement `DocumentPostProcessor`
  and add it to the advisor builder.

## Notes

- Swap Ollama for OpenAI/Anthropic/Mistral/etc. by replacing the
  `spring-ai-starter-model-ollama` dependency and `spring.ai.*` config block — the
  RAG wiring in `RagConfig` doesn't change.
- Swap PGVector for Chroma/Qdrant/Milvus/etc. the same way (`vectorStore` bean is
  auto-configured by whichever `spring-ai-starter-vector-store-*` you include).

## Upgrade notes (1.x -> 2.0.0)

If you're coming from an older Spring AI 1.x project (e.g. IntelliRH), two changes bit this
project when moving to 2.0.0 GA — worth checking your other projects for the same:

- **Artifact rename**: `spring-ai-advisors-vector-store` -> `spring-ai-vector-store-advisor`.
- **Config property flattening**: the `.options.` segment was dropped from Ollama's chat/embedding
  properties. `spring.ai.ollama.chat.options.model` -> `spring.ai.ollama.chat.model`, and
  `spring.ai.ollama.embedding.options.model` -> `spring.ai.ollama.embedding.model`.

Full list: https://docs.spring.io/spring-ai/reference/upgrade-notes.html
