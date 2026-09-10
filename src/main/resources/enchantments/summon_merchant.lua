local function summon(context, from_chat)
  if not context.cooldowns:local_ready("summon") then return end
  local npc = context.world:spawn_npc_at_location(context.parameters.npc, context.player.current_location)
  if not npc then return end
  if not context.item:consume(1) then npc:remove(); return end
  -- Cooldown precedes the optional chat dispatch, deduplicating both input paths.
  context.cooldowns:set_local(context.parameters.cooldown_ticks, "summon")
  if not from_chat then
    context.event:cancel()
    if context.parameters.players_say_message and context.parameters.message ~= "" then
      context.player:chat(context.parameters.message)
    end
  end
end

local function interact(context)
  if context.source.slot == "MAINHAND" then summon(context, false) end
end

return {
  api_version = 1,
  on_right_click = interact,
  on_shift_right_click = interact,
  on_chat = function(context)
    local message = context.parameters.message
    if message ~= "" and string.lower(context.source.message) == string.lower(message) then
      summon(context, true)
    end
  end,
}
