DROP TABLE IF EXISTS public.vector_store;

CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS vector_store (
  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  content  text,
  metadata jsonb,
  embedding vector(1536)          -- must match your embedding model dimension
);

-- HNSW index with cosine distance (pgvector ≥ 0.5)
CREATE INDEX IF NOT EXISTS vector_store_embedding_hnsw
  ON vector_store USING hnsw (embedding vector_cosine_ops);