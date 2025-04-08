CREATE OR REPLACE FUNCTION %prefix%create_activity (
    p_timestamp BIGINT,
    p_x INTEGER,
    p_y INTEGER,
    p_z INTEGER,
    p_action VARCHAR(25),
    p_cause VARCHAR(25),
    p_player VARCHAR(16),
    p_playerUuid CHAR(36),
    p_entityType VARCHAR(25),
    p_material VARCHAR(45),
    p_blockData VARCHAR(155),
    p_oldMaterial VARCHAR(45),
    p_oldBlockData VARCHAR(155),
    p_world VARCHAR(255),
    p_worldUuid CHAR(36),
    p_customDataVersion INTEGER,
    p_customData TEXT,
    p_descriptor VARCHAR(255),
    p_metadata VARCHAR(255)
)
RETURNS VOID AS $$
DECLARE
    v_actionId SMALLINT;
    v_causeId INTEGER;
    v_worldId SMALLINT;
    v_playerId INTEGER;
    v_entityTypeId SMALLINT;
    v_materialId SMALLINT;
    v_oldMaterialId SMALLINT;
    v_activityId INTEGER;
BEGIN
    -- Get or create action
    SELECT %prefix%get_or_create_action(p_action) INTO v_actionId;

    -- Get or create player
    IF p_playerUuid IS NOT NULL THEN
        SELECT %prefix%get_or_create_player(p_player, p_playerUuid) INTO v_playerId;
    END IF;

    -- Get or create cause
    SELECT %prefix%get_or_create_cause(p_cause, v_playerId) INTO v_causeId;

    -- Get or create world
    SELECT %prefix%get_or_create_world(p_world, p_worldUuid) INTO v_worldId;

    -- Get or create entity type
    IF p_entityType IS NOT NULL THEN
        SELECT %prefix%get_or_create_entity_type(p_entityType) INTO v_entityTypeId;
    END IF;

    -- Get or create material
    IF p_material IS NOT NULL THEN
        SELECT %prefix%get_or_create_material(p_material, p_blockData) INTO v_materialId;
    END IF;

    -- Get or create old material
    IF p_oldMaterial IS NOT NULL THEN
        SELECT %prefix%get_or_create_material(p_oldMaterial, p_oldBlockData) INTO v_oldMaterialId;
    END IF;

    -- Insert into activities table
    INSERT INTO %prefix%activities
        ("timestamp", world_id, x, y, z, action_id, material_id,
         old_material_id, entity_type_id, cause_id, descriptor, metadata)
    VALUES
        (p_timestamp, v_worldId, p_x, p_y, p_z, v_actionId, v_materialId,
         v_oldMaterialId, v_entityTypeId, v_causeId, p_descriptor, p_metadata)
    RETURNING activity_id INTO v_activityId;

    -- Insert into custom data table
    IF p_customData IS NOT NULL THEN
        INSERT INTO %prefix%activities_custom_data (activity_id, version, data)
        VALUES (v_activityId, p_customDataVersion, p_customData);
    END IF;
END;
$$ LANGUAGE plpgsql;