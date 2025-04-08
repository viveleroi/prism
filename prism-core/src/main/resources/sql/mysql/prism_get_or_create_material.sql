CREATE PROCEDURE %prefix%get_or_create_material
(IN `materialKey` VARCHAR(45), IN `blockData` VARCHAR(155), OUT `materialId` SMALLINT)
BEGIN
    IF blockData IS NOT NULL THEN
        SELECT material_id INTO `materialId` FROM
            %prefix%materials WHERE material = `materialKey` AND data = `blockData`;
    ELSE
        SELECT material_id INTO `materialId` FROM
            %prefix%materials WHERE material = `materialKey` AND data IS NULL;
    END IF;
    IF `materialId` IS NULL THEN
        INSERT INTO %prefix%materials (`material`, `data`)
        VALUES (`materialKey`, `blockData`);
        SET `materialId` = LAST_INSERT_ID();
    END IF;
END