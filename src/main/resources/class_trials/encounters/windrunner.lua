local function route(c,s)
 local p,points; local progress=0; local group=R.group(s,'strafe')
 local function move(c,s)
  progress=math.min(100,progress+1); local index=math.min(#points,1+math.floor(progress/5))
  if T.distance(c.trial:position(),points[index])>.2 and not c.trial:step(points[index],.12,true,true) then
   c.trial:clear_projectiles(group); T.fizzle(c); T.interrupt(c,s,{T.rest(60)}); s.ready.volley=s.tick+340; return
  end
  if s.tick%5==0 then for i=1,#points-1 do T.point(c,points[i],R.feather) end end
 end
 local seq={T.wait(28,function(c,s)
  p=T.copy(c.trial:position()); points={}; local x,z=T.direction(p,c.trial.player:get_location()); local side=s.phase==2 and -1 or 1
  for i=0,20 do local u=i/20; points[#points+1]=T.offset(p,-z*8*u*side-x*2*math.sin(math.pi*u),0,x*8*u*side-z*2*math.sin(math.pi*u)) end
  s.routeEnd=points[#points]; T.sound(c,'ENTITY_BREEZE_SLIDE',1.2)
 end,function(c,s,t) if t%4==0 then for _,point in ipairs(points) do T.point(c,point,R.feather) end; for _,i in ipairs({5,10,15}) do T.draw(c,T.circle(points[i],.6),T.gold) end end end),T.wait(20,nil,move)}
 for i=1,3 do T.append(seq,R.draw(c,s,{warn=24,lock=10,damage=.4,cap=1,group=group,releaseOnly=true,frame=move})) end
 T.append(seq,{T.wait(8,nil,move),R.flight(group,48),T.rest(60)})
 T.lockout(seq,'volley',340)
 return seq
end
return T.encounter{
 init=function(c,s) R.init(c,s,false) end, passive=R.passive,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'step') then s.phasePending=false; T.start(c,s,'step',M.move(c,s,s.routeEnd,20),M.cooldown)
  elseif T.ready(s,'tailwind') and T.ready(s,'volley') then T.start(c,s,'tailwind',route(c,s),440)
  else R.basic(c,s) end
 end
}
