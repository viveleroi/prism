CREATE PROCEDURE %prefix%get_or_create_item
(IN `materialKey` VARCHAR(45), OUT `itemId` SMALLINT)
BEGIN
    SELECT item_id INTO `itemId` FROM
        %prefix%items WHERE material = `materialKey`;

    IF `itemId` IS NULL THEN
        INSERT INTO %prefix%items (`material`) VALUES (`materialKey`);

        SET `itemId` = LAST_INSERT_ID();
    END IF;
END