INSERT INTO user_accounts (
    username,
    lastname,
    firstname,
    email,
    password,
    user_role,
    college,
    program,
    is_enabled,
    is_locked,
    created_at,
    updated_at,
    status,
    major
)
VALUES

    ('arjay.alvarado','ALVARADO','ARJAY','arjay.alvarado@chmsu.edu.ph','$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a','PROGRAM_CHAIR','CAS','NONE',b'1',b'0',NOW(),NOW(),'ACTIVE','NONE'),
    ('imee.perante','PERANTE','IMEE','imee.perante@chmsu.edu.ph','$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a','PROGRAM_CHAIR','COF','NONE',b'1',b'0',NOW(),NOW(),'ACTIVE','NONE'),
    ('joemarie.dormido','DORMIDO','JOE MARIE','joemarie.dormido@chmsu.edu.ph','$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a','PROGRAM_CHAIR','CCS','NONE',b'1',b'0',NOW(),NOW(),'ACTIVE','NONE'),
    ('noralyn.esona','ESONA','NORALYN','noralyn.esona@chmsu.edu.ph','$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a','PROGRAM_CHAIR','CIT','NONE',b'1',b'0',NOW(),NOW(),'ACTIVE','NONE');


UPDATE user_accounts ua
    INNER JOIN primary_faculty pf
ON LOWER(TRIM(pf.firstname)) = LOWER(TRIM(ua.firstname))
    AND LOWER(TRIM(pf.lastname)) = LOWER(TRIM(ua.lastname))
    SET ua.data_source = pf.legacy_database
WHERE pf.status = 'ACTIVE';