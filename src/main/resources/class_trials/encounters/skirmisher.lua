local function run(c,s)
 local target,lane; local group=R.group(s,'running_fan')
 local function move(c,s)
  if T.distance(c.trial:position(),target)>.3 and not c.trial:step(target,.115,true,true) then
   c.trial:clear_projectiles(group); T.fizzle(c); T.interrupt(c,s,{T.rest(50)}); s.ready.fan=s.tick+280
  end
 end
 local seq={T.wait(26,function(c,s)
  local p=c.trial:position(); local x,z=T.direction(p,c.trial.player:get_location()); local side=s.phase==2 and -1 or 1
  target=T.offset(p,-z*8*side,0,x*8*side); lane=T.lane(p,target,.8); T.sound(c,'ENTITY_BREEZE_SLIDE',1.1)
 end,function(c,s,t) if t%4==0 then T.draw(c,lane,R.feather) end end),
 T.wait(20,nil,move)}
 T.append(seq,R.draw(c,s,{warn=30,lock=12,angles={-22.5,0,22.5},damage=.35,cap=.85,group=group,releaseOnly=true,
  frame=function(c,s,t) move(c,s); if t%5==0 then T.draw(c,lane,R.feather) end end}))
 T.append(seq,{T.wait(30,nil,move),R.flight(group,48),T.rest(50)})
 T.lockout(seq,'fan',280)
 return seq
end
return T.encounter{
 init=function(c,s) R.init(c,s,false) end, passive=R.passive,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'step') then s.phasePending=false; T.start(c,s,'step',M.move(c,s),M.cooldown)
  elseif T.ready(s,'hunt') and T.ready(s,'fan') then T.start(c,s,'hunt',run(c,s),360)
  elseif T.ready(s,'fan') then T.start(c,s,'fan',R.draw(c,s,{warn=30,lock=12,angles={-22.5,0,22.5},damage=.35,cap=.85,recovery=50}),280)
  else R.basic(c,s) end
 end
}
