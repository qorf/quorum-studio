# Step 14: material details (which file is each map)

Part of `../QuorumStudio.md`. It needs step 9. Written 2026-10-20 at the
user's request; **applied 2026-10-20** (not built or run yet).

## Status

- **`Material`:** map kinds (`COLOR_MAP` ... `TEXTURE`) with
  `GetMapPath`/`SetMapPath`/`GetMapName`; `Copy`/`CopyFrom`; `MakePlain`;
  `IsColorMapName`, `GetSetName` and `FindTextureSetMaps(File, replace)`
  with the naming table below; `GetMapSizeProblem(path)`.
  `SetTextureSet(folder, name)` keeps its exact AmbientCG behavior, since
  games call it with paths relative to the program; its comment is
  corrected and points to `FindTextureSetMaps`.
- **Sharing by file:** `TerrainMaterial:Load(File, HashTable)`;
  `DefaultSceneManager` keeps the table per scene load and gives it out
  with `GetLoadedMaterials`; the editor's add and choose use it.
- **`TerrainMaterial:ReloadLayer`** and **`TerrainItem:RefreshMaterialLayer`**.
- **Adding a material** treats any color ending as a texture set, names it
  without AmbientCG's `_1K-PNG`, and says what maps it found.
- **`SceneEditor`:** `GetTerrainLayerMaterial`, `CountLayersUsing`,
  `SetTerrainLayerMap`, `ClearTerrainLayerMap`,
  `SetTerrainLayerNormalConvention`, `SetTerrainLayerValue`,
  `RenameTerrainLayerMaterial`, `FindTerrainLayerMaps`,
  `MakeTerrainLayerPlain`, `MaterialEdited`; new
  `Interface/Undo/SceneEditorMaterialEdit`.
- **The Materials section:** Details... on each row; the details area
  under the rows (`MakeDetails`, `FillDetails`), with its fields taken when
  the focus leaves them (new `Interface/Events/TerrainMaterialFieldListener`).
- **Not done:** Enter doesn't take a field's value: no key is bound for it
  in the Properties panel, and binding one would change every dialog. Tab
  or a click away takes it.

**The problem.** A material's maps are found once, by name, when the
material is made from a texture set's color map (step 9). After that, the
only way to see or change which image is the color, normal, roughness,
ambient occlusion or height map is to edit the material's `.json` file by
hand. The Materials section shows only the layer's name, its file, its tile
size, Choose... and Remove. If the name guess missed a map, nothing says so.

**The goal.** For any layer, the user can see each map's file, choose a
different one, or clear it, and set the values that go with them, all in
Studio. The material file is still the one place this lives.

## What exists

- **`Material`** (L/Game/Graphics/Models/Data) already has a getter and
  setter for every map (`Set/GetColorMapPath`, `NormalMapPath`,
  `RoughnessMapPath`, `AmbientOcclusionMapPath`, `HeightMapPath`, and
  `TexturePath` for a plain texture), the normal map's convention
  (`SetNormalMapDirectX`), the roughness and metallic factors, and
  `Load`/`Save` (step 9). A texture set is any material with a color map.
- **`Material:SetTextureSet(folder, name)`** guesses the maps from
  AmbientCG's names only (`_Color`, `_NormalGL`/`_NormalDX`, `_Roughness`,
  `_AmbientOcclusion`, `_Displacement`). **Its comment wrongly says it also
  handles Poly Haven's names**; it doesn't.
