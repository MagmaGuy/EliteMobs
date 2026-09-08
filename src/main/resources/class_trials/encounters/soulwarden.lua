local function communion(c,s)
 return {T.wait(36,function(c,s) c.trial:pose('cast'); T.sound(c,'BLOCK_NOTE_BLOCK_CHIME',.8) end,
  function(c,s,t) if t%4==0 then for _,v in ipairs(C.living(c,s)) do T.tether(c,c.trial:position(),v.actor:get_location()) end end end,
  function(c,s) s.links={}; for _,v in ipairs(C.living(c,s)) do s.links[v.id]={separated=0} end; s.communionUntil=s.tick+120; s.hadLinks=next(s.links)~=nil end),T.rest(30)}
end
local function intervention(c,s,id)
 s.interventions[id]=true
 return C.channel(c,s,'intervention',40,function(c,s) local actor=C.actor(c,id); if actor then T.tether(c,c.trial:position(),actor:get_location()); T.draw(c,T.circle(actor:get_location(),.8),T.gold) end end,
  function(c,s) if C.actor(c,id) then C.heal(c,s,id,c.trial.matched_hit); C.shield(c,s,id,c.trial.matched_hit,100) end end,{recovery=60,interruptRecovery=60})
end
return T.encounter{
 init=function(c,s) C.init(c,s,2); s.links={}; s.interventions={}; s.communionUntil=0 end,
 passive=function(c,s)
  C.passive(c,s)
  if s.communionUntil>s.tick and s.tick%5==0 then
   for id,link in pairs(s.links) do local actor=C.actor(c,id)
    if not actor then s.links[id]=nil
    else
     local distant=T.distance(c.trial:position(),actor:get_location())>6; link.separated=distant and link.separated+5 or 0
     if link.separated>=20 then s.links[id]=nil; T.sound(c,'BLOCK_CHAIN_BREAK',1.2)
     else T.tether(c,c.trial:position(),actor:get_location(),distant and T.amber or T.gold) end
    end
   end
   if s.hadLinks and next(s.links)==nil then s.hadLinks=false; s.communionUntil=0; c.trial:say('The burden is mine alone now. You saw the threads.'); C.recover(c,s,60,1.2) end
  end
 end,
 transferred=C.absorb,
 damaged=function(c,s)
  C.damage(c,s)
  local id=c.trial:damaged_actor(); if s.communionUntil<=s.tick or (id~='boss' and not s.links[id]) then return end
  local recipients={}; if id~='boss' then recipients[#recipients+1]='boss' end
  for linked in pairs(s.links) do if linked~=id and C.actor(c,linked) then recipients[#recipients+1]=linked end end
  if #recipients==0 then return end
  local damage=c.event.get_damage_amount(); c.event.set_damage_amount(damage*.6)
  for _,recipient in ipairs(recipients) do c.trial:transfer_to(recipient,damage*.4/#recipients) end
 end,
 choose=function(c,s)
  local threatened
  for _,v in ipairs(C.weakest(c,s,false)) do if not s.interventions[v.id] and v.actor:get_health()<v.actor:get_maximum_health()*.3 then threatened=v.id; break end end
  if s.phasePending and T.ready(s,'flight') then s.phasePending=false; local weakest=C.weakest(c,s,false); T.start(c,s,'flight',C.flight(c,s,threatened or (weakest[1] and weakest[1].id)),M.cooldown)
  elseif threatened then T.start(c,s,'intervention',intervention(c,s,threatened),0)
  elseif #C.living(c,s)>0 and T.ready(s,'communion') then T.start(c,s,'communion',communion(c,s),480)
  else T.basic(c,s,'melee') end
 end
}
