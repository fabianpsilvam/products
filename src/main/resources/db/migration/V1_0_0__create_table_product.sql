CREATE TABLE IF NOT EXISTS product (
	sku serial PRIMARY KEY,
	name varchar(255) NOT NULL,
	price FLOAT NOT NULL,
	brand varchar(255) NULL
);

CREATE INDEX product_sku_index ON product(sku);
CREATE INDEX product_name_index ON product(name);