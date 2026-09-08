local function line(c,s)
 local guard,arc; local swings=s.phase==2 and 2 or 3
 return {T.wait(32,function(c,s) local p=c.trial:position(); guard=T.cone(p,c.trial.player:get_location(),40,100); s.guard=nil; c.trial:pose('guard'); T.sound(c,'ITEM_SHIELD_BLOCK',.5) end,
  function(c,s,t) if t%4==0 then T.draw(c,T.cone(guard.p,T.offset(guard.p,guard.x,0,guard.z),4,100),T.gold) end end,
  function(c,s) s.guard=guard end),
 T.wait(100,nil,function(c,s,t)
  local interval=swings==2 and 45 or 32
  if t%interval==0 and math.floor(t/interval)<swings then arc=T.cone(c.trial:position(),T.offset(c.trial:position(),guard.x,0,guard.z),3.2,80); c.trial:pose('draw'); T.sound(c,'BLOCK_NOTE_BLOCK_BASEDRUM',.6) end
  if t%interval<20 and t%4==0 and arc then T.draw(c,arc) end
  if t%interval==20 and arc then c.trial:pose('swing'); T.hit(c,arc,.65); arc=nil end
 end,function(c,s) s.guard=nil end),T.rest(50)}
end
local function ground(c,s)
 local g
 return {T.wait(30,function(c,s) g=T.circle(c.trial:position(),4); c.trial:pose('guard'); T.sound(c,'BLOCK_STONE_PLACE',.6) end,
  function(c,s,t) if t%4==0 then T.draw(c,g,T.gold) end end,
  function(c,s) s.ground=g; s.groundUntil=s.tick+120 end),T.rest(50)}
end
return T.encounter{
 init=function(c,s) P.init(c,s); s.groundUntil=0 end,
 passive=function(c,s)
  local guarded=s.groundUntil>s.tick and T.contains(s.ground,c.trial:position())
  c.trial:control_guard(guarded)
  if guarded and s.tick%5==0 then T.draw(c,s.ground,T.gold) end
 end,
 damaged=function(c,s) if c.trial:damaged_actor()=='boss' and s.guard and T.contains(s.guard,c.trial.player:get_location()) then c.event.multiply_damage_amount(.3); T.sound(c,'ITEM_SHIELD_BLOCK',.6) end end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'steed') then s.phasePending=false; s.groundUntil=0; local seq=M.move(c,s); T.append(seq,line(c,s)); T.lockout(seq,'line',360); T.start(c,s,'steed',seq,M.cooldown)
  elseif T.ready(s,'line') then T.start(c,s,'line',line(c,s),360)
  elseif T.ready(s,'ground') then T.start(c,s,'ground',ground(c,s),440)
  else T.basic(c,s,'melee') end
 end
}
