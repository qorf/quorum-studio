# Step 10: world tools (presets, stamps, erosion, auto paint)

Part of `../QuorumStudio.md`. Generating the whole world needs only steps
3 to 5. The parts that work under the brush need step 7.

**Auto paint needs materials:** it paints the terrain's layers (step 9).
Without them, the auto-paint options are disabled, and the reason is
given.

## Status: applied 2026-10-20 (not built or run yet)

- **New `SC/Dialogs/TerrainGeneratePropertiesDialog`** (the Generate
  section, after Brush) with `Behaviors/Scenes/Properties/TerrainGenerateButtonBehavior`:
  - for the whole terrain: preset (radio), seed with "New seed", height,
    "Auto paint afterward", Generate, "Erode whole terrain", "Auto paint
    whole terrain";
  - **at the brush** (placed with any tool): stamp shape, stamp mode
    ("Usual for the shape", add, subtract, raise), stamp height, turn,
    "Stamp here", "Erode here", "Auto paint here". These are in this
    section rather than the Brush section, which is long already. No keys.
- **`SceneEditor`:** `GenerateTerrain`, `ErodeTerrain`, `AutoPaintTerrain`,
  `StampTerrain`, `ErodeTerrainAtBrush`, `AutoPaintTerrainAtBrush`, each a
  `TerrainStroke` operation that goes into the undo list as a
  `SceneEditorTerrainEdit`; `GetTerrainGenerator`.
- **Auto paint and fewer than four materials:** `TerrainData:FoldSplatLayers`
  gives the rules for missing layers to layer 1. Layer 1 is flat ground,
  2 slopes, 3 hollows, 4 low ground. It needs two or more materials.
- **`EditorTerrain:GetSampleEdge`** for the whole terrain's samples.

## What exists (library, W/`TerrainGenerator`)

- **Presets** for the whole world: rolling hills, mountains, plateaus,
  canyons and flat, with a seed and a height
  (`GeneratePreset(data, preset, startX, startZ, width, depth, height)`).
- **Stamps:**
  - shapes: hill, crater, mesa, ridge and others (`MakeStampShape`,
    `Stamp`);
  - modes: add, subtract and raise;
  - settings: a height and a turn.
- **Erosion:** water erosion (`Erode`, `ErodeWithRain`) and settling
  (`Settle`), under the brush or over the whole world.
- **Auto paint:** layers by height and slope (`SetPaintRules`,
  `AutoPaint`). Grass on flat ground, rock on slopes, dirt in hollows,
  sand low down.
- **Undo for them:** `TerrainStroke:BeginOperation(data, name,
  changesShape, changesSplat)` + `CopyArea(...)` + `End()` turns any
  operation into a `TerrainStrokeEdit`. The in-game editor's
  `StartOperation` and `FinishOperation` (TerrainEditor.quorum:599-625)
  are the recipe.

## What the user sees

**A Generate section in the Properties panel** (a terrain selected):
- **Preset** (radio group), **seed** (a number; "New seed" picks one at
  random), and **height** in world units.
- **Generate:** remakes the whole terrain's heights with the preset.
  - With **auto paint** on (a check box), it then paints the layers too.
  - It says when it's done ("Generated rolling hills, heights 0 to 32").
- **Erode whole world** and **Auto paint whole world:** buttons.

All of these can be undone as one edit each, and are saved with the scene.

**Under the brush** (a tool chosen, step 7):
- **Stamp:** the panel's stamp shape, mode, height and turn, pressed at
  the ring, as big as the ring. The in-game editor uses Enter; in Studio,
  Enter already selects at the cursor, so the panel's "Stamp here" button
  is used, or a key is asked about.
- **Erode here** and **auto paint here:** panel buttons. Keys (the in-game
  editor's X and U) only if asked.

## Library

- **New section `TerrainGenerateProperties`,** and stamp settings in the
  Brush section.
- **`EditorTerrain`:** `Generate(preset, seed, height, autoPaint)`,
  `ErodeAll`, `AutoPaintAll`, `StampAt(x, z)`, `ErodeAt(x, z)` and
  `AutoPaintAt(x, z)`. Each is wrapped in `BeginOperation` and `End`, and
  added to the edit manager.
- **Buttons:** the scene editor has buttons in its dialogs already, and
  they work with Enter and Space when focused.

## Risks

- **Time:** generating a large world, or eroding it, takes seconds and
  stalls Studio. That's fine for buttons. Say "Generating..." first, and
  the result after.
- **Undo size:** a whole-world operation copies every region.

## Tests

- Generate mountains with seed 7: the heights change, the Terrain section's
  height range updates, and it's spoken.
- Generate again with seed 7: the same world.
- Undo puts the flat world back.
- Auto paint after adding two materials in step 9: rock on the slopes.
- A stamp at the ring: a hill appears there; undo removes it.
- Save, reopen and the game keep it all.
