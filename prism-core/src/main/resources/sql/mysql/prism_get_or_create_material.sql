CREATE PROCEDURE %prefix%get_or_create_material
(IN `materialKey` VARCHAR(45), OUT `materialId` SMALLINT)
BEGIN
    SELECT material_id INTO `materialId` FROM
        %prefix%materials WHERE material = `materialKey`;

    IF `materialId` IS NULL THEN
        INSERT INTO %prefix%materials (`material`) VALUES (`materialKey`);

        SET `materialId` = LAST_INSERT_ID();
    END IF;
END