INSERT OR IGNORE INTO account(username, role) VALUES ('admin', 'ADMIN');

INSERT OR IGNORE INTO account(username, role) VALUES ('PL1', 'PL');
INSERT OR IGNORE INTO account(username, role) VALUES ('PL2', 'PL');

INSERT OR IGNORE INTO account(username, role) VALUES ('dev1', 'DEV');
INSERT OR IGNORE INTO account(username, role) VALUES ('dev2', 'DEV');
INSERT OR IGNORE INTO account(username, role) VALUES ('dev3', 'DEV');
INSERT OR IGNORE INTO account(username, role) VALUES ('dev4', 'DEV');
INSERT OR IGNORE INTO account(username, role) VALUES ('dev5', 'DEV');
INSERT OR IGNORE INTO account(username, role) VALUES ('dev6', 'DEV');
INSERT OR IGNORE INTO account(username, role) VALUES ('dev7', 'DEV');
INSERT OR IGNORE INTO account(username, role) VALUES ('dev8', 'DEV');
INSERT OR IGNORE INTO account(username, role) VALUES ('dev9', 'DEV');
INSERT OR IGNORE INTO account(username, role) VALUES ('dev10', 'DEV');

INSERT OR IGNORE INTO account(username, role) VALUES ('tester1', 'TESTER');
INSERT OR IGNORE INTO account(username, role) VALUES ('tester2', 'TESTER');
INSERT OR IGNORE INTO account(username, role) VALUES ('tester3', 'TESTER');
INSERT OR IGNORE INTO account(username, role) VALUES ('tester4', 'TESTER');
INSERT OR IGNORE INTO account(username, role) VALUES ('tester5', 'TESTER');

INSERT OR IGNORE INTO project(name, created_date)
VALUES ('project1', datetime('now'));