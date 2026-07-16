-- Roles table
CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(50) UNIQUE NOT NULL,
                       description TEXT
);

-- Accounts table
CREATE TABLE accounts (
                          id SERIAL PRIMARY KEY,
                          account_code VARCHAR(50) UNIQUE NOT NULL,
                          role_id INT NOT NULL REFERENCES roles(id),
                          is_active BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Positions table
CREATE TABLE positions (
                           id SERIAL PRIMARY KEY,
                           name VARCHAR(100) UNIQUE NOT NULL,
                           description TEXT
);

-- Account Statuses table
CREATE TABLE account_statuses (
                                  id SERIAL PRIMARY KEY,
                                  status_name VARCHAR(50) UNIQUE NOT NULL,
                                  description TEXT
);

-- Extend Accounts table to reference positions and statuses
ALTER TABLE accounts
    ADD COLUMN position_id INT REFERENCES positions(id),
    ADD COLUMN status_id INT REFERENCES account_statuses(id);

-- Account Work History table
CREATE TABLE account_work_history (
                                      id SERIAL PRIMARY KEY,
                                      account_id INT NOT NULL REFERENCES accounts(id),
                                      start_date DATE NOT NULL,
                                      end_date DATE,
                                      responsibilities TEXT,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
