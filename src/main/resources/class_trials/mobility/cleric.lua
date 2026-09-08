-- Guardian Flight: move to an existing ally or sanctuary, then leave a landing opening.
M.cooldown=360
function M.move(c,s,anchor)
  local target
  return {
    T.wait(20,function(c,s)
      target=anchor or s.sanctuary
      if not target then local p=c.trial:position(); local x,z=T.direction(p,c.trial.player:get_location()); target=T.offset(p,-z*4,0,x*4) end
      T.sound(c,'ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM',1.1)
    end,function(c,s,t) if t%4==0 then T.draw(c,T.lane(c.trial:position(),target,.6),T.gold) end end,
      function(c,s) c.trial:leap(target,20) end),
    T.wait(24), T.rest(30)
  }
end
