-- "Registered voter" (members) and "has a location" / "registered by law"
-- (branches) become three-way answers: not answered (NULL, the default), yes,
-- no. The old values were mostly a blanket default (every member and branch
-- was backfilled to TRUE), so they are reset to "not answered" -- except where
-- the data itself shows the answer: a member with a voter card number is a
-- voter, a branch with a map link has a location, a branch with a registration
-- document is registered, and a branch that was deliberately set to FALSE
-- (the default was TRUE) stays "no".

ALTER TABLE members
    ALTER COLUMN registered_voter DROP NOT NULL,
    ALTER COLUMN registered_voter DROP DEFAULT;

UPDATE members
SET registered_voter = CASE
        WHEN voter_card_number IS NOT NULL AND BTRIM(voter_card_number) <> '' THEN TRUE
        ELSE NULL
    END;

ALTER TABLE branches
    ALTER COLUMN has_location DROP NOT NULL,
    ALTER COLUMN has_location DROP DEFAULT,
    ALTER COLUMN legally_registered DROP NOT NULL,
    ALTER COLUMN legally_registered DROP DEFAULT;

UPDATE branches
SET has_location = CASE
        WHEN has_location = FALSE THEN FALSE
        WHEN google_map_url IS NOT NULL AND BTRIM(google_map_url) <> '' THEN TRUE
        ELSE NULL
    END,
    legally_registered = CASE
        WHEN legally_registered = FALSE THEN FALSE
        WHEN legal_document_file_id IS NOT NULL THEN TRUE
        ELSE NULL
    END;
