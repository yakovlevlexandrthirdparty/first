SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS users;

CREATE TABLE users
(
    id          INT(11) NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50),
    password    CHAR(80),
    first_name  VARCHAR(50),
    last_name   VARCHAR(50),
    email       VARCHAR(50),
    phone       VARCHAR(15),
    telegram_id INT(11) NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS roles;

CREATE TABLE roles
(
    id   INT(11) NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS users_roles;

CREATE TABLE users_roles
(
    user_id INT(11) NOT NULL,
    role_id INT(11) NOT NULL,

    PRIMARY KEY (user_id, role_id),

--  KEY FK_ROLE_idx (role_id),

    CONSTRAINT FK_USER_ID_01 FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE NO ACTION ON UPDATE NO ACTION,

    CONSTRAINT FK_ROLE_ID FOREIGN KEY (role_id)
        REFERENCES roles (id)
        ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS adverts;

CREATE TABLE adverts
(
    id        INT(11)        NOT NULL AUTO_INCREMENT,
    price     DECIMAL(10, 2) NOT NULL,
    title     varchar(255)   NOT NULL,
    description VARCHAR(255),
    author_id INT(11)        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_ADVERT_TO_USER FOREIGN KEY (author_id)
        REFERENCES users (id)
);

DROP TABLE IF EXISTS orders;

CREATE TABLE orders
(
    id INT(11) NOT NULL AUTO_INCREMENT,
    recipient_id INT(11) NOT NULL,
    advert_id INT(11) NOT NULL,
    state INT(2) NOT NULL,
    PRIMARY KEY (id)
);


DROP TABLE IF EXISTS order_dates;

CREATE TABLE order_dates
(
    id           int(11) NOT NULL AUTO_INCREMENT,
    recipient_id INT(11) NOT NULL,
    date         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    advert_id    INT(11) NOT NULL,
    state        int(2)  NOT NULL,
    order_id     int(11) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_ORDER_ID FOREIGN KEY (order_id)
        REFERENCES orders (id),
    CONSTRAINT FK_ORDER_TO_RECIPIENT FOREIGN KEY (recipient_id)
        REFERENCES users (id),
    CONSTRAINT FK_ORDER_TO_ADVERT FOREIGN KEY (advert_id)
        REFERENCES adverts (id)
);

DROP TABLE IF EXISTS advert_images;

CREATE TABLE advert_images
(
    id        INT(11)      NOT NULL AUTO_INCREMENT,
    advert_id INT(11)      NOT NULL,
    name      VARCHAR(250),
    path      VARCHAR(250) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_ADVERT_ID_IMG FOREIGN KEY (advert_id)
        REFERENCES adverts (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

SET
    FOREIGN_KEY_CHECKS = 1;

INSERT INTO roles (name)
VALUES ('ROLE_EMPLOYEE'),
       ('ROLE_MANAGER'),
       ('ROLE_ADMIN');