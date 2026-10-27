-- Altera publisher.country de INT para VARCHAR. Os valores validos sao definidos pelo enum Country na aplicacao.
ALTER TABLE publisher ALTER COLUMN country TYPE VARCHAR(30) USING country::VARCHAR;
