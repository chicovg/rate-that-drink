DELETE FROM drink d
WHERE EXISTS (SELECT * FROM beers b WHERE b.name = d.name);
