# Open Worlds Tier 2: handoff

Tier 2 is texturing the terrain. Updated after every completed operation,
so work can move between machines. The overall picture is in
`OpenWorlds.md` and `OpenWorldsHandoff.md`. Tier 1's record is in
`OpenWorldTier1Handoff.md`.

Last updated: 2026-10-18. **Paused.** Goal 1 (several ground textures,
blended) was done another way, as the splatting work in
`TextureSplattingPlan.md`. The rest of this plan (control map, auto shader,
paintable maps, holes) hasn't started.

## Rules for this work

Same as Tier 1:
- **Do not change the standard library** unless the user approves that
  specific change. Reading it is fine.
- **All code is written in AllTerrainVehicle first,** in package
  `Libraries.Game.Graphics.Worlds`, and moved to the library later.
- **Use Terrain3D as the guide** (`/Users/stefik/Repositories/Terrain3D`).
- **Test in `DriveOpen.quorum`.** Headless tests go in `Tests/Worlds`, run by
  `RunWorldTests`.
- **Claude never runs the game or `Default.jar`.** The user compiles, runs
  and pastes the output.

**Unlike Tier 1, Tier 2 can't be done without library changes.** Today every
3D model is drawn with one shader. Each change below is listed separately
for the user to approve (L1, L2, L3).

## Where we are

