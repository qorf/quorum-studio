# Step 1: the Worlds classes in the standard library

Part of `../QuorumStudio.md`.

## Status

- **Done (2026-10-20):** the 18 files were copied byte for byte from
  `AT/SourceCode/Worlds` into W/, and removed from the project with
  `git rm`. The project's programs now use the library's copies, since the
  package (`Libraries.Game.Graphics.Worlds`) didn't change.
- **Not committed yet** in either repository.

The files: `TerrainBrush`, `TerrainBrushPreview`, `TerrainCollision`,
`TerrainCollisionPiece`, `TerrainData`, `TerrainEditHistory`,
`TerrainGenerator`, `TerrainPatch`, `TerrainRegion`,
`TerrainRegionFarModel`, `TerrainRegionModel`,
`TerrainSplatFragmentShader`, `TerrainSplatShaderProgram`,
`TerrainStreamer`, `TerrainStroke`, `TerrainStrokeEdit`, `TerrainTool`,
`WorldTerrain`.

**Checked before the move:**
- **No name clashes:** none of the 18 class names exist elsewhere in the
  library, including the library's own `Terrain` and `TextureSplat` in W/.
- **No project code:** every `use` names a library class or another Worlds
  class.
- **No project files:** the only file names in the classes are the
  terrain's own save files, in a folder the caller chooses.
- **No index to update:** nothing in the library lists its classes.
- **Only AllTerrainVehicle** used these classes.

**The folder fix: applied 2026-10-20** (W/TerrainData.quorum,
`GetFileInFolder`).
- A file in a folder now keeps the folder's working directory, so a
  terrain given relative to a Studio project finds its files. It's needed
  by step 2.
- Every file `TerrainData` reads or writes in a folder goes through this
  action: settings, and region and splat images, both ways. Nothing else
  builds paths from the folder.
- Today's games are unaffected: a folder made with `SetPath` alone keeps
  the default working directory.
- A folder with an empty path now gives just the file's name, not
  `"/name"`.

## Remaining

1. ~~The folder fix~~ (done, above).
2. **Build and run** (the user):
   - the standard library;
   - AllTerrainVehicle;
   - `RunWorldTests` (all tests passing);
   - DriveOpen and TerrainEditor, unchanged.
3. **Commit:** the library (`MakeAWish`), and the project's removal of
   `SourceCode/Worlds` (`physics`).
4. **Studio's runtime library** (`S/Run/QuorumStandardLibrary.jar`) and
   Studio's copy of the standard library are rebuilt from `MakeAWish`
   before step 3 can be tried in Studio.

## Later

- **The tests** (`AT/SourceCode/Tests/Worlds`, `RunWorldTests`) could move
  into the library's own test suite.
- **The water classes** from Items 39 to 43, once applied, go into W/, not
  the project.
