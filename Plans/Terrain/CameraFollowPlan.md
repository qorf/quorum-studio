# Camera Follow: a plan for review

**Applied 2026-10-20** after review:
- `Camera.quorum` has the follow actions, and `Game.quorum` has
  `UpdateFollowingCameras`.
- DriveOpen uses `Follow` and `JumpToFollowPosition`, and `PlaceCamera` is
  deleted.

Review decisions:
- **Separate getters and setters only**, so there is no
  `SetFollowDistance`. `Follow(item, behind, above)` stays as the one-line
  start.
- **The side angle is OK.**
- **The rest stays as proposed:** defaults of 10 and 4, an explicit
  `JumpToFollowPosition`, level only, 3D only, and lights still placed by
  hand.

**Confirmed on screen (2026-10-20):** DriveOpen's camera follows with no
jitter, and 90 degrees puts the camera on the item's **right**.
`SetFollowAngle`'s comment says so: 90 is the right, and 270 is the left.

## Goal

A student writes one line, and the camera chases an item smoothly, with no
need to know about frame timing or physics steps:

```quorum
GetCamera3D():Follow(car, 12, 5)    // 12 behind, 5 above
```

The library places the camera every frame at the right moment: after
physics, once every item is where it will be drawn, and before drawing.
That's the timing `UpdateAfterPhysics` gets right, but here the student
never has to think about it.

## What students can say

All distances are in world units, the same units as `SetPosition`.

| Action | What it does | Default |
|---|---|---|
| `Follow(Item3D item)` | Starts following the item at the default distances. | |
| `Follow(Item3D item, number behind, number above)` | Starts following at the given distances. | |
| `SetDistanceBehind(number)` / `GetDistanceBehind` | How far back from the item. A negative number puts the camera in front. | 10 |
| `SetDistanceAbove(number)` / `GetDistanceAbove` | How far above the item's center. A negative number puts it below. | 4 |
| `SetFollowAngle(number degrees)` / `GetFollowAngle` | Turns the camera around the item. 0 is behind, 90 is to its right (confirmed on screen), 180 is in front. | 0 |
| `SetAimHeight(number)` / `GetAimHeight` | Aims the camera this far above the item's center, for example at a character's head instead of its waist. | 0 |
| `SetFollowTurning(boolean)` / `IsFollowTurning` | True: the camera stays behind the item as it turns (a chase camera). False: it keeps one direction in the world, like a side-on platformer or a top-down view. | true |
| `SetFollowSmoothing(number seconds)` / `GetFollowSmoothing` | How long the camera takes to catch up. 0 is rigid; 0.2 to 0.5 feels like a camera operator. It works the same at any frame rate. | 0 |
| `JumpToFollowPosition()` | Places the camera exactly on the next frame, skipping smoothing. Use it after teleporting the item. | |
| `StopFollowing()` | The camera stays where it is and can be placed by hand again. | |
| `IsFollowing()` / `GetFollowItem()` | Which item is being followed, if any. | |

**Notes:**
- **"Behind"** means opposite the way the item faces: its local +Z axis,
  the same direction the vehicle library drives along. A model built facing
  another way can use `SetFollowAngle` (for example 180, or 90) instead of
  being re-exported.
- **Level by default.** Only the item's level heading is used, so the
  camera doesn't swing up and down as a car climbs hills, or spin when a
  ball rolls over. DriveOpen does the same today.
- **While following,** the camera places itself every frame, so
  `SetPosition` and `LookAt` from the game are overwritten. To nudge it
  anyway, for example to add a shake, change it in
  `Game:UpdateAfterPhysics`, which runs after the follow.

### Examples for the documentation

```quorum
// A chase camera.
GetCamera3D():Follow(car, 12, 5)

// A softer chase camera that eases after the car.
Camera camera = GetCamera3D()
camera:Follow(car, 12, 5)
camera:SetFollowSmoothing(0.3)

// Side-on, like a platformer: 20 units to the side, never turning.
camera:Follow(player, 20, 2)
camera:SetFollowAngle(90)
camera:SetFollowTurning(false)

// Over the shoulder of a character, aimed at its head.
camera:Follow(hero, 4, 2)
camera:SetAimHeight(1.5)

// Straight down onto a board game, far above.
camera:Follow(board, 0.01, 30)
camera:SetFollowTurning(false)
```

