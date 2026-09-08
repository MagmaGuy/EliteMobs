local function cache(c,s)
 local p
 return {T.wait(36,function(c,s) p=T.offset(c.trial:position(),2,0,0); c.trial:pose('cast'); T.sound(c,'BLOCK_BARREL_OPEN',.8) end,
  function(c,s,t) if t%4==0 then T.draw(c,T.circle(p,.8),T.gold) end end,
  function(c,s) c.trial:remove_actor('cache'); if R.prop(c,s,'cache',p,2,'&eAmmunition Cache','BARREL') then s.cache=p; s.cacheUntil=s.tick+160 end end),T.rest(20)}
end
local function reload(c,s)
 return {T.wait(40,function(c,s) s.reloading=true; s.pressure=0; c.trial:crossbow(false); c.trial:pose('cast'); T.sound(c,'ITEM_CROSSBOW_LOADING_START',.7) end,
  function(c,s,t)
   if not c.trial:actor('cache') or T.distance(c.trial:position(),s.cache)>3 then s.reloading=false; s.quickLoaded=false; T.interrupt(c,s,{T.rest(50,1.2)}); return end
   if t%8==0 then T.tether(c,c.trial:position(),s.cache,T.gold); T.sound(c,'ITEM_CROSSBOW_LOADING_MIDDLE',.7+t/50) end
  end,function(c,s) s.reloading=false; s.quickLoaded=true; T.sound(c,'ITEM_CROSSBOW_LOADING_END',1.3) end),T.rest(30)}
end
local function burst(c,s)
 local group=R.group(s,'repeater'); local seq={}; local recovery=s.quickLoaded and 40 or 60; s.quickLoaded=false
 for i=1,4 do T.append(seq,R.draw(c,s,{warn=i==1 and 32 or 13,lock=7,damage=.35,cap=1.05,group=group,turnLimit=i>1 and 35 or nil,releaseOnly=true,speed=1.1})) end
 T.append(seq,{R.flight(group,48),T.rest(recovery)})
 return seq
end
return T.encounter{
 init=function(c,s) R.init(c,s,true); s.cacheUntil=0 end,
 passive=function(c,s)
  R.passive(c,s)
  if s.cacheUntil>s.tick then
   if not c.trial:actor('cache') then s.cacheUntil=0; s.quickLoaded=false; if s.reloading then s.reloading=false; c.trial:say('Supply cut. You bought yourself time.'); T.interrupt(c,s,{T.rest(50,1.2)}) end
   elseif s.tick%8==0 then T.draw(c,T.circle(s.cache,.8),T.gold) end
  elseif s.cache then c.trial:remove_actor('cache'); s.cache=nil; s.quickLoaded=false end
 end,
 damaged=function(c,s)
  if c.trial:damaged_actor()=='boss' and s.reloading then s.pressure=s.pressure+c.event.damage_amount
   if s.pressure>=2*c.trial.matched_hit then s.reloading=false; s.quickLoaded=false; c.trial:say('The mechanism can wait. You did not.'); T.interrupt(c,s,{T.rest(50,1.2)}) end
  end
 end,
 choose=function(c,s)
  if s.phasePending and c.trial:actor('cache') and T.ready(s,'step') then s.phasePending=false; local seq=M.move(c,s,s.cache); T.append(seq,reload(c,s)); T.lockout(seq,'reload',520); T.start(c,s,'step',seq,M.cooldown)
  elseif s.cacheUntil<=s.tick and T.ready(s,'cache') then local seq=cache(c,s); T.append(seq,reload(c,s)); T.start(c,s,'cache',seq,520)
  elseif T.ready(s,'burst') then T.start(c,s,'burst',burst(c,s),320)
  else R.basic(c,s) end
 end
}
