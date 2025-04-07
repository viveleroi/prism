CREATE OR REPLACE FUNCTION prism_get_or_create_entity_type(entityType VARCHAR(25))
RETURNS INTEGER AS $$
DECLARE
    entityTypeId INTEGER;
BEGIN
    SELECT entity_type_id INTO entityTypeId FROM %prefix%entity_types WHERE entity_type = entityType;

    IF entityTypeId IS NULL THEN
        INSERT INTO %prefix%entity_types (entity_type) VALUES (entityType) RETURNING entity_type_id INTO entityTypeId;
    END IF;

    RETURN entityTypeId;
END;
$$ LANGUAGE plpgsql;