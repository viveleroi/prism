CREATE OR REPLACE FUNCTION %prefix%create_activity (
    p_timestamp BIGINT,
    p_x INTEGER,
    p_y INTEGER,
    p_z INTEGER,
    p_action VARCHAR(25),
    p_cause VARCHAR(25),
    p_player VARCHAR(32),
    p_playerUuid CHAR(36),
    p_entityType VARCHAR(25),
    p_material VARCHAR(45),
    p_blockNamespace VARCHAR(55),
    p_blockName VARCHAR(55),
    p_blockData VARCHAR(255),
    p_replacedBlockNamespace VARCHAR(55),
    p_replacedBlockName VARCHAR(55),
    p_replacedBlockData VARCHAR(255),
    p_world VARCHAR(255),
    p_worldUuid CHAR(36),
    p_serializerVersion INTEGER,
    p_serializedData TEXT,
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
    v_blockId INTEGER;
    v_replacedBlockId INTEGER;
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
        SELECT %prefix%get_or_create_material(p_material) INTO v_materialId;
    END IF;

    -- Get or create block id
    IF p_blockName IS NOT NULL THEN
        SELECT %prefix%get_or_create_block(p_blockNamespace, p_blockName, p_blockData) INTO v_blockId;
    END IF;

    -- Get or create replaced block id
    IF p_replacedBlockName IS NOT NULL THEN
        SELECT %prefix%get_or_create_block(p_replacedBlockNamespace, p_replacedBlockName, p_replacedBlockData)
        INTO v_replacedBlockId;
    END IF;

    -- Insert into activities table
    INSERT INTO %prefix%activities
        ("timestamp", world_id, x, y, z, action_id, material_id,
         block_id, replaced_block_id, entity_type_id, cause_id, descriptor, metadata, serializer_version, serialized_data)
    VALUES
        (p_timestamp, v_worldId, p_x, p_y, p_z, v_actionId, v_materialId, v_blockId,
         v_replacedBlockId, v_entityTypeId, v_causeId, p_descriptor, p_metadata, p_serializerVersion, p_serializedData)
    RETURNING activity_id INTO v_activityId;
END;
$$ LANGUAGE plpgsql;