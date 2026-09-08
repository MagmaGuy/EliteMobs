local function breaker(c,s)
 local shape
 return {T.wait(30,function(c,s) local p=c.trial:position(); local x,z=T.direction(p,c.trial.player:get_location()); shape=T.lane(p,T.offset(p,x*7,0,z*7),2); c.trial:pose('cast'); T.sound(c,'BLOCK_AMETHYST_BLOCK_RESONATE',.8) end,
  function(c,s,t) if t%3==0 then T.draw(c,shape); S.blade(c,shape) end end,
  function(c,s) c.trial:pose('swing'); if T.hit(c,shape,.8) then T.weak(c,s,40,.15) end end),T.rest(56)}
end
return T.encounter{
 init=function(c,s) S.init(c,s); T.spawnAlly(c,s,'cadet',3,'IRON_SWORD',3) end,
 passive=function(c,s) S.passive(c,s); T.allies(c,s) end,damaged=S.damage,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'blink') then s.phasePending=false; local ally=c.trial:actor('cadet'); T.start(c,s,'blink',M.move(c,s,ally and T.offset(ally:get_location(),2,0,0)),M.cooldown)
  elseif T.ready(s,'aegis') then T.start(c,s,'aegis',S.channel(c,s,'aegis',36,function(c,s) T.draw(c,T.circle(c.trial:position(),5),S.violet) end,
   function(c,s) S.ward(c,s,'boss',1,100); local ally=c.trial:actor('cadet'); if ally and T.distance(c.trial:position(),ally:get_location())<=5 then S.ward(c,s,'cadet',1,100) end end),440)
  elseif T.ready(s,'breaker') then T.start(c,s,'breaker',breaker(c,s),360)
  else S.basic(c,s) end
 end
}
