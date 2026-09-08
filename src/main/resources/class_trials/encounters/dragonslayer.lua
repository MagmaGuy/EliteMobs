local ids={'first_giant','second_giant'}
local function giant(c,s)
 local preferred=s.phase==2 and ids[2] or ids[1]
 if c.trial:actor(preferred) then return preferred end
 for _,id in ipairs(ids) do if c.trial:actor(id) then return id end end
 return nil
end
local function mark(c,s)
 local seq=R.mark(c,s,30,140)
 local begin=seq[1].begin; seq[1].begin=function(c,s) begin(c,s); s.marked=giant(c,s) end
 return seq
end
local function piercer(c,s)
 s.bracing=true; s.braceTarget=giant(c,s)
 return R.draw(c,s,{warn=46,lock=18,damage=1.2,cap=1.2,speed=1.4,recovery=70,penetrate=s.braceTarget and {s.braceTarget} or {},
  target=function(c,s) local actor=s.braceTarget and c.trial:actor(s.braceTarget); return actor and actor:get_eye_location() or T.offset(c.trial.player:get_location(),0,1,0) end,
  release=function(c,s) s.bracing=false end})
end
return T.encounter{
 init=function(c,s)
  R.init(c,s,true); local p=c.trial:position(); local x,z=T.direction(p,c.trial.player:get_location())
  c.trial:spawn_actor(ids[1],T.offset(p,x*8,0,z*8),2,'&eArmored Training Effigy','IRON_GOLEM')
  c.trial:spawn_actor(ids[2],T.offset(p,x*9-z*5,0,z*9+x*5),2,'&eArmored Training Effigy','IRON_GOLEM')
 end,
 passive=function(c,s)
  R.passive(c,s)
  if s.markUntil>s.tick and s.marked then local actor=c.trial:actor(s.marked); if actor and s.tick%5==0 then T.draw(c,T.circle(actor:get_location(),1.1),T.gold) end end
  if s.bracing and s.braceTarget and not c.trial:actor(s.braceTarget) then s.bracing=false; c.trial:crossbow(false); c.trial:say('You took away the support before the shot.'); T.interrupt(c,s,{T.rest(70,1.25)}) end
 end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'step') then s.phasePending=false; T.start(c,s,'step',M.move(c,s),M.cooldown)
  elseif T.ready(s,'mark') then T.start(c,s,'mark',mark(c,s),480)
  elseif T.ready(s,'piercer') then T.start(c,s,'piercer',piercer(c,s),400)
  else R.basic(c,s) end
 end
}
