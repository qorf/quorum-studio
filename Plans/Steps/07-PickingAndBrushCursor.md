# Step 7: picking the ground, and the brush cursor

Part of `../QuorumStudio.md`. It needs steps 3, 4 and 6. It's the
groundwork for every brush (steps 8 to 11), and it changes no heights.

## Status: applied 2026-10-20 (not built or run yet)

**Library (`MakeAWish`):**
- SC/Items/`EditorTerrain`: holds a `TerrainBrush`, the chosen tool
  (`SetToolNumber`, 1 to 9 as in the in-game editor, 0 for none; mapped to
  `TerrainBrush:SetTool`), and the brush's place (`SetBrushPosition`,
  `IsBrushOnGround`, `DescribeBrush`).
- SC/`SceneEditor`: `SetTerrainTool`, `IsTerrainBrushActive`,
  `MoveTerrainBrush` (relative to the camera, flat on the ground),
  `ResizeTerrainBrush` (×1.15 a step, 0.5 to 64), `MoveTerrainBrushToPointer`
  (the click's pick ray, then `TerrainData:RayCast`), the ring
  (`UpdateBrushPreview`: one `TerrainBrushPreview`, editor-tagged, placed
  only when the brush moved or resized), and the Brush section.
  Moving the brush by keys says the tool, place, ground height and size
  when the key is let go.
- **New** SC/`TerrainBrushMouseListener`: the ring follows the pointer.
- SC/Items/Highlights/`TerrainSelectionHighlight`: with a tool chosen, its
  move keys move the brush and its size keys size it; up and down do
  nothing. When the terrain stops being selected, the tool is left.
- **New** SC/Dialogs/`TerrainBrushPropertiesDialog`: the **Brush** section
  (between Terrain and Physics): a tool radio group (No tool, then the
  nine), and size, strength and falloff fields.
- **New** L/Interface/Behaviors/Scenes/Selection/`TerrainChooseToolBehavior`,
  bound to Alt+1 to Alt+9 (step 6's keys) in the selected-item table.
- `SelectionCancelBehavior`: Escape first leaves the tool.
- `SelectScene3DItemBehavior`: a click does nothing while a tool is
  chosen (reserved for strokes).
- `SelectionScaleBehavior`: no "Scaled up" message for a terrain.

**Differences from the plan:**
- **Every tool can be chosen,** though none strokes yet; choosing one shows
  the ring and moves it. Strokes come one tool at a time, from step 8.
- **The arrows move the brush smoothly while held,** like a selection,
  rather than a step per press as in the in-game editor.
- **Shift for 8 times as far** isn't done: Shift+arrows aren't bound in the
  scene editor, so it would need new keys (to ask about with step 8).
- **The pick ray** is the same overload the selection click uses, with no
  mirroring; test 1 below checks it.

## What the user sees

- **Choosing a tool:** select a terrain, then choose a tool in the
  Properties panel's new **Brush** section, or with Alt+1 to Alt+9
  (step 6). A ring then shows on the ground, the brush preview.
- **Moving it:**
  - **Mouse:** the ring follows the pointer over the ground.
  - **Keys:** the arrows move it across the ground, relative to the way the
    camera faces, as in the in-game editor. Each move says the ground's
    height there (step 12).
- **Size:** Alt+= and Alt+- size the ring.
- **Escape** leaves the tool. The ring goes away, and the arrows move the
  cursor again.

**No stroke yet:** Space and left drag do nothing until step 8.

## Library

- **The Brush section in the Properties panel** (`TerrainBrushProperties`,
  SC/Properties/):
  - a tool radio group, all disabled except the tools built so far;
  - size, strength and falloff (gamma) fields;
  - each tool's own settings appear as it's added.
- **`EditorTerrain`** holds a `TerrainBrush` and the chosen tool.
- **`TerrainSelectionHighlight`** (step 3) takes the keys:
  - with a tool chosen, its move behaviors move the brush instead of
    saying "a terrain stays at the origin";
  - the move is relative to the editor camera's yaw, as in
    `TerrainEditor.quorum`'s `HandleBrushPosition` (lines 938-981);
  - Shift moves 8 times as far.
- **Mouse picking:**
  - the editor camera's `GetPickRay(x, y, viewportX, viewportY,
    viewportWidth, viewportHeight)` overload, as
    `SelectScene3DItemBehavior` uses it;
  - then `TerrainData:RayCast(origin, direction, distance)`;
  - **note:** the in-game editor mirrors x for the full-window
    `GetPickRay` (TerrainEditor.quorum:1010-1013). Check whether the
    viewport overload needs the same.
- **The ring:**
  - one `TerrainBrushPreview` per editor;
  - added to the editor's 3D layer with the editor tag, so it isn't saved,
    listed in the tree, or picked;
  - placed only when the brush moved or changed size, as in
    TerrainEditor's `UpdatePreview` (lines 1313-1325).
- **Mouse events while a tool is chosen:** the `"SceneEditor"` table's left
  click goes to the terrain instead of selecting items. Left drag is
  reserved for step 8's strokes.

## Tests

- Choosing a tool shows the ring under the pointer; moving the mouse moves
  it; off the ground it hides.
- The arrows move it relative to the camera; turning the camera with the
  mouse changes which way "up" moves.
- Alt+= / Alt+- change its size; the panel's size field follows.
- Escape leaves the tool, and the arrows move the cursor again.
- With a screen reader, every move says the height there (step 12).
