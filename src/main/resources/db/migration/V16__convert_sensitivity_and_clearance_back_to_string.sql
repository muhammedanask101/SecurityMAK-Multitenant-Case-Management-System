ALTER TABLE cases
ADD COLUMN sensitivity_level_tmp VARCHAR(20);

UPDATE cases
SET sensitivity_level_tmp =
    CASE sensitivity_level
        WHEN 1 THEN 'LOW'
        WHEN 2 THEN 'MEDIUM'
        WHEN 3 THEN 'HIGH'
        WHEN 4 THEN 'CRITICAL'
        WHEN 0 THEN 'LOW'
    END;

ALTER TABLE cases DROP COLUMN sensitivity_level;
ALTER TABLE cases RENAME COLUMN sensitivity_level_tmp TO sensitivity_level;
ALTER TABLE cases ALTER COLUMN sensitivity_level SET NOT NULL;


ALTER TABLE case_comments
ADD COLUMN sensitivity_level_tmp VARCHAR(20);

UPDATE case_comments
SET sensitivity_level_tmp =
    CASE sensitivity_level
        WHEN 1 THEN 'LOW'
        WHEN 2 THEN 'MEDIUM'
        WHEN 3 THEN 'HIGH'
        WHEN 4 THEN 'CRITICAL'
        WHEN 0 THEN 'LOW'
    END;

ALTER TABLE case_comments DROP COLUMN sensitivity_level;
ALTER TABLE case_comments RENAME COLUMN sensitivity_level_tmp TO sensitivity_level;
ALTER TABLE case_comments ALTER COLUMN sensitivity_level SET NOT NULL;

ALTER TABLE users
ADD COLUMN clearance_level_tmp VARCHAR(20);

UPDATE users
SET clearance_level_tmp =
    CASE clearance_level
        WHEN 1 THEN 'LOW'
        WHEN 2 THEN 'MEDIUM'
        WHEN 3 THEN 'HIGH'
        WHEN 4 THEN 'CRITICAL'
        WHEN 0 THEN 'LOW'
    END;

ALTER TABLE users DROP COLUMN clearance_level;
ALTER TABLE users RENAME COLUMN clearance_level_tmp TO clearance_level;
ALTER TABLE users ALTER COLUMN clearance_level SET NOT NULL;