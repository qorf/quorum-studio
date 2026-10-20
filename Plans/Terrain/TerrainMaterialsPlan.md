# Terrain materials: using AmbientCG texture sets (plan)

**Status (2026-10-19): steps 1 to 3 are done.** Step 1 is `Item24.md` (`3e6fde7e0`). Step 2, the terrain, is in the project and confirmed. Step 3, mipmaps, is `Item25.md` (`768335de9`). Step 4 (per-set tint and roughness, detiling, slope projection, an index map) is later work.

Written 2026-10-19; revised the same day for the user's direction.
**Step 1 is applied** on `TerrainShader` (`Item24.md`, 2026-10-19) and
checked with `SourceCode/TextureSetCheck.quorum` (results in `Item24.md`).
**Step 2 (the terrain) is written, project only, waiting for a run**
(2026-10-19). What was built, against design section 5:
- `WorldTerrain:SetLayerMaterial(layer, Material)` per layer rather than
  `SetTerrainMaterials(Array)`, so texture set layers and plain
  `TextureSplat` layers mix: TerrainEditor and DriveOpen give grass
  Ground037 (tile 3) and rock Rocks002 (tile 2.5); dirt and sand stay
  speckled. Also `SetHeightBlendSoftness`, `HasLayerMaterials`,
  `GetLayerTable`.
