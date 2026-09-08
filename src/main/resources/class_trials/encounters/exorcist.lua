local function smite(c,s)
 local shape
 return {T.wait(32,function(c,s) local p=c.trial:position(); shape=T.lane(p,T.rotate(p,c.trial.player:get_location(),0,6),2); c.trial:pose('draw'); T.sound(c,'BLOCK_BELL_USE',1.2) end,
  function(c,s,t) if t%4==0 then T.draw(c,shape) end end,
  function(c,s) c.trial:pose('swing'); if T.hit(c,shape,.8) then C.heal(c,s,'attendant_1',c.trial.matched_hit) end end),T.rest(60)}
end
local function banish(c,s)
 local shape
 return {T.wait(30,function(c,s) shape=T.cone(c.trial:position(),c.trial.player:get_location(),4,80); T.sound(c,'BLOCK_BELL_USE',.7); c.trial:pose('cast') end,
  function(c,s,t) if t%4==0 then T.draw(c,shape,T.gold) end end,
  function(c,s) if T.contains(shape,c.trial.player:get_location()) then c.trial:push(shape.p,.25); T.weak(c,s,40,.15) end; s.alliesPausedUntil=s.tick+30 end),T.rest(30)}
end
return T.encounter{
 init=function(c,s) C.init(c,s,1) end, passive=C.passive, damaged=C.damage,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'flight') then s.phasePending=false; local seq=C.flight(c,s,'attendant_1'); T.append(seq,banish(c,s)); T.lockout(seq,'banish',400); T.start(c,s,'flight',seq,M.cooldown)
  elseif T.ready(s,'banish') then T.start(c,s,'banish',banish(c,s),400)
  elseif T.ready(s,'smite') then T.start(c,s,'smite',smite(c,s),360)
  else T.basic(c,s,'melee') end
 end
}
