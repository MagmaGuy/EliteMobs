local function broken(c,s)
 c.trial:say('Too far apart. You found the edge of my protection.'); T.interrupt(c,s,{T.rest(50,1.2)})
end
return T.encounter{
 init=function(c,s) P.init(c,s,{{'archer','BOW'}}) end,
 passive=P.passive, damaged=P.damage,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'steed') then
   s.phasePending=false; local seq=P.rescue(c,s,{'archer'}); T.append(seq,P.wall(c,s)); T.lockout(seq,'wall',440); T.start(c,s,'steed',seq,M.cooldown)
  elseif c.trial:actor('archer') and T.ready(s,'link') then T.start(c,s,'link',P.link(c,s,{'archer'},{duration=120,fraction=.7,range=7,breakTicks=20,onBreak=broken}),360)
  elseif T.ready(s,'wall') then T.start(c,s,'wall',P.wall(c,s),440)
  else T.basic(c,s,'melee') end
 end
}
