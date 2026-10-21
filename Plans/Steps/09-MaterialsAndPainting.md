# Step 9: materials and painting

Part of `../QuorumStudio.md`. It needs steps 5 and 7, and follows step 8's
stroke and undo flow.

**Revised 2026-10-20.** A new terrain is flat, with no splat and no
materials (step 2). This step lets the user **add and remove materials on
a terrain, and paint them.** It also brings in the material file formats
designed with step 2:
- materials are files the scene refers to;
- every file is plain `.json`, and says what it is in its `"Format"`;
- paths inside a file are relative to that file.

## The files

```
MyProject/
    Scenes/World.qs                               the terrain entry gets "Material"
    Scenes/Terrains/Terrain1/terrain.json                unchanged
    Scenes/Terrains/Terrain1/region_0_0_splat.png        written once a region is painted
    Scenes/Materials/Meadow.json                         a terrain material (made by the editor)
    Textures/Ground037_1K-PNG/Ground037.json      a material (made by the editor, beside its set)
```

### A material (any `.json` file with `"Format": "Quorum Material"`)

One file describes one `Material`, with every value the class holds. It
works for terrains now, and for models later.

**A texture set:**

```json
{
    "Format": "Quorum Material",
    "Version": 1,
    "Name": "Mossy ground",
    "Color Map": "Ground037_1K-PNG_Color.png",
    "Normal Map": "Ground037_1K-PNG_NormalGL.png",
    "Normal Map Convention": "OpenGL",
    "Roughness Map": "Ground037_1K-PNG_Roughness.png",
    "Ambient Occlusion Map": "Ground037_1K-PNG_AmbientOcclusion.png",
    "Height Map": "Ground037_1K-PNG_Displacement.png",
    "Tile Size": 3
}
```

**A plain texture:**

```json
{
    "Format": "Quorum Material",
    "Version": 1,
    "Name": "Grass",
    "Texture": "grass.png",
    "Tile Size": 4
}
```

- **Paths are relative to the material file's own folder,** so a set's
  folder can be copied between projects, material file and all.
- **Each map is named in full,** so sets that don't follow AmbientCG's
  naming also work. `Material:SetTextureSet(folder, name)` fills them in
  for sets that do.
- **Other values `Material` holds**, written only when they differ from the
  default:
  - `"Color"` and `"Emissive"` (four numbers each);
  - `"Roughness"`, `"Metallic"`, `"Bump Shading"`, `"Opaque"`,
    `"Refractive Index"`.
- **When reading,** missing keys keep their defaults, and unknown keys are
  ignored.
- `"Normal Map Convention"` is `"OpenGL"` or `"DirectX"`.
- `"Tile Size"` is `SetTextureScale`: how many world units one tile covers.

### A terrain material (`"Format": "Quorum Terrain Material"`)

A terrain material is the ground's whole look: its layers, in splat-channel
order, and how they blend. Terrains can share one.

```json
{
    "Format": "Quorum Terrain Material",
    "Version": 1,
    "Name": "Meadow",
    "Layers": [
        { "Name": "Grass", "Material": "../Textures/Ground037_1K-PNG/Ground037.json" },
        { "Name": "Rock",  "Material": "../Textures/Rocks002_1K-PNG/Rocks002.json" }
    ],
    "Blend Softness": 0.2
}
```

- **Up to four layers:** layers 1 to 4 are the splat weights' red, green,
  blue and alpha.
- **Paths are relative to this file's folder.**
- **Later settings for the whole ground go here too,** such as Item 42's
  detiling, macro variation and cliffs, once applied.

### The scene's terrain entry gets `"Material"`

```json
    "Material": "Scenes/Materials/Meadow.json"
```

- It's relative to the project, like every scene path. It's written only
  when the terrain has a material.
- **The cost of naming the material in the scene:** painting is stored per
  channel. Choosing a terrain material with its layers in a different order
  moves painted areas onto other textures. The editor says so when the
  layer names differ.

## What the user sees

**A Materials section** in the Properties panel, for the selected terrain:
- **With no materials** (a new terrain): "No materials. The ground is
  plain." and an **Add material...** button.
- **Add material...:**
  - choose a material `.json`, or make one from a texture set's image (for
    example `Ground037_1K-PNG_Color.png`). The new one is written beside the
    set;
  - the first material added makes the terrain's terrain-material file in
    the project's `Scenes/Materials/` (named after the terrain, for example
    `Scenes/Materials/Terrain1.json`), refers to it from the scene, and covers the
    whole ground. Every sample's weight goes to layer 1;
  - layers 2 to 4 are added the same way, up to four. A new layer shows
    nowhere until painted.
