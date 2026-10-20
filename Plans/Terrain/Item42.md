# Item 42: the ground look (detiling, macro variation, dual scaling, cliffs; dirt and sand sets)

**Status (2026-10-19): for review.** Nothing is applied yet. All of it is
**project code**: the splat shader, `WorldTerrain` and DriveOpen. **No
library change.**

**Needs Item 38 first,** for the camera's position in the splat shader
(dual scaling). The rest doesn't need it, but the shader declares the
block, so it waits for 38.

**Scope:** Vulkan only, like the splat program. **OpenGL is not touched.**

Paths: **W/** = `AllTerrainVehicle/SourceCode/Worlds/`, **S** =
`W/TerrainSplatFragmentShader.quorum`.

## Why

`OpenWorlds.md`'s "What Terrain3D has that the port doesn't yet" lists
these four of Terrain3D's shader modules. Today, with texture sets:
- **the tiles show:** grass and rock repeat every 3 or so units, which
  reads as a grid from a few meters up;
- **wide views are one flat color** of each layer;
- **far ground is a fine, busy grid,** smoothed to mush by mipmaps;
- **steep slopes stretch:** textures laid from above smear into streaks on
  cliffs.

And dirt and sand are still plain speckled textures beside two real
texture sets.

## The design

All four parts are in the texture-set path of the splat shader
(`splatTextureSets`), and each can be switched on and off while the game
runs, through the **blue byte of the terrain's color override,** which is
0 today:

| Bit | Part | What it does |
|---|---|---|
| 1 | detiling | In soft patches about 17 units across, each texture is read a second time, turned 52° and 1.31 times larger, and blended in. Tiles stop lining up. |
| 2 | macro variation | Large, soft patches (about 90 units) of lighter and darker, warmer and cooler ground, on the final color, up to ±14%. |
| 4 | dual scaling | From 25 to 110 units away, the same textures read at 5 times their tile size are blended in, up to 60%. |
| 8 | cliffs | Where the slope is steeper than about 40°, the textures are also laid on from the x and z sides (triplanar), fully by about 60°. |

**With every bit off, the shader draws exactly as today:** same samples,
same blending, same normals. The normals are now turned into the world
per layer before blending, rather than after; since the turn is a
rotation, the result is the same.

**Plain-texture terrain** (the path without texture sets) is unchanged.
These parts need the layer table.

**How the parts fit together, per layer:**
1. `readLayer` reads a layer's color-height and normal-roughness textures
   at some coordinates.
2. `readProjected` lays a layer on a plane (above, or a side), with
   detiling and dual scaling.
3. `sampleLayer` does the top plane and, on cliffs, the two side planes,
   and blends them by which way the ground faces.
4. The existing height blending between layers is unchanged.
5. Macro variation tints the blended color.

**Costs** (texture reads per layer with weight, of two textures each):

| Parts on | Reads |
|---|---|
| none | 1 (as today) |
| detiling | 1 or 2 (2 only inside the patches) |
| dual scaling | +1 beyond 25 units |
| cliffs | ×3 on slopes over 40° only |

Usually one or two layers have weight at a point, so flat near ground
costs at most 4 reads where it costs 2 today.

**Noise without a texture:** a small hash and smooth value noise in the
shader, so nothing new is loaded.

**Mip levels** are kept right with `textureGrad`, from the position's
screen rates taken once at the top (Item 25's rule), turned and scaled
for each reading.

**Cliff normals:** Ben Golus's whiteout triplanar blend, with his fix for
the mirrored back faces. If cliff bumps look inside-out by eye, the fix is
to negate `normalX.y` and `normalZ.y` (the normal maps' green), noted in
the test plan.

### Dirt and sand texture sets

DriveOpen's layers 2 (dirt, blue) and 3 (sand, alpha) get texture sets the
same way grass and rock do, **if their folders are in `Textures/`.** I
don't download anything; the names are two fields, empty until set:
- download any AmbientCG 1K PNG dirt set and sand set from
  ambientcg.com;
- unzip each into `Textures/<name>/`, as `Ground037_1K-PNG` is;
- set `dirtSetName` and `sandSetName` to the folder names.

Until then, or if a folder is missing, dirt and sand stay speckled, with a
message saying so.

## The code

### 1. S (`W/TerrainSplatFragmentShader.quorum`)

**The class comment.** After the paragraph that begins "Height blending is
Terrain3D's idea", add:

```
With texture set layers, four more of Terrain3D's ideas can be switched on
by the blue byte of the color override (library item 42): detiling (1),
which reads each texture again, turned, in soft patches, so the tiles
don't line up; macro variation (2), large patches of lighter and darker
ground; dual scaling (4), which blends in the textures at a much larger
size far away; and cliffs (8), which lays the textures on from the sides
where the ground is steep. With the byte 0, the ground is drawn as
before.
```

**The constants.** After

```glsl
// How many world units one tile of each ground texture covers.
const float LAYER_REPEAT = " + tileText + ";
```

add:

```glsl
// The ground look (library item 42). Which parts are on comes from the
// color override's blue byte.
const uint LOOK_DETILING = 1u;
const uint LOOK_MACRO = 2u;
const uint LOOK_DUAL = 4u;
const uint LOOK_CLIFFS = 8u;
// Detiling: a second reading of each texture, turned and a little larger,
// blended in by a soft pattern about this many world units across.
const float DETILE_ANGLE = 0.9;
const float DETILE_SCALE = 1.31;
const float DETILE_PATTERN_SIZE = 17.0;
// Macro variation: lighter and darker patches about this many world units
// across, by up to this much.
const float MACRO_SIZE = 90.0;
const float MACRO_STRENGTH = 0.14;
// Dual scaling: from DUAL_START to DUAL_END units from the camera, the
// textures read at DUAL_SCALE times their tile size are blended in, up to
// DUAL_MIX of the color.
const float DUAL_START = 25.0;
const float DUAL_END = 110.0;
const float DUAL_SCALE = 5.0;
const float DUAL_MIX = 0.6;
// Cliffs: where the normal's up part falls below CLIFF_START (about 40
// degrees of slope), the textures are laid on from the sides as well,
// fully by CLIFF_END (about 60 degrees).
const float CLIFF_START = 0.77;
const float CLIFF_END = 0.5;

// The 3D camera uniform (library item 38): the camera's position, for
// dual scaling.
layout(set = 0, binding = 0) uniform Camera
{
    mat4 projectionViewMatrix;
    vec3 cameraPosition;
    float time;
    vec2 screenSize;
    float nearPlane;
    float farPlane;
} camera;
```

**The texture-set function.** The whole of `splatTextureSets`, with its
comment:

```glsl
// Colors terrain whose layers are texture sets, listed in the layer table,
// blending them by height. The weights are already normalized.
void splatTextureSets(Material material, vec4 weights, uvec4 overrideBytes)
{
    uint tablePlace = overrideBytes.x;

    // How fast the world position changes across the screen, for picking
    // mip levels. Taken here, before any branch, since layers with no
    // weight are skipped below (Terrain3D does the same).
    vec2 positionDx = dFdx(v_position.xz);
    vec2 positionDy = dFdy(v_position.xz);
    float softness = max(float(overrideBytes.y) / 254.0, 0.01);

    // The ground's own frame: the normal from the mesh, with x and z laid
    // onto it. Texture coordinates are world x and z, and a normal map's
    // green points to the image's top, which is toward smaller v, so
    // toward -z.
    vec3 groundNormal = normalize(v_tangentBitangentNormal[2]);
    vec3 tangent = normalize(vec3(1.0, 0.0, 0.0) - groundNormal * groundNormal.x);
    vec3 bitangent = cross(groundNormal, tangent);
    mat3 groundFrame = mat3(tangent, bitangent, groundNormal);

    vec4 albedoHeight[4];
    vec4 normalRoughness[4];
    bool hasNormals[4];
    float stand[4];
    float highest = -1.0;
    for (int i = 0; i < 4; i++)
    {
        albedoHeight[i] = vec4(0.0);
        normalRoughness[i] = vec4(0.5, 0.5, 1.0, material.roughness);
        hasNormals[i] = false;
        stand[i] = -1.0;
        if (weights[i] < 0.01)
            continue;

        uvec4 entry = uvec4(texelFetch(nonuniformEXT(sampler2D(globalTextures[tablePlace], immutableSampler)), ivec2(i, 0), 0) * 255.0 + 0.5);
        float tileSize = entry.z > 0u ? float(entry.z) / 4.0 : LAYER_REPEAT;
        vec2 coordinates = v_position.xz / tileSize;
        albedoHeight[i] = sampleGlobalTextureGrad(entry.x, coordinates, positionDx / tileSize, positionDy / tileSize);
        float height = 0.5;
        if (entry.y != 255u)
        {
            normalRoughness[i] = sampleGlobalTextureGrad(entry.y, coordinates, positionDx / tileSize, positionDy / tileSize);
            hasNormals[i] = true;
            height = albedoHeight[i].a;
        }
        stand[i] = height + weights[i];
        highest = max(highest, stand[i]);
    }

    // Each layer shows by how close it stands to the highest, within the
    // softness, and only where it has weight.
    float floorLevel = highest - softness;
    vec4 blend = vec4(0.0);
    for (int i = 0; i < 4; i++)
    {
        if (stand[i] >= 0.0)
            blend[i] = max(stand[i] - floorLevel, 0.0);
    }
    float blendTotal = blend.r + blend.g + blend.b + blend.a;
    if (blendTotal < 0.0001)
        blend = weights;
    else
        blend = blend / blendTotal;

    vec3 color = vec3(0.0);
    vec3 packedNormal = vec3(0.0);
    float roughness = 0.0;
    float occlusion = 0.0;
    for (int i = 0; i < 4; i++)
    {
        if (blend[i] <= 0.0)
            continue;
        color += albedoHeight[i].rgb * blend[i];
        vec3 layerNormal = vec3(0.0, 0.0, 1.0);
        float layerOcclusion = 1.0;
        if (hasNormals[i])
        {
            vec3 unpacked = normalRoughness[i].rgb * 2.0 - 1.0;
            // Terrain3D's packing: the normal's length carries ambient
            // occlusion, as in the default shader (library item 24).
            layerOcclusion = clamp(length(unpacked) * 2.0 - 1.0, 0.0, 1.0);
            layerNormal = normalize(unpacked);
        }
        packedNormal += layerNormal * blend[i];
        roughness += normalRoughness[i].a * blend[i];
        occlusion += layerOcclusion * blend[i];
    }

    outputColor = vec4(color, 1.0);
    outputPosition = v_position;
    outputNormal = vec4(normalize(groundFrame * normalize(packedNormal)), 1.0);
    outputPBR = vec4(occlusion, roughness, 0.0, 1.0);
}
```

becomes these new functions and the new `splatTextureSets`:

```glsl
// One layer's reading at a point: its color and height, its normal (in
// whatever frame it was read in, then in the world), roughness and
// ambient occlusion.
struct LayerSample
{
    vec4 albedoHeight;
    vec3 normal;
    float roughness;
    float occlusion;
};

// A number from 0 to 1 that looks random, the same every time for the
// same point.
float hash(vec2 point)
{
    point = fract(point * vec2(123.34, 456.21));
    point += dot(point, point + 45.32);
    return fract(point.x * point.y);
}

// Smooth noise from 0 to 1: random values at whole-number points, blended
// smoothly between them.
float valueNoise(vec2 point)
{
    vec2 corner = floor(point);
    vec2 inside = fract(point);
    vec2 curve = inside * inside * (3.0 - 2.0 * inside);
    float bottom = mix(hash(corner), hash(corner + vec2(1.0, 0.0)), curve.x);
    float top = mix(hash(corner + vec2(0.0, 1.0)), hash(corner + vec2(1.0, 1.0)), curve.x);
    return mix(bottom, top, curve.y);
}

// Reads a layer's two textures at some coordinates, with the rates the
// coordinates change across the screen for the mip level, and unpacks
// them. A layer without a normal texture (255 in the table) is flat, with
// the material's roughness and a middle height, as before.
LayerSample readLayer(uvec4 entry, vec2 coordinates, vec2 changeX, vec2 changeY, float plainRoughness)
{
    LayerSample result;
    result.albedoHeight = sampleGlobalTextureGrad(entry.x, coordinates, changeX, changeY);
    result.normal = vec3(0.0, 0.0, 1.0);
    result.roughness = plainRoughness;
    result.occlusion = 1.0;
    if (entry.y != 255u)
    {
        vec4 normalRoughness = sampleGlobalTextureGrad(entry.y, coordinates, changeX, changeY);
        vec3 unpacked = normalRoughness.rgb * 2.0 - 1.0;
        // Terrain3D's packing: the normal's length carries ambient
        // occlusion, as in the default shader (library item 24).
        result.occlusion = clamp(length(unpacked) * 2.0 - 1.0, 0.0, 1.0);
        result.normal = normalize(unpacked);
        result.roughness = normalRoughness.a;
    }
    else
    {
        result.albedoHeight.a = 0.5;
    }
    return result;
}

// Blends two readings of a layer.
LayerSample mixSamples(LayerSample first, LayerSample second, float amount)
{
    LayerSample result;
    result.albedoHeight = mix(first.albedoHeight, second.albedoHeight, amount);
    result.normal = normalize(mix(first.normal, second.normal, amount));
    result.roughness = mix(first.roughness, second.roughness, amount);
    result.occlusion = mix(first.occlusion, second.occlusion, amount);
    return result;
}

// Reads a layer laid flat on a plane, with detiling and dual scaling if
// they are on (Item 42). The place is the point's two coordinates on the
// plane, in world units; the normal stays in the plane's frame.
LayerSample readProjected(uvec4 entry, vec2 place, vec2 placeDx, vec2 placeDy, float tileSize, float plainRoughness, uint look, float farness)
{
    vec2 coordinates = place / tileSize;
    vec2 changeX = placeDx / tileSize;
    vec2 changeY = placeDy / tileSize;
    LayerSample result = readLayer(entry, coordinates, changeX, changeY, plainRoughness);

    // Detiling: in soft patches, a second reading, turned and a little
    // larger, so the tiles don't line up into a grid.
    if ((look & LOOK_DETILING) != 0u)
    {
        float amount = smoothstep(0.35, 0.65, valueNoise(place / DETILE_PATTERN_SIZE));
        if (amount > 0.0)
        {
            float turnCos = cos(DETILE_ANGLE);
            float turnSin = sin(DETILE_ANGLE);
            mat2 turn = mat2(turnCos, turnSin, -turnSin, turnCos);
            vec2 turnedCoordinates = turn * coordinates / DETILE_SCALE + vec2(0.37, 0.71);
            LayerSample turned = readLayer(entry, turnedCoordinates, turn * changeX / DETILE_SCALE, turn * changeY / DETILE_SCALE, plainRoughness);
            // The turned reading's slopes are turned back onto the plane.
            turned.normal.xy = transpose(turn) * turned.normal.xy;
            result = mixSamples(result, turned, amount);
        }
    }

    // Dual scaling: far away, the same textures read at a much larger
    // tile size are blended in, so the distance doesn't show a fine grid.
    if ((look & LOOK_DUAL) != 0u && farness > 0.0)
    {
        float farTile = tileSize * DUAL_SCALE;
        LayerSample farReading = readLayer(entry, place / farTile, placeDx / farTile, placeDy / farTile, plainRoughness);
        result = mixSamples(result, farReading, farness * DUAL_MIX);
    }
    return result;
}

// One layer at this point, in the world: laid on from above, and on
// cliffs also from the x and z sides (triplanar), blended by which way
// the ground faces (Item 42). The side normals are put into the world
// with Ben Golus's whiteout blend.
LayerSample sampleLayer(uvec4 entry, float tileSize, float plainRoughness, uint look, float farness, float cliff,
    mat3 groundFrame, vec3 groundNormal, vec3 positionDx, vec3 positionDy)
{
    vec3 position = v_position.xyz;
    LayerSample top = readProjected(entry, position.xz, positionDx.xz, positionDy.xz, tileSize, plainRoughness, look, farness);
    top.normal = normalize(groundFrame * top.normal);
    if (cliff <= 0.0)
        return top;

    float signX = groundNormal.x < 0.0 ? -1.0 : 1.0;
    float signZ = groundNormal.z < 0.0 ? -1.0 : 1.0;
    vec2 flipX = vec2(signX, 1.0);
    vec2 flipZ = vec2(-signZ, 1.0);
    LayerSample sideX = readProjected(entry, position.zy * flipX, positionDx.zy * flipX, positionDy.zy * flipX, tileSize, plainRoughness, look, farness);
    LayerSample sideZ = readProjected(entry, position.xy * flipZ, positionDx.xy * flipZ, positionDy.xy * flipZ, tileSize, plainRoughness, look, farness);

    vec3 normalX = sideX.normal;
    normalX.x *= signX;
    normalX = vec3(normalX.xy + groundNormal.zy, abs(normalX.z) * groundNormal.x);
    vec3 normalZ = sideZ.normal;
    normalZ.x *= -signZ;
    normalZ = vec3(normalZ.xy + groundNormal.xy, abs(normalZ.z) * groundNormal.z);

    vec2 sideWeights = pow(abs(groundNormal.xz), vec2(4.0));
    sideWeights /= max(sideWeights.x + sideWeights.y, 0.0001);
    LayerSample side = mixSamples(sideX, sideZ, sideWeights.y);
    side.normal = normalize(normalX.zyx * sideWeights.x + normalZ.xyz * sideWeights.y);
    return mixSamples(top, side, cliff);
}

// Large, soft patches of lighter and darker, warmer and cooler ground, so
// wide views don't look like one repeated texture (Item 42).
vec3 macroTint(vec2 place)
{
    float broad = valueNoise(place / MACRO_SIZE);
    float fine = valueNoise(place / (MACRO_SIZE * 0.31) + vec2(17.3, 5.1));
    float shade = (broad * 0.7 + fine * 0.3 - 0.5) * 2.0;
    float warmth = valueNoise(place / (MACRO_SIZE * 1.7) + vec2(3.7, 41.9)) - 0.5;
    return (1.0 + shade * MACRO_STRENGTH) * vec3(1.0 + warmth * 0.12, 1.0, 1.0 - warmth * 0.12);
}

// Colors terrain whose layers are texture sets, listed in the layer table,
// blending them by height. The weights are already normalized. The color
// override's blue byte says which parts of the ground look are on (Item
// 42); with none on, this draws exactly as before.
void splatTextureSets(Material material, vec4 weights, uvec4 overrideBytes)
{
    uint tablePlace = overrideBytes.x;
    uint look = overrideBytes.z;

    // How fast the world position changes across the screen, for picking
    // mip levels. Taken here, before any branch, since layers with no
    // weight are skipped below (Terrain3D does the same).
    vec3 positionDx = dFdx(v_position.xyz);
    vec3 positionDy = dFdy(v_position.xyz);
    float softness = max(float(overrideBytes.y) / 254.0, 0.01);

    // The ground's own frame: the normal from the mesh, with x and z laid
    // onto it. Texture coordinates are world x and z, and a normal map's
    // green points to the image's top, which is toward smaller v, so
    // toward -z.
    vec3 groundNormal = normalize(v_tangentBitangentNormal[2]);
    vec3 tangent = normalize(vec3(1.0, 0.0, 0.0) - groundNormal * groundNormal.x);
    vec3 bitangent = cross(groundNormal, tangent);
    mat3 groundFrame = mat3(tangent, bitangent, groundNormal);

    // How far into the dual scaling distance, and how much of a cliff.
    float farness = 0.0;
    if ((look & LOOK_DUAL) != 0u)
        farness = smoothstep(DUAL_START, DUAL_END, length(v_position.xyz - camera.cameraPosition));
    float cliff = 0.0;
    if ((look & LOOK_CLIFFS) != 0u)
        cliff = 1.0 - smoothstep(CLIFF_END, CLIFF_START, groundNormal.y);

    LayerSample layers[4];
    float stand[4];
    float highest = -1.0;
    for (int i = 0; i < 4; i++)
    {
        layers[i] = LayerSample(vec4(0.0), groundNormal, material.roughness, 1.0);
        stand[i] = -1.0;
        if (weights[i] < 0.01)
            continue;

        uvec4 entry = uvec4(texelFetch(nonuniformEXT(sampler2D(globalTextures[tablePlace], immutableSampler)), ivec2(i, 0), 0) * 255.0 + 0.5);
        float tileSize = entry.z > 0u ? float(entry.z) / 4.0 : LAYER_REPEAT;
        layers[i] = sampleLayer(entry, tileSize, material.roughness, look, farness, cliff, groundFrame, groundNormal, positionDx, positionDy);
        stand[i] = layers[i].albedoHeight.a + weights[i];
        highest = max(highest, stand[i]);
    }

    // Each layer shows by how close it stands to the highest, within the
    // softness, and only where it has weight.
    float floorLevel = highest - softness;
    vec4 blend = vec4(0.0);
    for (int i = 0; i < 4; i++)
    {
        if (stand[i] >= 0.0)
            blend[i] = max(stand[i] - floorLevel, 0.0);
    }
    float blendTotal = blend.r + blend.g + blend.b + blend.a;
    if (blendTotal < 0.0001)
        blend = weights;
    else
        blend = blend / blendTotal;

    vec3 color = vec3(0.0);
    vec3 normal = vec3(0.0);
    float roughness = 0.0;
    float occlusion = 0.0;
    for (int i = 0; i < 4; i++)
    {
        if (blend[i] <= 0.0)
            continue;
        color += layers[i].albedoHeight.rgb * blend[i];
        normal += layers[i].normal * blend[i];
        roughness += layers[i].roughness * blend[i];
        occlusion += layers[i].occlusion * blend[i];
    }
    if ((look & LOOK_MACRO) != 0u)
        color *= macroTint(v_position.xz);

    outputColor = vec4(color, 1.0);
    outputPosition = v_position;
    outputNormal = vec4(normalize(normal), 1.0);
    outputPBR = vec4(occlusion, roughness, 0.0, 1.0);
}
```

`splatTerrain`, `main` and the default path below are unchanged.

**GLSL names:** `sample` is a reserved word in GLSL 4, so the far reading is
`farReading`.

### 2. `W/WorldTerrain.quorum`

**New fields,** after `number heightBlendSoftness = 0.2`:

```
    // The ground look (library item 42), for texture set layers: which of
    // the splat shader's four extra parts are on. They go to the shader in
    // the blue byte of layerColor.
    boolean groundDetiling = true
    boolean groundMacroVariation = true
    boolean groundDualScaling = true
    boolean groundCliffs = true
```

**`CheckLayerTableReady`.** The lines

```
        Color color
        layerColor = color:CustomColor((tablePlace + 0.5) / 255.0, (math:Round(heightBlendSoftness * 254) + 0.5) / 255.0,
            0, 1)
```

become:

```
        // Blue says which parts of the ground look are on (library item 42).
        Color color
        layerColor = color:CustomColor((tablePlace + 0.5) / 255.0, (math:Round(heightBlendSoftness * 254) + 0.5) / 255.0,
            (GetGroundLookBits() + 0.5) / 255.0, 1)
```

**The class comment's layer table paragraph** (in the comment before
`CheckLayerTableReady`). The sentence

```
    Once the table has its own place, layerColor is made from it: red is
    the table's place, green the height blend softness, and alpha 255,
    which tells the shader to use the table.
```

becomes:

```
    Once the table has its own place, layerColor is made from it: red is
    the table's place, green the height blend softness, blue which parts
    of the ground look are on (library item 42), and alpha 255, which tells
    the shader to use the table.
```

**New actions,** after `GetHeightBlendSoftness`:

```
    /*
    This action switches the four parts of the ground look on or off, for
    texture set layers (library item 42). It takes effect on the next
    Update, so it can change while the game runs.
    - Detiling reads each texture a second time, turned, in soft patches,
      so the tiles don't line up into a grid.
    - Macro variation adds large patches of lighter and darker ground.
    - Dual scaling blends in the textures at a much larger size far away.
    - Cliffs lays the textures on from the sides where the ground is steep,
      instead of stretching them.
    All four are on by default.

    Attribute: Parameter detiling True to break up the tiles.
    Attribute: Parameter macroVariation True for large light and dark patches.
    Attribute: Parameter dualScaling True for larger tiles far away.
    Attribute: Parameter cliffs True to lay textures on steep ground from the sides.
    */
    action SetGroundLook(boolean detiling, boolean macroVariation, boolean dualScaling, boolean cliffs)
        groundDetiling = detiling
        groundMacroVariation = macroVariation
        groundDualScaling = dualScaling
        groundCliffs = cliffs
        // The layer color is made again, with the new parts, on the next
        // Update.
        if layersReady and HasLayerMaterials()
            layersReady = false
        end
    end

    /*
    This action returns which parts of the ground look are on, as the splat
    shader reads them: 1 detiling, 2 macro variation, 4 dual scaling, 8
    cliffs, added together.

    Attribute: Returns A number from 0 (none) to 15 (all four).
    */
    action GetGroundLookBits returns integer
        integer bits = 0
        if groundDetiling
            bits = bits + 1
        end
        if groundMacroVariation
            bits = bits + 2
        end
        if groundDualScaling
            bits = bits + 4
        end
        if groundCliffs
            bits = bits + 8
        end
        return bits
    end
