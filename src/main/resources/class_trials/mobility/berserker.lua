-- Crater Leap: a physical ballistic leap to a captured, warned landing zone.
M.cooldown=360
function M.move(c,s,anchor)
  local target,g,launched,landed=false
  return {
    T.wait(30,function(c,s) target=T.copy(anchor or c.trial.player:get_location()); g=T.circle(target,3); c.trial:pose('draw'); T.sound(c,'BLOCK_GRAVEL_BREAK',.65) end,
      function(c,s,t) if t%4==0 then T.draw(c,g) end end,
      function(c,s) launched=c.trial:leap(target,22); landed=false; if not launched then T.fizzle(c) end end),
    T.wait(30,nil,function(c,s,t)
      if launched and not landed and t>8 and c.boss:is_on_ground() then
        landed=true
        if T.distance(c.trial:position(),target)<1.5 then T.hit(c,g,.9); T.draw(c,g,{particle='DUST',red=160,green=100,blue=65}); T.sound(c,'ENTITY_GENERIC_EXPLODE',.65) end
      end
    end),
    T.rest(44)
  }
end
