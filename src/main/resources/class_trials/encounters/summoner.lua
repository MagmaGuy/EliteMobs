local function portal(c,s)
 local p
 return {T.wait(40,function(c,s) s.charges=s.charges-1; p=T.offset(c.trial:position(),3,0,2); c.trial:pose('cast'); T.sound(c,'BLOCK_PORTAL_AMBIENT',1.4) end,
  function(c,s,t) if t%4==0 then T.draw(c,T.circle(p,1.2),S.violet); T.draw(c,T.circle(p,.5),T.gold) end end,
  function(c,s) S.summon(c,s,'familiar',p,'WOLF',240,'pounce',{damage=.4,name='&dArcane Familiar',onEnd=function(c,s) s.sigilUntil=0; c.trial:remove_actor('sigil') end}) end),T.rest(40)}
end
local function sigil(c,s)
 local p
 return {T.wait(32,function(c,s)
  local ally=c.trial:actor('familiar'); if ally then local a=c.trial:position(); local b=ally:get_location(); p=T.offset(a,(b.x-a.x)/2,0,(b.z-a.z)/2) end
  c.trial:pose('cast'); T.sound(c,'BLOCK_AMETHYST_BLOCK_RESONATE',1.3)
 end,function(c,s,t) if p and t%3==0 then T.draw(c,T.circle(p,5),T.gold); T.eye(c,p) end end,
 function(c,s) if p and c.trial:actor('familiar') and S.prop(c,'sigil',p,2,'&dBinding Sigil','AMETHYST_BLOCK') then s.sigilUntil=s.tick+120; S.ward(c,s,'familiar',1,120) end end),T.rest(40)}
end
return T.encounter{
 init=function(c,s) S.init(c,s); s.servants={}; s.charges=2; s.sigilUntil=0 end,
 passive=function(c,s)
  S.passive(c,s); S.servants(c,s)
  if s.summonWarning and s.cast and s.cast.key=='basic' then T.interrupt(c,s,{T.rest(30)}) end
  local familiar=s.servants.familiar; if familiar then familiar.multiplier=1 end
  if s.sigilUntil>0 then
   local prop=c.trial:actor('sigil'); local ally=c.trial:actor('familiar')
   if not prop or s.tick>=s.sigilUntil or not ally then
    local broken=not prop and s.tick<s.sigilUntil; s.sigilUntil=0; s.wards.familiar=nil; c.trial:remove_actor('sigil'); if broken then S.recover(c,s,60) end
   else
    if T.distance(prop:get_location(),ally:get_location())<=5 and c.trial:actor_los('sigil','familiar') then familiar.multiplier=1.15
    else s.wards.familiar=nil end
    if s.tick%5==0 then T.draw(c,T.circle(prop:get_location(),5),T.gold); T.tether(c,prop:get_location(),ally:get_location(),S.violet) end
   end
  end
 end,damaged=S.damage,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'blink') then s.phasePending=false; local prop=c.trial:actor('sigil'); T.start(c,s,'blink',M.move(c,s,prop and T.rotate(prop:get_location(),c.trial:position(),180,3)),M.cooldown)
  elseif s.charges>0 and S.servantCount(c,s)==0 and T.ready(s,'portal') then T.start(c,s,'portal',portal(c,s),520)
  elseif c.trial:actor('familiar') and s.sigilUntil==0 and T.ready(s,'sigil') then T.start(c,s,'sigil',sigil(c,s),440)
  elseif s.summonWarning then s.nextChoice=s.tick+5
  else S.basic(c,s) end
 end
}