- **Packing:** the renderer packs a texture set into two textures when a
  layer is given it (`MaterialVulkan:PreloadTextureSet`).
  - **Every map must be the same size as the color map**, or it stops with
    an error.
  - Without a roughness map, the roughness factor is packed in, so changing
    the factor means packing again. The metallic factor is not packed.
  - **Packed sets are shared by their maps** (Item 34): giving the same
    maps again reuses the textures. Each new combination of maps packs
    anew, takes about a second, and uses up texture places in the renderer
    for good (step 9's risk).
- **`TerrainMaterial:ApplyTo`** only gives a layer to the renderer again
  when the layer's `Material` object changed, so it doesn't notice a map
  changing inside the same `Material`.
- **Sharing:** two terrain materials that name the same material file each
  load their own `Material` object today, so editing one doesn't change the
  other until the scene is reopened, and the last one saved wins.

## What the user sees

### The Materials section gets a Details button per layer

Each layer row (step 9) gets **Details...**, next to Choose... and Remove.
It opens a **Material details** area, under the layer rows, for that one
layer. Pressing Details... on another layer moves the area to it, and
**Close details** closes it. One layer's details show at a time, so the
panel stays short.

### The Material details area

Read as a group named "Material details, layer 2, Rocks".

- **Name:** a field. It is saved in the material file, and the layer's row
  shows it.
- **File:** the material file, such as `Textures/Rocks002_1K-PNG/Rocks002.json`
  (read only).
- **Shared:** "Also used by 2 other layers in this scene" when another
  layer, on this terrain or another, uses the same material file. Changes
  here change it everywhere.
- **Kind:** "Texture set" or "Plain texture" (read only), from whether it
  has a color map.
- **Maps**, one line each:
  - **Texture set:** Color, Normal, Roughness, Ambient occlusion, Height;
  - **plain texture:** Texture;
  - each line says the file's name relative to the material file, or
    "none", and has **Choose...** and **Clear**:
    - Choose... opens the file chooser in the material file's folder,
      showing images (`png`, `jpg`);
    - Clear removes the map. The color map of a texture set can't be
      cleared: clearing it is refused, with the reason;
    - choosing a color map for a plain texture makes it a texture set;
      **Make plain** (a button shown for a texture set) goes the other way,
      keeping the color map as its one texture.
- **Normal map convention:** OpenGL or DirectX (radio buttons). Shown when
  there's a normal map.
- **Find maps by name:** looks in the color map's folder for the maps that
  are "none", by the naming patterns below, and fills only those. It says
  what it found: "Found normal and roughness maps. No ambient occlusion or
  height map found."
- **Values:**
  - **Roughness** (0 to 1): used where there's no roughness map, and said
    so ("Not used: the roughness map sets it") when there is one;
  - **Metallic** (0 to 1);
  - the **tile size** stays in the layer's row (step 9).
- **What happens on a change:**
  - a map change, the convention, or a roughness change without a roughness
    map: the set is packed again and the ground redraws. It says "Packing
    the texture set..." first, since it takes about a second;
  - numbers are taken when the field is left, or with Enter, not on every
    key, so typing "0.35" packs once, not three times;
  - every change can be undone, and is saved into the material file when
    the scene is saved.
- **Checks before packing:**
  - **A map of a different size** from the color map is refused, with both
    sizes ("Rocks002_Roughness.png is 2048 by 2048, but the color map is
    1024 by 1024. Maps in a set must be the same size."). The material
    doesn't change.
  - **A file outside the project** is allowed but said
    ("This map is outside the project, so it won't be copied with it").

### Naming patterns (for adding a material, and Find maps by name)

The guess recognizes, case not mattering, a name ending in one of these,
then `.png` or `.jpg`:

| Map | AmbientCG | Poly Haven | Common others |
|---|---|---|---|
| Color | `_Color` | `_diff_1k`, `_diff_2k`... | `_albedo`, `_basecolor`, `_base_color`, `_diffuse` |
| Normal (OpenGL) | `_NormalGL` | `_nor_gl_1k`... | `_normal`, `_normal_gl` |
| Normal (DirectX) | `_NormalDX` | `_nor_dx_1k`... | `_normal_dx` |
| Roughness | `_Roughness` | `_rough_1k`... | `_roughness` |
| Ambient occlusion | `_AmbientOcclusion` | `_ao_1k`... | `_ao`, `_occlusion` |
| Height | `_Displacement` | `_disp_1k`... | `_height`, `_displacement` |

- **Adding a material** (step 9) uses this for any image whose name has a
  color ending, not only `_Color.png`. Other images are still plain
  textures.
- **When it adds a material, it says what it found,** as Find maps by name
  does, so a missed map is noticed at once.
- **A plain `_normal`** is taken as OpenGL, the more common one for these
  sources; the user can switch it.

## Library

1. **`Material`:**
   - `Copy` and `CopyFrom(Material)`, for undo;
   - `FindTextureSetMaps(File colorMap, boolean replace) returns text`: the
     naming patterns above; fills the maps it finds (only empty ones unless
     `replace`) and returns what it found, for speaking;
   - `SetTextureSet(folder, name)` uses it, and **its comment is
     corrected**;
   - `HasMapOfDifferentSize(text path) returns text`, or a check in the
     editor: reads the image's size without packing (`PixelMap` load);
   - `MakePlain`: keeps the color map as the plain texture and clears the
     set's maps.
2. **`TerrainMaterial`:** `ReloadLayer(WorldTerrain, integer layer)`: gives
   the renderer the layer again even though its `Material` object is the
   same, for a map change.
3. **Sharing a `Material` by file:**
   - `TerrainMaterial:Load(File, HashTable<text, Material> materials)`:
     a material file already loaded for the scene is shared;
   - `DefaultSceneManager` keeps that table per scene load, next to its
     terrain material table (step 9);
   - the editor uses the same table when it adds or chooses a layer from a
     material file.
4. **The editor:**
   - new `SC/Dialogs/` part: the details area inside
     `TerrainMaterialsPropertiesDialog` (not a separate section: it belongs
     to one layer of this one), with a behavior for its buttons;
   - `SceneEditor`: `SetTerrainLayerMap(terrain, layer, mapKind, File)`,
     `ClearTerrainLayerMap`, `SetTerrainLayerNormalConvention`,
     `SetTerrainLayerValue(terrain, layer, valueKind, number)`,
     `RenameTerrainLayerMaterial`, `FindTerrainLayerMaps`,
     `MakeTerrainLayerPlain`. Each:
     - checks first (size, outside the project), and says why it refuses;
     - changes the `Material`, then `ReloadLayer` on every terrain whose
       terrain material uses that `Material`, and redraws them;
     - marks their materials changed for the next save (step 9);
     - adds one undoable edit.
   - **new `Interface/Undo/SceneEditorMaterialEdit`:** a copy of the
     `Material` from before and after; Undo and Redo copy it back and
     reload the layers that use it. Packed sets are shared, so undoing back
     to earlier maps doesn't pack again.
5. **Saving:** unchanged from step 9. Each changed material is written to
   its own file, with paths relative to it. Nothing is deleted.

**Studio:** nothing; the section is in the library's scene editor. No new
keys: the panel is reached with Cmd/Ctrl+P (step 6), and every control is
in its Tab order. No Help menu changes.

## Accessibility (step 12)

- Every map line is read as "Normal map: Rocks002_1K-PNG_NormalGL.png" or
  "Normal map: none", and its buttons as "Choose normal map" and "Clear
  normal map".
- Each change is said: "Roughness map is now Rocks002_Roughness.png.
  Packing the texture set... Done."
- Refusals are said with their reason.
- The details area's Tab order runs through its controls in the order
  shown, then back to the layer rows; Close details returns focus to the
  layer's Details... button.

## Risks

- **Texture places** (step 9): each new combination of maps uses up
  places for good. Choosing maps one by one, five times, packs five times.
  - Packing on field exit, not per key, keeps this down.
  - **Check first:** how many packs Studio takes before ground textures
    stop showing. If it's few, add an **Apply** button so several changes
    pack once.
- **Map sizes:** the size check reads each chosen image once; large 16-bit
  maps take a moment.
- **Sharing by file** changes how step 9's materials load; a scene saved
  before it loads the same, but edits now reach every layer using the file.

## Tests

- Add Poly Haven's `rock_wall` set by its `_diff_1k.png`: all five maps
  are found and said.
- Open Details for an AmbientCG layer: each map's file shows; clear the
  roughness map, set Roughness to 0.9: the ground looks rougher; undo puts
  the map back.
- Choose a roughness map of another size: refused, with both sizes; the
  material is unchanged.
- Switch the normal map to DirectX: the bumps light from the other side;
  undo.
- Two terrains sharing a material file: changing its height map in one
  changes both at once, and "Also used by..." says so.
- Save, reopen: the material file has the new maps, relative to it; a game
  loading the scene looks the same.
- With VoiceOver and NVDA: every line, button and value is read, and Close
  details returns to the row.
