local valid = {water = true, ice = true, blue_ice = true, frosted_ice = true, packed_ice = true}
local offsets = {{0, 0, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}}

local function freeze(context, block)
  if not block or not valid[block.material] then return false end
  local durability = context.item:get_durability()
  if not durability or durability.current < 1 then return false end
  local changed = false
  local count = context.enchantment.level == 1 and 1 or #offsets
  for i = 1, count do
    local offset = offsets[i]
    local x, y, z = block.x + offset[1], block.y + offset[2], block.z + offset[3]
    if context.world:get_block_at(x, y, z) == "water"
      and context.action:place_block(x, y, z, "water", "minecraft:ice") then
      changed = true
    end
  end
  if changed then context.item:use_durability(1, true) end
  return changed
end

local function right_click(context)
  local eye = context.player:get_eye_location()
  local direction = context.player:get_look_direction()
  local hit = context.world:raycast(eye.x, eye.y, eye.z, direction.x, direction.y, direction.z, 6, "ALWAYS", true)
  if hit and freeze(context, hit.hit_block) then context.event:cancel() end
end

return {
  api_version = 1,
  on_right_click = right_click,
  on_shift_right_click = right_click,
  on_break_block = function(context)
    -- The accepted original block break remains with Minecraft; only neighbors/remaining water are frozen here.
    freeze(context, context.source.block)
  end,
}
