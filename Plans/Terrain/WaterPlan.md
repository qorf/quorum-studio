# Water effects (plan)

Written 2026-10-19. **Phases 1, 2, 3 and 5 are written, for review, not
applied:** `Item38.md` (time and camera data), `Item39.md` (the water pass
and `WaterSurface`), `Item41.md` (sky reflection) and `Item43.md` (sea level
and lakes), with fog as `Item40.md`. OpenGL water is Item 68, the extras
Item 66. Nothing is changed in the library yet.

**Where the Items differ from this plan** (the Items are what to apply):
- **No separate render pass:** forward programs draw inside the lighting
  render pass, after its full-screen draw. The lit image is already its
  attachment and the G-buffer is already readable there.
- **The water's settings are per program,** in push constants (18
  numbers), not per model: one `WaterShaderProgram` per look.
- **The water classes are project code** in `Libraries.Game.Graphics.Worlds`
  for now (decision 5), moved with the rest of Worlds in Item 71.
- **Sets:** 0 camera and sampler, 1 textures, 2 G-buffer, 3 lights and
  scene data, 4 sky cube (41). Once the decisions
at the end are settled, the work goes in Items with the exact code.

**The goal:** lakes and seas on the open-world terrain that look like water:
- moving ripples;
- the sky reflected at low angles;
- clear in the shallows and darker in the deep;
- a soft edge where water meets the shore;
- sun glints.

Later: refraction, waves that move the surface, foam, rivers.

