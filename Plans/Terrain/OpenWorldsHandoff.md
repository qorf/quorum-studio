# Open Worlds: handoff

Kept up to date as work goes on. The starting position, comparison and full
feature list are in `OpenWorlds.md`.

Last updated: 2026-10-20

## 2026-10-20: natives, 16-bit textures, and smooth physics

**Natives (`quorum-natives`, committed by the user, `948bb43` and
`daa3c65`; libraries in `quorum-language` `d2ffa8518`):**
- **The image loader** (`gdx2d_load`) uses stb_image 2.29. 16-bit PNGs
  and progressive JPEGs load. The crashing jpgd fallback is gone. There's a
  `requestedChannels` argument. (`VulkanChanges.md` G, fixed.)
- **The FreeType font functions** `GetFamily` and `GetStyleName` are back.
  They were missing from the source, and were rebuilt from the shipped
  binary.
- **AutoBuild** builds Mac (universal), Windows (MinGW cross-compile) and
  iOS from one CMakeLists on a Mac. It installs CMake and MinGW-w64 with
  Homebrew. See `quorum-natives/AutoBuildChanges.md`. Linux is planned in
  `quorum-natives/LinuxNatives.md`.

**16-bit texture sets (project, uncommitted):** DriveOpen and
TerrainEditor have `useOriginal16BitTextures = true`, and use the original
normal and height maps from each set's `Original16bit/`. Confirmed on
screen.

**Smooth physics and cameras (library `MakeAWish`, committed as `10c6ef617`
and `feda793e1`; confirmed on screen):**
- **I, interpolation:** physics items are drawn between their last two
  steps (`Item3D:GetDrawnTransform`, worked out once a frame in `Layer3D`).
  `Vehicle` reads physics positions in its step.
- **J, camera timing:**
  - `Game:UpdateAfterPhysics(seconds)` is a new game hook
    (`GameLoopAfterPhysics.md`).
  - `Camera:Follow(item, behind, above)` is a chase camera with separate
    getters and setters, angle (90 is the right), aim height, turning,
    smoothing and `JumpToFollowPosition` (`CameraFollowPlan.md`).
  - DriveOpen uses `Follow`, and `PlaceCamera` is gone.
- Details are in `VulkanChanges.md` I and J.

**Open from this round:**
- **`Layer2D`** has the same missing interpolation.
- **`PixelMap.java`** could pass 4 channels to the loader and skip the
  Vulkan RGBA conversion (see F).
- **TerrainWalk's `CameraFollower`** source is missing. It could now use
  `Camera:Follow` or `UpdateAfterPhysics` instead.

## Where we are (2026-10-19, end of session)

