# Open Worlds Tier 1: handoff

Updated after every completed operation, so work can move between
machines. The overall picture is in `OpenWorlds.md` and
`OpenWorldsHandoff.md`.

Last updated: 2026-10-18. **This tier is finished; this file is now a
record.** Current work is in `OpenWorldsHandoff.md`.

## Tier 1 is complete (2026-10-17)

**Note (Tier 3 step 2):** the save format described below has changed.
`terrain.txt` was replaced by `terrain.json` (version 2), which also holds
each region's height range. See `OpenWorldTier3Handoff.md`.

Checked by the user and committed (`d9ef406`, "Next"):
- all `RunWorldTests` pass;
- DriveOpen drives across 64 regions with the grass texture;
- E and Q editing works;
- the save check passes.

Every Tier 1 goal is met: texture coordinates and spacing, queries,
runtime editing, saving and loading, and building from noise
(`GenerateHeights`, tested in `RunWorldTests`; DriveOpen uses an imported
height map instead).

Left over:
- **The mesh leak when editing: fixed 2026-10-18** by Option A below, done
  as library order 7 (`Item7.md`). Region and far models now update their
  meshes in place. A new mesh is still made when a region's grid changes
  size, and those still can't be freed (Option B, library order 11).
- **Moving the classes into the standard library**
  (`Libraries.Game.Graphics.Worlds`), when the user decides.

The rest of this file is the record of how Tier 1 went.

## Rules for this work

- **Do not change the standard library** (`quorum-language/Quorum/Library/Standard`)
  at all unless the user asks for a specific change. Reading it is fine.
- **All code is written in AllTerrainVehicle first**
  (`physics/AllTerrainVehicle/SourceCode/`). It moves into the standard
  library later, piece by piece, when the user decides.
- **Use the package `Libraries.Game.Graphics.Worlds`**, where the classes
  will live in the library, so they move without changes.
- **Use Terrain3D as the guide** (`/Users/stefik/Repositories/Terrain3D`).
  It doesn't have to be an exact port, but each class starts from its
  Terrain3D counterpart.
- **Test in `DriveOpen.quorum`**, a copy of `Main.quorum`. Leave `Main`
  alone.
- **Claude never runs the game or `Default.jar`.** The user compiles and
  runs, and pastes the output. (From `physics/Handoff/Handoff.md`, which
  also lists Quorum gotchas and the comment style to follow.)

## Where we are

- **Step 1 written and compiled.** First run: 6 of 7 passed. The image
  import test failed with no message (see "Test run 1" below). It was
  replaced by a pixel test. The second run hasn't been reported.
- **Steps 3 and 4 done and checked by the user (2026-10-17):** all
  `RunWorldTests` tests pass, DriveOpen's car drives as expected across the
  64 regions, and the grass texture works.
- **Steps 2, 5 and 6 written, not yet compiled.** Waiting for the user to
  run `RunWorldTests` (expected `Worlds Tests: 11 of 11 passed.`) and then
  `DriveOpen` (see "To check steps 2, 5 and 6" below).
