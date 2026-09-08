local ids={'left_cadet','right_cadet'}
local function breakLink(c,s)
 if s.brokenLinks>=2 then s.links={}; c.trial:say('Both promises stretched too far. Well read.'); T.interrupt(c,s,{T.rest(60,1.2)})
 else T.interrupt(c,s,{T.rest(20)}) end
end
local function cover(c,s)
 local seq=P.reform(c,s,ids,T.offset(c.trial:position(),0,0,-3),30,80,40)
 local begin=seq[1].begin; seq[1].begin=function(c,s) begin(c,s); s.cover=T.cone(c.trial:position(),T.offset(c.trial:position(),0,0,-10),8,110) end
 local finish=seq[1].finish; seq[1].finish=function(c,s) finish(c,s); s.coverUntil=s.tick+80 end
 return seq
end
return T.encounter{
 init=function(c,s) P.init(c,s,{{ids[1],'IRON_SWORD'},{ids[2],'IRON_SWORD'}}) end,
 passive=function(c,s) P.passive(c,s); if (s.coverUntil or 0)>s.tick and s.tick%5==0 then T.draw(c,s.cover,T.gold) end end,
 damaged=P.damage,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'steed') then s.phasePending=false; T.start(c,s,'steed',P.rescue(c,s,ids),M.cooldown)
  elseif #T.alive(c,ids)>0 and T.ready(s,'links') then T.start(c,s,'links',P.link(c,s,ids,{warn=30,duration=120,fraction=.5,range=7,onBreak=breakLink}),400)
  elseif #T.alive(c,ids)>0 and T.ready(s,'cover') then T.start(c,s,'cover',cover(c,s),480)
  elseif T.ready(s,'steed') and T.distance(c.trial:position(),c.trial.player:get_location())>7 then T.start(c,s,'steed',M.move(c,s),M.cooldown)
  else T.basic(c,s,'melee') end
 end
}
