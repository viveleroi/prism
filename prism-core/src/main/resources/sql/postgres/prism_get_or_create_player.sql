CREATE OR REPLACE FUNCTION %prefix%get_or_create_player(playerName VARCHAR(32), uuid CHAR(36))
RETURNS INTEGER AS $$
DECLARE
    playerId INTEGER;
BEGIN
    INSERT INTO %prefix%players (player, player_uuid) VALUES (playerName, uuid)
    ON CONFLICT (player_uuid) DO UPDATE SET player = playerName
    RETURNING player_id INTO playerId;

    RETURN playerId;
END;
$$ LANGUAGE plpgsql;