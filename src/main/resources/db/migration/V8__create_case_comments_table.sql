CREATE TABLE case_comments (
    id BIGSERIAL PRIMARY KEY,

    case_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,

    content VARCHAR(5000) NOT NULL,

    sensitivity_level VARCHAR(50) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_case_comment_case
        FOREIGN KEY (case_id)
        REFERENCES cases(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_case_comment_author
        FOREIGN KEY (author_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- Optional but recommended indexes
CREATE INDEX idx_case_comment_case_id
    ON case_comments(case_id);

CREATE INDEX idx_case_comment_author_id
    ON case_comments(author_id);
