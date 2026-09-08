local function heal(c,s,actor,amount)
 if not actor or s.healBudget<=0 then return end
 local actual=math.min(amount,s.healBudget,actor:get_maximum_health()-actor:get_health())
 if actual>0 then actor:restore_health(actual); s.healBudget=s.healBudget-actual end
end
local function sanctuary(c,s)
 local g
 return {T.wait(30,function(c,s) s.sanctuary=T.copy(c.trial:position()); g=T.circle(s.sanctuary,3.5); T.sound(c,'BLOCK_AMETHYST_BLOCK_CHIME',1.3); c.trial:pose('cast') end,
   function(c,s,t) if t%4==0 then T.draw(c,g,T.gold) end end,
   function(c,s) s.sanctuaryShape=g; s.sanctuaryUntil=s.tick+120; s.nextPulse=s.tick+20 end),T.rest(30)}
end
local function mend(c,s)
 local recipient
 return {T.wait(40,function(c,s)
   recipient=c.trial:actor('acolyte') or c.boss; s.channelDamage=0; s.channel=true; s.interrupted=false
   c.trial:pose('cast'); T.sound(c,'BLOCK_BEACON_AMBIENT',1.2)
 end,function(c,s,t)
   if s.channel and recipient:is_alive() and t%4==0 then T.tether(c,c.trial:position(),recipient:get_location()) end
 end,function(c,s)
   if s.channel and recipient:is_alive() then heal(c,s,recipient,c.trial.matched_hit*(recipient.uuid==c.boss.uuid and .5 or 1)) end
   s.channel=false
 end),T.rest(40)}
end
return T.encounter{
 init=function(c,s) s.healBudget=c.boss:get_maximum_health()*.12; s.sanctuaryUntil=0; T.spawnAlly(c,s,'acolyte',3,'IRON_SWORD',3) end,
 passive=function(c,s)
   T.allies(c,s)
   if s.sanctuaryUntil>s.tick then
     if s.tick%5==0 then T.draw(c,s.sanctuaryShape,T.gold); T.point(c,s.sanctuary,{particle='END_ROD',amount=1}) end
     if s.tick>=s.nextPulse then
       s.nextPulse=s.tick+20
       if T.contains(s.sanctuaryShape,c.trial:position()) then heal(c,s,c.boss,c.trial.matched_hit*.25) end
       local ally=c.trial:actor('acolyte'); if ally and T.contains(s.sanctuaryShape,ally:get_location()) then heal(c,s,ally,c.trial.matched_hit*.25) end
     end
   end
 end,
 choose=function(c,s)
   if s.phasePending and T.ready(s,'flight') then
     s.phasePending=false; local ally=c.trial:actor('acolyte'); local destination=ally and T.offset(ally:get_location(),2,0,0) or s.sanctuary
     local seq=M.move(c,s,destination); T.append(seq,mend(c,s)); T.append(seq,{T.wait(1,nil,nil,function(c,s) s.ready.mend=s.tick+280 end)}); T.start(c,s,'flight',seq,M.cooldown)
   elseif T.ready(s,'sanctuary') and s.healBudget>0 then T.start(c,s,'sanctuary',sanctuary(c,s),400)
   elseif T.ready(s,'mend') and s.healBudget>0 then T.start(c,s,'mend',mend(c,s),280)
   elseif T.ready(s,'flight') and s.sanctuary and T.distance(c.trial:position(),s.sanctuary)>3 then T.start(c,s,'flight',M.move(c,s,s.sanctuary),M.cooldown)
   else T.basic(c,s,'melee') end
 end,
 damaged=function(c,s)
   if c.trial:damaged_actor()=='boss' and s.channel then
     s.channelDamage=s.channelDamage+c.event.damage_amount
     if s.channelDamage>=2*c.trial.matched_hit then s.channel=false; s.interrupted=true; T.sound(c,'BLOCK_GLASS_BREAK',1.5); c.trial:say('Well timed. The prayer can wait.'); c.trial:pose('idle'); s.ready.mend=s.tick+320; T.interrupt(c,s,{T.rest(40)}) end
   end
 end
}
