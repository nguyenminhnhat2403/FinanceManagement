-- Chạy script này trên MySQL 8.0+ hoặc PostgreSQL
-- 1. Insert 200 dòng vào bảng users
INSERT INTO users (name, email, password, created_at, updated_at)
WITH RECURSIVE cte AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM cte WHERE n < 200
)
SELECT
    CONCAT('User ', n),
    CONCAT('user', n, '@example.com'),
    'hashed_password_123',
    NOW(),
    NOW()
FROM cte;

-- 2. Insert 200 dòng vào bảng expenses tương ứng cho 200 user trên
INSERT INTO expenses (user_id, category, amount, description, expense_date, created_at, input_method)
WITH RECURSIVE cte AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM cte WHERE n < 200
)
SELECT
    n, -- user_id từ 1 đến 200 (map với ID của users vừa tạo)
    CASE (n % 9)
        WHEN 0 THEN 'FOOD_COFFEE' WHEN 1 THEN 'TRANSPORT'
        WHEN 2 THEN 'SHOPPING'    WHEN 3 THEN 'ENTERTAINMENT'
        WHEN 4 THEN 'EDUCATION'   WHEN 5 THEN 'HOUSING'
        WHEN 6 THEN 'HEALTH'      WHEN 7 THEN 'SALARY'
        ELSE 'OTHER'
        END,
    -- Giả sử amount giao động ngẫu nhiên, ở đây hardcode vài mức giá hoặc dùng rand()
    -- Cố tình để một số record có chung một lượng amount (vd: 500.00) để test query
    CASE WHEN n % 5 = 0 THEN 500.00 ELSE ROUND(RAND() * 1000 + 10, 2) END,
    CONCAT('Expense description ', n),
    NOW(),
    NOW(),
    CASE WHEN n % 2 = 0 THEN 'MANUAL' ELSE 'VOICE' END
FROM cte;