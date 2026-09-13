CREATE TABLE conversations (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               title VARCHAR(255) NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE uploaded_documents (
                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                    filename VARCHAR(255) NOT NULL,
                                    uploaded_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE chat_messages (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
                               role VARCHAR(20) NOT NULL,
                               content TEXT NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE message_sources (
                                 message_id UUID NOT NULL REFERENCES chat_messages(id) ON DELETE CASCADE,
                                 document_id UUID NOT NULL REFERENCES uploaded_documents(id) ON DELETE CASCADE,
                                 PRIMARY KEY (message_id, document_id)
);