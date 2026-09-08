local S={}
S.violet={particle='DUST',red=185,green=130,blue=255,amount=1}
S.ice={particle='DUST',red=165,green=225,blue=255,amount=1}
S.fire={particle='DUST',red=255,green=110,blue=45,amount=1}
function S.init(c,s,weapon)
 c.trial:magic_weapon(weapon or 'WAND'); s.wards={}; s.groups={}
end
function S.actor(c,id) return id=='boss' and c.boss or c.trial:actor(id) end
function S.prop(c,id,p,hits,name,head)
 local ground=c.trial:ground(p); if not ground then return nil end
 local actor=c.trial:spawn_actor(id,ground,hits,name,'ARMOR_STAND')
 if actor then actor:set_equipment('HEAD',head or 'AMETHYST_BLOCK',{}); actor:set_equipment('CHEST','LEATHER_CHESTPLATE',{}) end
 return actor
end
function S.recover(c,s,ticks,exposure)
 s.channel=nil
 if s.cast then T.interrupt(c,s,{T.rest(ticks,exposure)}) else T.start(c,s,'spell_recovery',{T.rest(ticks,exposure)},0) end
end
function S.ward(c,s,id,hits,ticks,onEnd)
 if S.actor(c,id) then s.wards[id]={remaining=hits*c.trial.matched_hit,expires=s.tick+ticks,onEnd=onEnd} end
end
function S.absorb(c,s)
 local id=c.trial:damaged_actor(); local ward=s.wards[id]
 if not ward or ward.expires<=s.tick then return 0 end
 local damage=c.event.get_damage_amount(); local absorbed=math.min(damage,ward.remaining)
 ward.remaining=ward.remaining-absorbed; c.event.set_damage_amount(damage-absorbed)
 if ward.remaining<=.001 then s.wards[id]=nil; T.sound(c,'BLOCK_GLASS_BREAK',1.2); if ward.onEnd then ward.onEnd(c,s,'broken') end end
 return absorbed
end
function S.damage(c,s)
 if c.trial:damaged_actor()=='boss' and s.channel then
  s.channel.damage=s.channel.damage+c.event.get_damage_amount()
  if s.channel.damage>=s.channel.limit*c.trial.matched_hit then
   local channel=s.channel; s.channel=nil; if channel.cancel then channel.cancel(c,s) end
   T.sound(c,'BLOCK_GLASS_BREAK',.8); c.trial:say('Good. Preparation is an opening.'); S.recover(c,s,channel.recovery or 60,channel.exposure)
  end
 end
 S.absorb(c,s)
end
function S.passive(c,s)
 for id,ward in pairs(s.wards) do
  local actor=S.actor(c,id)
  if not actor then s.wards[id]=nil
  elseif s.tick>=ward.expires then s.wards[id]=nil; if ward.onEnd then ward.onEnd(c,s,'expired') end
  elseif s.tick%6==0 then T.draw(c,T.circle(actor:get_location(),1),S.violet) end
 end
 for group,expires in pairs(s.groups) do if s.tick>=expires then c.trial:clear_projectiles(group); c.trial:forget_group(group); s.groups[group]=nil end end
end
function S.channel(c,s,key,ticks,draw,finish,options)
 options=options or {}
 return {T.wait(ticks,function(c,s)
  s.channel={limit=options.hits or 2,damage=0,cancel=options.cancel,recovery=options.interruptRecovery or 60,exposure=options.exposure}
  c.trial:pose('cast'); T.sound(c,'BLOCK_AMETHYST_BLOCK_RESONATE',.7); if options.begin then options.begin(c,s) end
 end,function(c,s,t) if t%4==0 and draw then draw(c,s,t) end end,
 function(c,s) if s.channel then s.channel=nil; finish(c,s) end end),T.rest(options.recovery or 50)}
end
function S.release(c,s,origin,target,damage,group,cap,speed)
 if not c.trial:active() then return end
 local projectile=c.boss:summon_projectile('SNOWBALL',origin,target,speed or .85,{gravity=false,persistent=false,spawn_at_origin=true,track=false})
 if projectile then c.trial:own_projectile(projectile,damage,group,cap,80); s.groups[group]=s.tick+100 end
 T.sound(c,'ENTITY_BLAZE_SHOOT',1.6)
