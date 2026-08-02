ALTER TABLE app_users ADD COLUMN first_name VARCHAR(60);
ALTER TABLE app_users ADD COLUMN last_name VARCHAR(60);

UPDATE app_users
SET first_name = SPLIT_PART(full_name, ' ', 1),
    last_name = CASE
        WHEN POSITION(' ' IN full_name) > 0 THEN SUBSTRING(full_name FROM POSITION(' ' IN full_name) + 1)
        ELSE first_name
    END;

ALTER TABLE app_users ALTER COLUMN first_name SET NOT NULL;
ALTER TABLE app_users ALTER COLUMN last_name SET NOT NULL;
ALTER TABLE app_users DROP COLUMN full_name;
