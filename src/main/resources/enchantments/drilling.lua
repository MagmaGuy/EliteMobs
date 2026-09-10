-- The original four authored levels retain their cross/plane/depth geometry.
-- Each child break is authorized once and uses the captured tool's natural loot.
return {
  api_version = 1,
  on_break_block = function(context)
    if context.source.sneaking then return end
    local block = context.source.block
    if not block then return end
    local loc = context.player.current_location
    local dx, dy, dz = block.x + .5 - loc.x, block.y - .5 - loc.y, block.z + .5 - loc.z
    local length = math.sqrt(dx * dx + dy * dy + dz * dz)
    if length == 0 then return end
    local axis, direction
    if math.abs(dy / length) > .9 then
      axis, direction = "y", dy > 0 and 1 or -1
    elseif math.abs(dx) > math.abs(dz) then
      axis, direction = "x", dx > 0 and 1 or -1
    else
      axis, direction = "z", dz > 0 and 1 or -1
    end
    local visited = {}
    local function process(origin, x, y, z)
      if not origin then return nil end
      local at = {x = origin.x + x, y = origin.y + y, z = origin.z + z}
      local key = at.x .. "," .. at.y .. "," .. at.z
      if visited[key] ~= nil then return visited[key] and at or nil end
      -- As before, different materials leave a usable geometric center but are not mined.
      if context.world:get_block_at(at.x, at.y, at.z) ~= block.material then return at end
      local removed = context.action:break_naturally(at.x, at.y, at.z, block.material)
      visited[key] = removed
      return removed and at or nil
    end
    local function forward(origin)
      return process(origin, axis == "x" and direction or 0,
        axis == "y" and direction or 0, axis == "z" and direction or 0)
    end
    local function plane(origin, corners)
      if not origin then return end
      for a = -1, 1 do
        for b = -1, 1 do
          if (a ~= 0 or b ~= 0) and (corners or a == 0 or b == 0) then
            if axis == "x" then process(origin, 0, a, b)
            elseif axis == "y" then process(origin, a, 0, b)
            else process(origin, a, b, 0) end
          end
        end
      end
    end
    local level = context.enchantment.level
    if level == 1 then forward(block)
    elseif level == 2 then plane(block, false)
    else
      plane(block, true)
      local next_block = forward(block)
      plane(next_block, level >= 4)
      if level >= 4 then plane(forward(next_block), level >= 5) end
    end
  end,
}
