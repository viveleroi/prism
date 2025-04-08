CREATE PROCEDURE %prefix%get_or_create_cause
    (IN `causeStr` VARCHAR(25), IN `playerId` INT, OUT `causeId` INT)
BEGIN
    IF `playerId` IS NOT NULL THEN
        SELECT cause_id INTO `causeId` FROM
        %prefix%causes WHERE player_id = `playerId`;
    ELSEIF `causeStr` IS NOT NULL THEN
        SELECT cause_id INTO `causeId` FROM %prefix%causes WHERE cause = `causeStr`;
    END IF;
    IF `causeId` IS NULL THEN
        INSERT INTO %prefix%causes (`cause`, `player_id`) VALUES (`causeStr`, `playerId`);
        SET `causeId` = LAST_INSERT_ID();
    END IF;
END