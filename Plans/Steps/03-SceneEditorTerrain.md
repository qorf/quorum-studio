# Step 3: terrain in the scene editor

Part of `../QuorumStudio.md`. It needs step 2.

## Status: applied 2026-10-20 (not built or run yet)

**Library (`MakeAWish`):**
- **New:**
  - SC/Blueprints/`TerrainBlueprint`;
  - SC/Items/`EditorTerrain`;
  - SC/Items/Highlights/`TerrainSelectionHighlight`.
- **Changed:**
  - SC/`SceneEditor`: `AddTerrain`, `FindLayer3D`, `UpdateTerrains`,
    `SaveChangedTerrains`, the `CreateItemPreview` branch, `CreateItem3D`,
    `SetFilteredItems`, `Save`, `Update`;
  - SC/`ScenePalette`: the Terrain entry, and `SetDefaultTerrainThumbnail`;
  - SC/`SceneTree`: a terrain's region models aren't listed;
  - SC/Items/`EditorCursor3D`: Enter finds a terrain by its ground;
  - L/Interface/Behaviors/Scenes/Selection/`SelectScene3DItemBehavior`:
    a click finds a terrain by `RayCast`;
  - L/Interface/Behaviors/Scenes/Palette/`SceneEditorAcceptDragBehavior`
    and `PaletteItemStartDragBehavior`: drag and drop.

**Studio (added 2026-10-20, after the first try):**
- **Why:** Terrain didn't appear in the palette. The rows show only an
  icon, and every other entry has a thumbnail image, so Terrain was the
  only one using the palette's default icon. That's a pink square drawn by
  `LoadFilledRectangle`, which apparently doesn't show under Vulkan.
- **What changed:**
  - `QS/../Resources/Graphics/SceneEditor/Primitives3D/TerrainThumbnail.png`
    (80 × 80, in the style of the other thumbnails);
  - `TERRAIN_THUMBNAIL` in `Resources.quorum`;
  - `scenePalette:SetDefaultTerrainThumbnail(...)` in `QuorumStudio.quorum`.
- **Still open (library):** `ScenePalette:LoadDefaultIcon` under Vulkan.
  Any blueprint without a thumbnail would be invisible in the palette.

**Three more corrections, found while building:**
1. **`EditorTerrain` is not an `EditorItem`.** `EditorItem` is the base for
   the editor's own helpers (highlights, previews, the cursor). It tags
   itself with `EDITOR_TAG` in `on create`, and the editor never saves,
   lists or picks tagged items. Real scene items (`EditorModel`,
   `EditorPointLight`, `EditorAudio3D`) aren't `EditorItem`s.
   `EditorTerrain` is a `TerrainItem` with the editor's link, a changed
   flag (`SaveIfChanged`), and its extent (`GetMinimumX`, ...).
2. **Drag and drop adds the terrain only on drop.**
   `SceneEditorAcceptDragBehavior` calls `CreateItemPreview` as soon as a
   drag enters the scene. That would have added a terrain before the drop,
   which dragging off couldn't take back.
   - The editor's drag behavior now handles `TerrainBlueprint` itself:
     the icon follows the pointer, and `DROPPED` calls `AddTerrain`.
   - The 2026-10-19 plan's change to `PaletteItemDropBehavior` is **not**
     made: with it, a drop would have added two terrains. That behavior's
     existing `else` branch already clears the dragged icon.
3. **The highlight is a box, not an outline.** It's a padded box over the
   terrain's extent and height range, as the editor does for planes. A
   flat terrain gets a thin slab, which needs no mesh code. Region models
   don't need the editor tag either: the click test, the cursor test and
   the tree only look at a layer's top-level items, and the tree skips a
   terrain's children.

**Behavior notes:**
- **Clicking:** the ground wins over a box item only if it's hit before
  that item's center, so a cube standing on the ground is still picked.
- **Opening a scene with a terrain** gives an `EditorTerrain`, through the
  editor's `CreateItem3D`.
- **Saving the scene** saves the files of terrains marked changed. Nothing
  marks them yet; steps 8 to 11 will.

**The base code** is in `../Terrain/QuorumStudioPlan-2026-10-19.md`:
- §6: `TerrainBlueprint`;
- §7: `EditorTerrain`;
- §8: `SceneEditor:AddTerrain`, the `CreateItemPreview` branch,
  `CreateItem3D` and `FindLayer3D`;
- §9: the palette entry in "Basic 3D";
- §10 and §11: drag and drop;
- §12: the scene tree, which hides the region models;
- §13: the Studio icon.

The additions below are corrections found on 2026-10-20.

## What the user sees

- **Adding one:** **Terrain** is in the palette's "Basic 3D" group.
  Activating it (Enter) or dragging it into the scene adds a flat terrain
  at once:
  - 2 × 2 regions of 64 samples, 1 unit apart, so 128 × 128 units centered
    on the origin;
  - saved into the project in `Scenes/Terrains/Terrain1/` (then `Terrain2`, ...);
  - selected, and announced.
- **Selecting it:** from the scene tree, by clicking the ground, or with
  the cursor's Enter.
