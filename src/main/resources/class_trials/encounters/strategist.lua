local ids={'bow_cadet','shield_cadet'}
local function intact(c,s)
 local a=c.trial:actor(ids[1]); local b=c.trial:actor(ids[2]); local p=c.trial:position()
 return a and b and T.distance(p,a:get_location())<=5 and T.distance(p,b:get_location())<=5 and T.distance(a:get_location(),b:get_location())<=5
end
local function formation(c,s)
 local seq=P.reform(c,s,ids,c.trial:position(),36,40,30)
 T.append(seq,{T.wait(1,nil,nil,function(c,s)
  s.formationUntil=s.tick+120; s.formationBroken=false
  for i,id in ipairs(ids) do s.allies[id].windup=nil; s.allies[id].ready=s.tick+((s.phase==2 and 2-i or i-1)*30); s.allies[id].tell=24 end
 end),T.rest(30)})
 return seq
end
local function fallback(c,s)
 local anchor=T.offset(c.trial:position(),0,0,-4)
 local seq=P.reform(c,s,ids,anchor,28,60,30)
 local old=seq[2].frame; seq[2].frame=function(c,s,t) old(c,s,t); c.trial:step(anchor,.15,true,true) end
 return seq
end
return T.encounter{
 init=function(c,s) P.init(c,s,{{ids[1],'BOW'},{ids[2],'IRON_SWORD'}}); local shield=c.trial:actor(ids[2]); if shield then shield:set_equipment('OFF_HAND','SHIELD',{}) end; s.formationUntil=0 end,
 passive=function(c,s)
  s.formationActive=s.formationUntil>s.tick and intact(c,s)
  if s.formationUntil>s.tick and not s.formationActive and not s.formationBroken then
   s.formationBroken=true; s.formationUntil=0; s.alliesPausedUntil=s.tick+60
   c.trial:say('One corner moved. The advantage is yours.'); if s.cast then T.interrupt(c,s,{T.rest(60,1.2)}) else T.start(c,s,'broken_formation',{T.rest(60,1.2)},0) end
  end
  if s.formationActive and s.tick%5==0 then
   local a=c.trial:actor(ids[1]):get_location(); local b=c.trial:actor(ids[2]):get_location()
   T.tether(c,c.trial:position(),a); T.tether(c,c.trial:position(),b); T.tether(c,a,b)
  end
  s.outgoing=s.formationActive and 1.15 or 1
  for _,id in ipairs(ids) do if s.allies[id] then s.allies[id].damage=s.outgoing end end
  P.passive(c,s)
 end,
 damaged=function(c,s) if s.formationActive then c.event.multiply_damage_amount(.7) end end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'steed') then s.phasePending=false; s.formationUntil=0; local seq=fallback(c,s); T.append(seq,M.move(c,s)); T.lockout(seq,'fallback',400); T.start(c,s,'steed',seq,M.cooldown)
  elseif #T.alive(c,ids)==2 and T.ready(s,'formation') then T.start(c,s,'formation',formation(c,s),440)
  elseif #T.alive(c,ids)>0 and T.ready(s,'fallback') then s.formationUntil=0; T.start(c,s,'fallback',fallback(c,s),400)
  else T.basic(c,s,'melee') end
 end
}
