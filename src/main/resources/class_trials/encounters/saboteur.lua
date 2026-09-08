local function beacon(c,s)
 local p,shape
 return {T.wait(32,function(c,s) p=T.copy(c.trial.player:get_location()); shape=T.circle(p,2.5); c.trial:pose('cast'); T.sound(c,'BLOCK_TRIPWIRE_ATTACH',.8) end,
  function(c,s,t) if t%4==0 then T.draw(c,shape) end end,
  function(c,s) c.trial:remove_actor('snare'); if R.prop(c,s,'snare',p,1,'&eSnare Beacon','TRIPWIRE_HOOK') then s.snare=shape; s.snareUntil=s.tick+120; s.snareInside=false end end),T.rest(30)}
end
local function payload(c,s)
 local impact,center,shape; local seq,group=R.draw(c,s,{warn=32,lock=12,damage=.35,cap=.9,releaseOnly=true,
  release=function(c,s) s.payloadActive=true end})
 T.append(seq,{R.flight(group,48,function(c,s,result) impact=result end),
 T.untilDone(10,nil,function(c,s,t)
  if impact and impact.hit_player then T.point(c,T.offset(c.trial.player:get_location(),0,1.2,0),T.amber) end
 end,function(c,s)
  if not impact then return end
  center=c.trial:ground(impact.hit_player and c.trial.player:get_location() or impact.location)
  if center then c.trial:remove_actor('payload'); if R.prop(c,s,'payload',center,1,'&ePayload Charge','TNT') then shape=T.circle(center,2.5); c.trial:actor('payload'):play_sound_at_self('ENTITY_TNT_PRIMED',.6,1.3) end end
 end,function(c,s) return not impact or not impact.hit_player end),
 T.wait(30,nil,function(c,s,t)
  local charge=c.trial:actor('payload'); if charge and shape then if t%4==0 then T.draw(c,shape); T.point(c,T.offset(center,0,1+t/30,0),T.amber) end; if t%10==0 then charge:play_sound_at_self('BLOCK_NOTE_BLOCK_HAT',.5,.8+t/30) end end
 end,function(c,s)
  if shape and c.trial:actor('payload') then
   c.trial:actor('payload'):play_sound_at_self('ENTITY_GENERIC_EXPLODE',.6,1.2)
   T.hit(c,shape,math.min(.65,math.max(0,.9-c.trial:group_damage(group)))); c.trial:remove_actor('payload')
  elseif shape then c.trial:say('Both warnings answered. Charge disarmed.') end
  s.payloadActive=false
 end),T.rest(60)})
 return seq
end
return T.encounter{
 init=function(c,s) R.init(c,s,true); s.snareUntil=0; s.snaredUntil=0; s.triggerReady=0 end,
 passive=function(c,s)
  R.passive(c,s)
  if s.snareUntil>s.tick and c.trial:actor('snare') then
   local inside=T.contains(s.snare,c.trial.player:get_location())
   if inside and not s.snareInside and not s.payloadActive and s.tick>=s.triggerReady then c.trial:slow_player(.2,20); s.snaredUntil=s.tick+20; s.triggerReady=s.tick+60; T.sound(c,'BLOCK_TRIPWIRE_CLICK_ON',1) end
   s.snareInside=inside
   if s.tick%5==0 then T.draw(c,s.snare,s.payloadActive and R.feather or T.amber) end
  elseif s.snare then c.trial:remove_actor('snare'); s.snare=nil; s.snareUntil=0 end
 end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'step') then s.phasePending=false; T.start(c,s,'step',M.move(c,s),M.cooldown)
  elseif s.snareUntil<=s.tick and T.ready(s,'beacon') then T.start(c,s,'beacon',beacon(c,s),440)
  elseif s.snaredUntil<=s.tick and T.ready(s,'payload') then T.start(c,s,'payload',payload(c,s),360)
  else R.basic(c,s) end
 end
}
