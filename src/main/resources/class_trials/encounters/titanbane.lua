local function anchor(c,s)
 local destination,line,stake; local hit=false
 return {T.wait(40,function(c,s)
  local construct=c.trial:actor('construct'); if not construct then return end
  local p=construct:get_location(); local x,z=T.direction(p,c.trial.player:get_location()); destination=T.offset(p,x*4,0,z*4)
  line=T.lane(p,T.offset(p,x*8,0,z*8),2); stake=T.offset(destination,z*2,0,-x*2)
  c.trial:pose('cast'); T.sound(c,'ENTITY_IRON_GOLEM_STEP',.6)
 end,function(c,s,t) if line and t%4==0 then T.draw(c,line); T.draw(c,T.circle(stake,.7),T.gold) end end),
 T.wait(20,nil,function(c,s,t)
  local construct=c.trial:actor('construct'); if not construct or not destination then return end
  c.trial:actor_step('construct',destination,.3,false)
  if not hit and T.contains(line,c.trial.player:get_location()) and T.distance(construct:get_location(),c.trial.player:get_location())<1.5 then hit=c.trial:actor_damage('construct',.45) end
 end,function(c,s)
  local construct=c.trial:actor('construct'); if not construct or not stake then return end
  construct:set_velocity_vector({x=0,y=0,z=0}); c.trial:remove_actor('chain_stake')
  if B.prop(c,'chain_stake',stake,2,'&eBreakable Chain Stake','IRON_BLOCK') then s.stake=stake; s.anchorUntil=s.tick+80; s.anchorAlive=true end
 end),T.rest(30)}
end
local function killer(c,s)
 local line
 return {T.wait(36,function(c,s)
  local construct=c.trial:actor('construct'); local p=c.trial:position()
  local target=construct and construct:get_location() or c.trial.player:get_location()
  line=T.lane(p,T.rotate(p,target,0,10),1.4); c.boss:set_equipment('HAND','IRON_SPEAR',{}); c.trial:pose('draw'); T.sound(c,'BLOCK_CHAIN_PLACE',.7)
 end,function(c,s,t) if t%4==0 then T.draw(c,line) end end,
  function(c,s) c.trial:pose('swing'); T.hit(c,line,.9); local construct=c.trial:actor('construct'); if construct then construct:set_velocity_vector({x=0,y=0,z=0}); T.draw(c,T.circle(construct:get_location(),1.2),T.gold) end end),T.rest(60)}
end
return T.encounter{
 init=function(c,s) B.init(c,s); c.trial:spawn_actor('construct',T.offset(c.trial:position(),5,0,2),4,'&fTraining Construct','IRON_GOLEM'); s.anchorUntil=0 end,
 passive=function(c,s)
  B.passive(c,s)
  if s.anchorAlive then
   local construct=c.trial:actor('construct')
   if s.tick>=s.anchorUntil or not construct then c.trial:remove_actor('chain_stake'); s.anchorAlive=false
   elseif not c.trial:actor('chain_stake') then
    s.anchorAlive=false; c.trial:remove_actor('chain_stake'); s.ready.killer=s.tick+460
    c.trial:say('Anchor broken. You have the opening.'); if s.cast then T.interrupt(c,s,{T.rest(60,1.25)}) else T.start(c,s,'broken_anchor',{T.rest(60,1.25)},0) end
   elseif s.tick%5==0 then T.tether(c,s.stake,construct:get_location(),T.gold) end
  end
 end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'leap') then s.phasePending=false; local construct=c.trial:actor('construct'); local target=construct and T.rotate(construct:get_location(),c.trial:position(),180,4) or nil; T.start(c,s,'leap',M.move(c,s,target),M.cooldown)
  elseif c.trial:actor('construct') and not s.anchorAlive and T.ready(s,'anchor') then T.start(c,s,'anchor',anchor(c,s),440)
  elseif T.ready(s,'killer') then T.start(c,s,'killer',killer(c,s),400)
  else T.basic(c,s,'melee') end
 end
}
