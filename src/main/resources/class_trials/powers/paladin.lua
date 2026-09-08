-- Shared Paladin techniques. Each encounter explicitly composes and tunes its own lessons.
local P={}
function P.init(c,s,cadets)
 s.links={}; s.allies={}; s.ringUntil=0; s.auraUntil=0; s.outgoing=1
 for i,v in ipairs(cadets or {}) do T.spawnAlly(c,s,v[1],i%2==1 and -3 or 3,v[2],3) end
end
function P.passive(c,s)
 local p=c.trial:position()
 if s.tick%5==0 then
  for id,link in pairs(s.links) do
   local actor=c.trial:actor(id)
   if not actor or s.tick>=link.untilTick then s.links[id]=nil
   elseif T.distance(p,actor:get_location())>link.range then
    link.separated=link.separated+5
    if link.separated>=link.breakTicks then
     s.links[id]=nil; s.brokenLinks=(s.brokenLinks or 0)+1; T.sound(c,'BLOCK_CHAIN_BREAK',1.2)
     if link.onBreak then link.onBreak(c,s) end
    end
   else link.separated=0; T.tether(c,p,actor:get_location()) end
  end
  if s.ringUntil>s.tick then T.draw(c,s.ring,T.gold) end
 end
 T.allies(c,s)
end
function P.damage(c,s)
 local id=c.trial:damaged_actor(); if id=='boss' then return end
 local actor=c.trial:actor(id); if not actor then return end
 local reduction=1
 if s.ringUntil>s.tick and T.contains(s.ring,actor:get_location()) then reduction=reduction*(1-s.ringReduction) end
 if (s.coverUntil or 0)>s.tick and s.cover and T.contains(s.cover,actor:get_location()) then reduction=reduction*.65 end
 c.event.multiply_damage_amount(reduction)
 local link=s.links[id]
 if link and link.untilTick>s.tick and T.distance(c.trial:position(),actor:get_location())<=link.range then
  local transfer=c.event.get_damage_amount()*link.fraction
  c.event.multiply_damage_amount(1-link.fraction); c.trial:transfer_damage(transfer)
 end
end
function P.link(c,s,ids,options)
 return {T.wait(options.warn or 28,function(c,s) c.trial:pose('guard'); T.sound(c,'ITEM_SHIELD_BLOCK',1.2); s.brokenLinks=0 end,
  function(c,s,t) if t%4==0 then for _,v in ipairs(T.alive(c,ids)) do T.tether(c,c.trial:position(),v.actor:get_location()) end end end,
  function(c,s) for _,v in ipairs(T.alive(c,ids)) do s.links[v.id]={untilTick=s.tick+options.duration,fraction=options.fraction,range=options.range or 40,breakTicks=options.breakTicks or 5,separated=0,onBreak=options.onBreak} end end),
  T.wait(options.duration,nil,nil,function(c,s) for _,id in ipairs(ids) do s.links[id]=nil end end),T.rest(options.recovery or 40)}
end
function P.ring(c,s,radius,reduction,duration)
 local g
 return {T.wait(30,function(c,s) g=T.circle(c.trial:position(),radius); T.sound(c,'BLOCK_BELL_USE',1.1); c.trial:pose('guard') end,
  function(c,s,t) if t%4==0 then T.draw(c,g,T.gold) end end,
  function(c,s) s.ring=g; s.ringReduction=reduction; s.ringUntil=s.tick+duration end),T.rest(30)}
end
function P.wall(c,s)
 local center,target
 return {T.wait(32,function(c,s)
  local p=c.trial:position(); target=T.copy(c.trial.player:get_location()); local x,z=T.direction(p,target); center=T.offset(p,x*1.5,0,z*1.5)
  c.trial:pose('guard'); T.sound(c,'BLOCK_AMETHYST_BLOCK_RESONATE',.8)
 end,function(c,s,t) if t%4==0 then
  local x,z=T.direction(center,target); T.draw(c,T.lane(T.offset(center,z*2.5,0,-x*2.5),T.offset(center,-z*2.5,0,x*2.5),.2),T.gold)
 end end,function(c,s) c.trial:wall('sanctuary',center,target,5,100) end),
 T.wait(100,nil,nil,function(c,s) c.trial:remove_wall('sanctuary') end),T.rest(30)}
