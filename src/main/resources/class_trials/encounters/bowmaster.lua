local ids={'paper_one','paper_two','paper_three'}
local function targets(c,s)
 local p=c.trial:position(); local x,z=T.direction(p,c.trial.player:get_location())
 for i,id in ipairs(ids) do c.trial:remove_actor(id); R.prop(c,s,id,T.offset(p,x*(3+2*i),0,z*(3+2*i)),1,'&fPaper Training Target','WHITE_WOOL') end
end
local function flight(c,s)
 return R.draw(c,s,{warn=36,lock=14,speed=1.1,damage=.9,cap=.9,recovery=50,penetrate=ids})
end
return T.encounter{
 init=function(c,s) R.init(c,s,false); targets(c,s) end,
 passive=function(c,s) R.passive(c,s); if s.markUntil>s.tick and s.tick%8==0 then local p=c.trial:position(); T.draw(c,T.lane(p,T.rotate(p,c.trial.player:get_location(),0,24),.2),R.feather) end end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'step') then s.phasePending=false; local seq=M.move(c,s); T.append(seq,{T.wait(1,nil,nil,targets),T.rest(30)}); T.start(c,s,'step',seq,M.cooldown)
  elseif T.ready(s,'sight') then T.start(c,s,'sight',R.mark(c,s,26,120),400)
  elseif T.ready(s,'flight') then targets(c,s); T.start(c,s,'flight',flight(c,s),280)
  else R.basic(c,s) end
 end
}