- The layer table: pixel = (color place, normal place or 255 for plain,
  tile size in quarter units or 0 for the shader's repeat, 255). The color
  override = (table place, softness byte, 0, 255); alpha 255 tells the
  shader to use the table.
- The splat shader: for each layer with weight, `stand = height + weight`;
  each shows by how far it is above `highest - softness`, normalized. The
  frame is built from the mesh normal with x as tangent and -z as
  bitangent (a GL normal map's green points to the image top, smaller v).
  Plain layers are flat, at height 0.5.
- TerrainEditor: B steps the softness (0.05, 0.2, 0.5, 1).

The goal: splat the terrain with full materials (color, normal, roughness,
ambient occlusion, height) instead of one flat color texture per layer,
using the AmbientCG sets in `Textures/`, and modeled on Terrain3D.

**The user's direction (2026-10-19):**
1. **Vulkan only.** So much of this already depends on Vulkan that the
   OpenGL side is left alone. OpenGL ignores the new parts.
2. **No new terrain material class.** Quorum's existing `Material` is
   extended so it can describe a material **either the old way** (one
   texture, a normal map, a glTF metal-rough map) **or the new way** (a
   texture set: color, normal, roughness, AO, height). The terrain is then
   given ordinary `Material`s for its layers.
3. **Compare with Terrain3D,** to be sure the plan is reasonable (at the
   end).

These are library changes (`Material`, `MaterialVulkan`, the 3D shaders,
and one small Java helper). As with Items 7, 9 and 20, the exact code goes
in an `ItemN.md` for review before anything is changed. This file settles
the design.

## The assets

Three sets in `Textures/`, each at 1024 by 1024: `Ground037_1K-PNG`,
`Rocks002_1K-PNG`, `Snow013_1K-PNG`. Each has:

| File | Contents | PNG format |
|---|---|---|
| `_Color.png` | albedo (base color) | 8-bit RGB |
| `_NormalGL.png` | tangent-space normal, OpenGL convention (+Y) | 16-bit RGB |
| `_NormalDX.png` | the same, DirectX convention (-Y) | 16-bit RGB |
| `_Roughness.png` | roughness | 8-bit gray |
| `_AmbientOcclusion.png` | ambient occlusion | 8-bit gray |
| `_Displacement.png` | height | 16-bit gray |
| `.tres`, `.mtlx`, `.blend`, `.usdc` | the same material for Godot, MaterialX, Blender and USD | |
| `<name>.png` (512 by 512) | a preview picture, not a map | |

The Godot `.tres` shows the intended use: albedo from Color; roughness, AO
and height from the gray maps; NormalGL for the normal. Metallic is 0.

## How Quorum loads these images under Vulkan

Found in `PixelMap.java` (`LoadPixelMap`, `ConvertToRGBA8888`):

- Every image becomes 8-bit RGBA.
  - **16-bit PNGs** (normals, displacement) are cut to 8 bits. That's fine
    for normals, and enough for height used in blending.
  - **8-bit RGB** gets alpha 255.
  - **Gray images come out with the value in alpha and white in RGB:**
    (255, 255, 255, v). The loader treats one channel as "alpha only". So
    roughness, AO and displacement must be read from **alpha**.

  The plan reads them that way while packing, so the loader itself isn't
  changed and nothing else is affected.
- PixelMap blends when writing pixels unless told not to (`VulkanChanges.md`
  E). Packing must turn blending off, or work below PixelMap (in Java).
- There are no mipmaps (#7). With detailed 1024 textures tiled every few
  units, distant ground will shimmer. See "Library changes alongside".

## Quorum's materials today

- **`Models/Data/Material`** (what a model file's loader fills in):
  - diffuse color, texture path, normal map path;
  - a "metal-rough map" path, glTF style: roughness in green, metallic in
    blue;
  - roughness and metallic factors;
  - emissive, bump shading, opacity and refractive index, which Vulkan
    doesn't use.
- **`Models/Vulkan/MaterialVulkan`:**
  - `Preload` loads the three textures;
  - `Load` writes a 32-byte record: diffuse, texture index, has-normal,
    has-rough, normal index, rough index, roughness, metallic;
  - the record is reached from each instance through a buffer address.
- **`MaterialManagerVulkan:GetMaterial(Material)`** makes and queues
  `MaterialVulkan`s. Its cache is commented out.
- **`DefaultFragmentShader3DVulkan`** writes the G-buffer: color = texture ×
  diffuse; normal from the normal map (OpenGL convention); PBR = (0.5,
  roughness, metallic).
- **`DefaultLightingFragmentShaderVulkan`** reads roughness and metallic.
  **It never reads the first PBR channel (the 0.5),** and has no ambient
  occlusion: ambient light is `ambientColor × albedo × intensity`.
- The material record is also read by the two shadow fragment shaders. All
  three read only fields at its start, so **fields added at the end don't
  disturb them.**

## The design

### 1. `Material`: the old way or the new way (library, data only)

`Models/Data/Material` keeps everything it has (the old way), and gains an
optional texture set (the new way):

- `SetColorMapPath`, `SetRoughnessMapPath`, `SetAmbientOcclusionMapPath`,
  `SetHeightMapPath`.
- The existing `SetNormalMapPath` is reused, with a new
  `SetNormalMapConvention(GL or DX)`. The default is GL, matching today's
  shader.
- `SetTextureScale(number)`: how many world units one tile covers when the
  material is laid on terrain by world position. Models use their own
  texture coordinates and ignore it.
- `SetTextureSet(text folder, text name)`: a convenience that fills the
  paths by AmbientCG's (and Poly Haven's similar) file naming:
  `<name>_Color.png`, `_NormalGL.png`, `_Roughness.png`,
  `_AmbientOcclusion.png`, `_Displacement.png`. Missing files are left out.
- `UsesTextureSet` returns true when a color map is set the new way.

Nothing changes for code that only uses the old setters. OpenGL ignores the
new fields.

### 2. `MaterialVulkan`: pack the set into two textures (library, Vulkan)

When `UsesTextureSet` is true, `Preload` builds **two channel-packed
textures, Terrain3D's layout:**

| Texture | R G B | A |
|---|---|---|
| albedo-height | color | height (from the gray map's alpha); 0.5 if none |
| normal-rough | normal (GL; DX gets green flipped while packing), scaled by `sqrt(ao) × 0.5 + 0.5` to carry AO, Terrain3D's trick | roughness (from the gray map's alpha); the roughness factor if none |

- **The packing is done in Java,** by a new helper on `PixelMap`:
  `CopyChannel(PixelMap source, integer sourceChannel, integer
  destinationChannel)` and `SetChannel(integer channel, number value)`.
  Quorum loops would be millions of calls per set, which is seconds at
  startup. In Java it's milliseconds, and blending doesn't come into it.
  The helper is additive and general.
- The AO-into-normal-length step is in the same helper, or done as a last
  pass in Java.
- Missing maps get defaults: flat normal (128, 128, 255), middle height,
  AO 1.
- **The material record grows from 8 to 12 words,** with new fields at the
  end so the shadow shaders are unaffected:
  - **flags:** uses a texture set; normal is DX (kept for information,
    since flipping is done at packing);
  - **albedo-height place:** the existing texture index field also holds it,
    so old-style readers still get a color texture;
  - **normal-rough place;**
  - **texture scale** (float).
- Getters for the terrain: `GetAlbedoHeightTexture` and
  `GetNormalRoughnessTexture`, so their places can be read once loaded.

### 3. The default 3D shader learns the new way (library, Vulkan)

In `DefaultFragmentShader3DVulkan`, when the flag says "texture set":
- **color:** the albedo-height texture's RGB × diffuse;
- **normal:** the normal-rough texture's RGB, unpacked, normalized, and
  turned into world space with the vertex's tangents (as the normal map is
  now);
- **roughness:** its A; metallic stays the factor (0);
- **AO:** `length(normal) × 2 − 1`, written into the first PBR channel
  (today's unused 0.5). Old-style materials write 1.0 there.

So any model, not just terrain, can use an AmbientCG set: give the model a
`Material` filled with `SetTextureSet`.

### 4. Ambient occlusion in lighting (library, Vulkan, small)

`DefaultLightingFragmentShaderVulkan` multiplies the ambient term by the
first PBR channel. Old-style materials write 1.0, so they look exactly as
before. Direct light isn't darkened, which is how AO is meant to work.

### 5. The terrain's splat shader uses Materials (project)

- **`WorldTerrain:SetTerrainMaterials(Array<Material>)`,** up to four, one
  per splat layer, next to today's `SetTextureSplat` (kept for plain
  textures). Each `Material` is passed to
  `MaterialManagerVulkan:GetMaterial`, so the library loads and packs it.
  Once loaded, the terrain reads each layer's two texture places.
- **Passing eight places to the shader:** the color override holds only four
  bytes.
  - A **layer table texture** (4 by 1, RGBA, written with blending off):
    pixel i = layer i's albedo-height place, normal-rough place, texture
    scale, and a spare byte.
  - The color override carries the table's place plus settings such as
    blend sharpness.
  - The shader reads the table with `texelFetch`.
  - No library change is needed for this part.
  - Limit, as now: places must be under 256 and in texture group 0, so the
    sets load at startup, before labels.
- **The splat shader's terrain branch,** for each layer with a weight above
  a small cutoff:
  1. sample both textures at world position ÷ the layer's texture scale;
  2. **height blend:** `w' = (w + height)^sharpness × w`, then normalize,
     as Terrain3D does;
  3. accumulate albedo, normal, roughness and AO.

  Then it writes the G-buffer like the default shader: the normal in world
  space through the vertex's tangents (the region meshes already have
  tangents along x and bitangents along z), roughness in PBR green, AO in
  PBR red.
- Splat weights, painting, saving, undo, the editor and auto paint are
  unchanged: still four weights per sample. TerrainEditor and DriveOpen pick
  the sets: for example red Ground037, green Rocks002, alpha Snow013, and a
  fourth to choose.

### What stays the old way

- OpenGL: untouched.
- `Material`s without a texture set: same record values, same shader path,
  plus AO = 1.
- The terrain with `SetTextureSplat`: as today.

## Library changes alongside (separate items)

- **Mipmaps (#7):** strongly recommended right after the shader works.
  Without them, detailed textures shimmer at a distance, and Terrain3D's
  look depends on them (Godot gives it mipmaps).
- **Anisotropic filtering (#8):** sharper ground at low angles; Terrain3D
  relies on Godot's.

## Compared with Terrain3D

| Topic | Terrain3D | This plan | Verdict |
|---|---|---|---|
| Texture set contents | albedo, normal, roughness, AO, height | the same | Same |
| Packing | 2 textures: albedo+height, normal+roughness, AO folded into normal length | the same layout and the same AO trick, packed on load in Java | Same; we pack at load instead of with an editor tool, so there's no extra file step |
| Normal convention | expects OpenGL; its tool flips DX | per material, GL or DX, flipped at packing | Same idea; we have it per material |
| Where sets live | `Texture2DArray`s (all albedo one size, all normal one size) | separate textures in Quorum's bindless array, found by index through a layer table | Equivalent for 4 layers. Arrays scale better past ~30 textures (a later library item) |
| How many per terrain | 32, through an index (control) map: 2 IDs and a blend per sample | 4, through RGBA splat weights | **Fewer.** Fine for test worlds; Terrain3D's index map is Tier 2 goal 2, later |
| Blending | height blending with a sharpness, bilinear across 4 samples by hand | height blending with the same formula; bilinear by the GPU's filter on the splat texture | Same look for 4 layers. Terrain3D blends by hand because its index map can't be filtered |
| Samples per pixel | 5 to 20 | about 10 (splat, table, 2 per active layer) | Comparable |
| Texture coordinates | world position × per-set scale | the same | Same |
| Per-set settings | scale, tint, roughness adjustment, AO strength, normal depth, detiling | scale (and tint through diffuse) now; the rest are shader additions later | **Fewer settings**, easily added |
| Detiling, slope projection, macro variation | yes | later, shader-only | Later |
| Mipmaps, anisotropy | from Godot | none today (#7, #8) | **Gap:** do #7 next |
| Compression (BC) | VRAM compressed | uncompressed RGBA8 | Gap. About 32 MB for 4 sets of 1024; fine at our scale |
| Ambient occlusion in lighting | Godot's lighting applies AO to ambient | the lighting change in step 4 | Same, once step 4 is in |
| Material on non-terrain models | n/a (terrain only) | the same `Material` works on any model | We get more: any model can use a texture set |
| Displacement | optional, by tessellation (its own displacement buffer) | none (height used for blending only) | Gap; large, not planned |

**Is it reasonable?** Yes, for a four-layer terrain.
- It follows Terrain3D on everything that makes its ground look good: the
  same packing, height blending, tangent-space normals with world-position
  coordinates, roughness and AO.
- The real differences are scale (4 sets against 32) and the missing
  mipmaps. Mipmaps (#7) should follow right after.
- The index map, which is what scales Terrain3D to 32 sets, is a later step
  this design doesn't block: the same `Material`s and packed textures would
  feed it.

## Suggested order

1. **`Item` proposal** with exact code:
   - `Material`'s new fields and `SetTextureSet`;
   - the `PixelMap` channel helper (Java);
   - `MaterialVulkan` packing and the 12-word record;
   - the default 3D shader's new path;
   - AO in lighting.

   Test with a model first: a box using `SetTextureSet` on Rocks002, beside
   a plain box, under a moving light.
2. **The terrain (project):** `SetTerrainMaterials`, the layer table, and
   the splat shader with height blending.
3. **Library #7 mipmaps,** as its own reviewed item.
4. Later: per-set tint and roughness adjustments, detiling, slope
   projection, and the index map for more sets.

## Open decisions for the user

1. **Packing in Java** with a small additive `PixelMap` helper, or in Quorum
   (no Java change, but slow at startup)?
2. **Which normal map to use by default:** GL (Terrain3D's choice) with a DX
   option? It needs a lit test either way, since the world is left-handed
   and `MeshVulkan` flips v.
3. **The fourth layer:** keep the speckled dirt as an old-style `Material`
   (color only), or add another AmbientCG set?
4. **Mipmaps (#7)** right after step 2, as recommended?
