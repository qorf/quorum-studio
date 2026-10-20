# Plans

Plans for Quorum Studio work.

## Terrain integration (started 2026-10-20)

**Start here: `QuorumStudio.md`,** the master plan:
- the goal and the decisions;
- what exists today, in the library and in Studio;
- the step list, the order of the steps, and their status;
- the risks to check first;
- corrections to the earlier plan.

### `Steps/`: one file per step

| File | Step |
|---|---|
| `01-WorldsInLibrary.md` | The Worlds terrain classes in the standard library (files moved) |
| `02-TerrainItem.md` | `TerrainItem`, terrain in `.qs` scene files, and no splat until painted |
| `03-SceneEditorTerrain.md` | Terrain in the scene editor: palette, selection, tree, saving |
| `04-PropertiesPanel.md` | A Properties panel docked on the right, for every scene item |
| `05-TerrainProperties.md` | The terrain's sections in the Properties panel: info and physics |
| `06-Keys.md` | Overridable scene-editor keys, and the terrain keys |
| `07-PickingAndBrushCursor.md` | Picking the ground with the mouse, and the brush ring and cursor |
| `08-SculptBrushes.md` | Raise, lower, smooth, flatten and ramp, one tool at a time |
| `09-MaterialsAndPainting.md` | Material files (plain `.json`), adding and removing materials on a terrain, and painting them |
| `10-WorldTools.md` | Presets, stamps, erosion and auto paint |
| `11-Regions.md` | Adding and removing regions |
| `12-Accessibility.md` | Describing the ground, and spoken values, with every step |
| `13-Games.md` | Terrain in games: scenes, streaming, collision, cameras |

### `Terrain/`: background documents

These are copies, made 2026-10-20, so they can be read beside the plan. The
originals live in the `physics` repository, and are newer if they differ.

| File | What it is | Original |
|---|---|---|
| `QuorumStudioPlan-2026-10-19.md` | The first Studio plan, with exact code for steps 2 and 3 (Item 71) and the first brush plan (Item 70). Its paths use `/Users/stefik/`, and its Studio branch is `Terrain`; the master plan has the corrections | `physics/QuorumStudio/QuorumStudioPlan.md` |
| `TierFour.md` | Open Worlds Tier 4: the editing tools (brush engine, undo, in-game editor), and what Studio has and lacks | `physics/AllTerrainVehicle/` |
| `TerrainEditorInstructions.md` | The user guide for the in-game terrain editor: every key and tool | `physics/AllTerrainVehicle/` |
| `OpenWorlds.md` | Open Worlds: the starting position, and the full feature list | `physics/AllTerrainVehicle/` |
| `OpenWorldsHandoff.md` | Open Worlds: the running handoff (state, priorities, open items) | `physics/AllTerrainVehicle/` |
| `OpenWorldTier1Handoff.md` | Tier 1: terrain data, regions and the height format | `physics/AllTerrainVehicle/` |
| `OpenWorldTier2Handoff.md` | Tier 2: texture splatting | `physics/AllTerrainVehicle/` |
| `OpenWorldTier3Handoff.md` | Tier 3: streaming, collision near the player, distant terrain | `physics/AllTerrainVehicle/` |
| `TerrainMaterialsPlan.md` | Terrain materials from AmbientCG texture sets | `physics/AllTerrainVehicle/` |
| `TextureSplattingPlan.md` | Texture splatting | `physics/AllTerrainVehicle/` |
| `GhostReconComparison.md` | Comparison with a large commercial terrain, and missing features | `physics/AllTerrainVehicle/` |
| `WaterPlan.md` | Water effects (not built yet) | `physics/AllTerrainVehicle/` |
| `Item42.md` | The ground look: detiling, macro variation, cliffs (for review, not applied) | `physics/AllTerrainVehicle/` |
| `Item43.md` | Sea level and lakes (for review, not applied) | `physics/AllTerrainVehicle/` |
| `CameraFollowPlan.md` | `Camera:Follow` (applied 2026-10-20) | `physics/AllTerrainVehicle/` |
| `GameLoopAfterPhysics.md` | `Game:UpdateAfterPhysics`, and smooth physics drawing (applied 2026-10-20) | `physics/AllTerrainVehicle/` |
