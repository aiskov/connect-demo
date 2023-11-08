CREATE SCHEMA IF NOT EXISTS `invoices`;
USE `invoices`;


CREATE TABLE client
(
    id              char(36)     NOT NULL,
    name            varchar(255) NOT NULL,

    created_at      timestamp    NOT NULL,
    last_updated_at timestamp    NOT NULL,

    PRIMARY KEY (id)
);

CREATE TABLE invoice
(
    id              char(36)     NOT NULL,
    code            varchar(255) NOT NULL,
    status          varchar(255) NOT NULL,

    created_at      timestamp    NOT NULL,
    last_updated_at timestamp    NOT NULL,

    client_id       char(36)     NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT invoice_to_client FOREIGN KEY (client_id) REFERENCES client (id)
);

CREATE TABLE invoice_item
(
    id         char(36)       NOT NULL,
    invoice_id char(36)       NOT NULL,

    name       varchar(255)   NOT NULL,

    quantity   int            NOT NULL,
    price      decimal(10, 2) NOT NULL,

    primary key (id),

    CONSTRAINT invoice_item_to_invoice FOREIGN KEY (invoice_id) REFERENCES invoice (id)
);

INSERT INTO client (id, name, created_at, last_updated_at) VALUES ('c1', 'Client 1', NOW(), NOW());
INSERT INTO client (id, name, created_at, last_updated_at) VALUES ('c2', 'Client 2', NOW(), NOW());