- **Goal 1 is working** through `TextureSplattingPlan.md`: four layers
  blended by splat weights, with slope-based weights and painting in
  DriveOpen. It uses one splat texture for the whole map, not one per
  region, because of the texture-group limit (`VulkanChanges.md` #19, #20).
- **L1 is done** (library order 9, `Item9.md`, 2026-10-18). The terrain
  draws with its own `TerrainSplatShaderProgram`.
- **L2 and L3** (mipmaps and anisotropy, library orders 10 and 8) are done
  (`Item25.md`, committed `768335de9`, confirmed). L4 (order 6) is not
  started.
- **Per-layer normal, roughness and AO** (this tier's PBR goal) work
  through texture sets (`Item24.md`, `TerrainMaterialsPlan.md`), with
  height blending.
- Goals 2 to 5 are not started. Tier 4's paint brushes (`TierFour.md`)
  build on the splat data.

## Goal

From `OpenWorlds.md` Tier 2:
1. Make the terrain draw several ground textures, blended.
2. Use Terrain3D's control map instead of the four-channel `TextureSplat`.
3. Auto shader by slope, height blending, detiling, macro variation, two
   texture scales.
4. Paintable color and roughness maps.
5. Holes.

## How Terrain3D textures its terrain

Sources: `Terrain3D/doc/docs/shader_design.md`, `controlmap_format.md`,
`src/shaders/main.glsl`, `src/shaders/auto_shader.glsl`.

- **Index map, not splat map.** Each vertex has one 32-bit control value:
  - base texture ID (5 bits, 0-31);
  - overlay texture ID (5 bits);
  - blend between them (8 bits);
  - texture rotation (4 bits) and scale (3 bits);
  - hole, navigation and auto-shader flags (1 bit each).

  A splat map stores a strength for every texture at every point instead,
  which costs far more memory for 16-32 textures.
- **Blending in the shader:**
  - Each pixel reads the control values at the 4 corners of its grid cell.
  - It weighs them by how close it is to each corner (bilinear).
  - For each corner, it samples the base and overlay textures.
  - Weights are sharpened by each texture's height, kept in the albedo's
    alpha, with `exp2(sharpness * log2(weight + texture_weight + height))`.
    So stones show through grass in their gaps instead of fading evenly.
    `blend_sharpness` sets how sharp.
- **Auto shader:** where a vertex's auto flag is set, base and overlay
  become two chosen textures, blended by slope (the normal's y), with an
  optional reduction by height (`auto_slope`, `auto_height_reduction`).
- **Detiling:** each texture tile gets a random rotation and shift from a
  hash of its position, scaled per texture, to hide repetition.
- **Texture assets** (`Terrain3DTextureAsset`): albedo (with height in
  alpha), normal (with roughness in alpha), color tint, UV scale, detiling,
  roughness change, AO settings.
- **Color map** per region: RGB tint plus roughness in alpha. Paintable.
- **Holes:** a control bit. The shader discards those pixels, and collision
  leaves those cells out.
- Terrain3D works out normals in the fragment shader from the height map.
  We don't need to, since our meshes are built on the CPU with normals.

## What Quorum's renderer allows (investigation, 2026-10-17)

G = `quorum-language/Quorum/Library/Standard/Libraries/Game/Graphics/`,
J = `quorum-language/Plugins/QuorumPlugins/src/main/java/plugins/quorum/Libraries/Game/Graphics/`.

### Custom shader programs can be written in this project

- A `ShaderProgram` becomes a pipeline through `VulkanShaderProgram`
  (G/Shaders/Vulkan/VulkanShaderProgram.quorum:25-73). Compiling registers
  it by name automatically.
- `DefaultShaderMapping3DVulkan` only describes the default layout:
  - 4 G-buffer outputs;
  - camera uniform and immutable sampler in set 0;
  - global textures in set 1;
  - 8 bytes of push constants.

  A terrain program can reuse it unchanged.
- **Everything needed is public:**
  - a `TerrainFragmentShader is Shader` (`SetType`, `SetName`, `SetCode`);
  - a `TerrainShaderProgram is ShaderProgram` that adds the default 3D vertex
    shader (`ShaderManager:GetDefaultVertexShader3D`) and the terrain fragment
    shader, with `DefaultShaderMapping3DVulkan`.

  Both can live in AllTerrainVehicle.

### But the painter ignores them (needs L1)

**Resolved 2026-10-18 by L1 (library order 9).** Kept as the record of why.

- `VulkanPainter3D` binds one pipeline for the whole geometry pass. The
  code says "TO-DO: Support custom shaders for 3D." (G/Vulkan/VulkanPainter3D.quorum:1098, and 1453).
- Batches go into draw queues chosen only by texture group
  (`GetCurrentDrawQueue`, about 1658-1687).

### Passing data to a terrain shader

- **Per-instance data is full** (104 bytes):
  - pointers to vertices, indices and materials;
  - texture override flag and index;
  - color override flag and color;
  - the model matrix.

  Adding a field means changing the Java writer and three shaders.
- **The texture override is the per-model slot we can use.** Tier 1
  already sets it per region (to the grass). In Tier 2 it will hold **each
  region's control map texture**. The default vertex shader already passes
  it to the fragment shader as `v_textureOverrideIndex`.
- **Ground texture indices** (up to 32) can be written into the shader's
  code as constants when the program is built. Textures only get their
  index once the texture manager processes them (during a frame), so the
  program must be built after the ground textures have indices.
- **The fragment shader also gets the world position** (`v_position`), so
  ground texture coordinates come from world x and z. Mesh UVs can hold
  each vertex's sample position in its region instead, for looking up the
  control map.
- **Texture index mismatch** (J/Vulkan/VulkanDrawQueue3DInstanceMappedMemory.java:52):
  - the override writes the global texture index;
  - queues are chosen by texture group.

  These agree only while there is one texture group, which is true in
  practice. It's a latent library bug, M4 below.

### Shadows and lighting keep working

- The shadow pass uses its own default shadow pipeline. It reads the same
  instance data and only samples a texture for alpha, so terrain with a
  custom fragment shader casts shadows unchanged (control map texture is
  opaque).
- Lighting and receiving shadows happen in the deferred lighting pass from
  the G-buffer, so they work as long as the terrain shader writes the same 4
  outputs.

## Library changes needed (each needs the user's approval)

| # | Change | Size | Needed for |
|---|---|---|---|
| **L1** (done 2026-10-18, `Item9.md`) | **Per-model shader programs in `VulkanPainter3D`:** use `Model:GetShaderProgram()` when set, else the default. Split draw queues by program (or batch on blueprint plus program). In `End`, bind each program's pipeline (made lazily against `sceneRenderPass`) before its queues. Keep the shadow pass drawing every queue with the default shadow pipeline. Custom programs must use the default layout. | ~40-60 lines, `VulkanPainter3D.quorum` and `VulkanPainter3DBatch.quorum`, no Java | Everything in Tier 2 |
| **L2** | **Mipmaps.** New `BlitImageCommand` (Quorum and Java; there is no blit today). Images get `IMAGE_USAGE_TRANSFER_SOURCE_BIT` and a full mip chain, built with barriers and blits per level. The view gets the level count. | ~110 lines plus ~60 Java | Ground textures that don't shimmer (`VulkanChanges.md` #7) |
| **L3** | **Sampler:** linear mip mode and anisotropic filtering on the default sampler. `VulkanDevice.java` already turns on the device feature when supported. Must be set before any pipeline is made, since the sampler is built into every layout. | ~5 lines | Sharp ground at low angles (`VulkanChanges.md` #8) |
| L4 | Fix the override index mismatch: write the local index in the group | ~1 line Java | Only once there is more than one texture group |

L1 is `VulkanChanges.md` #6. L2 and L3 are #7 and #8.

## Plan

Each step ends with tests or a DriveOpen run, then an update to this file.

1. **Control maps (no library change).**
   - `TerrainControl`: encodes and decodes the 32-bit control value with
     Terrain3D's exact bit layout (base, overlay, blend, angle, scale,
     hole, navigation, auto).
   - `TerrainRegion` gets a control map (`Integer32BitArray`). New regions
     start with auto shading on, like Terrain3D.
   - `TerrainData` gets world-position actions: `SetTextureAt` (base,
     overlay, blend), `SetHole`, `SetAutoShader`, getters.
   - Saving and loading: a second PNG per region (`control_x_z.png`), the
     32 bits stored as the pixel's 4 bytes exactly.
   - Headless tests for encoding, painting, and the pixel round trip.
2. **Texture assets (no library change).** `TerrainTextureAsset`: albedo
   file (height in alpha, optional), UV scale, color tint, detiling.
   `WorldTerrain` keeps up to 32 (`AddTexture`), plus auto shader settings
   (base, overlay, slope, height reduction) and blend sharpness.
3. **L1, then a first terrain shader.** After L1:
   - `TerrainFragmentShader` and `TerrainShaderProgram` in this project.
   - A first version that only samples one ground texture by world
     position. This proves custom programs work and that shadows and
     lighting still do.
4. **Control-map shader.**
   - Each `TerrainRegionModel` makes a control texture for its mesh (region
     plus the overlap row and column) from `TerrainData`, and sets it as its
     texture override.
   - Mesh UVs become sample positions.
   - The fragment shader reads the 4 corner controls with `texelFetch`,
     weighs them, samples base and overlay ground textures by world
     position, height-blends them, and writes the G-buffer.
   - DriveOpen: grass, rock (`TerrainSplatting/rock.png`), and a third
     texture. The auto shader puts rock on slopes, and the stack pads get
     painted dirt.
5. **Detiling, per-texture UV scale and tint, macro variation** in the
   shader.
6. **Painting in DriveOpen.** Keys to paint the texture under or ahead of
   the vehicle, rebuilding changed regions' control textures, like E and Q.
   Brushes proper are Tier 4.
7. **Holes.** The shader discards hole pixels. Collision must also leave
   hole cells out. `HeightmapTerrainShape` asks for every cell's triangles,
   so `TerrainRegionModel:FillAtPixel` needs a way to say "no triangle". The
   shape may need a small change; to be investigated.
8. **Color and roughness maps.** There is no free per-model texture slot
   after the control map. Options:
   - pack color into a second texture referenced from the region's own
     material (each region already has its own material);
   - extend the instance data (library);
   - combine control and color into one larger texture.

   To be decided when we get there.
9. **L2 and L3** whenever the user approves them. They make every step look
   better, so earlier is better.

## Open decisions for the user

1. ~~**Approve L1**~~: done (library order 9).
2. **Approve L2 and L3** (mipmaps, anisotropic filtering), now or later?
3. **Ground textures:** use `grass.png` and `rock.png` from TerrainSplatting
   (128 by 128, origin and license unknown), or does the user have a
   preferred set (for example CC0 textures from ambientCG or Poly Haven,
   which come with height maps)?

## Known limits carried from Tier 1

- ~~Every rebuild leaks the old mesh~~: fixed by library order 7; meshes are
  updated in place.
- Textures still can't be updated or freed (`VulkanChanges.md` #20), so the
  splat texture rebuilt after each paint stroke leaks the old one. This is
  the same problem for a control map.
