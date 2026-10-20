# Texture splatting: plan

Written 2026-10-17. Updated 2026-10-18.

**Where it stands:** steps 1 to 6 are done. Splatting runs in DriveOpen with
one splat texture for the whole map, and since library order 9 (Route 2)
the terrain is drawn with its own `TerrainSplatShaderProgram`. Other models
use the default shader. What's left:
- confirm the Route 2 run visually (run 4 below crashed on minimize, fixed
  as `VulkanChanges.md` #23);
- remove the shader's non-terrain branch, the metallic -1 marker and
  `InstallAsDefault`;
- textures still can't be updated or freed (#20), so each paint stroke uses
  up a texture place.

**Confirmed on screen (2026-10-19), in TerrainEditor:** rock on the slopes,
and paint under the brush. Two old bugs had hidden splatting until then:
- the splat map was mirrored along z;
- PixelMap's default blending wiped every pixel with sand weight 0.

DriveOpen uses the same code, so it should now show splatting properly too.

The user has moved on to Tier 4 (`TierFour.md`). Its paint brush builds on
`TerrainData:PaintSplat` and `WorldTerrain:UpdateSplatTexture`.

## Status (2026-10-17, with later runs added)

### Written

| File | What |
|---|---|
| `Worlds/TerrainRegion.quorum` | Splat weights per sample (`Integer32BitArray splats`, packed RGBA like a PixelMap pixel), all red to start. `GetSplat`, `SetSplat`, `FillSplat`, `GetSplatImage`, `SetSplatFromImage` (laid out like the height image: first `SetPixel` number is z). |
| `Worlds/TerrainData.quorum` | `PackSplat`, `GetSplatWeight`, `GetSampleSplat`, `SetSampleSplat`, `ImportSplatMap`, `GenerateSplatBySlope` (smoothstep between two angles, like Terrain3D's auto shader), `PaintSplat` (moves weights toward one layer, full at the center, smooth falloff). A separate **splat edited area**, so splat changes don't rebuild meshes. Saving writes `region_x_z_splat.png` beside each height image; `LoadRegion` reads it if it exists. |
| `Worlds/TerrainSplatFragmentShader.quorum` | The default 3D fragment shader plus a terrain branch for materials with metallic < 0: weights from the texture override (the region's splat texture), the four layer textures' places from the color override's bytes, layer coordinates = world x and z / `LAYER_REPEAT`. Writes the same 4 G-buffer outputs. Plain green until the layers are ready. |
| `Worlds/TerrainSplatShaderProgram.quorum` | Default 3D vertex shader, the splat fragment shader, `DefaultShaderMapping3DVulkan`. `InstallAsDefault(layerRepeat)` compiles it under `"Default 3D Shader Program"` (Route 1), and returns whether it took. `InstallAsProgram` compiles it under its own name; since 2026-10-18 DriveOpen uses this (Route 2), and `WorldTerrain:SetSplatProgram` gives it to the terrain's models. |
| `Worlds/TerrainRegionModel.quorum`, `TerrainRegionFarModel.quorum` | `SetUsesSplat`. With it on, texture coordinates are `(sample + 0.5) / (regionSize + 1)` (the vertex's own pixel in the splat texture), and the material gets metallic -1 and roughness 0.9. |
| `Worlds/WorldTerrain.quorum` | `SetTextureSplat(TextureSplat)` (red, green, blue, alpha layers; missing ones use red), `GetTextureSplat`, `AreLayersReady`. One splat texture per region, `(regionSize + 1)` square, reaching into the next regions, made by `BuildSplatTexture` (first `SetPixel` number is x here, to match texture coordinates). It's shared by the full and far models (`splatTextureMap`). `Update` checks when all four layer textures have their places, packs them into `layerColor` (each as `(index + 0.5) / 255`, since the packing truncates), and gives it to every terrain model. `Rebuild` also rebuilds the splat textures the splat edited area touches, and gives them to the region's models. With splatting, the far tint isn't shown. |
| `Tests/Worlds/TerrainSplatTest.quorum` | Packing, defaults, setting across regions, splat edited area separate from the edited area, the slope rule, painting. |
| `DriveOpen.quorum` | `useSplatting = true`: compiles the program first thing in `CreateGame` (with `InstallAsDefault` until 2026-10-18, now `InstallAsProgram`). Layers: grass (red), rock (green, copied to `Textures/rock.png`), dirt (blue) and sand (alpha), the last two made in code as speckled 64-by-64 textures. First coat: grass and rock by slope (12 to 22 degrees), sand below height 8, dirt on the stack pads. **P** paints the current layer ahead (strength 0.5, `editRadius`), **C** cycles grass, rock, dirt, sand. `layerRepeat = 6`. |

### Run 1 of the prototype

- 16 of 16 tests pass. `Splat shader installed ...: true`, and the splat
  textures became ready. The user sees some kind of splatting, **but the car
  is no longer drawn.**
- Ruled out: the terrain marker. The car's glTF has `metallicFactor` 0, and
  the loader defaults to 0, so the car takes the default branch.
- **Suspected cause (to confirm):** texture groups.
  - `VulkanGraphics:GetMaxDescriptorsPerStage` sizes a texture group by the
    device's maximum *uniform buffers* per stage
    (`GetMaxPerStageDescriptorUniformBuffers`), not sampled images. On
    Apple GPUs through MoltenVK that can be about 31.
  - The prototype makes about 68 textures (64 regions' splat textures,
    rock, dirt, sand, and so on) before the car loads, so the car's
    textures, and some splat textures, land in later groups.
  - The multi-group path was never used before, and has at least one
    known bug (`VulkanChanges.md` #13: the texture override writes the
    global index, which is wrong outside group 0). The splat shader also
    uses global indices.
- **Diagnostic added:** DriveOpen's `ReportTextureGroups` prints, once:
  - the textures per group;
  - the ground layers' indices and groups;
  - one splat texture's index and group;
  - the car materials' groups.
- **If confirmed, the fix without a library change:** fewer textures. One
  splat texture for the whole map (512 by 512 in DriveOpen) instead of one
  per region, so the terrain needs 5 textures in all, and load the car
  before the terrain. The proper fixes are library changes: the right
  limit, and #13.

### Run 2: texture groups confirmed, design changed

- `Texture check: 31 textures per group.` Ground layers at indices 1 to 4,
  the first region's splat texture at 5 (group 0), **the car's 3 materials
  in group 4.**
- Confirmed `VulkanChanges.md` #19: groups of 31, sized by the wrong limit.
  The multi-group path doesn't draw the car at all, and splat textures past
  group 0 don't show properly (#13). Also found #20: reloading a texture
  always takes a new place, and places are never reused.
- **New design (no library change):**
  - **One splat texture for the whole terrain**, not one per region, so
    the terrain uses 5 textures and everything stays in group 0.
  - `WorldTerrain:PrepareSplatTexture` covers every region location that
    has a region (loaded or saved), plus one sample on the far sides: 513
    by 513 in DriveOpen. Saved regions are loaded just long enough to read.
  - A copy of the pixels is kept (`splatPixels`), so painting updates only
    the changed pixels before making the new texture
    (`UpdateSplatTexture`).
  - Models get `SetSplatArea(startX, startZ, width, depth)`, and their
    texture coordinates are `(sample - start + 0.5) / width`.
  - `GetSplatTexture`, `GetSplatTextureCount`.
- **Painting:** P changes only the weights. The texture is made again once,
  when P is let go (`FinishPainting`). Each stroke uses one texture place
  (#20), so after about 25 strokes painting stops showing properly. A
  warning prints from 20.
- Regions added outside the splat texture's area later aren't covered.

### Run 3 (2026-10-18, with library order 7)

- `Texture check`: ground layers at indices 1 to 4, the splat texture at 5
  (group 0), **the car's materials in group 2.** The car shows, since groups
  0 to 2 are drawn (see `VulkanChanges.md` #22: groups 3 and up never are).
  So more textures than expected are still made before the car; not looked
  into yet.
- Splatting showed and editing worked; no errors.

### Run 4 (2026-10-18, Route 2: library order 9)

- `Splat shader compiled for the terrain's models: true`, and `Splat
  textures are ready`.
- Crashed after the window was minimized and restored
  (`BufferOverflowException` in a draw queue). That was an older painter bug,
  now fixed (`VulkanChanges.md` #23, library order 12). **Waiting for a
  rerun** to confirm Route 2 looks the same as Route 1.

### To check

1. `RunWorldTests`: expected `Worlds Tests: 16 of 16 passed.`
2. `DriveOpen` console:
   - `Splat shader compiled for the terrain's models: true` (Route 2; with
     Route 1 this was `Splat shader installed as the default 3D program`).
   - Then, after a frame or two: `Splat textures are ready; the ground is
     now splatted.`
3. Look for:
   - grass on flat ground, rock on slopes, sand in valleys, dirt under the
     box stacks;
   - smooth blends between them, with no seams at region edges;
   - **orientation:** dirt should be centered under the stacks. If it's
     mirrored or swapped along x and z, the splat texture's layout or the
     texture coordinates are flipped. **Found 2026-10-19: it was mirrored
     along z** (MeshVulkan flips v), fixed in `MakeSplatTexture`. **Also
     found: PixelMap's default blending wiped every splat pixel with alpha
     (sand) 0,** in the texture and in saved splat images, so splatting
     never really showed. Fixed by turning blending off while writing;
   - the car, boxes and balls still look exactly as before (since Route 2
     they use the default program itself);
   - shadows and lighting on the ground as before.
4. **P** paints dirt ahead; **C** changes the layer (printed); painted
   areas survive their region unloading and loading again.
5. Expected but not a bug: textures shimmer in the distance (no mipmaps);
   each paint stroke makes new splat textures, which leak until textures
   can be freed.

## Goal

Draw the terrain with several ground textures (for example grass, rock,
dirt, sand), blended smoothly by a splat map: each point on the ground says
how much of each texture to show.

This uses the standard library's existing `TextureSplat` class, which
already holds four layer textures (red, green, blue, alpha) and a splat map,
but which nothing draws today. It's the classic four-layer splat map, not
yet Terrain3D's 32-texture index map. That can come later (see "Later").

## The one thing the library is missing

- Every 3D model is drawn with one default shader program.
  `Model:SetShaderProgram` exists, but `VulkanPainter3D` ignores it ("TO-DO:
  Support custom shaders for 3D", `VulkanPainter3D.quorum:1098`).
- So the terrain needs its own fragment shader, and there's no supported
  way to give it one.
- **Everything else can be done in this project.** The shader itself, the
  splat data, the textures, and passing data to the shader all work with
  what the library has.

## Two ways to get the terrain its own shader

### Route 1: no library change (a prototype)

- `ShaderManager:GetDefaultShaderProgram3D` first looks for a program
  registered under the name `"Default 3D Shader Program"`, and compiling a
  program registers it under its name.
- So a program the project compiles under that name in `CreateGame`, before
  the first frame, becomes the program **every** model is drawn with.
- Its fragment shader would be the default one, plus a branch: models whose
  material has a marker (metallic set to -1, a value no real material uses)
  are drawn splatted; everything else is drawn exactly as before.
- **Good:** no library change at all, so the shader can be developed and
  checked right away.
- **Bad:** it takes over the default shader for the whole game. It's a
  trick, not something to keep, and it would break if the default fragment
  shader changed.

### Route 2: one library change (the proper way)

- `VulkanPainter3D` draws a model with its own shader program when it has
  one, and the default otherwise.
- That's item #6 in `VulkanChanges.md` (L1 in the Tier 2 plan): about 40-60
  lines in `VulkanPainter3D.quorum` and `VulkanPainter3DBatch.quorum`, no
  Java.
- Draw queues split per program. Custom programs must use the default
  layout. Shadows and lighting are unchanged.
- Then only terrain models get `SetShaderProgram(terrainProgram)`, and no
  marker is needed.
- To be shown as an exact before and after, and approved, before it's made.

### Recommendation

Develop the shader with **Route 1**, so it can be seen working before any
library change is reviewed. Then make the **Route 2** change, and switch
over by deleting the default-name trick and the marker branch. The shader
code carries over unchanged.

## How data gets to the shader (both routes)

The per-model data the renderer already sends to the shader is full, but
two slots can be used:

- **The splat map goes in the model's texture override.** Each region gets
  its own small splat texture, set with `GetResources():SetTexture`, the way
  the grass is shared today. The shader reads it as `v_textureOverrideIndex`.
- **The four layer textures' indices go in the model's color override.**
  - The color override is 4 bytes, read in the shader with
    `unpackUnorm4x8`. The shader reads them back as four whole numbers: the
    red, green, blue and alpha layers' places in the global texture array.
  - Textures only get their place once the renderer has processed them,
    during a frame. So `WorldTerrain` fills in the color override once all
    four layers have one.
  - Until then the region is drawn with plain grass.
  - **Limit:** each index must be under 256, and in the first texture group.
    That's true for this project; it's noted for later.
- **Layer texture coordinates come from the world position.** The fragment
  shader already receives each pixel's world position (`v_position`), so
  layers tile by world x and z and line up across regions, with no change to
  the vertex shader.
- **Splat map coordinates come from the mesh.** Region meshes' texture
  coordinates change from world-based to "place in the region's splat map",
  so each vertex lands on its own splat texel. Linear filtering blends
  between them.
- How much a layer repeats (world units per tile) is written into the
  shader's code as a constant when it's built, and can be changed by
  rebuilding it.

## Plan

Each step ends with tests or a DriveOpen run, then an update to this file.

1. **Splat data (no library change, headless tests).**
   - `TerrainRegion` gets splat weights: one RGBA value per sample, packed
     in an `Integer32BitArray`. New regions are all layer one (red).
   - `TerrainData`:
     - `GetSplat` and `SetSplat` by sample and by world position;
     - `ImportSplatMap(PixelMap, startX, startZ)`, laid out like
       `ImportHeightMap`;
     - `GenerateSplatBySlope`, an automatic first coat: one layer on flat
       ground, another on slopes, like Terrain3D's auto shader.
   - Saving and loading: a second image per region, `region_x_z_splat.png`.
     Exact, since the weights are already 8 bits each. `terrain.json` gains
     a `"splat": true` field.
   - Tests: packing, setting, and the slope rule.
2. **Splat textures (no library change).** `WorldTerrain` makes one splat
   texture per region from its weights:
   - a `PixelMap`, then `Texture:LoadFromPixelMap`;
   - covering the region plus its one-sample overlap;
   - shared by the region's full model and its far model.

   `WorldTerrain:SetTextureSplat(TextureSplat)` takes the four layer
   textures, using the library's existing class. Region and far meshes
   switch to splat-map texture coordinates.
3. **The splat shader (no library change).**
   - `TerrainSplatFragmentShader`: the default fragment shader's inputs and
     its four G-buffer outputs, so lighting and shadows work as before.
   - It reads the splat map, reads the four layers at world position, and
     blends them by the weights (normalized so they add up to 1).
   - `TerrainSplatShaderProgram`: the default vertex shader with this
     fragment shader, and the default mapping
     (`DefaultShaderMapping3DVulkan`).
4. **Route 1 prototype in DriveOpen.**
   - The program is compiled under the default name, with the marker
     branch.
   - DriveOpen layers: grass, rock and dirt (from `TerrainSplatting`), plus
     a fourth.
   - The splat is generated by slope, with dirt painted on the stack pads.
   - The user runs it.
5. **Painting in DriveOpen.** A key paints a layer under the vehicle, and
   the region's splat texture is rebuilt.
   - Like mesh rebuilds, each rebuilt texture leaks until the library can
     free textures. Fine for trying it out.
6. **Route 2.** Show the painter change (#6) for approval, make it, switch
   the terrain to `SetShaderProgram`, and remove the Route 1 trick.
   **Done 2026-10-18, waiting for a run** (`Item9.md`). DriveOpen uses
   `InstallAsProgram` and `WorldTerrain:SetSplatProgram`. The shader's
   non-terrain branch and the metallic marker are still there; they come
   out once Route 2 is seen working.

## Not needed for splatting, but would help

- **Mipmaps (#7):** without them the ground layers shimmer in the distance.
  This was already true of the grass. A separate, medium library change.
- **Anisotropic filtering (#8):** sharper ground at low angles. Small.
- **Updating a texture in place:** so painting doesn't make a new texture
  each time. Not yet investigated.

## Later

- Height-based blending (Terrain3D's): each layer's alpha is a height, so
  stones show through grass in their gaps instead of fading evenly.
- Detiling: rotating each tile randomly, to hide repetition.
- More than four layers: Terrain3D's index map (base texture, overlay
  texture and blend per sample), from the Tier 2 plan.

## Open decisions for the user

1. ~~**Route 1 first, then Route 2?**~~ Done in that order.
2. ~~**Four layers with the `TextureSplat` class** to start?~~ Done with four
   layers. Terrain3D's index map is still open (Tier 2 goal 2).
3. **Ground textures:** `grass.png`, `rock.png` (128 by 128) and the splat
   maps from `TerrainSplatting`, origin and license unknown? Or another set,
   such as CC0 textures from ambientCG or Poly Haven?
