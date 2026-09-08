local function challenge(c,s)
 local sectors
 return {T.wait(30,function(c,s)
  local p=c.trial:position(); sectors={}; for _,angle in ipairs({-110,-55,0,55}) do sectors[#sectors+1]=T.cone(p,T.rotate(p,c.trial.player:get_location(),angle,5),5,55) end
  c.trial:pose('cast'); T.sound(c,'ITEM_GOAT_HORN_SOUND_1',.8)
 end,function(c,s,t) if t%4==0 then for _,shape in ipairs(sectors) do T.draw(c,shape) end end end,
  function(c,s) for _,shape in ipairs(sectors) do if T.contains(shape,c.trial.player:get_location()) then T.weak(c,s,20,.15); break end end end),T.rest(30)}
end
local function engine(c,s)
 return B.cuts(c,s,{{ticks=32,angle=-25,arc=90,damage=.65},{ticks=26,angle=25,arc=90,damage=.65}},
  {cap=1.1,recovery=60,result=function(c,s,hit,i) if hit and i==1 then c.trial:push(c.trial:position(),.2); c.trial:player_grace(30) end end})
end
return T.encounter{
 init=B.init, passive=B.passive,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'leap') then s.phasePending=false; local seq=M.move(c,s); T.append(seq,challenge(c,s)); T.lockout(seq,'challenge',360); T.start(c,s,'leap',seq,M.cooldown)
  elseif T.ready(s,'challenge') then T.start(c,s,'challenge',challenge(c,s),360)
  elseif T.ready(s,'engine') then T.start(c,s,'engine',engine(c,s),440)
  else T.basic(c,s,'melee') end
 end
}
