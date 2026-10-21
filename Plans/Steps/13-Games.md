# Step 13: terrain in games (scenes, streaming, collision, cameras)

Part of `../QuorumStudio.md`. It needs step 2. Parts of it can be done any
time after that.

## Status: applied 2026-10-20 (not built or run yet)

- **`TerrainItem` updates itself:** its `Update(seconds)`, which the layer
  calls each frame, makes ground textures ready and keeps a streaming
  terrain around the camera, or an item set with `SetFollowItem`. A game
  needs no terrain code. (`EditorTerrain` overrides it to do nothing; the
  editor updates its terrains itself.)
- **Collision modes:** `TerrainItem:SetCollisionMode`/`GetCollisionMode`
  (WorldTerrain's FULL, DYNAMIC, DISABLED); `SetCollision(boolean)` maps to
  FULL or DISABLED. The scene has `"Collision": "Full"`, `"Dynamic"` or
  `"Disabled"`, and still `"Physics Enabled"` for older readers.
- **Streaming:** `TerrainItem:SetStreaming(stream, radius)` before `Load`;
  `Load` then only notes the folder, and the first update calls
  `WorldTerrain:StartStreaming`. Saving a streaming terrain keeps unloaded
  regions listed. The scene has `"Streaming": true` and `"Draw Radius"`,
  written only for a streaming terrain.
- **The Physics section:** collision is three choices (every region, only
  near moving bodies, none); "Stream regions in games" and a draw radius.
  `EditorTerrain` keeps these for the scene and never streams or collides.
- **The drive test is in QuincyWorld:** `SourceCode/Main.quorum` loads the
  scene, finds its terrain, and drives the toy car on it with
  `Camera:Follow` (arrows drive, Space brakes, R resets). From
  AllTerrainVehicle, only the car came: `Car/ToyCarCentered.gltf` and the
  nine files it names, and DriveOpen's chassis, wheel and reset code.
  AllTerrainVehicle is unchanged.
- **The car is steadier than DriveOpen's** (the user found it flipped
  easily): wheels 1.6 to each side instead of 1.12, roll influence 0,
  friction slip 6, steering 0.35, angular damping 0.9. The numbers are
  fields at the top of `Main`, for tuning.

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
3. **The drive test:** done in QuincyWorld instead (see Status), moving
   only the car from AllTerrainVehicle (the user, 2026-10-20).

**Studio's copy of the library isn't part of this plan:** Quorum Studio
already manages it (the user, 2026-10-20).

**The test project** for terrain in games is
`/Users/stefik/Repositories/worlds/QuincyWorld` (the user, 2026-10-20). Its
`Main.quorum` loads `Scenes/Scene.qs`, which has one terrain in
`Scenes/Terrains/Terrain1`.

## Tests

- **`TerrainSceneCheck`** (2026-10-19 plan §14): the cube lands on the
  Studio terrain.
- **A scene with materials and painting** (step 9) looks the same in the
  game as in the editor.
- **A car using `Camera:Follow`** drives on a Studio terrain without
  jitter.
- **Streaming** (once built): a 16 × 16-region terrain in a scene streams
  around the camera, and memory stays flat.
