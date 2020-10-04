-- :name create-user! :<!
-- :doc creates a new user record
INSERT INTO users
(first_name, last_name, email, pass)
VALUES (:first_name, :last_name, :email, :pass)
RETURNING id

-- :name update-user! :! :n
-- :doc updates an existing user record
UPDATE users
SET first_name = :first_name, last_name = :last_name, email = :email
WHERE id = :id

-- :name get-user :? :1
-- :doc retrieves a user record given the id
SELECT id, first_name, last_name, email
FROM users
WHERE id = :id

-- :name get-user-by-email :? :1
-- :doc retrieves the user record matching the provided email address
SELECT id, first_name, last_name, email, pass
FROM users
WHERE email = :email

-- :name delete-user! :? :n
-- :doc deletes a user record given the id
DELETE FROM users
WHERE id = :id

-- :name create-beer! :<!
-- :doc creates a new beer record
INSERT INTO beers (
    user_id,
    name,
    brewery,
    style,
    appearance,
    smell,
    taste,
    aftertaste,
    drinkability,
    created_at,
    updated_at
)
VALUES (
    :user_id,
    :name,
    :brewery,
    :style,
    :appearance,
    :smell,
    :taste,
    :aftertaste,
    :drinkability,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
RETURNING id

-- :name update-beer! :! :n
-- :doc updates an existing beer record
UPDATE beers
SET name = :name,
    brewery = :brewery,
    appearance = :appearance,
    smell = :smell,
    taste = :taste,
    aftertaste = :aftertaste,
    drinkability = :drinkability,
    updated_at = CURRENT_TIMESTAMP
WHERE id = :id

-- :name get-beers :? :*
-- :doc gets the full list of beers from the database
SELECT * FROM beers
WHERE user_id = :user_id

-- :name get-beer :? :1
-- :doc gets a single beer by id
SELECT * FROM beers
WHERE id = :id

-- :name delete-beer! :! :n
-- :doc deletes the beer record with the given id
DELETE FROM beers
WHERE id = :id
