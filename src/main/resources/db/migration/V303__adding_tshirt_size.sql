ALTER TABLE members
    ADD COLUMN tshirt_size VARCHAR(10);

ALTER TABLE members
    ADD CONSTRAINT chk_member_tshirt_size
        CHECK (
            tshirt_size IS NULL
                OR tshirt_size IN (
                                   'XS',
                                   'S',
                                   'M',
                                   'L',
                                   'XL',
                                   '2XL',
                                   '3XL'
                )
            );

