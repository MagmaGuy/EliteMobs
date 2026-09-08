local function vessel(c,s)
 local p
 return S.channel(c,s,'vessel',40,function(c,s) if p then T.draw(c,T.circle(p,1.5),T.gold) end end,
  function(c,s)
   if S.prop(c,'phylactery',p,3,'&dPhylactery','DECORATED_POT') then s.vesselActive=true; c.trial:arm_survival('boss','phylactery',0,41) end
  end,{begin=function(c,s) s.vesselUsed=true; p=T.offset(c.trial:position(),3,0,2) end,recovery=40})
end
return T.encounter{
 init=function(c,s) S.init(c,s,'STAFF'); S.corpses(c,s,2); s.healBudget=c.boss:get_maximum_health()*.04; s.vesselUsed=false; s.vesselActive=false; s.rebirthBudget=0 end,
 passive=function(c,s)
  S.passive(c,s); S.servants(c,s)
  if c.trial:consume_survival('phylactery') then
   s.vesselActive=false; c.trial:remove_actor('phylactery'); s.rebirthBudget=c.boss:get_maximum_health()*.06
   T.start(c,s,'rebirth',{T.wait(40,function(c,s) c.trial:pose('guard'); T.sound(c,'BLOCK_RESPAWN_ANCHOR_DEPLETE',.8) end,
    function(c,s,t) if t%5==0 and c.boss:is_alive() then local amount=math.min(s.rebirthBudget,c.boss:get_maximum_health()*.0075); local before=c.boss:get_health(); c.boss:restore_health(amount); s.rebirthBudget=math.max(0,s.rebirthBudget-(c.boss:get_health()-before)); T.draw(c,T.circle(c.trial:position(),1.5),T.gold) end end),T.rest(40)},0)
  elseif s.vesselActive then
   local prop=c.trial:actor('phylactery')
   if not prop then s.vesselActive=false; c.trial:disarm_survival('phylactery'); S.recover(c,s,70,1.2)
   elseif s.tick%5==0 then T.tether(c,c.trial:position(),prop:get_location(),T.gold) end
  end
 end,damaged=S.damage,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'blink') then s.phasePending=false; local prop=c.trial:actor('phylactery'); T.start(c,s,'blink',M.move(c,s,prop and T.offset(prop:get_location(),2,0,0)),M.cooldown)
  elseif not s.vesselUsed then T.start(c,s,'vessel',vessel(c,s),0)
  elseif S.nextCorpse(c,s) and S.servantCount(c,s)<2 and T.ready(s,'coil') then T.start(c,s,'coil',S.raise(c,s,44,{damage=.3,onHit=function(c,s) S.heal(c,s,c.boss:get_maximum_health()*.01) end}),440)
  else S.basic(c,s) end
 end
}
