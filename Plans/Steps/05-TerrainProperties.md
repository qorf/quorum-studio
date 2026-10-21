# Step 5: the terrain's sections in the Properties panel

Part of `../QuorumStudio.md`. It needs steps 3 and 4.

## Status: applied 2026-10-20 (not built or run yet)

**Library (`MakeAWish`), new:**
- SC/Dialogs/`TerrainPropertiesDialog`: the **Terrain** section. Name
  (editable), then read-only lines that the keyboard moves to, so a screen
  reader reads each: size, regions and region size, sample spacing, ground
  height range, folder.
- SC/Dialogs/`TerrainPhysicsPropertiesDialog`: the **Physics** section.
  "Collision in games" (a checkbox) and Friction.
- L/Interface/Behaviors/Scenes/Properties/`TerrainCollisionBehavior`: the
  checkbox's behavior.

**Library, changed:**
- SC/Items/`EditorTerrain`: **a bug fix.** It turned collision off so the
  editor only draws, but the scene saved that, so a terrain added in Studio
  was saved with `"Physics Enabled": false`, and things fell through it in
  games. Now `SetCollision` and `HasCollision` hold the game's setting
  (default on, saved and loaded with the scene), and the editor's own region
  models still never collide.
- SC/Dialogs/`PropertiesDialog`: remembers its first and last fields in Tab
  order (`GetFirstFocus`, `GetLastFocus`).
- SC/`PropertiesPanel`: links Tab order through all the showing sections
  (`LinkFocus`), so Tab goes from Terrain into Physics and back.
- SC/`SceneEditor`: the two dialogs, added with `AddPropertiesDialog` when
  the panel is first set. **Only with a panel:** as floating dialogs, the
  two would cover each other at the same corner.

**How it decides to show:** each dialog shows when the selection is a
`TerrainSelectionHighlight`, and hides otherwise, as the built-in dialogs
do for their items. A terrain shows no "Object Properties" (that's for
models). The order is Terrain, then Physics.

**Differences from the plan below:**
- **Edits apply as they're typed,** like the built-in dialogs, not when the
  field is left. Friction text that isn't a number, or is below 0, is
  ignored.
- **The size comes from `EditorTerrain`'s extent** (`GetMinimumX` and so
  on, from step 3), not a new `TerrainData` helper.
- **The name is in the Terrain section,** with no separate "Basic" section.
- **Renaming** changes the terrain's name at once; whether the scene tree's
  entry follows without reselecting is to be checked.

**Fix after the first try (2026-10-20): a scene whose terrain files are
missing.** Opening a scene stopped with "There is no saved terrain in
Scenes/Terrains/Terrain1, since it has no terrain.json" when the project had been
copied without its `Terrains` folder.
- W/`TerrainItem:Load` now checks for the `terrain.json` first. If it isn't
  there, the item is empty (no regions), keeps the file's path, and says so
  in the output. `IsMissing` reports it. The scene opens, and saving it
  keeps the terrain's entry unchanged.
- `SaveTerrain` does nothing for a missing terrain, so it can't overwrite
  the real files with an empty terrain.
- **Also fixed:** `SaveTerrain` saved relative to the program's working
  folder. It now saves through the file it was loaded or made from, with
  that file's working directory (the project, in Studio).
- The Terrain section says "Files not found: this terrain is empty" for
  such a terrain.
- **That scene also has `"Physics Enabled": false`,** from before step 5's
  collision fix. Check "Collision in games" and save to correct it.

## Tests (as applied)

1. **Select a terrain** (tree, click, or the cursor's Enter): the panel
   shows Terrain then Physics. A new terrain reads: "Size: 128 by 128
   units", "Regions: 4, each 64 by 64 samples", "Sample spacing: 1 units",
   "Ground height: 0 to 0", "Folder: Scenes/Terrains/Terrain1".
2. **Collision in games** is checked for a new terrain. Save, then look at
   the `.qs`: `"Physics Enabled": true`. Uncheck, save: `false`. Reopen:
   the checkbox matches.
3. **Friction:** type 0.1, save: the `.qs` has `"Friction": 0.1`.
   Letters are ignored.
4. **Tab** moves name → each info line → collision → friction → back to
   name. **Shift+Tab** goes the other way. **Escape** returns to the editor.
5. **Selecting a cube** shows Object Properties only; **nothing selected**
   shows "Nothing selected".
6. **Rename** the terrain, save, reopen: the new name is there. Check the
   scene tree's entry too.
7. **In a game** (`TerrainSceneCheck` on the first machine): a cube lands
   on a terrain saved from Studio (the bug fix), and falls through with
   collision unchecked.
8. **With a screen reader:** "Terrain" and "Terrain Physics" are read as
   groups; each info line is read when Tab reaches it.

## The plan (2026-10-20, before applying)

**Revised 2026-10-20:** a new terrain has no materials (step 2), so this
step shows only what a flat terrain has: its info and its physics.
Choosing materials, adding and removing them, and painting are step 9,
which adds a **Materials** section to the panel.

## What the user sees

With a terrain selected, the Properties panel shows these sections, after
"Basic" (the name only, since a terrain has no position, rotation or
scale):

1. **Terrain** (read only):
   - size in world units (for example "128 by 128");
   - region count, region size in samples, and sample spacing;
   - lowest and highest ground;
   - the folder its files are in (for example `Scenes/Terrains/Terrain1`).
2. **Physics** (in games; the editor never collides):
   - **Collision:** on or off. This is the scene's `"Physics Enabled"`.
   - **Friction.**

Changes are applied when a field's edit is committed (Enter, or leaving
the field), and saved with the scene (step 2's `"Physics Enabled"` and
`"Friction"`).

## Library

- **New sections,** as step 4 builds them: two `PropertiesDialog`
  subclasses in SC/Dialogs/, `TerrainInfoPropertiesDialog` and
  `TerrainPhysicsPropertiesDialog`. Each shows when the selection is a
  `TerrainSelectionHighlight`.
- **`SceneEditor`** adds them with `AddPropertiesDialog` (step 4), so they
  dock in the panel, after the built-in dialogs.
- **`PropertiesPanel`** links the Tab order across the showing sections
  (step 4's note on test 11).
- **The Terrain section's values** come from `TerrainData`:
  - `GetRegionCount`, `GetRegionSize` and `GetVertexSpacing`;
  - `GetLowestHeight` and `GetHighestHeight`;
  - the size, from the regions' extent. A small helper, `GetWorldMinimumX`
    and so on, belongs in `TerrainData`. TerrainEditor.quorum:791-855 has
    one, but it isn't in the library.
- **Step 10 and later** keep the Terrain section current as heights
  change.

## Tests

- A new terrain shows **Terrain** with "128 by 128", 4 regions, 64 samples,
  spacing 1, heights 0 to 0, and its folder.
- **Physics:**
  - collision off, then save: a cube in a game falls through;
  - friction 0.1: a cube slides further;
  - reopening the scene shows the same values.
- **With a screen reader,** each field reads its label and value.
