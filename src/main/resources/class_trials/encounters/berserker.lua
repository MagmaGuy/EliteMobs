local function cry(c,s)
  local edge
  return {
    T.wait(24,function(c,s) edge=T.circle(c.trial:position(),4,3.2); T.sound(c,'ENTITY_RAVAGER_ROAR',1.4) end,
      function(c,s,t) if t%4==0 then T.draw(c,edge) end end,
      function(c,s) if T.contains(edge,c.trial.player:get_location()) then c.trial.player:apply_potion_effect('SLOWNESS',10,0) end end),
    T.rest(30)
  }
end
local function rampage(c,s)
  local seq={T.wait(30,function(c,s) s.comboSpent=0; c.trial:pose('draw') end,
    function(c,s,t) if t%10==0 then T.sound(c,'BLOCK_NOTE_BLOCK_BASEDRUM',.6+t/30) end end)}
  for i=1,3 do
    local side=i%2==1 and -25 or 25
    local shape,advance
    T.append(seq,{
      T.wait(8,function(c,s) local p=c.trial:position(); local x,z=T.direction(p,c.trial.player:get_location()); advance=T.offset(p,x,0,z) end,
        function(c,s) c.trial:step(advance,.18,true,true) end,function(c,s) c.trial:stop() end),
      T.wait(18,function(c,s)
        local p=c.trial:position(); local target=c.trial.player:get_location(); c.trial:face(target,10)
        local x,z=T.direction(p,target); local a=math.rad(side)
        shape=T.cone(p,T.offset(p,x*math.cos(a)-z*math.sin(a),0,x*math.sin(a)+z*math.cos(a)),3.5,85)
      end,function(c,s,t) if t%4==0 then T.draw(c,shape) end end,
      function(c,s) c.trial:pose('swing'); local amount=math.min(.55,1.1-s.comboSpent); if amount>0 and T.hit(c,shape,amount) then s.comboSpent=s.comboSpent+amount end; T.sound(c,'ENTITY_PLAYER_ATTACK_SWEEP',.75) end)
    })
  end
  return T.append(seq,{T.rest(50,1.2)})
end
return T.encounter{
 init=function(c,s) s.comboSpent=0 end,
 choose=function(c,s)
   if s.phasePending and T.ready(s,'rampage') then s.phasePending=false; local seq=cry(c,s); T.append(seq,rampage(c,s)); T.append(seq,{T.wait(1,nil,nil,function(c,s) s.ready.cry=s.tick+280 end)}); T.start(c,s,'rampage',seq,320)
   elseif T.distance(c.trial:position(),c.trial.player:get_location())>5 and T.ready(s,'leap') then T.start(c,s,'leap',M.move(c,s),M.cooldown)
   elseif T.ready(s,'rampage') then T.start(c,s,'rampage',rampage(c,s),320)
   elseif T.ready(s,'cry') then T.start(c,s,'cry',cry(c,s),280)
   else T.basic(c,s,'melee') end
 end
}
