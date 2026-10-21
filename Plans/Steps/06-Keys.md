# Step 6: overridable scene-editor keys, and the terrain keys

Part of `../QuorumStudio.md`. It can start any time, and must be in before
step 7.

**The rule (the user's, 2026-10-20):**
- the terrain uses keys Studio already has wherever their meaning carries
  over;
- every new key can be overridden.

## Status: applied 2026-10-20 (not built or run yet)

**Library (`MakeAWish`):**
- **New** L/Interface/`SceneEditorKeys`: one action per scene-editor key
  (51), each returning today's key exactly, Mac or Windows/Linux as
  `DefaultInputSets` chose it; plus the terrain keys of Part B
  (`GetTerrainToolKey(1 to 9)` Alt+1 to Alt+9, `GetTerrainBrushUseKey` Space,
  `GetTerrainStrengthUpKey` Alt+], `GetTerrainStrengthDownKey` Alt+[), and
  `GetCursorSizeUpKey` / `GetCursorSizeDownKey`.
- `DefaultInputSets:SetupSceneEditorInput(GameInput, SceneEditorKeys)`: every
  keyboard key in it comes from the keys object. The one-argument version
  passes the defaults. Mouse and drag inputs are unchanged.
- `Game:ReplaceSceneEditorInput(SceneEditorKeys)`.
- `SceneEditorInputTable` now names itself `"SceneEditor"`.

**Studio (`MakeAWish`):**
- **New** QS/Interface/`StudioSceneEditorKeys is SceneEditorKeys`: every key
  from the key map, falling back to the library's default.
- `QuorumStudio:SetupInputTables` calls `ReplaceSceneEditorInput` with it,
  after the key map is loaded.
- `KeyMap`: 14 new key names and getters (cursor size, the nine tools,
  brush use, strength), and `GetMappedKey`.
- **`MacKeyMap` and `WindowsKeyMap`:** every scene-editor key the library
  binds (44) now returns the library's own default, so the key map's
  defaults match the real bindings by construction (the old mismatches,
  such as Mac scale X down, are gone). The keyboard camera keys keep their
  key-map defaults, since the library doesn't bind them.
- **`RemapKeyMap`:** a key missing from a user's key map file now takes this
  platform's default, instead of the whole file being rejected (which threw
  away the user's code-editor keys). The key map version stays 2.0.
- `KeyMapLoader`: forwards the new getters.

**Not done (from the plan):**
- the keyboard camera bindings (optional);
- Space and the strength keys are defined and overridable, but bound with
  step 8's strokes.

## Today

- **The real bindings are hard-coded in the library:**
  `DefaultInputSets:SetupSceneEditorInput` (L/Interface/DefaultInputSets.quorum:1430-2197)
  builds the scene editor's input tables. `Game:CreateDefaultInputTables`
  calls it (L/Game/Game.quorum:546-567).
- **The tables, by input group:**
  - `"SceneEditor"` (cursor mode; a `SceneEditorInputTable` with separate
    2D and 3D maps);
  - `"SelectionHighlight3D"` and `"SelectionHighlight2D"`;
  - `"CameraItem3D"` and `"CameraItem2D"`;
  - `"NewItemPreview3D"` and `"NewItemPreview2D"`;
  - `"PropertiesDialog"`, `"VisualEditorFindDialog"`,
    `"PaletteItemContent"`.
- **Studio's key map has an entry for most of them, but nothing reads
  them.**
  - `QS/Interface/KeyMap.quorum:111-171` declares about 50 visual-editor
    keys. Mac and Windows defaults are in `MacKeyMap` and `WindowsKeyMap`.
  - A user's changes are loaded from the key map file by `KeyMapLoader` /
    `RemapKeyMap`.
  - No Studio code calls those getters, so changing them does nothing.
  - **Some defaults don't match the real keys.** For example, Mac
    "selection scale X down" is Cmd+Shift+A in the key map, but the real
    binding is Alt+A. Windows "scale Z up" is Ctrl+W in the key map, but
    the real binding is Alt+W.
- **There's no screen for remapping keys.** The key map file is edited by
  hand.

## Part A: make the existing scene-editor keys overridable

### Library

1. **New L/Interface/`SceneEditorKeys.quorum`:**
   - one action per scene-editor binding, each returning an `InputSet`, for
     example `GetCursorLeft3DKey`, `GetSelectionRotateYawLeftKey`,
     `GetCursorSizeUpKey`, `GetFocusPropertiesKey`;
   - each returns today's key exactly, using the same Mac/Windows choice
     (`ShouldUseMacKeys`).
2. **`DefaultInputSets:SetupSceneEditorInput(GameInput, SceneEditorKeys)`:**
   - every hard-coded `InputSet` in it comes from the keys object instead;
   - the existing one-argument version passes a default `SceneEditorKeys`,
     so games and other hosts are unchanged.
3. **`Game:ReplaceSceneEditorInput(SceneEditorKeys keys)`** builds the
   tables again and replaces the old ones by their identifiers.
4. **Small fix:** `SceneEditorInputTable`'s constructor names itself
   `"CodeCompletionMenuRoot"`; it should be `"SceneEditor"`.

### Studio

1. **New QS/Interface/`StudioSceneEditorKeys is SceneEditorKeys`:** each
   getter returns the key map's binding (through `KeyMapLoader`), so a
   user's key map file overrides it.
2. **After the key map loads,** `QuorumStudio` calls
   `ReplaceSceneEditorInput(studioKeys)`. It does so again if the key map
   is reloaded.
3. **Fix the key map's defaults** in `MacKeyMap` and `WindowsKeyMap` so
   each matches today's real binding. Add the few bindings that have no
   key-map entry yet: cursor size up and down, report camera direction, and
   items in view.
4. **The key map's version** (`KeyMap.quorum:13`, 2.0) must be considered
   carefully.
   - Raising it makes `RemapKeyMap` drop every user's changes, including
     their code-editor keys.
   - **Recommended:** don't raise it. New keys fall back to their
     defaults when a user's file doesn't have them. This needs a small
     change in `RemapKeyMap`, so a missing key uses the default rather than
     failing.

**The result:** the scene editor's keys are the same as today, but each one
can be changed in Studio's key map file.

### The keyboard camera (optional, recommended for terrain)

- `DefaultInputSets` makes the keyboard camera behaviors (about lines
  1514-1546) but binds none of them, so only the mouse can turn or move
  the 3D view.
- The key map already has `CAMERA_ROTATE_*`, `CAMERA_MOVE_*` and
  `CAMERA_STRAFE_*` entries.
- **Proposal:** bind them in the `"SceneEditor"` table with
  `SceneEditorKeys`, using the key map's current defaults. That needs a
  check for conflicts with the cursor arrows; the key map's defaults may
  need changing.
- Looking around a terrain without a mouse matters for accessibility.

## Part B: the terrain keys

When a terrain is selected and a brush tool is chosen (steps 7 to 11), the
focus is on the terrain's highlight. These keys then apply:

| Action | Key | Where it comes from |
|---|---|---|
| Move the brush along the ground | ← → ↑ ↓ | **Existing:** the cursor and selection move keys |
| Brush size up / down | Alt+= / Alt+- | **Existing:** cursor size, and scale up and down for a selection |
| Use the brush (hold) | Space | **New**, overridable. Plain Space isn't bound in the scene editor; Studio uses Ctrl+Space and Alt+Space elsewhere |
| Brush strength up / down | Alt+] / Alt+[ | **New**, overridable |
| Choose a tool | Alt+1 to Alt+9, in the in-game editor's order (1 raise, 2 lower, 3 smooth, 4 flatten, 5 ramp, 6 paint, 7 blend paint, 8 add region, 9 remove region) | **New**, overridable. Alt+digits aren't bound; Cmd/Ctrl+digits are Studio's focus keys. Tools can also be chosen in the Properties panel, with no key |
| Cancel a stroke, then leave the tool | Escape | **Existing:** cancel |
| Say the ground under the brush | Ctrl+Shift+1 (report location) | **Existing**, with the height and main layer added for a terrain |
| Focus the brush settings | Cmd/Ctrl+P | **Existing:** focus properties |
| Undo / Redo / Save | Studio's | **Existing** |
| Stroke with the mouse | Left drag over the ground | **Existing** mouse button, a new meaning while a tool is chosen. Selecting other items is done from the tree, or after Escape |

**Keys a later tool may need** (each asked about with its step; defaults
are only suggestions):
- **Paint layer:** next layer, Alt+L. **Added with step 9** (2026-10-20).
- **Flatten:** take the height under the brush, **Alt+M** (added
  2026-10-21; Alt+T is Studio's Team menu). The flatten mode is a panel
  choice, with no key.
- **Ramp:** start and end points, **Alt+G** (added 2026-10-21; Alt+R is
  Studio's Run menu).
- **Slope limit:** Alt+G. Grid uses Cmd/Ctrl+G and its modifiers, so plain
  Alt+G needs a check.
- **Stamps:** next stamp, press stamp, stamp height and turn. Panel only,
  unless asked.

**Every new key:**
- has a `SceneEditorKeys` getter with its default;
- has a Studio key-map entry (constant, Mac and Windows defaults, the
  `RemapKeyMap` getter);
- is listed in the user guide (`Terrain/TerrainEditorInstructions.md`
  becomes Studio's terrain guide in step 12).

## Tests

- **Part A:** every existing scene-editor key does what it does today, on
  Mac and on Windows.
- **Overriding a key:**
  - set cursor-left to J in the key map file and restart Studio: J moves
    the cursor and ← doesn't;
  - delete the override, and the default is back.
- **An old key map file** (without the new keys) keeps the user's code
  keys and uses the defaults for the rest.
- **Part B:** each tool's keys are tested with that tool (steps 8 to 11).