**Library branch: `MakeAWish`** in `quorum-language`, from Item 37 on.
Before that, `TerrainShader`; both held the same commit, `8a656d7c3`, when
the switch was made. **Never switch branches:** use the one checked out
(the user's rule).

**Library commits this round:**
- `8d0ca607f`: Item 31;
- `b1d8e1a8e`: Items 32 to 35;
- `8a656d7c3`: the white-texture fix;
- `0b5de0b62`: Item 37 (spotlights).

**Stopped here.** The current work is spotlights and water.
- **Spotlights (`SpotlightPlan.md`):**
  - Phases 1 and 2 are **committed** as `0b5de0b62` (`Item37.md`), Vulkan
    only, **not yet reported on screen**:
    - the point light direction fix;
    - a range falloff for point lights;
    - `SpotLight`, with no shadows.
  - **Under OpenGL,** adding a spotlight alerts. `EnvironmentOpenGL` was
    left unchanged at the user's request.
  - **Project:**
    - `SpotlightCheck.quorum` (A/Z cone, R/F range, S sweep, P point
      light);
    - **headlights in DriveOpen:** H switches them, N switches night. They
      are placed from the car's physics transform every frame.
- **Water (`WaterPlan.md`):** phase 1 is written as **`Item38.md`, for
  review, not applied**. It covers time, the camera position, screen size
  and near and far planes for every Vulkan 3D shader.
- **Group 1 of the priority list is written, for review, nothing
  applied:** `Item39.md` (the water pass, a general forward stage, and
  `WaterSurface` with `WaterCheck`), `Item40.md` (distance fog),
  `Item41.md` (the sky reflected in water), `Item42.md` (the ground look,
  project only) and `Item43.md` (sea level and lakes, project only). Each
  has exact before and after code.
  - **Apply order:** 38, then 39, 40, 41; 42 and 43 after 38 and 39.
  - **Library files touched:** `VulkanShaderMapping` (forward flag),
    `VulkanPipelineInfoCreator` (`AddForwardInfo3D`), `VulkanPainter3D`
    (forward draw after lighting, sky set 4), `EnvironmentVulkan` and the
    lighting shader (fog). OpenGL is untouched.
  - **New project files:** `Worlds/WaterShaderProgram`,
    `WaterShaderMapping`, `WaterFragmentShader`, `WaterSurface`, and
    `SourceCode/WaterCheck.quorum`.
  - **DriveOpen keys added:** F fog (40), G ground look (42), L sea, U/J
    sea level, B drop a lake, V remove lakes (43).
- **Item 71 is written for review** (`../QuorumStudio/QuorumStudioPlan.md`,
  the new folder for Quorum Studio documents): the Worlds classes move into
  the standard library, a terrain becomes a scene item (`TerrainItem`,
  `"Class": "Terrain"` pointing at its `terrain.json`), and Studio's
  palette adds a flat terrain. No new keys. Item 70 (brushes) follows it,
  one tool at a time.
- **Memory:** done (`MemoryPlan.md`, Items 31 to 35, run and confirmed).
  `Item36.md` (per-frame garbage) is still for review.

**Next, in order:**
1. **Run SpotlightCheck and DriveOpen at night** (N, then H):
   - soft-edged cones;
   - the green cone turning with its platform;
   - the lamp lighting the ground around it correctly.
2. **Review and apply `Item38.md`, then `Item39.md` to `Item43.md`,** in
   that order (each is written; nothing applied). Run `WaterCheck` after
   39, and DriveOpen after 40, 42 and 43.
3. **Then Group 2 of "Priority list (Items 39 on)"** below: the bug batch
   (44), shadow fixes (45), physics (46), and Item 36.

**Finished in this round (2026-10-19), all committed in the library:**
- **terrain materials:** AmbientCG texture sets with height blending;
- **mipmaps and anisotropic filtering;**
- **a skybox** on the shader system (OpenGL and Vulkan), and a generated day
  sky in DriveOpen;
- **MSAA** for 3D and 2D under Vulkan;
- **resize fixes:** a sharp 3D view, and shadows that survive it;
- **no boxing** in `VulkanPainter3DBatch`;
- **`vSyncEnabled` under Vulkan.**

In TerrainWalk (project), the ball snapping back was the camera being
placed before the physics step. `CameraFollower` places it after.
**Fixed in the library on 2026-10-20** for every game: use
`Camera:Follow` or `Game:UpdateAfterPhysics` (`VulkanChanges.md` J).

### What works, confirmed by the user

- **Tiers 1 and 3** (terrain data, collision near the player, streaming,
  distant terrain): see the Done table below.
- **Splatting:** four layers, painted in TerrainEditor and DriveOpen, with
  one splat texture updated in place (`Item20.md`).
- **Tier 4, stages 1 to 3** (`TierFour.md`):
  - the brush engine, and undo and redo (`RunWorldTests` 22 of 22);
  - `TerrainEditor.quorum`, with a keyboard control for every mouse control;
  - picking, the brush preview, presets, stamps, erosion and auto paint;
  - the user guide, `TerrainEditorInstructions.md`.
- **Texture sets** (`Item24.md`), on boxes and on the terrain:
  - grass is Ground037 and rock is Rocks002; dirt and sand are still plain;
  - B in the editor changes how softly they blend.
- **Mipmaps and anisotropy** (`Item25.md`): no shimmer in the distance.
- **Skybox** (`Item26.md`): SkyboxCheck under Vulkan, and the Day sky in
  DriveOpen (K toggles it).
- **3D MSAA** (`Item27.md`) and a **sharp view after a resize**
  (`Item28.md`).

### Waiting for a run

These are committed, but nobody has reported on them yet:
- **Item 30:** resize with 3D MSAA on; models stay lit and shadowed.
- **Item 29:** `MSAA2DCheck`; turned 2D squares are smooth at 4.
- **#28:** memory no longer climbing from batch boxing.
- **#29:**
  - Vulkan games now run uncapped by default (`vSyncEnabled = false`);
  - setting it to `true` gives FIFO.
- **Older items:**
  - DriveOpen: minimize and restore (#23), and the "Meshes so far" count;
  - TerrainEditor: Shift N, Shift X and Shift U.

### Commits

- **Library (`quorum-language`, `TerrainShader`):**
  - `804c85c40`: Items 7, 9 and 20, and #23;
  - `3e6fde7e0`: Item 24;
  - `768335de9`: Items 25 and 26;
  - `7ad442acb`: Items 27-30, #28 and #29.

  Nothing is uncommitted.
- **Project (`physics`):** `bb2f7c5` holds most of the work. Still
  uncommitted: TerrainWalk's `CameraFollower` and diagnostic keys, and some
  documents.

### Priority list (Items 39 on, 2026-10-19)

**Every code task still open, numbered in the Item series.**
- **Item 37** (spotlights) is committed. **Item 38** (time and camera data)
  is written for review. **Item 36** (per-frame garbage) is written for
  review and keeps its number.
- **Library** items change `quorum-language` on the checked-out branch
  (`MakeAWish`; never switch). Each needs an approved `ItemN.md` with the
  exact code first.
- **Project** items change AllTerrainVehicle. They get an `ItemN.md` too
  when they are large.
- **Sizes:** S is under a day of changes, M a few files, L a plan of its
  own first.

**Why this order:**
1. Finish what is in flight: spotlights and water.
2. Then cheap fixes and visual wins that touch everything after them.
3. Then changes to the terrain's data format, before more ground is
   painted in the old format.
4. Then the large engine work.
5. OpenGL parity comes last, as the user prefers Vulkan-only changes.

#### 1. In flight: lights and water

| Item | Work | Kind | Size | Notes |
|---|---|---|---|---|
| 37 | Run SpotlightCheck and DriveOpen at night (N, H) | test only | — | Committed, `0b5de0b62` |
| 38 | Time, camera position, screen size and near/far for 3D shaders | library | S | `Item38.md`, for review. Water phase 1 |
| 39 | Water pass and `WaterSurface`: depth color, soft shore, scrolling normals, sun glint; `WaterCheck` | library + project | L | `Item39.md`, for review. Water phase 2. Needs 38. A general forward stage (`UseForwardPass`, `AddForwardInfo3D`) drawn inside the lighting pass |
| 40 | Fog (#17): distance fade to the sky's color in the lighting pass | library | S | `Item40.md`, for review. Water decision 6: alongside water. Also hides the far edge of the terrain. API on `EnvironmentVulkan` |
| 41 | Water sky reflection with fresnel (binds the sky cube in the water pass) | library | M | `Item41.md`, for review. Water phase 3. Needs 39 and 40 |
| 42 | Ground look: detiling, macro variation, dual scaling, cliff projection; texture sets for dirt and sand | project (splat shader) | M | `Item42.md`, for review. Terrain3D's shader modules, switchable at run time (G in DriveOpen). Uses 38's camera data. Dirt and sand sets wait for the user's download |
| 43 | Sea level and lakes in WorldTerrain; water in DriveOpen | project | M | `Item43.md`, for review. Water phase 5. Needs 39 |

#### 2. Correctness and cleanup (small, affects everything)

| Item | Work | Kind | Size | Notes |
|---|---|---|---|---|
| 44 | Library bug batch: B (`Integer32BitArray` before `SetSize`), A (`FileReader:ReadLines`), H (a new `Material`'s zero diffuse), #13 (texture override index), #12 (tidying), `EnvironmentVulkan:SetMaxLights`'s undeclared `isDirty` | library | S | `VulkanChanges.md`, orders 1 to 6 |
| 45 | Shadow correctness: cascade snapping (`row3column1`), `lastSplitDistance`, #14 (animation compute ordering), #4 (optional) | library | S | Shadow shimmer when the camera moves |
| 46 | Physics: a game-level after-physics hook, drawn positions blended between physics steps, and C (an item removed and added again doesn't collide) | library | M | The TerrainWalk findings |
| 36 | Per-frame garbage, in four parts, each measured; with MemoryCheck's L, T and S loops | library + project | M | `Item36.md`, written |

#### 3. Lights, continued

| Item | Work | Kind | Size | Notes |
|---|---|---|---|---|
| 47 | Spotlight shadows: one layer per spotlight, frustum culling of casters | library | M | Spotlight phase 3 |
| 48 | Shadow map memory: a per-light map size, and fewer or smaller maps for point lights (DriveOpen's lamp costs about 2.6 GB) | library | M | Options in `VulkanChanges.md` |

#### 4. Terrain data, before much more painting

| Item | Work | Kind | Size | Notes |
|---|---|---|---|---|
| 49 | Index (control) map: up to 32 texture sets, base and overlay per pixel, per-pixel texture angle and scale; with #19 (texture group size) | project + library | L | Changes the saved splat format. A plan first |
| 50 | Holes: caves, tunnels, building sites (mesh, collision, shader) | project | M | |
| 51 | Color, roughness and wetness maps, paintable in TerrainEditor | project | M | |
| 52 | Background terrain beyond the regions (flat or noise ring) | project | M | Gives the sea a horizon |
| 53 | Heightmap import and export: EXR, R16 | project + library | S–M | G (16-bit PNGs) is fixed (2026-10-20); 16-bit heightmap PNGs now load, though only 8 bits per channel are kept |
| 54 | Debug views: region grid, contour lines, height, control and PBR views | project | S | Shader switches |

#### 5. World-making workflow (project)

| Item | Work | Kind | Size | Notes |
|---|---|---|---|---|
| 55 | Height layers | project | M | |
| 56 | Rule-based painting from derived maps (slope, height, curvature) | project | M | |
| 57 | Splines for roads and rivers | project | L | Rivers lead to water extras (66) |

#### 6. Filling the world

| Item | Work | Kind | Size | Notes |
|---|---|---|---|---|
| 58 | Instancing at scale (#18: draw queues past 8,192 instances) | library | M | Before foliage |
| 59 | Instancer: foliage and props per region, levels of detail, placement rules | project + library | L | Tier 5 |
| 60 | Grass and wind (uses 38's time) | project | M | |

#### 7. Large engine work

| Item | Work | Kind | Size | Notes |
|---|---|---|---|---|
| 61 | GPU clipmap mesh | library + project | L | Tier 3, "two routes" |
| 62 | Displacement (tessellation from texture heights) | library | L | After 61 |
| 63 | Floating origin, camera-relative drawing (#16) | library + project | L | For worlds past about 10 km |
| 64 | Occlusion culling | library | L | |
| 65 | Navigation mesh baking | library | L | Quorum has no navigation system |
| 66 | Water extras: refraction, waves, foam, underwater (needs 40), rivers (needs 57) | library + project | L | Water phase 6 |

#### 8. Parity, editor and packaging (lowest)

| Item | Work | Kind | Size | Notes |
|---|---|---|---|---|
| 67 | OpenGL spotlights (per-vertex; stops the alert under OpenGL) | library (OpenGL) | M | Spotlight phase 4 |
| 68 | OpenGL water | library (OpenGL) | M | Water phase 4 |
| 69 | Spotlights in scene files and the scene editor | library | M | Spotlight phase 5 |
| 70 | Tier 4 stage 4: brushes in Quorum Studio | Studio | L | After 71. Plan in `../QuorumStudio/QuorumStudioPlan.md`: brushes in the scene editor, one tool at a time, keys matching Studio's. First check Vulkan 3D inside a Studio panel |
| 71 | Moving the Worlds classes into the standard library, and terrain in scene files and Studio's palette | library | M | **Written for review** in `../QuorumStudio/QuorumStudioPlan.md`, with exact code. The user moved it ahead of the other groups (2026-10-19): real scene files make everything after it easier to test. After it, Items 39 to 43's water files go into the library's Worlds folder instead of the project's |

**Only if needed:** #9 (texture formats other than RGBA8) and #10 (textures
in vertex shaders).

**Paused tracks** (in `Handoff/Handoff.md`):
- the culling tests;
- the car model;
- shadow leftovers;
- older library bugs.

### Done

| Work | Record | Status |
|---|---|---|
| Tier 1: terrain data, queries, region meshes, editing, save and load | `OpenWorldTier1Handoff.md` | Done |
| Tier 3: collision near the player, streaming, distant terrain | `OpenWorldTier3Handoff.md` | Done |
| Texture splatting (Tier 2 goal 1) | `TextureSplattingPlan.md` | Done |
| Tier 4 stages 1 to 3 | `TierFour.md`, `TerrainEditorInstructions.md` | Done |
| Terrain materials (texture sets, height blending) | `TerrainMaterialsPlan.md`, `Item24.md` | Done |
| Library: mesh update in place, per-model shaders, minimize crash, textures in place | `Item7.md`, `Item9.md`, `Item20.md` | Committed, confirmed |
| Library: mipmaps and anisotropy | `Item25.md` | Committed, confirmed |
| Library: skybox (OpenGL and Vulkan) | `SkyboxPlan.md`, `Item26.md` | Committed, confirmed |
| Library: MSAA 3D and 2D; resize; shadows after a resize | `MSAAPlan.md`, `MSAA2DPlan.md`, `Item27.md` to `Item30.md` | Committed; 2D MSAA and Item 30 waiting for a run |
| Library: batch boxing (#28), `vSyncEnabled` under Vulkan (#29) | `VulkanChanges.md` | Committed, waiting for a run |

### Rules (unchanged)

- All code goes in AllTerrainVehicle first, in
  `Libraries.Game.Graphics.Worlds`, and moves to the standard library later.
- The standard library is changed only when the user approves that specific
  change, with the exact before and after shown first. Larger ones get an
  `ItemN.md`.
- Claude never runs the game or `Default.jar`. The user compiles, runs and
  pastes the output.
- Test main: `SourceCode/DriveOpen.quorum`. Headless tests:
  `RunWorldTests`.

## Renderer check (2026-10-17)

Kept as the record of what was found then. Since then: per-model shader
programs work (library order 9), and meshes can be updated in place (order
7).

Bugs and changes for the renderer are collected in `VulkanChanges.md`.

Paths are under `quorum-language/Quorum/Library/Standard/Libraries/Game/Graphics/`.

### How 3D drawing works now

- **Deferred rendering.** A geometry pass writes four images: position,
  color, normal, and PBR values (roughness and metallic). A full-screen
  lighting pass then lights them. A terrain shader only has to fill those
  four outputs, and lighting and shadows from lights come for free.
- **Vertex pulling.** The vertex shader reads vertices and indices straight
  from GPU memory through pointers (`GL_EXT_buffer_reference`), not from
  bound vertex attributes. See `Shaders/Vulkan/Vertex/DefaultVertexShader3DVulkan.quorum`.
- **Bindless textures.** Every loaded texture sits in one global array,
  `globalTextures[]`, sized to the device's limit. Shaders pick a texture by
  index. See `Shaders/Vulkan/Fragment/DefaultFragmentShader3DVulkan.quorum`
  and `Vulkan/VulkanTextureManager.quorum`.
- **Materials** hold one diffuse color, one texture index, an optional
  normal map and an optional roughness map. One texture per material.
- **Shaders are GLSL compiled at runtime** to SPIR-V through shaderc
  (`Shaders/SPIRVShaderCompiler.quorum`). Vertex, fragment, geometry,
  compute and tessellation stages are all wired into the compiler.
- **Compute shaders are already used**: animated models are pre-processed
  by a compute shader that writes their vertices, and are then drawn by the
  normal pipeline (`Vulkan/VulkanPainter3D.quorum` near line 1080).

### The four questions

| Question | Answer |
|---|---|
| Read textures in the vertex shader? | **Not today, but a one-line change.** The global texture binding is fragment-only (`VulkanTextureManager.quorum:61`), and the upload barrier targets the fragment stage only (line 169). Adding the vertex stage to both would allow it. **Better option:** store heights in a GPU buffer and read them through a pointer, the same way vertices are read now. That needs no descriptor change. |
| Texture arrays? | **No real arrays** (every image is 2D with one layer). **Not needed**: the bindless global array already lets a shader pick any of many textures by index, which covers Terrain3D's 32 textures. |
| Unsigned integer textures (for the control map)? | **No.** Every texture is uploaded as `R8G8B8A8_UNORM` from a `PixelMap` (`VulkanTextureManager.quorum:135`, `:184`); the format line is commented out. 16-bit and float formats aren't supported either. **Workaround:** keep the control map (and heights) in GPU buffers of 32-bit unsigned integers and floats, read through pointers. |
| A custom shader and material for terrain? | **Not today.** `Model` has `SetShaderProgram`, but `VulkanPainter3D` ignores it: every model in the scene is drawn with one default pipeline (`VulkanPainter3D.quorum:1099-1100`). The shadow pass also uses one fixed pipeline. |

### Other things found

- **No mipmaps.** Textures have one level, and the one shared sampler uses
  nearest mip mode. Tiled terrain textures will shimmer and look noisy at a
  distance. Mipmap generation on upload is needed for good terrain
  texturing, and helps every model.
- **One sampler** for everything: linear filtering, repeat wrapping. Repeat
  suits tiled ground textures. Anisotropic filtering is off, even though the
  device feature query exists. Turning it on would make terrain seen at low
  angles much sharper.
- **Skybox is not implemented** in the Vulkan painter
  (`VulkanPainter3D.quorum:1690`). It matters for open worlds later.

### Renderer changes needed, by tier

- **Tier 1:** none. A CPU-built mesh with proper texture coordinates draws
  with the default pipeline and one texture, with shadows.
- **Tier 2 (splatting):** a terrain fragment shader, and either
  - **(a)** per-batch pipeline choice in `VulkanPainter3D`, which would
    also make `Model:SetShaderProgram` work for everyone (recommended); or
  - **(b)** a terrain flag in the default material and a branch in the
    default fragment shader (smaller, but grows the shader for every model).

  Also mipmaps, and preferably anisotropic filtering.
- **Tier 3 (camera-following mesh):** two possible routes.
  - **Compute route:** follow the animation pattern. A compute shader moves
    the clipmap grid's vertices to the right heights each frame, and the
    default pipeline draws them. Shadows and picking keep working unchanged.
  - **Vertex route:** a terrain vertex shader reads heights from a buffer.
    This is the Terrain3D way, but it needs its own geometry and shadow
    pipelines.

  The compute route reuses more of what exists.

## Decisions made

- Terrain3D is the guide, used the way Bullet was for physics.
- Terrain and world code goes in the standard library; brushing tools go in
  Quorum Studio (`TierFour.md`).
- Splatting uses one splat texture for the whole map, because of the
  renderer's texture groups (`VulkanChanges.md` #19, #20).
- The terrain is drawn with its own shader program (Route 2), not by
  replacing the default program (2026-10-18).
- Floating origin is deferred: the grid tops out near 8 km.

## Known issues in the current code

- `Terrain` sets every texture coordinate to `(0,0)`.
- `Terrain` stores `stickWidth` and `stickDepth` but never uses them.
- `TextureSplat` is not read by the renderer itself; our
  `TerrainSplatShaderProgram` draws the splatting.
- Texture groups 3 and up are never drawn (#22). Ground textures and
  texture sets must load before labels, or the splat shader can't reach
  them (WorldTerrain warns when one lands outside group 0).
- Meshes made for a new grid size, and streamed-out regions, can't be freed
  (order 11). This is part of the memory work next (`VulkanChanges.md`).
- A new `Material`'s diffuse is (0,0,0,0) (`VulkanChanges.md` H):
  hand-built materials must set a diffuse.
- Adding regions outside the splat texture's square makes a new splat
  texture, which lands outside group 0, and the ground turns grey until a
  restart (`TerrainEditorInstructions.md`). Reusing texture places (memory
  work) and #22 would fix it.
- Vulkan games now run uncapped by default (#29). Set `vSyncEnabled = true`
  for FIFO.

## Key places

| What | Where |
|---|---|
| Quorum terrain | `quorum-language/Quorum/Library/Standard/Libraries/Game/Graphics/Worlds/` |
| Terrain collision | `.../Libraries/Game/Collision/Shapes/HeightmapTerrainShape.quorum` |
| Noise | `.../Libraries/Compute/Noise/` |
| 3D painter (pipelines, passes) | `.../Libraries/Game/Graphics/Vulkan/VulkanPainter3D.quorum` |
| Texture upload and sampler | `.../Libraries/Game/Graphics/Vulkan/VulkanTextureManager.quorum` |
| 3D shaders | `.../Libraries/Game/Graphics/Shaders/Vulkan/` (Vertex, Fragment, Compute, Mapping) |
| Quorum Studio | `/Users/stefik/Repositories/quorum-studio/QuorumStudio` |
| Terrain3D source | `/Users/stefik/Repositories/Terrain3D/src` |
| Terrain3D design notes | `/Users/stefik/Repositories/Terrain3D/doc/docs` |
| Splatting test | `physics/TerrainSplatting` |
