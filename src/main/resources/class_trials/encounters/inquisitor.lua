local function expose(c,s)
 return {T.wait(24,function(c,s) c.trial:pose('cast'); T.sound(c,'BLOCK_ENCHANTMENT_TABLE_USE',.7) end,
  function(c,s,t) if t%4==0 then T.eye(c,c.trial.player:get_location()) end end,
  function(c,s) s.markUntil=s.tick+120 end),T.rest(20)}
end
local function sentence(c,s)
 local lane
 return {T.wait(36,function(c,s) c.trial:pose('draw'); T.sound(c,'BLOCK_ANVIL_PLACE',1.3) end,
  function(c,s,t)
   if not lane or t<22 then local p=c.trial:position(); local target=T.rotate(p,c.trial.player:get_location(),0,7); lane=T.lane(p,target,1.2); c.trial:face(target,5) end
   if t%4==0 then T.draw(c,lane) end
  end,function(c,s)
   c.trial:pose('swing'); T.sound(c,'ENTITY_PLAYER_ATTACK_SWEEP',.8)
   local low=c.trial.player:get_health()<c.trial.player:get_maximum_health()*.35
   local damage=math.min(1.2,(low and 1.05 or .9)*T.damageScale(c))/T.damageScale(c)
   if not T.hit(c,lane,damage) then s.markUntil=0; c.trial:say('No opening. No sentence.') end
  end),T.rest(60)}
end
return T.encounter{
 init=function(c,s) P.init(c,s); s.markUntil=0 end,
 passive=function(c,s) s.outgoing=s.markUntil>s.tick and 1.15 or 1; if s.markUntil>s.tick and s.tick%10==0 then T.eye(c,c.trial.player:get_location()) end end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'steed') then s.phasePending=false; local seq=M.move(c,s); T.append(seq,expose(c,s)); T.append(seq,sentence(c,s)); T.lockout(seq,'mark',360); T.lockout(seq,'sentence',320); T.start(c,s,'steed',seq,M.cooldown)
  elseif T.ready(s,'mark') then T.start(c,s,'mark',expose(c,s),360)
  elseif T.ready(s,'sentence') then T.start(c,s,'sentence',sentence(c,s),320)
  else T.basic(c,s,'melee') end
 end
}
