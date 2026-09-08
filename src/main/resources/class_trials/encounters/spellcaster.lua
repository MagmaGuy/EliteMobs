local function bolt(c,s)
 return T.aimShot(c,s,{kind='SNOWBALL',damage=.65,windup=24,lock=10,recovery=26,speed=.7})
end
local function ward(c,s)
 return {
   T.wait(28,function(c,s) c.trial:magic_weapon('STAFF'); c.trial:pose('cast'); T.sound(c,'BLOCK_AMETHYST_BLOCK_RESONATE',.8) end,
     function(c,s,t) if t%4==0 then for i=1,3 do local a=i*math.pi*2/3; T.point(c,T.offset(c.trial:position(),math.cos(a)*1.2,.5+i*.2,math.sin(a)*1.2),T.gold) end end end,
     function(c,s) s.ward=2*c.trial.matched_hit; s.wardActive=true end),
   T.wait(80,nil,function(c,s,t) if s.wardActive and t%5==0 then T.draw(c,T.circle(c.trial:position(),1.3),T.gold) end end,
     function(c,s) s.ward=0; s.wardActive=false; c.trial:magic_weapon('WAND') end),
   T.rest(26)
 }
end
return T.encounter{
 init=function(c,s) s.ward=0; s.wardActive=false end,
 choose=function(c,s)
   if s.phasePending and T.ready(s,'ward') then
     s.phasePending=false; local seq=bolt(c,s); T.append(seq,bolt(c,s)); T.append(seq,ward(c,s));
     T.append(seq,{T.wait(1,nil,nil,function(c,s) s.ready.bolt=s.tick+140 end)}); T.start(c,s,'ward',seq,360)
   elseif T.distance(c.trial:position(),c.trial.player:get_location())<5 and T.ready(s,'blink') then T.start(c,s,'blink',M.move(c,s),M.cooldown)
   elseif T.ready(s,'bolt') then T.start(c,s,'bolt',bolt(c,s),140)
   elseif T.ready(s,'ward') then T.start(c,s,'ward',ward(c,s),360)
   else T.basic(c,s,'magic') end
 end,
 damaged=function(c,s)
   if c.trial:damaged_actor()~='boss' or not s.wardActive then return end
   local incoming=c.event.damage_amount; local absorbed=math.min(incoming,s.ward); s.ward=s.ward-absorbed; c.event.set_damage_amount(incoming-absorbed)
   if s.ward<=0 then
     s.wardActive=false; c.trial:magic_weapon('WAND'); T.sound(c,'BLOCK_GLASS_BREAK',1.2); c.trial:say('Exactly. A ward has a limit.')
     T.start(c,s,'ward',{T.rest(50,1.2)},360)
   end
 end
}

