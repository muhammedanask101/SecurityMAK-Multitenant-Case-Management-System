-- Step 1: Add temporary column
ALTER TABLE case_comments
ADD COLUMN sensitivity_level_tmp SMALLINT;

-- Step 2: Convert existing VARCHAR values to numeric
UPDATE case_comments
SET sensitivity_level_tmp =
    CASE sensitivity_level
        WHEN 'LOW' THEN 1
        WHEN 'MEDIUM' THEN 2
        WHEN 'HIGH' THEN 3
        WHEN 'CRITICAL' THEN 4
    END;

-- Step 3: Drop old column
ALTER TABLE case_comments
DROP COLUMN sensitivity_level;

-- Step 4: Rename new column
ALTER TABLE case_comments
RENAME COLUMN sensitivity_level_tmp TO sensitivity_level;

-- Optional: make NOT NULL if needed
ALTER TABLE case_comments
ALTER COLUMN sensitivity_level SET NOT NULL;
