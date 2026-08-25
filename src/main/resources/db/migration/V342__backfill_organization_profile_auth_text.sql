UPDATE organization_profile
SET hero_headline_km = COALESCE(NULLIF(hero_headline_km, ''), 'សមាជិក · សកម្មភាព · វិភាគទាន'),
    hero_headline_en = COALESCE(NULLIF(hero_headline_en, ''), 'Members · Activities · Donations'),
    hero_description_km = COALESCE(NULLIF(hero_description_km, ''), 'គ្រប់គ្រងទិន្នន័យសមាជិក ការបង់វិភាគទាន និងសកម្មភាពទាំងនៅទីនេះដោយពួកគេ'),
    hero_description_en = COALESCE(NULLIF(hero_description_en, ''), 'Manage member data, donations, and activities in one place.'),
    tagline_km = COALESCE(NULLIF(tagline_km, ''), 'ការគ្រប់គ្រងប្រព័ន្ធយុវជន'),
    tagline_en = COALESCE(NULLIF(tagline_en, ''), 'Youth management system')
WHERE id = 1;
