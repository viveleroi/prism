CREATE TABLE IF NOT EXISTS %prefix%airtags
(
    airtag String,
    player_uuid String,
    player_name String,
    created_at UInt32,
    latest_item_material String,
    latest_item_data String,
    latest_item_timestamp UInt32
)
ENGINE = ReplacingMergeTree(created_at)
ORDER BY airtag
SETTINGS enable_block_number_column = 1, enable_block_offset_column = 1