## Library changes

### 1. `Camera.quorum`: the settings and `UpdateFollow`

These go in the `Camera` class (Game/Graphics/Camera.quorum), so
perspective and orthographic cameras both get them. The class needs
`use Libraries.Interface.Item3D` and `use Libraries.Compute.Matrix4`
(Matrix4 is already used).

```quorum
    // Following an item (see Follow). Distances are in world units.
    Item3D followItem = undefined
    number distanceBehind = 10
    number distanceAbove = 4
    number followAngle = 0
    number aimHeight = 0
    boolean followTurning = true
    number followSmoothing = 0
    // True until the camera has been placed exactly once, so a new follow,
    // or a jump, doesn't ease in from wherever the camera was.
    boolean followJump = true
    // The item's last level heading, kept for when it points straight up
    // or down and has none.
    number headingX = 0
    number headingZ = 1
    Math followMath

    action Follow(Item3D item)
        followItem = item
        followJump = true
    end

    action Follow(Item3D item, number behind, number above)
        distanceBehind = behind
        distanceAbove = above
        Follow(item)
    end

    action StopFollowing
        followItem = undefined
    end

    action IsFollowing returns boolean
        return followItem not= undefined
    end

    action JumpToFollowPosition
        followJump = true
    end

    // ...and the plain Get/Set pairs from the table above.

    /*
    This action places a following camera behind and above its item, and
    aims it. The game calls it once a frame for each camera, after the
    physics, when every item is where it will be drawn. Most users never
    need to call it.

    Attribute: Parameter seconds The time since the last frame, in seconds.
    */
    action UpdateFollow(number seconds)
        if followItem = undefined
            return now
        end

        number targetX = followItem:GetGlobalX()
        number targetY = followItem:GetGlobalY()
        number targetZ = followItem:GetGlobalZ()

        // The level part of the way the item faces (its local +Z). For a
        // physics item this is its drawn rotation, which moves smoothly.
        if followTurning
            Matrix4 rotation = followItem:GetRotationTransform()
            number forwardX = rotation:row0column2
            number forwardZ = rotation:row2column2
            number flat = followMath:SquareRoot(forwardX * forwardX + forwardZ * forwardZ)
            if flat > 0.0001
                headingX = forwardX / flat
                headingZ = forwardZ / flat
            end
        else
            headingX = 0
            headingZ = 1
        end

        // Turn the heading about the vertical axis by the follow angle.
        number radians = followMath:DegreesToRadians(followAngle)
        number cosine = followMath:Cosine(radians)
        number sine = followMath:Sine(radians)
        number faceX = headingX * cosine - headingZ * sine
        number faceZ = headingX * sine + headingZ * cosine

        number wantX = targetX - faceX * distanceBehind
        number wantY = targetY + distanceAbove
        number wantZ = targetZ - faceZ * distanceBehind

        if followJump or followSmoothing <= 0
            SetPosition(wantX, wantY, wantZ)
            followJump = false
        else
            // Close the same share of the gap each second at any frame
            // rate: after followSmoothing seconds, about 63% of it.
            number blend = 1 - followMath:RaiseToPower(followMath:e, -seconds / followSmoothing)
            Vector3 current = GetPosition()
            SetPosition(current:GetX() + (wantX - current:GetX()) * blend,
                current:GetY() + (wantY - current:GetY()) * blend,
                current:GetZ() + (wantZ - current:GetZ()) * blend)
        end

        // Up first, so no roll builds up over time.
        SetUp(0, 1, 0)
        LookAt(targetX, targetY + aimHeight, targetZ)
    end
```

Notes on the code:
- **Drawn positions.** `GetGlobalX/Y/Z` and `GetRotationTransform` of a
  physics item are now its drawn pose (the interpolation change), so the
  follow is smooth with no special case. For an item that isn't a physics
  item, they're simply its position and rotation.
- **Smoothing eases the position only.** The camera always aims at the
  item itself, so the item stays centered while the camera swings in
  behind it.
