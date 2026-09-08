local ids={'vanguard','rearguard'}
local function shout(c,s)
 return {T.wait(28,function(c,s) c.trial:pose('cast'); T.sound(c,'ITEM_GOAT_HORN_SOUND_0',.8) end,
  function(c,s,t) if t%4==0 then T.draw(c,T.circle(c.trial:position(),5),T.gold) end end,
  function(c,s)
   s.auraUntil=s.tick+100; s.aura=T.copy(c.trial:position())
   for i,id in ipairs(ids) do if s.allies[id] then s.allies[id].windup=nil; s.allies[id].ready=s.tick+(i-1)*30; s.allies[id].tell=24 end end
  end),T.rest(40)}
end
return T.encounter{
 init=function(c,s) P.init(c,s,{{ids[1],'IRON_SWORD'},{ids[2],'IRON_SWORD'}}) end,
 passive=function(c,s) P.aura(c,s,ids,s.aura or c.trial:position(),5,1.2,s.auraUntil); P.passive(c,s) end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'steed') then s.phasePending=false; local seq=M.move(c,s); T.append(seq,P.reform(c,s,ids,nil,24,60,40)); T.lockout(seq,'rally',400); T.start(c,s,'steed',seq,M.cooldown)
  elseif #T.alive(c,ids)>0 and T.ready(s,'shout') then T.start(c,s,'shout',shout(c,s),360)
  elseif #T.alive(c,ids)>0 and T.ready(s,'rally') then T.start(c,s,'rally',P.reform(c,s,ids,nil,24,60,40),400)
  else T.basic(c,s,'melee') end
 end
}
