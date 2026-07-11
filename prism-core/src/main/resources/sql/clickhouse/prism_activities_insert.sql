INSERT INTO %prefix%activities (
    activity_id, `timestamp`, world, world_uuid, x, y, z, action,
    affected_material, affected_item_data, affected_item_airtag, affected_item_quantity,
    affected_block_ns, affected_block_name, affected_block_data, affected_block_translation_key,
    replaced_block_ns, replaced_block_name, replaced_block_data,
    affected_entity_type, affected_player, affected_player_uuid,
    cause, cause_player, cause_player_uuid,
    cause_entity_type, cause_entity_type_translation_key,
    cause_block_ns, cause_block_name, cause_block_translation_key,
    descriptor, metadata, serializer_version, serialized_data, reversed
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
