CREATE OR REPLACE FUNCTION %prefix%get_or_create_block(
    blockNs VARCHAR(55),
    blockName VARCHAR(55),
    blockData VARCHAR(255),
    translationKey VARCHAR(155))
RETURNS INTEGER AS $$
DECLARE
    blockId INTEGER;
BEGIN
    IF blockData IS NOT NULL THEN
        SELECT block_id INTO blockId FROM
        %prefix%blocks WHERE ns = blockNs AND name = blockName AND data = blockData;
    ELSE
        SELECT block_id INTO blockId FROM
        %prefix%blocks WHERE ns = blockNs AND name = blockName AND data IS NULL;
    END IF;

    IF blockId IS NULL THEN
        INSERT INTO %prefix%blocks (ns, name, data, translation_key)
        VALUES (blockNs, blockName, blockData, translationKey) RETURNING block_id INTO blockId;
    END IF;

    RETURN blockId;
END;
$$ LANGUAGE plpgsql;