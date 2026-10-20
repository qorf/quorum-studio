# Open Worlds: starting position

Written 2026-10-17. This records where Quorum's terrain stood, what
Terrain3D does, and what could be adapted from it. Work in progress is
tracked in `OpenWorldsHandoff.md`.

## Status of the tiers (2026-10-18; see the 2026-10-19 comparison below)

| Tier | Status | Where |
|---|---|---|
| 1. Fill the gaps | Done | `OpenWorldTier1Handoff.md` |
| 2. Texturing | Goal 1 (blended layers) works, through splatting. The rest is paused. | `TextureSplattingPlan.md`, `OpenWorldTier2Handoff.md` |
| 3. Open worlds | Goals 1, 3 done (regions, streaming, collision near the player), plus coarse distant terrain. The clipmap and background terrain are not started; 64-bit coordinates deferred. | `OpenWorldTier3Handoff.md` |
| 4. Editing tools | **Next.** Plan written, waiting for review. | `TierFour.md` |
| 5. Filling the world | Not started | |

Library changes made along the way are in `LibraryChangesPlan.md`
(`Item7.md`, `Item9.md`). The end goal is compared in
`GhostReconComparison.md`.

## Goal

Change Quorum's graphics engine so it can build and edit open 3D worlds.
The work goes in two places:

- **Standard library**, `quorum-language/Quorum/Library/Standard`: terrain,
  world data and brush operations.
- **Quorum Studio**, `quorum-studio/QuorumStudio`: brushing tools for
  scenes.

## The reference: Terrain3D

`/Users/stefik/Repositories/Terrain3D`, MIT license (checked). It is a C++
terrain system for Godot 4, about 11,400 lines in `src/` plus GLSL shaders
in `src/shaders/`. It is used as a guide, the way Bullet was for physics.

Main files:

| File | What it does |
|---|---|
| `terrain_3d.cpp` | The terrain node; ties everything together |
| `terrain_3d_data.cpp` | Regions, maps, and queries such as height and normal |
| `terrain_3d_region.cpp` | One region's height, control and color maps |
| `terrain_3d_editor.cpp` | Brush tools, operations, undo and redo |
| `terrain_3d_mesher.cpp` | The camera-following clipmap mesh |
| `terrain_3d_material.cpp` and `shaders/` | Texturing and the terrain shader |
| `terrain_3d_collision.cpp` | Heightfield collision per region |
| `terrain_3d_instancer.cpp`, `terrain_3d_mesh_asset.cpp` | Foliage and prop instancing |
| `doc/docs/` | Design notes: `system_architecture.md`, `controlmap_format.md`, `shader_design.md` and others |

## What Quorum has now

- **`Terrain`** (`Libraries/Game/Graphics/Worlds/Terrain.quorum`) is a
  `Model` built from one `PixelMap`. Each pixel becomes one vertex.
  - Height is stored at 16 bits across the red and green channels, times
    `stickHeight`.
  - The mesh is built on the CPU once. It has one plain white material and
    no levels of detail.
  - There is no way to change heights after loading, and no way to save.
  - `GetRenderBounds` supports frustum culling.
- **Gaps found while reading:**
  - Every texture coordinate is set to `(0,0)`, so no texture can show.
  - `stickWidth` and `stickDepth` are stored but never used when building
    the mesh. Vertices are always one unit apart.
- **`TextureSplat`** (`Worlds/TextureSplat.quorum`) only stores five
  textures (red, green, blue, alpha and the splat map). Nothing in the
  renderer reads it, so splatting is not drawn. The `TerrainSplatting` test
  sets it up, but it has no effect.
- **`HeightmapTerrainShape`** (`Libraries/Game/Collision/Shapes/`) is a
  Bullet-style concave collision shape. It finds triangles by walking the
  height grid, and has a ray test.
- **`NoiseGenerator` and `PerlinNoise`** (`Libraries/Compute/Noise/`) give
  layered, ridged, warped and slope-eroded noise. This is already a good
  base for generating terrain.

## Comparison

| Area | Terrain3D | Quorum |
|---|---|---|
| Drawing the mesh | A flat clipmap grid with up to 10 built-in detail levels follows the camera. The vertex shader raises each vertex by reading the height texture. | One static mesh built on the CPU |
| World size | Sparse regions (64 to 2048 pixels each), up to 65 km across. Space between regions can be empty, flat or noise. | One image |
| Stored data per region | Height map (32-bit float); control map (32 bits per pixel: base and overlay texture IDs, blend, texture angle and scale, hole, navigation and auto-shader flags); color map with roughness in alpha | Height only |
| Texturing | 32 textures in arrays. Base plus overlay per pixel, height-based blending, detiling, slope-based auto shader, large-scale variation, two texture scales, cliff projection, optional tessellation | None drawn yet |
| Editing | Tools: region, sculpt, height, texture, color, roughness, auto shader, holes, navigation, instancer. Operations: add, subtract, replace, average, gradient. Brush: image shape, size, strength, gamma, spin, align to view, slope filter, auto-create regions. Undo and redo by region copies. | None |
| Collision | Heightfield shapes per region, built near the camera ("dynamic") or everywhere ("full") | One concave shape over everything |
| Queries | Height, normal, slope test and texture at a world position. Mouse picking from GPU depth. | Triangle lookup for collision only |
| Foliage | Instances stored per region, up to 10 detail levels, shadow impostors, density, random scale and rotation, generated grass cards | None |
| Input and output | Imports and exports EXR, R16 and PNG heightmaps; one file per region | Loads a PNG |
| Other | Navigation data, debug views, vertex spacing, 64-bit coordinates | None |

