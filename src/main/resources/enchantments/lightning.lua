local function strike(context)
  local target = context.target
  if not target or not target.is_elite or not context.cooldowns:local_ready("strike") then return end
  local stats = context.source.providers.elitemobs
  if not stats or stats.weapon_level <= 0 then return end
  -- The current EM inventory owner reads Lightning from the attacking main hand only.
  local chance = context.enchantment.level * context.enchantment.level / 1000
  if math.random() >= chance then return end
  context.cooldowns:set_local(context.parameters.cooldown_ticks, "strike")
  local at = target.current_location
  context.world:lightning_effect_at_location(at)
  for _, entity in ipairs(context.world:get_nearby_entities(at.x,at.y,at.z,2.5)) do
    if entity.is_elite and entity.is_alive and entity:can_receive_hostile_effect(context.player.uuid) then
      context.action:damage_target(entity.uuid, stats.weapon_level * 2.5)
    end
  end
end
return {api_version=1, on_attack_entity=strike, on_projectile_hit=strike}
