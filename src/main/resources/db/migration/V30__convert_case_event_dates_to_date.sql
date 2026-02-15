-- Convert event_date from TIMESTAMP to DATE
ALTER TABLE case_events
ALTER COLUMN event_date TYPE DATE
USING event_date::DATE;

-- Convert next_date from TIMESTAMP to DATE
ALTER TABLE case_events
ALTER COLUMN next_date TYPE DATE
USING next_date::DATE;
