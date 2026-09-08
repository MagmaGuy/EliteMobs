local function sever(c,s,opening)
 s.linkUntil=0; s.sharedWard=0; s.separated=0
 if opening then T.sound(c,'BLOCK_GLASS_BREAK',1.4); c.trial:say('A bond still needs someone at each end.'); S.recover(c,s,60,1.2) end
end
local function lantern(c,s)
 local p
 return {T.wait(40,function(c,s) s.charges=s.charges-1; p=T.offset(c.trial:position(),3,0,3); c.trial:pose('cast'); T.sound(c,'BLOCK_AMETHYST_BLOCK_RESONATE',1.5) end,
  function(c,s,t) if t%3==0 then T.draw(c,T.circle(p,1.2),S.ice); T.point(c,T.offset(p,0,1,0),T.gold) end end,
  function(c,s)
   if S.prop(c,'lantern',p,2,'&bEidolon Lantern','SOUL_LANTERN') then
    local actor=S.summon(c,s,'eidolon',T.offset(p,1.5,0,0),'VEX',240,'pass',{damage=.35,name='&bBound Eidolon',onEnd=function(c,s) c.trial:remove_actor('lantern'); sever(c,s,true) end})
    if not actor then c.trial:remove_actor('lantern') end
   end
  end),T.rest(50)}
end
return T.encounter{
 init=function(c,s) S.init(c,s); s.servants={}; s.charges=2; s.linkUntil=0; s.sharedWard=0; s.separated=0 end,
 passive=function(c,s)
  S.passive(c,s); S.servants(c,s)
  if s.servants.eidolon and not c.trial:actor('lantern') then S.endServant(c,s,'eidolon') end
  if s.linkUntil>0 then
   local ally=c.trial:actor('eidolon')
   if not ally or s.tick>=s.linkUntil then sever(c,s,false)
   else
    if T.distance(c.trial:position(),ally:get_location())>6 then s.separated=s.separated+1 else s.separated=0 end
    if s.separated>=20 then sever(c,s,true)
    elseif s.tick%4==0 then T.tether(c,c.trial:position(),ally:get_location(),S.ice); if s.sharedWard>0 then T.draw(c,T.circle(ally:get_location(),1),T.gold) end end
   end
  end
 end,
 damaged=function(c,s)
  local id=c.trial:damaged_actor(); S.damage(c,s)
  if s.linkUntil>s.tick and (id=='boss' or id=='eidolon') and c.trial:actor('eidolon') then
   local damage=c.event.get_damage_amount(); local absorbed=math.min(damage,s.sharedWard); s.sharedWard=s.sharedWard-absorbed; damage=damage-absorbed
   local transfer=damage*.4; c.event.set_damage_amount(damage-transfer)
   if transfer>0 then c.trial:transfer_to(id=='boss' and 'eidolon' or 'boss',transfer) end
  end
 end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'blink') then s.phasePending=false; T.start(c,s,'blink',M.move(c,s),M.cooldown)
  elseif s.charges>0 and S.servantCount(c,s)==0 and T.ready(s,'lantern') then T.start(c,s,'lantern',lantern(c,s),520)
  elseif c.trial:actor('eidolon') and s.linkUntil==0 and T.ready(s,'essence') then
   T.start(c,s,'essence',{T.wait(34,function(c,s) c.trial:pose('cast'); T.sound(c,'BLOCK_AMETHYST_BLOCK_CHIME',1.6) end,
    function(c,s,t) local ally=c.trial:actor('eidolon'); if ally and t%4==0 then T.tether(c,c.trial:position(),ally:get_location(),S.ice) end end,
    function(c,s) if c.trial:actor('eidolon') then s.linkUntil=s.tick+120; s.sharedWard=c.trial.matched_hit; s.separated=0 end end),T.rest(40)},440)
  else S.basic(c,s) end
 end
}
