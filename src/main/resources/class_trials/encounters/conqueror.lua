local function press(c,s)
 local seq={T.wait(24,function(c,s) c.boss:set_equipment('HAND','IRON_AXE',{}); c.trial:pose('draw'); T.sound(c,'ENTITY_RAVAGER_STEP',.6) end)}
 local budget={spent=0,cap=1.1}
 for i=1,3 do
  local target,arc
  T.append(seq,{T.wait(8,function(c,s) target=T.rotate(c.trial:position(),c.trial.player:get_location(),0,1.5) end,
   function(c,s) c.trial:step(target,.2,true,true) end),
  T.wait(18,function(c,s) arc=T.cone(c.trial:position(),T.rotate(c.trial:position(),c.trial.player:get_location(),i%2==1 and -18 or 18,4),3.5,85); c.trial:pose('draw'); T.sound(c,'BLOCK_NOTE_BLOCK_BASEDRUM',.7+i*.1) end,
   function(c,s,t) if t%4==0 then T.draw(c,arc) end end,
   function(c,s) c.trial:pose('swing'); T.budgetHit(c,s,arc,.55,budget) end)})
 end
 T.append(seq,{T.rest(50,1.2),T.wait(1,nil,nil,function(c,s) c.boss:set_equipment('HAND','IRON_SWORD',{}) end)})
 return seq
end
return T.encounter{
 init=function(c,s) P.init(c,s) end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'steed') then s.phasePending=false; local seq=M.move(c,s); T.append(seq,P.roar(c,s)); T.lockout(seq,'roar',360); T.start(c,s,'steed',seq,M.cooldown)
  elseif T.ready(s,'roar') then T.start(c,s,'roar',P.roar(c,s),360)
  elseif T.ready(s,'press') then T.start(c,s,'press',press(c,s),320)
  else T.basic(c,s,'melee') end
 end
}