- With these, every Tier 1 step has been written. Editing still uses the
  leaky rebuild, since no library change has been approved
  (`VulkanChanges.md` #3).
- `SourceCode/DriveOpen.quorum` created: an exact copy of `Main.quorum`
  (including its uncommitted one-line change), except the class is named
  `DriveOpen`. It doesn't use the new classes yet.
- Mesh update and free investigation done (below). It needs a decision.

### To check step 1

1. Set the project's main file to `/SourceCode/RunWorldTests.quorum`.
   In Quorum Studio this is the project's Main setting (`Project/Project.qp`).
2. Compile and run. Paste the output.
3. Expected: `Worlds Tests: 7 of 7 passed.` (6 data tests plus the pixel test).
4. Then set the main file back to `Main.quorum` or `DriveOpen.quorum`.

### To check steps 2, 5 and 6

1. Run `RunWorldTests`. Expected: `Worlds Tests: 11 of 11 passed.` New
   tests: surface height, normals and slopes, raising an area, and region
   height pixels.
2. Run `DriveOpen`. The console should show, after the two lines from
   before:
   - `Save check: 64 of 64 regions loaded from .../SavedTerrain, 0 missing.
     262144 samples compared, largest difference ...`. The difference should
     be tiny, under 0.00001.
   - The folder `SavedTerrain` (64 PNGs and `terrain.txt`) is created in
     the game's working folder, which is probably `AllTerrainVehicle`. It
     can be deleted, or ignored in git.
3. Drive, and hold **E** to raise a mound 7 units ahead, or **Q** to dig a
   hollow. Each change prints where it was, how many regions were rebuilt
   (1 to 4), and the slope there. Check:
   - the ground changes where expected, with no cracks at region edges;
   - the vehicle drives over the new mound and into the hollow (collision
     follows the change);
   - balls and boxes resting there react, or at least don't fall through
     (sleeping bodies may not wake up);
   - a short flicker of the rebuilt region for a frame is possible, while
     its new mesh uploads.
4. Box stacks and the vehicle's start height now come from
   `GetSurfaceHeight`, so they should sit right on the ground, as before.

### DriveOpen run 1 for steps 2, 5 and 6

- Import check and region count as before (104 of 104, 64 regions).
- The save check crashed in `TerrainData:Load` with `EndOfFileError` from
  `FileReader:ReadLines`. That is a library bug: `ReadLines` always fails on
  a non-empty file. See `VulkanChanges.md`, "Other library bugs", A.
- Fix: `Load` now reads one line at a time with `ReadLine` until
  `IsAtEndOfFile`. **Waiting for the next run.**
- Saving got as far as writing the files before the crash, so `SavedTerrain`
  should already exist.

### To check DriveOpen (steps 3 and 4, passed)

1. Set the project's main file to `/SourceCode/DriveOpen.quorum`.
2. Compile and run. The console should show:
   - `Import check: 104 of 104 samples match.` This checks
     `ImportHeightMap`'s pixel order and placement. It is the check that
     couldn't run in `RunWorldTests`.
   - `Terrain: 64 regions, 64 region models.`
3. Look for:
   - grass on the ground, lining up across region edges;
   - no cracks or seams between regions, in the mesh or the lighting;
   - the vehicle driving across region edges without bumps or falling
     through;
   - balls landing and rolling on every region, and the box stacks standing
     on their pads;
   - the edge of the map at plus or minus 128.
4. Expected but not a bug: the texture shimmers in the distance, because
   Vulkan textures have no mipmaps (`VulkanChanges.md` #7).

### What DriveOpen does now

- (Steps 2, 5 and 6.) `GetGroundHeightAt` now asks
  `TerrainData:GetSurfaceHeight`, instead of the noise.
  - E and Q edit the ground ahead (`EditGroundAhead`), with settings
    `editRadius`, `editAhead`, `editAmount` and `editInterval`.
  - `CheckSaving` runs at startup while `checkSaving` is true, saving to
    `saveFolder`.
  - `GetHeading` was pulled out of `PlaceCamera`.

- The map is 512 by 512 samples, half a unit apart: 256 units across,
  from -128 to 128. It used to be 257 samples, 128 units across.
- Regions are 64 samples (32 units) across, so there are 8 by 8 = 64
  regions. This is smaller than the 256 default on purpose, so the vehicle
  crosses region edges often, which tests collision across regions.
- `GenerateHeightMap` is unchanged, including the flat pads under the box
  stacks. The image goes through `TerrainData:ImportHeightMap`, with height
  scale `heightScale * terrainScale` and offset `baseY`. That gives the same
  heights the old `Terrain` did.
- `CheckImport` compares 104 samples (the 4 corners and 100 random ones)
  against the noise.
- Grass texture: `AllTerrainVehicle/Textures/grass.png`, copied from
  `physics/TerrainSplatting`. It repeats every 4 units.
- The render tree check's quadtree and octree regions grew from 80 to 140.
- Other world sizes (rain area, edges) come from `mapSize`, `terrainScale`
  and the base, so they grew with the map.

### Test run 1

- Everything compiled, including the shared package
  `Libraries.Game.Graphics.Worlds`.
- Only "Terrain data from a height map" failed. It had no message, so it
  stopped with an error rather than failing a check.
- Cause: `PixelMap.java` has a static block that calls a native function
  (`SetBlendNative`) when the class loads. The native library only loads
  when a game starts; its `System.load` line is commented out. **`PixelMap`
  can't be used in plain test programs.**
- Fix: `TerrainData:GetHeightFromPixel` is now public, and
  `TerrainDataPixelTest` checks it with pixels built as numbers.
  `ImportHeightMap` itself still needs checking in `DriveOpen` (step 4),
  where a game is running.

## Decisions made

1. **Package:** `Libraries.Game.Graphics.Worlds` (open decision 1).
2. **Region size:** 256 samples, Terrain3D's default (open decision 2).
   Regions go in a 32-by-32 grid of locations, -16 to 15, as in Terrain3D.
3. **Classes follow Terrain3D's split** (final names are the user's call):
   - `TerrainRegion` ← `Terrain3DRegion`;
   - `TerrainData` ← `Terrain3DData`;
   - `TerrainRegionModel` is the drawn and collidable piece;
   - `WorldTerrain` ← the `Terrain3D` node, used by games.
   These replace the earlier names `HeightField`, `TerrainPiece` and
   `OpenTerrain`.
7. **One shared texture:** set per model with `GetResources():SetTexture`.
   A texture path on the material would load the file again for every
   region, since `MaterialManagerVulkan` doesn't cache materials.
8. **Edges of the world:** where no region holds a sample, a mesh uses the
   nearest sample inside itself, so the ground carries on flat for normals.
   A mesh only reaches into the next region if that region exists.
4. **Saving uses `PixelMap:Save`.** Helper actions for packing heights into
   pixels can go on the new classes (step 6).
5. **Heights are full floats in world units** in a `Number32BitArray`, like
   Terrain3D's 32-bit float height maps. The 16-bit red and green format is
   only used to import images, the same way `Terrain` reads them.
6. **Missing regions:** `GetHeight` returns 0 and `SetHeight` does nothing
   where there is no region. Terrain3D returns NaN and logs an error, but
   Quorum has no NaN. `HasHeight` and `HasSample` say whether a region is
   there.

## Files written

| File | What it is |
|---|---|
| `SourceCode/Worlds/TerrainRegion.quorum` | One square of heights: region size, location, get and set a sample, `Fill`, height range (grows on set; `CalculateHeightRange` for the exact range), modified flag. |
| `SourceCode/Worlds/TerrainData.quorum` | All regions. Region size (64 to 2048, default 256), vertex spacing (default 1). Add, get and remove regions. Sample and world-position lookup with correct handling of negative samples. Edited area (a rectangle of changed samples, like Terrain3D's `_edited_area`). `ImportHeightMap(PixelMap, startX, startZ, heightScale, offset)` and `GenerateHeights(NoiseGenerator, startX, startZ, width, depth, heightScale, offset)`, both adding regions as needed. |
| `SourceCode/Tests/Worlds/WorldsTest.quorum` | Base test class: checks with messages, and `PackHeight`. |
| `SourceCode/Tests/Worlds/*Test.quorum` | 7 tests: region heights; data regions; samples; world positions; edited area; noise; height map pixels. |
| `SourceCode/Tests/WorldsTester.quorum` | Runs the tests, like `PhysicsTester`. |
| `SourceCode/RunWorldTests.quorum` | Main file that runs the tests and prints failures. |
| `SourceCode/Worlds/TerrainRegionModel.quorum` | `is Terrain`. Draws one region and is its collision shape. The mesh reaches one sample into the next region along x and z, if there is one, so there are no gaps. Normals and texture coordinates come from `TerrainData` across region edges. Vertex spacing is the model's x and z scale; the model sits at its lowest height. Overrides `GetPixelWidth`, `GetPixelHeight` and `FillAtPixel`, which read heights straight from `TerrainData`, so `HeightmapTerrainShape` works unchanged. Loads with the public `Model:Load(blueprint)`. The texture is shared through `GetResources():SetTexture`, so all regions use one copy. |
| `SourceCode/Worlds/WorldTerrain.quorum` | What a game uses (Terrain3D's main node). Holds `TerrainData`. Texture, texture repeat, friction, name. `Build` makes one `TerrainRegionModel` per region with physics. The game adds the models itself (`GetRegionModelCount`, `GetRegionModel`). |
| `Textures/grass.png` | Ground texture, copied from `physics/TerrainSplatting`. |
| `SourceCode/DriveOpen.quorum` | Copy of `Main`, now using `WorldTerrain` (see "What DriveOpen does now"). |

## Mesh update and free: investigation (open decision 3)

**Outcome (2026-10-18):** Option A was done as library order 7
(`Item7.md`), with per-frame staging buffers and its own barriers, so #4 was
not needed first. The Java `StopMapping` fix (#1) and Option B are still
open (library orders 3 and 11).

All Vulkan bugs and needed changes found so far are collected in
`VulkanChanges.md`, with fixes and a suggested order. The notes below are
the original findings.

Paths are under `Libraries/Game/Graphics/`. Java natives are in
`quorum-language/Plugins/QuorumPlugins/src/main/java/plugins/quorum/Libraries/Game/Graphics/Vulkan/`.

### What happens now

- `MeshVulkan:Load` (`Models/Vulkan/MeshVulkan.quorum:48-165`) packs 14
  floats per vertex into a CPU-visible **staging buffer**. The data is
  copied once into a **GPU-only buffer**, which the vertex shader reads by
  its address.
- The copy is recorded by `ModelManagerVulkan:ProcessPendingResources`
  (`:76-114`), every frame, for meshes waiting in `pendingMeshes`.
  `TransferBuffers` only ever copies once (`dataTransferred` guard).
- **Nothing is ever freed.**
  - `VulkanBuffer` has `Dispose`, but nothing in `Models/Vulkan` calls it.
  - Staging buffers stay alive after the copy.
  - `cachedBlueprints` is never emptied.
- **Native bug, confirmed:** `VulkanBuffer.java` `StopMapping` passes the
  mapped memory pointer to `vmaUnmapMemory`. VMA expects the allocation
  handle (`memoryHandle`). This is almost certainly the "crash" in the
  comment at `MeshVulkan.quorum:161`, and it makes `Dispose` unsafe on a
  mapped buffer.
- **The painter reads each mesh's buffer addresses fresh every frame**
  (`VulkanPainter3DBatch`, `VulkanDrawQueue3DInstanceMappedMemory`). So
  swapping a mesh's buffers is safe for later frames. Old buffers just have
  to live until the 2 or 3 frames already in flight finish.
- There is **no deferred-deletion queue**. Frames in flight = the swapchain
  image count, each with its own fence (`VulkanGraphics:410-415`).
- The "global vertex buffer index" in `MeshVulkan` is unused leftover code.
  There is no buffer table to clean up.
- **`Model:Load(ModelBlueprint)` is public** (`Model.quorum:2162`). A
  custom mesh can already be built: make `ModelData` with a unique ID, get a
  blueprint from `ModelManagerVulkan:GetModelBlueprint(ModelData)`, then call
  `model:Load(blueprint)`. `Terrain` calls the private `SetModelBlueprint`
  instead, which a project subclass can't.

### Option A: update a mesh in place (smallest change)

About 60 to 80 lines in 2 files; no Java change.

- `MeshVulkan`:
  - pull the vertex packing into its own action;
  - add `UpdateVertices(MeshData)` for the same vertex count. It re-packs
    into the staging buffer, which is still mapped, and marks the mesh for
    another copy.
- `ModelManagerVulkan`: add `QueueMeshUpdate(MeshVulkan)`.
- Copy barriers:
  - add a barrier before the copy (vertex shader reads, then transfer
    write);
  - make the barrier after the copy always run, and include the vertex
    shader stage.
- Safe as long as a mesh is updated at most once per frame. A fully safe
  version keeps one staging buffer per frame in flight, about 30 more lines.

This is enough for Tier 1 editing. Terrain pieces keep the same vertex
count when heights change.

### Option B: full dispose support

About 200 to 250 lines in about 6 files, plus the Java fix.

1. Fix `StopMapping` in `VulkanBuffer.java`: use `memoryHandle`, reset the
   pointer, and guard `Dispose`.
2. Add a deferred-deletion queue in `VulkanGraphics`: buffers are freed in
   `BeginNextFrame` once the frames that used them have finished.
3. `MeshVulkan:Dispose` queues all its buffers. Staging buffers are freed
   after the first copy unless the mesh is marked as changing.
4. `ModelBlueprintVulkan:Dispose`, plus
   `ModelManagerVulkan:RemoveBlueprint(key)` to drop a blueprint from the
   cache.
5. `ModelResourcesVulkan:Dispose` for animated models' buffers, which also
   leak today when a model's blueprint is replaced.

### Recommendation

Do **Option A** when Tier 1 reaches editing (step 5). Do the **Java
`StopMapping` fix** at the same time, because it's a real bug. Do
**Option B** as its own piece of work before Tier 3, when regions will load
and unload. All of these are library changes, so each needs the user's
go-ahead. Until then, rebuilding a piece means a new blueprint under a new
key, which leaks GPU memory.

## Steps

1. **Data** (`TerrainRegion`, `TerrainData`). **Done; tests pass.**
2. **Queries. Done; tests pass.** `GetSurfaceHeight`,
   `GetNormal`, `GetSlope`, `IsInSlope` on `TerrainData`. Following
   Terrain3D:
   - `GetSurfaceHeight`, blending between samples. Terrain3D blends the
     four corners bilinearly. Our mesh and collision use two triangles per
     cell, so this should follow the triangles so a query lands on the drawn
     ground.
   - `GetNormal`, as in Terrain3D: from the height here and one step along
     x and z.
   - `IsInSlope(x, z, minimumDegrees, maximumDegrees)`.
3. **The drawn piece. Done; checked in DriveOpen.** A `Terrain` subclass for one region. Its mesh
   reaches one sample into the next region so there are no gaps. It has
   texture coordinates and normals from `TerrainData`. Vertex spacing is
   applied as x and z scale, and the piece sits at its lowest height. It
   overrides `FillAtPixel`, `GetPixelWidth`, `GetPixelHeight` and
   `GetRenderBounds` so `HeightmapTerrainShape` works unchanged. It loads
   with the public `Model:Load(blueprint)`.
4. **The terrain object. Done; checked in DriveOpen.**
5. **Runtime editing. Done; checked in DriveOpen.**
   - `TerrainData:RaiseArea(x, z, radius, amount)`, a smooth mound or
     hollow, only for trying out editing until Tier 4 brushes.
   - `TerrainRegionModel:Rebuild` and `IsAffectedBy`.
   - `WorldTerrain:Rebuild()` rebuilds only models touched by the edited
     area, then clears it.
   - Rebuild order: new mesh with `Model:Load`, then the sizes, then
     `SetShape` (which recreates the physics nodes, since the model is in a
     layer), then `PlaceInWorld` (which reports a move, so the layer brings
     the physics bounds up to date).
   - Each rebuild leaked the old mesh until `VulkanChanges.md` #3 was done
     (2026-10-18, `Item7.md`); rebuilds now update the mesh in place.
6. **Save and load. Done; the save check passes.**
   - `TerrainData:Save(File folder)` and `Load(File folder)`. One PNG per
     region, `region_<x>_<z>.png`, like Terrain3D's one file per region.
     `terrain.txt` holds a version line, `regionSize`, `vertexSpacing`, and
     one `region x z lowest highest` line per region.
   - The helpers are on `TerrainRegion`: `GetHeightImage`,
     `SetHeightsFromImage`, `GetPixelFromHeight`, `GetHeightFromPixel`.
   - `terrain.txt` was later replaced by `terrain.json`
     (`OpenWorldTier3Handoff.md`).
   - Each height is a 24-bit level (red, green, blue) between the region's
     lowest and highest heights; alpha is full. Red and green are the coarse
     and fine parts, as in `Terrain`, so a saved region also imports as a
     16-bit height map with `ImportHeightMap`.
   - The first number given to `PixelMap:SetPixel` is z, as in `Terrain`
     and `ImportHeightMap`. `PixelMap:Save` writes the first number as the
     image's x, so the saved images look transposed (x and z swapped) when
     viewed. They are consistent with how `Terrain` reads images.
   - Save and Load need a running game (PixelMap), so they are checked by
     `DriveOpen:CheckSaving`, not by `RunWorldTests`.

## Open decisions for the user

1. ~~**Mesh update and free**~~: decided. Option A is done (library order
   7). Option B and the Java fix wait in `LibraryChangesPlan.md`.

## Risks to check while working

- Whether many pieces, each with its own collision shape, slow physics.
- Whether moving a static physics object (a piece's lowest height changes
  after an edit) updates its broadphase box.
- Whether `Terrain`'s own fields cause trouble in a subclass that never
  calls `LoadHeightMap`.
- `TerrainData` looks up a region for every sample, and
  `TerrainRegionModel` reads heights from it for every collision triangle.
  If building or physics is slow, keep the last region found, or keep a
  copy of each mesh's heights.
- If a region and its neighbors along x and z exist, but the one on the
  diagonal doesn't, the far corner of the mesh uses its own corner height.
  This leaves a tiny seam. It can't happen in DriveOpen's full square of
  regions.
- Startup is slower: the height map is 4 times as large (512 squared), and
  64 meshes are built.
