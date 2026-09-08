local stakes={'west_root','east_root'}
local function bloom(c,s)
 local p
 return {T.wait(36,function(c,s) p=T.copy(c.trial:position()); c.trial:pose('cast'); T.sound(c,'BLOCK_AZALEA_LEAVES_PLACE',.8) end,
  function(c,s,t) if t%4==0 then T.draw(c,T.circle(p,4),C.green) end end,
  function(c,s)
   s.garden=T.circle(p,4); s.gardenUntil=s.tick+120; s.nextPulse=s.tick+40; s.sanctuary=p; s.roots=true
   for i,id in ipairs(stakes) do c.trial:remove_actor(id); C.focus(c,id,T.offset(p,i==1 and -3 or 3,0,0),1,'&eGarden Root Stake','MANGROVE_ROOTS') end
  end),T.rest(30)}
end
local function ward(c,s)
 local shape
 return {T.wait(32,function(c,s) shape=T.arc(s.garden.p,c.trial.player:get_location(),4,3,270); c.trial:pose('cast'); T.sound(c,'BLOCK_SWEET_BERRY_BUSH_PLACE',.8) end,
  function(c,s,t) if t%4==0 then T.draw(c,shape) end end,
  function(c,s)
   s.thorns=shape; s.wardUntil=s.tick+80; s.thornHits=0; s.thornInside=false; s.nextThorn=s.tick
   for _,v in ipairs(C.living(c,s)) do if T.contains(s.garden,v.actor:get_location()) then C.shield(c,s,v.id,c.trial.matched_hit,80) end end
  end),T.rest(30)}
end
return T.encounter{
 init=function(c,s) C.init(c,s,2); s.gardenUntil=0; s.wardUntil=0 end, damaged=C.damage,
 passive=function(c,s)
  C.passive(c,s)
  if s.roots and not c.trial:actor(stakes[1]) and not c.trial:actor(stakes[2]) then
   s.roots=false; s.gardenUntil=0; s.wardUntil=0; c.trial:say('The roots are gone. You have opened the garden.'); C.recover(c,s,60)
  end
  if s.gardenUntil>s.tick then
   if s.tick%5==0 then T.draw(c,s.garden,C.green) end
   if s.tick>=s.nextPulse then s.nextPulse=s.tick+40; for _,v in ipairs(C.weakest(c,s,true)) do if T.contains(s.garden,v.actor:get_location()) then C.heal(c,s,v.id,.25*c.trial.matched_hit) end end end
  end
  if s.wardUntil>s.tick then
   if s.tick%4==0 then T.draw(c,s.thorns) end
   local inside=T.contains(s.thorns,c.trial.player:get_location())
   if inside and not s.thornInside and s.thornHits<2 and s.tick>=s.nextThorn and T.hit(c,s.thorns,.35) then s.thornHits=s.thornHits+1; s.nextThorn=s.tick+30 end
   s.thornInside=inside
  end
  if s.roots and s.gardenUntil<=s.tick and s.wardUntil<=s.tick then s.roots=false; for _,id in ipairs(stakes) do c.trial:remove_actor(id) end end
 end,
 choose=function(c,s)
  if s.phasePending and not s.roots and T.ready(s,'flight') then s.phasePending=false; local seq=M.move(c,s); T.append(seq,bloom(c,s)); T.lockout(seq,'bloom',480); T.start(c,s,'flight',seq,M.cooldown)
  elseif not s.roots and T.ready(s,'bloom') then T.start(c,s,'bloom',bloom(c,s),480)
  elseif s.roots and s.gardenUntil>s.tick and T.ready(s,'ward') then T.start(c,s,'ward',ward(c,s),440)
  else T.basic(c,s,'melee') end
 end
}
