local function prayer(c,s)
 local chosen
 local seq=C.channel(c,s,'prayer',40,function(c,s)
  for _,v in ipairs(chosen) do local actor=C.actor(c,v.id); if actor then T.tether(c,c.trial:position(),actor:get_location()) end end
 end,nil,{recovery=1,begin=function(c,s) chosen=C.weakest(c,s,false); if #chosen==0 then chosen={{id='boss',actor=c.boss}} end end})
 for i=1,3 do T.append(seq,{T.wait(8,function(c,s) local target=chosen[i]; if target then C.heal(c,s,target.id,.5*c.trial.matched_hit); T.sound(c,'BLOCK_NOTE_BLOCK_CHIME',.8+i*.2) end end)}) end
 T.append(seq,{T.rest(50)})
 return seq
end
return T.encounter{
 init=function(c,s) C.init(c,s,3) end, passive=C.passive, damaged=C.damage,
 choose=function(c,s)
  local weakest=C.weakest(c,s,false); local id=weakest[1] and weakest[1].id or 'boss'
  if s.phasePending and T.ready(s,'flight') then s.phasePending=false; local seq=C.flight(c,s,id); T.append(seq,prayer(c,s)); T.lockout(seq,'prayer',400); T.start(c,s,'flight',seq,M.cooldown)
  elseif s.healBudget>0 and T.ready(s,'prayer') then T.start(c,s,'prayer',prayer(c,s),400)
  elseif T.ready(s,'purify') then T.start(c,s,'purify',C.cleanse(c,s,id,28,60),360)
  else T.basic(c,s,'melee') end
 end
}
