"""Writes src/main/resources/vanilla_item_names.tsv: every vanilla item's official English name, by Material name.

ItemDisplayNames reads that table, so quest rewards, loot messages and lore show "Steak" for COOKED_BEEF the way the
game does. Rerun this for each new Minecraft version:

    python -I tools/vanilla-item-names/generate.py                  # latest release, writes the resource
    python -I tools/vanilla-item-names/generate.py --version 26.3

The item list is the client jar's assets/minecraft/items/*.json, one model definition per registered item, which
matches the Spigot Material constants whose isItem() is true. Names come from the jar's en_us.json: the
item.minecraft.<id> key, else block.minecraft.<id>.

A few item families share one name in game and tell their members apart with a detail line under it. Those get the
detail in parentheses, which is the first line the tooltip shows:
    music discs        jukebox_song.minecraft.<song>          Music Disc (C418 - cat)
    smithing templates upgrade.minecraft.netherite_upgrade,   Smithing Template (Netherite Upgrade)
                       trim_pattern.minecraft.<pattern>       Smithing Template (Sentry Armor Trim)
    anything else with item.minecraft.<id>.desc               Banner Pattern (Flower Charge), Disc Fragment (Music Disc - 5)
A detail already part of the name is left out, so a later rename to a specific name needs no change here.

--locales writes the same table in other game languages (for the translation files), with the same items getting
details; zh_cn, zh_tw and ja_jp use full-width parentheses:
    python -I tools/vanilla-item-names/generate.py --locales zh_cn,fr_fr --out-dir <folder>

Downloads go to a new temporary folder that is deleted afterwards, are checked against Mojang's SHA-1 and are only
read as JSON or as a zip listing. Uses only the Python standard library.
"""
import argparse
import hashlib
import json
import os
import re
import shutil
import sys
import tempfile
import urllib.request
import zipfile

MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
RESOURCES = "https://resources.download.minecraft.net/"
REPO = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DEFAULT_OUT = os.path.join(REPO, "src", "main", "resources", "vanilla_item_names.tsv")
FULL_WIDTH = {"zh_cn", "zh_tw", "ja_jp"}


def fetch(url, folder, name, sha1=None):
    path = os.path.join(folder, name)
    with urllib.request.urlopen(url, timeout=60) as response, open(path, "wb") as out:
        shutil.copyfileobj(response, out)
    if sha1:
        digest = hashlib.sha1()
        with open(path, "rb") as f:
            for chunk in iter(lambda: f.read(1 << 20), b""):
                digest.update(chunk)
        if digest.hexdigest() != sha1:
            raise SystemExit(f"{url} failed its SHA-1 check")
    return path


def read_json(path):
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def detail_key(item_id, lang):
    if item_id.startswith("music_disc_"):
        return "jukebox_song.minecraft." + item_id[len("music_disc_"):]
    if item_id == "netherite_upgrade_smithing_template":
        return "upgrade.minecraft.netherite_upgrade"
    if item_id.endswith("_armor_trim_smithing_template"):
        return "trim_pattern.minecraft." + item_id[:-len("_armor_trim_smithing_template")]
    key = f"item.minecraft.{item_id}.desc"
    return key if key in lang else None


def name_key(item_id, lang):
    for key in (f"item.minecraft.{item_id}", f"block.minecraft.{item_id}"):
        if key in lang:
            return key
    return None


def first_line(text):
    return text.split("\n", 1)[0].strip()


def plan(items, en_us):
    """(item id, name key, detail key or None) for every item, decided on the English names."""
    rows, missing = [], []
    for item_id in items:
        key = name_key(item_id, en_us)
        if key is None:
            missing.append(item_id)
            continue
        detail = detail_key(item_id, en_us)
        if detail is not None:
            if detail not in en_us:
                raise SystemExit(f"{item_id}: detail key {detail} is missing from en_us")
            if first_line(en_us[detail]).lower() in en_us[key].lower():
                detail = None
        rows.append((item_id, key, detail))
    if missing:
        raise SystemExit("No en_us name for: " + ", ".join(missing))
    return rows


