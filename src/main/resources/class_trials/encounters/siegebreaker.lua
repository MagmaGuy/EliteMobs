local function breach(c,s)
 return B.cuts(c,s,{{ticks=28,angle=0,arc=70,range=3.2,damage=.55,weapon='IRON_AXE'}},
  {cap=.55,recovery=30,result=function(c,s,hit) if hit then s.breachUntil=s.tick+80; T.sound(c,'ITEM_SHIELD_BREAK',1) end end})
end
local function impact(c,s)
 local seq=B.slam(c,s,{warn=36,radius=4,damage=1.1,recovery=60,exposure=1.2})
 local begin=seq[1].begin; seq[1].begin=function(c,s) c.boss:set_equipment('HAND','MACE',{}); begin(c,s) end
 return seq
end
return T.encounter{
 init=function(c,s) B.init(c,s); s.breachUntil=0 end,
 passive=function(c,s) B.passive(c,s); s.outgoing=s.breachUntil>s.tick and 1.15 or 1; if s.breachUntil>s.tick and s.tick%8==0 then T.eye(c,c.trial.player:get_location()); T.sound(c,'BLOCK_NOTE_BLOCK_HAT',.5) end end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'leap') then s.phasePending=false; local seq=M.move(c,s); T.append(seq,breach(c,s)); T.lockout(seq,'breach',360); T.start(c,s,'leap',seq,M.cooldown)
  elseif T.ready(s,'breach') then T.start(c,s,'breach',breach(c,s),360)
  elseif T.ready(s,'impact') then T.start(c,s,'impact',impact(c,s),440)
  else T.basic(c,s,'melee') end
 end
}
