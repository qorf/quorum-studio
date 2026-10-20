# Item 43: sea level and lakes on the open-world terrain (water, phase 5)

**Status (2026-10-19): for review.** Nothing is applied yet. This is phase 5
of `WaterPlan.md`: `WorldTerrain` gets a sea that always covers the view and
lakes placed anywhere, and DriveOpen uses them. All of it is **project
code**. **No library change.**

**Needs Item 39** (`WaterSurface`, `WaterShaderProgram`). It looks best
with Items 40 (fog hides the sea's far edge) and 41 (the sky's reflection),
but works without them.

**Scope:** Vulkan only, like the water program. **OpenGL is not touched.**

Paths: **W/** = `AllTerrainVehicle/SourceCode/Worlds/`, **T** =
`W/WorldTerrain.quorum`.

## The design

**The sea** is one large `WaterSurface` (1,000 × 1,000 by default) at the sea
level:
- **It follows the target** (the vehicle) in steps of 100 units, so it
  always reaches past the camera's far plane in every direction. It moves
  only when the target crosses a step, not every frame.
- **The ripples don't slide when it moves,** because the water shader
  reads them at world positions, not along the sheet.
- **Where the land is higher than the sea,** the land hides the water,
  with no cutting: Item 39's shader discards water behind closer ground.
- **Past the map's edge,** where there is no ground, the water is opaque
  deep water. DriveOpen becomes an island.
- **Where the sheet ends** (at the camera's far plane), the fog hides it if
  the fog's end is inside the far plane (Item 40).

**Lakes** are `WaterSurface`s added with `AddLake`, at a level, with a size.
A lake can be a rectangle bigger than its basin: wherever the ground rises
above the lake's level, the ground hides it, so the shoreline follows the
terrain by itself.

**One water program** draws the sea and all the lakes, made and installed
by `WorldTerrain` the first time water is asked for, or given with
`SetWaterProgram`. They share one look (Item 39's design).

**Asking about water:** `GetWaterLevelAt(x, z)` and `IsUnderWater(x, y, z)`,
for later buoyancy, splashes or an underwater tint. The highest water
covering a point wins: a lake above the sea is its own level.

**What it doesn't do:**
- **No physics:** the car drives along the bottom. Buoyancy and drag are a
  separate task (Group 7, Item 66's extras).
- **No water brush** in the TerrainEditor yet (Item 70).
- **Lakes aren't saved** with the terrain's files. They're made by the game.

## The code

### 1. `W/WaterSurface.quorum` (Item 39's file)

**New fields,** after `WaterShaderProgram waterProgram = undefined`:

```
    // The sheet's size, from Load (library item 43).
    number sheetWidth = 0
    number sheetDepth = 0
```

**In `Load`,** after `waterProgram = program`, add:

```
        sheetWidth = width
        sheetDepth = depth
```

**New actions,** after `GetLevel`:

```
    /*
    This action returns true if the sheet reaches over a point on the
    ground: the point is inside the sheet's rectangle, seen from above.

    Attribute: Parameter x The point's x position.
    Attribute: Parameter z The point's z position.
    Attribute: Returns True if the sheet covers the point.
    */
    action Covers(number x, number z) returns boolean
        Math math
        return math:AbsoluteValue(x - GetX()) <= sheetWidth / 2 and math:AbsoluteValue(z - GetZ()) <= sheetDepth / 2
    end

    /*
    This action returns the sheet's size along x, from Load.

    Attribute: Returns The width.
    */
    action GetSheetWidth returns number
        return sheetWidth
    end

    /*
    This action returns the sheet's size along z, from Load.

    Attribute: Returns The depth.
    */
    action GetSheetDepth returns number
        return sheetDepth
    end
```

**New `use`:** `use Libraries.Compute.Math`.

`Covers` assumes the sheet isn't turned, which `WorldTerrain` never does.

### 2. T (`W/WorldTerrain.quorum`)

**The class comment.** At its end, before `Attribute: Example`, add:

```
Water (library item 43): SetSeaLevel puts a sea at a height, one large
WaterSurface that follows the target given to Update, so it always covers
the view; where the ground is higher, the ground hides it. AddLake puts a
lake anywhere, at its own level; it can be larger than its basin, since
the ground hides it where it rises above the water. GetWaterLevelAt and
IsUnderWater say where the water is. Water needs Vulkan.
```

**New fields,** after the ground look's fields (Item 42), or after
`number heightBlendSoftness = 0.2`:

```
    // Water (library item 43): one sea that follows the target, and lakes,
    // all drawn with one water program.
    WaterShaderProgram waterProgram = undefined
    WaterSurface sea = undefined
    boolean seaOn = false
    boolean seaInLayer = false
    number seaLevel = 0
    number seaSize = 1000
    constant number SEA_STEP = 100
    // GetWaterLevelAt's answer where there is no water.
    constant number NO_WATER = -1000000
    Array<WaterSurface> lakes
