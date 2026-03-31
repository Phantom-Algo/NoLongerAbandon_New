CREATE TABLE IF NOT EXISTS word (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    word TEXT NOT NULL UNIQUE,
    source_language TEXT NOT NULL DEFAULT 'en',
    search_count INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS word_card (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    word_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    raw_markdown TEXT NOT NULL,
    generated_by_model TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (word_id) REFERENCES word(id)
);

CREATE TABLE IF NOT EXISTS word_card_section (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    word_card_id INTEGER NOT NULL,
    section_key TEXT NOT NULL,
    section_title TEXT NOT NULL,
    section_content TEXT NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_preset INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (word_card_id) REFERENCES word_card(id)
);

CREATE TABLE IF NOT EXISTS vocabulary_book (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vocabulary_book_word (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    vocabulary_book_id INTEGER NOT NULL,
    word_id INTEGER NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (vocabulary_book_id, word_id),
    FOREIGN KEY (vocabulary_book_id) REFERENCES vocabulary_book(id),
    FOREIGN KEY (word_id) REFERENCES word(id)
);

CREATE TABLE IF NOT EXISTS ai_chat_session (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    model_code TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_chat_message (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id INTEGER NOT NULL,
    role TEXT NOT NULL,
    content TEXT NOT NULL,
    referenced_context TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES ai_chat_session(id)
);

CREATE TABLE IF NOT EXISTS model_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    provider_name TEXT NOT NULL,
    display_name TEXT NOT NULL,
    base_url TEXT,
    model_name TEXT NOT NULL,
    api_key_encrypted TEXT,
    enabled INTEGER NOT NULL DEFAULT 1,
    is_word_generation_default INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_profile (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nickname TEXT,
    profile_markdown TEXT,
    allow_ai_read_profile INTEGER NOT NULL DEFAULT 0,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);