end
function S.bolt(c,s,options)
 local target,origin; local group=options.group or ('spell_'..s.tick)
 return T.wait(options.warn or 30,function(c,s)
  origin=options.origin and T.copy(options.origin) or c.boss:get_eye_location(); target=T.offset(c.trial.player:get_location(),0,1,0)
  c.trial:pose('cast'); T.sound(c,'BLOCK_AMETHYST_BLOCK_CHIME',.9)
 end,function(c,s,t)
  if t<(options.warn or 30)-(options.lock or 8) and not options.target then target=T.offset(c.trial.player:get_location(),0,1,0); c.trial:face(target,8) end
  if options.target then target=options.target end
  if t%3==0 then T.draw(c,T.lane(T.offset(origin,0,-1,0),T.offset(target,0,-1,0),.4),S.violet); T.point(c,origin,S.violet) end
 end,function(c,s)
  if not options.valid or options.valid(c,s) then S.release(c,s,origin,target,options.damage or .35,group,options.cap or .9,options.speed); if options.released then options.released(c,s,origin,target) end end
 end)
end
function S.flight(c,s,group,recovery,finish)
 local exposure=1
 return {T.untilDone(82,nil,nil,function(c,s)
  local hit=c.trial:group_damage(group)>0; c.trial:clear_projectiles(group); if finish then exposure=finish(c,s,hit) or 1 end
  c.trial:forget_group(group); s.groups[group]=nil
 end,function(c,s) return c.trial:projectile_count(group)==0 end),T.wait(recovery or 50,function(c,s) c.trial:pose('idle'); c.trial:stop(); s.exposure=exposure end,nil,function(c,s) s.exposure=1 end)}
end
function S.blade(c,g)
 local p=T.offset(g.p,0,1,0); local x,z=g.x,g.z
 for i=0,8 do T.point(c,T.offset(p,x*i*.25,.1,z*i*.25),S.violet) end
 for _,side in ipairs({-1,1}) do T.point(c,T.offset(p,z*.4*side,0,-x*.4*side),T.gold) end
end
function S.basic(c,s) T.basic(c,s,'magic') end
function S.heal(c,s,amount)
 if not c.trial:active() or (s.healBudget or 0)<=0 or not c.boss:is_alive() then return end
 local before=c.boss:get_health(); c.boss:restore_health(math.min(amount,s.healBudget,c.boss:get_maximum_health()-before))
 s.healBudget=math.max(0,s.healBudget-math.max(0,c.boss:get_health()-before))
end
function S.summon(c,s,id,p,type,lifetime,kind,options)
 options=options or {}; local actor=c.trial:spawn_actor(id,p,4,options.name or '&dBound Servitor',type)
 if not actor then return nil end
 s.servants=s.servants or {}; s.servants[id]={expires=s.tick+lifetime,ready=s.tick+40,kind=kind,damage=options.damage or .3,onHit=options.onHit,onEnd=options.onEnd,cap=options.cap,spent=0}
 if type=='WITHER_SKELETON' then actor:set_equipment('HAND','STONE_SWORD',{unbreakable=true}) end
 return actor
end
function S.endServant(c,s,id)
 local servant=s.servants and s.servants[id]; if not servant then return end
 s.servants[id]=nil; s.wards[id]=nil; c.trial:remove_actor(id)
 if servant.group then c.trial:clear_projectiles(servant.group); c.trial:forget_group(servant.group); s.groups[servant.group]=nil end
 if servant.onEnd then servant.onEnd(c,s) end
end
function S.servantCount(c,s)
 local count=0; for id in pairs(s.servants or {}) do if c.trial:actor(id) then count=count+1 end end; return count
