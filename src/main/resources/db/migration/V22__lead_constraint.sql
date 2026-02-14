CREATE UNIQUE INDEX unique_lead_per_case
ON case_assignments (case_id)
WHERE role = 'LEAD' AND active = TRUE;