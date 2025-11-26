-- Add address fields and timestamps to firms table
ALTER TABLE firms
    ADD COLUMN address_street VARCHAR(255),
    ADD COLUMN address_city VARCHAR(100),
    ADD COLUMN address_state VARCHAR(100),
    ADD COLUMN address_postal_code VARCHAR(20),
    ADD COLUMN address_country VARCHAR(100),
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

-- Update existing records to have default address values
UPDATE firms
SET address_street = 'Unknown',
    address_city = 'Unknown',
    address_state = 'Unknown',
    address_postal_code = 'Unknown',
    address_country = 'Unknown'
WHERE address_street IS NULL;

-- Make address fields NOT NULL after setting defaults
ALTER TABLE firms
    ALTER COLUMN address_street SET NOT NULL,
    ALTER COLUMN address_city SET NOT NULL,
    ALTER COLUMN address_state SET NOT NULL,
    ALTER COLUMN address_postal_code SET NOT NULL,
    ALTER COLUMN address_country SET NOT NULL;

-- Create index on firm name for faster lookups
CREATE INDEX idx_firms_name ON firms (name);

-- Add comment to table
COMMENT ON TABLE firms IS 'Stores firm (tenant) information including identity, contact details, and audit timestamps';

-- Add comments to columns
COMMENT ON COLUMN firms.id IS 'Unique firm identifier (UUID)';
COMMENT ON COLUMN firms.name IS 'Firm legal or operating name';
COMMENT ON COLUMN firms.address_street IS 'Street address line';
COMMENT ON COLUMN firms.address_city IS 'City name';
COMMENT ON COLUMN firms.address_state IS 'State, province, or region';
COMMENT ON COLUMN firms.address_postal_code IS 'Postal or ZIP code';
COMMENT ON COLUMN firms.address_country IS 'Country name';
COMMENT ON COLUMN firms.created_at IS 'Timestamp when the firm was created';
COMMENT ON COLUMN firms.updated_at IS 'Timestamp when the firm was last updated';
