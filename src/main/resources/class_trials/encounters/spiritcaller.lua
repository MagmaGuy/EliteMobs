local function echo(c,s,recovery)
 local chosen=C.weakest(c,s,false)[1]; local id=chosen and chosen.id; local p
 return C.channel(c,s,'echo',40,function(c,s) local actor=id and C.actor(c,id); if actor then T.tether(c,p,actor:get_location()); T.draw(c,T.circle(p,.8),C.water) end end,
  function(c,s)
   if id and C.actor(c,id) then
    C.heal(c,s,id,c.trial.matched_hit); c.trial:remove_actor('echo_focus')
    if C.focus(c,'echo_focus',p,1,'&eAncestral Echo','AMETHYST_BLOCK') then s.echo=p; s.echoId=id; s.echoAt=s.tick+60 end
   end
  end,{begin=function(c,s) p=T.copy(c.trial:position()) end,recovery=recovery or 50})
end
local function link(c,s)
 return {T.wait(32,function(c,s) c.trial:pose('cast'); T.sound(c,'BLOCK_NOTE_BLOCK_CHIME',1.3) end,
  function(c,s,t) if t%4==0 then for _,v in ipairs(C.living(c,s)) do T.tether(c,c.trial:position(),v.actor:get_location()) end end end,
  function(c,s) s.links={}; for _,v in ipairs(C.living(c,s)) do s.links[v.id]=true end; s.linkUntil=s.tick+120 end),T.rest(30)}
end
return T.encounter{
 init=function(c,s) C.init(c,s,2); s.links={}; s.linkUntil=0 end,
 passive=function(c,s)
  C.passive(c,s)
  if s.echo then
   local focus=c.trial:actor('echo_focus'); local actor=C.actor(c,s.echoId)
   if not focus then s.echo=nil
   elseif s.tick>=s.echoAt then if actor and T.distance(s.echo,actor:get_location())<=5 then C.heal(c,s,s.echoId,.5*c.trial.matched_hit) end; c.trial:remove_actor('echo_focus'); s.echo=nil
   elseif s.tick%5==0 then T.draw(c,T.circle(s.echo,5),C.water); if actor then T.tether(c,s.echo,actor:get_location(),C.water) end end
  end
  if s.linkUntil>s.tick and s.tick%5==0 then
   for id in pairs(s.links) do local actor=C.actor(c,id)
    if not actor or T.distance(c.trial:position(),actor:get_location())>7 then s.links[id]=nil; c.trial:say('That thread can carry no more.'); C.recover(c,s,50)
    else T.tether(c,c.trial:position(),actor:get_location()) end
   end
  end
 end,
 damaged=function(c,s)
  C.damage(c,s); local id=c.trial:damaged_actor()
  if id~='boss' and s.linkUntil>s.tick and s.links[id] then local damage=c.event.get_damage_amount(); c.event.set_damage_amount(damage*.7); c.trial:transfer_damage(damage*.3) end
 end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'flight') then
   s.phasePending=false; local p=c.trial:position(); local x,z=T.direction(p,c.trial.player:get_location()); local target=T.offset(p,-z*4,0,x*4)
   -- The last second of the channel's recovery also previews the flight; movement still waits the full 2.5 seconds.
   local seq=echo(c,s,30); T.append(seq,M.move(c,s,target)); T.lockout(seq,'echo',480); T.start(c,s,'flight',seq,M.cooldown)
  elseif s.healBudget>0 and #C.living(c,s)>0 and T.ready(s,'echo') then T.start(c,s,'echo',echo(c,s),480)
  elseif #C.living(c,s)>0 and T.ready(s,'link') then T.start(c,s,'link',link(c,s),440)
  else T.basic(c,s,'melee') end
 end
}