end
function P.judgment(c,s,cap,onHit)
 local arc
 return {T.wait(30,function(c,s) c.trial:pose('guard'); T.sound(c,'BLOCK_ANVIL_PLACE',.6) end,
  function(c,s,t) if t%5==0 then T.draw(c,T.circle(c.trial:position(),1),T.gold) end end,
  function(c,s) s.collecting=true; s.weights=0 end),
 T.wait(60,nil,function(c,s,t) if t%4==0 then for i=1,s.weights do T.point(c,T.offset(c.trial:position(),(i-2)*.7,2,0),T.gold) end end end,
  function(c,s) s.collecting=false; arc=T.cone(c.trial:position(),c.trial.player:get_location(),3.5,80) end),
 T.wait(30,function(c,s) c.trial:pose('draw'); T.sound(c,'BLOCK_NOTE_BLOCK_BELL',.7) end,
  function(c,s,t) if t%4==0 then T.draw(c,arc) end end,
  function(c,s) c.trial:pose('swing'); T.sound(c,'ENTITY_PLAYER_ATTACK_CRIT',.8); local hit=T.hit(c,arc,math.min(cap,.6+.2*s.weights)); if hit and onHit then onHit(c,s) end end),T.rest(60)}
end
function P.collect(c,s) if c.trial:damaged_actor()=='boss' and s.collecting then s.weights=math.min(3,s.weights+1); T.sound(c,'BLOCK_NOTE_BLOCK_BELL',.6+s.weights*.2) end end
function P.censure(c,s)
 local ring
 return {T.wait(26,function(c,s) ring=T.circle(c.trial:position(),4); T.sound(c,'BLOCK_BELL_USE',.85) end,
  function(c,s,t) if t%4==0 then T.draw(c,ring) end end,
  function(c,s) if T.contains(ring,c.trial.player:get_location()) then T.weak(c,s,60,.15); T.eye(c,c.trial.player:get_location()) end end),T.rest(30)}
end
function P.reform(c,s,ids,anchor,warn,duration,after)
 local destinations,origin
 return {T.wait(warn,function(c,s)
  origin=T.copy(anchor or c.trial:position()); destinations={}; c.trial:pose('guard'); T.sound(c,'ITEM_GOAT_HORN_SOUND_0',1.2)
  for i,id in ipairs(ids) do destinations[id]=T.offset(origin,i%2==1 and -2.5 or 2.5,0,-3) end
 end,function(c,s,t) if t%5==0 then for _,v in ipairs(T.alive(c,ids)) do T.draw(c,T.lane(v.actor:get_location(),destinations[v.id],1),T.gold) end end end,
  function(c,s) s.alliesPausedUntil=s.tick+duration+after; for _,v in ipairs(T.alive(c,ids)) do c.trial:cleanse(v.id,true,0) end end),
 T.wait(duration,nil,function(c,s,t) if t%5==0 then for _,v in ipairs(T.alive(c,ids)) do c.trial:actor_step(v.id,destinations[v.id]); T.draw(c,T.circle(destinations[v.id],.7),T.gold) end end end),T.rest(after)}
end
function P.aura(c,s,ids,center,radius,bonus,untilTick)
 for _,id in ipairs(ids) do local state=s.allies[id]; local actor=c.trial:actor(id)
  if state then state.damage=actor and s.tick<untilTick and T.distance(center,actor:get_location())<=radius and bonus or 1 end
 end
 if s.tick<untilTick and s.tick%5==0 then T.draw(c,T.circle(center,radius),T.gold) end
end
function P.rescue(c,s,ids)
 local farthest=nil; local distance=-1
 for _,v in ipairs(T.alive(c,ids)) do local d=T.distance(c.trial:position(),v.actor:get_location()); if d>distance then distance=d; farthest=v.actor:get_location() end end
 return M.move(c,s,farthest)
end
function P.roar(c,s)
 local ring
 return {T.wait(30,function(c,s) ring=T.circle(c.trial:position(),5); T.sound(c,'ENTITY_RAVAGER_ROAR',.8); c.trial:pose('cast') end,
  function(c,s,t) if t%4==0 then T.draw(c,ring); T.draw(c,T.circle(ring.p,5-4*t/30)) end end,
  function(c,s) if T.contains(ring,c.trial.player:get_location()) then c.trial:push(ring.p,-.25); T.weak(c,s,40,.15) end end),T.rest(30)}
end
