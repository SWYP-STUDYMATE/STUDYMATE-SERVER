-- STUDYMATE Database Initialization Script
-- This script ensures the database and user are properly set up

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS studymate 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

-- Use the database
USE studymate;

-- Grant privileges to the studymate user
GRANT ALL PRIVILEGES ON studymate.* TO 'studymate'@'%';
GRANT ALL PRIVILEGES ON studymate.* TO 'studymate'@'localhost';

-- Flush privileges to ensure they take effect
FLUSH PRIVILEGES;

-- Create a test table to verify connectivity (will be dropped by Hibernate if ddl-auto=create-drop)
CREATE TABLE IF NOT EXISTS connection_test (
    id INT AUTO_INCREMENT PRIMARY KEY,
    test_message VARCHAR(100) DEFAULT 'Database connected successfully',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert a test record
INSERT INTO connection_test (test_message) VALUES ('Database initialization completed');

SELECT 'Database initialization completed successfully!' as status;