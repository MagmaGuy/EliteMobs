-- Double-tap sneak on a rail to ride a packet-only minecart down the track the way you face.
-- Sneaking again, the end of the track or taking the boots off gets you off.
return {api_version=1,on_sneak=function(context)
  if context.action:has_previous() then
    -- This sneak is the dismount; it does not start another double tap.
    context.action:replace_previous()
    context.cooldowns:set_local(0,'tap')
    return
  end
  if not context.source.providers.elitemobs.usable_slots.FEET then return end
  if context.cooldowns:local_ready('tap') then context.cooldowns:set_local(context.parameters.double_tap_ticks,'tap'); return end
  context.cooldowns:set_local(0,'tap')
  context.action:ride_rails(context.parameters.speed)
end}
