EXPLAIN SELECT 
  HEX(`world_uuid`) AS worldUuid, 
  `action`, 
  `materials`.`material`, 
  `entity_type`, 
  `cause`, 
  HEX(`player_uuid`) AS playerUuid, 
  COUNT(*) OVER() AS totalRows, 
  AVG(`x`) AS `x`, 
  AVG(`y`) AS `y`, 
  AVG(`z`) AS `z`, 
  AVG(`timestamp`) AS `timestamp`, 
  COUNT(*) AS groupCount 
FROM 
  prism_activities AS activities 
  JOIN prism_actions AS actions ON `actions`.`action_id` = `activities`.`action_id` 
  JOIN prism_causes AS causes ON `causes`.`cause_id` = `activities`.`cause_id` 
  JOIN prism_worlds AS worlds ON `worlds`.`world_id` = `activities`.`world_id` 
  LEFT JOIN prism_activities_custom_data AS custom_data ON `custom_data`.`activity_id` = `activities`.`activity_id` 
  LEFT JOIN prism_players AS players ON `players`.`player_id` = `causes`.`player_id` 
  LEFT JOIN prism_entity_types AS entity_types ON `entity_types`.`entity_type_id` = `activities`.`entity_type_id` 
  LEFT JOIN prism_materials AS materials ON `materials`.`material_id` = `activities`.`material_id` 
WHERE 
  (
    `x` BETWEEN 2151 
    AND 2161
  ) 
  AND (
    `y` BETWEEN 63
    AND 73
  ) 
  AND (
    `z` BETWEEN -816
    AND -806
  ) 
  AND `world_uuid` = UNHEX("65ff612f2cd642dca96833c9afbd74f2") 
GROUP BY 
  `world_uuid`, 
  `activities`.`action_id`, 
  `materials`.`material`, 
  `entity_type`, 
  `cause`, 
  `player_uuid` 
ORDER BY 
  AVG(`timestamp`) DESC;

