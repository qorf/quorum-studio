# Quorum Studio: terrain integration plan

Started 2026-10-20. **This is the master plan.** Each numbered step has
its own file in `Steps/`, and the background documents are in `Terrain/`
(see `README.md`).

## Where things are

| What | Repository and branch | Path |
|---|---|---|
| Studio | `quorum-studio`, `MakeAWish` (Vulkan is the default: `5ca3bead`) | `QuorumStudio/SourceCode` (304 files) |
| Standard library | `quorum-language`, `MakeAWish` | `Quorum/Library/Standard/Libraries` |
| Terrain project | `physics`, `master` | `AllTerrainVehicle` (DriveOpen, TerrainEditor, tests) |

Short names used in the step files:
- **L/** = the standard library's `Libraries/` folder;
- **W/** = `L/Game/Graphics/Worlds/`;
- **SC/** = `L/Interface/Controls/Scenes/`;
- **QS/** = `quorum-studio/QuorumStudio/SourceCode/`;
- **AT/** = `physics/AllTerrainVehicle/`.

**Rules carried over from the terrain work:**
- Never switch branches.
- The user builds and runs everything.
- Library changes are reviewed per change.
- One tool at a time for brushes.

## Goal

Terrain becomes an ordinary part of Quorum Studio's scene editor:
- added from the palette;
- saved in `.qs` scenes;
- shown in the 3D view;
- set up from a new **Properties panel** on the right, which works for
  every scene item and has more for terrains;
- edited with brushes later, one tool at a time.

A game loads it with `LoadScene`, like any model.

## Decisions

1. **Basic terrain first, no brushes.** A flat terrain from the palette,
   saved with the scene, shown in the editor, set up from the Properties
   panel. Brushes come after, one tool at a time (steps 7 to 11).
2. **Library first.** Studio's scene editor is mostly the standard
   library's `SceneEditor` (SC/). Almost all the work goes there, so other
   hosts get it too. Studio itself only changes for its layout (the panel)
   and its key map.
3. **A terrain stays at the origin.** Its heights are world positions:
   collision, streaming and `TerrainData:GetHeight` all assume that. So the
   move, turn and scale keys don't move it.
4. **Vulkan only, for now.** The region models, texture sets and splat
   shader are Vulkan classes. Studio's `MakeAWish` branch runs Vulkan by
   default. A scene with a terrain says so if loaded under OpenGL.
5. **Keys** (step 6):
   - **Use Studio's existing scene-editor keys wherever a meaning carries
     over.** Arrows move the scene cursor, which becomes the brush.
     Alt+= and Alt+- size the cursor, which becomes the brush size. Enter,
     Escape, Delete, Undo, Redo, Save, and Cmd/Ctrl+P to focus the
     properties all keep their meanings.
   - **Every new key is overridable**, through Studio's key map
     (`KeyMap` / `RemapKeyMap`).
   - Today the scene editor's keys are hard-coded in the library
     (`L/Interface/DefaultInputSets.quorum`), and Studio's matching key-map
     entries are never read. Step 6 connects them first.
6. **The Properties panel** (steps 4 and 5):
   - It replaces today's floating "Object Properties" dialog. That's the
     window with the Physics section, `SC/Dialogs/Item3DPropertiesDialog`,
     pinned over the editor's top-right corner.
   - The new panel is **docked on the right, inside the scene editor tab**
     (decided 2026-10-20), and shows the selected item's properties.
   - **For every item:** name, transform and physics, as today.
   - **For a terrain:** its size and regions, and collision and friction
     (step 5). Later: its materials (step 9), brush settings (step 7) and
     generation (step 10).
   - **How (step 4's write-up):** the existing properties dialogs dock in
     the panel instead of floating. Every later section is another
     `PropertiesDialog`, added with `SceneEditor:AddPropertiesDialog`; the
     panel shows all that apply, in order, each as a named group.
7. **Materials are files** (decided 2026-10-20), as in other engines.
   - **A material file** describes one material: a texture set or a
     texture, with its tile size.
   - **A terrain material file** lists a terrain's layers by referring to
     material files, plus how they blend.
   - **The scene's terrain entry** refers to its terrain material by path,
     as it refers to `terrain.json`.
   - **Every file is plain `.json`,** and says what it is in its
     `"Format"` field.
   - **Paths inside a file** are relative to that file.
   - **Physics settings** stay inline in the scene.
   - Models can use material files later.
8. **A new terrain is flat, with no splat and no materials** (decided
   2026-10-20). Adding and removing materials, and painting them, are
   step 9.
9. **A scene's own files go under the project's `Scenes` folder**
   (decided 2026-10-20). `Scenes` holds the `.qs` files, and under it:
   - `Scenes/Terrains/`: one folder per terrain (`Terrain1`, `Terrain2`, ...),
     with its `terrain.json` and region images;
   - `Scenes/Materials/`: material and terrain-material files (step 9).

   The editor makes them there, and the scene refers to them by
   project-relative paths, such as `Scenes/Terrains/Terrain1/terrain.json`.
   The folder names are `SceneProperties:SCENES_FOLDER`, `TERRAINS_FOLDER`
   and `MATERIALS_FOLDER`. A scene that still names a terrain at the old
   top-level `Terrains/...` finds it under `Scenes/` if it was moved there,
   and saves the new path.

## What exists today

**Terrain (now in the library, W/)**
- **Data and display:**
  - `TerrainData` holds the heights, splat weights, save and load
    (`terrain.json` plus region PNGs), and `RayCast`.
  - `WorldTerrain` builds and streams the region models, does collision,
    and draws texture sets and splatting.
- **Making a world:** `TerrainGenerator` has presets, stamps, erosion and
  auto paint.
- **Brushes:** `TerrainBrush`, `TerrainTool` and `TerrainStroke` do raise,
  lower, smooth, flatten, ramp, paint and regions.
- **Undo:** `TerrainStrokeEdit` is a standard `Edit`; `TerrainEditHistory`
  keeps them with a memory cap.
- **Preview:** `TerrainBrushPreview` draws the ring.
- **The reference editor** is `AT/SourceCode/TerrainEditor.quorum`. It has
  every tool with keys and speech, and is documented in
  `Terrain/TerrainEditorInstructions.md`.

**Studio's scene editor**
- **Layout:**
  - `QS/Interface/QuorumStudio.quorum:676-841` lays out three things: a
    left tab pane (Projects, Scene tree, Palette), a drag bar, and the code
    and editor tabs.
  - There's **no right-hand panel**.
  - A `.qs` file opens a `QS/Interface/Controls/VisualEditorTab`, which
    holds an `SC/SceneEditor`.
- **Adding items:** the palette (`SC/ScenePalette`) adds items through
  blueprints and previews (`SceneEditor:CreateItemPreview`,
  `ConfirmItemPreview`).
- **Selection:**
  - Selecting puts a highlight around the item
    (`SceneEditor:SetFilteredItems`, SC/SceneEditor.quorum:769).
  - The highlight takes the focus and forwards moves to the item.
  - The highlight code casts every 3D item to `EditorModel`.
- **Undo:** only adding and deleting items can be undone.
- **Saving:** scenes are JSON (`L/Game/Scenes/DefaultSceneManager`), and
  each item is identified by its `"Class"`. The editor overrides parts of
  it.
- **Camera:**
  - The editor camera is `EditorCamera3D`, an orbit camera.
  - The mouse rotates, orbits, dollies and pans it.
  - No keyboard keys move it (those behaviors exist but aren't bound).

## Steps

| # | Step | File | Library | Studio | Status |
|---|---|---|---|---|---|
| 1 | Worlds classes into the library | `Steps/01-WorldsInLibrary.md` | yes | no | **Files moved and folder fix applied 2026-10-20**; build, tests and commit remain (the user) |
| 2 | `TerrainItem`, terrain in scene files, no splat until painted | `Steps/02-TerrainItem.md` | yes | no | **Applied 2026-10-20**; build and `TerrainSceneCheck` remain (the user) |
| 3 | Terrain in the scene editor: palette, selection, tree, saving | `Steps/03-SceneEditorTerrain.md` | yes | icon (optional, not done) | **Applied 2026-10-20**; build and Studio test remain (the user) |
| 4 | The Properties panel, docked on the right, for every item | `Steps/04-PropertiesPanel.md` | yes | layout | **Applied 2026-10-20**; build and Studio test remain (the user). The six dialogs dock in the panel unchanged, instead of being split into sections |
| 5 | Terrain sections in the Properties panel: info and physics | `Steps/05-TerrainProperties.md` | yes | no | **Applied 2026-10-20**; build and Studio test remain (the user). Also fixes terrains being saved with collision off |
| 6 | Overridable scene-editor keys, and the terrain keys | `Steps/06-Keys.md` | yes | key map | Plan |
| 7 | Picking the ground and the brush cursor | `Steps/07-PickingAndBrushCursor.md` | yes | no | Plan |
| 8 | Sculpt brushes: raise, lower, smooth, flatten, ramp | `Steps/08-SculptBrushes.md` | yes | no | Plan (one tool at a time) |
| 9 | Materials (as `.json` files) and painting: add and remove materials on a terrain, and paint them | `Steps/09-MaterialsAndPainting.md` | yes | no | Plan |
| 10 | World tools: presets, stamps, erosion, auto paint | `Steps/10-WorldTools.md` | yes | no | Plan |
| 11 | Regions: add and remove | `Steps/11-Regions.md` | yes | no | Plan |
| 12 | Accessibility: describing the ground, spoken values | `Steps/12-Accessibility.md` | yes | no | Plan (with every step) |
| 13 | Terrain in games: loading scenes, streaming, collision, cameras | `Steps/13-Games.md` | yes | no | Plan |

**Order and dependencies:**
- **1 → 2 → 3** gives a working basic terrain.
- **4** doesn't need terrain and can be done in parallel with 2 and 3.
- **5** needs 3 and 4.
- **6** can start any time, and must be in before 7.
- **8 to 11** each need 7.
- **12** goes along with every step.
- **13** needs 2.

## Before anything else: risks to check in Studio (Stage 0)

These are quick checks, each of which could change the plan. They come
from `Terrain/TierFour.md` and `Terrain/QuorumStudioPlan-2026-10-19.md`.

1. **3D under Vulkan inside the editor panel.** The 3D lighting pass reads
   the G-buffer across the whole window but draws into the panel's
   viewport, so the scene may be shrunk or offset inside Studio. If so, a
   small library fix comes first. The scene editor needs it anyway.
2. **Texture group 0.** A terrain's ground textures, splat texture and
   layer table must be among the first textures the renderer sees (group
   0, the first 255). The terrain editor does this by loading them before
   any label. Studio has many glyph textures already, so a terrain added
   later may draw plain. A new terrain has no textures (step 2), so this
   is checked with the first material in step 9. If it fails, the fix goes
   in the library before step 9's interface.
3. **Studio's own copy of the standard library** (`Library/Standard`,
   dated 2026-09-25) and its runtime jar must be rebuilt from `MakeAWish`.
   Games run from Studio use that copy, and the editor uses the jar.
4. **Frame budget.** Terrain edits rebuild meshes within the frame, and
   inside Studio that stalls the whole program. That's fine for the basic
   terrain. Measure it before the brushes (step 8).

## Corrections to the 2026-10-19 plan

Found while mapping the scene editor on 2026-10-20:
1. **Selecting a terrain would fail.** `SceneEditor:SetFilteredItems` casts
   every selected 3D item to `EditorModel` for its highlight, and the
   planned `EditorTerrain` isn't one. Step 3 adds a
   `TerrainSelectionHighlight` and a branch for it.
2. **Clicking can hit region models.** Clicking in the 3D view
   (`SelectScene3DItemBehavior`) tests every item's box in the layer, and
   so does the cursor's Enter (`EditorCursor3D:GetItemsAtCursor`). A
   terrain's box is all of its regions. Step 3 makes a click on a terrain
   select it through `TerrainData:RayCast`, and keeps the region models out
   of item lists.
3. **The editor must save the terrain's own files** when the scene is
   saved, once edits exist (from step 5 on). `SceneEditor:Save` only
   writes the `.qs` today.
4. **The editor must call the terrain's `Update` each frame,** with the
   editor camera's position. Splat layers only become ready after a
   frame, and streaming needs it.
5. **Studio's branch** is now `MakeAWish`, not `Terrain`. The plan's
   commands also used `/Users/stefik/`; the paths are `/Users/stefika/`.

## Found along the way (not terrain)

- **Skyboxes are never saved.** `DefaultSceneManager:SaveSkybox` exists but
  is never called, so a scene's skybox is loaded but not saved.
- **Some of Studio's key-map defaults don't match the real bindings.** For
  example, Mac `GetSelectionScaleXDownKey` is Cmd+Shift+A in the key map,
  but the real binding is Alt+A. Step 6 fixes these.
- **`SceneEditorInputTable`'s constructor names it
  `"CodeCompletionMenuRoot"`.** `DefaultInputSets` renames it, so it works,
  but it's a copy-and-paste leftover.
- **Property edits can't be undone** (only adding and deleting can). The
  Properties panel (step 4) is a natural place to start making edits
  undoable.
- **The keyboard can't move the 3D camera.** The behaviors exist in
  `DefaultInputSets` (about lines 1514-1546) but aren't bound. That matters
  for terrain work without a mouse (step 6).

## Not in this plan (not built yet)

- Water, sea level and lakes (Items 38, 39, 41 and 43);
- distance fog (Item 40);
- the ground look (Item 42).

These are all "for review" in `AT/`. Once applied, each gets a step here:
a water surface as a scene item, and its settings in the Properties panel.
