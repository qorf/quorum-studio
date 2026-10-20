# A game hook after physics

**Applied 2026-10-20: Option A.**
- **`Game.quorum`:** `UpdateAfterPhysics` is added, and `ContinueGame`
  calls it after `TestForCollisions`.
- **DriveOpen:** its camera, headlights and lamp moved into an
  `UpdateAfterPhysics` override, and `VehicleFollower` was deleted.
- **TerrainWalk:** not changed. Its `CameraFollower` source is missing.

Option B was not applied.

**Confirmed on screen (2026-10-20):** DriveOpen's jitter is gone, both
from the interpolation fix and from placing the camera after physics. The
camera is now placed by `Camera:Follow` (`CameraFollowPlan.md`), and the
headlights and lamp by `UpdateAfterPhysics`. Recorded as I and J in
`VulkanChanges.md`.

## Background

DriveOpen's car jittered. There were two causes:
- **No interpolation (fixed in the library on 2026-10-20).** Bodies were
  drawn at their last fixed 60 Hz step. With uncapped Vulkan frame rates,
  some frames have a step and some don't, so motion came in uneven jumps.
  Each awake body now gets a drawn pose between its last two steps
  (`Item3D:GetDrawnTransform`), as Bullet does. It's worked out once a
  frame in `Layer3D:SetScreenPositionsFromPhysicsPositions`, in the same
  loop that copies poses for drawing, and not after every physics step.
- **The camera was placed before physics.** Games place cameras in
  `Update`, which runs before the physics step. The camera aims at where the
  car was, then physics moves the car, then the frame is drawn. DriveOpen now
  works around this with `VehicleFollower`, a `PhysicsBehavior3D` whose
  `UpdateAfterPhysics` places the camera. TerrainWalk did the same thing
  earlier.

The second cause is a design question about the library. Every game with a
chase camera, or anything else that follows a physics body, will run into
it. Each one currently has to find a workaround like `VehicleFollower` on
its own.

## The game loop today

`Game:ContinueGame` (Game.quorum, about line 831):

```
gameInput:ProcessInputEvents()
UpdateAll()                 // each layer: item and behavior Update(seconds)
Update(seconds)             // the game's Update
if collide
    TestForCollisions(seconds)
        // per layer: StepPhysics (0..n fixed steps, then the drawn poses),
        // SetScreenPositionsFromPhysicsPositions,
        // UpdateBehaviorsAfterPhysics (PhysicsBehavior3D:UpdateAfterPhysics),
        // collision boxes and render tree
end
accessibility:Update()
ClearScreen()
DrawAll()
```

So the game's `Update` sees the world as it was **before** this frame's
physics. Anything it places from a physics body's position is one frame
behind the body as drawn.

How other engines order this, from general knowledge:
- **Unity:** physics (`FixedUpdate`), then `Update`, then `LateUpdate`, the
  documented place for cameras, then rendering.
- **Godot:** `_physics_process`, then `_process`, then drawing.
- **Unreal:** tick groups run before, during and after physics, and the
  camera manager updates after physics.

In all three, game code that places a camera runs after physics. Quorum is
the exception.

## Options

### A. Add `Game:UpdateAfterPhysics(number seconds)` (recommended)

This is a new action, empty by default, which `ContinueGame` calls once a
frame after `TestForCollisions` and before drawing:

```quorum
    action ContinueGame
        gameInput:ProcessInputEvents()
        UpdateAll()

        number seconds = GetSecondsBetweenFrames()
        Update(seconds)

        if collide
            TestForCollisions(seconds)
        end
        UpdateAfterPhysics(seconds)

        if accessibility not= undefined
            accessibility:Update()
        end

        ClearScreen()
        DrawAll()
        ProcessScreenshots()
    end

    /*
    This action is called once a frame, after physics has run and every
    item is where it will be drawn, even on frames with no physics step.
    Override it to place anything that should follow physics items on
    screen, such as a camera chasing a car or a light carried by a body.
    Changing physics here (forces, velocities, positions) takes effect in
    the next frame's physics step. By default it does nothing.

    Attribute: Parameter seconds The time since the last frame, in seconds.

    Attribute: Example

    use Libraries.Game.Game
    use Libraries.Game.Graphics.Model

    class Main is Game
        Model ball

        action Main
            StartGame()
        end

        action CreateGame
            ball:LoadBox(1, 1, 1)
            ball:EnablePhysics(true)
            Add(ball)
        end

        action UpdateAfterPhysics(number seconds)
            GetCamera3D():SetPosition(ball:GetX(), ball:GetY() + 5, ball:GetZ() - 10)
            GetCamera3D():LookAt(ball:GetX(), ball:GetY(), ball:GetZ())
        end
    end
    */
    action UpdateAfterPhysics(number seconds)
    end
```

