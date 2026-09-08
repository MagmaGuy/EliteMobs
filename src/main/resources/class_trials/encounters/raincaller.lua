local function mark(c,s)
 return {T.wait(30,function(c,s) s.stormCenter=T.copy(c.trial.player:get_location()); c.trial:pose('draw'); T.sound(c,'BLOCK_NOTE_BLOCK_FLUTE',1) end,
  function(c,s,t) if t%4==0 then T.draw(c,T.circle(s.stormCenter,6),R.feather) end end),T.rest(20)}
end
local function storm(c,s)
 local center,x,z,lanes; local group=R.group(s,'storm')
 local function strips(c,s,stage)
  local safe=stage==1 and 2 or (s.phase==2 and 3 or 1)
  T.draw(c,T.circle(center,6))
  for i=1,3 do T.draw(c,lanes[i],i==safe and R.feather or T.amber) end
 end
 local function wave(c,s,stage)
  local safe=stage==1 and 2 or (s.phase==2 and 3 or 1)
  local remaining=math.max(0,1.2-c.trial:group_damage(group))
  if remaining<=0 then return end
  for lane=1,3 do if lane~=safe then
   for _,along in ipairs({-3,-1,1,3}) do local side=(lane-2)*4
    local ground=T.offset(center,x*along-z*side,0,z*along+x*side)
    if T.distance(center,ground)<=5.8 then local from=T.offset(ground,0,7,0); c.trial:arrow(from,ground,.9,.2,group,1.2,{lifetime=14}) end
   end
  end end
 end
 return {T.wait(40,function(c,s)
  center=T.copy(s.stormCenter or c.trial.player:get_location()); x,z=T.direction(c.trial:position(),center); lanes={}
  for i=1,3 do local side=(i-2)*4; lanes[i]=T.lane(T.offset(center,-x*5-z*side,0,-z*5+x*side),T.offset(center,x*5-z*side,0,z*5+x*side),4) end
  c.trial:pose('draw'); T.sound(c,'ENTITY_ARROW_SHOOT',.6)
  local origin=c.boss:get_eye_location(); for i=-2,2 do c.trial:arrow(origin,T.offset(origin,i*.2,6,0),.7,0,group..'_lift',0,{lifetime=25,gravity=true}) end
 end,function(c,s,t) if t%4==0 then strips(c,s,1) end end),
 T.wait(80,nil,function(c,s,t)
  local stage=t<40 and 1 or 2
  if t==30 then T.sound(c,'BLOCK_NOTE_BLOCK_BELL',1.4); T.draw(c,lanes[s.phase==2 and 3 or 1],R.feather) end
  if t==40 then c.trial:clear_projectiles(group) end
  if t%5==0 then strips(c,s,stage) end
  if (t%12==0 and t<=68) or t==40 then wave(c,s,stage) end
 end,function(c,s) c.trial:clear_projectiles(group) end),T.rest(60)}
end
return T.encounter{
 init=function(c,s) R.init(c,s,false) end, passive=R.passive,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'step') then s.phasePending=false; local seq=M.move(c,s); T.append(seq,mark(c,s)); T.lockout(seq,'mark',400); T.start(c,s,'step',seq,M.cooldown)
  elseif T.ready(s,'mark') then T.start(c,s,'mark',mark(c,s),400)
  elseif T.ready(s,'storm') then T.start(c,s,'storm',storm(c,s),440)
  else R.basic(c,s) end
 end
}
