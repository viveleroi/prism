CREATE PROCEDURE %prefix%get_or_create_player (
    IN playerName VARCHAR(32),
    IN uuid CHAR(36),
    OUT playerId INT
)
BEGIN
    SELECT player_id INTO playerId FROM %prefix%players WHERE player_uuid = uuid;

    IF playerId IS NULL THEN
        INSERT INTO %prefix%players (player, player_uuid) VALUES (playerName, uuid);
        SELECT LAST_INSERT_ID() INTO playerId;
    ELSE
        UPDATE %prefix%players
        SET player = playerName
        WHERE player_uuid = uuid AND player <> playerName;
    END IF;
END;
