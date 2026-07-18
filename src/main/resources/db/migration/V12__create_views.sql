--50. activity_incomes
CREATE VIEW activity_incomes AS
SELECT
    d.id AS donation_id,
    d.activity_id,

    d.donation_no,
    d.donation_type_id,

    d.member_id,
    d.sponsor_id,
    d.donor_name,

    d.branch_id,

    d.amount_khr,
    d.amount_usd,
    d.exchange_rate_khr_per_usd,
    d.total_amount_usd,

    d.payment_method_id,
    d.paid_at AS received_at,

    d.payment_reference,
    d.receipt_file_id,

    d.recorded_by,
    d.note AS description,

    d.created_at,
    d.updated_at
FROM donations d
         JOIN donation_types dt
              ON dt.id = d.donation_type_id
WHERE dt.code = 'ACTIVITY_DONATION'
  AND d.activity_id IS NOT NULL;