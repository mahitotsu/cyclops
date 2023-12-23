DROP TABLE IF EXISTS items;
CREATE TABLE items (
    pk      varchar(256) PRIMARY KEY,
    item    jsonb
);