end
function S.servants(c,s)
 s.summonWarning=false
 for id,servant in pairs(s.servants or {}) do
  local actor=c.trial:actor(id)
  if not actor or s.tick>=servant.expires then S.endServant(c,s,id)
  else
   local p=actor:get_location(); local player=c.trial.player:get_location(); local distance=T.distance(p,player)
   local damage=servant.damage*(servant.multiplier or 1); if servant.cap then damage=math.min(damage,math.max(0,servant.cap-servant.spent)) end
   if servant.warning then
    s.summonWarning=true
    if s.tick%3==0 then T.draw(c,servant.shape,S.violet) end
    if s.tick>=servant.warning then
     servant.warning=nil; servant.ready=s.tick+(servant.kind=='ember' and 60 or 80)
     if servant.kind=='ember' then
      servant.group='ember_'..id..'_'..s.tick; S.release(c,s,actor:get_eye_location(),servant.target,damage,servant.group,damage,.65)
     elseif servant.kind=='pounce' then
      servant.landing=c.trial:actor_leap(id,servant.target,20) and s.tick+20 or nil
     elseif servant.kind=='pass' then servant.passUntil=s.tick+24; servant.passHit=false
     elseif T.contains(servant.shape,player) and c.trial:actor_damage(id,damage) then
      servant.spent=servant.spent+damage; if servant.onHit then servant.onHit(c,s,id) end
     end
    end
   elseif servant.landing then
    if s.tick>=servant.landing then servant.landing=nil; if T.distance(p,servant.target)<2 and T.contains(servant.shape,player) and c.trial:actor_damage(id,damage) then servant.spent=servant.spent+damage; if servant.onHit then servant.onHit(c,s,id) end end end
   elseif servant.passUntil then
    if s.tick>=servant.passUntil or not c.trial:actor_step(id,servant.target,.32,false) then servant.passUntil=nil
    elseif not servant.passHit and distance<1.6 and c.trial:actor_damage(id,damage) then servant.passHit=true; servant.spent=servant.spent+damage end
   elseif s.tick>=servant.ready and distance<=(servant.kind=='melee' and 3 or 12) then
    servant.target=T.copy(player)
    if servant.kind=='ember' then servant.target=T.offset(player,0,1,0); servant.shape=T.lane(p,player,.5); servant.warning=s.tick+28
    elseif servant.kind=='pounce' then servant.shape=T.circle(player,2); servant.warning=s.tick+26
    elseif servant.kind=='pass' then local x,z=T.direction(p,player); servant.target=T.offset(p,x*math.min(7,distance+2),0,z*math.min(7,distance+2)); servant.shape=T.lane(p,servant.target,1.6); servant.warning=s.tick+28
    else servant.shape=T.cone(p,player,3,70); servant.warning=s.tick+24 end
    s.summonWarning=true; actor:play_sound_at_self('BLOCK_AMETHYST_BLOCK_CHIME',.5,1.2)
   elseif distance>3 and servant.kind~='ember' and s.tick%5==0 then
    local x,z=T.direction(p,player); c.trial:actor_step(id,T.offset(player,-x*2.5,0,-z*2.5),.12)
   end
  end
 end
end
function S.corpses(c,s,count)
 s.corpses={}; s.servants={}; s.raises=0
 for i=1,count do local id='corpse_'..i; local p=T.offset(c.trial:position(),(i-2)*3,0,4)
  if S.prop(c,id,p,2,'&7Prepared Remains','SKELETON_SKULL') then s.corpses[#s.corpses+1]=id end
 end
end
function S.nextCorpse(c,s)
 for _,id in ipairs(s.corpses or {}) do if c.trial:actor(id) then return id end end
end
function S.raise(c,s,ticks,options)
 local id=S.nextCorpse(c,s); local p; local completed=false
 return {T.wait(ticks,function(c,s) local prop=id and c.trial:actor(id); p=prop and prop:get_location(); s.raiseFocus=id; c.trial:pose('cast'); T.sound(c,'BLOCK_SOUL_SAND_HIT',.6) end,
  function(c,s,t)
   if not id or not c.trial:actor(id) then s.raiseFocus=nil; S.recover(c,s,60); return end
   if t%4==0 then T.tether(c,c.trial:position(),p,S.violet); T.draw(c,T.circle(p,1.5),S.violet) end
  end,function(c,s)
   if not id or not c.trial:actor(id) then return end
   c.trial:remove_actor(id); s.raiseFocus=nil; s.raises=s.raises+1
   local servantId='undead_'..s.raises
   local actor=S.summon(c,s,servantId,p,'WITHER_SKELETON',200,'melee',options)
   if actor and options.raised then options.raised(c,s,servantId) end
   completed=true
  end),T.rest(50)}
end