```

`Update` already calls `CheckLayersReady` when `splatting and not
layersReady`, which goes to `CheckLayerTableReady`. The table is already
made, so it only makes the new color and gives it to every model. Until
then, models keep the old color, so nothing flickers.

### 3. `SourceCode/DriveOpen.quorum`

**New `use`** (if missing): `use Libraries.System.File` is already there.

**New fields,** after `boolean useTextureSets = true`:

```
    // Texture sets for dirt and sand (library item 42): the names of
    // AmbientCG 1K PNG folders in Textures, such as "Ground037_1K-PNG".
    // Empty, or a folder that isn't there, keeps the speckled texture.
    text dirtSetName = ""
    text sandSetName = ""

    // G steps through the ground look (library item 42): all of it, none,
    // then each part alone.
    integer groundLookChoice = 0
    boolean gWasDown = false
```

**`UseGroundTextureSets`.** The action

```
    /*
    This action gives the grass and rock layers AmbientCG texture sets
    (library item 24): Ground037, mossy ground, for grass, and Rocks002,
    pebbles, for rock. They then have bumps, roughness and ambient
    occlusion, and blend with each other by height. Dirt and sand stay the
    plain speckled textures.
    */
    action UseGroundTextureSets
        Material grass
        grass:SetTextureSet("Textures/Ground037_1K-PNG", "Ground037_1K-PNG")
        grass:SetTextureScale(3)
        terrain:SetLayerMaterial(0, grass)

        Material rock
        rock:SetTextureSet("Textures/Rocks002_1K-PNG", "Rocks002_1K-PNG")
        rock:SetTextureScale(2.5)
        terrain:SetLayerMaterial(1, rock)
    end
