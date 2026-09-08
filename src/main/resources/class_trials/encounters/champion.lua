local function salute(c,s)
 local cone
 return {T.wait(26,function(c,s) cone=T.cone(c.trial:position(),c.trial.player:get_location(),4,70); c.boss:set_equipment('HAND','IRON_SWORD',{}); c.trial:pose('guard'); T.sound(c,'BLOCK_NOTE_BLOCK_CHIME',1.3) end,
  function(c,s,t) if t%4==0 then T.draw(c,cone,T.gold) end end,
  function(c,s) if T.contains(cone,c.trial.player:get_location()) then c.trial:push(cone.p,.2); s.duelUntil=s.tick+100 end end),T.rest(30)}
end
local function dominion(c,s)
 local budget={spent=0,cap=1.4}; local seq={}; local lead=s.phase==2 and 1 or -1
 for i,v in ipairs({{30,lead*22,.45,90},{20,-lead*22,.45,90},{26,0,1.1,50}}) do
  local arc
  T.append(seq,{T.wait(v[1],function(c,s) c.boss:set_equipment('HAND',i==3 and 'IRON_AXE' or 'IRON_SWORD',{}); c.trial:pose('draw'); T.sound(c,'BLOCK_NOTE_BLOCK_BASEDRUM',i==3 and .5 or 1) end,
   function(c,s,t)
    if not arc or t<v[1]-10 then local p=c.trial:position(); arc=T.cone(p,T.rotate(p,c.trial.player:get_location(),v[2],4),3.6,v[4]); c.trial:face(T.offset(p,arc.x,0,arc.z),4) end
    if t%4==0 then T.draw(c,arc) end
   end,function(c,s) c.trial:pose('swing'); T.sound(c,'ENTITY_PLAYER_ATTACK_SWEEP',i==3 and .6 or 1.1); T.budgetHit(c,s,arc,v[3],budget) end)})
 end
 T.append(seq,{T.rest(60,1.2),T.wait(1,nil,nil,function(c,s) c.boss:set_equipment('HAND','IRON_SWORD',{}) end)})
 return seq
end
return T.encounter{
 init=function(c,s) P.init(c,s); s.duelUntil=0 end,
 passive=function(c,s) if s.duelUntil>s.tick and s.tick%10==0 then T.eye(c,c.trial.player:get_location()) end end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'steed') then s.phasePending=false; local seq=M.move(c,s); T.append(seq,salute(c,s)); T.append(seq,dominion(c,s)); T.lockout(seq,'salute',320); T.lockout(seq,'dominion',360); T.start(c,s,'steed',seq,M.cooldown)
  elseif T.ready(s,'salute') then T.start(c,s,'salute',salute(c,s),320)
  elseif T.ready(s,'dominion') then T.start(c,s,'dominion',dominion(c,s),360)
  else T.basic(c,s,'melee') end
 end
}
