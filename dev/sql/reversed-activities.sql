SELECT 
  `action`, 
  `descriptor`,
  `player`,
  `world`, 
  `x`, 
  `y`, 
  `z`, 
  `timestamp`, 
  `reversed`
FROM 
  prism_activities AS activities 
  JOIN prism_actions AS actions ON `actions`.`action_id` = `activities`.`action_id` 
  JOIN prism_causes AS causes ON `causes`.`cause_id` = `activities`.`cause_id` 
  JOIN prism_worlds AS worlds ON `worlds`.`world_id` = `activities`.`world_id` 
  LEFT JOIN prism_players AS players ON `players`.`player_id` = `causes`.`player_id` 
  LEFT JOIN prism_materials AS materials ON `materials`.`material_id` = `activities`.`material_id`
WHERE reversed = 1;