```

becomes:

```
    /*
    This action gives the grass and rock layers AmbientCG texture sets
    (library item 24): Ground037, mossy ground, for grass, and Rocks002,
    pebbles, for rock. They then have bumps, roughness and ambient
    occlusion, and blend with each other by height. Dirt and sand get
    texture sets too if their folders are in Textures (library item 42);
    otherwise they stay the plain speckled textures.
    */
    action UseGroundTextureSets
        Material grass
        grass:SetTextureSet("Textures/Ground037_1K-PNG", "Ground037_1K-PNG")
        grass:SetTextureScale(3)
        terrain:SetLayerMaterial(0, grass)

        Material rock
        rock:SetTextureSet("Textures/Rocks002_1K-PNG", "Rocks002_1K-PNG")
        rock:SetTextureScale(2.5)
        terrain:SetLayerMaterial(1, rock)

        UseOptionalTextureSet(2, "Dirt", dirtSetName, 2.5)
        UseOptionalTextureSet(3, "Sand", sandSetName, 2)
    end

    /*
    This action gives a ground layer an AmbientCG texture set, if its
    folder is in Textures (library item 42). Otherwise the layer keeps its
    plain texture, and a message says why.

    Attribute: Parameter layer The ground layer, 0 to 3.
    Attribute: Parameter layerName The layer's name, for the message.
    Attribute: Parameter setName The set's folder name in Textures, or empty for none.
    Attribute: Parameter tileSize How many world units one tile covers.
    */
    action UseOptionalTextureSet(integer layer, text layerName, text setName, number tileSize)
        if setName = ""
            output layerName + ": the plain speckled texture (no texture set named)."
            return now
        end
        File colorMap
        colorMap:SetPath("Textures/" + setName + "/" + setName + "_Color.png")
        if not colorMap:Exists()
            output layerName + ": the plain speckled texture; " + colorMap:GetPath() + " isn't there."
            return now
        end
        Material layerMaterial
        layerMaterial:SetTextureSet("Textures/" + setName, setName)
        layerMaterial:SetTextureScale(tileSize)
        terrain:SetLayerMaterial(layer, layerMaterial)
        output layerName + ": the texture set " + setName + "."
    end
