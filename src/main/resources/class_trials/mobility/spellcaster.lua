-- Blink: marked, grounded, unobstructed relocation with a full arrival opening.
M.cooldown=300
function M.move(c,s,anchor)
  local target,valid
  return {
    T.wait(20,function(c,s)
      local p=c.trial:position(); local x,z=T.direction(p,c.trial.player:get_location()); local side=s.blinkSide or 1; s.blinkSide=-side
      target=anchor or T.offset(p,-z*5*side,0,x*5*side); valid=c.trial:safe_destination(target,true,true)~=nil
      if not valid then target=T.offset(p,z*5*side,0,-x*5*side); valid=c.trial:safe_destination(target,true,true)~=nil end
      T.sound(c,'BLOCK_AMETHYST_BLOCK_CHIME',.8)
    end,function(c,s,t) if t%4==0 then T.draw(c,T.circle(target,.65),{particle='DUST',red=160,green=120,blue=255}) end end,
      function(c,s) if valid then c.trial:blink(target); T.sound(c,'ENTITY_ENDERMAN_TELEPORT',1.3) end end),
    T.rest(26)
  }
end
