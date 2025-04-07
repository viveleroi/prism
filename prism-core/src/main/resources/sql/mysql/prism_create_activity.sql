CREATE PROCEDURE prism_create_activity (
    IN `timestamp` INT,
    IN `x` INT,
    IN `y` INT,
    IN `z` INT,
    IN `action` VARCHAR(25),
    IN `cause` VARCHAR(25),
    IN `player` VARCHAR(16),
    IN `playerUuid` CHAR(36),
    IN `entityType` VARCHAR(25),
    IN `material` VARCHAR(45),
    IN `blockData` VARCHAR(155),
    IN `oldMaterial` VARCHAR(45),
    IN `oldBlockData` VARCHAR(155),
    IN `world` VARCHAR(255),
    IN `worldUuid` CHAR(36),
    IN `customDataVersion` SMALLINT,
    IN `customData` TEXT,
    IN `descriptor` VARCHAR(255),
    IN `metadata` VARCHAR(255)
)
BEGIN
    SET @entityId = NULL;
    SET @materialId = NULL;
    SET @oldMaterialId = NULL;
    SET @playerId = NULL;
    CALL prism_get_or_create_action(`action`, @actionId);
    IF `playerUuid` IS NOT NULL THEN
        CALL prism_get_or_create_player(`player`, `playerUuid`, @playerId);
    END IF;
    CALL prism_get_or_create_cause(`cause`, @playerId, @causeId);
    CALL prism_get_or_create_world(`world`, `worldUuid`, @worldId);
    IF `entityType` IS NOT NULL THEN
        CALL prism_get_or_create_entity_type(entityType, @entityId);
    END IF;
    IF `material` IS NOT NULL THEN
        CALL prism_get_or_create_material(material, blockData, @materialId);
    END IF;
    IF `oldMaterial` IS NOT NULL THEN
        CALL prism_get_or_create_material(oldMaterial, oldBlockData, @oldMaterialId);
    END IF;
    INSERT INTO `%prefix%activities`
    (`timestamp`, `world_id`, `x`, `y`, `z`, `action_id`, `material_id`,
    `old_material_id`, `entity_type_id`, `cause_id`, `descriptor`, `metadata`)
    VALUES
    (`timestamp`, @worldId, `x`, `y`, `z`, @actionId, @materialId,
    @oldMaterialId, @entityId, @causeId, `descriptor`, `metadata`);
    SET @activityId = LAST_INSERT_ID();
    IF `customData` IS NOT NULL THEN
       INSERT INTO `%prefix%activities_custom_data` (`activity_id`, `version`, `data`)
        VALUES (@activityId, `customDataVersion`, `customData`);
    END IF;
END