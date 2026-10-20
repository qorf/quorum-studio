# Step 5: the terrain's sections in the Properties panel

Part of `../QuorumStudio.md`. It needs steps 3 and 4.

**Revised 2026-10-20:** a new terrain has no materials (step 2), so this
step shows only what a flat terrain has: its info and its physics.
Choosing materials, adding and removing them, and painting are step 9,
which adds a **Materials** section to the panel.

## What the user sees

With a terrain selected, the Properties panel shows these sections, after
"Basic" (the name only, since a terrain has no position, rotation or
scale):

1. **Terrain** (read only):
   - size in world units (for example "128 by 128");
   - region count, region size in samples, and sample spacing;
   - lowest and highest ground;
   - the folder its files are in (for example `Terrains/Terrain1`).
2. **Physics** (in games; the editor never collides):
   - **Collision:** on or off. This is the scene's `"Physics Enabled"`.
   - **Friction.**

Changes are applied when a field's edit is committed (Enter, or leaving
the field), and saved with the scene (step 2's `"Physics Enabled"` and
`"Friction"`).

## Library

- **New sections** (SC/Properties/): `TerrainInfoProperties` and
  `TerrainPhysicsProperties`. Each `AppliesTo` an `EditorTerrain`.
- **`SceneEditor`** adds them to its panel when the panel is set.
- **The Terrain section's values** come from `TerrainData`:
  - `GetRegionCount`, `GetRegionSize` and `GetVertexSpacing`;
  - `GetLowestHeight` and `GetHighestHeight`;
  - the size, from the regions' extent. A small helper, `GetWorldMinimumX`
    and so on, belongs in `TerrainData`. TerrainEditor.quorum:791-855 has
    one, but it isn't in the library.
- **Step 10 and later** keep the Terrain section current as heights
  change.

## Tests

- A new terrain shows **Terrain** with "128 by 128", 4 regions, 64 samples,
  spacing 1, heights 0 to 0, and its folder.
- **Physics:**
  - collision off, then save: a cube in a game falls through;
  - friction 0.1: a cube slides further;
  - reopening the scene shows the same values.
- **With a screen reader,** each field reads its label and value.