- **Delete, Undo and Redo** work as for any item.
- **The move, turn and scale keys** do nothing to it, and say why.
- **It can't be added** without Vulkan, or in a scene that isn't in a
  project. The editor says why.

**Keys:** none new. These keys keep their meanings: the palette's Enter,
drag and drop, the tree, Delete, Undo, Redo, Escape, the mouse camera, and
Cmd/Ctrl+P (focus the properties).

## Corrections and additions to the 2026-10-19 plan

### A. A highlight for a selected terrain

`SceneEditor:SetFilteredItems` (SC/SceneEditor.quorum:769) puts a
highlight around each selected 3D item. It casts the item to
`EditorModel`:

```
                highlight:Load(cast(EditorModel, item), me)
```

`EditorTerrain` isn't an `EditorModel`, so selecting a terrain would stop
with a cast error.

**New SC/Items/Highlights/`TerrainSelectionHighlight.quorum`**
(`is SelectionHighlight3D`):
- `Load(EditorTerrain, SceneEditor)` keeps the terrain, and draws a low
  outline at the terrain's edges, at its lowest height. A box over the
  whole terrain would hide it.
- `MoveX`, `MoveY`, `MoveZ`, `Move` and the turn and scale paths do
  nothing to the terrain. The first time, they say "A terrain stays at the
  origin."
- `GetItem` returns the terrain, so the tree, the Properties panel (step 4)
  and Delete see the terrain.
- Steps 7 to 11 build on it: with a brush tool chosen, the arrow keys move
  the brush.

**In `SetFilteredItems`**, a new branch before `else`:

```
                elseif item is EditorTerrain
                    TerrainSelectionHighlight terrainHighlight
                    terrainHighlight:LoadTerrain(cast(EditorTerrain, item), me)
                    highlight = terrainHighlight
```

The existing `highlight:Load(cast(EditorModel, item), me)` line moves into
the other branches, so it's never reached for a terrain.

**For review:** whether the outline is drawn as a `Model` mesh (like
`TerrainBrushPreview`'s rings) or as four thin boxes. The rings' code makes
a mesh outline easy.

### B. Clicking the ground selects the terrain, and region models stay out of item lists

- **Clicking in the 3D view:** `SelectScene3DItemBehavior` tests every
  non-editor item's box in the layer. For a terrain, that box covers all
  its regions, so a click anywhere above the ground would select it, even
  in front of other items.
  - **Change:** the behavior skips `TerrainItem`s in its box test. It then
    also asks each terrain `GetWorldTerrain():GetData():RayCast(origin,
    direction, distance)`.
  - The nearest hit, whether box or ground, wins.
- **The cursor's Enter** (`EditorCursor3D:GetItemsAtCursor`): a terrain is
  "at the cursor" when the cursor is within its regions and close to the
  ground height there (`TerrainData:GetHeight`), not by its box.
- **Region models** (the terrain's children) are never offered as items:
  - the tree already skips them (§12);
  - the click test and cursor test only look at the layer's top-level
    items;
  - region models get the editor tag (`SceneConstants:EDITOR_TAG`) as a
    second guard.

### C. Updating the terrain each frame

`EditorLayer3D`'s update calls `Update(layer, cameraX, cameraZ)` on each
`EditorTerrain`, with the editor camera's position:
- splat layers become ready;
- streaming, if a scene ever uses it, follows the view.

This is cheap when nothing changes.

### D. Saving

From step 5 on, a terrain can change in the editor. `SceneEditor:Save`
calls `SaveTerrain` on each `EditorTerrain` marked changed, then writes the
`.qs`.
- `EditorTerrain` gets a `changed` flag, which the brush and generator
  steps set.
- **Studio has no dirty flag** for visual tabs (from `TierFour.md`). The
  changed flag doubles as a reason to show the tab as unsaved, later.

### E. Vulkan and the texture-group check

- `AddTerrain` checks for Vulkan, as planned.
- **A new terrain has no textures** (step 2), so the texture-group-0 check
  from Stage 0 waits for step 9's first material.

## Files

- **Library:**
  - new: `TerrainBlueprint`, `EditorTerrain`, `TerrainSelectionHighlight`;
  - changed: `SceneEditor` (`AddTerrain`, `FindLayer3D`, the
    `CreateItemPreview` branch, `CreateItem3D`, `SetFilteredItems`,
    `Save`), `ScenePalette`, `SceneTree`, `EditorLayer3D`,
    `PaletteItemStartDragBehavior`, `PaletteItemDropBehavior`,
    `SelectScene3DItemBehavior`, `EditorCursor3D`.
- **Studio:** only the optional palette icon (`Resources`,
  `QuorumStudio.quorum`, an image).

## Tests

- **The 2026-10-19 plan's Studio list (§ "Test plan" 3 to 7):** palette,
  drag and drop, tree, move keys, undo, delete, save and reopen, no
  project, a screen reader.
- **Selection:**
  - from the tree: the highlight outline shows, and there's no error;
  - clicking the ground selects the terrain;
  - clicking a cube standing on the ground selects the cube.
- **The cursor's Enter** at ground level selects the terrain; well above
  the ground it doesn't.
- **A new terrain** draws flat and plain, and its folder has no splat
  images.
