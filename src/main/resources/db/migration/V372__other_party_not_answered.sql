-- "Came from another party" becomes a three-way answer like the voter box:
-- not answered (NULL, the default), yes, no. The old FALSE was the default for
-- everyone, so it is reset to "not answered"; a TRUE was ticked on purpose
-- (and a member with a former party's name is a "yes" regardless).

ALTER TABLE members
    ALTER COLUMN from_other_party DROP NOT NULL,
    ALTER COLUMN from_other_party DROP DEFAULT;

UPDATE members
SET from_other_party = CASE
        WHEN from_other_party = TRUE THEN TRUE
        WHEN former_party IS NOT NULL AND BTRIM(former_party) <> '' THEN TRUE
        ELSE NULL
    END;
