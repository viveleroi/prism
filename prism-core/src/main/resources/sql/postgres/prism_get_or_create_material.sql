CREATE OR REPLACE FUNCTION %prefix%get_or_create_material(materialKey VARCHAR(45))
RETURNS SMALLINT AS $$
DECLARE
    materialId SMALLINT;
BEGIN
    SELECT material_id INTO materialId FROM
    %prefix%materials WHERE material = materialKey;

    IF materialId IS NULL THEN
        INSERT INTO %prefix%materials (material)
        VALUES (materialKey) RETURNING material_id INTO materialId;
    END IF;

    RETURN materialId;
END;
$$ LANGUAGE plpgsql;