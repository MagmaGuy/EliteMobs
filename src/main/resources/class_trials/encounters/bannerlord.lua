local ids={'port_cadet','starboard_cadet'}
local function advance(c,s)
 local destination,lane
 return {T.wait(28,function(c,s) local p=c.trial:position(); destination=T.rotate(p,c.trial.player:get_location(),0,6); lane=T.lane(p,destination,3); T.sound(c,'ITEM_GOAT_HORN_SOUND_0',.7) end,
  function(c,s,t) if t%4==0 then T.draw(c,lane,T.gold) end end),
 T.wait(80,nil,function(c,s,t) if t%5==0 then T.draw(c,lane,T.gold) end; c.trial:step(destination,.12,true,true) end),T.rest(20)}
end
local function standard(c,s)
 return {T.wait(30,function(c,s) c.boss:set_equipment('OFF_HAND','WHITE_BANNER',{}); c.trial:pose('guard'); T.sound(c,'ITEM_ARMOR_EQUIP_LEATHER',.7) end,
  function(c,s,t) if t%4==0 then T.draw(c,T.circle(c.trial:position(),4),T.gold) end end,
  function(c,s) s.auraUntil=s.tick+160 end),T.rest(20)}
end
return T.encounter{
 init=function(c,s) P.init(c,s,{{ids[1],'IRON_SWORD'},{ids[2],'IRON_SWORD'}}); s.folded=true end,
 passive=function(c,s)
  P.aura(c,s,ids,c.trial:position(),4,1.2,s.auraUntil)
  if s.auraUntil>s.tick then s.folded=false
  elseif not s.folded then
   s.folded=true; c.boss:set_equipment('OFF_HAND','AIR',{}); s.alliesPausedUntil=s.tick+60
   T.start(c,s,'fold',T.lockout({T.rest(60,1.2)},'standard',440),0)
  end
  P.passive(c,s)
 end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'steed') and s.auraUntil<=s.tick then
   s.phasePending=false; local destination=T.rotate(c.trial:position(),c.trial.player:get_location(),0,5); T.start(c,s,'steed',M.move(c,s,destination),M.cooldown)
  elseif #T.alive(c,ids)>0 and s.auraUntil<=s.tick and T.ready(s,'standard') then T.start(c,s,'standard',standard(c,s),0)
  elseif s.auraUntil-s.tick>=130 and T.ready(s,'advance') then T.start(c,s,'advance',advance(c,s),360)
  else T.basic(c,s,'melee') end
 end
}
