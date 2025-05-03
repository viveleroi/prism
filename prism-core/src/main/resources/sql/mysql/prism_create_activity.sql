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
    IN `itemQuantity` SMALLINT,
    IN `itemData` TEXT,
    IN `blockNamespace` VARCHAR(55),
    IN `blockName` VARCHAR(55),
    IN `blockData` VARCHAR(255),
    IN `replacedBlockNamespace` VARCHAR(55),
    IN `replacedBlockName` VARCHAR(55),
    IN `replacedBlockData` VARCHAR(255),
    IN `world` VARCHAR(255),
    IN `worldUuid` CHAR(36),
    IN `serializerVersion` SMALLINT,
    IN `serializedData` TEXT,
    IN `descriptor` VARCHAR(255),
    IN `metadata` VARCHAR(255),
    IN `translationKey` VARCHAR(155)
)
BEGIN
    SET @entityId = NULL;
    SET @itemId = NULL;
    SET @blockId = NULL;
    SET @replacedBlockId = NULL;
    SET @playerId = NULL;

    -- Create the action
    CALL %prefix%get_or_create_action(`action`, @actionId);

    -- Create the player
    IF `playerUuid` IS NOT NULL THEN
        CALL %prefix%get_or_create_player(`player`, `playerUuid`, @playerId);
    END IF;

    -- Create the cause
    CALL %prefix%get_or_create_cause(`cause`, @playerId, @causeId);

    -- Create the world
    CALL %prefix%get_or_create_world(`world`, `worldUuid`, @worldId);

    -- Create the entity type
    IF `entityType` IS NOT NULL THEN
        CALL %prefix%get_or_create_entity_type(entityType, @entityId);
    END IF;

    -- Create the item
    IF `material` IS NOT NULL THEN
        CALL %prefix%get_or_create_item(material, itemData, @itemId);
    END IF;

    -- Create the block
    IF `blockName` IS NOT NULL THEN
        CALL %prefix%get_or_create_block(blockNamespace, blockName, blockData, translationKey, @blockId);
    END IF;

    -- Create the replaced block
    IF `replacedBlockName` IS NOT NULL THEN
        CALL %prefix%get_or_create_block(replacedBlockNamespace, replacedBlockName, replacedBlockData, null, @replacedBlockId);
    END IF;

    -- Create the activities
    INSERT INTO `%prefix%activities`
    (`timestamp`, `world_id`, `x`, `y`, `z`, `action_id`, `item_id`, `item_quantity`,
    `block_id`, `replaced_block_id`, `entity_type_id`, `cause_id`, `descriptor`, `metadata`, `serializer_version`, `serialized_data`)
    VALUES
    (`timestamp`, @worldId, `x`, `y`, `z`, @actionId, @itemId, `itemQuantity`,
    @blockId, @replacedBlockId, @entityId, @causeId, `descriptor`, `metadata`,
     `serializerVersion`, `serializedData`);

    SET @activityId = LAST_INSERT_ID();
END