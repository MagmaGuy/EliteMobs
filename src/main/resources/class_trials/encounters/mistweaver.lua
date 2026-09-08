local function tide(c,s)
 local p,x,z,preview; local healed={}; local hit=false
 return {T.wait(36,function(c,s) p=T.copy(c.trial:position()); x,z=T.direction(p,c.trial.player:get_location()); preview=T.lane(T.offset(p,-x*.4,0,-z*.4),T.offset(p,x*8.4,0,z*8.4),5); c.trial:pose('cast'); T.sound(c,'BLOCK_CONDUIT_AMBIENT_SHORT',1) end,
  function(c,s,t) if t%4==0 then T.draw(c,preview,C.water) end end),
 T.wait(50,nil,function(c,s,t)
  local distance=8*(t+1)/50; local front=T.offset(p,x*distance,0,z*distance); local shape=T.lane(T.offset(front,-x*.4,0,-z*.4),T.offset(front,x*.4,0,z*.4),5)
  if t%4==0 then T.draw(c,shape,C.water) end
  if not hit then hit=T.hit(c,shape,.65) end
  for _,v in ipairs(C.living(c,s)) do if not healed[v.id] and T.contains(shape,v.actor:get_location()) then healed[v.id]=true; C.heal(c,s,v.id,.5*c.trial.matched_hit) end end
 end),T.rest(50)}
end
local function veil(c,s)
 local destinations,chosen
 return {T.wait(30,function(c,s)
  local p=c.trial:position(); local x,z=T.direction(p,c.trial.player:get_location()); destinations={T.offset(p,-z*4,0,x*4),T.offset(p,z*4,0,-x*4)}; chosen=s.phase==2 and 2 or 1
  c.trial:pose('cast'); T.sound(c,'ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM',1.4)
 end,function(c,s,t)
  if t%4==0 then for i,point in ipairs(destinations) do T.draw(c,T.circle(point,.6),i==chosen and T.gold or C.water); for h=0,3 do T.point(c,T.offset(point,0,h*.5,0),i==chosen and T.gold or C.water) end end end
 end,function(c,s)
  if c.trial:blink(destinations[chosen]) then c.trial:cleanse('boss',false,0); s.veilUntil=s.tick+40 else T.sound(c,'BLOCK_FIRE_EXTINGUISH',1.2) end
 end),T.rest(50)}
end
return T.encounter{
 init=function(c,s) C.init(c,s,2); s.veilUntil=0 end, passive=C.passive,
 damaged=function(c,s) C.damage(c,s); if c.trial:damaged_actor()=='boss' and s.veilUntil>s.tick then c.event.multiply_damage_amount(.75) end end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'mobility') then s.phasePending=false; local weakest=C.weakest(c,s,false); T.start(c,s,'mobility',C.flight(c,s,weakest[1] and weakest[1].id),M.cooldown)
  elseif T.ready(s,'tide') then T.start(c,s,'tide',tide(c,s),440)
  elseif T.ready(s,'mobility') then T.start(c,s,'mobility',veil(c,s),400)
  else T.basic(c,s,'melee') end
 end
}
