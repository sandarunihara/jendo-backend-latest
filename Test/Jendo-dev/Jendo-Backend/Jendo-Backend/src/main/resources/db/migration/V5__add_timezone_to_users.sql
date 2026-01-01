-- Add timezone column to users table
ALTER TABLE users ADD COLUMN timezone VARCHAR(50) DEFAULT 'Asia/Colombo';

-- Update existing users to have Sri Lanka timezone
UPDATE users SET timezone = 'Asia/Colombo' WHERE timezone IS NULL;
