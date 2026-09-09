DROP TABLE IF EXISTS vector_store;

CREATE TABLE vector_store (
                              id UUID PRIMARY KEY,
                              content TEXT,
                              metadata JSONB,
                              embedding vector(768)  -- must match your embedding model dimensions
);
