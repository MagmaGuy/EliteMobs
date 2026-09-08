local function immovable(c,s)
 return {T.wait(30,function(c,s) c.trial:pose('guard'); T.sound(c,'ITEM_ARMOR_EQUIP_NETHERITE',.5) end,
  function(c,s,t) if t%5==0 then T.draw(c,T.circle(c.trial:position(),1.5),T.gold) end end,
  function(c,s) s.armed=true end),T.rest(20)}
end
local function worldbreaker(c,s)
 local shape
 return {T.wait(50,function(c,s) s.charging=true; s.pressure=0; s.controlUntil=s.armed and s.tick+50 or 0; c.trial:pose('draw'); T.sound(c,'BLOCK_ANVIL_PLACE',.5) end,
  function(c,s,t)
   if not shape or t<20 then local p=c.trial:position(); shape=T.cone(p,c.trial.player:get_location(),6,280); c.trial:face(c.trial.player:get_location(),2) end
   if t%4==0 then T.draw(c,shape); for _,angle in ipairs({-70,0,70}) do T.draw(c,T.lane(shape.p,T.rotate(shape.p,T.offset(shape.p,shape.x,0,shape.z),angle,6),.2)) end end
  end,function(c,s) s.charging=false; s.armed=false; s.controlUntil=0; c.trial:pose('swing'); T.sound(c,'ENTITY_GENERIC_EXPLODE',.4); T.hit(c,shape,1.4) end),T.rest(80,1.25)}
end
return T.encounter{
 init=function(c,s) B.init(c,s); s.armed=false end, passive=B.passive,
 damaged=function(c,s)
  if s.charging and s.armed then
   s.pressure=s.pressure+c.event.damage_amount; c.event.multiply_damage_amount(.6)
   if s.pressure>=3*c.trial.matched_hit then s.charging=false; s.armed=false; s.controlUntil=0; T.sound(c,'ITEM_SHIELD_BREAK',.5); c.trial:say('You broke the commitment. Take what it cost me.'); T.interrupt(c,s,{T.rest(80,1.25)}) end
  end
 end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'leap') then s.phasePending=false; local seq=M.move(c,s); if T.ready(s,'immovable') then T.append(seq,immovable(c,s)); T.lockout(seq,'immovable',480) end; T.append(seq,worldbreaker(c,s)); T.lockout(seq,'worldbreaker',480); T.start(c,s,'leap',seq,M.cooldown)
  elseif T.ready(s,'immovable') then T.start(c,s,'immovable',immovable(c,s),480)
  elseif T.ready(s,'worldbreaker') then T.start(c,s,'worldbreaker',worldbreaker(c,s),480)
  else T.basic(c,s,'melee') end
 end
}
