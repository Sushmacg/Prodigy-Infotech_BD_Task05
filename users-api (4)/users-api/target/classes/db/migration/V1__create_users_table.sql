CREATE TABLE users (
    id CHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_age CHECK (age >= 0 AND age <= 150)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
