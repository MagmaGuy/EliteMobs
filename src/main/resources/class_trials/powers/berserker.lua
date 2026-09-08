local B={}
B.red={particle='DUST',red=190,green=45,blue=45,amount=1}
function B.init(c,s) s.outgoing=1; s.markUntil=0; s.controlUntil=0; s.playerGraceUntil=0 end
function B.passive(c,s)
 c.trial:control_guard(s.controlUntil>s.tick)
 if s.markUntil>s.tick and s.tick%10==0 then T.eye(c,c.trial.player:get_location()) end
 if s.controlUntil>s.tick and s.tick%10==0 then T.draw(c,T.circle(c.trial:position(),1.1),T.gold) end
end
function B.heal(c,s,key,amount)
 local remaining=s[key] or 0
 local actual=math.min(remaining,amount,c.boss:get_maximum_health()-c.boss:get_health())
 if actual>0 then c.boss:restore_health(actual); s[key]=remaining-actual; T.draw(c,T.circle(c.trial:position(),1),B.red) end
end
function B.mark(c,s,warn,duration)
 return {T.wait(warn,function(c,s) c.trial:pose('cast'); T.sound(c,'ENTITY_WOLF_GROWL',.8) end,
  function(c,s,t) if t%4==0 then T.eye(c,c.trial.player:get_location()) end end,
  function(c,s) s.markUntil=s.tick+duration end),T.rest(24)}
end
function B.pursue(c,s)
 local p=c.trial:position(); local target=c.trial.player:get_location()
 if T.distance(p,target)>3 and s.markUntil>s.tick then
  local destination=T.rotate(p,target,0,math.min(3,T.distance(p,target)-2.5))
  T.start(c,s,'pursuit',{T.wait(12,nil,function(c,s) c.trial:face(target,4); c.trial:step(destination,.253,true,true) end),T.rest(4)},0)
 else T.basic(c,s,'melee') end
end
function B.cuts(c,s,cuts,options)
 local seq={}; local budget={spent=0,cap=options.cap}; local connected=0
 for i,cut in ipairs(cuts) do
  local shape
  T.append(seq,{T.wait(cut.ticks,function(c,s)
   c.trial:pose('draw'); T.sound(c,'BLOCK_NOTE_BLOCK_BASEDRUM',.5+i*.15)
   if cut.weapon then c.boss:set_equipment('HAND',cut.weapon,{}) end
   if options.exposure then s.exposure=options.exposure end
  end,function(c,s,t)
   if not shape or t<cut.ticks-10 then
    local p=c.trial:position(); local aim=T.rotate(p,c.trial.player:get_location(),cut.angle or 0,cut.range or 3.6)
    shape=cut.width and T.lane(p,aim,cut.width) or T.cone(p,aim,cut.range or 3.6,cut.arc or 85)
    c.trial:face(aim,4)
   end
   if t%4==0 then T.draw(c,shape) end
  end,function(c,s)
   c.trial:pose('swing'); T.sound(c,'ENTITY_PLAYER_ATTACK_SWEEP',.8)
   local damage=type(cut.damage)=='function' and cut.damage(c,s) or cut.damage
   local hit=T.budgetHit(c,s,shape,damage,budget)
   if hit then connected=connected+1 end
   if options.result then options.result(c,s,hit,i,connected) end
  end)})
 end
 T.append(seq,{T.rest(options.recovery or 60,options.recoveryExposure),T.wait(1,nil,nil,function(c,s)
  if options.finished then options.finished(c,s,connected) end
 end)})
 return seq
end
function B.slam(c,s,options)
 local shape
 return {T.wait(options.warn or 36,function(c,s)
  shape=T.circle(c.trial:position(),options.radius or 3.5); c.trial:pose('draw'); T.sound(c,'BLOCK_GRAVEL_BREAK',.6)
 end,function(c,s,t) if t%4==0 then T.draw(c,shape); T.point(c,T.offset(shape.p,0,1+t/40,0),T.amber) end end,
  function(c,s)
   c.trial:pose('swing'); T.sound(c,'ENTITY_GENERIC_EXPLODE',.7)
   local hit=T.hit(c,shape,options.damage)
   if hit and options.launch then c.trial:launch_player(.25); c.trial:player_grace(30) end
   if options.result then options.result(c,s,hit) end
  end),T.rest(options.recovery or 56,options.exposure)}
end
function B.iron(c,s,warn)
 return {T.wait(warn,function(c,s) c.trial:pose('guard'); T.sound(c,'ITEM_ARMOR_EQUIP_NETHERITE',.6) end,
  function(c,s,t) if t%4==0 then T.draw(c,T.circle(c.trial:position(),1.3),T.gold) end end,
  function(c,s) s.controlUntil=s.tick+100 end),T.rest(30)}
end
function B.prop(c,id,p,hits,name,material)
 local prop=c.trial:spawn_actor(id,p,hits,name,'ARMOR_STAND')
 if prop then prop:set_equipment('HEAD',material,{}); prop:set_equipment('CHEST','LEATHER_CHESTPLATE',{}) end
 return prop
end
