local function marker(c,s)
 local p
 return {T.wait(30,function(c,s) p=T.copy(c.trial.player:get_location()); T.sound(c,'BLOCK_ANVIL_PLACE',1.4) end,
  function(c,s,t) if t%4==0 then T.draw(c,T.circle(p,1.5)) end end,
  function(c,s) s.marker=p; s.markerUntil=s.tick+120; s.markerSpent=false end),T.rest(20)}
end
local function cluster(c,s)
 local impact; local charges={}; local budget={spent=0,cap=1.2}; local blastSpent=0
 local seq,group=R.draw(c,s,{warn=36,lock=14,damage=.25,cap=1.2,releaseOnly=true})
 T.append(seq,{R.flight(group,48,function(c,s,result) impact=result; budget.spent=c.trial:group_damage(group) end),
 T.wait(65,function(c,s)
  if not impact then return end
  local center=c.trial:ground(impact.location); if not center then return end
  local facing=T.rotate(center,c.trial:position(),s.phase==2 and 60 or 0,4)
  for i,angle in ipairs({0,120,240}) do
   local id='charge_'..i; local p=T.rotate(center,facing,angle,4); c.trial:remove_actor(id)
   if R.prop(c,s,id,p,1,'&ePowder Canister '..i,'TNT') then
    local boosted=s.markerUntil>s.tick and not s.markerSpent and T.distance(p,s.marker)<=1.5
    if boosted then s.markerSpent=true end
    charges[#charges+1]={id=id,shape=T.circle(p,2),fuse=16+16*i,boosted=boosted,resolved=false}
    c.trial:actor(id):play_sound_at_self('ENTITY_TNT_PRIMED',.5,boosted and 1.5 or 1)
   end
  end
 end,function(c,s,t)
  for _,charge in ipairs(charges) do if not charge.resolved then
   local actor=c.trial:actor(charge.id)
   if not actor then charge.resolved=true; T.sound(c,'BLOCK_FIRE_EXTINGUISH',1.4)
   elseif t>=charge.fuse then
    local amount=math.min(charge.boosted and .45 or .35,math.max(0,1-blastSpent),math.max(0,budget.cap-budget.spent))
    actor:play_sound_at_self('ENTITY_GENERIC_EXPLODE',.6,1.2)
    if amount>0 and T.budgetHit(c,s,charge.shape,amount,budget) then blastSpent=blastSpent+amount end
    charge.resolved=true; c.trial:remove_actor(charge.id)
   else
    if t%4==0 then T.draw(c,charge.shape); T.point(c,T.offset(charge.shape.p,0,1+t/charge.fuse,0),charge.boosted and T.gold or T.amber) end
    if t%16==0 then actor:play_sound_at_self('BLOCK_NOTE_BLOCK_HAT',.4,.8+t/charge.fuse) end
   end
  end end
 end,function(c,s) for _,charge in ipairs(charges) do c.trial:remove_actor(charge.id) end end),T.rest(70)})
 return seq
end
return T.encounter{
 init=function(c,s) R.init(c,s,true); s.markerUntil=0 end,
 passive=function(c,s) R.passive(c,s); if s.markerUntil>s.tick and not s.markerSpent and s.tick%5==0 then T.draw(c,T.circle(s.marker,1.5),T.amber) end end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'step') then s.phasePending=false; T.start(c,s,'step',M.move(c,s),M.cooldown)
  elseif T.ready(s,'marker') then T.start(c,s,'marker',marker(c,s),440)
  elseif T.ready(s,'cluster') then T.start(c,s,'cluster',cluster(c,s),480)
  else R.basic(c,s) end
 end
}