Paths: **G/** = `quorum-language/Quorum/Library/Standard/Libraries/Game/Graphics/`,
**P** = `G/Vulkan/VulkanPainter3D.quorum`.

## What the renderer can do today (summary of the investigation)

**There is no water code anywhere.** `GhostReconComparison.md` lists water
as "new; needs library work, large".

**Transparency doesn't really exist in the Vulkan 3D renderer.**
- "Transparent" models (`Model:HasTransparency`, set by a color with alpha
  below 1) go into the same G-buffer draw queues as opaque ones, just
  later. There's no sorting and no separate pass.
- Only the albedo attachment blends. Position, normal, material and depth
  are overwritten.
- So a half-transparent water plane is lit as if it were solid. Only its
  color mixes with the ground under it.
- It can't show depth, refraction or a shore fade.

**The scene's depth image is discarded after the scene pass.** It is
attachment-only, with `STORE_OP_DONT_CARE`, and transient under MSAA. But
the world-position G-buffer image (RGBA32F) survives into the result pass,
and works as depth: it gives the distance to whatever is behind the water.

**No time and no camera position reach 3D shaders.**
- The camera uniform holds only `projectionViewMatrix`.
- The push constant is only the instance buffer address.
- Materials are uploaded once.
- Moving ripples need time; reflections and fresnel need the camera
  position.

**The sky cube can only be sampled by the skybox pipeline,** through its
own descriptor set (`GetSkyboxDescriptor`, a `samplerCube` at set 0
binding 0). Any other pass can bind the same set.

**The lighting pass** has Cook-Torrance specular with Schlick fresnel,
but no environment reflection and no fog (#17).

**OpenGL 3D is a forward renderer** that sorts and blends transparent
objects back to front, with the skybox drawn before them. Water there is
simpler: a shader with blending.

**Animating vertices on the CPU** works today (`MeshVulkan:UpdateVertices`,
Item 7), but for every model sharing the blueprint.

## The design (recommended): a water pass after lighting

Water is drawn **after** the deferred lighting, into the lit image, with
blending, in its own small pass. This is how deferred renderers usually
draw water and glass. It keeps water out of the G-buffer, so lighting,
shadows and MSAA for everything else are unchanged.

**The new frame order:**
1. scene (G-buffer);
2. shadows;
3. lighting into the lit image;
4. **water into the lit image (new);**
5. result (sky, then the lit image, onto the screen).

### The water pass (Vulkan)

**One render pass:**
- **Color attachment:** the lit image (load, store).
- **No depth attachment.**
  - **What the fragment shader samples:** the position G-buffer at the
    pixel, which is what is behind the water.
  - **When it discards:** if the ground is closer than the water.
  - This works with or without MSAA, because the G-buffer images are
    resolved by then.
  - Water rarely overlaps itself, so not sorting water against water is
    acceptable for lakes and seas.

**Per pixel, the shader works out:**
1. **Thickness:** the distance from the water surface to the ground behind
   it, along the view ray.
2. **Color:** a mix of the shallow and deep colors by
   `1 - exp(-thickness × clarity)`. Opacity rises the same way.
3. **Shore fade:** opacity goes to 0 where the thickness goes to 0, so the
   edge is soft rather than a hard line on the terrain.
4. **Normal:** two scrolling normal-map layers, at different scales,
   directions and speeds, from time. Optionally a third, very large, for
   swell.
5. **Reflection:** the sky cube sampled along the reflected view vector,
   with the skybox's rotation and mirror applied, so it matches the sky.
   Weighted by Schlick fresnel: little looking down, nearly a mirror at a
   low angle.
6. **Sun glint:** GGX specular from the first directional light, read from
   the existing light buffer (set 1), with low roughness.
7. **Blending:** over the lit image, with standard alpha.

**What the pass binds:**
- **Set 0:** the sky cube (the skybox's set and layout). With no skybox, a
  flat color from the ambient light.
- **Set 1:** light data and scene data (the lighting pass's set 1). It
  already holds the camera position.
- **Set 2:** the position G-buffer sampler. This is a new small set,
  linked like the lighting sets and kept across resizes, as Item 32 does.
- **Push constants:** the instance buffer address (as the scene pass), and
  time.

**Shadows on water:** not in the first version. Water is lit by the sun
without its shadow, which is hard to notice on a moving surface. Sampling
the sun's cascades in this pass is a later step.

**MSAA:** the lit image is single-sample, so water edges against the sky
or terrain aren't anti-aliased. The shore fade hides most of that.

### The water objects: `G/Worlds/WaterSurface.quorum` (library)

**`class WaterSurface is Model`:** a flat, finely divided plane, drawn by the
water pass instead of the G-buffer.

**Actions:**
- `Load(number width, number depth, integer divisions)`;
- `SetShallowColor(Color)` and `SetDeepColor(Color)`;
- `SetClarity(number)`: how quickly it darkens with depth;
- `SetShoreFade(number)`: the thickness over which the edge fades;
- `SetWaveScale(number)`, `SetWaveSpeed(number)` and
  `SetWaveDirection(number x, number z)`;
- `SetNormalMaps(Texture a, Texture b)`. A default pair ships with the
  library.
- `SetReflectionStrength(number)`.

**How the painter routes it:** `VulkanPainter3D:Render` sends a model
whose shader program is marked as a water or forward program
(`ShaderProgram:IsForward`, decision 1) to a separate list. That list is
drawn by the water pass, not the scene pass. It never casts shadows.

**Its data:** the per-model water settings (colors, clarity, wave values)
go in a small per-model uniform. It is indexed through the reserved words
of the instance or material data, the same way the terrain reads its
layer table, so no new descriptor is needed per model.

### What other 3D shaders gain: time and camera position

Every 3D shader gets time and the camera position, by growing the camera
uniform (set 0 binding 0) from 64 to 96 bytes:
- `projectionViewMatrix`;
- `cameraPosition` and `time`;
- spare.

It is visible to the vertex and fragment stages.
- **Who sets them:** the painter, every frame.
- **Time:** seconds since the game started, wrapped every hour, so shaders
  keep precision.
- **What uses them:**
  - custom shaders, such as animated grass and wind (later);
  - the terrain, which can use the camera position for distance-based
    detail.
- **Changes to existing code:** `AddDefaultInfo3D` (stage flags) and the
  camera buffer size and fill (P). Existing shaders keep reading the
  matrix at offset 0 and are unaffected.

### OpenGL (decision 4)

The forward renderer already blends sorted transparent objects. A
`WaterSurface` there uses a GLSL water shader with:
- the scrolling normals;
- fresnel against a flat sky color, or the cube if bound;
- the sun glint;
- alpha from a fixed clarity.

There is no depth tint or shore fade. Depth isn't available to that
shader without a depth texture, which OpenGL here doesn't make.

### Later steps (not in the first version)

- **Refraction:** copy the lit image before the water pass (one more
  screen-sized RGBA16F image), then sample it offset by the wave normal.
  The bottom wobbles. About 17 MB at 1080p.
- **Moving geometry:** Gerstner waves in the vertex shader, from time, for
  open sea. Also bump the surface up for the shore check.
- **Foam:** a foam texture where the thickness is small, and on wave
  crests.
- **Underwater:** when the camera is below the surface, tint and fog the
  view. This needs the fog work (#17).
- **Rivers:** a mesh along a path with a flow map (direction per point)
  instead of a fixed wave direction. `GhostReconComparison.md`'s river
  notes apply.
- **Physics:** buoyancy and drag for the car and balls in water, a
  separate physics task.

### Project side (AllTerrainVehicle)

- **`WaterCheck.quorum`:** a terrain with a basin, a `WaterSurface` at a
  set level, a skybox and the sun.
  - **Keys:** water level up and down, clarity, wave speed, and switching
    reflections.
  - **What to look for:**
    - the shore fades softly;
    - the deep middle is darker;
    - the sky shows at low angles but not looking down;
    - the ripples move, and the sun glints.
- **WorldTerrain:**
  - a sea level: one large `WaterSurface` that follows the camera in
    steps, with a far ring, so the sea always covers the view;
  - lakes as `WaterSurface`s placed in basins.
  - **TerrainEditor:** a water brush is a later step.

## Phases (each its own reviewed Item)

1. **Time and camera position** for 3D shaders (the camera uniform).
   Small; useful on its own.
2. **The water pass and `WaterSurface`:**
   - thickness color;
   - shore fade;
   - scrolling normals;
   - sun glint.
   - `WaterCheck`.
3. **Sky reflection with fresnel** (binding the sky cube).
4. **OpenGL water** (decision 4).
5. **WorldTerrain sea level and lakes,** and DriveOpen.
6. **Later, each optional:** refraction, waves, foam, underwater, rivers.

**Size:**
- Phases 1 to 3 are the core: about 600 to 800 lines, mostly the shader and
  the pass setup in P.
- Phase 2 is the largest single Item so far, about the size of the skybox
  (Item 26) and MSAA (Item 27) together.

**Fog (#17)** is close to this work. Water and terrain both look better with
distance fog, and underwater needs it. It is a small Item (lighting pass,
camera distance), and could come first or alongside.

## The cheap alternative (not recommended)

A G-buffer water program with no library change:
- It writes scrolling normals, with time smuggled in through the model's
  color override each frame, plus low roughness and alpha below 1.
- **What it gives:** sun glints and color mixing over the ground.
- **What it lacks:** no reflection, no depth tint, no shore fade, no
  refraction.
- **Also:** it must be first used after the terrain program, or it draws
  under it.
- It is a quick placeholder, at most.

## Decisions for the user

1. **The approach:** a separate water pass after lighting (recommended), or
   the cheap G-buffer placeholder? With the pass, should it be a general
   "forward" stage that other transparent shaders (glass, particles) can
   use later (`ShaderProgram:IsForward`, recommended), or water only?
2. **Phase 1 (time and camera position for all 3D shaders):** do it as its
   own Item first (recommended)?
3. **First version contents:** depth color, shore fade, ripples, sun glint,
   then sky reflection (recommended). Or include refraction from the start
   (one more screen-sized image)?
4. **OpenGL:** a simpler water shader there too (recommended, keeps
   parity), or Vulkan only?
5. **Where the water class lives:** `Libraries.Game.Graphics.Worlds`
   alongside `Terrain` (recommended), or project code first and moved
   later?
6. **Fog (#17):** before water, alongside, or after?
