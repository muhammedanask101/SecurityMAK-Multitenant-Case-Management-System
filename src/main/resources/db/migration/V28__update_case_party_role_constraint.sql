ALTER TABLE case_parties
DROP CONSTRAINT IF EXISTS chk_case_party_role;

ALTER TABLE case_parties
ADD CONSTRAINT chk_case_party_role
CHECK (
    role IN (
        'PETITIONER',
        'PLAINTIFF',
        'RESPONDENT',
        'DEFENDANT',
        'ACCUSED',
        'COMPLAINANT',
        'WITNESS',
        'OTHER'
    )
);