- **The sign of `followAngle`** (whether 90 is the item's left or right in
  Quorum's coordinates) has to be checked on screen and documented to
  match.

### 2. `Game.quorum`: call it each frame

In `ContinueGame`, between the physics and the game's `UpdateAfterPhysics`:

```quorum
        if collide
            TestForCollisions(seconds)
        end
        UpdateFollowingCameras(seconds)
        UpdateAfterPhysics(seconds)
```

```quorum
    // Reused each frame, so a camera shared by several layers is placed once.
    Array<Camera> followedCameras

    /*
    Places every following camera once, after the physics and before the
    game's UpdateAfterPhysics, so the game can still adjust a camera there.
    */
    private action UpdateFollowingCameras(number seconds)
        followedCameras:Empty()
        integer i = 0
        repeat while i < layers:GetSize()
            Layer layer = layers:Get(i)
            if layer is Layer3D
                Layer3D layer3D = cast(Layer3D, layer)
                Camera camera = layer3D:GetCamera()
                if camera not= undefined and camera:IsFollowing() and not followedCameras:Has(camera)
                    camera:UpdateFollow(seconds)
                    followedCameras:Add(camera)
                end
            end
            i = i + 1
        end
    end
```

- **Every frame, physics or not.** It runs even with collisions off, so an
  item moved by hand in `Update` is followed too.
- **Once per camera.** Layers that share a camera don't move it twice,
  which matters when smoothing is on.
- **Cost:** a check per layer each frame, plus a few multiplications for
  each camera that is following.

### 3. DriveOpen, before and after

Before (today):

```quorum
    action UpdateAfterPhysics(number seconds)
        PlaceHeadlights()
        PlaceCamera()          // about 15 lines: heading, offsets, SetUp, LookAt
        ...
    end
```

After:

```quorum
    // in CreateGame, after BuildChassis:
    GetCamera3D():Follow(chassis, cameraBehind, cameraAbove)

    // in ResetVehicle, after moving the chassis:
    GetCamera3D():JumpToFollowPosition()

    action UpdateAfterPhysics(number seconds)
        PlaceHeadlights()      // the lights still follow by hand
        ...
    end
```

`PlaceCamera` is deleted. `GetHeading` stays, because the terrain editing
uses it.

## Decisions for review

1. **Names.** `Follow` / `StopFollowing`, and `SetDistanceBehind` /
   `SetDistanceAbove`. Alternatives: `FollowItem`, or `SetFollowDistance`
   only.
2. **Defaults.** 10 behind and 4 above suit a car or a person a few units
   tall. DriveOpen uses 12 and 5.
3. **Teleports.** `JumpToFollowPosition` is explicit. Optionally, the
   camera could also jump on its own when the item moves farther in one
   frame than some distance (say, twice the follow distance). That's less
   to remember, but it's a hidden rule.
4. **Tilting.** Level only for now. A later `SetFollowTilting(true)` could
   follow the item's full rotation, which flight games want.
5. **2D.** This plan is 3D only (`Item3D`). A 2D version (`Follow(Item2D)`
   on the 2D camera, with an offset and smoothing) is a natural
   follow-up, but would be a separate change.
6. **Lights and other things.** Headlights and lamps still follow by hand in
   `UpdateAfterPhysics`. A general way to attach any light to an item (as
   children of the item) is a separate question.
7. **The followed item leaves the game.** The camera keeps aiming where it
   last was, until `StopFollowing` or a new `Follow`. Stopping on its own
   could also be reasonable.

## Testing

- **DriveOpen:** the follow replaces `PlaceCamera`. Expect the same view,
  with no jitter, including after R (reset) with `JumpToFollowPosition`.
- **Smoothing:** check that 0.3 feels the same at vSync on (60 Hz) and
  uncapped. With the formula above it should.
- **Angle 90 and 180** in DriveOpen: confirm which side 90 is, and update
  the documentation to match.
- **`SetFollowTurning(false)`:** the camera keeps one direction while the
  car turns.
- **A non-physics item** moved in `Update` (a box moved with arrow keys):
  followed with no lag, since the follow runs after `Update`.
- **Two layers sharing one camera:** it's placed once per frame (smoothing
  doesn't double).