```

`Array`, `Layer3D` and `Math` are already used in T.

**In `Update`,** before its last line, `return changed`:

```
        // The sea follows the target (library item 43).
        if seaOn and sea not= undefined
            if not seaInLayer
                layer:Add(sea)
                seaInLayer = true
            end
            PlaceSea(targetX, targetZ)
        end
```

**New actions,** after `GetGroundLookBits` (Item 42), or after
`GetHeightBlendSoftness`:

```
    /*
    This action sets the water program the sea and lakes are drawn with. It
    must be installed (WaterShaderProgram:Install). Without one, the
    terrain makes and installs its own the first time water is asked for.
    Set it before the sea or any lake, since water already made keeps its
    program.

    Attribute: Parameter program An installed water program.
    */
    action SetWaterProgram(WaterShaderProgram program)
        waterProgram = program
    end

    /*
    This action returns the water program the sea and lakes are drawn with,
    so its look can be changed.

    Attribute: Returns The water program, or undefined before any water is made.
    */
    action GetWaterProgram returns WaterShaderProgram
        return waterProgram
    end

    /*
    This action puts a sea at a height, or moves it there. The sea is one
    large sheet of water that follows the target given to Update, so it
    always covers the view; wherever the ground is higher, the ground hides
    it. It is added to the layer on the next Update.

    Attribute: Parameter level The sea's height.
    */
    action SetSeaLevel(number level)
        seaLevel = level
        if sea = undefined
            WaterSurface surface
            surface:Load(seaSize, seaSize, GetOrMakeWaterProgram())
            sea = surface
        end
        sea:SetLevel(level)
        seaOn = true
    end

    /*
    This action returns the sea's height.

    Attribute: Returns The sea level, from SetSeaLevel.
    */
    action GetSeaLevel returns number
        return seaLevel
    end

    /*
    This action returns true if there is a sea.

    Attribute: Returns True after SetSeaLevel, until RemoveSea.
    */
    action HasSea returns boolean
        return seaOn
    end

    /*
    This action takes the sea away. SetSeaLevel brings it back.

    Attribute: Parameter layer The layer the terrain is in.
    */
    action RemoveSea(Layer3D layer)
        seaOn = false
        if seaInLayer
            layer:Remove(sea)
            seaInLayer = false
        end
    end

    /*
    This action sets the size of the sea's sheet, along x and along z. It
    must reach past the camera's far plane from the target, twice over,
    plus a step of 100. Set it before SetSeaLevel.

    Attribute: Parameter size The sea's size. The default is 1000.
    */
    action SetSeaSize(number size)
        if sea not= undefined
            alert("Set the sea's size before its level: the sea is already made.")
        end
        seaSize = size
    end

    /*
    This action returns the sea sheet, or undefined before SetSeaLevel.

    Attribute: Returns The sea's WaterSurface.
    */
    action GetSea returns WaterSurface
        return sea
    end

    /*
    This action puts a lake in the layer: a sheet of water, centered at a
    point, at a level. It can be larger than the basin it sits in, since
    the ground hides it wherever the ground rises above the water.

    Attribute: Parameter layer The layer the terrain is in.
    Attribute: Parameter x The lake's center along x.
    Attribute: Parameter z The lake's center along z.
    Attribute: Parameter width The lake's size along x.
    Attribute: Parameter depth The lake's size along z.
    Attribute: Parameter level The water's height.
    Attribute: Returns The lake.
    */
    action AddLake(Layer3D layer, number x, number z, number width, number depth, number level) returns WaterSurface
        WaterSurface lake
        lake:Load(width, depth, GetOrMakeWaterProgram())
        lake:SetPosition(x, level, z)
        layer:Add(lake)
        lakes:Add(lake)
        return lake
    end

    /*
    This action takes a lake away.

    Attribute: Parameter layer The layer the terrain is in.
    Attribute: Parameter lake A lake from AddLake.
    */
    action RemoveLake(Layer3D layer, WaterSurface lake)
        if lakes:Remove(lake)
            layer:Remove(lake)
        end
    end

    /*
    This action takes every lake away.

    Attribute: Parameter layer The layer the terrain is in.
    */
    action RemoveLakes(Layer3D layer)
        integer i = 0
        repeat while i < lakes:GetSize()
            layer:Remove(lakes:Get(i))
            i = i + 1
        end
        lakes:Empty()
    end

    /*
    This action returns how many lakes there are.

    Attribute: Returns The number of lakes.
    */
    action GetLakeCount returns integer
        return lakes:GetSize()
    end

    /*
    This action returns a lake.

    Attribute: Parameter index Which lake, from 0.
    Attribute: Returns The lake.
    */
    action GetLake(integer index) returns WaterSurface
        return lakes:Get(index)
    end

    /*
    This action returns the height of the water over a point: the highest
    of the sea and the lakes covering it. Where there is none, it returns
    -1000000, below anything.

    Attribute: Parameter x The point's x position.
    Attribute: Parameter z The point's z position.
    Attribute: Returns The water's height there.
    */
    action GetWaterLevelAt(number x, number z) returns number
        number level = NO_WATER
        if seaOn
            level = seaLevel
        end
        integer i = 0
        repeat while i < lakes:GetSize()
            WaterSurface lake = lakes:Get(i)
            if lake:Covers(x, z) and lake:GetLevel() > level
                level = lake:GetLevel()
            end
            i = i + 1
        end
        return level
    end

    /*
    This action returns true if a point is under the water: below the
    water's height there, and not where the ground stands above it. It
    doesn't check the ground; a point inside a hill under a lake's sheet
    counts as under water.

    Attribute: Parameter x The point's x position.
    Attribute: Parameter y The point's height.
    Attribute: Parameter z The point's z position.
    Attribute: Returns True if the point is below the water's height there.
    */
    action IsUnderWater(number x, number y, number z) returns boolean
        return y < GetWaterLevelAt(x, z)
    end

    /*
    This action returns the water program, making and installing one the
    first time.

    Attribute: Returns The water program.
    */
    private action GetOrMakeWaterProgram returns WaterShaderProgram
        if waterProgram = undefined
            WaterShaderProgram program
            program:Install("World Terrain Water")
            waterProgram = program
        end
        return waterProgram
    end

    /*
    This action moves the sea under the target, in steps of 100 units, so
    it only moves when the target crosses a step.

    Attribute: Parameter targetX The target's position along x.
    Attribute: Parameter targetZ The target's position along z.
    */
    private action PlaceSea(number targetX, number targetZ)
        number x = math:Floor(targetX / SEA_STEP + 0.5) * SEA_STEP
        number z = math:Floor(targetZ / SEA_STEP + 0.5) * SEA_STEP
        if sea:GetX() not= x or sea:GetZ() not= z or sea:GetY() not= seaLevel
            sea:SetPosition(x, seaLevel, z)
        end
    end
