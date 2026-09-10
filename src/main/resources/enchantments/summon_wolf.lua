local function summon(context)
  if not context.cooldowns:local_ready("summon") then return end
  local stats = context.player.elite_player
  if not stats then return end
  local wolf = context.world:spawn_boss_at_location(context.parameters.boss, context.player.current_location, stats.tier)
  if not wolf then return end
  if wolf.entity_type ~= "wolf" or not wolf:set_owner(context.player.uuid) or not context.item:consume(1) then
    if wolf.elite then wolf.elite:remove() else wolf:remove() end
    return
  end
  -- The existing custom-boss/pet owner retains the successfully summoned wolf.
  context.cooldowns:set_local(context.parameters.cooldown_ticks, "summon")
  context.event:cancel()
end

return {
  api_version = 1,
  on_right_click = summon,
  on_shift_right_click = summon,
}