```

**In `Update`,** after the `kWasDown = kDown` line:

```
        boolean gDown = monitor:IsKeyPressed(keys:G)
        if useSplatting and gDown and not gWasDown
            groundLookChoice = (groundLookChoice + 1) mod 6
            ShowGroundLook()
        end
        gWasDown = gDown
```

**New action,** after `ToggleSky`:

```
    /*
    This action sets the ground look for the current choice (library item
    42): all four parts, none, or each alone, so each can be compared with
    the plain ground.
    */
    action ShowGroundLook
        if groundLookChoice = 0
            terrain:SetGroundLook(true, true, true, true)
            output "Ground look: all (detiling, macro variation, dual scaling, cliffs)."
        elseif groundLookChoice = 1
            terrain:SetGroundLook(false, false, false, false)
            output "Ground look: none, as before item 42."
        elseif groundLookChoice = 2
            terrain:SetGroundLook(true, false, false, false)
            output "Ground look: detiling only."
        elseif groundLookChoice = 3
            terrain:SetGroundLook(false, true, false, false)
            output "Ground look: macro variation only."
        elseif groundLookChoice = 4
            terrain:SetGroundLook(false, false, true, false)
            output "Ground look: dual scaling only."
        else
            terrain:SetGroundLook(false, false, false, true)
            output "Ground look: cliffs only."
        end
    end