```

`Array:Remove(item)` returns a boolean and `Array:Empty()` clears it
(checked). `math` is T's existing `Math` field.

### 3. `SourceCode/DriveOpen.quorum`

**New `use`s:**

```
use Libraries.Game.Graphics.Worlds.WaterSurface
```

**New fields,** after the ground look's (Item 42):

```
    // Water (library item 43), Vulkan only: a sea around the island, at
    // seaLevel, and lakes. L turns the sea on or off, U and J raise and
    // lower it, B drops a lake ahead of the vehicle, and V takes the lakes
    // away. The ground runs from about -6 to 34.
    boolean useSea = true
    number seaLevel = -2
    number lakeSize = 30
    boolean lWasDown = false
    boolean bWasDown = false
    boolean vWasDown = false
```

**The fog's end** (Item 40). The line

```
    number fogEnd = 260
```

becomes:

```
    // Inside the camera's far plane (232 here), so the sea's far edge is
    // all fog (library item 43).
    number fogEnd = 220
```

**In `CreateGame`,** after `BuildTerrain()`:

```
        GameStateManager waterState
        if useSea and waterState:GetGameGraphics() is VulkanGraphics
            terrain:SetSeaLevel(seaLevel)
            ReportSea()
        end
```

**In `Update`,** after the G key (Item 42):

```
        boolean lDown = monitor:IsKeyPressed(keys:L)
        if lDown and not lWasDown
            if terrain:HasSea()
                terrain:RemoveSea(GetCurrentLayer3D())
                output "Sea: off."
            else
                terrain:SetSeaLevel(seaLevel)
                ReportSea()
            end
        end
        lWasDown = lDown

        if terrain:HasSea() and (monitor:IsKeyPressed(keys:U) or monitor:IsKeyPressed(keys:J))
            if monitor:IsKeyPressed(keys:U)
                seaLevel = seaLevel + 2 * seconds
            else
                seaLevel = seaLevel - 2 * seconds
            end
            terrain:SetSeaLevel(seaLevel)
        end

        boolean bDown = monitor:IsKeyPressed(keys:B)
        if bDown and not bWasDown
            DropLake()
        end
        bWasDown = bDown

        boolean vDown = monitor:IsKeyPressed(keys:V)
        if vDown and not vWasDown and terrain:GetLakeCount() > 0
            terrain:RemoveLakes(GetCurrentLayer3D())
            output "Lakes: removed."
        end
        vWasDown = vDown
