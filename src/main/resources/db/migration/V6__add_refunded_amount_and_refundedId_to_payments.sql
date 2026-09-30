ALTER TABLE payments
      ADD COLUMN refunded_amount NUMERIC(12, 2),
      ADD COLUMN refunded_id VARCHAR(255);
