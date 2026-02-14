ALTER TABLE cases
ADD COLUMN sensitivity_level_tmp SMALLINT;

UPDATE cases
SET sensitivity_level_tmp =
    CASE sensitivity_level
        WHEN 'LOW' THEN 1
        WHEN 'MEDIUM' THEN 2
        WHEN 'HIGH' THEN 3
        WHEN 'CRITICAL' THEN 4
    END;

ALTER TABLE cases
DROP COLUMN sensitivity_level;

ALTER TABLE cases
RENAME COLUMN sensitivity_level_tmp TO sensitivity_level;

ALTER TABLE cases
ALTER COLUMN sensitivity_level SET NOT NULL;


ALTER TABLE users
ADD COLUMN clearance_level_tmp SMALLINT;

UPDATE users
SET clearance_level_tmp =
    CASE clearance_level
        WHEN 'LOW' THEN 1
        WHEN 'MEDIUM' THEN 2
        WHEN 'HIGH' THEN 3
        WHEN 'CRITICAL' THEN 4
    END;

ALTER TABLE users
DROP COLUMN clearance_level;

ALTER TABLE users
RENAME COLUMN clearance_level_tmp TO clearance_level;

ALTER TABLE users
ALTER COLUMN clearance_level SET NOT NULL;