## What Terrain3D has that the port doesn't yet (2026-10-19)

Compared again after Tiers 1, 3 and 4 (stages 1 to 3), texture sets, and
the Vulkan library work. Sources: Terrain3D's `src/shaders/` modules and
`doc/docs/`.

**The port already covers, sometimes by another route:**
- regions, saved one per file, with streaming;
- height, normal and slope queries;
- collision near the player ("dynamic") or everywhere ("full");
- **editing:** a brush engine, sculpt operations, undo and redo, picking, a
  brush preview, presets, stamps, erosion and auto paint (slope);
- **texturing:** four splat layers with texture sets and height blending;
  mipmaps and anisotropy;
- **distant terrain:** low-detail far models, where Terrain3D uses a
  clipmap;
- **picking:** done on the CPU, where Terrain3D reads a GPU depth render.

**Not in the port:**

| Area | Terrain3D feature | Notes |
|---|---|---|
| Texturing | Index (control) map: 32 texture sets, base and overlay per pixel | The port has 4 layers |
| | Per-pixel texture angle and scale painting | Stored in the control map |
| | Detiling, to break up visible repeats | Shader |
| | Macro variation: large-scale color noise | Shader; small |
| | Dual scaling: one texture at two scales, near and far | Shader; small |
| | Cliff projection: triplanar on steep slopes | Shader |
| Maps | Color map: paintable tint (GIS imports, variation) | A per-region map |
| | Roughness and wetness painting | A per-region map |
| | Holes, for caves, tunnels and buildings | Mesh, collision and shader |
| Mesh | GPU clipmap with levels of detail | Not started (Tier 3). Far models stand in |
| | Displacement: tessellation from texture heights near the camera | Large; needs a clipmap or tessellation |
| | Background terrain (flat or noise) beyond the edited regions | Not done (Tier 3 goal 4) |
| World | 64-bit coordinates | Deferred (floating origin, Tier 3 step 3) |
| Filling the world | Instancer: foliage and props per region, levels of detail, shadow impostors, placement rules | Tier 5. Needs instancing at scale (#18) first |
| | Grass particles; wind and player interaction for foliage | Tier 5 |
| Game support | Navigation mesh baking from the terrain | Quorum has no navigation system yet |
| | Occlusion culling (baked occluders) | Quorum has frustum culling only |
| Import and export | EXR and R16 heightmaps | The port uses PNG and 16-bit heights |
| Tools | Debug views and overlays: region grid, vertex grid, contour lines, height, control-map, PBR and displacement views | Mostly shader switches |

The work to close these gaps, with everything else still open, is
numbered in `OpenWorldsHandoff.md`, "Priority list (Items 39 on)".

## Features that could be added

**Tier 1: fill the gaps in what exists (CPU work, no renderer changes)**

1. Real texture coordinates; use `stickWidth` and `stickDepth` as vertex
   spacing.
2. Queries by world position: `GetHeight(x, z)`, `GetNormal(x, z)`,
   `IsInSlope`.
3. Changing heights at runtime (`SetHeight`), updating only the changed
   part of the mesh and collision.
4. Saving and loading terrain, and 16-bit and float heightmap formats
   beyond PNG.
5. Building terrain straight from a `NoiseGenerator`.

**Tier 2: texturing**

1. Make `TextureSplat` render. This needs a terrain shader in the Vulkan
   pipeline.
2. Replace the four-channel splat with Terrain3D's control map: two
   texture IDs per pixel, a blend amount, and up to 32 textures in an array.
3. Slope-based auto shader, height-based blending, detiling, large-scale
   variation, two texture scales.
4. Paintable color and roughness maps.
5. Holes, for caves, tunnels and buildings.

**Tier 3: open worlds**

1. A world of sparse regions, each loaded and saved on its own.
2. The camera-following GPU clipmap mesh in place of the static mesh. This
   is the largest renderer change.
3. Collision built only near the camera, per region.
4. Background terrain (flat or noise) beyond the edited regions.
5. Optional 64-bit world coordinates.

**Tier 4: editing tools (Studio)**

1. A brush system in the standard library: tool plus operation, with size,
   strength, gamma, image shape, spin and slope filter.
2. Sculpt operations: raise, lower, smooth (average), flatten (replace),
   ramp (gradient).
3. Painting textures, colors, roughness, holes and navigation.
4. Undo and redo with region copies, fitting Studio's undo model.
5. Mouse picking on the terrain, with a brush preview drawn on the surface.

**Tier 5: filling the world**

1. Brush-placed foliage and props, stored per region, with detail levels.
2. Placement rules: density, height offset, random scale and rotation,
   slope limits.
3. Generated grass cards and shadow impostors.
4. Navigation data from the terrain.

## Suggested order

Tiers 1 and 2 give a large visible payoff at low risk and don't wait on a
renderer redesign. The Tier 4 brushes can be built on the editable CPU mesh
from Tier 1 and moved to the GPU mesh later. Tier 3 is a rendering project
that needs its own design.

## Open question before Tier 2 and Tier 3

Can Quorum's Vulkan renderer support:

- reading textures in the vertex shader;
- texture arrays;
- unsigned integer textures (for the control map);
- a custom shader and material for terrain?

Checked 2026-10-17. The answers are in `OpenWorldsHandoff.md` under
"Renderer check". Since then, a custom shader for terrain works (library
order 9, 2026-10-18).
