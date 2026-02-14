CREATE INDEX idx_case_events_next_date
ON case_events (next_date)
WHERE active = TRUE;