local function chain(c,s)
 local p; local budget={spent=0,cap=1.1}; local seq={T.wait(36,function(c,s) p=T.copy(c.trial:position()); c.trial:pose('draw'); T.sound(c,'BLOCK_NOTE_BLOCK_BASEDRUM',.5) end,
  function(c,s,t) if t%4==0 then T.draw(c,T.circle(p,2,1)) end end)}
 for _,radius in ipairs({2,4,6}) do
  local shape
  T.append(seq,{T.wait(16,function(c,s) shape=T.circle(p,radius,radius-1) end,
   function(c,s,t) if t%4==0 then T.draw(c,shape) end end,
   function(c,s) T.sound(c,'ENTITY_GENERIC_EXPLODE',.6+radius*.1); T.budgetHit(c,s,shape,.55,budget); T.draw(c,shape,B.red) end)})
 end
 T.append(seq,{T.rest(60)})
 return seq
end
return T.encounter{
 init=B.init, passive=B.passive,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'leap') then s.phasePending=false; local seq=M.move(c,s); T.append(seq,chain(c,s)); T.lockout(seq,'chain',440); T.start(c,s,'leap',seq,M.cooldown)
  elseif T.ready(s,'roar') then T.start(c,s,'roar',B.iron(c,s,28),400)
  elseif T.ready(s,'chain') then T.start(c,s,'chain',chain(c,s),440)
  else T.basic(c,s,'melee') end
 end
}
