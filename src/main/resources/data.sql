-- =============================================
-- SAMPLE DATA FOR FIXIT APPLICATION
-- Run this to populate countries and cities
-- =============================================

-- COUNTRIES
INSERT INTO countries (id, name, iso2, iso3, phone_code) VALUES 
(1, 'United States', 'US', 'USA', '+1'),
(2, 'Canada', 'CA', 'CAN', '+1'),
(3, 'United Kingdom', 'GB', 'GBR', '+44'),
(4, 'Australia', 'AU', 'AUS', '+61'),
(5, 'Germany', 'DE', 'DEU', '+49'),
(6, 'France', 'FR', 'FRA', '+33'),
(7, 'India', 'IN', 'IND', '+91'),
(8, 'Japan', 'JP', 'JPN', '+81'),
(9, 'Brazil', 'BR', 'BRA', '+55'),
(10, 'Mexico', 'MX', 'MEX', '+52');

-- CITIES - United States
INSERT INTO cities (id, name, country_id, state_name) VALUES 
(1, 'New York', 1, 'New York'),
(2, 'Los Angeles', 1, 'California'),
(3, 'Chicago', 1, 'Illinois'),
(4, 'Houston', 1, 'Texas'),
(5, 'Phoenix', 1, 'Arizona'),
(6, 'Philadelphia', 1, 'Pennsylvania'),
(7, 'San Antonio', 1, 'Texas'),
(8, 'San Diego', 1, 'California'),
(9, 'Dallas', 1, 'Texas'),
(10, 'San Jose', 1, 'California');

-- CITIES - Canada
INSERT INTO cities (id, name, country_id, state_name) VALUES 
(11, 'Toronto', 2, 'Ontario'),
(12, 'Montreal', 2, 'Quebec'),
(13, 'Vancouver', 2, 'British Columbia'),
(14, 'Calgary', 2, 'Alberta'),
(15, 'Edmonton', 2, 'Alberta'),
(16, 'Ottawa', 2, 'Ontario');

-- CITIES - United Kingdom
INSERT INTO cities (id, name, country_id, state_name) VALUES 
(17, 'London', 3, 'England'),
(18, 'Manchester', 3, 'England'),
(19, 'Birmingham', 3, 'England'),
(20, 'Glasgow', 3, 'Scotland'),
(21, 'Edinburgh', 3, 'Scotland'),
(22, 'Liverpool', 3, 'England');

-- CITIES - Australia
INSERT INTO cities (id, name, country_id, state_name) VALUES 
(23, 'Sydney', 4, 'New South Wales'),
(24, 'Melbourne', 4, 'Victoria'),
(25, 'Brisbane', 4, 'Queensland'),
(26, 'Perth', 4, 'Western Australia'),
(27, 'Adelaide', 4, 'South Australia');

-- CITIES - Germany
INSERT INTO cities (id, name, country_id, state_name) VALUES 
(28, 'Berlin', 5, 'Berlin'),
(29, 'Munich', 5, 'Bavaria'),
(30, 'Hamburg', 5, 'Hamburg'),
(31, 'Frankfurt', 5, 'Hesse'),
(32, 'Cologne', 5, 'North Rhine-Westphalia');

-- CITIES - France
INSERT INTO cities (id, name, country_id, state_name) VALUES 
(33, 'Paris', 6, 'Île-de-France'),
(34, 'Marseille', 6, 'Provence-Alpes-Côte d''Azur'),
(35, 'Lyon', 6, 'Auvergne-Rhône-Alpes'),
(36, 'Toulouse', 6, 'Occitanie'),
(37, 'Nice', 6, 'Provence-Alpes-Côte d''Azur');

-- CITIES - India
INSERT INTO cities (id, name, country_id, state_name) VALUES 
(38, 'Mumbai', 7, 'Maharashtra'),
(39, 'Delhi', 7, 'Delhi'),
(40, 'Bangalore', 7, 'Karnataka'),
(41, 'Hyderabad', 7, 'Telangana'),
(42, 'Chennai', 7, 'Tamil Nadu'),
(43, 'Kolkata', 7, 'West Bengal');

-- CITIES - Japan
INSERT INTO cities (id, name, country_id, state_name) VALUES 
(44, 'Tokyo', 8, 'Tokyo'),
(45, 'Osaka', 8, 'Osaka'),
(46, 'Kyoto', 8, 'Kyoto'),
(47, 'Yokohama', 8, 'Kanagawa'),
(48, 'Nagoya', 8, 'Aichi');

-- CITIES - Brazil
INSERT INTO cities (id, name, country_id, state_name) VALUES 
(49, 'São Paulo', 9, 'São Paulo'),
(50, 'Rio de Janeiro', 9, 'Rio de Janeiro'),
(51, 'Brasília', 9, 'Federal District'),
(52, 'Salvador', 9, 'Bahia'),
(53, 'Fortaleza', 9, 'Ceará');

-- CITIES - Mexico
INSERT INTO cities (id, name, country_id, state_name) VALUES 
(54, 'Mexico City', 10, 'Mexico City'),
(55, 'Guadalajara', 10, 'Jalisco'),
(56, 'Monterrey', 10, 'Nuevo León'),
(57, 'Puebla', 10, 'Puebla'),
(58, 'Tijuana', 10, 'Baja California');

-- CATEGORIES
INSERT INTO categories (id, name, is_active) VALUES 
(1, 'Computer & IT Support', true),
(2, 'Home Appliance Repair', true),
(3, 'Plumbing', true),
(4, 'Electrical Work', true),
(5, 'Automotive Repair', true),
(6, 'Smartphone & Tablet Support', true),
(7, 'Home Improvement', true),
(8, 'Gardening & Landscaping', true),
(9, 'Tutoring & Education', true),
(10, 'Health & Fitness Coaching', true);

-- SAMPLE ADMIN USER (password: admin123)
-- Password is BCrypt encoded
INSERT INTO users (id, email, password_hash, first_name, last_name, user_type, account_status, created_at) VALUES 
(1, 'admin@fixit.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'User', 'ADMIN', 'ACTIVE', CURRENT_TIMESTAMP);

-- Note: To create more test users, use the registration form in the frontend
-- or use an online BCrypt generator for passwords
