# Step 8: sculpt brushes (raise, lower, smooth, flatten, ramp)

Part of `../QuorumStudio.md`. It needs step 7.

**One tool at a time** (the user's rule): each tool is its own reviewed
write-up. **Raise and lower come first** (recommended). Smooth, flatten and
ramp follow, in that order.

## Status: raise and lower applied 2026-10-20 (not built or run yet)

**Library (`MakeAWish`):**
- **`SceneEditor`:** `BeginTerrainStroke(terrain, byMouse, key)`,
  `EndTerrainStroke`, `CancelTerrainStroke`, `IsTerrainStroking`,
  `CanStrokeWithTool` (1 and 2 only), `ChangeTerrainStrength`, and
  `TerrainEdited` (called after an undo or redo). A stroke is kept going
  from `Update`, once a frame, while its key (polled with an
  `InputMonitor`) or the left mouse button is held; each frame applies the
  stroke and calls `WorldTerrain:Rebuild()`. At its end the stroke goes
  into the edit manager, the terrain is marked changed, and the Terrain
  section's heights are refreshed. Choosing another tool, or losing the
  selection, ends a stroke first.
- **New `Interface/Undo/SceneEditorTerrainEdit`:** wraps the stroke's
  `TerrainStrokeEdit`, and has the editor redraw the terrain after Undo and
  Redo.
- **New behaviors** (`Behaviors/Scenes/Selection`): `TerrainBrushUseBehavior`
  (Space by default) and `TerrainStrengthBehavior` (Alt+] and Alt+[), bound
  in the selection table from `SceneEditorKeys`, so Studio's key map
  overrides them.
- **`SelectScene3DItemBehavior`:** with a tool chosen, a left click moves
  the brush under the pointer and starts a stroke.
- **`TerrainBrushMouseListener`:** the brush follows the pointer while
  dragging too.
- **`SelectionCancelBehavior`:** Escape cancels a stroke first, then leaves
  the tool.

**Studio:** nothing new; the keys were already in the key map (step 6).

**Not done:**
- the edit manager's memory cap (small terrains don't need it yet);
- the selection border isn't reshaped after a stroke, so ground raised at
  the terrain's edge can cover it until the terrain is selected again;
- the frame budget is still to be measured (see Risks).

## What exists (library, W/)

- **`TerrainTool`:** `SCULPT` with `ADD` (raise) and `SUBTRACT` (lower),
  `AVERAGE` (smooth), `REPLACE` (flatten) and `GRADIENT` (ramp).
- **`TerrainBrush`:** size, strength, gamma, slope limits, flatten height,
  and ramp points.
- **`TerrainStroke`:**
  - `Begin(data, brush)` → `Apply(x, z, seconds)` each frame → `End()`,
    which returns a `TerrainStrokeEdit` (or undefined if nothing changed);
  - `Cancel()`.
- **`TerrainStrokeEdit is Edit`:** whole-region copies from before and
  after, so it goes straight into an edit manager.
- **After a change:**
  - `WorldTerrain:UpdateRegionModels(layer)` and `Rebuild()` redraw it;
  - TerrainEditor's `RedrawAfterEdit` (lines 1165-1173) is the recipe.

## What the user sees (raise and lower)

- **Doing a stroke:** with Raise or Lower chosen, hold **Space**, or drag
  with the left mouse button. The ground under the ring rises or sinks
  while it's held. The view updates as it goes.
- **Strength:** Alt+] and Alt+[ change it (step 6), or use the panel.
- **Escape during a stroke** cancels it and puts the ground back.
- **Undo** takes the whole stroke back, and **Redo** puts it back.
- **Saving the scene** saves the terrain's files (step 3 D).

## Library

- **`EditorTerrain`:** `BeginStroke`, `ApplyStroke(x, z, seconds)` (called
  each frame from the editor's update while held), `EndStroke` and
  `CancelStroke`.
- **The ending edit** goes into the scene editor's `SceneEditorEditManager`,
  so Studio's Undo and Redo work unchanged (`QS/Behaviors/Interface/Edit/UndoBehavior.quorum:28-33`).
  - **Undo or redo of a terrain edit** also redraws the terrain.
    `TerrainStrokeEdit`'s Undo and Redo only change the data, so the
    editor redraws after a terrain edit is undone or redone, through a
    small hook or a wrapping edit.
- **Memory:**
  - `SceneEditorEditManager` has no limit, and a stroke copies whole
    regions. A 64-sample region is about 32 KB per copy, which is small.
  - With larger terrains, add `TerrainEditHistory`'s memory cap (256 MB by
    default) to the edit manager, dropping the oldest terrain edits first.
- **The input:**
  - Space (hold) is the new "use the brush" key (step 6);
  - left drag over the ground does the same while a tool is chosen.
- **Marks the terrain changed** for saving.

## Risks

**Frame budget** (Stage 0, check 4): each frame of a stroke rebuilds the
touched regions within the frame, which stalls all of Studio.
- **Measure it** with a 2 × 2 terrain of 64 samples, and with 4 × 4 of
  128.
- **If it's slow:** rebuild once every few frames during a stroke, and
  fully at its end.

## Tests (raise and lower)

- Holding Space raises the ground under the ring; the panel's heights and
  the spoken height follow.
- Left drag raises along the path.
- Lower sinks the ground.
- Escape during a stroke puts the ground back as it was.
- Undo removes the stroke; Redo puts it back; both redraw.
- Save, close and reopen: the shape is kept. A game loading the scene has
  the same ground, and a cube lands on the hill.

## The next tools (each a short write-up when its turn comes)

- **Smooth** (`AVERAGE`): no new keys.
- **Flatten** (`REPLACE`):
  - settings: "to the height at the stroke's start" or "to a set height";
  - "take the height under the brush" (the in-game editor's T; Alt+T is
    suggested in step 6, and asked about then).
- **Ramp** (`GRADIENT`): a start point and an end point (the in-game
  editor's R, R; Alt+R is suggested, and asked about then).
- **Slope limit** (any, under 15°, over 25°): a panel setting, and maybe a
  key (Alt+G suggested).
