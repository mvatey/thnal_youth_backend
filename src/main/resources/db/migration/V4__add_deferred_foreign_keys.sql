--adding FK
ALTER TABLE files
    ADD CONSTRAINT fk_file_uploaded_by
        FOREIGN KEY (uploaded_by)
            REFERENCES users(id)
            ON DELETE SET NULL;

ALTER TABLE branches
    ADD CONSTRAINT fk_branch_created_by
        FOREIGN KEY (created_by)
            REFERENCES users(id)
            ON DELETE SET NULL;

ALTER TABLE members
    ADD CONSTRAINT fk_member_created_by
        FOREIGN KEY (created_by)
            REFERENCES users(id)
            ON DELETE SET NULL;

