CREATE PROCEDURE %prefix%create_activity (
    IN `timestamp` INT,
    IN `x` INT,
    IN `y` INT,
    IN `z` INT,
    IN `action` VARCHAR(25),
    IN `cause` VARCHAR(25),
    IN `player` VARCHAR(32),
    IN `playerUuid` CHAR(36),
    IN `entityType` VARCHAR(25),
    IN `material` VARCHAR(45),
    IN `blockData` VARCHAR(155),
    IN `oldMaterial` VARCHAR(45),
    IN `oldBlockData` VARCHAR(155),
    IN `world` VARCHAR(255),
    IN `worldUuid` CHAR(36),
    IN `serializerVersion` SMALLINT,
    IN `serializedData` TEXT,
    IN `descriptor` VARCHAR(255),
    IN `metadata` VARCHAR(255)
)
BEGIN
    SET @entityId = NULL;
    SET @materialId = NULL;
    SET @oldMaterialId = NULL;
    SET @playerId = NULL;
    CALL %prefix%get_or_create_action(`action`, @actionId);
    IF `playerUuid` IS NOT NULL THEN
        CALL %prefix%get_or_create_player(`player`, `playerUuid`, @playerId);
    END IF;
    CALL %prefix%get_or_create_cause(`cause`, @playerId, @causeId);
    CALL %prefix%get_or_create_world(`world`, `worldUuid`, @worldId);
    IF `entityType` IS NOT NULL THEN
        CALL %prefix%get_or_create_entity_type(entityType, @entityId);
    END IF;
    IF `material` IS NOT NULL THEN
        CALL %prefix%get_or_create_material(material, blockData, @materialId);
    END IF;
    IF `oldMaterial` IS NOT NULL THEN
        CALL %prefix%get_or_create_material(oldMaterial, oldBlockData, @oldMaterialId);
    END IF;
    INSERT INTO `%prefix%activities`
    (`timestamp`, `world_id`, `x`, `y`, `z`, `action_id`, `material_id`,
    `old_material_id`, `entity_type_id`, `cause_id`, `descriptor`, `metadata`, `serializer_version`, `serialized_data`)
    VALUES
    (`timestamp`, @worldId, `x`, `y`, `z`, @actionId, @materialId,
    @oldMaterialId, @entityId, @causeId, `descriptor`, `metadata`,
     `serializerVersion`, `serializedData`);
    SET @activityId = LAST_INSERT_ID();
END