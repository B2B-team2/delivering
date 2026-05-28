CREATE SCHEMA IF NOT EXISTS "operation-db";
SET search_path TO "operation-db", public;

CREATE TABLE IF NOT EXISTS "p_slack_messages" (
    message_id          UUID          NOT NULL,
    receiver_user_id    UUID,
    receiver_slack_id   VARCHAR(36)   NOT NULL,
    message_content     TEXT          NOT NULL,
    reference_type      VARCHAR(30),
    status              VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    sent_at             TIMESTAMP,

    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID,
    updated_at          TIMESTAMP,
    updated_by          UUID,
    deleted_at          TIMESTAMP,
    deleted_by          UUID,

    CONSTRAINT pk_p_slack_messages PRIMARY KEY (message_id)
    );

CREATE INDEX IF NOT EXISTS idx_slack_messages_receiver_user_id ON "p_slack_messages" (receiver_user_id);
CREATE INDEX IF NOT EXISTS idx_slack_messages_status ON "p_slack_messages" (status);