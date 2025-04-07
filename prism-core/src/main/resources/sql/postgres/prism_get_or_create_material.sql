CREATE OR REPLACE FUNCTION prism_get_or_create_material(materialKey VARCHAR(45), blockData VARCHAR(155))
RETURNS SMALLINT AS $$
DECLARE
    materialId SMALLINT;
BEGIN
    IF blockData IS NOT NULL THEN
        SELECT material_id INTO materialId FROM
        %prefix%_materials WHERE material = materialKey AND data = blockData;
    ELSE
        SELECT material_id INTO materialId FROM
        %prefix%_materials WHERE material = materialKey AND data IS NULL;
    END IF;

    IF materialId IS NULL THEN
        INSERT INTO %prefix%_materials (material, data)
        VALUES (materialKey, blockData) RETURNING material_id INTO materialId;
    END IF;

    RETURN materialId;
END;
$$ LANGUAGE plpgsql;