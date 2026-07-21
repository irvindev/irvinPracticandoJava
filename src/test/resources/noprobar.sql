-- Datos de tbl_menu
INSERT INTO tbl_menu (name, description, image_url, type, properties)
VALUES
    ('Breakfast Menu', 'Morning breakfast options', 'https://example.com/breakfast.jpg', 'breakfast', '{"vegetarian": true, "spicy": false}'),
    ('Lunch Menu', 'Delicious lunch meals', 'https://example.com/lunch.jpg', 'lunch', '{"vegetarian": false, "spicy": true}');

-- Datos de tbl_user
INSERT INTO tbl_user (id, username, credentials_expired, disabled,expired,locked,password,registration_completed,phone_number)
VALUES
    ('user001', 'John Doe', 0,0,0,0,'password123',0,'51912775356'),
    ('user002', 'Jane Smith', 0,0,0,0,'password456',0,'51912775356');

-- Datos de tbl_user_delivery_counts
INSERT INTO tbl_user_delivery_counts (delivery_user_id, orders_asigned_count)
VALUES
    ('user001', 10),
    ('user002', 5);

-- Datos de tbl_menu_calendar
INSERT INTO tbl_menu_calendar (calendar_date, menu_list)
VALUES
    ('2024-12-01', '[{"id": 1, "name": "Breakfast Menu"}, {"id": 2, "name": "Lunch Menu"}]'),
    ('2024-12-02', '[{"id": 2, "name": "Lunch Menu"}]');

-- Datos de tbl_user_profile
INSERT INTO tbl_profile (user_id, name, lastname, born_date, district, address, description, information)
VALUES
    ('user001', 'John', 'Doe', '1985-05-15', 'Lima', '123 Main St', 'Regular customer', '{"preferences": ["vegan", "non-spicy"]}'),
    ('user002', 'Jane', 'Smith', '1990-07-20', 'Cusco', '456 Side St', 'VIP customer', '{"preferences": ["spicy", "desserts"]}');

-- Datos de tbl_delivery_point
INSERT INTO tbl_delivery_point (user_id, address, description, location)
VALUES
    ('user001', '123 Main St', 'Home address', ST_PointFromText('POINT(-77.042754 38.907192)')),
    ('user002', '456 Side St', 'Work address', ST_PointFromText('POINT(-77.033738 38.89511)'));

-- Datos de tbl_invoice_address
INSERT INTO tbl_invoice_address (address, description)
VALUES
    ('123 Main St', 'Billing address for John'),
    ('456 Side St', 'Billing address for Jane');

-- Datos de tbl_invoice
INSERT INTO tbl_invoice (id, description, emission_date, status, total_price, address_id)
VALUES
    ('inv001', 'Invoice for breakfast order', '2024-12-01', 'P', 15.00, 1),
    ('inv002', 'Invoice for lunch order', '2024-12-02', 'P', 20.00, 2);

-- Datos de tbl_order
INSERT INTO tbl_order (id, user_id, menu_type_items, delivery_point_id, delivery_user_id, create_date, delivery_date, closing_date, status)
VALUES
    (1, 'user001', '[1]', '123 Main St', 'user002', '2024-12-01 08:00:00', '2024-12-01 09:00:00', NULL, 'P'),
    (2, 'user002', '[2]', '456 Side St', 'user001', '2024-12-02 12:00:00', '2024-12-02 13:00:00', NULL, 'P');

-- Datos de tbl_invoice_orders
INSERT INTO tbl_invoice_orders (invoice_id, order_id)
VALUES
    ('inv001', 1),
    ('inv002', 2);

-- Datos de tbl_subscription_plan
INSERT INTO tbl_subscription_plan (description, real_price, previous_price, level, available)
VALUES
    ('Basic Plan', 10.00, 12.00, 'basic', true),
    ('Premium Plan', 20.00, 25.00, 'premium', true);

-- Datos de tbl_benefits
INSERT INTO tbl_benefits (benefits_period, detail, assigned_plan, subscrption_plan_id)
VALUES
    (30, '{["name": "lunch", "value": 20]}', true, 1),
    (60, '{["name": "dinner", "value": 21]}', true, 2);

-- Datos de tbl_plan_user
INSERT INTO tbl_plan_user (user_id, benefits_id, plan_init_date, plan_expiration_date, benefits_consumption, need_day, objectives_registration, summary_week)
VALUES
    ('user001', 1, '2024-12-01', '2024-12-31', '{"used": 5}', '{"required": ["weekend discounts"]}', '{"goals": ["use all benefits"]}', '{"week_1": "active"}'),
    ('user002', 2, '2024-12-01', '2025-01-31', '{"used": 10}', '{"required": ["holiday discounts"]}', '{"goals": ["reach premium level"]}', '{"week_1": "active"}');

-- Datos de tbl_recommendations
INSERT INTO tbl_recommendations (user_id, recommended_plan_id)
VALUES
('user001', 2),
    ('user002', 1);
