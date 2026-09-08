local function whiteout(c,s)
 local shape
 return {T.wait(36,function(c,s)
  local p=c.trial:position(); local target=c.trial.player:get_location(); shape=T.arc(p,target,5,1.2,270)
  c.trial:pose('cast'); T.sound(c,'BLOCK_GLASS_HIT',.7)
 end,function(c,s,t) if t%3==0 then T.draw(c,shape,S.ice); for i=0,3 do local p=T.rotate(shape.p,c.trial.player:get_location(),i*90,4); T.draw(c,T.lane(shape.p,p,.1),S.ice) end end end,
 function(c,s) s.storm={shape=shape,expires=s.tick+100,nextPulse=s.tick,budget={cap=.8,spent=0}} end),T.rest(12)}
end
local function barrier(c,s)
 return {T.wait(32,function(c,s) c.trial:pose('guard'); T.sound(c,'BLOCK_AMETHYST_BLOCK_RESONATE',1.4) end,
  function(c,s,t) if t%4==0 then for i=1,3 do T.point(c,T.offset(c.trial:position(),(i-2)*.8,t/24,0),S.ice) end end end,
  function(c,s) c.trial:cleanse('boss',false,0); S.ward(c,s,'boss',2,100,function(c,s,reason)
   s.storm=nil; c.trial:clear_player_slow(); S.recover(c,s,reason=='broken' and 60 or 40)
  end) end),T.wait(100),T.rest(40)}
end
return T.encounter{
 init=function(c,s) S.init(c,s,'STAFF') end,
 passive=function(c,s)
  S.passive(c,s)
  if s.storm then
   if s.tick>=s.storm.expires then s.storm=nil; c.trial:clear_player_slow()
   else
    if s.tick%4==0 then T.draw(c,s.storm.shape,S.ice) end
    if not T.contains(s.storm.shape,c.trial.player:get_location()) then c.trial:clear_player_slow() end
    if s.tick>=s.storm.nextPulse then if T.budgetHit(c,s,s.storm.shape,.25,s.storm.budget) then c.trial:slow_player(.2,16) end; s.storm.nextPulse=s.tick+20 end
   end
  end
 end,damaged=S.damage,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'blink') then s.phasePending=false; T.start(c,s,'blink',M.move(c,s),M.cooldown)
  elseif not s.storm and T.ready(s,'whiteout') then T.start(c,s,'whiteout',whiteout(c,s),480)
  elseif not s.wards.boss and T.ready(s,'barrier') then T.start(c,s,'barrier',barrier(c,s),440)
  else S.basic(c,s) end
 end
}