What this changes in the library:
- **Game.quorum:** the new action, and one call in `ContinueGame`. It's
  called whether or not `collide` is on, so the action means "after
  physics, if there was any".
- **No behavior change** for existing games. Their `Update` runs exactly
  where it does now.
- **Documentation:** the Game class's loop description (Game.quorum, about
  line 262, "Repeatedly calls Update()") gains a step. Camera examples in the
  docs and tutorials can move to `UpdateAfterPhysics`.
- **Optional:** a matching action on `Layer`, for layer-level work after
  physics. `PhysicsBehavior3D:UpdateAfterPhysics` already covers this for
  3D, and Layer2D has no equivalent, so this can wait.

What games change:
- DriveOpen: delete `VehicleFollower` and move `FollowVehicle`'s body into
  `UpdateAfterPhysics`. TerrainWalk the same.
- **Input stays as responsive as today.** Driving and forces still go in
  `Update`, before the step, so they act in the same frame.

Cost: one empty call per frame.

### B. Move `Update` itself after physics

This is the Unity and Godot order: `UpdateAll`, then physics, then `Update`,
then drawing.

What it would change:
- **Fixes every chase camera with no code changes.**
- **Adds one frame of latency to physics input.** Forces, impulses,
  velocities and steering set in `Update` reach the physics a frame later.
  At 60 Hz or more that's hard to feel, but it changes how existing games
  respond, and fixed-step tests that count frames would shift by one.
- **Positions set in `Update` aren't simulated until the next frame,** but
  they're drawn right away. A teleport (such as `ResetVehicle`) shows one
  frame before the physics knows about it. That's harmless, but it's a
  change.
- **Collision events** (`TestForCollisions` fires collision listeners)
  would come before `Update` in the same frame, instead of after it. Games
  that react to a collision in `Update` by reading state set by a listener
  would see it a frame sooner.
- **Behaviors and items** (`UpdateAll`) still run before physics, so the
  game's `Update` and its items' `Update` would run on opposite sides of
  the step. That's confusing to explain to students.
- **It changes every existing Quorum game,** including classroom material
  that describes the loop.

That's too broad a change to make silently. It would need a version note,
and probably a setting to keep the old order.

### C. Leave it to games

Document the `PhysicsBehavior3D:UpdateAfterPhysics` pattern
(`VehicleFollower`). It works today, but it's a lot of machinery for "put
the camera behind the car", and only works in 3D.

## Recommendation

**Option A.** It's additive, costs nothing for games that don't use it,
matches where other engines put camera code, and keeps input in the same
frame. Option B can be reconsidered later as a major-version change, with A
already teaching users where camera code belongs.

## Related: what the interpolation change means for games

This is already applied, but worth documenting alongside A:
- **`GetX`, `GetY`, `GetZ` and the drawn rotation of a physics item are
  now its drawn pose,** up to one step (1/60 s) behind the physics. That's
  correct for cameras, labels, lights and anything else on screen.
- **Physics code should read `GetCenterOfMassTransform`** (the physics pose)
  instead. In the library, only `Vehicle:UpdateWheel` read drawn positions
  during a step, and it has been changed.
- **Game code** that compares positions for gameplay (distance checks, "fell
  off the map") is fine either way. The difference is at most one step of
  motion.
- **Compound bodies:** child items of a physics body are placed from its
  drawn pose. If children carry their own collision shapes, their
  collision boxes can be up to one step off from the parent's physics pose.
  This needs a check if compound physics items are used.
- **2D:** `Layer2D` has the same structure (`SetScreenPositionsFromPhysicsPositions`)
  without interpolation. The same fix applies if 2D physics games show
  jitter.
