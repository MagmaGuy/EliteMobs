# Translation Finalization Plan — Progress Tracker

**Goal:** Translate all remaining untranslated rows across 18 language CSV files so every language reaches ~100% coverage (minus legitimate skips like proper nouns).

**Method (v4 — Subagent chunks):** Spawn one subagent per 500-line chunk. Each agent reads its chunk with the Read tool, finds rows where en == custom, and translates them with the Edit tool. Agents run sequentially (one at a time per language) to avoid edit conflicts. **NO PYTHON. NO BASH SCRIPTS. Read + Edit only.**

---

## File Info

- **Path pattern:** `testbed/plugins/EliteMobs/translations/custom_{lang}.csv`
- **Dist pattern:** `testbed/plugins/EliteMobs/translations/dist/custom_{lang}.csv`
- **Format:** `"key","en","{lang_code}"` — all double-quoted, LF line endings, UTF-8
- **Total lines per file:** ~26,482 (varies slightly)
- **Chunks per language:** ~53 (at 500 lines each)

## Skip Rules

Rows where en == lang_col should NOT be translated if they are:
- Proper nouns (boss names: Fenrir, Dullahan, Zoltan, Agdluak, etc.)
- NPC names (Oasis names, Primis Latin names, Yggdrasil Norse names, etc.)
- Color-code-only strings (`&a`, `&c`, `&7&l&m----`)
- Placeholder-only strings (`$weaponOrArmorStats`, `%player%`)
- Formatting/separator strings (`&e&l-----`, ` &7/ `, `&8???`, `&m⎯⎯⎯`)
- Gradient/hex-encoded NPC names/roles (`&x&hex` per-character coloring)
- Universal gaming terms (OK, NPC, HP, XP, TNT, Casino, BFF)
- Onomatopoeia/memes (uwu, OwO, Meow, Woof, Oink, Noooo)
- Empty strings, URLs, obfuscated text (`&k`)
- Words identical in both languages
- Config format strings (ItemSettings, player_status_screen, initialize, etc.)
- Reversed/encoded lore text (cave boiler notes)
- Credits lines

## Preservation Rules

- ALL `&` color codes, `$placeholder` variables, `%placeholder%` variables
- Gradient tags `<g:#HEX:#HEX>...</g>`
- CSV quoting, escaped quotes `""`
- Line endings

---

## Subagent Prompt Template

Each agent is spawned with `subagent_type: "general-purpose"` and the following prompt (fill in LANGUAGE, LANG_CODE, FILE_PATH, START, END):

```
You are translating a Minecraft plugin CSV file to LANGUAGE.

CRITICAL RULES — VIOLATING ANY OF THESE IS UNACCEPTABLE:
1. Use ONLY the Read tool and Edit tool. NEVER use Python, Bash, scripts, or any other tool.
2. Do NOT write any code. Do NOT run any commands. Read + Edit ONLY.

TASK:
1. Use the Read tool to read lines START to END of FILE_PATH.
2. Scan every row. The CSV format is: "key","en","LANG_CODE"
3. Find rows where column 2 (English) is IDENTICAL to column 3 (translation) — these are untranslated.
4. For each untranslated row that genuinely needs translation, use the Edit tool to replace the row. The old_string is the full CSV row, the new_string is the same row with column 3 translated.
5. SKIP rows that are legitimately the same in both languages:
   - Proper nouns (boss/NPC names like Fenrir, Dullahan, Zoltan)
   - Color-code-only strings (&a, &c, &7&l&m----)
   - Placeholder-only strings ($weaponOrArmorStats, %player%)
   - Formatting strings (&e&l-----, &8???, &m⎯⎯⎯)
   - Gradient/hex-encoded names (&x& per-character coloring)
   - Universal gaming terms (OK, NPC, HP, XP, TNT, Casino)
   - Onomatopoeia (uwu, OwO, Meow, Woof)
   - URLs, empty strings, obfuscated text (&k)
   - Config format strings (variable names, not player-facing text)
   - Credits lines
6. PRESERVE exactly: all & color codes, $placeholder variables, %placeholder% variables, gradient tags <g:#HEX:#HEX>...</g>, CSV double-quoting, escaped quotes ""
7. When done, report: "Translated X rows, skipped Y legitimate same-value rows, Z rows were already translated."
```

---

## Process Per Language

1. Loop from chunk 1 (lines 1-500) through chunk 53 (lines 26001-26482).
2. For each chunk, spawn one subagent with the prompt template above.
3. Wait for it to finish before spawning the next (sequential, avoids conflicts).
4. After all 53 chunks complete, copy file to dist/: `cp custom_{lang}.csv dist/custom_{lang}.csv`

---

## Language Status

### COMPLETED (in dist/)

| # | Lang | File | Status | Notes |
|---|------|------|--------|-------|
| 1 | es | custom_es.csv | DONE | 0 rows needed translation |
| 2 | fr | custom_fr.csv | DONE | ~1,517 rows translated |
| 3 | de | custom_de.csv | DONE | 77 rows translated |
| 4 | hu | custom_hu.csv | DONE | Verified clean, 0 rows needed |
| 5 | ko | custom_ko.csv | DONE | Verified clean, 1 fix |
| 6 | nl | custom_nl.csv | DONE | 3 minor rows, verified acceptable |
| 7 | pl | custom_pl.csv | DONE | Verified clean, 0 rows needed |
| 8 | pt-BR | custom_pt-BR.csv | DONE | 10 rows translated |
| 9 | ro | custom_ro.csv | DONE | Verified mostly clean |
| 10 | zh-CN | custom_zh-CN.csv | DONE | ~250+ rows translated. Verified 0 remaining. |
| 11 | zh-TW | custom_zh-TW.csv | DONE | ~25 rows translated (mythic items, arena lines) |

| 12 | ru | custom_ru.csv | DONE | 5 rows translated (difficulty prefixes, BFF) |
| 13 | ja | custom_ja.csv | DONE | Full scan, translated remaining rows |
| 14 | tr | custom_tr.csv | DONE | ~1,172 edits (quests, skills, items, arena, zombie dialogs) |
| 15 | it | custom_it.csv | DONE | ~610 edits |
| 16 | vi | custom_vi.csv | DONE | ~2,525 edits |
| 17 | id | custom_id.csv | DONE | ~164 edits |
| 18 | cs | custom_cs.csv | DONE | ~180 edits |

### ALL 18 LANGUAGES COMPLETE

---

## Difficulty Translations Reference

| English | zh-CN | zh-TW | ja | ko | ru | tr | it | vi | id | cs |
|---------|-------|-------|----|----|----|----|----|----|----|----|
| [Hard] | [困难] | [困難] | [ハード] | [어려움] | [Сложный] | [Zor] | [Difficile] | [Khó] | [Sulit] | [Těžký] |
| [Mythic] | [史诗] | [史詩] | [神話] | [신화] | [Мифический] | [Efsanevi] | [Mitico] | [Huyền thoại] | [Mitos] | [Mýtický] |
| [Normal] | [普通] | [普通] | [ノーマル] | [일반] | [Обычный] | [Normal] | [Normale] | [Bình thường] | [Normal] | [Normální] |
| [Dynamic] | [动态] | [動態] | [ダイナミック] | [동적] | [Динамический] | [Dinamik] | [Dinamico] | [Động] | [Dinamis] | [Dynamický] |
