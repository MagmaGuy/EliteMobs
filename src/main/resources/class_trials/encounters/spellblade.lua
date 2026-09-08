local function cuts(c,s)
 local steps={}; local budget={cap=1,spent=0}
 for i=1,3 do local shape
  steps[#steps+1]=T.wait(i==1 and 30 or 16,function(c,s) c.trial:pose('cast'); T.sound(c,'BLOCK_AMETHYST_BLOCK_CHIME',.8+i*.2) end,
   function(c,s,t) if not shape or t<(i==1 and 30 or 16)-9 then local p=c.trial:position(); local x,z=T.direction(p,c.trial.player:get_location()); shape=T.lane(p,T.offset(p,x*5,0,z*5),1.4); c.trial:face(c.trial.player:get_location(),8) end
    if t%3==0 then T.draw(c,shape); S.blade(c,shape) end end,
   function(c,s) c.trial:pose('swing'); T.budgetHit(c,s,shape,.4*(s.warUntil>s.tick and 1.1 or 1),budget) end)
 end
 steps[#steps+1]=T.wait(1,nil,nil,function(c,s) s.warUntil=0; s.wards.boss=nil; s.approachSpeed=.22 end)
 steps[#steps+1]=T.rest(60,1.2); return steps
end
return T.encounter{
 init=function(c,s) S.init(c,s); s.warUntil=0 end,
 passive=function(c,s) S.passive(c,s); s.approachSpeed=s.warUntil>s.tick and .253 or .22 end,
 damaged=S.damage,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'blink') then s.phasePending=false; T.start(c,s,'blink',M.move(c,s),M.cooldown)
  elseif T.ready(s,'warcasting') then T.start(c,s,'warcasting',{T.wait(28,function(c,s) c.trial:pose('cast') end,function(c,s,t) if t%4==0 then T.draw(c,T.circle(c.trial:position(),1.4),S.violet) end end,function(c,s) s.warUntil=s.tick+100; S.ward(c,s,'boss',1,100) end),T.rest(12)},440)
  elseif T.ready(s,'aether') then if not T.approach(c,s,3.5) then T.start(c,s,'aether',cuts(c,s),360) end
  else S.basic(c,s) end
 end
}
