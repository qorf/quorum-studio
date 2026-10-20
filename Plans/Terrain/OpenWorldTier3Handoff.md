# Open Worlds Tier 3: handoff

Tier 3 is open worlds: terrain far larger than what is near the player,
drawn and collided with at a cost that doesn't grow with the world's size.
Updated after every completed operation, so work can move between machines.
The overall picture is in `OpenWorlds.md` and `OpenWorldsHandoff.md`.

Last updated: 2026-10-18. Steps 1 and 2 and distant terrain are done and
running in DriveOpen. Step 3 is deferred, and the clipmap is not started.
Since then, work moved to texture splatting (a Tier 2 feature, in
`TextureSplattingPlan.md`) and the library changes it needed (orders 7 and
9 in `LibraryChangesPlan.md`). Next is Tier 4 (`TierFour.md`).

## Where we are

- **Step 1, collision only near the player: done.** No library changes.
  Checked by the user on 2026-10-17: all 13 `RunWorldTests` pass, and the
  car drives in DriveOpen with dynamic collision. Not yet reported: frame
  rate compared with full collision.
- **Step 2, streaming regions around the vehicle: done.** No library
  changes. Checked by the user on 2026-10-17: all tests pass, driving
  works with regions streaming, the vehicle no longer falls through, and
  E and Q mounds work with physics. Not explicitly reported: a mound
  surviving its region being unloaded and loaded again. Includes the user's request (2026-10-17) to
  save the settings as JSON (`terrain.json`), read with the standard
  library's `JavaScriptObjectNotation`.
- **Step 3, floating origin: deferred.** The region grid tops out at about
  8 km, where 32-bit floats are still precise to about 1 mm, so there is
  nothing to fix yet (explained to the user 2026-10-17).
- **Distant terrain with less detail: done.** No library changes. After
  the tint and taller hills (below), it has been running in every DriveOpen
  run since (for example "12 drawn in full, 52 drawn far", 2026-10-18). The
  checks under "To check distant terrain" were not each reported on
  separately.
- **Since 2026-10-18,** rebuilds of region and far models update their
  meshes in place (library order 7, `Item7.md`), and with splatting both
  draw with their own shader program (library order 9, `Item9.md`).

### Distant terrain: what was written

- **`TerrainRegionFarModel`** (new) `is TerrainPatch`.
  - **Coarse mesh:** a vertex every `step` samples (default 4), so a
    64-sample region is 17 by 17 vertices instead of 65 by 65. It reaches the
    next region's first sample (clamped where that isn't loaded), with
    normals from neighbors one step away. Same world-based texture
    coordinates as the full models.
  - **Skirts:** each of the four edges has a strip hanging `skirtDepth`
    (default 1) straight down, drawn facing both ways, since back faces
    are culled. They hide cracks where a far model meets a full model, or a
    neighbor built while its own neighbor wasn't loaded.
  - **No collision.** Placed lower by the skirt depth, since its vertices
    are measured up from the skirt bottom (`PlaceInWorld` override).
  - Has `SetNeedsRebuild` and `NeedsRebuild`.
- **`WorldTerrain`:**
  - `SetFarRadius` (default 0, which is off), `SetFarStep`,
    `SetSkirtDepth`, `GetFarModelCount`.
  - While streaming, after the full models are updated,
    `UpdateFarModels`:
    - takes far models out where a full model is now shown, or past the far
      radius plus half a region;
    - puts far models in for regions in reach that aren't drawn in full.
  - A far model is built once per region (`farModelMap`) and reused. If the
    region isn't loaded, `BuildFarModel` loads it just long enough to build,
    then unloads it.
  - Builds share the per-update limit with the full models; full models
    come first.
  - The full model replaces the far one only once it is ready, so there are
    no holes while it builds.
  - Edits mark affected far models to rebuild. One being shown is taken
    out, so the next update rebuilds it.
- **DriveOpen:** `farRadius = 200` (every corner of the map from the
  middle), `farStep = 4`. The streaming lines also report how many regions
  are drawn far.

### Distant terrain run 1

- The user saw nothing in the distance.
- Cause: the game's default camera is a `PerspectiveCamera`, which sets its
  far plane to **100** when created (`PerspectiveCamera.quorum:18`). The
  camera is 12 units behind the vehicle, so nothing beyond about 88 units
  ahead was drawn. That left only a thin band past the full-detail regions
  (shown out to 40 to 56 units) where far models could appear, often hidden
  by hills. DriveOpen's comment wrongly said the default was 1000; fixed.
- Fix: when distant terrain is on and `cameraFarDistance` is 0, DriveOpen
  sets the far plane to `farRadius + cameraBehind + 20` (232).