```

`G` is free in DriveOpen (taken: W A S D H N K R E Q P C, and F after Item
40). The ground look starts with everything on, the WorldTerrain default.

## What changes

**On screen, in DriveOpen:** the ground no longer shows a tile grid; wide
views have soft light and dark patches; far ground keeps texture instead of
going to mush; steep hillsides show rock laid on from the side instead of
streaks. Dirt and sand only change once their folders are added.

**Cost:** see the table above; fragment cost on terrain rises by up to
about 2× on flat near ground with detiling, and 3× on cliffs.

**Memory:** none, apart from the dirt and sand sets if added (about 11 MB
each at 1K, as grass and rock).

## Test plan

1. **DriveOpen, G through the six choices,** from a hill top and close to
   the ground:
   - none: exactly as today (compare with a screenshot from before);
   - detiling: no grid of repeated tufts or stones, and no visible seams
     where the patches blend;
   - macro: soft lighter and darker areas across the valley;
   - dual: far slopes keep a coarse texture rather than a flat color, and
     there is no line where it starts;
   - cliffs: the steepest hillsides show rock without streaks. If the
     side bumps look lit from the wrong side, negate `normalX.y` and
     `normalZ.y` in `sampleLayer`.
2. **No shimmer** when driving: mip levels are right (moving lines or
   sparkle in the distance would mean a wrong rate of change).
3. **Painting (P, C) still works,** and painted layers blend as before.
4. **The far rings** (`SetFarRadius`) look the same as the near ground at
   their edge.
5. **FPS** with all four on, against none: note both in the handoff.
6. **With dirt and sand sets added:** both layers show bumps and blend
   by height with grass and rock.

## Rollback

Revert S, `WorldTerrain` and DriveOpen to before the Item 42 lines. With
`SetGroundLook(false, false, false, false)`, the new shader already draws
as before, so a quick test of "is it the ground look" needs no rebuild.
