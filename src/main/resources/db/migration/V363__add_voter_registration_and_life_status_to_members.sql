-- Voter registration ("មានឈ្មោះបោះឆ្នោត") and life status (alive / deceased)
-- on members, plus three more activity types so the organization's five
-- reporting categories each exist as a type.

ALTER TABLE members
    ADD COLUMN registered_voter BOOLEAN NOT NULL DEFAULT FALSE;

-- Every existing member is marked registered: most have no date of birth
-- on file, so an "18 or older" backfill would leave nearly all of them
-- unchecked. New members take the value entered on the create form.
UPDATE members
SET registered_voter = TRUE;

ALTER TABLE members
    ADD COLUMN life_status VARCHAR(20) NOT NULL DEFAULT 'ALIVE';

ALTER TABLE members
    ADD CONSTRAINT chk_members_life_status
        CHECK (life_status IN ('ALIVE', 'DECEASED'));

-- INTERNAL ("ប្រជុំផ្ទៃក្នុង") and EXTERNAL ("ជាមួយស្ថាប័នខាងក្រៅ") already
-- exist; these are the other three report categories.
INSERT INTO activity_types (code, label_km, label_en, description, sort_order)
VALUES
    (
        'BRANCH_MEETING',
        'ប្រជុំសាខា/អនុសាខា',
        'Branch / Sub-branch Meeting',
        'A meeting of a branch or sub-branch.',
        3
    ),
    (
        'GIFT_DISTRIBUTION',
        'ចុះចែកអំណោយ',
        'Gift Distribution',
        'Going out to distribute gifts to families.',
        4
    ),
    (
        'FIELD_SUPPORT',
        'ចុះជួយមូលដ្ឋាន',
        'Field Support',
        'Going out to help a local community.',
        5
    )
ON CONFLICT (code) DO NOTHING;
