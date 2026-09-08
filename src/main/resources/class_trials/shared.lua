-- Shared geometry and cast bookkeeping. Encounters author their own moves and order.
local T = {}
local M = {}
function T.copy(p) return {x=p.x,y=p.y,z=p.z,world=p.world,yaw=p.yaw or 0,pitch=p.pitch or 0} end
function T.offset(p,x,y,z) local q=T.copy(p); q.x=q.x+x; q.y=q.y+y; q.z=q.z+z; return q end
function T.distance(a,b) return math.sqrt((a.x-b.x)^2+(a.z-b.z)^2) end
function T.direction(a,b) local d=T.distance(a,b); if d<0.001 then return 0,1 end; return (b.x-a.x)/d,(b.z-a.z)/d end
function T.circle(p,r,inner) return {kind='circle',p=T.copy(p),r=r,inner=inner or 0} end
function T.cone(p,target,r,degrees) local x,z=T.direction(p,target); return {kind='cone',p=T.copy(p),r=r,x=x,z=z,angle=math.rad(degrees/2)} end
function T.arc(p,target,r,inner,degrees) local g=T.cone(p,target,r,degrees); g.kind='arc'; g.inner=inner; return g end
function T.lane(p,target,width) local x,z=T.direction(p,target); return {kind='lane',p=T.copy(p),q=T.copy(target),r=width/2,x=x,z=z,length=T.distance(p,target)} end
function T.contains(g,p)
  if math.abs(p.y-g.p.y)>3 then return false end
  local x,z=p.x-g.p.x,p.z-g.p.z
  local d=math.sqrt(x*x+z*z)
  if g.kind=='circle' then return d<=g.r and d>=g.inner end
  if g.kind=='cone' or g.kind=='arc' then return d<=g.r and d>=(g.inner or 0) and (d<0.05 or (x*g.x+z*g.z)/d>=math.cos(g.angle)) end
  local along=x*g.x+z*g.z
  return along>=0 and along<=g.length and math.abs(x*g.z-z*g.x)<=g.r
end
local amber={particle='DUST',red=255,green=185,blue=65,amount=1}
local gold={particle='DUST',red=255,green=235,blue=150,amount=1}
T.amber=amber; T.gold=gold
function T.point(c,p,particle) c.world:spawn_particle_at_location(T.offset(p,0,.12,0),particle or amber,1) end
function T.draw(c,g,particle)
  if g.kind=='circle' then
    for i=0,27 do local a=i*math.pi*2/28; T.point(c,T.offset(g.p,math.cos(a)*g.r,0,math.sin(a)*g.r),particle) end
    if g.inner>0 then for i=0,23 do local a=i*math.pi*2/24; T.point(c,T.offset(g.p,math.cos(a)*g.inner,0,math.sin(a)*g.inner),particle) end end
  elseif g.kind=='cone' or g.kind=='arc' then
    local base=math.atan2(g.z,g.x)
    for i=0,18 do local a=base-g.angle+2*g.angle*i/18; T.point(c,T.offset(g.p,math.cos(a)*g.r,0,math.sin(a)*g.r),particle) end
    if g.kind=='arc' then
      for i=0,18 do local a=base-g.angle+2*g.angle*i/18; T.point(c,T.offset(g.p,math.cos(a)*g.inner,0,math.sin(a)*g.inner),particle) end
    else for i=0,8 do for _,a in ipairs({base-g.angle,base+g.angle}) do T.point(c,T.offset(g.p,math.cos(a)*g.r*i/8,0,math.sin(a)*g.r*i/8),particle) end end end
  else
    for i=0,18 do for _,side in ipairs({-1,1}) do T.point(c,T.offset(g.p,g.x*g.length*i/18+g.z*g.r*side,0,g.z*g.length*i/18-g.x*g.r*side),particle) end end
  end
