# Step 13: terrain in games (scenes, streaming, collision, cameras)

Part of `../QuorumStudio.md`. It needs step 2. Parts of it can be done any
time after that.

## What a game does with a terrain from Studio

```quorum
use Libraries.Game.Game
use Libraries.Game.Graphics.Worlds.TerrainItem

class Main is Game
    action Main
        UseVulkan(true)
        StartGame()
    end

    action CreateGame
        EnablePhysics3D(true)
        LoadScene("Scenes/World.qs")
        // The terrain is an item in the scene's 3D layer, found by its name.
    end
end
```

- **`LoadScene`** makes a `TerrainItem` with its collision and friction,
  as saved by the editor, and its material once step 9 adds them.
- **The terrain's `Update`** each frame makes splat layers ready (step 2).
  If `TerrainItem` does it itself through `Item3D`'s update, a game needs
  no code.
- **Finding the terrain:** the layer's items include it by name, as the
  `TerrainSceneCheck` program shows (2026-10-19 plan §14). A helper,
  `Game:GetTerrain(name)` or `Layer3D:FindItem(name)`, would make that
  easier (optional).

## Already in the library (2026-10-20), useful with terrain

- **`Camera:Follow(item, behind, above)`:** a chase camera that follows an
  item without jitter. It has separate getters and setters for distances,
  angle (90 is the right), aim height, turning, smoothing, and
  `JumpToFollowPosition` (`Terrain/CameraFollowPlan.md`).
- **`Game:UpdateAfterPhysics(seconds)`:** for things that follow physics
  items on screen (`Terrain/GameLoopAfterPhysics.md`).
- **Smooth physics drawing:** physics items are drawn between their last
  two steps (`Item3D:GetDrawnTransform`).
- **16-bit images load** (the new native image loader), so texture sets
  can use their 16-bit normal and height maps.

## To do

1. **Streaming terrains in scenes:** a large terrain, loaded around the
   player instead of all at once (`WorldTerrain:StartStreaming(folder,
   layer, x, z)` and `Update` each frame).
   - The scene entry gets a `"Streaming": true` option, with a draw radius.
   - `TerrainItem` then starts streaming instead of loading every region,
     and follows the camera, or a chosen item.
2. **Collision modes in scenes:**
   - `COLLISION_FULL` (every region, the default);
   - `COLLISION_DYNAMIC` (only near moving bodies, for large worlds);
   - `COLLISION_DISABLED`.
   - Step 5's "Collision" check box becomes these three choices once
     streaming exists.
3. **Studio's library copy:** games run from Studio use Studio's own copy
   of the standard library (`Library/Standard`, dated 2026-09-25), and the
   editor uses `Run/QuorumStandardLibrary.jar`. Both are rebuilt from
   `MakeAWish` before scenes with terrain run from Studio.
4. **AllTerrainVehicle's DriveOpen** could load its world from a scene made
   in Studio, as the real-world test. Optional, after steps 8 to 10.

## Tests

- **`TerrainSceneCheck`** (2026-10-19 plan §14): the cube lands on the
  Studio terrain.
- **A scene with materials and painting** (step 9) looks the same in the
  game as in the editor.
- **A car using `Camera:Follow`** drives on a Studio terrain without
  jitter.
- **Streaming** (once built): a 16 × 16-region terrain in a scene streams
  around the camera, and memory stays flat.
