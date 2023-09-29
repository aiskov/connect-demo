CREATE SCHEMA IF NOT EXISTS `invoices`;
USE `invoices`;

CREATE TABLE invoice
(
    id              char(36)     NOT NULL,
    code            varchar(255) NOT NULL,

    created_at      timestamp    NOT NULL,
    last_updated_at timestamp    NOT NULL,

    PRIMARY KEY (id)
);

CREATE TABLE invoice_item
(
    id              char(36)  NOT NULL,
    invoice_id      char(36)  NOT NULL,

    name            varchar(255) NOT NULL,

    primary key (id),

    CONSTRAINT invoice_item_to_invoice FOREIGN KEY (invoice_id) REFERENCES invoice (id)
);