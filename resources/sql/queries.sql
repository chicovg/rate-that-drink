-- :name create-user! :<!
-- :doc creates a new user record
INSERT INTO users
(first_name, last_name, email, pass)
VALUES (:first_name, :last_name, :email, :pass)
RETURNING id, first_name, last_name, email

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
    comments,
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
    :comments,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
RETURNING id

-- :name update-beer! :! :n
-- :doc updates an existing beer record
UPDATE beers
SET name = :name,
    brewery = :brewery,
    style = :style,
    appearance = :appearance,
    smell = :smell,
    taste = :taste,
    aftertaste = :aftertaste,
    drinkability = :drinkability,
    comments = :comments,
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

-- :name query-drinks :? :*
-- :doc queries for drink records
SELECT d.*,
       ((appearance + smell + (3 * taste)) / 5.0) as rating
FROM drink d
WHERE user_id = :user_id

-- :name get-drink :? :1
-- :doc get a sindle drink by id
SELECT * FROM drink
WHERE id = :id

-- :name create-drink! :<!
-- :doc creates a new drink record
INSERT INTO drink (
    user_id,
    name,
    maker,
    type,
    style,
    appearance,
    appearance_notes,
    smell,
    smell_notes,
    taste,
    taste_notes,
    comments,
    created_at,
    updated_at
)
VALUES (
    :user_id,
    :name,
    :maker,
    :type,
    :style,
    :appearance,
    :appearance_notes,
    :smell,
    :smell_notes,
    :taste,
    :taste_notes,
    :comments,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
RETURNING *

-- :name update-drink! :<!
-- :doc updates an existing drink record
UPDATE drink
SET name = :name,
    maker = :maker,
    type = :type,
    style = :style,
    appearance = :appearance,
    appearance_notes = :appearance_notes,
    smell = :smell,
    smell_notes = :smell_notes,
    taste = :taste,
    taste_notes = :taste_notes,
    comments = :comments,
    updated_at = CURRENT_TIMESTAMP
WHERE id = :id
RETURNING *

-- :name delete-drink! :! :n
-- :doc deletes the drink record with the given id
DELETE FROM drink
WHERE id = :id
