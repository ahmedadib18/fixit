-- Verify data loaded
SELECT 'Countries:' as table_name, COUNT(*) as count FROM countries
UNION ALL
SELECT 'Cities:', COUNT(*) FROM cities
UNION ALL
SELECT 'Categories:', COUNT(*) FROM categories;

-- Show sample data
SELECT 'Sample Countries:' as info;
SELECT id, name, iso2 FROM countries LIMIT 5;

SELECT 'Sample Cities:' as info;
SELECT id, name, country_id FROM cities LIMIT 5;
