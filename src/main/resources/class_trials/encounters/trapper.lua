local ids={'left_wire','right_wire'}
local function wires(c,s)
 local shapes
 return {T.wait(36,function(c,s)
  local p=c.trial.player:get_location(); local x,z=T.direction(c.trial:position(),p); shapes={}
  for _,side in ipairs({-2.5,2.5}) do shapes[#shapes+1]=T.lane(T.offset(p,-x*2.5-z*side,0,-z*2.5+x*side),T.offset(p,x*2.5-z*side,0,z*2.5+x*side),2) end
  c.trial:pose('cast'); T.sound(c,'BLOCK_TRIPWIRE_ATTACH',.7)
 end,function(c,s,t) if t%4==0 then for _,shape in ipairs(shapes) do T.draw(c,shape) end end end,
  function(c,s)
   s.wires=shapes; s.wiresUntil=s.tick+140; s.wireInside={}
   for i,id in ipairs(ids) do c.trial:remove_actor(id); R.prop(c,s,id,shapes[i].p,1,'&eTanglewire Anchor','TRIPWIRE_HOOK') end
  end),T.rest(30)}
end
local function zone(c,s)
 local cells,path; local caught=false
 local seq={T.wait(36,function(c,s)
  s.wiresPaused=true; local p=c.trial.player:get_location(); cells={T.circle(T.offset(p,-1.5,0,-1.5),.8),T.circle(T.offset(p,1.5,0,-1.5),.8),T.circle(T.offset(p,0,0,1.5),.8)}
  path=T.lane(T.offset(p,-2.5,0,0),T.offset(p,2.5,0,0),1.2); T.sound(c,'BLOCK_TRIPWIRE_ATTACH',1.2)
 end,function(c,s,t) if t%4==0 then for _,shape in ipairs(cells) do T.draw(c,shape) end; T.draw(c,path,R.feather) end end)}
 for i=1,3 do T.append(seq,{T.wait(26,nil,function(c,s,t)
  if t<20 and t%4==0 then T.draw(c,cells[i]); T.draw(c,path,R.feather) end
  if t==20 then T.sound(c,'BLOCK_TRIPWIRE_CLICK_ON',1+i*.2); if not caught and T.contains(cells[i],c.trial.player:get_location()) then caught=true; c.trial:slow_player(1,10) end end
 end)}) end
 T.append(seq,{T.rest(40)})
 T.append(seq,R.draw(c,s,{warn=36,lock=14,damage=.65,cap=.65,recovery=60}))
 T.append(seq,{T.wait(1,nil,nil,function(c,s) s.wiresPaused=false end)})
 return seq
end
return T.encounter{
 init=function(c,s) R.init(c,s,true); s.wiresUntil=0; s.triggerReady=0 end,
 passive=function(c,s)
  R.passive(c,s)
  if s.wiresUntil>s.tick then
   for i,id in ipairs(ids) do if c.trial:actor(id) then
    local inside=T.contains(s.wires[i],c.trial.player:get_location())
    if inside and not s.wireInside[i] and not s.wiresPaused and s.tick>=s.triggerReady then c.trial:slow_player(.25,20); s.triggerReady=s.tick+60; T.sound(c,'BLOCK_TRIPWIRE_CLICK_ON',.8) end
    s.wireInside[i]=inside; if s.tick%5==0 then T.draw(c,s.wires[i],s.wiresPaused and R.feather or T.amber) end
   end end
  elseif s.wires then for _,id in ipairs(ids) do c.trial:remove_actor(id) end; s.wires=nil end
 end,
 choose=function(c,s)
  if s.phasePending and s.wiresUntil<=s.tick and T.ready(s,'step') then s.phasePending=false; local seq=M.move(c,s); T.append(seq,wires(c,s)); T.lockout(seq,'wires',480); T.start(c,s,'step',seq,M.cooldown)
  elseif s.wiresUntil<=s.tick and T.ready(s,'wires') then T.start(c,s,'wires',wires(c,s),480)
  elseif T.ready(s,'zone') then T.start(c,s,'zone',zone(c,s),440)
  else R.basic(c,s) end
 end
}
