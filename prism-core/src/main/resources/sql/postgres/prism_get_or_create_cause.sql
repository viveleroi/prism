CREATE OR REPLACE FUNCTION %prefix%get_or_create_cause(causeStr VARCHAR(25), playerId INTEGER)
RETURNS INTEGER AS $$
DECLARE
    causeId INTEGER;
BEGIN
    IF playerId IS NOT NULL THEN
        SELECT cause_id INTO causeId FROM %prefix%causes WHERE player_id = playerId;
    ELSEIF causeStr IS NOT NULL THEN
        SELECT cause_id INTO causeId FROM %prefix%causes WHERE cause = causeStr;
    END IF;

    IF causeId IS NULL THEN
        INSERT INTO %prefix%causes (cause, player_id) VALUES (causeStr, playerId) RETURNING cause_id INTO causeId;
    END IF;

    RETURN causeId;
END;
$$ LANGUAGE plpgsql;