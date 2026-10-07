-- =====================================================
-- IPOS-PU Seed Data
-- Nova Solutions — Team 24/C
-- =====================================================
-- Run AFTER ipos_pu_schema.sql.
-- Populates test data for development and demo.
--
-- Required minimums:
--   10+ products (for catalogue browsing demo)
--   3+ members   (non-commercial, commercial, admin)
--   Sample promotion campaigns
-- =====================================================

USE ipos_pu;

-- Clear existing data (safe for dev resets)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE campaign_tracking;
TRUNCATE TABLE campaign_products;
TRUNCATE TABLE promotion_campaigns;
TRUNCATE TABLE email_queue;
TRUNCATE TABLE refunds;
TRUNCATE TABLE payments;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE cart_items;
TRUNCATE TABLE products;
TRUNCATE TABLE commercial_applications;
TRUNCATE TABLE members;
SET FOREIGN_KEY_CHECKS = 1;

-- ─── Members ─────────────────────────────────────────────────────────────────
-- Passwords are bcrypt hashes. Plain-text equivalents shown in comments (dev only).

INSERT INTO members (full_name, email, password_hash, member_type, membership_status, company_name, order_count, is_first_login, created_at) VALUES

-- Admin account — password: Admin1234!
('Admin User',
 'admin@infopharma.co.uk',
 '$2a$10$7EqJtq98hPqEX7fNZaFWoOe3kTiIRsxHQR.vqwv3YKEd8g0T0KNZK',
 'ADMIN', 'APPROVED', NULL, 0, FALSE, NOW()),

-- Non-commercial member — password: Test1234!  (is_first_login=FALSE for easy demo login)
('Alice Johnson',
 'alice@example.com',
 '$2a$10$7EqJtq98hPqEX7fNZaFWoOe3kTiIRsxHQR.vqwv3YKEd8g0T0KNZK',
 'NON_COMMERCIAL', 'APPROVED', NULL, 9, FALSE, NOW()),

-- Non-commercial member on 10th order (loyalty discount triggers next purchase)
('Bob Smith',
 'bob@example.com',
 '$2a$10$7EqJtq98hPqEX7fNZaFWoOe3kTiIRsxHQR.vqwv3YKEd8g0T0KNZK',
 'NON_COMMERCIAL', 'APPROVED', NULL, 10, FALSE, NOW()),

-- Commercial member — password: Test1234!
('Carol White',
 'carol@healthplus.co.uk',
 '$2a$10$7EqJtq98hPqEX7fNZaFWoOe3kTiIRsxHQR.vqwv3YKEd8g0T0KNZK',
 'COMMERCIAL', 'APPROVED', 'HealthPlus Ltd', 3, FALSE, NOW());

-- ─── Products ─────────────────────────────────────────────────────────────────
-- Local product table used for catalogue display when ipos_ca is unavailable.
-- Primary catalogue source is ipos_ca.ca_stock_items via InventoryDBAdapter.

INSERT INTO products (name, description, price, stock_quantity, active, created_at) VALUES
('Paracetamol 500mg Tablets (32)',    'Standard paracetamol tablets for pain and fever relief. 32 tablets per pack.',             2.49,  150, 1, NOW()),
('Ibuprofen 400mg Tablets (24)',      'Anti-inflammatory tablets for pain, fever, and inflammation. 24 tablets per pack.',         3.99,  120, 1, NOW()),
('Aspirin 300mg Tablets (32)',        'Low-dose aspirin for pain relief and blood-thinning. 32 tablets per pack.',                 1.99,   80, 1, NOW()),
('Cetirizine 10mg Antihistamine (7)', 'Non-drowsy allergy relief tablets for hay fever and allergic rhinitis. 7 tablets.',        3.49,   60, 1, NOW()),
('Loratadine 10mg Tablets (30)',      'Non-drowsy antihistamine for allergy relief. 30 tablets per pack.',                        4.99,   75, 1, NOW()),
('Omeprazole 20mg Capsules (28)',     'Proton pump inhibitor for heartburn and acid reflux. 28 capsules per pack.',               6.99,   50, 1, NOW()),
('Vitamin C 1000mg Effervescent (20)','High-strength vitamin C effervescent tablets. Orange flavour. 20 tablets.',                4.49,  200, 1, NOW()),
('Vitamin D3 1000 IU Tablets (90)',   'Daily vitamin D3 supplement for bone and immune health. 90 tablets.',                      5.99,  180, 1, NOW()),
('Cod Liver Oil Capsules (60)',       'High-strength cod liver oil with vitamins A and D. 60 capsules.',                          7.49,   90, 1, NOW()),
('Multivitamin & Mineral Tablets (30)','Comprehensive daily multivitamin for adults. 30 tablets.',                                5.49,  110, 1, NOW()),
('Chlorphenamine 4mg Tablets (28)',   'Antihistamine for hay fever, allergies, and itchy skin. 28 tablets per pack.',             2.99,   65, 1, NOW()),
('Loperamide 2mg Capsules (12)',      'Fast-acting relief from diarrhoea. 12 capsules per pack.',                                 3.29,   55, 1, NOW()),
('Rehydration Sachets (6)',           'Oral rehydration salts for fluid and electrolyte replacement. Lemon flavour. 6 sachets.',  3.79,   45, 1, NOW()),
('Naproxen 250mg Tablets (12)',       'Anti-inflammatory pain relief for period pain and arthritis. 12 tablets.',                 4.79,   40, 1, NOW()),
('Lactulose Solution 300ml',          'Gentle laxative for constipation relief. 300ml bottle.',                                   3.59,   35, 1, NOW());

