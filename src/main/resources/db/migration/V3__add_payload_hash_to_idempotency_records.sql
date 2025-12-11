ALTER TABLE idempotency_records 
ADD COLUMN payload_hash TEXT NOT NULL DEFAULT 'legacy_record';
