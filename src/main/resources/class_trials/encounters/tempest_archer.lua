local function cyclone(c,s)
 local paths,origin; local group=R.group(s,'cyclone')
 local seq={T.wait(36,function(c,s) c.trial:pose('draw'); T.sound(c,'ENTITY_BREEZE_WIND_BURST',.7) end,
  function(c,s,t)
   if not paths or t<22 then
    origin=c.boss:get_eye_location(); local target=T.offset(c.trial.player:get_location(),0,1,0); local x,z=T.direction(origin,target); paths={}
    for _,side in ipairs({-1,1}) do local points={}
     for i=0,39 do local u=i/39; local lateral=side*(1.8+3*math.sin(math.pi*u)); points[#points+1]=T.offset(origin,x*24*u-z*lateral,(target.y-origin.y)*u,z*24*u+x*lateral) end
     paths[#paths+1]=points
    end
   end
   if t%4==0 then for _,points in ipairs(paths) do for i=1,#points,2 do T.point(c,T.offset(points[i],0,-1.4,0),R.feather) end end end
  end,function(c,s)
   if T.distance(origin,c.boss:get_eye_location())>1 then T.sound(c,'BLOCK_FIRE_EXTINGUISH',1.2); return end
   for _,points in ipairs(paths) do c.trial:curve_arrow(points,.55,group,.9) end
   c.trial:pose('idle'); T.sound(c,'ENTITY_ARROW_SHOOT',1.3)
  end),R.flight(group,42,function(c,s)
   if c.trial:group_damage(group)==0 then s.markUntil=0; c.trial:say('You found the quiet air between them.') end
  end),T.rest(60)}
 return seq
end
return T.encounter{
 init=function(c,s) R.init(c,s,false) end,
 passive=function(c,s) R.passive(c,s); s.approachSpeed=s.markUntil>s.tick and .253 or .22 end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'step') then s.phasePending=false; T.start(c,s,'step',M.move(c,s),M.cooldown)
  elseif T.ready(s,'mark') then T.start(c,s,'mark',R.mark(c,s,28,120),440)
  elseif T.ready(s,'cyclone') then T.start(c,s,'cyclone',cyclone(c,s),360)
  else R.basic(c,s) end
 end
}
