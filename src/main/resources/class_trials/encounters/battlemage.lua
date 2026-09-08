local function cleave(c,s)
 local shape
 return {T.wait(30,function(c,s) c.trial:pose('cast'); T.sound(c,'BLOCK_AMETHYST_BLOCK_RESONATE',1.1) end,
  function(c,s,t) if not shape or t<18 then shape=T.cone(c.trial:position(),c.trial.player:get_location(),4,80); c.trial:face(c.trial.player:get_location(),8) end
   if t%3==0 then T.draw(c,shape); S.blade(c,shape) end end,
  function(c,s) c.trial:pose('swing'); if T.hit(c,shape,.85) then S.ward(c,s,'boss',1,60) end end),T.rest(56)}
end
return T.encounter{
 init=function(c,s) S.init(c,s); s.guardHits=0; s.guardUntil=0 end,
 passive=function(c,s) S.passive(c,s); if s.guardUntil>s.tick and s.guardHits>0 and s.tick%5==0 then T.eye(c,c.trial:position()) end end,
 damaged=function(c,s)
  if c.trial:damaged_actor()~='boss' then return end
  local damage=c.event.get_damage_amount(); local ward=s.wards.boss
  if s.guardUntil>s.tick and s.guardHits>0 and (not ward or ward.remaining<damage*.4) then
   c.event.multiply_damage_amount(.6); s.guardHits=s.guardHits-1
   if s.guardHits==0 then s.wards.boss=nil; S.recover(c,s,50,1.2) end
  else S.damage(c,s) end
 end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'blink') then s.phasePending=false; T.start(c,s,'blink',M.move(c,s,T.rotate(c.trial.player:get_location(),c.trial:position(),90,3)),M.cooldown)
  elseif T.ready(s,'guard') then T.start(c,s,'guard',{T.wait(28,function(c,s) c.trial:pose('guard') end,function(c,s,t) if t%4==0 then T.eye(c,c.trial:position()) end end,function(c,s) s.guardHits=2; s.guardUntil=s.tick+80 end),T.rest(20)},400)
  elseif T.ready(s,'cleave') then if not T.approach(c,s,3) then T.start(c,s,'cleave',cleave(c,s),320) end
  else S.basic(c,s) end
 end
}
