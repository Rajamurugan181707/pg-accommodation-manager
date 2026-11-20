-- =============================================================
--  PG Accommodation Management System  -  MySQL schema
--  II CSE F  |  A. Raja Murugan  |  Mini project (DS + OOPS)
--
--  Run once:   mysql -u root -p < sql/schema.sql
--  WARNING: this re-creates the tables, so any data in them is lost.
-- =============================================================

CREATE DATABASE IF NOT EXISTS pg_accommodation;
USE pg_accommodation;

DROP TABLE IF EXISTS rent_payments;
DROP TABLE IF EXISTS monthly_bills;
DROP TABLE IF EXISTS tenants;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS mess_plans;
DROP TABLE IF EXISTS mess_menu;

-- ---------- ROOM module ----------
CREATE TABLE rooms (
    room_id      INT AUTO_INCREMENT PRIMARY KEY,
    room_no      VARCHAR(10)   NOT NULL UNIQUE,
    floor_no     INT           NOT NULL,
    room_type    ENUM('SINGLE','DOUBLE','TRIPLE') NOT NULL,
    capacity     INT           NOT NULL,
    occupied     INT           NOT NULL DEFAULT 0,
    rent_per_bed DECIMAL(10,2) NOT NULL,
    has_ac       BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_occupied CHECK (occupied >= 0 AND occupied <= capacity)
);

-- ---------- MESS module ----------
CREATE TABLE mess_plans (
    plan_id        INT AUTO_INCREMENT PRIMARY KEY,
    plan_name      VARCHAR(40)   NOT NULL UNIQUE,
    meals_per_day  INT           NOT NULL,
    monthly_charge DECIMAL(10,2) NOT NULL
);

CREATE TABLE mess_menu (
    day_of_week ENUM('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') PRIMARY KEY,
    breakfast   VARCHAR(80) NOT NULL,
    lunch       VARCHAR(80) NOT NULL,
    dinner      VARCHAR(80) NOT NULL
);

-- ---------- TENANT module ----------
CREATE TABLE tenants (
    tenant_id    INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(60)   NOT NULL,
    phone        VARCHAR(15)   NOT NULL,      -- unique among ACTIVE tenants (checked in the app)
    email        VARCHAR(80),
    gender       ENUM('MALE','FEMALE','OTHER') NOT NULL,
    occupation   VARCHAR(40),
    room_id      INT           NOT NULL,
    mess_plan_id INT,                         -- NULL = not in mess
    join_date    DATE          NOT NULL,
    leave_date   DATE,
    deposit      DECIMAL(10,2) NOT NULL DEFAULT 0,
    status       ENUM('ACTIVE','LEFT') NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_tenant_room FOREIGN KEY (room_id)      REFERENCES rooms(room_id),
    CONSTRAINT fk_tenant_plan FOREIGN KEY (mess_plan_id) REFERENCES mess_plans(plan_id)
);
CREATE INDEX idx_tenant_phone ON tenants (phone);

-- ---------- RENT module ----------
-- One bill per tenant per month. It is made the first time that month is
-- looked at, from the tenant's room rent and mess plan at that moment, and
-- never changes afterwards - so a later rent change, room shift or mess
-- plan change does not rewrite old bills.
CREATE TABLE monthly_bills (
    tenant_id   INT           NOT NULL,
    bill_month  CHAR(7)       NOT NULL,       -- 'YYYY-MM'
    room_label  VARCHAR(60)   NOT NULL,
    room_rent   DECIMAL(10,2) NOT NULL,
    mess_label  VARCHAR(60),                  -- NULL = not in mess that month
    mess_charge DECIMAL(10,2) NOT NULL DEFAULT 0,
    created_on  DATE          NOT NULL,
    PRIMARY KEY (tenant_id, bill_month),
    CONSTRAINT fk_bill_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id)
);

CREATE TABLE rent_payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    tenant_id  INT           NOT NULL,
    bill_month CHAR(7)       NOT NULL,        -- 'YYYY-MM'
    amount     DECIMAL(10,2) NOT NULL,
    paid_on    DATE          NOT NULL,
    mode       ENUM('CASH','UPI','CARD','BANK') NOT NULL,
    note       VARCHAR(80),
    CONSTRAINT fk_payment_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id),
    CONSTRAINT chk_amount CHECK (amount > 0)
);
CREATE INDEX idx_payment_month ON rent_payments (bill_month);

