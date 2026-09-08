local function chill(c,s)
 local p
 return {T.wait(30,function(c,s) p=T.copy(c.trial.player:get_location()); c.trial:pose('cast'); T.sound(c,'BLOCK_SOUL_SAND_HIT',.6) end,
  function(c,s,t) if t%3==0 then T.draw(c,T.circle(p,3),S.ice) end end,
  function(c,s) if S.prop(c,'grave_candle',p,1,'&bGrave Candle','SOUL_LANTERN') then s.chill={shape=T.circle(p,3),expires=s.tick+80} end end),T.rest(40)}
end
return T.encounter{
 init=function(c,s) S.init(c,s,'STAFF'); S.corpses(c,s,3); s.healBudget=c.boss:get_maximum_health()*.06 end,
 passive=function(c,s)
  S.passive(c,s); S.servants(c,s)
  if s.chill then
   if s.tick>=s.chill.expires or not c.trial:actor('grave_candle') then c.trial:remove_actor('grave_candle'); s.chill=nil; c.trial:clear_player_slow(); s.playerWeakUntil=0
   elseif T.contains(s.chill.shape,c.trial.player:get_location()) then c.trial:slow_player(.2,6); T.weak(c,s,6,.15)
   else c.trial:clear_player_slow(); s.playerWeakUntil=0 end
   if s.chill and s.tick%4==0 then T.draw(c,s.chill.shape,S.ice) end
  end
 end,damaged=S.damage,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'blink') then s.phasePending=false; local id=S.nextCorpse(c,s); local prop=id and c.trial:actor(id); T.start(c,s,'blink',M.move(c,s,prop and T.offset(prop:get_location(),2,0,0)),M.cooldown)
  elseif S.nextCorpse(c,s) and S.servantCount(c,s)<2 and T.ready(s,'drain') then T.start(c,s,'drain',S.raise(c,s,46,{damage=.3,onHit=function(c,s) S.heal(c,s,c.boss:get_maximum_health()*.01) end}),440)
  elseif not s.chill and T.ready(s,'chill') then T.start(c,s,'chill',chill(c,s),400)
  else S.basic(c,s) end
 end
}
