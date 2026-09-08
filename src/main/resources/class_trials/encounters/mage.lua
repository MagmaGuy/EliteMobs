local function volley(c,s)
 local group='volley_'..s.tick; local echo=s.echoUntil>s.tick; s.echoUntil=0
 local cap=echo and 1.1 or .9; local lastOrigin,lastTarget
 local steps={}
 for i=1,3 do steps[#steps+1]=S.bolt(c,s,{warn=i==1 and 30 or 12,lock=8,damage=.35,group=group,cap=cap,
  released=function(c,s,origin,target) lastOrigin=T.copy(origin); lastTarget=T.copy(target) end}) end
 if echo then
  steps[#steps+1]=T.wait(1,nil,nil,function(c,s) S.prop(c,'echo',T.offset(lastOrigin,0,-1.5,0),1,'&dSpell Echo','AMETHYST_BLOCK') end)
  steps[#steps+1]=T.wait(24,function(c,s) T.sound(c,'BLOCK_AMETHYST_BLOCK_CHIME',1.6) end,
   function(c,s,t) if c.trial:actor('echo') and t%3==0 then T.draw(c,T.lane(T.offset(lastOrigin,0,-1,0),T.offset(lastTarget,0,-1,0),.4),S.violet); T.eye(c,lastOrigin) end end,
   function(c,s) if c.trial:actor('echo') then S.release(c,s,lastOrigin,lastTarget,.45,group,cap) end; c.trial:remove_actor('echo') end)
 end
 return T.append(steps,S.flight(c,s,group,50))
end
return T.encounter{
 init=function(c,s) S.init(c,s); s.echoUntil=0 end,
 passive=S.passive,damaged=S.damage,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'blink') then s.phasePending=false; T.start(c,s,'blink',M.move(c,s),M.cooldown)
  elseif T.ready(s,'echo') and s.echoUntil<=s.tick then T.start(c,s,'echo',{T.wait(28,function(c,s) c.trial:pose('cast') end,function(c,s,t) if t%4==0 then T.eye(c,c.trial:position()) end end,function(c,s) s.echoUntil=s.tick+120 end),T.rest(12)},440)
  elseif T.ready(s,'volley') then T.start(c,s,'volley',volley(c,s),280)
  else S.basic(c,s) end
 end
}
