# Open Worlds Tier 4: editing tools (plan)

Written 2026-10-18.

## Status

**2026-10-19: stages 1 to 3 are done and working.** TerrainEditor paints
(after library item 20 and two project fixes), uses texture sets with
height blending, and has a user guide (`TerrainEditorInstructions.md`).
The world-wide keys (Shift N, X, U) haven't been reported on yet. Stage 4
(Quorum Studio) is not started. Its plan is now in
`../QuorumStudio/QuorumStudioPlan.md` (2026-10-19, for review): first Item
71, the Worlds classes in the standard library and terrain in Studio's scene
files and palette; then Item 70, brushes in the scene editor, one tool at a
time. The stage 4 section below is the earlier record.

The notes below are the record from 2026-10-18.

**Stages 1 and 2: done.** `RunWorldTests` 20 of 20 (2026-10-18). No
library changes.

**Stage 3 (first part): running.** Checked by the user on 2026-10-18: mouse picking, raising, and undo (including undoing past the last edit). The new world is auto painted (rock showed under the brush). The other tools, the world-making keys, and `RunWorldTests` with the generator test are not reported on yet. No library
changes. It adds `SourceCode/TerrainEditor.quorum`; make it the project's
main file to run it. `RunWorldTests` should now show 21 of 21, with the new
ray cast test.

New for Stage 3:
- **`TerrainData:RayCast(origin, direction, maximumDistance)`.** Mouse
  picking without physics, tested headless by `TerrainDataRayCastTest`.
  - It marches half a sample at a time over the part of the ray between
    the lowest and highest ground, then narrows down the crossing by
    halving.
  - It crosses gaps where there is no region.
- **`TerrainData:ForgetRegionsNotLoaded`:** before saving, regions removed
  with the brush are taken off the saved list, so loading doesn't bring them
  back. No files are deleted.
- **`WorldTerrain:UpdateRegionModels(layer)`,** for Build mode:
  - removes models whose region is gone, and makes models for new regions;
  - if a new region is outside the splat texture's area, makes the splat
    texture again and rebuilds every model's texture coordinates.
- **`TerrainBrushPreview is Model`:**
  - two yellow rings, at the brush's edge and at half its size, floating
    just above the ground;
  - updated in place every time the brush moves;
  - double-sided, and casts no shadows.
- **The region brush** now also marks the region for the splat texture, so
  a region added again doesn't show old paint.
- **`TerrainEditor.quorum`:**
  - loads `EditorTerrain/` or makes a new 8 by 8 region world from noise
    (grass and rock by slope);
  - Build mode, everything in memory (so undo is safe), no collision, no
    physics;
  - every mouse control has a key (below), and every change is printed,
    shown at the bottom of the screen, and announced to a screen reader.

  `EditorTerrain/` is not in `.gitignore`, since worlds made here are worth
  keeping and sharing; add it if not.

**TerrainEditor's controls** (first pass; the user will adjust them):

| Action | Mouse | Keyboard |
|---|---|---|
| Use the brush | Left button held | Space held |
| Move the brush | Pointer over the ground | Arrow keys (Shift: 8 times further), relative to the camera |
| Turn around the brush | Right drag, sideways | Q and E |
| Tilt | Right drag, up and down | Home and End |
| Zoom | Wheel | Page Up and Page Down |
| Move across the ground | Middle drag | Arrow keys |
| Look at the brush | | C |
| Tools | | 1 raise, 2 lower, 3 smooth, 4 flatten, 5 ramp, 6 paint, 7 blend paint, 8 add region, 9 remove region |
| Size, strength, falloff | | `[` `]`, `-` `=`, `;` `'` |
| Paint texture | | L (grass, rock, dirt, sand) |
| Flatten to the stroke's start, or a set height | | F; T sets the height from under the brush |
| Slope limit | | G (any, flat only, steep only) |
| Ramp start and end | | R, then R |
| Cancel the stroke | | Escape |
| Undo, redo, save | | Control or Command with Z, Y (or Shift Z), S |
| Describe here, the area, the keys | | I, A, H |

**Stage 3's world-making tools: written, waiting for a compile and run**
(2026-10-18). No library changes. `RunWorldTests` should show 22 of 22.
- **`TerrainGenerator`** (new, in `Worlds/`), tested by
  `TerrainGeneratorTest`:
  - **Presets:** rolling hills, mountains (ridged noise), plateaus
    (terraced), canyons (ridged multifractal), flat. They are laid out by
    world position and repeat with the same seed.
  - **Stamps:**
    - five shapes made in code: hill, mountain, mesa, crater, ridge;
    - any size and turn, sampled smoothly;
    - three ways: add, subtract, or raise to (the stamp stands on the
      ground under its center, and higher ground is left alone).
  - **Erosion:**
    - rain: Beyer's droplet method, seeded, so it repeats;
    - then slope settling past 40 degrees;
    - both move ground without adding or losing any (tested).
  - **Auto paint:** rock between 25 and 40 degrees of slope, sand below
    height 3 (fading over 1.5), dirt in hollows (0.4 below the average
    within 4 samples), grass elsewhere.