end
function T.damageScale(c) return c.state.outgoing or 1 end
function T.hit(c,g,amount) return T.contains(g,c.trial.player:get_location()) and c.trial:damage(amount*T.damageScale(c)) end
function T.sound(c,name,pitch) c.boss:play_sound_at_self(name,.65,pitch or 1) end
function T.wait(ticks,begin,frame,finish) assert(ticks>=1); return {ticks=ticks,begin=begin,frame=frame,finish=finish} end
function T.rest(ticks,exposure)
  return T.wait(ticks,function(c,s) c.trial:pose('idle'); c.trial:stop(); s.exposure=exposure or 1 end,nil,function(c,s) s.exposure=1 end)
end
function T.warning(ticks,geometry,sound,pose)
  local g
  return T.wait(ticks,function(c,s) g=geometry(c,s); c.trial:pose(pose or 'cast'); if sound then T.sound(c,sound) end end,
    function(c,s,elapsed) if elapsed%4==0 then T.draw(c,g) end end)
end
function T.shot(c,s,kind,target,speed,damage,group,cap)
  local p=c.boss:get_eye_location()
  local x,z=T.direction(p,target)
  local origin=T.copy(p)
  local projectile=c.boss:summon_projectile(kind,origin,target,speed,{gravity=false,persistent=false,spawn_at_origin=true,track=false})
  if projectile then c.trial:own_projectile(projectile,damage*T.damageScale(c),group,cap*T.damageScale(c)) end
end
function T.fan(c,s,target,angles,damage,group,cap)
  local origin=c.boss:get_eye_location()
  local x,z=T.direction(origin,target)
  for _,degrees in ipairs(angles) do local a=math.rad(degrees); local dx=x*math.cos(a)-z*math.sin(a); local dz=x*math.sin(a)+z*math.cos(a)
    T.shot(c,s,'ARROW',T.offset(origin,dx*24,target.y-origin.y,dz*24),.9,damage,group,cap)
  end
  T.sound(c,'ENTITY_ARROW_SHOOT',1); c.trial:pose('idle')
end
function T.aimShot(c,s,options)
  local target
  local group=options.group or ('shot_'..s.tick)
  return {
    T.wait(options.windup or 26,function(c,s) target=T.offset(c.trial.player:get_location(),0,1,0); c.trial:pose((options.angles or options.kind=='ARROW') and 'draw' or 'cast'); T.sound(c,options.sound or ((options.angles or options.kind=='ARROW') and 'BLOCK_NOTE_BLOCK_HARP' or 'BLOCK_AMETHYST_BLOCK_CHIME'),.8) end,
      function(c,s,t)
        if t<(options.windup or 26)-(options.lock or 12) then target=T.offset(c.trial.player:get_location(),0,1,0); c.trial:face(target,5) end
        if t%4==0 then
          if options.angles then
            local origin=c.boss:get_location(); local x,z=T.direction(origin,target)
            for _,degrees in ipairs(options.angles) do local a=math.rad(degrees); local dx=x*math.cos(a)-z*math.sin(a); local dz=x*math.sin(a)+z*math.cos(a)
              T.draw(c,T.lane(origin,T.offset(origin,dx*24,0,dz*24),.35))
            end
          else T.draw(c,T.lane(c.boss:get_location(),T.offset(target,0,-1,0),.35)) end
        end
      end,function(c,s)
        if options.angles then T.fan(c,s,target,options.angles,options.damage,group,options.cap or options.damage)
        else T.shot(c,s,options.kind or 'SNOWBALL',target,options.speed or .7,options.damage,group,options.cap or options.damage); T.sound(c,options.kind=='ARROW' and 'ENTITY_ARROW_SHOOT' or 'ENTITY_BLAZE_SHOOT',1.3) end
      end),
    T.rest(options.recovery or 30)
  }
end
function T.melee(c,s,options)
  local g
  return {
    T.wait(options.windup or 16,function(c,s) c.trial:pose('draw'); T.sound(c,'BLOCK_WOODEN_BUTTON_CLICK_ON',.8) end,
      function(c,s,t)
        if t<(options.windup or 16)-8 then c.trial:face(c.trial.player:get_location(),5) end
        if not g or t<(options.windup or 16)-8 then g=T.cone(c.boss:get_location(),c.trial.player:get_location(),options.radius or 3,options.angle or 70) end
        if t%4==0 then T.draw(c,g) end
      end,function(c,s) c.trial:pose('swing'); T.sound(c,'ENTITY_PLAYER_ATTACK_SWEEP'); local hit=T.hit(c,g,options.damage or .45); if options.result then options.result(c,s,hit) end end),
    T.rest(options.recovery or 30,options.exposure)
  }
