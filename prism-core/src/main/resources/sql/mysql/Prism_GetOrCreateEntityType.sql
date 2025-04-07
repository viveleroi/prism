CREATE PROCEDURE Prism_GetOrCreateEntityType
    (IN `entityType` VARCHAR(25), OUT `entityTypeId` SMALLINT)
BEGIN
    SELECT entity_type_id INTO `entityTypeId` FROM
    %prefix%entity_types WHERE entity_type = `entityType`;
    IF `entityTypeId` IS NULL THEN
        INSERT INTO %prefix%entity_types (`entity_type`) VALUES (`entityType`);
        SET `entityTypeId` = LAST_INSERT_ID();
    END IF;
END