-- =============================================================
--  Sample data (so the demo has something to show)
-- =============================================================
INSERT INTO rooms (room_no, floor_no, room_type, capacity, rent_per_bed, has_ac) VALUES
('G01', 0, 'SINGLE', 1,  9000, FALSE),
('G02', 0, 'DOUBLE', 2,  6500, FALSE),
('G03', 0, 'TRIPLE', 3,  5000, FALSE),
('101', 1, 'SINGLE', 1, 11000, TRUE),
('102', 1, 'DOUBLE', 2,  7500, TRUE),
('103', 1, 'TRIPLE', 3,  5500, FALSE),
('201', 2, 'DOUBLE', 2,  7500, TRUE),
('202', 2, 'TRIPLE', 3,  6000, TRUE);

INSERT INTO mess_plans (plan_name, meals_per_day, monthly_charge) VALUES
('Breakfast only', 1, 1500),
('Two meals',      2, 2800),
('Full board',     3, 3800);

INSERT INTO mess_menu VALUES
('MONDAY',    'Idli, sambar, chutney',  'Rice, sambar, poriyal, curd', 'Chapati, kurma'),
('TUESDAY',   'Pongal, vada',           'Rice, rasam, kootu, curd',    'Dosa, chutney'),
('WEDNESDAY', 'Poori, masala',          'Veg biryani, raita',          'Idiyappam, kurma'),
('THURSDAY',  'Upma, chutney',          'Rice, kara kuzhambu, curd',   'Parotta, salna'),
('FRIDAY',    'Dosa, sambar',           'Rice, sambar, appalam, curd', 'Chapati, dal'),
('SATURDAY',  'Rava kichadi',           'Lemon rice, curd rice',       'Uthappam, chutney'),
('SUNDAY',    'Masala dosa',            'Chicken / paneer biryani',    'Light dinner: idli');

INSERT INTO tenants (name, phone, email, gender, occupation, room_id, mess_plan_id, join_date, deposit) VALUES
('Arun Kumar',   '9840011111', 'arun@example.com',   'MALE', 'Software engineer', 4, 3, '2026-06-01', 22000),
('Karthik S',    '9840022222', 'karthik@example.com','MALE', 'Student',           2, 2, '2026-07-05', 13000),
('Vignesh R',    '9840033333', NULL,                 'MALE', 'Student',           2, 3, '2026-07-05', 13000),
('Suresh Babu',  '9840044444', 'suresh@example.com', 'MALE', 'Bank clerk',        3, NULL, '2026-08-10', 10000),
('Pradeep M',    '9840055555', NULL,                 'MALE', 'Student',           3, 1, '2026-09-01', 10000),
('Dinesh Raj',   '9840066666', 'dinesh@example.com', 'MALE', 'Sales executive',   5, 2, '2026-10-01', 15000);

-- keep the room occupancy in step with the tenants above
UPDATE rooms r
SET occupied = (SELECT COUNT(*) FROM tenants t WHERE t.room_id = r.room_id AND t.status = 'ACTIVE');

-- Every month up to October fully paid; November has one full payment,
-- one part payment, and the rest still pending.
INSERT INTO rent_payments (tenant_id, bill_month, amount, paid_on, mode, note) VALUES
(1, '2026-06', 14800, '2026-06-01', 'UPI',  'first month'),
(1, '2026-07', 14800, '2026-07-02', 'UPI',  NULL),
(1, '2026-08', 14800, '2026-08-03', 'UPI',  NULL),
(1, '2026-09', 14800, '2026-09-02', 'UPI',  NULL),
(2, '2026-07',  9300, '2026-07-05', 'CASH', 'first month'),
(2, '2026-08',  9300, '2026-08-06', 'CASH', NULL),
(2, '2026-09',  9300, '2026-09-05', 'CASH', NULL),
(3, '2026-07', 10300, '2026-07-05', 'UPI',  'first month'),
(3, '2026-08', 10300, '2026-08-05', 'UPI',  NULL),
(3, '2026-09', 10300, '2026-09-04', 'UPI',  NULL),
(4, '2026-08',  5000, '2026-08-10', 'BANK', 'first month'),
(4, '2026-09',  5000, '2026-09-08', 'BANK', NULL),
(5, '2026-09',  6500, '2026-09-01', 'UPI',  'first month'),
(1, '2026-10', 14800, '2026-10-03', 'UPI',  NULL),
(2, '2026-10',  9300, '2026-10-05', 'CASH', NULL),
(3, '2026-10', 10300, '2026-10-05', 'UPI',  NULL),
(4, '2026-10',  5000, '2026-10-07', 'BANK', NULL),
(5, '2026-10',  6500, '2026-10-02', 'UPI',  NULL),
(6, '2026-10', 10300, '2026-10-01', 'CARD', 'first month'),
(1, '2026-11', 14800, '2026-11-02', 'UPI',  NULL),
(2, '2026-11',  5000, '2026-11-06', 'CASH', 'balance next week');
