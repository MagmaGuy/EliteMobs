local ids={'lancer'}
local function plant(c,s)
 local p
 return {T.wait(36,function(c,s) p=T.offset(c.trial:position(),2,0,0); c.trial:pose('cast'); T.sound(c,'BLOCK_WOOD_PLACE',.7) end,
  function(c,s,t) if t%4==0 then T.draw(c,T.circle(p,5),T.gold); T.draw(c,T.circle(p,.7),T.gold) end end,
  function(c,s)
   local banner=c.trial:spawn_actor('standard',p,2,'&eBattle Standard','ARMOR_STAND')
   if banner then banner:set_equipment('HEAD','WHITE_BANNER',{}); s.standard=p; s.standardUntil=s.tick+160; s.standardAlive=true end
  end),T.rest(30)}
end
local function reform(c,s)
 local seq=P.reform(c,s,ids,s.standard,26,60,30)
 local begin=seq[2].begin; seq[2].begin=function(c,s) if begin then begin(c,s) end; s.reformingUntil=s.tick+60 end
 local frame=seq[2].frame; seq[2].frame=function(c,s,t) frame(c,s,t); c.trial:step(T.offset(s.standard,-2.5,0,0),.15,true,true) end
 return seq
end
return T.encounter{
 init=function(c,s) P.init(c,s,{{'lancer','IRON_SPEAR'}}); s.standardUntil=0 end,
 passive=function(c,s)
  if s.standardAlive then
   if s.tick>=s.standardUntil then c.trial:remove_actor('standard'); s.standardAlive=false
   elseif not c.trial:actor('standard') then
    c.trial:remove_actor('standard'); s.standardAlive=false; s.outgoing=1; s.alliesPausedUntil=s.tick+60
    c.trial:say('The standard falls. Our advantage falls with it.'); T.interrupt(c,s,{T.rest(60,1.2)})
    if not s.cast then T.start(c,s,'broken_standard',{T.rest(60,1.2)},0) end
   end
  end
  local untilTick=s.standardAlive and s.standardUntil or 0
  P.aura(c,s,ids,s.standard or c.trial:position(),5,1.2,untilTick)
  s.outgoing=untilTick>s.tick and T.distance(s.standard,c.trial:position())<=5 and 1.2 or 1
  P.passive(c,s)
 end,
 damaged=function(c,s) if (s.reformingUntil or 0)>s.tick and c.trial:damaged_actor()~='standard' then c.event.multiply_damage_amount(.7) end end,
 choose=function(c,s)
  if s.phasePending and not s.standardAlive and T.ready(s,'steed') then s.phasePending=false; local seq=M.move(c,s); T.append(seq,plant(c,s)); T.lockout(seq,'standard',480); T.start(c,s,'steed',seq,M.cooldown)
  elseif not s.standardAlive and T.ready(s,'standard') then T.start(c,s,'standard',plant(c,s),480)
  elseif s.standardAlive and c.trial:actor('lancer') and T.ready(s,'reform') then T.start(c,s,'reform',reform(c,s),360)
  else T.basic(c,s,'melee') end
 end
}
