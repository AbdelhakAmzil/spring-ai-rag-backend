ALTER TABLE uploaded_documents
    ADD COLUMN chunks_indexed INTEGER NOT NULL DEFAULT 0;