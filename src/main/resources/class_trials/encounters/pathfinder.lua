local beaconIds={'trail_one','trail_two','trail_three'}
local function trail(c,s)
 local p,target,segments
 return {T.wait(32,function(c,s)
  p=T.copy(c.trial:position()); target=T.rotate(p,c.trial.player:get_location(),0,9); local x,z=T.direction(p,target); segments={}
  for i=1,3 do segments[i]=T.lane(T.offset(p,x*(i-1)*3,0,z*(i-1)*3),T.offset(p,x*i*3,0,z*i*3),4) end
  T.sound(c,'BLOCK_NOTE_BLOCK_FLUTE',1.4)
 end,function(c,s,t) if t%4==0 then for _,shape in ipairs(segments) do T.draw(c,shape,R.feather) end end end,
  function(c,s)
   s.trail=segments; s.trailUntil=s.tick+140; s.trailStart=p
   for i,id in ipairs(beaconIds) do c.trial:remove_actor(id); R.prop(c,s,id,segments[i].q,1,'&eTrail Beacon','SEA_LANTERN') end
  end),T.rest(30)}
end
local function pack(c,s)
 local destination,shape,launched; local pounceHit=false
 local seq=R.mark(c,s,30,100,'BLOCK_NOTE_BLOCK_FLUTE')
 if c.trial:actor('wolf') then
  T.append(seq,{T.wait(20,function(c,s) s.pouncing=true; destination=T.copy(c.trial.player:get_location()); shape=T.circle(destination,2); T.sound(c,'ENTITY_WOLF_GROWL',1) end,
   function(c,s,t) if t%4==0 then T.draw(c,shape) end end,
   function(c,s) launched=c.trial:actor_leap('wolf',destination,20) end),
  T.wait(22,nil,nil,function(c,s)
   local wolf=c.trial:actor('wolf'); if launched and wolf and T.distance(wolf:get_location(),destination)<1.5 and T.contains(shape,c.trial.player:get_location()) then pounceHit=c.trial:actor_damage('wolf',.45) end
   s.pouncing=false
  end),T.rest(10)})
 end
 T.append(seq,R.draw(c,s,{warn=30,lock=12,damage=function(c,s) return pounceHit and .45 or .65 end,cap=.65,recovery=56}))
 return seq
end
return T.encounter{
 init=function(c,s) R.init(c,s,false); s.trailUntil=0; c.trial:spawn_actor('wolf',T.offset(c.trial:position(),3,0,1),4,'&fTraining Wolf','WOLF') end,
 passive=function(c,s)
  R.passive(c,s)
  if s.trailUntil>s.tick then
   if s.tick%5==0 then for i,id in ipairs(beaconIds) do if c.trial:actor(id) then T.draw(c,s.trail[i],R.feather) end end end
  elseif s.trail then for _,id in ipairs(beaconIds) do c.trial:remove_actor(id) end; s.trail=nil end
  local wolf=c.trial:actor('wolf')
  if wolf and not s.pouncing and s.tick%5==0 then
   local speed=.15; if s.trailUntil>s.tick then for i,id in ipairs(beaconIds) do if c.trial:actor(id) and T.contains(s.trail[i],wolf:get_location()) then speed=.18; break end end end
   local target=c.trial.player:get_location(); if T.distance(wolf:get_location(),target)>3 then c.trial:actor_step('wolf',T.rotate(target,wolf:get_location(),0,2.5),speed,true) end
  end
 end,
 choose=function(c,s)
  if s.phasePending and s.trailUntil<=s.tick and T.ready(s,'step') then s.phasePending=false; local seq=M.move(c,s); T.append(seq,trail(c,s)); T.lockout(seq,'trail',480); T.start(c,s,'step',seq,M.cooldown)
  elseif s.trailUntil<=s.tick and T.ready(s,'trail') then T.start(c,s,'trail',trail(c,s),480)
  elseif T.ready(s,'pack') then T.start(c,s,'pack',pack(c,s),360)
  else R.basic(c,s) end
 end
}
