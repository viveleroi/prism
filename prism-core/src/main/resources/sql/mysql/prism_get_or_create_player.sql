CREATE PROCEDURE %prefix%get_or_create_player
    (IN `playerName` VARCHAR(16), IN `uuid` VARCHAR(55), OUT `playerId` INT)
BEGIN
    SELECT player_id INTO `playerId` FROM
        %prefix%players WHERE player_uuid = `uuid`;
    IF `playerId` IS NULL THEN
        INSERT INTO %prefix%players (`player`, `player_uuid`)
        VALUES (`playerName`, `uuid`);
        SET `playerId` = LAST_INSERT_ID();
    END IF;
END