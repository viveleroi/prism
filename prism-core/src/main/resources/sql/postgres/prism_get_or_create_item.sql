CREATE OR REPLACE FUNCTION %prefix%get_or_create_item(materialKey VARCHAR(45))
RETURNS SMALLINT AS $$
DECLARE
    itemId SMALLINT;
BEGIN
    SELECT item_id INTO itemId FROM
    %prefix%items WHERE material = materialKey;

    IF itemId IS NULL THEN
        INSERT INTO %prefix%items (material)
        VALUES (materialKey) RETURNING item_id INTO itemId;
    END IF;

    RETURN itemId;
END;
$$ LANGUAGE plpgsql;