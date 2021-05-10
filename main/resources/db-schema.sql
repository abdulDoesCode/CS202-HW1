create table if not exists sample(
    id serial PRIMARY KEY,
                                     name VARCHAR(64) NOT NULL,
    data text,
    value int default 0
    );

create FUNCTION sample_trigger() RETURNS TRIGGER AS
    '
    BEGIN
        IF (SELECT value FROM sample where id = NEW.id ) > 1000
           THEN
           RAISE SQLSTATE ''23503'';
           END IF;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

create TRIGGER sample_value AFTER insert ON sample
    FOR EACH ROW EXECUTE PROCEDURE sample_trigger();

create table if not exists zip_city(
        zip int PRIMARY KEY UNIQUE ,
        city VARCHAR(64)
    );
create table if not exists company(
    name VARCHAR(64) PRIMARY KEY ,
    country VARCHAR(64),
    zip int,
    street VARCHAR(64),
    phone VARCHAR(64) NOT NULL UNIQUE,
    foreign key (zip) references zip_city(zip)
    );

create table if not exists emails(
    name VARCHAR(64),
    email VARCHAR(64),
    primary key (name,email),
    foreign key (name) references company(name)
    );

create table if not exists product(
    id serial PRIMARY KEY,
    name VARCHAR(64) NOT NULL ,
    description VARCHAR(64),
    brand_name VARCHAR(64)
    );

create table if not exists produce(
    id serial PRIMARY KEY ,
    company VARCHAR(64) NOT NULL,
    product_id int NOT NULL ,
    capacity int,
    foreign key (company) references company(name),
    foreign key (product_id) references product(id)
    );

create table if not exists product_order(
    id serial PRIMARY KEY ,
    company VARCHAR(64) NOT NULL ,
    product_id int NOT NULL ,
    amount int,
    order_date timestamp with time zone,
    foreign key (company) references company(name),
    foreign key (product_id)references product(id)
    );

create FUNCTION order_trigger() RETURNS TRIGGER AS
    '
    BEGIN
        IF (SELECT SUM(amount) FROM product_order where company = NEW.company AND product_id = NEW.product_id ) >
            (SELECT capacity FROM produce WHERE company = NEW.company AND product_id = NEW.product_id )

            THEN
            RAISE SQLSTATE ''23503'';
            END IF;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

create TRIGGER order_capacity AFTER insert ON product_order
    FOR EACH ROW EXECUTE PROCEDURE order_trigger();