```

**New actions,** after `ShowGroundLook` (Item 42):

```
    /*
    This action puts a lake a little ahead of the vehicle, its water 1.5
    units above the ground there, so it fills the dip and the ground around
    hides the rest (library item 43).
    */
    action DropLake
        GameStateManager waterState
        if not (waterState:GetGameGraphics() is VulkanGraphics)
            return now
        end
        Vector3 heading = GetHeading()
        number x = chassis:GetX() + heading:GetX() * 15
        number z = chassis:GetZ() + heading:GetZ() * 15
        number level = GetGroundHeightAt(x, z) + 1.5
        terrain:AddLake(GetCurrentLayer3D(), x, z, lakeSize, lakeSize, level)
        output "Lake " + terrain:GetLakeCount() + ": at (" + math:Round(x, 1) + ", " + math:Round(z, 1) + "), level " +
            math:Round(level, 2) + ". V takes the lakes away."
    end

    /*
    This action reports the sea level, and how much of the map lies under
    it, from a 64 by 64 grid of ground heights (library item 43).
    */
    action ReportSea
        integer under = 0
        integer total = 0
        number span = mapSize * terrainScale
        integer row = 0
        repeat while row < 64
            integer column = 0
            repeat while column < 64
                number x = baseX + (column + 0.5) * span / 64
                number z = baseZ + (row + 0.5) * span / 64
                if GetGroundHeightAt(x, z) < seaLevel
                    under = under + 1
                end
                total = total + 1
                column = column + 1
            end
            row = row + 1
        end
        output "Sea: at " + math:Round(seaLevel, 2) + ", covering " + math:Round(100.0 * under / total, 1) +
            "% of the map. L turns it off, U and J raise and lower it."
    end
```

`GetHeading`, `GetGroundHeightAt`, `chassis`, `mapSize`, `terrainScale`,
`baseX`, `baseZ` and `math` are DriveOpen's existing names.

**Keys:** L, U, J, B and V are free in DriveOpen (taken: W A S D H N K R E
Q P C, F after Item 40, G after Item 42).

## What changes

**On screen, in DriveOpen:** a sea around the island at -2, with lowland
under water and the map's edges a coastline. B drops ponds into dips ahead.

**Cost:** the water's pixels only (Item 39's costs). The sea is one draw.

**Memory:** the water program's ripple texture (Item 39), once.

## Test plan

1. **DriveOpen at start:** the sea report gives the level and the share of
   the map under water; the low ground is under the sea, and the shore
   fades softly.
2. **Driving along:** the sea always reaches the haze at the horizon, and
   never shows an edge or jumps when it steps after the vehicle.
3. **Off the edge of the map:** past the last terrain, deep water out to
   the fog.
4. **U and J:** the coastline moves up and down the hills smoothly; L
   twice reports the new share.
5. **B in a dip:** a pond fills it, with a soft shore, and the ground
   around hides the rest of the square. B on a slope: the pond is a
   half-moon against the hill. V removes them.
6. **Under water:** driving into the sea, the car drives on along the
   bottom, and the surface overhead is drawn.
7. **Night (N) and headlights (H) on the water:** the headlights light
   the water's surface (Item 39's spotlight support).
8. **Memory report** steady over a few minutes of driving with the sea on.

## Rollback

Revert the Item 43 lines in `WaterSurface`, T and DriveOpen. With
`useSea = false`, DriveOpen has no sea and lakes only appear with B.