-- ─── Commercial Applications ─────────────────────────────────────────────────
-- For IPOS-SA testing: mix of PENDING, APPROVED, REJECTED statuses.
-- member_id is NULL for all — set by IPOS-PU after approval creates a member account.

INSERT INTO commercial_applications (companies_house_number, director_names, business_type, business_address, email, status, submitted_at, reviewed_at) VALUES

-- 3 PENDING — main targets for IPOS-SA to approve/reject
('12345678', 'James Hargreaves',              'Pharmacy',         '14 Mill Lane, Leeds, LS1 5DL',         'j.hargreaves@meridianpharm.co.uk',  'PENDING',  '2026-04-10 09:15:00', NULL),
('87654321', 'Priya Nair, Ravi Nair',         'Wholesale',        '7 Commerce Park, Birmingham, B12 0QR', 'procurement@nairsupplies.co.uk',    'PENDING',  '2026-04-11 14:30:00', NULL),
('11223344', 'Sophie Clarke',                 'Healthcare',       '3 Victoria Road, Manchester, M2 4BH',  'sclarke@clarkehealthcare.co.uk',    'PENDING',  '2026-04-12 08:45:00', NULL),

-- 2 APPROVED — so IPOS-SA can see previously reviewed records
('55667788', 'Thomas Reid',                   'Pharmacy',         '22 High Street, Edinburgh, EH1 1TB',   't.reid@reidpharmacy.co.uk',         'APPROVED', '2026-04-05 11:00:00', '2026-04-06 10:30:00'),
('99001122', 'Fatima Al-Hassan, Omar Hassan', 'Medical Supplies',  '9 Parkway, Bristol, BS1 4ND',          'info@hassanmedical.co.uk',          'APPROVED', '2026-04-07 16:20:00', '2026-04-08 09:00:00'),

-- 1 REJECTED — for completeness
('33445566', 'David Kowalski',                'Retail',           '50 Broad Street, Norwich, NR1 1EZ',    'd.kowalski@kowalskigoods.co.uk',    'REJECTED', '2026-04-03 13:00:00', '2026-04-04 14:15:00');

-- ─── Promotion Campaigns ─────────────────────────────────────────────────────

INSERT INTO promotion_campaigns (name, start_date, end_date, discount_percent, status, created_at) VALUES
('Spring Health Sale',  '2026-03-01', '2026-04-30', 15.00, 'ACTIVE', NOW()),
('Vitamin Week',        '2026-04-01', '2026-04-07', 20.00, 'ACTIVE', NOW());

-- Campaign products — links products to campaigns with their discount percentages
INSERT INTO campaign_products (campaign_id, product_id, discount_percent) VALUES
-- Spring Health Sale (id=1): paracetamol, ibuprofen, aspirin
(1, 1, 15.00),
(1, 2, 15.00),
(1, 3, 15.00),
-- Vitamin Week (id=2): vitamins and supplements
(2, 7, 20.00),
(2, 8, 20.00),
(2, 9, 20.00),
(2, 10, 20.00);

-- Campaign tracking — initialise counters
INSERT INTO campaign_tracking (campaign_id, product_id, click_count, items_added_count, items_purchased_count) VALUES
(1, NULL, 0, 0, 0),
(2, NULL, 0, 0, 0);
