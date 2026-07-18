--seed 1. roles
INSERT INTO roles (
    code,
    label_km,
    label_en,
    description,
    sort_order
)
VALUES
    (
        'ADMIN',
        'អ្នកគ្រប់គ្រង',
        'Administrator',
        'Full access to system administration and all branches.',
        1
    ),
    (
        'SECRETARY',
        'លេខាធិការ',
        'Secretary',
        'Manages organization records, activities, members, and documents.',
        2
    ),
    (
        'BRANCH_LEADER',
        'ប្រធានសាខា',
        'Branch Leader',
        'Manages information and operations within an assigned branch.',
        3
    ),
    (
        'MEMBER',
        'សមាជិក',
        'Member',
        'Standard member access.',
        4
    )
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              description = EXCLUDED.description,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();


--seed 2. account_statuses
INSERT INTO account_statuses (
    code,
    label_km,
    label_en,
    description,
    sort_order
)
VALUES
    (
        'ACTIVE',
        'សកម្ម',
        'Active',
        'The account can sign in and use the system.',
        1
    ),
    (
        'INACTIVE',
        'អសកម្ម',
        'Inactive',
        'The account is disabled and cannot sign in.',
        2
    ),
    (
        'SUSPENDED',
        'បានផ្អាក',
        'Suspended',
        'The account is temporarily suspended by an administrator.',
        3
    )
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              description = EXCLUDED.description,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();

--seed 3. member_statuses
INSERT INTO member_statuses (
    code,
    label_km,
    label_en,
    description,
    sort_order
)
VALUES
    (
        'ACTIVE',
        'សកម្ម',
        'Active',
        'The person is currently an active member.',
        1
    ),
    (
        'INACTIVE',
        'អសកម្ម',
        'Inactive',
        'The person is currently an inactive member.',
        2
    ),
    (
        'SUSPENDED',
        'បានផ្អាក',
        'Suspended',
        'The membership has been temporarily suspended.',
        3
    ),
    (
        'RESIGNED',
        'បានលាលែង',
        'Resigned',
        'The person voluntarily resigned from membership.',
        4
    )
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              description = EXCLUDED.description,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();

--seed 4. religions
INSERT INTO religions (
    code,
    label_km,
    label_en,
    sort_order
)
VALUES
    ('BUDDHISM', 'ព្រះពុទ្ធសាសនា', 'Buddhism', 1),
    ('ISLAM', 'សាសនាអ៊ីស្លាម', 'Islam', 2),
    ('CHRISTIANITY', 'សាសនាគ្រិស្ត', 'Christianity', 3),
    ('HINDUISM', 'សាសនាហិណ្ឌូ', 'Hinduism', 4),
    ('OTHER', 'ផ្សេងៗ', 'Other', 5),
    ('PREFER_NOT_TO_SAY', 'មិនចង់បញ្ជាក់', 'Prefer not to say', 6)
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();


--seed 5. member_levels
INSERT INTO member_levels (
    code,
    label_km,
    label_en,
    description,
    sort_order
)
VALUES
    (
        'LEVEL-ONE',
        'កាំ ក',
        'New Member',
        'A newly registered member.',
        1
    ),
    (
        'LEVEL-TWO',
        'កាំ ខ',
        'Regular Member',
        'A regular active member.',
        2
    ),
    (
        'LEVEL-THREE',
        'កាំ គ',
        'Senior Member',
        'A member with seniority or long-term service.',
        3
    ),
    (
        'LEVEL-FOUR',
        'កាំ ឃ',
        'Honorary Member',
        'An honorary member recognized by the organization.',
        4
    ),
    (
        'LEVEL-FIVE',
        'កាំ ង',
        'Honorary Member',
        'An honorary member recognized by the organization.',
        5
    )
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              description = EXCLUDED.description,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();

--seed 6. employment_sectors
INSERT INTO employment_sectors (
    code,
    label_km,
    label_en,
    sort_order
)
VALUES
    ('GOVERNMENT', 'ស្ថាប័នរដ្ឋ', 'Government', 1),
    ('PRIVATE_COMPANY', 'ក្រុមហ៊ុនឯកជន', 'Private Company', 2),
    ('NGO', 'អង្គការមិនមែនរដ្ឋាភិបាល', 'NGO', 3),
    ('SELF_EMPLOYED', 'អាជីវកម្មផ្ទាល់ខ្លួន', 'Self-employed', 4),
    ('ENTERPRISE', 'សហគ្រាស', 'Enterprise', 5),
    ('STUDENT', 'សិស្ស/និស្សិត', 'Student', 6),
    ('UNEMPLOYED', 'មិនមានការងារ', 'Unemployed', 7),
    ('OTHER', 'ផ្សេងៗ', 'Other', 8)
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();