def names(rows, lang, locale):
    opening, closing = ("（", "）") if locale in FULL_WIDTH else (" (", ")")
    table = {}
    for item_id, key, detail in rows:
        name = lang[key]
        if detail is not None:
            name = name + opening + first_line(lang[detail]) + closing
        if not name or re.search(r"[\t\r\n]", name):
            raise SystemExit(f"{locale} {item_id}: unusable name {name!r}")
        table[item_id.upper()] = name
    return table


def write(table, path, version, locale):
    duplicates = {}
    for material, name in table.items():
        duplicates.setdefault(name, []).append(material)
    for name, materials in sorted(duplicates.items()):
        if len(materials) > 1 and locale == "en_us":
            print(f"note: {', '.join(sorted(materials))} share the name {name!r}", file=sys.stderr)
    lines = [f"# Official Minecraft {version} item names ({locale}), by Material name.",
             "# Generated by tools/vanilla-item-names/generate.py; rerun it for a new Minecraft version instead of editing."]
    lines += [f"{material}\t{table[material]}" for material in sorted(table)]
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(lines) + "\n")
    print(f"wrote {len(table)} {locale} names to {path}")


def main():
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    parser.add_argument("--version", help="Minecraft release id (default: the latest release)")
    parser.add_argument("--locales", help="comma-separated game locales to write instead of the en_us resource")
    parser.add_argument("--out-dir", help="folder for the --locales tables")
    parser.add_argument("--out", default=DEFAULT_OUT, help="where the en_us table goes")
    args = parser.parse_args()
    if args.locales and not args.out_dir:
        parser.error("--locales needs --out-dir")

    folder = tempfile.mkdtemp(prefix="vanilla-item-names-")
    try:
        manifest = read_json(fetch(MANIFEST, folder, "manifest.json"))
        version_id = args.version or manifest["latest"]["release"]
        entry = next((v for v in manifest["versions"] if v["id"] == version_id), None)
        if entry is None:
            raise SystemExit(f"Unknown Minecraft version {version_id}")
        version = read_json(fetch(entry["url"], folder, "version.json", entry["sha1"]))
        client = version["downloads"]["client"]
        jar = fetch(client["url"], folder, "client.jar", client["sha1"])
        with zipfile.ZipFile(jar) as z:
            en_us = json.loads(z.read("assets/minecraft/lang/en_us.json").decode("utf-8"))
            items = sorted(n[len("assets/minecraft/items/"):-len(".json")] for n in z.namelist()
                           if re.fullmatch(r"assets/minecraft/items/[a-z0-9_]+\.json", n))
        rows = plan(items, en_us)

        if not args.locales:
            write(names(rows, en_us, "en_us"), args.out, version_id, "en_us")
            return
        index_info = version["assetIndex"]
        objects = read_json(fetch(index_info["url"], folder, "assets.json", index_info["sha1"]))["objects"]
        os.makedirs(args.out_dir, exist_ok=True)
        for locale in args.locales.split(","):
            locale = locale.strip()
            if locale == "en_us":
                lang = en_us
            else:
                obj = objects.get(f"minecraft/lang/{locale}.json")
                if obj is None or not re.fullmatch(r"[0-9a-f]{40}", obj["hash"]):
                    raise SystemExit(f"Minecraft {version_id} has no {locale} language")
                lang = read_json(fetch(RESOURCES + obj["hash"][:2] + "/" + obj["hash"], folder, locale + ".json", obj["hash"]))
                # The game shows the English text for anything a language file lacks.
                lang = {**en_us, **lang}
            write(names(rows, lang, locale), os.path.join(args.out_dir, locale + ".tsv"), version_id, locale)
    finally:
        shutil.rmtree(folder, ignore_errors=True)


if __name__ == "__main__":
    main()
