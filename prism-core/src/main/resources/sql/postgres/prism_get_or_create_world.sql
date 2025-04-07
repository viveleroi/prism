CREATE OR REPLACE FUNCTION prism_get_or_create_world(worldName VARCHAR(255), uuid VARCHAR(55))
RETURNS SMALLINT AS $$
DECLARE
    worldId SMALLINT;
BEGIN
    SELECT world_id INTO worldId FROM %prefix%_worlds WHERE world_uuid = uuid;

    IF worldId IS NULL THEN
        INSERT INTO %prefix%_worlds (world, world_uuid) VALUES (worldName, uuid) RETURNING world_id INTO worldId;
    END IF;

    RETURN worldId;
END;
$$ LANGUAGE plpgsql;