--seed 7. education_levels
INSERT INTO education_levels (
    code,
    label_km,
    label_en,
    description,
    sort_order
)
VALUES
    ('NO_FORMAL_EDUCATION', 'មិនបានចូលរៀន', 'No Formal Education', NULL, 1),
    ('PRIMARY', 'បឋមសិក្សា', 'Primary School', NULL, 2),
    ('LOWER_SECONDARY', 'អនុវិទ្យាល័យ', 'Lower Secondary School', NULL, 3),
    ('UPPER_SECONDARY', 'វិទ្យាល័យ', 'Upper Secondary School', NULL, 4),
    ('VOCATIONAL', 'បណ្ដុះបណ្ដាលវិជ្ជាជីវៈ', 'Vocational Training', NULL, 5),
    ('ASSOCIATE', 'បរិញ្ញាបត្ររង', 'Associate Degree', NULL, 6),
    ('BACHELOR', 'បរិញ្ញាបត្រ', 'Bachelor Degree', NULL, 7),
    ('MASTER', 'បរិញ្ញាបត្រជាន់ខ្ពស់', 'Master Degree', NULL, 8),
    ('DOCTORATE', 'បណ្ឌិត', 'Doctorate', NULL, 9),
    ('OTHER', 'ផ្សេងៗ', 'Other', NULL, 10)
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              description = EXCLUDED.description,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();

--seed 8. proficiency_levels
                          INSERT INTO proficiency_levels (
    code,
    label_km,
    label_en,
    description,
    sort_order
)
                          VALUES
                              (
                              'BEGINNER',
                              'ចាប់ផ្តើម',
                              'Beginner',
                              'Has little or no experience.',
                              1
                              ),
                              (
                              'BASIC',
                              'មូលដ្ឋាន',
                              'Basic',
                              'Can perform simple tasks with guidance.',
                              2
                              ),
                              (
                              'INTERMEDIATE',
                              'មធ្យម',
                              'Intermediate',
                              'Can work independently on common tasks.',
                              3
                              ),
                              (
                              'ADVANCED',
                              'កម្រិតខ្ពស់',
                              'Advanced',
                              'Can handle complex tasks with confidence.',
                              4
                              ),
                              (
                              'EXPERT',
                              'ជំនាញ',
                              'Expert',
                              'Highly experienced and able to mentor others.',
                              5
                              )
                          ON CONFLICT (code) DO UPDATE
                                                    SET
                                                        label_km = EXCLUDED.label_km,
                                                    label_en = EXCLUDED.label_en,
                                                    description = EXCLUDED.description,
                                                    sort_order = EXCLUDED.sort_order,
                                                    updated_at = NOW();

--seed 9. branch_levels
INSERT INTO branch_levels (
    code,
    label_km,
    label_en,
    hierarchy_order,
    description,
    sort_order
)
VALUES
    (
        'PROVINCE',
        'សាខារាជធានី/ខេត្ត',
        'Province Branch',
        1,
        'សាខានៃរាជធានី/ខេត្ត',
        1
    ),
    (
        'DISTRICT',
        'អនុសាខាខណ្ឌ/ស្រុក',
        'District Branch',
        2,
        'អនុសាខានៃខណ្ឌ/ស្រុករបស់រាជធានី/ខេត្ត',
        2
    ),
    (
        'COMMUNE',
        'អនុសាខាសង្កាត់/ឃុំ',
        'Commune Branch',
        3,
        'អនុសាខានៃសង្កាត់/ឃុំរបស់ខណ្ឌ/ស្រុក',
        3
    )
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              hierarchy_order = EXCLUDED.hierarchy_order,
                              description = EXCLUDED.description,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();

--seed 10. branch_statuses
INSERT INTO branch_statuses (
    code,
    label_km,
    label_en,
    description,
    sort_order
)
VALUES
    (
        'ACTIVE',
        'សកម្ម',
        'Active',
        'The branch is currently active.',
        1
    ),
    (
        'INACTIVE',
        'អសកម្ម',
        'Inactive',
        'The branch is currently inactive.',
        2
    )
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              description = EXCLUDED.description,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();

--seed 11. positions
INSERT INTO positions (
    code,
    label_km,
    label_en,
    description,
    sort_order
)
VALUES
    (
        'BRANCH_LEADER',
        'ប្រធានសាខា',
        'Branch Leader',
        'Leads and manages a branch.',
        1
    ),
    (
        'SECRETARY',
        'លេខាធិការ',
        'Secretary',
        'Supports administration and record management for a branch.',
        2
    ),
    (
        'MEMBER',
        'សមាជិក',
        'Member',
        'Members.',
        3
    )
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              description = EXCLUDED.description,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();

