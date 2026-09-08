local R={}
R.feather={particle='DUST',red=160,green=225,blue=205,amount=1}
function R.init(c,s,crossbow) s.crossbow=crossbow or false; s.markUntil=0; s.serial=0; s.outgoing=1; s.props={}; c.trial:crossbow(false) end
function R.group(s,key) s.serial=s.serial+1; return key..'_'..s.serial end
function R.passive(c,s)
 if s.markUntil>s.tick and s.tick%10==0 then T.eye(c,c.trial.player:get_location()) end
end
function R.mark(c,s,warn,duration,sound)
 return {T.wait(warn,function(c,s) c.trial:pose('draw'); T.sound(c,sound or 'BLOCK_NOTE_BLOCK_FLUTE',1.3) end,
  function(c,s,t) if t%4==0 then T.eye(c,c.trial.player:get_location()); T.draw(c,T.circle(c.trial:position(),1),R.feather) end end,
  function(c,s) s.markUntil=s.tick+duration end),T.rest(20)}
end
function R.flight(group,ticks,onResult)
 local result
 return T.untilDone(ticks,nil,function(c,s)
  local next=c.trial:projectile_result(group); if next and (not result or next.hit_player) then result=next end
 end,function(c,s)
  c.trial:clear_projectiles(group)
  if onResult then onResult(c,s,result) end
 end,function(c,s) return c.trial:projectile_count(group)==0 end)
end
function R.draw(c,s,o)
 local direction,target; local group=o.group or R.group(s,'shot'); local firstYaw
 local seq={T.wait(o.warn or 30,function(c,s)
  c.trial:pose(s.crossbow and 'cast' or 'draw'); c.trial:crossbow(true)
  firstYaw=o.turnLimit and s.lastShotYaw or c.trial:position().yaw or 0; T.sound(c,s.crossbow and 'ITEM_CROSSBOW_LOADING_START' or 'BLOCK_NOTE_BLOCK_HARP',.6)
 end,function(c,s,t)
  local origin=c.boss:get_eye_location()
  if not direction or t<(o.warn or 30)-(o.lock or 12) then
   local desired=o.target and o.target(c,s) or T.offset(c.trial.player:get_location(),0,1,0)
   if o.turnLimit then
    local yaw=math.deg(math.atan2(-(desired.x-origin.x),desired.z-origin.z)); local delta=(yaw-firstYaw+540)%360-180
    local a=math.rad(firstYaw+math.max(-o.turnLimit,math.min(o.turnLimit,delta)))
    desired=T.offset(origin,-math.sin(a)*24,desired.y-origin.y,math.cos(a)*24)
   end
   c.trial:face(desired,6)
   local yaw=c.trial:position().yaw or 0
   local a=math.rad(yaw); local d=math.max(1,T.distance(origin,desired)); direction={x=-math.sin(a),y=(desired.y-origin.y)/d,z=math.cos(a)}
  end
  target=T.offset(origin,direction.x*24,direction.y*24,direction.z*24)
  if t%4==0 then
   local ground=c.trial:position()
   for _,angle in ipairs(o.angles or {0}) do T.draw(c,T.lane(ground,T.rotate(ground,target,angle,24),.3)) end
  end
  if t==math.floor((o.warn or 30)/3) or t==math.floor(2*(o.warn or 30)/3) then T.sound(c,'BLOCK_NOTE_BLOCK_HARP',.8+t/50) end
  if o.frame then o.frame(c,s,t) end
 end,function(c,s)
  local origin=c.boss:get_eye_location(); local damage=(type(o.damage)=='function' and o.damage(c,s) or o.damage)*T.damageScale(c)
  s.lastShotYaw=math.deg(math.atan2(-direction.x,direction.z))
  for _,angle in ipairs(o.angles or {0}) do
   local a=math.rad(angle); local x=direction.x*math.cos(a)-direction.z*math.sin(a); local z=direction.x*math.sin(a)+direction.z*math.cos(a)
   c.trial:arrow(origin,T.offset(origin,x*24,direction.y*24,z*24),o.speed or .95,damage,group,o.cap and o.cap*T.damageScale(c) or damage,
    {lifetime=o.lifetime or 48,penetrate=o.penetrate or {}})
  end
  c.trial:crossbow(false); c.trial:pose('idle'); T.sound(c,s.crossbow and 'ITEM_CROSSBOW_SHOOT' or 'ENTITY_ARROW_SHOOT',1)
  if o.release then o.release(c,s,group) end
 end)}
 if not o.releaseOnly then
  T.append(seq,{R.flight(group,o.lifetime or 48,function(c,s,result)
   local hit=c.trial:group_damage(group)>0
   if o.result then o.result(c,s,hit,result) end
   s.shotExposure=not hit and o.missExposure or 1
  end),T.wait(o.recovery or 50,function(c,s) c.trial:stop(); c.trial:pose(s.crossbow and 'cast' or 'idle'); s.exposure=s.shotExposure or 1; if s.crossbow then T.sound(c,'ITEM_CROSSBOW_LOADING_MIDDLE',.7) end end,
   nil,function(c,s) s.exposure=1; c.trial:pose('idle') end)})
 end
 return seq,group
end
function R.basic(c,s)
 if T.distance(c.trial:position(),c.trial.player:get_location())>12 and T.approach(c,s,9) then return end
 if not T.ready(s,'basic') then s.nextChoice=s.tick+5; return end
 T.start(c,s,'basic',R.draw(c,s,{warn=s.crossbow and 28 or 22,lock=10,damage=.35,recovery=s.crossbow and 60 or 25,lifetime=40}),60)
end
function R.prop(c,s,id,p,hits,name,material)
 local ground=c.trial:ground(p); if not ground then return nil end
 local actor=c.trial:spawn_actor(id,ground,hits,name,'ARMOR_STAND')
 if actor then actor:set_equipment('HEAD',material,{}); actor:set_equipment('CHEST','LEATHER_CHESTPLATE',{}); s.props[id]=true end
 return actor
end
function R.step(c,s) return M.move(c,s) end
