ALTER TABLE user_accounts
ADD COLUMN firstname VARCHAR(100) NULL AFTER email;

ALTER TABLE user_accounts
ADD COLUMN lastname VARCHAR(100) NULL AFTER username;

ALTER TABLE user_accounts
MODIFY COLUMN college ENUM(
    'CAS',
    'CIT',
    'COED',
    'COENG',
    'CCJ',
    'CCS',
    'COF',
    'CBMA',
    'FOR_MIGRATION'
);

INSERT INTO user_accounts (
    username,
    firstname,
    email,
    lastname,
    password,
    user_role,
    college,
    program,
    is_enabled,
    is_locked,
    last_login_at,
    created_at,
    updated_at,
    reset_token,
    reset_token_expiry,
    status
) VALUES
('ga.feljane', 'Feljane', 'feljane.ga@chmsu.edu.ph', 'Ga', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'CCJ', 'BSCRIM', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('teresa.ballados', 'Ma. Teresa', 'teresa.ballados@chmsu.edu.ph', 'Ballados', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'CBMA', 'MBA', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('carnit.cordova', 'Carnit George', 'carnit.cordova@chmsu.edu.ph', 'Cordova', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'CCS', 'BSIS', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('marilou.jonota', 'Marilou', 'marilou.jonota@chmsu.edu.ph', 'Jonota', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'CBMA', 'BSBA', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('kenrose.laguyo', 'Kenrose', 'kenrose.laguyo@chmsu.edu.ph', 'Laguyo', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'CBMA', 'BSMA', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('jonah.perulino', 'Jonah', 'jonah.perulino@chmsu.edu.ph', 'Perulino', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'CBMA', 'BSE', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('joyfe.quingco', 'Joyfe', 'joyfe.quingco@chmsu.edu.ph', 'Quingco', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'CBMA', 'BSOA', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('geofrey.rivera', 'Geofrey', 'geofrey.rivera@chmsu.edu.ph', 'Rivera', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'CBMA', 'BSA', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('jorie.benedicto', 'Jorie', 'jorie.benedicto@chmsu.edu.ph', 'Benedicto', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'CIT', 'BSIND', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('nathaniel.jamandron', 'Nathaniel', 'njamandron@chmsu.edu.ph', 'Jamandron', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'CIT', 'BIT', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('ruel.malapitan', 'Ruel', 'ruel.malapitan@chmsu.edu.ph', 'Malapitan', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'COENG', 'BSECE', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('marites.manganti', 'Marites', 'marites.manganti@chmsu.edu.ph', 'Manganti', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'CCS', 'BSIT', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('roldan.raymundo', 'Roldan', 'roldan.raymundo@chmsu.edu.ph', 'Raymundo', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'CCS', 'BSIS', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('alberto.delacruz', 'Alberto', 'alberto.delacruz@chmsu.edu.ph', 'De la cruz', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'COED', 'BSED', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('gailyreyapril.guzon', 'Gaily Rey April', 'gailyreyapril.guzon@chmsu.edu.ph', 'Guzon', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'CCS', 'BSIT', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('mary.mabasa', 'Mary Luna', 'mary.mabasa@chmsu.edu.ph', 'Mabasa', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'COED', 'BEED', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('kervin.mahinay', 'Kervin', 'kervin.mahinay@chmsu.edu.ph', 'Mahinay', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'COED', 'BSFI', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('regina.manoso', 'Regina', 'regina.manoso@chmsu.edu.ph', 'Manoso', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'COED', 'BTLED', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('preciousjane.canal', 'Precious Jane', 'preciousjane.canal@chmsu.edu.ph', 'Canal', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'CAS', 'BPA', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('jose.lampinez', 'Jose Lizael', 'jose.lampinez@chmsu.edu.ph', 'Lampinez', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'CAS', 'MPA', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('joy.picar', 'Joy', 'joy.picar@chmsu.edu.ph', 'Picar', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'CAS', 'BSAM', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('ruth.nemenzo', 'Ruth', 'ruth.nemenzo@chmsu.edu.ph', 'Nemenzo', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'CAS', 'BAEL', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('robert.pardillo', 'Robert', 'robert.pardillo@chmsu.edu.ph', 'Pardillo', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'CAS', 'BASS', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE'),
('riza.deocadez', 'Riza', 'riza.deocadez@chmsu.edu.ph', 'Deocadez', '$2a$12$5sofiiPN.AraWznt9z1AJuGvniqEC.1pewOFI3LTD.BmfJV0iFm0a', 'PROGRAM_CHAIR', 'CAS', 'BS PSYCH', b'1', b'0', NULL, NOW(6), NOW(6), NULL, NULL, 'ACTIVE');