- **`TerrainStroke:BeginOperation` and `CopyArea`** let any operation be
  undone like a stroke: copy the area, change the data, then End.
- **New worlds** now use the rolling hills preset and auto paint.
- **TerrainEditor keys** (each operation is one undoable edit):

  | Key | What |
  |---|---|
  | N | next preset; Shift N remakes the whole world with it (a new seed each time) and auto paints it |
  | K | next stamp shape |
  | , and . | stamp lower and higher |
  | / | turn the stamp 45 degrees |
  | Enter | press the stamp into the ground at the brush, as big as the brush |
  | X | erode under the brush (one drop per sample); Shift X the whole world (one drop per 8 samples) |
  | U | auto paint under the brush; Shift U the whole world |

- **First run (2026-10-18):**
  - Presets cycle and stamps place.
  - Erosion and auto paint run.
  - The arrow keys speak the ground.

  Fixed after it:
  - **Erosion dug pits and piled spikes** (the steepest slope jumped to 72
    to 82 degrees after some passes). Ground picked up, and what a drop
    leaves when it stops, is now shared among samples within 2 (Beyer's
    erosion radius), not one sample.
  - **Auto paint wrote weights even when they didn't change,** so each repeat
    made a new splat texture and used up a texture place. Now it only writes
    changed weights, and the editor drops the edit when nothing changed
    (tested).
  - "1 regions" now reads "1 region".

  Second run (2026-10-18): no more spikes; auto paint repeats now say
  "changed nothing" instead of making a texture. Found: **eroding the same
  spot again and again levels it flat** (13.7 everywhere, 2 degrees), since
  drops can't carry ground out of the brush's square, so it all settles back
  inside. **Fixed (user's choice):** drops now start under the brush and
  may run on one brush size past it, so ground is carried downhill and away
  (`TerrainGenerator:Erode` and `ErodeWithRain` take a start rectangle and a
  larger run rectangle; tested). Slope settling covers the larger area too,
  and undo copies it. Waiting for a run.
  Not yet checked: Shift N (remaking the world), Shift X and Shift U, and
  `RunWorldTests`.

  Third run (2026-10-18), three issues from the user:
  1. **Paint turns all the ground grey.** Likely cause, to confirm: the new
     splat texture lands outside texture group 0. The splat shader finds
     it, and the four ground textures, by index in the bound group
     (`VulkanChanges.md` #13, #19), so a splat texture in a later group
     reads the wrong textures everywhere. The first splat texture is made
     at startup, early (index 5). Later ones come after the labels' glyph
     textures, which may fill group 0 (the outlined info line makes many).
     **Diagnostic added, and confirmed (fourth run):** the startup splat
     texture is at index 5, group 0. By the first paint stroke about 66
     more textures exist (very likely the labels' glyphs), so every new
     splat texture lands at index 72 and up, in group 2, where the splat
     shader can't read it. **There is no fix in this project alone:** a
     texture can't be changed after it is made (#20), so each paint makes a
     new one, placed after everything else.
     - **Recommended library fix: #20, update a texture in place.** The
       splat texture keeps index 5 for good, and painting stops using up
       texture places. **Applied 2026-10-19 (`Item20.md`),** with
       `WorldTerrain` loading the same splat `Texture` again while its size
       is unchanged. **First run (2026-10-19): 22 of 22 tests; no grey
       and no new splat textures, but paint doesn't show at all,** though
       the data changes (rock 0 to 98 percent under the brush). Every step
       checked on paper is right: the load reaches `RequestLoad`; the copy,
       barriers, image handle and staging mapping are as for a first load.
       **Diagnostic added:** after each paint stroke, "Paint check" prints
       the painted layer's weight at the brush in the data and in the splat
       texture's pixel copy, the splat texture's index (still 5 if it was
       updated in place; the index printed is from before the reload, so
       look at the next stroke's line), and the update count.
       **Result:** rock is 1.0 in the data and in the texture's pixels; the
       index stays 5 after an update was processed (so the in-place path
       ran, not a new load); updates count up. So the pixels reach
       `VulkanTextureManager`, but the image on screen doesn't change.
       **Next check:** `SourceCode/TextureUpdateCheck.quorum`, a separate
       main with no library change. It reloads one 2D texture in place,
       red and blue in turn, every second. If the square changes color,
       in-place updates work and the fault is on the 3D or splat side; if
       not, the update itself doesn't reach the GPU image.
       **Result: the 2D square changes color, and the index stays 0.** So
       in-place updates reach the GPU, and the fault is on the 3D side.
       **Next check:** the same program now also draws two 3D boxes with the
       texture: one as its own texture, one as a texture override (as the
       terrain gets its splat texture). **That run:** both boxes were black
       from the start, so the check told nothing about updates. The likely
       cause is the test's light: its color was never set (Light keeps a
       `Color` field that starts unset), and DriveOpen always sets its sun's.
       The default 3D shader has no "index 0 means no texture" rule, so the
       texture's index 0 isn't the cause. The light's color is now set.
       **Rerun: the square and both boxes change color.** So in-place
       updates work for 2D, 3D and texture overrides; Item 20 is fine.
       **Cause found: the splat texture was mirrored along z.**
       - libGDX's native `gdx2d_set_pixel` stores row y as the y-th row from
         the top, so the PixelMap's first row is the texture's v = 0.
       - `MeshVulkan` turns every mesh's v into 1 - v (right for images from
         files, wrong for this generated map), so sample z read row
         depth - 1 - z.
       - Paint therefore showed across the map's middle line along z, not
         under the brush. The startup rock-by-slope pattern was mirrored
         too, which wasn't noticeable on random hills.

       **Fixed** in `WorldTerrain:MakeSplatTexture`, which now writes rows
       flipped; that covers region and far models. Waiting for a run:
       paint should show under the brush, and rock should sit on the steep
       slopes at startup.
       **Rerun:** still nothing changes, and **no rock is visible even at
       startup**, though the data has rock on slopes. So the terrain isn't
       showing its splat weights at all; painting was never the problem.
       Two ways the splat shader looks like grass whatever the weights:
       - no color override (the ground textures' places): flat olive;
       - no texture override (the splat texture): all-grass weights.

       The splat map's flip (above) stays: it is correct either way.
       **Diagnostics added:**
       - the shader shows **magenta** for the first case and **cyan** for
         the second, both temporary;
       - the editor prints "Terrain drawing check" once the ground textures
         are ready (region model 0's program, texture override, color
         override, and the ground textures' places), or after 5 seconds if
         they never are.

       Splatting under Route 2 (`Item9.md`) has never been confirmed on
       screen, so a Route 2 problem is possible too.
       **Result:** magenta for a moment, then the grass texture everywhere
       (never cyan). The check line: region model 0 draws with the Terrain
       Splat Shader Program, texture override index 5 (the splat texture),
       color override set, ground textures at 1 to 4 in group 0. So the
       plumbing is right; the weights the shader reads come out all grass.
       The vertex shader passes texture coordinates straight through.
       **Next diagnostic:** the shader draws the weights as flat colors
       (grass green, rock red, dirt blue, sand yellow). Mostly green with red
       on slopes means the weights are right and the blending is wrong. One
       flat color means the texture's contents or coordinates are wrong.
       **Result: solid green everywhere,** even on slopes with rock in the
       data. That is the shader's fallback for weights that add up to
       nearly 0, so the splat texture's pixels were all zero.

       **Root cause found (project side, not the renderer):**
       - `PixelMap`'s global blending starts as source-over, and libGDX's
         `gdx2d_set_pixel` blends when it is on.
       - A splat pixel's alpha is the sand weight, usually 0. Setting a pixel
         with alpha 0 over a new, empty map left it empty, so every weight was
         wiped before upload.
       - The red and blue test texture had full alpha, so it was unaffected.
       - The same wiped the **saved splat images**
         (`TerrainRegion:GetSplatImage`), so splat data never survived saving
         either.

       **Fixed:** `MakeSplatTexture` and `GetSplatImage` turn blending off
       (`Blending:NONE`) while writing, then put the previous setting back.

       The temporary diagnostics are removed (shader colors and the per-stroke
       and startup check lines); the one-time "Splat texture check" stays.
       Item 20 and the z flip stay. Splatting has probably never shown
       correctly before this, in DriveOpen either, so the next run is the
       first real look.
       **Confirmed working (2026-10-19):** rock shows on the slopes at
       startup, and paint shows under the brush. This also confirms Item 20
       on screen, the z flip, and splatting under Route 2. Remaining limit: adding regions
       outside the splat area changes its size, which still makes a new
       texture past group 0 (grey until a restart). Documented in the
       user guide.
     - #19 (texture groups sized by the right limit) would only delay it.
  2. **Ramp did nothing visible.** The ramp worked, but its ends were at
     12.83 and 12.96, so it was level and acted like flattening. Now the
     editor gives the ramp's slope and warns when it is under 3 degrees.
  3. **Smooth did nothing visible.** Smoothing averaged only the 8 nearest
     samples (1.5 units across, against a 6-unit brush), so even slopes
     stayed as they were, and quick clicks moved very little. The average
     now covers a quarter of the brush's size (1 to 3 samples each way).
     The guide now says to hold the brush.

  Also: "add region" over land now explains that it works past the edge of
  the land.

  Expected, not changed: the ridge stamp makes near-vertical sides (85
  degrees) at height 15 on a brush of radius 6. Use a bigger brush, or
  lower the stamp with the comma key.
- **Watch for:**
  - how long Shift X and Shift U take on the whole world;
  - whether the presets' heights suit DriveOpen's vehicle;
  - each auto paint uses up a splat texture place, as painting does.

Not done: importing real elevation data (16-bit height maps already
import with `ImportHeightMap`, but not from inside the editor), and
choosing a new world's size inside the editor.

Things to watch in the first run:
- **Turning directions.** Which way Q and E, and right dragging, turn, and
  whether Left and Right move the right way on screen. The world is
  left-handed, so a sign may need flipping.
- **Mouse picking.** First run (2026-10-18): the brush was mirrored left to
  right from the pointer, because `Camera:GetPickRay` flips x. Fixed in
  `PickBrushFromMouse` by mirroring x before the call. **Confirmed working
  in the second run.**
  The same run: raising worked, and the area report spoke.
- **Paint.** Each paint stroke (and each undo or redo of one) makes a new
  splat texture. After about 20, a warning is announced; after about 25,
  paint stops showing until the editor is restarted (`VulkanChanges.md`
  #20).
- **Frame rate** while holding a large brush.

New files in `SourceCode/Worlds/`:
- `TerrainTool`: the tool and operation constants and their names.
- `TerrainBrush`: the settings and the falloff.
- `TerrainStroke`: Begin, Apply, End and Cancel, and all the operations.
- `TerrainStrokeEdit is Edit`: undo and redo of one stroke.
- `TerrainEditHistory`: undo and redo lists with a memory limit (256 MB by
  default).

Added to the existing classes:
- `TerrainRegion`: `Copy`, `CopyFrom`, `GetHeightAt`, `GetSplatAt` and
  `GetMemorySize`.
- `TerrainData`: `RestoreRegion`.

New tests in `Tests/Worlds/`, run by `WorldsTester:TestEditing`:
- `TerrainBrushTest`;
- `TerrainStrokeSculptTest`;
- `TerrainStrokePaintTest`;
- `TerrainStrokeUndoTest`.

`RaiseArea` and `PaintSplat` are unchanged, and DriveOpen still uses them.

What was built, against the plan below:
- **Tools:**
  - Sculpt: raise, lower, smooth and ramp.
  - Height: flatten, to a set height or to where the stroke starts.
  - Texture: paint a layer, and blend.
  - Region: add or remove the region under the brush.
- **Brush settings:**
  - size, strength, gamma;
  - a shape (from numbers, or from a gray image in a running game), with
    rotation;
  - a slope range and a ramp.

  Jitter is left for later.
- **Strength:**
  - Raise and lower move by strength times seconds times the falloff.
  - Everything else moves 1 - e^-(strength × seconds × falloff) of the way
    to its goal. So the result is the same at any frame rate; this is
    tested.
- **Painting at low strength:** a move too small to show in the 1/255 steps
  takes one whole step instead, so a slow brush at a high frame rate still
  paints.
- **Undo copies whole regions** the first time a stroke touches them. Copying
  only the touched rectangle is a later improvement.

Known limits:
- **Removing a region** only removes it from memory. Its saved files stay,
  so streaming or loading brings it back. Terrain3D deletes them on save;
  we'll need the same before region removal is used on saved worlds.
- **Undo and streaming:** undoing a stroke whose region has since been
  unloaded loads the copy back into memory. In the terrain editor, data
  streaming should stay off, or keep edited regions loaded.
- **`WorldTerrain` and regions removed by hand:** its region models for
  removed regions aren't taken away yet. Stage 3 handles this.

### Stage 3 decisions (the user, 2026-10-18)

- The program is **`TerrainEditor.quorum`**, its own main. **No vehicle and
  no driving:** it only edits terrain. DriveOpen keeps its E/Q/P keys as
  they are.
- **Every mouse control has a keyboard control too.** Claude picks sensible
  keys, and the user will adjust them later.


Tier 4 in `OpenWorlds.md` is the editing tools:
1. a brush system;
2. sculpt operations;
3. painting;
4. undo and redo;
5. mouse picking with a brush preview.

The user wants them now for a practical reason: **bigger and more realistic
test worlds** are needed before the renderer issues in
`LibraryChangesPlan.md` can be worked out properly. So this plan puts
"tools that make good test worlds quickly" first, and full Quorum Studio
integration second.

Sources:
- Quorum Studio, read on 2026-10-18 (`/Users/stefik/Repositories/quorum-studio`).
- The standard library's scene editor, which Studio hosts.
- Terrain3D's editor (`src/terrain_3d_editor.cpp`, `project/addons/terrain_3d/src/`).
- Our `Worlds` classes.

Paths:
- **QS/** = `quorum-studio/QuorumStudio/SourceCode/`
- **STD/** = `quorum-language/Quorum/Library/Standard/Libraries/`

## What Quorum Studio has today

The most important finding: **Studio's visual editor is mostly in the
standard library, not in Studio.**

### The scene editor

- **`SceneEditor`** (STD/Interface/Controls/Scenes/SceneEditor.quorum, about
  2200 lines) is a `Control` and a `DefaultSceneManager`. It edits `.qs`
  scene files, which are JSON.
- **Studio's side is thin.**
  - `QS/Interface/Controls/VisualEditorTab.quorum` makes a `SceneEditor`
    and hosts it in a tab.
  - `CodeEditorTabPane:OpenNewTab` chooses the tab by file extension
    (`quorum`, `qs`, `csv`; hard-coded there and in
    `QuorumStudio:IsSupportedFile`).
- **The 3D view is a real `Layer3D` in Studio's own game.** Studio is itself
  a Quorum `Game`. `EditorLayer3D` (`is Layer3D`) is added to Studio's game
  when the tab is shown, and its viewport follows the control's bounds.
  Terrain models would be drawn there like any other item.
- **Camera:** `EditorCamera3D`, an orbit camera with mouse controls. The
  keyboard camera movement is commented out in `SceneEditor:Update`.
- **Picking:** left click casts a ray (`Camera:GetPickRay`) against each
  item's unrotated box. There's no mesh or height-map picking.
  `HeightmapTerrainShape:RayCast` exists in the library.
- **Placing items:**
  - The `ScenePalette` is a tree of items, built from asset packs (JSON
    tile sets).
  - Choosing one makes a preview, which is moved with the keyboard and
    confirmed with Enter.
  - **There are no tool modes** (select, move, paint), no brushes, and no 3D
    gizmos or 3D mouse dragging.
- **Properties** are edited in dialogs (`PropertiesDialog` and subclasses),
  with text fields, check boxes and radio buttons only. **The library has no
  slider, combo box, spinner or color picker control.**

### Undo, files and running

- **Undo:** `STD/Interface/Undo/Edit` and `EditManager`, with plain undo
  and redo lists and no merging.
  - The scene editor has only insert and delete edits. Moves and property
    changes aren't undoable.
  - Studio's `UndoBehavior` and `RedoBehavior` switch on the type of the
    focused editor, so a new kind of editor needs a branch in each.
- **Files:**
  - Scenes refer to assets by project-relative paths.
  - The game loads the same `.qs` file at run time (`LoadScene`).
  - Studio builds and runs the game as a separate program, so the editor
    and the game share only files.
  - Our terrain folder (`terrain.json` plus PNGs per region) fits this
    model as it is.
- **Visual tabs have no "dirty" flag:** saving always rewrites them.

### Accessibility patterns to build on

- **Spoken updates:** `SceneEditor:NotifyAccessibility` sends text to the
  screen reader.
- **A keyboard cursor** (`EditorCursor3D`): grid-snapped, moved with the
  arrow keys. It speaks its position or the items under it.
- **Moves, rotations, camera changes and grid settings are spoken.**
- **Report keys:** Ctrl+Shift+1 to 3 report location, direction and the
  items in view.
- **`SceneTree`:** a navigable tree of layers and items.
- **There is no sonification.**

### Risks found

1. **Renderer.** Studio runs on **OpenGL** unless started with `vulkan`.
   Our terrain builds Vulkan meshes (`ModelManagerVulkan`), as the library's
   own `Terrain` does, and the splat shader is Vulkan-only. So inside
   Studio, terrain needs Vulkan, or an OpenGL path in `WorldTerrain`.
2. **Library drift.** Studio compiles user programs against its own copy of
   the standard library (`QuorumStudio/Library/Standard`, from 2026-09-25).
   That copy predates frustum culling and everything since. The `Worlds`
   classes would have to reach that copy, or the user points Studio at the
   current library (an existing option).
3. **Checked, not a problem:** `EditorLayer3D` in the current library has
   fewer `Add` overrides than Studio's copy. But the remaining
   `Add(Item3D)` still sets the scene editor's input group, and the others
   went with the `Layer3D` actions they overrode. (Recorded as D in
   `VulkanChanges.md`.)
4. **The editor shares Studio's frame.** A brush that rebuilds a lot every
   frame stalls the whole IDE. Region-sized, in-place updates (library
   order 7) are what make this workable.

## What Terrain3D's editor does (the model to follow)

- **Tool times operation.** Tools: region, sculpt, height, texture, color,
  roughness, auto shader, holes, navigation, instancer. Operations: add,
  subtract, replace, average, gradient. "Raise" is sculpt and add; "smooth"
  is sculpt and average; "flatten" is height and replace.
- **One brush settings record** (a dictionary):
  - size, strength, height, color, roughness;
  - a brush image (the shape), with spin and align-to-view;
  - gamma (how sharp the falloff is);
  - a slope range filter;
  - jitter;
  - the texture to paint;
  - auto-regions (sculpting off the edge makes new regions).
- **Strokes:** `start_operation`, `operate` each mouse move, and
  `stop_operation` on release.
  - The first time a stroke touches a region, a copy of that region is
    kept.
  - At the end, the copies and the edited area become one undo action
    (`_store_undo`).
  - Undo puts the copies back; redo puts the edited regions back.
- **Picking:** Terrain3D reads the ground position from a depth render of
  the terrain. We'd ray-cast against the height data instead (below).
- **UI:** a toolbar of tools, a settings bar (sliders, a brush shape list,
  the texture list), and a live info panel.

## What we already have

- **`TerrainData`:**
  - heights and splat weights per sample, by world position or by sample;
  - `RaiseArea` (a smooth mound) and `PaintSplat` (moves weights toward a
    layer);
  - an edited area and a splat edited area;
  - `AddRegion` and `RemoveRegion`;
  - `GenerateHeights` (noise), `ImportHeightMap`, `GenerateSplatBySlope`;
  - save and load per region.
- **`WorldTerrain`:**
  - `Rebuild` rebuilds only the touched regions, now in place (library
    order 7);
  - the splat texture is rebuilt for the painted area;
  - streaming and far models.
- **DriveOpen:** E and Q (raise and lower), P (paint) and C (layer), all
  aimed a few units ahead of the vehicle. No mouse, no undo, no brush
  settings.

## The plan

Four stages. Stages 1 to 3 are **project code in AllTerrainVehicle, no
library changes**, following the standing rule. Stage 4 is the Studio
integration, which needs library and Studio changes, each shown first.

### Stage 1: the brush engine (headless, tested)

New classes in `Libraries.Game.Graphics.Worlds`, beside `TerrainData`:

- **`TerrainBrush`:** the settings, with getters and setters and sensible
  defaults:
  - size (radius in world units), strength, gamma;
  - falloff (smooth by default, or an image: a `PixelMap` read as alpha,
    with spin);
  - slope range (only affect ground between two angles);
  - target height (for flatten);
  - layer (for painting);
  - jitter.
- **Tool and operation**, as constants on a `TerrainTool` class (Quorum
  has no enums):
  - **Sculpt:** add (raise), subtract (lower), average (smooth), gradient
    (ramp between two points).
  - **Height:** replace (flatten to a height).
  - **Texture:** paint a layer; average (blur the weights); auto (apply
    the slope rule under the brush).
  - **Region:** add or remove whole regions.
  - Later, when Tier 2 has the data: color, roughness and holes.
    Navigation and instancing wait for Tier 5.
- **`TerrainStroke`:** one press-to-release.
  - `Begin(TerrainData, TerrainBrush)`, `Apply(worldX, worldZ, seconds)`,
    then `End`.
  - Strength is scaled by the time since the last apply, so a brush acts
    the same at any frame rate.
  - Spacing: when the mouse moves far between frames, it applies at points
    along the way, so fast strokes don't leave gaps.
  - **Snapshots for undo:** the first time the stroke touches a region, it
    copies that region's heights and splat weights (`TerrainRegion` gets
    `Copy`).
- **The operations work sample by sample over the brush's square,** using
  `GetSampleHeight` and `SetSampleHeight`, and `GetSampleSplat` and
  `SetSampleSplat`. They add to the edited areas, as `RaiseArea` and
  `PaintSplat` do. `RaiseArea` and `PaintSplat` stay, as one-line users of
  the new code.
- **Tests (`RunWorldTests`):**
  - each operation on a small flat terrain, checking exact heights;
  - falloff and gamma;
  - the slope filter;
  - a stroke across a region edge;
  - frame-rate independence;
  - snapshots (a region touched twice is copied once).

### Stage 2: undo and redo (headless, tested)

- **`TerrainStrokeEdit is Edit`**, using the library's
  `Libraries.Interface.Undo.Edit`, so it later drops straight into Studio's
  `EditManager`.
  - It holds the region copies from before the stroke and, made at `End`,
    copies of the same regions after it.
  - `Undo` and `Redo` put one or the other back, and mark the whole area as
    edited, so `WorldTerrain:Rebuild` redraws it.
  - Its name is spoken: "Undo raise", "Redo paint rock".
- **One edit per stroke,** so no merging is needed. That is exactly the
  granularity Terrain3D uses.
- **Memory:**
  - A 256-sample region is about 260 KB of heights plus 260 KB of weights,
    so a stroke touching 4 regions keeps about 4 MB (before and after).
  - Cap the undo list by total size (for example 200 MB), dropping the
    oldest. `EditManager` has no limit, so the terrain editor keeps its own
    count.
  - Later improvement: copy only the touched rectangle, not the whole
    region.
- **Region add and remove** are edits too, holding the removed region.
- **Saving:** unsaved edits are tracked per region (`TerrainData` already
  knows which regions are modified, for streaming). Undo marks them
  modified again.
- **Known limit:** undoing or redoing a paint stroke rebuilds the splat
  texture, which uses up a texture place (`VulkanChanges.md` #20). Fine for
  testing; the real fix is that library item.
- **Tests:** undo and redo for every tool, undo across region edges, redo
  after undo, a new stroke clearing redo, and the size cap.

### Stage 3: an editor in the game (DriveOpen first, then its own main)

This is the fastest way to make better test worlds, and it proves picking,
preview and the accessible controls before any Studio work.

- **Picking: `TerrainData:RayCast(origin, direction)`.**
  - March along the ray a sample at a time (a 2D grid walk over the cells),
    then test the two triangles of each cell. This is the same triangle
    split the mesh and collision use.
  - It works on any loaded region, with or without collision pieces, and
    with no physics running. That matters in an editor, where collision
    exists only near the vehicle.
  - The ray comes from `Camera:GetPickRay`, as the scene editor does.
  - Tested headless with known rays.
- **Brush preview:**
  - A ring that follows the ground: one model, a thin strip with a vertex
    every few degrees, its heights read from `TerrainData`.
  - It moves every frame using in-place vertex updates (library order 7),
    so it doesn't leak.
  - It's drawn with the default shader and a bright color override.
  - The ring's radius is the brush size. An inner ring shows where
    strength drops off.
- **Controls (a "terrain edit mode" in DriveOpen, toggled with a key):**
  - Mouse: left drag applies, right drag orbits, the wheel zooms.
  - Keys:
    - number keys choose the tool;
    - `[` and `]` change the size;
    - `-` and `=` change the strength;
    - Ctrl+Z undoes and Ctrl+Y redoes;
    - S saves.
  - Every change is spoken and printed: "Raise, size 8, strength 0.5".
- **Keyboard brush cursor (accessibility, from the start):**
  - The arrow keys move the brush over the ground a sample step (or a grid
    step) at a time, and Space applies one dab.
  - The cursor speaks:
    - its position, height and slope;
    - the main layer under it ("x 120, z 64, height 12.5, slope 18
      degrees, rock");
    - a summary of the area after each stroke ("raised 3 metres at the
      center").
  - A report key describes the area under the brush: lowest, highest,
    steepest, and the main layers. This follows the scene editor's
    Ctrl+Shift report keys.
  - An optional idea, to discuss: sonify height under the cursor (pitch
    rising with height) while moving.
- **World-making tools for test worlds** (the motivation):
  - **Generate:** fill a rectangle of regions from noise, with presets:
    rolling hills, mountains, a plateau with cliffs. This uses
    `GenerateHeights`, plus a ridge function and terracing.
  - **Stamp:** press a height image (a mountain, a crater) onto the ground
    with add, subtract or replace, at a size and rotation. GRW's "stamp a
    mountain in one click" comes from this; it's the brush image at a large
    size, with replace.
  - **Erode:** a simple, slow hydraulic or thermal erosion pass over a
    rectangle, for realistic slopes and valleys. It's a tool, run once, and
    undoable.
  - **Auto paint:** run the slope and height rules over a rectangle (the
    existing `GenerateSplatBySlope`, with more rules).
  - **New world:** pick a size in regions and a preset, then save it. A
    folder then is a test world anyone can load.
- **Its own main, `TerrainEditor.quorum`,** once DriveOpen's mode works.
  It's a fly camera with no vehicle, loading and saving a terrain folder.
  This becomes the reference for the Studio editor, and is the main way to
  make test worlds until Studio has it.
- **Performance targets:**
  - a 20-unit brush, dragged, at 60 frames a second with 64 regions loaded;
  - a stroke's rebuild cost stays within the regions it touches.

  Profile with `profile.sh` if it isn't met. The likely costs are normals
  for a whole region per dab, and collision rebuilds. In edit mode,
  collision can wait until the stroke ends.

### Stage 4: Quorum Studio integration (library and Studio changes)

Two ways in. **Recommended: (a) first, then (b).**

- **(a) A terrain editor tab.**
  - Studio opens a terrain folder's `terrain.json` in its own tab, with no
    scene around it.
  - New library control `TerrainEditor` (in
    `Libraries.Interface.Controls.Scenes` or a new `...Controls.Terrain`):
    - it holds an `EditorLayer3D` (or its own `Layer3D`), a
      `WorldTerrain`, the brush engine and an `EditManager`;
    - it's built from Stage 3's `TerrainEditor.quorum`.
  - Studio:
    - a `TerrainEditorTab`, a copy of `VisualEditorTab`;
    - `terrain.json` recognized in `IsSupportedFile` and `OpenNewTab`;
    - branches in `UndoBehavior`, `RedoBehavior`, `ClosedTab` and
      `SaveDirtyTabs`;
    - menu items (File, New Terrain).
  - This touches `SceneEditor` not at all. It's the smallest, safest step.
- **(b) Terrain in scenes.**
  - A scene can hold a terrain: a "Terrain" class in `DefaultSceneManager`
    (load and save, with a project-relative "File" pointing at the terrain
    folder).
  - The `SceneEditor` gets a terrain mode next to placing items.
  - Games then get their terrain with `LoadScene`, exactly as they get
    models today.
  - Larger, since it changes the scene format and the scene editor.

**Tool UI in Studio:**
- **Tool choice:** a list or radio group in a side panel. The palette
  `Tree` can hold tools as items, which is already keyboard and screen-reader
  friendly.
- **Brush settings:** text fields first, since the library has no slider.
  A `Slider` control is a separate, generally useful library addition (it
  needs its own accessibility: role, value, step keys). Proposed, not
  assumed.
- **Layers:** a list of the four layers' textures, chosen like palette
  items.
- **A live info line** (position, height, slope, layer), also spoken on
  request.

**Library and Studio changes Stage 4 needs** (each reviewed first, as with
`Item7.md`):

| # | Change | Where | Why |
|---|---|---|---|
| T1 | Move `Libraries.Game.Graphics.Worlds` (our classes and the brush engine) into the standard library | Library | Studio and user games need them. This is the "last" step of `LibraryChangesPlan.md`, moved earlier for Studio. |
| T2 | Terrain under OpenGL, or Studio's 3D editor on Vulkan | Library or Studio | Studio defaults to OpenGL; the terrain is Vulkan-only. Simplest: the terrain tab requires Vulkan and says so. Fuller: an OpenGL mesh path in `TerrainRegionModel`, and an OpenGL splat shader. |
| T3 | `TerrainEditor` control | Library | Stage 4 (a). |
| T4 | `TerrainEditorTab`, file type, undo/redo/save branches, menu | Studio | Stage 4 (a). |
| T5 | Studio's copy of the standard library brought up to date | Studio (build) | Its copy is from 2026-09-25. |
| T6 | `Slider` control | Library | Optional; better brush settings. |
| T7 | Terrain in scenes: a scene-manager class entry, and a scene-editor terrain mode | Library | Stage 4 (b). |

## Suggested order

1. **Stage 1 and Stage 2** together: the brush engine and undo, with tests.
   No window needed.
2. **Stage 3** in DriveOpen: picking, the preview ring, mouse and keyboard
   control, spoken values.
3. **Stage 3 world-making tools** and `TerrainEditor.quorum`. Make two or
   three larger test worlds (for example 16 by 16 regions of mountains with
   erosion), and use them for the paused renderer work: #22, mipmaps,
   texture updates, freeing.
4. **Stage 4 (a)** after T1, T2 and T5 are decided.
5. **Stage 4 (b)** when scenes with terrain are wanted.

## Mapping to the Tier 4 list in OpenWorlds.md

| Tier 4 item | Where in this plan |
|---|---|
| 1. Brush system (tool plus operation; size, strength, gamma, image, spin, slope filter) | Stage 1 |
| 2. Sculpt: raise, lower, smooth, flatten, ramp | Stage 1 (add, subtract, average, replace, gradient) |
| 3. Paint textures, colors, roughness, holes, navigation | Stage 1 for textures; colors, roughness and holes wait for Tier 2's data; navigation for Tier 5 |
| 4. Undo and redo with region copies, fitting Studio's undo model | Stage 2 (`TerrainStrokeEdit is Edit`) |
| 5. Mouse picking and a brush preview | Stage 3 (`TerrainData:RayCast`, ring preview) |
| (new) Keyboard brush cursor and spoken values | Stage 3 |
| (new) Generate, stamp, erode, auto paint, new world | Stage 3 |
| (new) Studio integration | Stage 4 |

## Open decisions for the user

1. **The order:** engine and undo, then the in-game editor and world
   tools, then Studio? Or Studio sooner?
2. **Studio's first step:** a terrain tab (a), or terrain inside scenes
   (b)?
3. **Renderer in Studio (T2):** require Vulkan for terrain editing, or plan
   an OpenGL path?
4. **When to move the `Worlds` classes into the library (T1):** before
   Stage 4, or keep everything in the project until the tools settle?
5. **World-making tools:** which matter most for the test worlds you have
   in mind? Noise presets, stamps, erosion, or importing real elevation
   data (for example a 16-bit height map)?
6. **Sonification** of height under the keyboard cursor: worth trying?