- **Each layer row:**
  - its name;
  - its material file, with **Choose...**;
  - its tile size, saved into its material file;
  - **Remove**.
    - Removing a layer moves its painted weight to layer 1.
    - Removing the last layer goes back to no materials and no splat: the
      terrain's splat files are deleted on save, and `"Material"` leaves
      the scene.
- **Blend softness,** saved into the terrain-material file.
- **Shared files:** editing a material or terrain material changes it
  wherever it's used. The section says "Used by 2 terrains in this scene"
  when it's shared.
- **Undo:** adding or removing a layer is one undoable edit, including the
  weights it moved.

**Painting** (Brush section, step 7):
- **Paint:** pick the layer (radio buttons named from the terrain
  material), then hold Space or drag. That layer fills in under the ring.
- **Blend paint:** softens the borders between layers.
- **Undo, Redo, Escape and Save** work as for sculpting (step 8).
- The paint tools are disabled, with the reason given, until the terrain
  has two or more layers.

## Library

1. **L/Game/Graphics/Models/Data/`Material.quorum`:**
   - **`Load(File)`:** reads a material file. It checks `"Format"`.
     Each map is resolved against the file's folder, keeping the file's
     working directory, and stored as an absolute path, so the Vulkan
     material manager finds it wherever the program runs.
   - **`Save(File)`:** writes the maps relative to the file. The relative
     names are kept beside the absolute paths for this.
   - **`GetFile`:** the file it was loaded from or saved to.
2. **New W/`TerrainMaterial.quorum`:**
   - `Load(File)` and `Save(File)`;
   - `GetLayerCount`, `GetLayerName(i)`, `GetLayerMaterial(i)`,
     `AddLayer(name, Material)`, `RemoveLayer(i)`;
   - `Get/SetBlendSoftness`;
   - **`ApplyTo(WorldTerrain)`:**
     - texture-set layers go to `SetLayerMaterial`;
     - plain layers go to a `TextureSplat`'s channels;
     - then `SetHeightBlendSoftness`, and the splat program
       (`TerrainSplatShaderProgram:InstallAsProgram`);
     - **one limit:** plain layers share one tile size (the splat
       program's), so `ApplyTo` uses the first plain layer's, and the
       editor says so if they differ. Texture-set layers keep their own.
3. **`TerrainItem`:** `SetMaterial(TerrainMaterial)`, `GetMaterial`,
   `LoadMaterial(File)`, and `RebuildModels` (step 2) when it changes.
4. **`TerrainData`:**
   - `MoveSplatWeight(fromLayer, toLayer)` over the whole terrain, for
     removing a layer;
   - `ClearSplat`, which sets every region back to unpainted (step 2's
     flag), for removing the last layer.
5. **`SceneProperties`:** `MATERIAL = "Material"`.
6. **`DefaultSceneManager`:**
   - `LoadTerrain` reads `"Material"` (through `ConvertToProjectFile`),
     loading each material file once per scene (a cache by absolute path);
   - `SaveTerrain` writes it when there is one.
7. **The editor:** the Materials section (SC/Properties/), and the paint
   tools on step 8's stroke code with `TerrainTool:TEXTURE`.
8. **Next layer key:** Alt+L is suggested in step 6, and asked about with
   this step. The radio buttons work without a key.

## Risks

- **Texture group 0** (master plan, Stage 0). Ground textures must be among
  the first textures the renderer sees. In Studio, many exist before a
  material is added. **Test this first,** with one texture-set material on
  a terrain in Studio.
- **Packing a texture set** takes about a second (`Item24.md`). That's fine
  for adding a layer.
- **Painting outside the current splat texture's area** (with regions added
  in step 11) makes a new splat texture, which shows grey until a restart
  (`Terrain/TierFour.md`).

## Tests

- **A new terrain:** the Materials section says it has none, and the folder
  has no splat files.
- **Adding Ground037:**
  - the ground shows it everywhere;
  - `Scenes/Materials/Terrain1.json` and `Ground037.json` exist;
  - the scene has `"Material"`;
  - save, reopen and the game all show it.
- **Adding Rocks002 and painting it:** rocks under the ring; undo and redo;
  save and reopen keep the painting; splat files now exist for the painted
  regions.
- **Removing Rocks002:** the painted areas go back to Ground037; undo
  brings them back.
- **Removing the last layer:** the ground is plain again; after saving,
  the splat files are gone and `"Material"` is out of the scene.
- **Two terrains sharing a terrain material:** changing its blend softness
  changes both.
- **A material file moved with its texture folder** to another project
  still loads.
