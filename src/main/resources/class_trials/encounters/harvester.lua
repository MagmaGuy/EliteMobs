local function pair(c,s)
 if s.pairs>=3 then return end
 s.pairs=s.pairs+1; s.effigies={}; local p=c.trial:position(); local spread=s.phase==2 and 4 or 3
 for i=1,2 do
  local id='effigy_'..s.pairs..'_'..i; local point=T.offset(p,i==1 and -spread or spread,0,3)
  if B.prop(c,id,point,1,'&eWounded Training Effigy','HAY_BLOCK') then s.effigies[#s.effigies+1]=id end
 end
end
local function chain(c,s)
 local route={}; local start
 local seq={T.wait(36,function(c,s)
  start=T.copy(c.trial:position()); route={}
  for _,id in ipairs(s.effigies) do local actor=c.trial:actor(id); if actor then route[#route+1]={id=id,p=T.copy(actor:get_location())} end end
  route[#route+1]={p=T.copy(c.trial.player:get_location())}; c.trial:pose('draw'); T.sound(c,'BLOCK_CHAIN_PLACE',.8)
 end,function(c,s,t)
  if t%4==0 then local previous=start; for _,leg in ipairs(route) do T.draw(c,T.lane(previous,leg.p,1.4)); previous=leg.p end end
 end)}
 -- Three authored slots at most: two effigies and the captured challenger position.
 for index=1,3 do
  local valid=false; local shape
  T.append(seq,{T.wait(12,function(c,s)
   local leg=route[index]; if not leg then return end
   if leg.id and not c.trial:actor(leg.id) then c.trial:say('A missing link. My reach ends there.'); T.interrupt(c,s,{T.rest(40)}); return end
   shape=T.lane(c.trial:position(),leg.p,1.4); valid=c.trial:safe_destination(leg.p,true,false)~=nil
  end,function(c,s,t)
   local leg=route[index]; if not leg or not valid then return end
   valid=c.trial:step(leg.p,.65,true,false); if t%4==0 then T.draw(c,shape) end
  end,function(c,s)
   local leg=route[index]; if not leg then return end
   if not valid or T.distance(c.trial:position(),leg.p)>1.5 then c.trial:stop(); T.interrupt(c,s,{T.rest(40)}); return end
   if leg.id then
    if c.trial:actor(leg.id) then c.trial:remove_actor(leg.id); B.heal(c,s,'harvestHealing',c.boss:get_maximum_health()*.02)
    else c.trial:say('The chain has nothing left to hold.'); T.interrupt(c,s,{T.rest(40)}) end
   else T.hit(c,shape,.9) end
  end),T.rest(10)})
 end
 T.append(seq,{T.rest(60)})
 return seq
end
local function reap(c,s)
 local line
 return {T.wait(30,function(c,s) local p=c.trial:position(); line=T.lane(p,T.rotate(p,c.trial.player:get_location(),0,6),1.5); c.trial:pose('draw'); T.sound(c,'BLOCK_CHAIN_PLACE',.6) end,
  function(c,s,t) if t%4==0 then T.draw(c,line) end end,
  function(c,s) if T.contains(line,c.trial.player:get_location()) then c.trial:push(line.p,-.25) end end),T.rest(30)}
end
return T.encounter{
 init=function(c,s) B.init(c,s); s.pairs=0; s.effigies={}; s.harvestHealing=c.boss:get_maximum_health()*.12; pair(c,s) end,
 passive=B.passive,
 choose=function(c,s)
  local alive=#T.alive(c,s.effigies)
  if s.phasePending and alive==0 and T.ready(s,'leap') then
   s.phasePending=false; local seq=M.move(c,s); T.append(seq,{T.wait(1,nil,nil,pair),T.rest(30)}); T.start(c,s,'leap',seq,M.cooldown)
  elseif alive==0 and s.pairs<3 and T.ready(s,'chain') then pair(c,s); T.start(c,s,'new_links',{T.rest(40)},0)
  elseif T.ready(s,'reap') then T.start(c,s,'reap',reap(c,s),360)
  elseif alive>0 and T.ready(s,'chain') then T.start(c,s,'chain',chain(c,s),440)
  else T.basic(c,s,'melee') end
 end
}