- **Side effect to watch:** the painter fits the sun's shadow cascades
  between the camera's near and far planes, so shadows near the vehicle may
  look softer or blockier. Setting `cameraFarDistance` overrides this.
- Whether the far models themselves were built isn't known yet. The
  startup line (`... drawn far ...`) will say.

### Distant terrain run 2 (console only)

- Startup: `32 regions loaded, 12 drawn in full, 52 drawn far`, as
  expected, so far models are built. Save check difference 2.4e-7.
- Driving toward an edge: regions load ahead and unload behind, full and
  far counts trade places, and near the map's edge the totals drop (corner
  regions pass beyond the far radius; full drops to 8 and far to 46).
  Collision pieces fall from 113 to 28 as the vehicle nears the edge.
- No errors. Not yet reported: whether the distance is now visible, cracks
  between full and far regions, shadows, and frame rate.

### Distant terrain: tint and taller hills

- The user saw only roughly flat ground in the distance. That is what far
  models should look like with DriveOpen's old hills (`heightScale` 24, so
  only a few units of rise, seen from a camera 5 units up), so it couldn't
  be told apart from before.
- Added `WorldTerrain:SetFarTint(Color)` / `GetFarTint`, which use the
  per-model color override, `GetResources():SetColor`. It multiplies the
  texture. Applied after each far build, and to already-built far models
  when set. Also `GetSkirtDepth`.
- DriveOpen:
  - `tintFarTerrain = true`, a reddish tint (1, 0.55, 0.55).
  - `heightScale` raised from 24 to 120.
  - `farSkirtDepth = 6`, since steeper hills leave larger cracks.
  - The render tree check's region top raised from 40 to 80.
- **Ran (2026-10-17):** the hills showed, and led to the changes below.
  Expected: red-tinted hills out to the map's edge,
  turning to untinted full detail about 40 units from the vehicle.

### Hills the vehicle can climb

- With `heightScale` 120 the distant hills were visible (the tint
  works), but the vehicle couldn't climb the slopes.
- Steepness goes with height times noise frequency: originally 24 × 0.0125
  = 0.3, then 120 × 0.0125 = 1.5.
- Now `heightScale` = 80 and a new `hillFrequency` = 0.005 (a hill about
  every 100 units instead of 40): 80 × 0.005 = 0.4. That is tall, wide hills
  that are a little steeper than the original. **Ran:** the vehicle has
  driven and edited these hills in the runs since.

### Vehicle tuned for the taller hills (DriveOpen only; Main unchanged)

- The user asked for the vehicle to be less likely to hit the slopes.
- Cause: the chassis's physics box (1.55 tall, 5.84 long, centered on the
  chassis) cleared the ground by only about 0.2, and reached 0.86 past the
  front wheels. So its front corner hit any slope over about 14 degrees
  before the wheels did.
- Fix:
  - The box is now 1.2 by 5.2 (width unchanged): about 0.39 clearance, so
    about 36 degrees to drive onto a slope and about 21 to drive off one.
  - The suspension was not raised, because the drawn wheels are part of the
    body model and would float.
  - Engine force went from 3000 to 5000 (reverse from 2000 to 3000).
    Gravity on the 800 kg vehicle at 30 degrees is about 3900.
- **Ran:** used in every DriveOpen run since.

### To check distant terrain

1. `RunWorldTests`: still 15 of 15 (no new tests; far models need a running
   game to build their meshes).
2. `DriveOpen` startup line: `... 12 drawn in full, 52 drawn far, ...`. The
   whole map should now be visible from the start, not just 40 units
   around the vehicle.
3. Look for:
   - cracks or gaps where full and far regions meet (the skirts should
     hide them);
   - a visible "pop" as a region switches between far and full detail
     about 40 units away (expected: the shape changes slightly);
   - the frame rate, compared with `farRadius = 0`;
   - startup time (every far region is loaded briefly once to build its
     model).
4. Edit with E near the edge of the draw radius, drive away until that
   region is drawn far, and check the far model shows the edit.



### Step 2: what was written

