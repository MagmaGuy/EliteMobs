local function gate(c,s)
 local p; local opened=false
 return {T.wait(46,function(c,s)
  s.charges=s.charges-1; p=T.offset(c.trial:position(),-3,0,3); c.trial:pose('cast'); T.sound(c,'BLOCK_RESPAWN_ANCHOR_CHARGE',.7)
  opened=S.prop(c,'gate_anchor',p,2,'&cNether Gate Anchor','CRYING_OBSIDIAN')~=nil
 end,function(c,s,t)
  if not opened or not c.trial:actor('gate_anchor') then c.trial:remove_actor('gate_anchor'); S.recover(c,s,60); return end
  if t%4==0 then for i=1,3 do T.draw(c,T.circle(T.rotate(p,c.trial:position(),i*120,1.3),.5),S.fire) end end
 end,function(c,s)
  if c.trial:actor('gate_anchor') then c.trial:remove_actor('gate_anchor'); S.summon(c,s,'servitor',p,'BLAZE',200,'ember',{damage=.4,name='&cBound Nether Servitor'}) end
 end),T.rest(50)}
end
local function pact(c,s)
 return {T.wait(36,function(c,s) c.trial:pose('cast'); T.sound(c,'BLOCK_RESPAWN_ANCHOR_CHARGE',1.2) end,
  function(c,s,t) local ally=c.trial:actor('servitor'); if ally and t%3==0 then T.tether(c,c.trial:position(),ally:get_location(),S.fire); T.eye(c,c.trial:position()) end end,
  function(c,s) if c.trial:actor('servitor') then s.pactUntil=s.tick+100; S.ward(c,s,'servitor',1,100) end end),T.rest(60)}
end
return T.encounter{
 init=function(c,s) S.init(c,s,'STAFF'); s.servants={}; s.charges=2; s.pactUntil=0 end,
 passive=function(c,s)
  S.passive(c,s); S.servants(c,s)
  if s.servants.servitor then s.servants.servitor.multiplier=s.pactUntil>s.tick and 1.2 or 1 end
  if s.pactUntil>s.tick and s.tick%5==0 then T.draw(c,T.circle(c.trial:position(),1.3),S.fire) end
 end,
 damaged=function(c,s) if c.trial:damaged_actor()=='boss' and s.pactUntil>s.tick then c.event.multiply_damage_amount(1.25) end; S.damage(c,s) end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'blink') then s.phasePending=false; T.start(c,s,'blink',M.move(c,s),M.cooldown)
  elseif s.charges>0 and S.servantCount(c,s)==0 and T.ready(s,'gate') then T.start(c,s,'gate',gate(c,s),560)
  elseif c.trial:actor('servitor') and s.pactUntil<=s.tick and T.ready(s,'pact') then T.start(c,s,'pact',pact(c,s),440)
  else S.basic(c,s) end
 end
}
