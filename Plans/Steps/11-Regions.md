# Step 11: regions (add and remove)

Part of `../QuorumStudio.md`. It needs step 7.

## What exists (library, W/)

- **`TerrainTool:REGION`:** add a region where the brush is, or remove
  one.
- **`TerrainData`:**
  - `AddRegion` and `RemoveRegion`;
  - the grid is fixed at 32 × 32 locations, -16 to 15;
  - `ForgetRegionsNotLoaded` keeps removed regions out of `terrain.json`.
- **`WorldTerrain:UpdateRegionModels(layer)`** adds and removes the region
  models.

## What the user sees

- **Add region / Remove region tools** (Brush section, or Alt+8 and Alt+9):
  - Space or a click adds or removes the region under the ring;
  - it's spoken ("Added region 1, 0. The terrain is now 192 by 128").
- **The Terrain section** (step 5) gets "Grow by one region on every side",
  so a terrain can be made bigger without the brush.
- **Undo, Redo and Save** work as for the other tools.

## Library

- `EditorTerrain:AddRegionAt(x, z)` and `RemoveRegionAt(x, z)`, each an
  edit.
- `TerrainItem` keeps its region-model children in step with the data
  (`UpdateRegionModels` adds models to the item, not the layer).
- **On save,** a removed region's files are deleted from the terrain's
  folder. Today they're left behind (`Terrain/TierFour.md`; TerrainData
  never deletes them).
  - **For review:** deleting files from a project is new for the scene
    editor. The alternative is to leave them, and list them in
    `terrain.json` as unused.

## Risks

- **New regions outside the splat texture's area** make a new splat
  texture, which shows grey until a restart (step 9's risk). Check this
  first.
- **Grid limits:** adding past -16 or 15 says it can't, rather than doing
  nothing.

## Tests

- Adding a region beside the terrain shows a flat new region, and the size
  updates.
- Removing it makes it go.
- Undo and Redo.
- Save and reopen: the region count is kept, and a removed region's files
  are gone (or listed, by the decision above).
- A game loading the scene has the same regions.
