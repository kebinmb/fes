-- ============================================
-- TEST DATA FOR user_accounts
-- ============================================

INSERT INTO user_accounts (
    user_id,
    username,
    email,
    password,
    user_role,
    college,
    is_enabled,
    is_locked,
    last_login_at,
    created_at,
    updated_at,
    reset_token,
    reset_token_expiry
) VALUES
-- 🔐 SUPERVISOR ACCOUNT (PASSWORD: admin123)
(
    1,
    'admin',
    'admin@university.edu',
    '$2a$12$6M6XeFaBG6k4eeGcHC5Y1e.r0NnKMzzoe6OIRXM3F2VJ.NYKIJ7CC', -- BCrypt for "admin123"
    'DEAN',
    'CCS',
    b'1',
    b'0',
    NULL,
    NOW(),
    NOW(),
    NULL,
    NULL
),

-- 👨‍🏫 FACULTY USER (PASSWORD: faculty123)
(
    2,
    'faculty1',
    'faculty1@university.edu',
    '$2a$12$6M6XeFaBG6k4eeGcHC5Y1e.r0NnKMzzoe6OIRXM3F2VJ.NYKIJ7CC', -- BCrypt
    'DEAN',
    'CAS',
    b'1',
    b'0',
    NULL,
    NOW(),
    NOW(),
    NULL,
    NULL
),

-- 🔒 LOCKED USER
(
    3,
    'locked_user',
    'locked@university.edu',
    '$2a$12$6M6XeFaBG6k4eeGcHC5Y1e.r0NnKMzzoe6OIRXM3F2VJ.NYKIJ7CC',
    'FACULTY',
    'CIT',
    b'1',
    b'1',
    NULL,
    NOW(),
    NOW(),
    NULL,
    NULL
),

-- ❌ DISABLED USER
(
    4,
    'disabled_user',
    'disabled@university.edu',
    '$2a$12$6M6XeFaBG6k4eeGcHC5Y1e.r0NnKMzzoe6OIRXM3F2VJ.NYKIJ7CC',
    'FACULTY',
    'CAS',
    b'0',
    b'0',
    NULL,
    NOW(),
    NOW(),
    NULL,
    NULL
);