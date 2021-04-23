INSERT INTO drink (
    user_id,
    name,
    maker,
    type,
    style,
    appearance,
    smell,
    taste,
    comments,
    created_at,
    updated_at
)
SELECT
    user_id,
    name,
    brewery,
    'beer',
    style,
    ceil(appearance / 2),
    ceil(smell / 2),
    ceil((taste + aftertaste + drinkability) / 16),
    comments,
    created_at,
    CURRENT_TIMESTAMP
FROM beers b
WHERE NOT EXISTS (SELECT * from drink d WHERE d.name = b.name);
