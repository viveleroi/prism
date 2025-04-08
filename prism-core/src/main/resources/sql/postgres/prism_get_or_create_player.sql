CREATE OR REPLACE FUNCTION %prefix%get_or_create_player(playerName VARCHAR(16), uuid VARCHAR(55))
RETURNS INTEGER AS $$
DECLARE
    playerId INTEGER;
BEGIN
    SELECT player_id INTO playerId FROM %prefix%players WHERE player_uuid = uuid;

    IF playerId IS NULL THEN
        INSERT INTO %prefix%players (player, player_uuid) VALUES (playerName, uuid) RETURNING player_id INTO playerId;
    END IF;

    RETURN playerId;
END;
$$ LANGUAGE plpgsql;