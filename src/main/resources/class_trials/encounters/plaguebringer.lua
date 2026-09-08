local function clearTrail(c,s) s.trail=nil; s.miasma=nil; c.trial:clear_player_slow(); s.playerWeakUntil=0 end
local function trail(c,s,id)
 local points={}; local servant=c.trial:actor(id); if not servant then return {T.rest(40)} end
 local p=servant:get_location(); local x,z=T.direction(p,c.trial.player:get_location())
 for i=1,3 do points[i]=T.offset(p,x*(i-1)*2,0,z*(i-1)*2) end
 return {T.wait(30,function(c,s) c.trial:pose('cast'); T.sound(c,'BLOCK_BREWING_STAND_BREW',.7) end,
  function(c,s,t) if t%4==0 then for _,point in ipairs(points) do T.draw(c,T.circle(point,2),S.violet) end end end,
  function(c,s) if c.trial:actor(id) then s.trail={id=id,points=points,hit={},expires=s.tick+60} end end),T.rest(40)}
end
return T.encounter{
 init=function(c,s) S.init(c,s,'STAFF'); S.corpses(c,s,3) end,
 passive=function(c,s)
  S.passive(c,s); S.servants(c,s)
  if s.trail then
   local servant=s.servants[s.trail.id]
   if not servant or s.tick>=s.trail.expires then s.trail=nil
   else for i,p in ipairs(s.trail.points) do local shape=T.circle(p,2)
    if s.tick%4==0 then T.draw(c,shape,S.violet) end
    if not s.trail.hit[i] and servant.spent<.9 and T.hit(c,shape,math.min(.25,.9-servant.spent)) then s.trail.hit[i]=true; servant.spent=math.min(.9,servant.spent+.25) end
   end end
  end
  if s.miasma then
   if s.tick>=s.miasma.expires then s.miasma=nil; c.trial:clear_player_slow(); s.playerWeakUntil=0
   else
    if s.tick%4==0 then T.draw(c,s.miasma.shape,S.violet) end
    if T.contains(s.miasma.shape,c.trial.player:get_location()) then c.trial:slow_player(.15,6); T.weak(c,s,6,.15)
    else c.trial:clear_player_slow(); s.playerWeakUntil=0 end
   end
  end
 end,damaged=S.damage,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'blink') then s.phasePending=false; T.start(c,s,'blink',M.move(c,s),M.cooldown)
  elseif S.nextCorpse(c,s) and S.servantCount(c,s)==0 and T.ready(s,'raise') then
   T.start(c,s,'raise',S.raise(c,s,46,{damage=.3,cap=.9,onEnd=clearTrail,raised=function(c,s,id) s.trailPending=id end}),440)
  elseif s.trailPending then local id=s.trailPending; s.trailPending=nil; T.start(c,s,'trail',trail(c,s,id),0)
  elseif S.servantCount(c,s)>0 and not s.miasma and T.ready(s,'miasma') then
   local shape; T.start(c,s,'miasma',{T.wait(32,function(c,s) shape=T.arc(c.trial:position(),c.trial.player:get_location(),4,2.5,270); c.trial:pose('cast') end,
    function(c,s,t) if t%4==0 then T.draw(c,shape,S.violet) end end,function(c,s) s.miasma={shape=shape,expires=s.tick+80} end),T.rest(40)},440)
  else S.basic(c,s) end
 end
}
