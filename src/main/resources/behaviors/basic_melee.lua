-- The same combat behavior works with ground, flying, aquatic and amphibious body navigation.
-- Change these shared modules once to tune every mob using this preset.
return ai.program {
    id = 'elitemobs:behavior/basic_melee', revision = 1,
    modules = {
        'elitemobs:behavior/target', 'elitemobs:behavior/pursuit',
        'elitemobs:behavior/melee', 'elitemobs:behavior/wander'
    },
    budget = {
        callback_micros = 2000, entity_micros = 4000, server_micros = 5000,
        max_callbacks = 20, max_instructions = 12000, max_action_requests = 2
    }
}
