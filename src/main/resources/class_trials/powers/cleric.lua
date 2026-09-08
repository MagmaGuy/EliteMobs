local C={}
C.green={particle='DUST',red=100,green=220,blue=130,amount=1}
C.water={particle='DUST',red=140,green=210,blue=255,amount=1}
function C.actor(c,id) return id=='boss' and c.boss or c.trial:actor(id) end
function C.init(c,s,count)
 s.ids={}; s.allies={}; s.shields={}; s.healBudget=c.boss:get_maximum_health()*.12
 for i=1,count do local id='attendant_'..i; s.ids[#s.ids+1]=id; T.spawnAlly(c,s,id,i==1 and -3 or i==2 and 3 or 0,'IRON_SWORD',3) end
end
function C.living(c,s,includeBoss)
 local result={}; for _,id in ipairs(s.ids) do local actor=C.actor(c,id); if actor then result[#result+1]={id=id,actor=actor} end end
 if includeBoss then result[#result+1]={id='boss',actor=c.boss} end
 return result
end
function C.weakest(c,s,includeBoss)
 local values=C.living(c,s,includeBoss)
 table.sort(values,function(a,b) return a.actor:get_health()/a.actor:get_maximum_health()<b.actor:get_health()/b.actor:get_maximum_health() end)
 return values
end
function C.heal(c,s,id,amount)
 local actor=C.actor(c,id); if not actor or not actor:is_alive() or s.healBudget<=0 then return 0 end
 local before=actor:get_health(); local requested=math.min(amount,s.healBudget,actor:get_maximum_health()-before)
 if requested<=0 then return 0 end
 actor:restore_health(requested); local healed=math.max(0,actor:get_health()-before); s.healBudget=math.max(0,s.healBudget-healed)
 if healed>0 then T.draw(c,T.circle(actor:get_location(),.8),T.gold) end
 return healed
end
function C.recover(c,s,ticks,exposure)
 s.channel=nil; c.trial:pose('idle')
 if s.cast then T.interrupt(c,s,{T.rest(ticks,exposure)}) else T.start(c,s,'tending',{T.rest(ticks,exposure)},0) end
end
function C.shield(c,s,id,amount,duration,onEnd)
 if not C.actor(c,id) then return end
 s.shields[id]={remaining=amount,untilTick=s.tick+duration,onEnd=onEnd}
end
function C.passive(c,s)
 for id,ward in pairs(s.shields) do
  local actor=C.actor(c,id)
  if not actor then s.shields[id]=nil
  elseif s.tick>=ward.untilTick then s.shields[id]=nil; if ward.onEnd then ward.onEnd(c,s,'expired') end
  elseif s.tick%6==0 then T.draw(c,T.circle(actor:get_location(),.9),T.gold) end
 end
 T.allies(c,s)
end
function C.damage(c,s)
 local id=c.trial:damaged_actor()
 if id=='boss' and s.channel then
  s.channel.damage=s.channel.damage+c.event.get_damage_amount()
  if s.channel.damage>=2*c.trial.matched_hit then
   local channel=s.channel; s.channel=nil; T.sound(c,'BLOCK_GLASS_BREAK',1.4)
   c.trial:say(channel.feedback or 'Well timed. You found the moment between the notes.')
   if channel.cancel then channel.cancel(c,s) end
   C.recover(c,s,channel.recovery or 50,channel.exposure)
  end
 end
 C.absorb(c,s)
end
function C.absorb(c,s)
 local id=c.trial:damaged_actor(); local ward=s.shields[id]
 if ward and ward.untilTick>s.tick then
  local incoming=c.event.get_damage_amount(); local absorbed=math.min(incoming,ward.remaining)
  ward.remaining=ward.remaining-absorbed; c.event.set_damage_amount(incoming-absorbed); T.sound(c,'ITEM_SHIELD_BLOCK',1.3)
  if ward.remaining<=.001 then s.shields[id]=nil; if ward.onEnd then ward.onEnd(c,s,'broken') end end
 end
end
function C.channel(c,s,key,ticks,draw,finish,options)
 options=options or {}
 return {T.wait(ticks,function(c,s)
  s.channel={id=key,damage=0,recovery=options.interruptRecovery or 50,exposure=options.interruptExposure,feedback=options.feedback,cancel=options.cancel}
  c.trial:pose('cast'); T.sound(c,'BLOCK_NOTE_BLOCK_CHIME',1.4); if options.begin then options.begin(c,s) end
 end,function(c,s,t)
  if t==math.floor(ticks*.65) then T.sound(c,'BLOCK_NOTE_BLOCK_CHIME',.9) end
  if t%4==0 and draw then draw(c,s,t) end
 end,function(c,s)
  if s.channel and s.channel.id==key then s.channel=nil; if finish then finish(c,s) end end
 end),T.rest(options.recovery or 50)}
end
function C.flight(c,s,id)
 local actor=id and C.actor(c,id)
 return M.move(c,s,actor and T.offset(actor:get_location(),2,0,0) or s.sanctuary)
end
function C.focus(c,id,p,hits,name,material)
 local ground=c.trial:ground(p); if not ground then return nil end
 local prop=c.trial:spawn_actor(id,ground,hits,name,'ARMOR_STAND')
 if prop then prop:set_equipment('HEAD',material,{}); prop:set_equipment('CHEST','LEATHER_CHESTPLATE',{}) end
 return prop
end
function C.cleanse(c,s,id,warn,protection)
 local actor
 return {T.wait(warn,function(c,s) actor=C.actor(c,id); c.trial:pose('cast'); T.sound(c,'BLOCK_BELL_USE',1.3) end,
  function(c,s,t) if actor and actor:is_alive() and t%4==0 then T.tether(c,c.trial:position(),actor:get_location()) end end,
  function(c,s) if C.actor(c,id) then c.trial:cleanse(id,false,protection) end end),T.rest(36)}
end
function C.gather(c,s,warn,duration,protection,recovery)
 local destinations
 return {T.wait(warn,function(c,s)
  destinations={}; local p=c.trial:position(); for i,id in ipairs(s.ids) do destinations[id]=T.offset(p,(i-2)*2,0,2) end
  c.trial:pose('cast'); T.sound(c,'BLOCK_BELL_USE',.9)
 end,function(c,s,t) if t%4==0 then for _,v in ipairs(C.living(c,s)) do T.draw(c,T.lane(v.actor:get_location(),destinations[v.id],.6),T.gold) end end end,
  function(c,s) s.alliesPausedUntil=s.tick+duration+recovery; s.gatherUntil=s.tick+duration; s.gatherReduction=protection end),
 T.wait(duration,nil,function(c,s,t) if t%5==0 then for _,v in ipairs(C.living(c,s)) do c.trial:actor_step(v.id,destinations[v.id]); T.draw(c,T.circle(destinations[v.id],.6),T.gold) end end end),T.rest(recovery)}
end
