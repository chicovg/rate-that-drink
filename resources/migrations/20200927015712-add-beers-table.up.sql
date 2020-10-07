CREATE TABLE beers
(id SERIAL PRIMARY KEY,
 user_id INTEGER NOT NULL,
 name VARCHAR(100) NOT NULL,
 brewery VARCHAR(100),
 style VARCHAR(100),
 appearance INT,
 smell INT,
 taste INT,
 aftertaste INT,
 drinkability INT,
 created_at TIMESTAMP NOT NULL,
 updated_at TIMESTAMP NOT NULL,
 comments TEXT,
 CONSTRAINT fk_user
   FOREIGN KEY (user_id)
     REFERENCES users(id)
);
