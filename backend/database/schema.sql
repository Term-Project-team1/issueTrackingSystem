CREATE TABLE IF NOT EXISTS accounts (
                                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                                        username TEXT NOT NULL,
                                        role TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS projects (
                                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                                        name TEXT NOT NULL,
                                        created_date TEXT
);

CREATE TABLE IF NOT EXISTS issues (
                                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                                      title TEXT NOT NULL,
                                      description TEXT,
                                      status TEXT,
                                      priority TEXT,
                                      reporter_id INTEGER,
                                      assignee_id INTEGER,
                                      fixer_id INTEGER,
                                      reported_date TEXT
);

CREATE TABLE IF NOT EXISTS comments (
                                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                                        issue_id INTEGER,
                                        author_id INTEGER,
                                        content TEXT,
                                        created_date TEXT
);