--seed 12. activity_types
INSERT INTO activity_types (
    code,
    label_km,
    label_en,
    description,
    sort_order
)
VALUES
    (
        'INTERNAL',
        'កម្មវិធីផ្ទៃក្នុង',
        'Internal Activity',
        'An activity organized for members or internal participants.',
        1
    ),
    (
        'EXTERNAL',
        'កម្មវិធីខាងក្រៅ',
        'External Activity',
        'An activity involving external organizations or the public.',
        2
    )
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              description = EXCLUDED.description,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();

--seed 13. activity_sectors
INSERT INTO activity_sectors (
    code,
    label_km,
    label_en,
    description,
    sort_order
)
VALUES
    (
        'EDUCATION',
        'អប់រំ',
        'Education',
        'Education, training, and capacity-building activities.',
        1
    ),
    (
        'SOCIAL',
        'សង្គម',
        'Social',
        'Community and social-support activities.',
        2
    ),
    (
        'HEALTH',
        'សុខភាព',
        'Health',
        'Health promotion and well-being activities.',
        3
    ),
    (
        'ENVIRONMENT',
        'បរិស្ថាន',
        'Environment',
        'Environmental protection and awareness activities.',
        4
    ),
    (
        'CULTURE',
        'វប្បធម៌',
        'Culture',
        'Cultural and traditional activities.',
        5
    ),
    (
        'SPORT',
        'កីឡា',
        'Sport',
        'Sports and physical-development activities.',
        6
    ),
    (
        'VOLUNTEERING',
        'ស្ម័គ្រចិត្ត',
        'Volunteering',
        'Volunteer and community-service activities.',
        7
    ),
    (
        'OTHER',
        'ផ្សេងៗ',
        'Other',
        'Activities outside the listed sectors.',
        8
    )
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              description = EXCLUDED.description,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();

--seed 14. activity_statuses
INSERT INTO activity_statuses (
    code,
    label_km,
    label_en,
    description,
    sort_order
)
VALUES
    (
        'DRAFT',
        'ព្រាង',
        'Draft',
        'The activity is still being prepared.',
        1
    ),
    (
        'UPCOMING',
        'នឹងមកដល់',
        'Upcoming',
        'The activity is scheduled but has not started.',
        2
    ),
    (
        'ONGOING',
        'កំពុងដំណើរការ',
        'Ongoing',
        'The activity is currently in progress.',
        3
    ),
    (
        'COMPLETED',
        'បានបញ្ចប់',
        'Completed',
        'The activity has been completed.',
        4
    ),
    (
        'CANCELLED',
        'បានលុបចោល',
        'Cancelled',
        'The activity was cancelled.',
        5
    ),
    (
        'FROZEN',
        'បានបង្កក',
        'Frozen',
        'The activity record is temporarily frozen from modification.',
        6
    )
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              description = EXCLUDED.description,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();

--seed 15. attendance_statuses
INSERT INTO attendance_statuses (
    code,
    label_km,
    label_en,
    description,
    sort_order
)
VALUES
    (
        'PRESENT',
        'បានចូលរួម',
        'Present',
        'The member attended the activity.',
        1
    ),
    (
        'ABSENT',
        'មិនបានចូលរួម',
        'Absent',
        'The member did not attend the activity.',
        2
    )
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              description = EXCLUDED.description,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();

--seed 16. donation_types
INSERT INTO donation_types (
    code,
    label_km,
    label_en,
    description,
    sort_order
)
VALUES
    (
        'MONTHLY_DONATION',
        'ការបរិច្ចាគប្រចាំខែ',
        'Monthly Donation',
        'A recurring monthly donation made by a registered member.',
        1
    ),
    (
        'ACTIVITY_DONATION',
        'ការបរិច្ចាគសម្រាប់សកម្មភាព',
        'Activity Donation',
        'A donation allocated to a specific activity.',
        2
    ),
    (
        'SPONSOR_DONATION',
        'ការបរិច្ចាគពីអ្នកឧបត្ថម្ភ',
        'Sponsor Donation',
        'A donation made by a registered sponsor.',
        3
    )
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              description = EXCLUDED.description,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();

--seed 17. payment_methods
INSERT INTO payment_methods (
    code,
    label_km,
    label_en,
    description,
    sort_order
)
VALUES
    (
        'CASH',
        'សាច់ប្រាក់',
        'Cash',
        'Cash payment.',
        1
    ),
    (
        'ABA',
        'ABA',
        'ABA',
        'ABA Bank transfer.',
        2
    ),
    (
        'ACLEDA',
        'ACLEDA',
        'ACLEDA',
        'ACLEDA Bank transfer.',
        3
    ),
    (
        'WING',
        'Wing',
        'Wing',
        'Wing Money transfer.',
        4
    ),
    (
        'TRUEMONEY',
        'TrueMoney',
        'TrueMoney',
        'TrueMoney transfer.',
        5
    ),
    (
        'OTHER',
        'ផ្សេងៗ',
        'Other',
        'Another payment method.',
        6
    )
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              description = EXCLUDED.description,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();