**Saving format, version 2 (replaces Tier 1's `terrain.txt`):**
- `terrain.json` in the folder, written by `TerrainData:SaveSettings` and read
  by `LoadSettings` with `JavaScriptObjectNotation`:

  ```
  {"format": "TerrainData", "version": 2, "regionSize": 64, "vertexSpacing": 0.5,
   "regions": [{"x": -1, "z": 2, "lowest": -3.25, "highest": 7.5}, ...]}
  ```

- One `region_x_z.png` per region, as before. Its height range now lives in
  `terrain.json`; there are no per-region text files.
- Reading `terrain.json` uses `File:Read`, so `FileReader:ReadLines` (bug A
  in `VulkanChanges.md`) is no longer involved.
- The library's `GetNumber(key)` only accepts values written with a decimal
  point, and `GetInteger(key)` only whole ones. `TerrainData`'s private
  `GetJsonNumber` accepts either.
- Version 1 folders (`terrain.txt`) can't be read any more. DriveOpen writes
  a fresh save each run.

**`TerrainData`, new or changed:**
- `LoadSettings(folder)` reads `terrain.json` without loading regions.
- `SaveSettings(folder)` writes it.
- `SaveRegion(folder, region)` writes the image and updates `terrain.json`.
- `LoadRegion(folder, x, z)` loads one region.
- `UnloadRegion(x, z)` removes one region without saving and without
  touching the edited area.
- `HasSavedRegion`, `HasRegionAnywhere`, `GetSavedLowestHeight`,
  `GetSavedHighestHeight`.
- `Save` and `Load` are now built from these. Saving to a new folder starts
  a new list of saved regions. `RemoveRegion` = `UnloadRegion` plus the
  edited area.

**`TerrainStreamer` (new; Terrain3D doesn't stream yet):**
- Load radius, and an unload radius half a region larger, so regions at the
  edge don't flicker.
- `Update(x, z)`: unloads up to 2 far regions (saving modified ones first),
  then loads up to `loadsPerUpdate` (1) nearest saved regions in reach.
- `LoadAllNear` for the start, `SaveChanges`, `UnloadAll`.
- `GetDistanceToRegion` measures to the nearest point of a region's square.
- `GetLast...Count` for reporting.

**`WorldTerrain`:**
- `StartStreaming(folder, layer, x, z)` in place of `Build`:
  - unloads (saving changes), then reads `terrain.json`;
  - load radius = draw radius plus 1.5 regions, so every drawn region's
    neighbors are loaded;
  - loads everything near the target, and puts models for drawable regions
    in the layer.
- `Update` then streams, updates drawn models, and updates collision.
- A region is drawn only within `drawRadius` (default 100) and when
  `IsDrawable`: each of its 8 neighbors is loaded or not saved at all. Its
  mesh and normals read into them.
- Models are kept by location (`modelMap`) and reused when a region comes
  back. So GPU memory grows with the number of distinct regions drawn, not
  with how often they come back.
- At most `buildsPerUpdate` (1) models are built or rebuilt per update.
- Edits mark models that aren't in the layer as needing a rebuild
  (`TerrainRegionModel:SetNeedsRebuild`), done when they come back.
- Models are taken out of the layer half a region past the draw radius.
- `TerrainRegionModel` gained `SetTerrainRegion`, `SetNeedsRebuild`, and
  `NeedsRebuild`.

**DriveOpen:**
- `streamTerrain = true`, `drawRadius = 40`.
- At startup the generated world is saved to `SavedTerrain` (by
  `CheckSaving`, or by `Save` if that is off), then streamed back from
  there, starting at (0, 0).
- `ReportTerrainChanges` prints a line when regions load or unload, and
  when the number of collision pieces changes.
- The render tree check isn't told about streamed region models.
- `streamTerrain = false` builds everything as before.

**Tests:**
- `TerrainDataSettingsTest`: a hand-written `terrain.json` with whole,
  negative and exponent numbers; `LoadSettings`, then `SaveSettings`, then
  reading it again, plus reading it with `JavaScriptObjectNotation`
  directly. Files go under `Build/WorldTests` (ignored by git).
- `TerrainStreamingTest`: unloading, distances and radii, and `IsDrawable`.

### Test run 1 for step 2

- `Worlds Tests: 13 of 15 passed.` Both new tests failed in `LoadSettings`
  with "The saved terrain has no number named x." (The new error output in
  `RunWorldTests` showed exactly where.)
- Cause: how `JavaScriptObjectNotation` reads `"regions": [ {...} ]`
  (`JavaScriptObjectNotationDefaultListener`, `EnterArray`).
  `GetObject("regions")` returns a holder marked as an array object. The
  array is the holder's one child, returned by `GetArray()`, and the region
  objects are the array's children. The code treated the holder as the
  array.
- Fix: `LoadSettings` unwraps it with `GetArray()` when `IsArrayObject()`.
- **Waiting for the next run.**

### Test run 2 for step 2

- All tests pass, and driving works, but driving straight ahead the car
  eventually falls through the ground.
- Cause: a step 1 bug, exposed by driving further. `Layer3D:Remove` turns
  an item's collision off (`SetCollidable(false)`), and `Layer3D:Add`
  doesn't turn it back on (a library bug, `VulkanChanges.md` "Other library
  bugs" C). `TerrainCollision` reuses pieces by removing them and adding
  them again, so every reused piece stopped colliding. Holes appeared once
  the vehicle had crossed enough squares for pieces to be reused. Pieces
  re-placed after E and Q edits had the same problem.
- Fix: `TerrainCollision:AddToLayer` calls `SetCollidable(true)` before
  `Add`. `WorldTerrain` sets each returning region model's collidable to
  match whether it has a collision shape.
- Run 3: physics fixed; mounds and growing them work with physics.

### To check step 2

1. `RunWorldTests`: expected `Worlds Tests: 15 of 15 passed.`
2. `DriveOpen`. Startup should show the import and save checks, then:
   - `Streaming: N regions loaded, M drawn, from .../SavedTerrain`. At the
     map's center, with a draw radius of 40 and 32-unit regions, 12
     are drawn and 32 loaded.
   - `SavedTerrain` should now hold `terrain.json` and the region PNGs.
     Old `terrain.txt` and `.txt` files from earlier runs can be deleted.
3. Drive toward an edge of the map. Check:
   - regions ahead appear, about 40 units out, one per frame;
   - regions behind disappear;
   - `Streaming: loaded ..., unloaded ...` lines appear;
   - the frame rate doesn't hitch badly when a region loads (one 64 by 64
     PNG and one mesh build at most).
4. **Persistence:** raise a mound with E, drive more than about 120 units
   away until that region unloads (the line says `saved 1`), then drive
   back. The mound should still be there.
5. `streamTerrain = false` should behave as before step 2.

### Step 1 record


### Step 1: what was written

| File | What it is |
|---|---|
| `SourceCode/Worlds/TerrainPatch.quorum` | New. `is Terrain`. The shared part of drawn regions and collision pieces: a rectangle of samples read live from `TerrainData` (`Measure`), `ApplyCollisionShape`, `PlaceInWorld`, `FillAtPixel`, `GetPatchHeight`, `IsAffectedBy`. Moved out of `TerrainRegionModel`. |
| `SourceCode/Worlds/TerrainRegionModel.quorum` | Now `is TerrainPatch`. Keeps the mesh building. `Build` takes a fifth parameter, `withCollision`; without it the model only draws (`SetCollidable(false)`). |
| `SourceCode/Worlds/TerrainCollisionPiece.quorum` | New. `is TerrainPatch`, hidden, no mesh. `Place(data, firstX, firstZ, size)`, `Covers`. Like Terrain3D's dynamic collision shapes. |
| `SourceCode/Worlds/TerrainCollision.quorum` | New. Terrain3D's dynamic mode. Shape size 16 and radius 64 (in samples) by default, as in Terrain3D. `Update(layer, x, z)` only works when the target moves into a new square of the grid. It chooses squares in a region whose centers are within the radius, removes pieces no longer wanted, and reuses them (from a free list) for new squares. `Rebuild(area)` re-places pieces after edits; `Clear`; `Refresh`. `ChooseCells` and the `GetChosen...` actions are public so they can be tested. |
| `SourceCode/Worlds/WorldTerrain.quorum` | Collision modes `COLLISION_DISABLED`, `COLLISION_DYNAMIC` and `COLLISION_FULL` (the default here, so older code behaves the same; Terrain3D's default is dynamic). `SetCollisionMode`, `GetCollision`, `Update(layer, x, z)`. `Build` gives region models collision only in full mode. `Rebuild` also re-places nearby collision pieces in dynamic mode. |
| `SourceCode/Tests/Worlds/TerrainCollisionCellsTest.quorum`, `TerrainCollisionPieceTest.quorum` | New tests: square choice (47 at the origin with 4 regions, 31 near a corner, 0 far away, 5 with radius 16), piece sizes at edges, placement, triangles, `IsAffectedBy`. |
| `SourceCode/DriveOpen.quorum` | `dynamicCollision = true`, `collisionRadius = 96` (48 units, so the box stacks start on ground), `collisionShapeSize = 16`. Calls `terrain:Update` at startup at (0, 0) and every frame at the vehicle. Rain falls within `rainRadius` (20 units) of the vehicle while collision is dynamic. Set `dynamicCollision = false` to compare with full collision. |

Findings that made this possible without library changes:
- `Model:Draw` returns early for a hidden model, and drawing into shadows
  goes through `Draw` too. Physics never checks visibility. So a hidden
  `Terrain` with no mesh collides but is never drawn.
- `Item3D:SetCollidable(false)` keeps an item out of the broadphase, so
  drawn-only region models cost nothing in physics.
- `Layer3D` adds and removes items with physics cleanly (about log n), so
  pieces are taken out of the layer while they are moved, and put back.

### Test run 1 for step 1

- `Worlds Tests: 12 of 13 passed.` "Terrain collision squares" failed
  with no message, which means an error.
- Cause: `Integer32BitArray:GetSize` throws a null pointer error until
  `SetSize` has been called (a library bug, `VulkanChanges.md`, "Other
  library bugs", B). `ChooseCells` asked its new arrays their size.
- Fix: `TerrainCollision` keeps its own count (`wantedRoom`). Also guarded
  `TerrainRegion:CalculateHeightRange`.
- `RunWorldTests` now runs again any test that failed with no message,
  catches the error, and prints it with its stack trace, since the `Tester`
  hides errors.
- Run 2: all 13 tests pass, and the car drives.

### To check step 1

1. `RunWorldTests`: expected `Worlds Tests: 13 of 13 passed.`
2. `DriveOpen`. The console should show the lines from before, then
   `Collision: dynamic, N pieces near the start.` With a radius of 96 and a
   shape size of 16, N should be about 113 (all squares in reach are inside
   the map).
3. While driving, `Collision pieces near the vehicle: N` appears each time
   the vehicle crosses into a new 8-unit square. N shrinks near the map's
   edge.
4. Check:
   - the vehicle drives normally everywhere, including across region
     edges and across collision-square edges;
   - rain lands around the vehicle;
   - box stacks far behind the vehicle stay put while asleep, but fall
     through if woken once the vehicle is far away (expected; no ground
     there);
   - E and Q still work, and the vehicle follows the changed ground (the
     pieces there are re-placed);
   - frame rate compared with `dynamicCollision = false`.
5. Expected but not a bug: balls that roll more than about 48 units from
   the vehicle fall through and are removed.

## Earlier notes

## Rules for this work

Same as Tiers 1 and 2:
- **Do not change the standard library** unless the user approves that
  specific change.
- **All code is written in AllTerrainVehicle first,** in package
  `Libraries.Game.Graphics.Worlds`.
- **Use Terrain3D as the guide** (`/Users/stefik/Repositories/Terrain3D`).
- **Test in `DriveOpen.quorum`;** headless tests in `Tests/Worlds`.
- **Claude never runs the game or `Default.jar`.**

## Goal

From `OpenWorlds.md` Tier 3:
1. A world of sparse regions, each loaded and saved on its own.
2. A camera-following GPU mesh (clipmap) in place of one mesh per region.
3. Collision only near the camera.
4. Background terrain (flat or noise) beyond the edited regions.
5. Optional 64-bit world coordinates.

Item 1 is partly done in Tier 1: regions are sparse, and each saves to its
own file. Loading and unloading regions while the game runs is not done.

## How Terrain3D does it

### Clipmap mesh (`src/terrain_3d_mesher.cpp`, `doc/docs/system_architecture.md`)

- **The mesh is made once.** It's a set of flat grid pieces, which the
  vertex shader raises to the right heights by reading the height maps.
  Nothing is rebuilt when the camera moves or the ground is edited; only
  the height textures change.
- **Pieces** (`_generate_mesh_types`, lines 15-40), with `mesh_size` = 48 by
  default:
  - TILE: mesh_size by mesh_size;
  - EDGE_A and EDGE_B: 2-wide strips that bridge to the next level of
    detail;
  - FILL_A and FILL_B: 4-wide strips for levels 1 and up;
  - TRIM_A and TRIM_B: 2-wide strips for level 0;
  - "standard" versions of these for level 0.
- **Levels of detail** (`_generate_clipmap`, lines 114-184): 7 by default
  (`_mesh_lods`).
  - Level 0 has 16 tiles; every other level has 12, in a ring around the
    level inside it.
  - Each level has 2 of each edge piece, and 2 fills or trims.
  - The pieces are instances of the 10 shared meshes. Each level is scaled
    by 2^level times the vertex spacing, so each ring has half the detail of
    the one inside it and twice the size.
  - With the defaults that's about 7 × 20 = 140 instances, covering roughly
    48 × 4 × 2^7 ≈ 24,000 units across.
- **Following the camera** (`snap`, lines 319-406):
  - Nothing moves until the camera has moved at least one vertex spacing.
  - Then each level snaps to a grid of 2^(level+1) × spacing. That keeps
    vertices on the same height samples as they move, so the ground doesn't
    "swim".
  - The edge strips pick one of three positions, so they always sit
    between a level and the next coarser one.
- **Bounds:** every instance shares its mesh's box. The box is stretched to
  the terrain's whole height range (`update_aabbs`), so culling works without
  knowing the heights under each piece.
- **Seams between levels:** Terrain3D's shader blends vertex heights near the
  outer edge of each level toward the next level's. That hides cracks and
  popping.

### Collision near the camera (`src/terrain_3d_collision.cpp`, `doc/docs/collision.md`)

- Modes: Dynamic/Game (the default), Dynamic/Editor, Full/Game,
  Full/Editor, Disabled.
- **Dynamic** (`update`, lines 233+):
  - Collision is made only within `collision_radius` (64 by default) of a
    target, usually the camera or player.
  - It's built from heightfield shapes `collision_shape_size` (16 by
    default) samples across, on a grid snapped to that size.
  - When the target crosses a grid line, shapes outside the radius are
    disabled and reused for new cells inside it. Shapes are pooled, not
    made and destroyed.
- **Full** builds everything once at the start, at a high memory cost on big
  worlds.
- There's no collision outside regions, in the background noise.
- Terrain3D recommends `get_height()` (a lookup in the data) over a physics
  ray when x and z are known. Our `GetSurfaceHeight` already does this.

### Regions and loading (`src/terrain_3d_data.cpp`, `doc/docs/performance.md`)

- Regions: 64 to 2048 samples across, up to 32 by 32 of them, one file each
  (`save_directory`, `load_directory`, `load_region`).
- **Terrain3D does not stream regions yet.** Its performance page says
  "Region streaming is being worked on for v1.2". All regions are loaded at
  the start. For streaming, we would go beyond our guide.

### Background beyond regions (`src/shaders/backgrounds.glsl`)

- Modes: NONE (no ground), FLAT (`ground_level`, with `region_blend` easing
  regions into it), NOISE (fractal noise drawn by the shader:
  `world_noise_scale`, `world_noise_height`, octaves that drop with
  distance, `world_noise_lod_distance`).
- Only the clipmap can draw it, since it's made in the vertex shader on the
  same mesh. There's no collision on the background.

### Large coordinates (`doc/docs/double_precision.md`)

- Single-precision floats start to break down at about 10-40 km from the
  origin: movement jitters and rendering is corrupted.
- Terrain3D's answer is to build Godot and Terrain3D with 64-bit floats.
  Shaders still use 32-bit floats; it points to emulating double precision
  by drawing relative to the camera.
- The largest world is 65.5 km across (more with vertex spacing).

## Quorum side (investigation, 2026-10-17)

G = `quorum-language/Quorum/Library/Standard/Libraries/`,
J = `quorum-language/Plugins/QuorumPlugins/src/main/java/plugins/quorum/Libraries/`.
Line numbers were right on 2026-10-17.

### What can already be done from this project

- **Upload any data to a GPU buffer:**
  - Make it with `VulkanBuffer:Create` (storage and device-address usage,
    host-writable), or `CreateMappableBuffer`.
  - Write it with `MapToNumber32BitMemory` and
    `VulkanNumber32BitMappedMemory:Set`
    (G/Game/Graphics/Vulkan/VulkanBuffer.quorum:27-126).

  All public. The device comes from `VulkanGraphics:GetDevice()`.
- **Run a compute shader:**
  - Subclass `Shader` like `AnimationComputeShaderVulkan`.
  - Copy how `VulkanPainter3D:GetAnimationPipeline` (lines 1571-1609)
    builds its pipeline. It uses no descriptor sets, only push constants
    holding buffer addresses.

  All public.
- **Add and remove items cheaply:**
  - The render tree (`DynamicVolumeRenderTree3D` by default) and the physics
    broadphase (a dynamic box tree) both insert and remove in about log n
    time.
  - Removing a physics item is clean: its contacts and nodes are destroyed
    (G/Game/Layer3D.quorum:585-601).
  - Both must happen on the main thread.
- **Static ground costs nothing per physics step,** apart from pairs near
  moving bodies. Each step only goes through awake bodies (Layer3D about
  3011-3397, CollisionManager3D.quorum:50-70).

### What can't be done without library changes

- **A model can't draw from a buffer we supply.**
  - The per-model compute-written buffers (`ModelResourcesVulkan`, lines
    98-121) are only used for animated models.
  - Batching and the compute pass are private to the painter.
- **No custom 3D shader** (Tier 2's L1), and **no free per-instance slot**
  for a pointer to height data (the 104-byte instance record is full).
- **Nothing is ever freed:**
  - meshes, blueprints, the blueprint cache;
  - painter batches (they have an `age` but are never pruned,
    G/Game/Graphics/Vulkan/VulkanPainter3DBatch.quorum:20, 64).

  So unloading regions doesn't give memory back.
- **Draw queues are fixed at 8192 instances per texture group**
  (VulkanPainter3D.quorum:108), with no growth.
- **Positions go to the GPU as 32-bit floats, measured from the world
  origin:**
  - the model matrix is cast to float in Java
    (J/Game/Graphics/Vulkan/VulkanDrawQueue3DInstanceMappedMemory.java:72+);
  - the camera matrix goes through `MatrixToArray` (VulkanPainter3D
    1720-1738).

  There's no camera-relative drawing, so there will be jitter far from the
  origin. On the CPU, positions, `Vector3`, `Matrix4` and physics are all
  64-bit `number`.
- **No fog or distance fade** in any shader, and **no skybox** in Vulkan
  (`SetSkybox` is empty, VulkanPainter3D 1689-1692).
- **No threads or background loading.** `PixelMap.LoadAsynchronously` is
  synchronous on desktop (PixelMap.java:112-117). Loading has to be spread
  across frames on the main thread.

### Costs to watch

- **`Layer3D:Update` calls `UpdateAll` on every item every frame**
  (Layer3D.quorum:867-876). A world of thousands of region models pays for
  every one each frame, even far away. A clipmap (about 140 instances) or
  fewer, larger pieces avoid this.
- The painter makes one batch per blueprint, so one draw command per region
  mesh. A clipmap would share a few blueprints across many instances.
- **Camera far plane:** `Camera` defaults to 1000, but `PerspectiveCamera`
  sets 100 (PerspectiveCamera.quorum:18). DriveOpen uses the default
  camera. When a directional light's culling range is 0, its shadow
  cascades stretch to the far plane, so a far plane of kilometers thins
  shadows. DriveOpen already sets `sunCullingRange`.

### Library bugs found

- **Compute to draw race:** the animation compute shader writes vertex
  buffers on the compute queue with only a fence. Nothing makes the
  graphics pass wait for those writes; the only barrier is transfer to
  vertex input (VulkanPainter3D 1069-1077). In practice the compute queue
  is usually the same family as graphics. Recorded as `VulkanChanges.md`
  #14.

## Recommended approach

### 1. Collision only near the player: in this project, no library change

- Follow Terrain3D's dynamic mode. A `TerrainCollision` keeps a pool of
  small collision pieces (Terrain3D: 16 samples across, radius 64) on a grid
  snapped around a target (the vehicle).
- When the target crosses a grid line, pieces out of range are moved to
  new cells in range: `SetPosition`, plus `FillAtPixel` reading from the new
  cell. They're not removed and re-added.
- Drawing and collision then split. Region models stop having physics, and
  the far-away ground costs nothing in physics.
- **To check first:** a collision piece is a `Terrain` (a `Model`) with no
  mesh. It must collide, but not draw or break the painter. If a mesh-less
  model is a problem, give it an empty or tiny blueprint, or keep it out of
  the drawn layer.

### 2. Streaming regions: data now, drawing after a library change

- **Data, in this project:**
  - `TerrainData` loads and unloads single regions (`LoadRegion`,
    `UnloadRegion`), with one file per region already in place from Tier 1.
  - A `TerrainStreamer` keeps regions within a radius of the player loaded,
    and works through a few per frame. Quorum has no background threads.
  - Saving a modified region before unloading it, like Terrain3D's
    modified flag.
- **Drawing:** region models can be added and removed with the regions.
  Without freeing, every unloaded region's mesh stays in GPU memory, and
  its batch stays in the painter. **Needs S1 below.**
- Until S1, a fixed pool of region models could be reused. That needs a
  mesh update in place (`VulkanChanges.md` #3), which is also a library
  change.

### 3. Clipmap mesh: needs library changes, two routes

Both routes keep a fixed set of ring pieces around the camera, following
Terrain3D's layout (TILE, EDGE, FILL and TRIM pieces, 7 levels, snapping at
2^(level+1) × spacing).

- **Vertex route (Terrain3D's way, recommended):**
  - One flat grid blueprint per piece shape, drawn as many instances.
  - A terrain vertex shader reads heights from one GPU buffer of loaded
    regions (heights and a region table), through a buffer address.
  - Editing writes the changed heights into the mapped buffer in place.
    **No mesh rebuilds and no leaks when editing.**
  - Needs:
    - per-model shader programs (S3; the same change as Tier 2's L1);
    - a way to hand the shader the buffer's address (S4);
    - a matching shadow vertex shader.
  - About 150-300 lines in the library.
  - Because S3 is shared with Tier 2, doing Tier 2 first makes this route
    cheaper.
- **Compute route (smaller library hook):**
  - A compute shader writes displaced vertices for each piece into
    per-model buffers, like animated models. The default pipeline draws
    them, and shadows keep working.
  - Needs S2: a vertex-buffer override on `ModelResourcesVulkan`, a public
    "before drawing" compute hook recorded into the frame's command buffer,
    and a compute to vertex-shader barrier. About 30-60 lines.
  - Costs a compute pass each frame (or each snap), and doesn't help Tier 2.
- **Edge seams:** like Terrain3D, blend each level's outer heights toward the
  next level's, in the vertex or compute shader.
- **Normals:** the CPU no longer builds them. Work them out in the shader
  from neighboring heights, as Terrain3D does in its fragment shader.
- **Collision is unchanged,** from step 1, since it reads `TerrainData` on
  the CPU.
- **Before the GPU version, a CPU stand-in could prove the layout:**
  rebuild the ring pieces' meshes on the CPU when the camera snaps. Since
  `VulkanChanges.md` #3 (library order 7), a piece whose vertex count stays
  the same can be updated in place, so this no longer leaks.

### 4. Background terrain

- **In this project:** a few large, coarse, static models around the
  regions (or the clipmap's outer levels, once there is one), drawing noise
  from `NoiseGenerator`. No collision, as in Terrain3D. Set the camera's far
  plane to suit.
- **Library, small:** distance fog in the lighting shader (S6, about 20-40
  lines) to fade the far edge. A skybox for Vulkan (S7, medium) for what's
  beyond.

### 5. Large coordinates

- **In this project now:** a floating origin. When the player is more than
  a few kilometers from the origin, move every top-level item, the camera
  and the lights back by the same offset, and keep a 64-bit world offset
  for game logic and `TerrainData` lookups. Each shift is one pass over the
  items (each reports its move once).
- **Library, about 40 lines (S5):** camera-relative drawing. Subtract the
  camera position from each model's translation in 64-bit before the cast
  to float, and send a view matrix without translation. This removes most
  jitter without shifting the world.
- Terrain3D's region grid is 32 by 32 regions. At our default 256 samples
  and 1 unit spacing, that's 8 km across, which is short of where floats
  break down. The limit only matters if the grid is made larger.

## Library changes for Tier 3 (each needs the user's approval)

| # | Change | Size | Needed for | Also in |
|---|---|---|---|---|
| S1 | **Freeing GPU memory:** the Java `StopMapping` fix, a deferred-deletion queue, `MeshVulkan`/`ModelBlueprintVulkan`/`ModelResourcesVulkan` dispose, remove from the blueprint cache, prune painter batches by age | ~200-250 lines, ~6 files, plus Java | Streaming drawn regions | `VulkanChanges.md` #1, #2, #5, #15 |
| S2 | Vertex-buffer override per model, "before drawing" compute hook, compute to vertex barrier | ~30-60 lines | Clipmap, compute route | new |
| S3 | Per-model shader programs in the Vulkan painter. **Done 2026-10-18** (library order 9, `Item9.md`) | about 230 lines with comments | Clipmap, vertex route; Tier 2 | Tier 2 L1, `VulkanChanges.md` #6 |
| S4 | Pass a user buffer address to a shader: a field in the material, or a larger instance record | ~30-80 lines, Java and 3 shaders if the instance record grows | Clipmap, vertex route | new |
| S5 | Camera-relative drawing | ~40 lines | Large worlds without jitter | `VulkanChanges.md` #16 |
| S6 | Distance fog in the lighting shader | ~20-40 lines | Hiding the far edge | `VulkanChanges.md` #17 |
| S7 | Skybox for Vulkan | medium | Sky beyond the terrain | `VulkanChanges.md` #11 |
| S8 | Draw queues that grow past 8192 instances | small | Many objects (foliage later) | `VulkanChanges.md` #18 |
| S9 | Fix the compute to draw race for animation | a few lines | Animated models | `VulkanChanges.md` #14 |

## Suggested order

1. **Collision near the player** (no library change). It also tests
   collision pieces as plain `Terrain` items.
2. **Region streaming of data** (no library change): load and unload
   regions around the player, a few per frame, saving modified ones.
3. **Floating origin experiment** in DriveOpen (no library change), with a
   larger region grid or a test at large coordinates.
4. **Library: S1 (freeing).** Then streaming of drawn region models.
5. **Choose the clipmap route.** S3 now exists (library order 9), so the
   vertex route needs only S4. Otherwise the compute route
   (S2) is the smaller step.
6. **Background terrain, S6 fog, S5 camera-relative drawing,** as needed.

## Open decisions for the user

1. ~~**Order of Tier 2 and Tier 3.**~~ Settled by events: splatting (Tier 2)
   came first and brought S3, so the vertex-route clipmap now needs only S4.
2. **Clipmap route:** vertex (Terrain3D's way, editing without rebuilds) or
   compute (smaller hook)?
3. **Which library changes to approve,** and when: S1 is needed before
   drawn regions can stream.
4. **World size target.** Up to about 8 km (the current 32 by 32 grid at
   256 samples) needs no precision work. Larger needs S5 or a floating
   origin, and a larger region grid.
