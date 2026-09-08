local function fault(c,s)
 local lane,sections; local hit=false
 return {T.wait(30,function(c,s)
  local p=c.trial:position(); local target=T.rotate(p,c.trial.player:get_location(),0,10); lane=T.lane(p,target,2); sections={}
  for i=0,2 do local a=T.offset(p,lane.x*10*i/3,0,lane.z*10*i/3); local b=T.offset(p,lane.x*10*(i+1)/3,0,lane.z*10*(i+1)/3); sections[#sections+1]=T.lane(a,b,2) end
  c.trial:pose('draw'); T.sound(c,'BLOCK_STONE_BREAK',.6)
 end,function(c,s,t) if t%4==0 then T.draw(c,lane) end end),
 T.wait(24,nil,function(c,s,t)
  if t%8==0 then local shape=sections[1+t/8]; T.draw(c,shape,{particle='DUST',red=130,green=120,blue=110,amount=1}); T.sound(c,'ENTITY_GENERIC_EXPLODE',.6+t/50)
   if not hit and T.hit(c,shape,.65) then hit=true; c.trial:launch_player(.25); c.trial:player_grace(30) end
  end
 end),T.rest(56)}
end
return T.encounter{
 init=B.init, passive=B.passive,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'leap') then
   s.phasePending=false; local seq=fault(c,s); T.append(seq,M.move(c,s,T.rotate(c.trial.player:get_location(),c.trial:position(),90,3))); T.lockout(seq,'fault',320); T.start(c,s,'leap',seq,M.cooldown)
  elseif T.ready(s,'fault') then T.start(c,s,'fault',fault(c,s),320)
  elseif T.ready(s,'quake') then T.start(c,s,'quake',B.slam(c,s,{warn=36,radius=4,damage=1,launch=true,recovery=56}),400)
  else T.basic(c,s,'melee') end
 end
}