--seed 18. sponsor_types
INSERT INTO sponsor_types (
    code,
    label_km,
    label_en,
    description,
    sort_order
)
VALUES
    (
        'INDIVIDUAL',
        'បុគ្គល',
        'Individual',
        'An individual external sponsor or donor.',
        1
    ),
    (
        'ORGANIZATION',
        'ស្ថាប័ន',
        'Organization',
        'A company, NGO, institution, association, or other organization.',
        2
    )
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              description = EXCLUDED.description,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();

--seed 19. document_types
INSERT INTO document_types (
    code,
    label_km,
    label_en,
    description,
    sort_order
)
VALUES
    (
        'ORGANIZATION_DOCUMENT',
        'ឯកសារអង្គភាព',
        'Organization Document',
        'A document belonging to the organization as a whole.',
        1
    ),
    (
        'BRANCH_DOCUMENT',
        'ឯកសារសាខា',
        'Branch Document',
        'A document belonging to a specific branch.',
        2
    ),
    (
        'ACTIVITY_DOCUMENT',
        'ឯកសារសកម្មភាព',
        'Activity Document',
        'A document related to a specific activity.',
        3
    ),
    (
        'MEMBER_DOCUMENT',
        'ឯកសារសមាជិក',
        'Member Document',
        'A document belonging to a specific member.',
        4
    ),
    (
        'ACTIVITY_CERTIFICATE',
        'វិញ្ញាបនបត្រសកម្មភាព',
        'Activity Certificate',
        'A certificate issued for participation in an activity.',
        5
    )
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              description = EXCLUDED.description,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();

--seed 20. notification_types
INSERT INTO notification_types (
    code,
    label_km,
    label_en,
    description,
    sort_order
)
VALUES
    (
        'ACTIVITY_INVITATION',
        'ការអញ្ជើញចូលរួមកម្មវិធី',
        'Activity Invitation',
        'Sent when a member is invited to an activity.',
        1
    ),
    (
        'ACTIVITY_UPDATED',
        'កម្មវិធីត្រូវបានកែប្រែ',
        'Activity Updated',
        'Sent when important activity information changes.',
        2
    ),
    (
        'ACTIVITY_CANCELLED',
        'កម្មវិធីត្រូវបានលុបចោល',
        'Activity Cancelled',
        'Sent when an activity is cancelled.',
        3
    ),
    (
        'ACTIVITY_REMINDER',
        'រំលឹកកម្មវិធី',
        'Activity Reminder',
        'Sent before an upcoming activity.',
        4
    ),
    (
        'DOCUMENT_ADDED',
        'ឯកសារថ្មីត្រូវបានបន្ថែម',
        'Document Added',
        'Sent when a relevant document is added.',
        5
    ),
    (
        'MEMBERSHIP_UPDATE',
        'បច្ចុប្បន្នភាពសមាជិកភាព',
        'Membership Update',
        'Sent when membership information or status changes.',
        6
    ),
    (
        'SYSTEM_ANNOUNCEMENT',
        'សេចក្តីជូនដំណឹងប្រព័ន្ធ',
        'System Announcement',
        'A general announcement sent by the organization.',
        7
    )
    ON CONFLICT (code) DO UPDATE
                              SET
                                  label_km = EXCLUDED.label_km,
                              label_en = EXCLUDED.label_en,
                              description = EXCLUDED.description,
                              sort_order = EXCLUDED.sort_order,
                              updated_at = NOW();

--seed 21. organization_profile
INSERT INTO organization_profile (
    id,
    name_km,
    name_en,
    tagline_km,
    tagline_en,
    about_km,
    about_en,
    address,
    phone,
    email,
    website,
    facebook_url,
    telegram_url,
    youtube_url,
    logo_file_id,
    cover_file_id,
    updated_by
)
VALUES (
           1,
           'សមាគមថ្នាលយុវជនកម្ពុជា',
           'Cambodian Youth Nursery Association',
           NULL,
           NULL,
           NULL,
           NULL,
           NULL,
           NULL,
           NULL,
           NULL,
           NULL,
           NULL,
           NULL,
           NULL,
           NULL,
           NULL
       )
    ON CONFLICT (id) DO UPDATE
                            SET
                                name_km = EXCLUDED.name_km,
                            name_en = EXCLUDED.name_en,
                            updated_at = NOW();