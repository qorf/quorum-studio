# Step 2: `TerrainItem`, and terrain in scene files

Part of `../QuorumStudio.md`. It needs step 1 (done).

## Status: applied 2026-10-20 (not built or run yet)

**Library (`MakeAWish`):**
- **new** W/`TerrainItem.quorum`;
- W/`TerrainRegion.quorum`: the `splatPainted` flag, with `IsSplatPainted`
  and `SetSplatPainted`;
- W/`TerrainData.quorum`: `SaveRegionImage` writes the splat image only for
  painted regions, and `Save`'s documentation says so;
- L/Game/Scenes/`DefaultSceneManager.quorum`:
  - the `"Terrain"` branches in `CreateItem3D`, `LoadItem3D` and
    `SaveItem3D`;
  - new `LoadTerrain` and `SaveTerrain`;
  - two new `use` lines.

**Project:** **new** `AT/SourceCode/TerrainSceneCheck.quorum`. It checks
this step without Studio, in two runs: the first makes a terrain and a
cube and saves the scene; the second loads it and reports.

**In Studio before step 3:** a scene with a terrain opens, since the scene
editor's `CreateItem3D` passes `"Terrain"` to the base class. But it loads
as a plain `TerrainItem`, and selecting it would hit the `EditorModel` cast
that step 3 fixes.

**Revised 2026-10-20 after review.** Decisions:
1. **Materials are files** that the scene refers to by path, as in other
   engines. They are **not** raw "ground layers" saved inside the scene.
   - Paths inside a material file are **relative to that file**.
   - The **scene** names a terrain's material, not `terrain.json`.
2. **Every file is plain `.json`.** No special extensions: the name doesn't
   guarantee the contents anyway. Each file says what it is in its
   `"Format"` field.
3. **Physics settings are inline** in the scene entry, as other items'
   are.
4. **A new terrain is flat, with no splat and no materials.** Adding and
   removing materials, and painting, come in a later step (step 9). The
   material file formats are designed now (in step 9) so this step's scene
   entry leaves room for them.

**The `TerrainItem` code** in `../Terrain/QuorumStudioPlan-2026-10-19.md` §3
still applies, without its ground-texture parts (see below). The scene
manager code in §4 and §5 also applies, changed as below.

## Files

```
MyProject/
    Scenes/World.qs                     the scene: refers to the terrain
    Scenes/Terrains/Terrain1/terrain.json      the terrain's data: unchanged format (version 2)
    Scenes/Terrains/Terrain1/region_0_0.png    heights, one image per region
```

**A new terrain has no `region_*_splat.png` files.** They're written only
once a region has been painted (see "No splat until painted").

## The scene's terrain entry

```json
{
    "Class": "Terrain",
    "Name": "Terrain",
    "Description": "Ground for the scene, made of height map regions.",
    "File": "Scenes/Terrains/Terrain1/terrain.json",
    "Physics Enabled": true,
    "Friction": 0.8
}
```

- **`"File"`** is relative to the project, like every path in a scene
  (`ConvertToProjectFile`).
- **`"Physics Enabled"` and `"Friction"`** are the scene's existing keys
  (`SceneProperties:PHYSICS_ENABLED`, `FRICTION`).
  - "Physics Enabled" means collision in games (the editor never
    collides).
  - If they're missing, collision is on and friction is the default.
- **`"Material"`** names the terrain's material file. It's added in step 9,
  so this step's reader ignores it and writes none.
- **No position, rotation or scale:** a terrain is always at the origin.
- **The region models aren't saved** as children.
- **An older Quorum** reads `"Terrain"` as a plain `Item3D`, so the scene
  version doesn't change.
- **The old plan's** `"Ground Texture"` and `"Texture Repeat"` are dropped.

## Library

1. **W/`TerrainItem.quorum`** (the 2026-10-19 plan §3, changed):
   - **kept:** `Load(path)`, `Load(File)`, `CreateFlat(file,
     regionsAcross, regionSize, spacing)`, `SaveTerrain`, `GetTerrainFile`,
     `GetWorldTerrain`, `SetCollision`/`HasCollision`, and the private
     `BuildModels`, `CheckEmpty` and `GetFolder`;
   - **removed:** `SetGroundTexture`, `GetGroundTexturePath`,
     `SetTextureRepeat`, `GetTextureRepeat`, and the ground-texture lines
     in `BuildModels`. A new terrain draws with `WorldTerrain`'s plain
     default look;
   - **added:**
     - `SetFriction(number)` and `GetFriction`, given to `WorldTerrain`'s
       `SetFriction` before `Build`;
     - `Update(Layer3D layer, number x, number z)`, which forwards to
       `WorldTerrain:Update`. It does nothing visible until step 9 adds
       materials, but the editor and games call it from the start;
     - `RebuildModels` (private): removes its region-model children,
       rebuilds, and adds them again. Step 9 uses it when materials change,
       and step 11 when regions change.
2. **No splat until painted** (W/`TerrainRegion.quorum`,
   W/`TerrainData.quorum`):
   - **Today:** every region holds splat weights, filled with layer 1, and
     `SaveRegionImage` always writes `region_<x>_<z>_splat.png`.
   - **Change:** a region keeps a `splatPainted` flag.
     - It's set by `SetSplat` and `FillSplat`, and by `SetSplatFromImage`
       when loading.
     - `CopyFrom`, which undo uses, copies it.
     - **The region's own setup doesn't set it.** That setup fills the
       default weights with `FillSplat` (TerrainRegion.quorum:84-86), so it
       must use a private fill that leaves the flag alone.
     - `SaveRegionImage` writes the splat image only when the flag is set.
   - **Loading already copes** with a missing splat image: the region keeps
     the default weights (TerrainData.quorum, the "Terrain saved before
     splat weights existed" branch). So an unpainted terrain is just
     `terrain.json` and its height images.
   - **Games that paint today** (DriveOpen, TerrainEditor) set weights with
     `SetSplat` or `FillSplat`, so they still save their splat images.
3. **L/Game/Scenes/`DefaultSceneManager.quorum`** (the 2026-10-19 plan §5,
   changed):
   - `CreateItem3D`, the `"Terrain"` branch in `LoadItem3D`, and the
     `TerrainItem` branch in `SaveItem3D` stay as planned;
   - **`LoadTerrain`** reads `"Name"`, `"Description"`, `"File"`,
     `"Physics Enabled"` and `"Friction"`. The ground-texture lines go;
   - **`SaveTerrain`** writes the same keys. The ground-texture lines go.
4. **L/Game/Scenes/`SceneProperties.quorum`:** no new constants. The
   2026-10-19 plan's `GROUND_TEXTURE` and `TEXTURE_REPEAT` aren't added.
   `MATERIAL` comes in step 9.

## Tests

- **`TerrainSceneCheck`** (the 2026-10-19 plan §14):
  - the terrain from Studio is flat and plain, at height 0;
  - the cube lands on it;
  - check physics on the item's children.
- **A new terrain's folder** holds `terrain.json` and its region images, and
  no splat images.
- **DriveOpen and TerrainEditor** still save and load their painting.
  TerrainEditor's Save writes splat images for painted regions.
- **Friction 0.1** in the scene: the cube slides further. **"Physics
  Enabled" false:** the cube falls through.
- **A scene without a terrain** loads exactly as before.
