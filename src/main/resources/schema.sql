CREATE TABLE IF NOT EXISTS account (
                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       username TEXT NOT NULL UNIQUE,
                                       role TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS project (
                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       name TEXT NOT NULL,
                                       created_date TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS issue (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     project_id INTEGER NOT NULL,
                                     title TEXT NOT NULL,
                                     description TEXT,
                                     status TEXT NOT NULL,
                                     priority TEXT NOT NULL,
                                     reporter_id INTEGER NOT NULL,
                                     assignee_id INTEGER,
                                     fixer_id INTEGER,
                                     reported_date TEXT NOT NULL,

                                     FOREIGN KEY(project_id) REFERENCES project(id),
    FOREIGN KEY(reporter_id) REFERENCES account(id),
    FOREIGN KEY(assignee_id) REFERENCES account(id),
    FOREIGN KEY(fixer_id) REFERENCES account(id)
    );

CREATE TABLE IF NOT EXISTS comment (
                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       issue_id INTEGER NOT NULL,
                                       author_id INTEGER NOT NULL,
                                       content TEXT NOT NULL,
                                       created_date TEXT NOT NULL,

                                       FOREIGN KEY(issue_id) REFERENCES issue(id),
    FOREIGN KEY(author_id) REFERENCES account(id)
    );