end
function T.append(a,b) for _,v in ipairs(b) do a[#a+1]=v end; return a end
function T.start(c,s,key,steps,cooldown)
  assert(type(steps)=='table' and #steps>0,'Missing authored timeline: '..key)
  s.cast={key=key,steps=steps,index=1,elapsed=0,cooldown=cooldown or 0}
end
function T.advance(c,s)
  local cast=s.cast; if not cast then return end
  local step=cast.steps[cast.index]
  if cast.elapsed==0 and step.begin then step.begin(c,s) end
  if s.cast~=cast then return end
  if step.frame then step.frame(c,s,cast.elapsed) end
  if s.cast~=cast then return end
  cast.elapsed=cast.elapsed+1
  if cast.elapsed>=step.ticks then
    if step.finish then step.finish(c,s) end
    if s.cast~=cast then return end
    cast.index=cast.index+1; cast.elapsed=0
    if cast.index>#cast.steps then s.ready[cast.key]=s.tick+cast.cooldown; s.cast=nil; c.trial:pose('idle') end
  end
end
function T.encounter(spec)
  assert(type(spec.choose)=='function' and type(spec.init)=='function','Encounter needs authored initialization and ordering')
  return {api_version=1,
    on_game_tick=function(c)
      local s=c.state; s.tick=c.trial:tick()
      if not s.initialized then s.initialized=true; s.ready={}; s.phase=1; s.exposure=1; s.nextChoice=40; c.boss:set_ai_enabled(false); spec.init(c,s) end
      if spec.passive then spec.passive(c,s) end
      if s.cast then T.advance(c,s); return end
      if s.phase==1 and c.boss:get_health()<=c.boss:get_maximum_health()*.5 then s.phase=2; s.nextChoice=s.tick+40; s.phasePending=true end
      if s.tick<s.nextChoice then return end
      spec.choose(c,s)
    end,
    on_boss_damaged_by_player=function(c)
      local s=c.state; if not s.initialized then return end
      if c.trial:is_transfer() then return end
      if (s.playerWeakUntil or 0)>s.tick then c.event.multiply_damage_amount(1-(s.playerWeakness or .15)) end
      if c.trial:damaged_actor()=='boss' then c.event.multiply_damage_amount(s.exposure or 1) end
      if spec.damaged then spec.damaged(c,s) end
    end
  }
end
function T.ready(s,key) return s.tick>=(s.ready[key] or 0) end
function T.approach(c,s,range)
  local p=c.trial:position(); local target=c.trial.player:get_location(); local d=T.distance(p,target)
  if math.abs(d-range)<1 then return false end
  local x,z=T.direction(p,target); local travel=math.min(3,math.abs(d-range)); if d<range then travel=-travel end
  local destination=T.offset(p,x*travel,0,z*travel)
  T.start(c,s,'position',{T.wait(12,nil,function(c,s) c.trial:face(target,4); c.trial:step(destination,.22,true,true) end),T.rest(4)},0)
  return true
end
function T.basic(c,s,kind)
  if T.approach(c,s,kind=='melee' and 2.5 or 9) then return end
  if not T.ready(s,'basic') then s.nextChoice=s.tick+5; return end
  if kind=='melee' then T.start(c,s,'basic',T.melee(c,s,{damage=.45,windup=16,recovery=18}),50)
  else T.start(c,s,'basic',T.aimShot(c,s,{kind=kind=='bow' and 'ARROW' or 'SNOWBALL',damage=.35,windup=20,lock=8,recovery=20,speed=.85}),60) end
end
function T.spawnAlly(c,s,id,offset,weapon,health,type)
  local p=T.offset(c.trial:position(),offset,0,3)
  local actor=c.trial:spawn_actor(id,p,health or 3,'&fSparring '..id,type or 'HUSK')
  if actor then actor:set_equipment('HAND',weapon or 'IRON_SWORD',{unbreakable=true}); s.allies=s.allies or {}; s.allies[id]={ready=s.tick+40,windup=nil,bow=weapon=='BOW'} end
  return actor
end
function T.allies(c,s)
  local close=0
  for id,state in pairs(s.allies or {}) do
    local actor=c.trial:actor(id)
    if actor then
      local p=actor:get_location(); local target=c.trial.player:get_location(); local d=T.distance(p,target)
      if (s.alliesPausedUntil or 0)>s.tick or (state.pausedUntil or 0)>s.tick then
        state.windup=nil
      elseif state.bow then
        if state.windup then
          if s.tick%4==0 then T.draw(c,T.lane(p,T.offset(state.target,0,-1,0),.3)) end
          if s.tick>=state.windup then c.trial:actor_arrow(id,state.target,.2*(state.damage or 1)); state.windup=nil; state.ready=s.tick+60 end
        elseif s.tick>=state.ready and d<=24 then
          state.target=T.offset(target,0,1,0); state.windup=s.tick+(state.tell or 24); actor:play_sound_at_self('BLOCK_NOTE_BLOCK_HARP',.4,1.2)
        end
      else
      if d<3.5 then close=close+1 end
      if state.windup then
        if s.tick%5==0 then T.draw(c,state.shape) end
        if s.tick>=state.windup then if T.contains(state.shape,target) then c.trial:actor_damage(id,.2*(state.damage or 1)) end; state.windup=nil; state.ready=s.tick+60 end
      elseif d<3 and close<=2 and s.tick>=state.ready then
        state.shape=T.cone(p,target,3,70); state.windup=s.tick+(state.tell or 20); actor:play_sound_at_self('BLOCK_WOODEN_BUTTON_CLICK_ON',.4,.8)
      elseif d>3 and s.tick%5==0 and close<2 then
        local x,z=T.direction(p,target); c.trial:actor_step(id,T.offset(target,-x*2.5,0,-z*2.5))
      end
      end
    end
  end
end
function T.lockout(steps,key,cooldown)
 return T.append(steps,{T.wait(1,nil,nil,function(c,s) s.ready[key]=s.tick+cooldown end)})
end

function T.alive(c,ids)
  local result={}; for _,id in ipairs(ids) do local actor=c.trial:actor(id); if actor then result[#result+1]={id=id,actor=actor} end end
  return result
end
function T.weak(c,s,ticks,fraction)
  s.playerWeakUntil=s.tick+ticks; s.playerWeakness=fraction or .15
end
function T.rotate(p,target,degrees,distance)
  local x,z=T.direction(p,target); local a=math.rad(degrees)
  return T.offset(p,(x*math.cos(a)-z*math.sin(a))*distance,0,(x*math.sin(a)+z*math.cos(a))*distance)
end
function T.budgetHit(c,s,g,amount,budget)
  local remaining=math.max(0,budget.cap-budget.spent)
  local hit=remaining>0 and T.hit(c,g,math.min(amount,remaining))
  if hit then budget.spent=budget.spent+math.min(amount,remaining) end
  return hit
end
function T.tether(c,a,b,particle)
  for i=0,16 do local q=T.copy(a); q.x=a.x+(b.x-a.x)*i/16; q.y=a.y+1+(b.y-a.y)*i/16; q.z=a.z+(b.z-a.z)*i/16; T.point(c,q,particle or T.gold) end
end

function T.interrupt(c,s,steps)
  local cast=s.cast
  if cast then T.start(c,s,cast.key,steps,cast.cooldown) end
end

function T.eye(c,p)
  for i=0,20 do local x=-.9+1.8*i/20; local z=.4*math.sin(math.pi*i/20)
    T.point(c,T.offset(p,x,0,z),T.gold); T.point(c,T.offset(p,x,0,-z),T.gold)
  end
  T.draw(c,T.circle(p,.15),T.gold)
end
