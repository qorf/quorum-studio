# Quorum Studio plan: terrain in scenes (Item 71) and brushes (Item 70)

**Status (2026-10-19): for review.** Nothing is applied.
- **Item 71 is written out** with exact before/after code: the Worlds
  classes move into the standard library, and a terrain becomes a scene
  item that Studio's scene editor can add from the left palette.
- **Item 70 is now a short plan** that builds on Item 71: brushes come one
  at a time, inside the scene editor.

This folder (`physics/QuorumStudio/`) holds the Quorum Studio documents. It
replaces `AllTerrainVehicle/Item70.md`.

**Why this order (the user's choice, 2026-10-19):** with terrain in real
scene files, everything after it is easier to test. A terrain made in Studio
is loaded by a game with `LoadScene`, like any model.

**Repositories and branches** (never switch branches):
- **Library:** `quorum-language`, branch `MakeAWish`.
- **Studio:** `quorum-studio`, branch `Terrain` (Vulkan is the default there,
  `5ca3bead`).
- **Project:** `physics/AllTerrainVehicle`.

**Paths:**
- **L/** = `quorum-language/Quorum/Library/Standard/Libraries/`;
- **W/** = `L/Game/Graphics/Worlds/` (the library's Worlds folder, which
  already holds `Terrain` and `TextureSplat`);
- **SC/** = `L/Interface/Controls/Scenes/`;
- **AT/** = `physics/AllTerrainVehicle/`;
- **S/** = `quorum-studio/QuorumStudio/`.

---

# Item 71: the Worlds classes in the standard library, and terrain in scenes

## What it does

1. **Moves the 18 Worlds classes** from AT/SourceCode/Worlds into W/,
   unchanged except for one fix to how `TerrainData` finds files in a
   folder (section 2).
2. **Adds `TerrainItem`** (W/): a terrain as an `Item3D`, loaded from its
   `terrain.json`. Its region models are its children, so it goes into a
   layer, a scene and the scene editor like any item.
3. **Scene files** get a new item class, `"Terrain"`, that points at the
   terrain's `terrain.json`, relative to the project, as a model points at
   its `.obj`. The terrain's own files (`terrain.json` and the region
   images) stay where they are; the scene only refers to them.
4. **Studio's scene editor** gets **Terrain** in the left palette's "Basic
   3D" group.
   - **Activating it** (the palette's usual Enter) or **dragging it into the
     scene** adds a **flat** terrain: 2 × 2 regions of 64 samples, 1 unit
     apart, so 128 × 128 units centered on the origin, at height 0.
   - Its files are written at once into the project, in
     `Terrains/Terrain1/` (then `Terrain2`, and so on).
   - It is named "Terrain" (then "Terrain 1", ...), selected, and
     announced.

**Keys:** none new. The terrain uses the scene editor's existing keys:
- the palette's activation key and drag and drop add it;
- the scene tree selects it;
- Delete removes it;
- Undo and Redo undo and redo adding or removing it;
- the camera keys move the view.

**What the editor's move, turn and scale keys do to a terrain: nothing.**
They only act on `EditorItem3D`s, and the editor's terrain is not one. A
terrain's heights are world positions (its collision, its streaming and
`TerrainData:GetHeight` all assume so), so it always stays at the origin.

**Not in Item 71:**
- painting or sculpting (Item 70, one tool at a time);
- choosing ground textures in Studio (the scene can name one; section 6);
- streaming terrain in scenes;
- picking the terrain with the mouse (select it in the scene tree; mouse
  picking comes with the brushes, using `TerrainData:RayCast`);
- OpenGL: a scene with a terrain needs Vulkan, and says so.

## The scene file

A terrain in a layer's `"Items"`:

```json
{
    "Class": "Terrain",
    "Name": "Terrain",
    "Description": "Ground for the scene, made of height map regions.",
    "File": "Terrains/Terrain1/terrain.json"
}
```

With a ground texture (optional; Studio doesn't set one yet):

```json
    "Ground Texture": "Textures/grass.png",
    "Texture Repeat": 4
```

- **No position, rotation or scale:** a terrain is always at the origin.
- **Its region models aren't saved** as children; they come from the
  terrain's files.
- **An older Quorum** reading this scene makes a plain `Item3D` for
  `"Terrain"` (`CreateItem3D`'s default) and ignores the rest. So the scene
  version doesn't change.

## The code

### 1. Move the Worlds classes (files only)

**From** AT/SourceCode/Worlds **to** W/, unchanged (their package is
already `Libraries.Game.Graphics.Worlds`):

`TerrainBrush`, `TerrainBrushPreview`, `TerrainCollision`,
`TerrainCollisionPiece`, `TerrainData`, `TerrainEditHistory`,
`TerrainGenerator`, `TerrainPatch`, `TerrainRegion`,
`TerrainRegionFarModel`, `TerrainRegionModel`,
`TerrainSplatFragmentShader`, `TerrainSplatShaderProgram`,
`TerrainStreamer`, `TerrainStroke`, `TerrainStrokeEdit`, `TerrainTool`,
`WorldTerrain` (each `.quorum`).

```bash
cp /Users/stefik/Repositories/physics/AllTerrainVehicle/SourceCode/Worlds/*.quorum /Users/stefik/Repositories/quorum-language/Quorum/Library/Standard/Libraries/Game/Graphics/Worlds/
```

```bash
git -C /Users/stefik/Repositories/physics rm -r AllTerrainVehicle/SourceCode/Worlds
```

The folder holds exactly those 18 files today (checked 2026-10-19). If
Items 39 to 43 are applied first, their water files are in it too and move
the same way.

**Checked:**
- every `use` in the 18 files is a standard library class or another
  Worlds class, so nothing in them points back at the project;
- no name clashes with the library's `Terrain` and `TextureSplat`.

**What stays in the project, unchanged:**
- DriveOpen, TerrainEditor, WaterCheck and the other programs: their
  `use Libraries.Game.Graphics.Worlds...` lines now find the library's
  classes;
- the tests (`SourceCode/Tests/Worlds`, `RunWorldTests`), which test the
  library's classes from the project. Moving them into the library's test
  suite is a later step.

**Items 39 to 43 after this:** their "project" water files
(`WaterShaderProgram`, `WaterShaderMapping`, `WaterFragmentShader`,
`WaterSurface`) go into W/ instead of AT/SourceCode/Worlds. Their code
doesn't change.

### 2. W/`TerrainData.quorum`: files in a folder keep the folder's working directory

A scene's paths are relative to the project, so the scene manager hands the
terrain a `File` whose working directory is the project (Studio's
`ConvertToProjectFile`). `TerrainData` built each file in a folder from the
folder's path alone, which lost that working directory.

The action

```
    private action GetFileInFolder(File folder, text name) returns File
        File file
        file:SetPath(folder:GetPath() + "/" + name)
        return file
    end
```

becomes:

```
    private action GetFileInFolder(File folder, text name) returns File
        // The folder's working directory is kept, so a folder given relative
        // to a project (as a scene gives it) finds its files (Item 71).
        File file
        file:SetWorkingDirectory(folder:GetWorkingDirectory())
        if folder:GetPath() = ""
            file:SetPath(name)
        else
            file:SetPath(folder:GetPath() + "/" + name)
        end
        return file
    end
```

Every file `TerrainData` reads or writes in a folder goes through this
action (checked: settings, region and splat images, both ways). For a
folder made with `SetPath` alone, the working directory is the default one,
so games that load terrain as they do today are unaffected.

### 3. W/`TerrainItem.quorum` (new)

```
package Libraries.Game.Graphics.Worlds

use Libraries.Interface.Item3D
use Libraries.System.File
use Libraries.Game.Graphics.Texture

/*
A TerrainItem is a terrain as an item: it holds a WorldTerrain loaded from a
terrain's folder, and its region models are its children. So a terrain can
go into a layer, be saved in a scene, and be loaded with LoadScene, like a
model. Quorum Studio's scene editor adds one from its palette.

A terrain's heights are world positions, so a TerrainItem stays at the
origin: it isn't moved, turned or scaled. Its region models have collision
by default, so physics objects land on it.

A terrain is loaded from its terrain.json file, which TerrainData's Save
writes into the terrain's folder beside the region images. It needs Vulkan.

Attribute: Example
use Libraries.Game.Game
use Libraries.Game.Graphics.Worlds.TerrainItem

class Main is Game
    TerrainItem ground

    action Main
        StartGame()
    end

    action CreateGame
        EnablePhysics3D(true)
        ground:Load("Terrains/Terrain1/terrain.json")
        Add(ground)
        output "The ground at the origin is at height " + ground:GetWorldTerrain():GetData():GetHeight(0, 0)
    end
end
*/
class TerrainItem is Item3D
    WorldTerrain terrain
    // The terrain.json file's path, as given to Load or CreateFlat, so a
    // scene can save it as it was written.
    text terrainFile = ""
    Texture groundTexture = undefined
    text groundTexturePath = ""
    number textureRepeat = 4
    boolean collision = true

    /*
    This action loads a terrain from its terrain.json file, and makes its
    region models, as the item's children.

    Attribute: Parameter path The terrain.json file's path.
    */
    action Load(text path)
        File file
        file:SetPath(path)
        Load(file)
    end

    /*
    This action loads a terrain from its terrain.json file, and makes its
    region models, as the item's children. The item must not have a
    terrain loaded already.

    Attribute: Parameter file The terrain.json file.
    */
    action Load(File file)
        CheckEmpty()
        terrainFile = file:GetPath()
        terrain:GetData():Load(GetFolder(file))
        BuildModels()
    end

    /*
    This action makes a new, flat terrain at height 0, centered on the
    origin, saves it into the folder of the terrain.json file given, and
    makes its region models. The folder is made if it isn't there.

    Attribute: Parameter file Where the terrain.json file goes.
    Attribute: Parameter regionsAcross How many regions along x and along z.
    Attribute: Parameter regionSize How many samples along each side of a region, a power of 2 such as 64.
    Attribute: Parameter spacing How far apart the samples are, in world units.
    */
    action CreateFlat(File file, integer regionsAcross, integer regionSize, number spacing)
        CheckEmpty()
        if regionsAcross < 1
            alert("A terrain needs at least one region across, but was given " + regionsAcross + ".")
        end
        terrainFile = file:GetPath()
        TerrainData data = terrain:GetData()
        data:SetRegionSize(regionSize)
        data:SetVertexSpacing(spacing)
        integer first = -(regionsAcross / 2)
        integer x = 0
        repeat while x < regionsAcross
            integer z = 0
            repeat while z < regionsAcross
                data:AddRegion(first + x, first + z)
                z = z + 1
            end
            x = x + 1
        end
        data:Save(GetFolder(file))
        BuildModels()
    end

    /*
    This action saves the terrain's heights and painting back into its
    folder.
    */
    action SaveTerrain
        if terrainFile = ""
            alert("The terrain can't be saved, because it wasn't loaded from or made into a terrain.json file.")
        end
        File file
        file:SetPath(terrainFile)
        terrain:GetData():Save(GetFolder(file))
    end

    /*
    This action returns the terrain.json file's path, as given to Load or
    CreateFlat.

    Attribute: Returns The path, or an empty text if no terrain is loaded.
    */
    action GetTerrainFile returns text
        return terrainFile
    end

    /*
    This action returns the WorldTerrain the item holds, to read heights,
    edit the ground, or update its collision.

    Attribute: Returns The WorldTerrain.
    */
    action GetWorldTerrain returns WorldTerrain
        return terrain
    end

    /*
    This action sets a texture laid over the whole ground. Set it before
    Load or CreateFlat.

    Attribute: Parameter file The texture's image file.
    */
    action SetGroundTexture(File file)
        Texture texture
        texture:LoadFromFile(file)
        groundTexture = texture
        groundTexturePath = file:GetPath()
    end

    /*
    This action returns the ground texture's path, as given to
    SetGroundTexture.

    Attribute: Returns The path, or an empty text for none.
    */
    action GetGroundTexturePath returns text
        return groundTexturePath
    end

    /*
    This action sets how far apart, in world units, the ground texture
    repeats. Set it before Load or CreateFlat.

    Attribute: Parameter distance The distance. The default is 4.
    */
    action SetTextureRepeat(number distance)
        textureRepeat = distance
    end

    /*
    This action returns how far apart the ground texture repeats.

    Attribute: Returns The distance.
    */
    action GetTextureRepeat returns number
        return textureRepeat
    end

    /*
    This action sets whether the terrain's region models have collision
    shapes, so physics objects land on them. Set it before Load or
    CreateFlat. Quorum Studio's editor turns it off, since it only draws.

    Attribute: Parameter enabled True for collision, the default.
    */
    action SetCollision(boolean enabled)
        collision = enabled
    end

    /*
    This action returns whether the terrain's region models have collision
    shapes.

    Attribute: Returns True for collision.
    */
    action HasCollision returns boolean
        return collision
    end

    /*
    This action makes the terrain's region models and adds them to the item.
    */
    private action BuildModels
        if collision
            terrain:SetCollisionMode(terrain:COLLISION_FULL)
        else
            terrain:SetCollisionMode(terrain:COLLISION_DISABLED)
        end
        if groundTexture not= undefined
            terrain:SetTexture(groundTexture)
            terrain:SetTextureRepeat(textureRepeat)
        end
        terrain:SetName(GetName())
        terrain:Build()
        integer i = 0
        repeat while i < terrain:GetRegionModelCount()
            Add(terrain:GetRegionModel(i))
            i = i + 1
        end
    end

    /*
    This action stops a second terrain being loaded into the same item.
    */
    private action CheckEmpty
        if terrain:GetData():GetRegionCount() > 0
            alert("This TerrainItem already holds a terrain. Use a new TerrainItem for another.")
        end
    end

    /*
    This action returns the folder holding a terrain.json file, keeping the
    file's working directory, so a path relative to a project still works.

    Attribute: Parameter file The terrain.json file.
    Attribute: Returns The folder.
    */
    private action GetFolder(File file) returns File
        text name = "terrain.json"
        text path = file:GetPath()
        if not path:EndsWith(name)
            alert("A terrain is loaded from its terrain.json file, but was given " + path + ".")
        end
        text folderPath = path:GetSubtext(0, path:GetSize() - name:GetSize())
        if folderPath:EndsWith("/")
            folderPath = folderPath:GetSubtext(0, folderPath:GetSize() - 1)
        end
        File folder
        folder:SetWorkingDirectory(file:GetWorkingDirectory())
        folder:SetPath(folderPath)
        return folder
    end
end
```

**Notes:**
- `WorldTerrain` has `SetName`, `SetCollisionMode`, `SetTexture(Texture)`,
  `SetTextureRepeat`, `Build`, `GetRegionModelCount` and `GetRegionModel`
  (checked). Its models are named after the terrain, so they read as
  "Terrain (0, 0)".
- `TerrainData:AddRegion` makes a flat region (heights 0).
- **Freeing a terrain:** `Item:DisposeAll` (already in the library) frees
  the region models, since they are the item's children. `TerrainItem`
  adds no `Dispose` of its own, so they are never freed twice.

### 4. L/Game/Scenes/`SceneProperties.quorum`

**New constants,** after `public constant text FILE = "File"`:

```
    /*
    The following constants are used for terrains in a scene (Item 71). A
    terrain's FILE is its terrain.json.
    */
    public constant text GROUND_TEXTURE = "Ground Texture"
    public constant text TEXTURE_REPEAT = "Texture Repeat"
```

### 5. L/Game/Scenes/`DefaultSceneManager.quorum`

**New `use`s,** after `use Libraries.Game.Audio3D`:

```
use Libraries.Game.Graphics.Worlds.TerrainItem
use Libraries.Game.Graphics.Vulkan.VulkanGraphics
```

**`CreateItem3D`.** The lines

```
        elseif type = "Audio3D"
            Audio3D audio
            return audio
        else
```

become:

```
        elseif type = "Audio3D"
            Audio3D audio
            return audio
        elseif type = "Terrain"
            TerrainItem terrain
            return terrain
        else
```

**`LoadItem3D`.** Its first lines

```
    private action LoadItem3D(JavaScriptObjectNotation json) returns Item3D
        SceneProperties properties

        // Load Items, Models
        text classValue = json:GetValue(properties:CLASS)
```

become:

```
    private action LoadItem3D(JavaScriptObjectNotation json) returns Item3D
        SceneProperties properties

        // Load Items, Models
        text classValue = json:GetValue(properties:CLASS)

        // A terrain has its own files and no transform (Item 71).
        if classValue = "Terrain"
            return LoadTerrain(json)
        end
```

**`SaveItem3D`.** Its first lines

```
    private action SaveItem3D(Item3D item) returns JavaScriptObjectNotation
        SceneProperties properties
        JavaScriptObjectNotation json
```

become:

```
    private action SaveItem3D(Item3D item) returns JavaScriptObjectNotation
        // A terrain saves only where its files are; its region models come
        // from them, so they aren't saved as children (Item 71).
        if item is TerrainItem
            return SaveTerrain(cast(TerrainItem, item))
        end

        SceneProperties properties
        JavaScriptObjectNotation json
```

**New private actions,** after `LoadSkybox`:

```
    /*
    This action loads a terrain from a scene: a TerrainItem, from the
    terrain.json file the scene names, relative to the project (Item 71). It
    needs Vulkan.

    Attribute: Parameter json The terrain's entry in the scene.
    Attribute: Returns The TerrainItem.
    */
    private action LoadTerrain(JavaScriptObjectNotation json) returns Item3D
        SceneProperties properties
        if not (gameState:GetGameGraphics() is VulkanGraphics)
            alert("This scene has a terrain, and a terrain needs Vulkan for now.")
        end
        TerrainItem terrain = cast(TerrainItem, CreateItem3D("Terrain"))
        if json:HasValue(properties:NAME)
            terrain:SetName(json:GetValue(properties:NAME))
        end
        if json:HasValue(properties:DESCRIPTION)
            terrain:SetDescription(json:GetValue(properties:DESCRIPTION))
        end
        if json:HasValue(properties:GROUND_TEXTURE)
            if json:HasValue(properties:TEXTURE_REPEAT)
                terrain:SetTextureRepeat(json:GetNumber(properties:TEXTURE_REPEAT))
            end
            terrain:SetGroundTexture(ConvertToProjectFile(json:GetValue(properties:GROUND_TEXTURE)))
        end
        terrain:Load(ConvertToProjectFile(json:GetValue(properties:FILE)))
        return terrain
    end

    /*
    This action saves a terrain into a scene: its name, its description,
    its terrain.json file, and its ground texture if it has one (Item 71).

    Attribute: Parameter terrain The terrain.
    Attribute: Returns The terrain's entry for the scene.
    */
    private action SaveTerrain(TerrainItem terrain) returns JavaScriptObjectNotation
        SceneProperties properties
        JavaScriptObjectNotation json
        json:Add(properties:CLASS, "Terrain")
        json:Add(properties:NAME, terrain:GetName())
        json:Add(properties:DESCRIPTION, terrain:GetDescription())
        json:Add(properties:FILE, terrain:GetTerrainFile())
        if terrain:GetGroundTexturePath() not= ""
            json:Add(properties:GROUND_TEXTURE, terrain:GetGroundTexturePath())
            json:Add(properties:TEXTURE_REPEAT, terrain:GetTextureRepeat())
        end
        return json
    end
```

`LoadTerrain` makes the item with `CreateItem3D`, so the scene editor,
which overrides it, gets its own `EditorTerrain` (section 7). In a game, a
scene's terrain is found among the layer's items (`GetIterator`), as the
`TerrainItem` with its name.

### 6. SC/Blueprints/`TerrainBlueprint.quorum` (new)

```
package Libraries.Interface.Controls.Scenes.Blueprints

use Libraries.Game.Graphics.TextureRegion
use Libraries.Interface.Controls.Scenes.Blueprints.ItemBlueprint

/*
The TerrainBlueprint is the palette's entry for a terrain (Item 71). Adding
it to a scene makes a new, flat terrain, saved into the scene's project:
by default 2 by 2 regions of 64 samples, 1 unit apart, so 128 by 128 units
centered on the origin.
*/
class TerrainBlueprint is ItemBlueprint
    TextureRegion paletteIcon = undefined
    integer regionsAcross = 2
    integer regionSize = 64
    number vertexSpacing = 1

    /*
        This action returns the texture to be used as an icon in a palette
        Attribute: Returns A texture for an icon for a terrain
    */
    action GetPaletteIcon returns TextureRegion
        return paletteIcon
    end

    /*
        This action sets the texture to be used as an icon in a palette
        Attribute: Parameter paletteIcon An icon texture for a terrain
    */
    action SetPaletteIcon(TextureRegion paletteIcon)
        me:paletteIcon = paletteIcon
    end

    /*
        This action returns how many regions a new terrain has along x and along z.
        Attribute: Returns The number of regions across
    */
    action GetRegionsAcross returns integer
        return regionsAcross
    end

    /*
        This action sets how many regions a new terrain has along x and along z.
        Attribute: Parameter count The number of regions across, 1 or more
    */
    action SetRegionsAcross(integer count)
        regionsAcross = count
    end

    /*
        This action returns how many samples are along each side of a region.
        Attribute: Returns The region size
    */
    action GetRegionSize returns integer
        return regionSize
    end

    /*
        This action sets how many samples are along each side of a region, a power of 2 such as 64.
        Attribute: Parameter size The region size
    */
    action SetRegionSize(integer size)
        regionSize = size
    end

    /*
        This action returns how far apart a new terrain's samples are, in world units.
        Attribute: Returns The spacing
    */
    action GetVertexSpacing returns number
        return vertexSpacing
    end

    /*
        This action sets how far apart a new terrain's samples are, in world units.
        Attribute: Parameter spacing The spacing
    */
    action SetVertexSpacing(number spacing)
        vertexSpacing = spacing
    end
end
```

### 7. SC/Items/`EditorTerrain.quorum` (new)

```
package Libraries.Interface.Controls.Scenes.Items

use Libraries.Game.Graphics.Worlds.TerrainItem

/*
The EditorTerrain class is a terrain in the Scene Editor (Item 71). It
draws the terrain without collision. It is an EditorItem, so it can be
selected, named, deleted and saved, but not an EditorItem3D, so the
editor's move, turn and scale keys leave it where it is: a terrain's
heights are world positions.
*/
class EditorTerrain is EditorItem, TerrainItem
    on create
        // The editor only draws the terrain.
        SetCollision(false)
    end

    /*
    A terrain is never scaled, so there is nothing to reset.
    */
    action ResetScale
    end
end
```

### 8. SC/`SceneEditor.quorum`

**New `use`s,** after `use Libraries.Interface.Controls.Scenes.Blueprints.DrawableBlueprint`:

```
use Libraries.Interface.Controls.Scenes.Blueprints.TerrainBlueprint
use Libraries.Interface.Controls.Scenes.Items.EditorTerrain
use Libraries.Game.Graphics.Vulkan.VulkanGraphics
```

**`CreateItemPreview(ItemBlueprint, File)`.** Its first line

```
    action CreateItemPreview(ItemBlueprint schematic, File asset) returns ItemPreview
        if schematic is DrawableBlueprint
```

becomes:

```
    action CreateItemPreview(ItemBlueprint schematic, File asset) returns ItemPreview
        // A terrain has no preview to place: it is added flat, at the
        // origin, at once (Item 71).
        if schematic is TerrainBlueprint
            AddTerrain(cast(TerrainBlueprint, schematic))
            return undefined
        end

        if schematic is DrawableBlueprint
```

The palette's activation behavior ignores what `CreateItemPreview`
returns (checked), so returning undefined is safe.

**`CreateItem3D`** (the editor's override). The lines

```
        elseif type = "PointLight"
            EditorPointLight light
            return light
        end
```

become:

```
        elseif type = "PointLight"
            EditorPointLight light
            return light
        elseif type = "Terrain"
            EditorTerrain terrain
            return terrain
        end
```

**New actions,** after `CancelItemPreview`:

```
    /*
    This action adds a new, flat terrain to the scene's 3D layer (Item 71).
    Its files are saved at once into the project, in a new folder,
    Terrains/Terrain1 (then Terrain2, and so on), and the scene refers to its
    terrain.json. It is then selected and announced. Adding it can be
    undone. It needs Vulkan, and a scene that is in a project.

    Attribute: Parameter schematic The terrain's blueprint, from the palette.
    Attribute: Returns The terrain, or undefined if it couldn't be added.
    */
    action AddTerrain(TerrainBlueprint schematic) returns EditorTerrain
        if not (manager:GetGameGraphics() is VulkanGraphics)
            NotifyAccessibility(me, "A terrain needs Vulkan, so it can't be added here.")
            return undefined
        end
        if projectLocation = undefined
            NotifyAccessibility(me, "A terrain is saved in the scene's project, but this scene isn't in a project.")
            return undefined
        end
        EditorLayer3D layer = FindLayer3D()
        if layer = undefined
            return undefined
        end

        // The first free folder: Terrains/Terrain1, Terrains/Terrain2, ...
        text folderPath = ""
        integer count = 1
        repeat while folderPath = ""
            File candidateFolder
            candidateFolder:SetWorkingDirectory(projectLocation:GetAbsolutePath())
            candidateFolder:SetPath("Terrains/Terrain" + count)
            if not candidateFolder:Exists()
                folderPath = "Terrains/Terrain" + count
            end
            count = count + 1
        end
        File terrainFile
        terrainFile:SetWorkingDirectory(projectLocation:GetAbsolutePath())
        terrainFile:SetPath(folderPath + "/terrain.json")

        EditorTerrain terrain
        terrain:SetSceneEditor(me)
        text name = schematic:GetName()
        terrain:SetName(name + GetNextNamePostfix(name))
        IncrementNameCount(name)
        terrain:SetDescription(schematic:GetDescription())
        terrain:CreateFlat(terrainFile, schematic:GetRegionsAcross(), schematic:GetRegionSize(), schematic:GetVertexSpacing())

        Add(layer, terrain)
        SetSelection(layer, terrain)
        terrain:Focus()
        number width = schematic:GetRegionsAcross() * schematic:GetRegionSize() * schematic:GetVertexSpacing()
        NotifyAccessibility(terrain, "Placed " + terrain:GetName() + ", flat, " + width + " by " + width +
            " units, saved in " + folderPath + ".")
        return terrain
    end

    /*
    This action makes a 3D layer the selected layer, if it isn't already,
    and returns it: the same search CreateItemPreview makes for 3D items.

    Attribute: Returns The 3D layer, or undefined if the scene has none.
    */
    private action FindLayer3D returns EditorLayer3D
        if GetSelectedLayer() is EditorLayer3D
            return cast(EditorLayer3D, GetSelectedLayer())
        end
        integer counter = 0
        repeat while counter < layers:GetSize()
            EditorLayer layer = layers:Get(counter)
            if layer is EditorLayer3D
                SetSelectedLayer(layer)
                return cast(EditorLayer3D, layer)
            end
            counter = counter + 1
        end
        return undefined
    end
```

**Already handled, no change:**
- **Undo and Redo** of the add: `Add(layer, item)` records the
  `SceneEditorInsertEdit` and tells the scene tree, as for any item.
- **Delete:** `Delete(Item)` works on any `Item3D`, and records its own
  edit. The terrain's folder stays in the project, as a model's file does.
- **Saving:** the editor's `SaveItem3D` hands anything that isn't an
  `EditorAudio3D` to `DefaultSceneManager:SaveItem3D`, which writes the
  terrain's entry (section 5).
- **Loading:** `LoadItem3D` hands `"Terrain"` to the parent, whose
  `LoadTerrain` calls the editor's `CreateItem3D`, so the editor gets an
  `EditorTerrain`, with paths through the editor's
  `ConvertToProjectFile` (the project).
- **Movement:** the editor moves and turns the focus only when it is an
  `EditorItem3D` (checked in `Update`), and the scale behavior only scales
  `EditorItem3D`s.

### 9. SC/`ScenePalette.quorum`

**New `use`,** after `use Libraries.Interface.Controls.Scenes.Blueprints.PointLightBlueprint`:

```
use Libraries.Interface.Controls.Scenes.Blueprints.TerrainBlueprint
```

**New field,** after `text customCylinderThumbnailLocation = ""`:

```
    text terrainThumbnailLocation = ""
```

**New action,** after `SetDefaultCustomCylinderThumbnail`:

```
    /*
        Sets the image file used as the palette icon for a terrain (Item 71).
    */
    action SetDefaultTerrainThumbnail(text file)
        terrainThumbnailLocation = file
    end
```

**`LoadDefaultAssets`.** The lines

```
            primitives3D:Add(audio)

            LoadAssetPack(primitives3D)
```

become:

```
            primitives3D:Add(audio)

            // A flat terrain, added at once at the origin (Item 71).
            TerrainBlueprint terrain
            terrain:SetName("Terrain")
            terrain:SetDescription("Ground for the scene, made of height map regions. It starts flat, 128 by 128 units.")
            if terrainThumbnailLocation not= ""
                Drawable thumbnail
                thumbnail:Load(terrainThumbnailLocation)
                terrain:SetPaletteIcon(thumbnail)
            else
                terrain:SetPaletteIcon(LoadDefaultIcon())
            end
            primitives3D:Add(terrain)

            LoadAssetPack(primitives3D)
```

### 10. L/Interface/Behaviors/Scenes/Palette/`PaletteItemStartDragBehavior.quorum`

**New `use`:** `use Libraries.Interface.Controls.Scenes.Blueprints.TerrainBlueprint`.

The lines

```
            dragPreview:SetPosition(dragEvent:GetX() - dragPreview:GetWidth()/2, dragEvent:GetY() - dragPreview:GetWidth()/2)
            palette:SetDraggedItem(dragPreview)
        else
```

become:

```
            dragPreview:SetPosition(dragEvent:GetX() - dragPreview:GetWidth()/2, dragEvent:GetY() - dragPreview:GetWidth()/2)
            palette:SetDraggedItem(dragPreview)
        elseif schematic is TerrainBlueprint
            // A terrain is dragged as its icon, as a model is (Item 71).
            TerrainBlueprint terrainBlueprint = cast(TerrainBlueprint, schematic)
            Drawable dragPreview
            dragPreview:Load(terrainBlueprint:GetPaletteIcon())
            dragPreview:SetPosition(dragEvent:GetX() - dragPreview:GetWidth()/2, dragEvent:GetY() - dragPreview:GetWidth()/2)
            palette:SetDraggedItem(dragPreview)
        else
```

### 11. L/Interface/Behaviors/Scenes/Palette/`PaletteItemDropBehavior.quorum`

**New `use`:** `use Libraries.Interface.Controls.Scenes.Blueprints.TerrainBlueprint`.

The lines

```
            controller:Add(layer, drawable)
            controller:IncrementNameCount(schematic:GetName())
        else
            palette:SetDraggedItem(undefined)
        end
```

become:

```
            controller:Add(layer, drawable)
            controller:IncrementNameCount(schematic:GetName())
        elseif schematic is TerrainBlueprint
            // Dropping a terrain anywhere on the scene adds it, flat, at
            // the origin (Item 71).
            palette:SetDraggedItem(undefined)
            controller:AddTerrain(cast(TerrainBlueprint, schematic))
        else
            palette:SetDraggedItem(undefined)
        end
```

### 12. SC/`SceneTree.quorum`

A terrain's region models are its children, but they aren't separate items
in the scene, so the tree doesn't list them.

**New `use`:** `use Libraries.Game.Graphics.Worlds.TerrainItem`.

In `ItemToTreeItems(Item3D item, EditorLayer layer)`, the lines

```
        Iterator<Item3D> children = item:GetItems()
        repeat while children:HasNext()
            treeItem:Add(cast(TreeItem, ItemToTreeItems(children:Next(), layer)))
        end
```

become:

```
        // A terrain's region models come from its files, and aren't
        // separate items in the scene (Item 71).
        if not (item is TerrainItem)
            Iterator<Item3D> children = item:GetItems()
            repeat while children:HasNext()
                treeItem:Add(cast(TreeItem, ItemToTreeItems(children:Next(), layer)))
            end
        end
```

### 13. Studio (S/)

**No Studio code is required:** the palette, editor and behaviors are all
library code. Two things:
1. **Optional icon.** Until there is one, the palette shows the default
   pink square for Terrain. To add one:
   - an image, `S/Resources/Graphics/SceneEditor/Primitives3D/TerrainThumbnail.png`
     (to make or choose);
   - in `S/SourceCode/Interface/Resources.quorum`, beside
     `CUBE_THUMBNAIL`:
     ```
         public constant text TERRAIN_THUMBNAIL = PRIMITIVES_3D_THUMBNAILS + "/" + "TerrainThumbnail.png"
     ```
   - in `S/SourceCode/Interface/QuorumStudio.quorum`, after
     `scenePalette:SetDefaultCustomCylinderThumbnail(resources:CUSTOM_CYLINDER_THUMBNAIL)`:
     ```
         scenePalette:SetDefaultTerrainThumbnail(resources:TERRAIN_THUMBNAIL)
     ```
2. **Builds** (the user's):
   - Studio's runtime library (`S/Run/QuorumStandardLibrary.jar`) rebuilt
     from `MakeAWish`, so Studio's editor has the new classes.
   - **Studio's copy of the standard library** (`S/Library/Standard`, from
     2026-09-25) brought up to date (T5), so games run from Studio can load a
     scene with a terrain.

### 14. Project: `AT/SourceCode/TerrainSceneCheck.quorum` (new)

A game that loads a scene made in Studio and checks the terrain in it. **No
keys.**

**Make the scene first, in Studio:**
1. open the AllTerrainVehicle project;
2. make a new scene, `Scenes/TerrainCheck.qs`;
3. add **Terrain** from the palette;
4. add a **Cube** above it: lift it 5 units, and turn on its physics;
5. save.

```
use Libraries.Game.Game
use Libraries.Game.Layer3D
use Libraries.Game.Graphics.Worlds.TerrainItem
use Libraries.Game.Graphics.DirectionalLight
use Libraries.Game.Graphics.AmbientLight
use Libraries.Interface.Item3D
use Libraries.Containers.Iterator

/*
TerrainSceneCheck loads a scene made in Quorum Studio with a terrain in it
(library item 71, QuorumStudioPlan.md): Scenes/TerrainCheck.qs, with a flat
terrain and a cube with physics above it.

What to see:
1. The output names the terrain, its region count (4) and the ground's
   height at the origin (0).
2. A flat, square ground, 128 units across, under the camera.
3. The cube falls and comes to rest on the ground.

Attribute: Example
// TerrainSceneCheck is a main program: set it as the project's main file
// and run it.
*/
class TerrainSceneCheck is Game
    DirectionalLight sun
    AmbientLight ambient

    action Main
        UseVulkan(true)
        StartGame()
    end

    action CreateGame
        EnablePhysics3D(true)
        SetGravity3D(0, -9.8, 0)
        LoadScene("Scenes/TerrainCheck.qs")

        sun:SetDirection(-1, -2, 1)
        sun:SetIntensity(2)
        Add(sun)
        ambient:SetColor(0.4, 0.4, 0.4, 1)
        SetAmbientLight(ambient)

        GetCamera3D():SetPosition(0, 25, -60)
        GetCamera3D():LookAt(0, 0, 0)

        TerrainItem terrain = FindTerrain()
        if terrain = undefined
            output "Terrain scene check: no terrain in the scene. Add one from Studio's palette and save."
        else
            output "Terrain scene check: " + terrain:GetName() + " from " + terrain:GetTerrainFile() + ", " +
                terrain:GetWorldTerrain():GetData():GetRegionCount() + " regions, height at the origin " +
                terrain:GetWorldTerrain():GetData():GetHeight(0, 0) + "."
        end
    end

    /*
    This action returns the first terrain in the 3D layer, or undefined.
    */
    action FindTerrain returns TerrainItem
        Layer3D layer = GetCurrentLayer3D()
        Iterator<Item3D> items = layer:GetIterator()
        repeat while items:HasNext()
            Item3D item = items:Next()
            if item is TerrainItem
                return cast(TerrainItem, item)
            end
        end
        return undefined
    end
end
```

**To check when applying:** whether physics enabled on a layer's item's
*children* is set up when the parent is added. The region models have
collision and are the item's children. If the cube falls through, the fix
goes in `TerrainItem` (or `Layer3D:Add`) before Item 71 is called done.

## Test plan

1. **The library builds;** AllTerrainVehicle builds after its Worlds
   folder is removed; `RunWorldTests` still passes all its tests.
2. **DriveOpen, TerrainEditor, TerrainWalk:** unchanged, now on the
   library's classes.
3. **Studio, a scene in a project:**
   - **Terrain** is in the palette's Basic 3D group;
   - activating it adds a flat terrain, selects it, and says where it was
     saved; `Terrains/Terrain1` appears in the project with `terrain.json`
     and four region images;
   - dragging it into the scene does the same, as `Terrain2`;
   - the scene tree lists it once, with no region children;
   - the move, turn and scale keys leave it in place;
   - Undo removes it, Redo puts it back; Delete removes it;
   - Save, close, reopen: the terrain is there.
4. **Studio, a scene not in a project:** activating Terrain says it can't,
   and nothing is added.
5. **TerrainSceneCheck,** as in its header.
6. **A scene without a terrain:** loads exactly as before, in Studio and in
   games.
7. **With a screen reader:** the palette entry reads "Terrain" with its
   description; placing it is announced.

## Rollback

- **Library:** delete W/'s 18 moved files and `TerrainItem`, the two new
  scene classes, and revert the five changed files.
- **Project:** restore `AT/SourceCode/Worlds` from git.
- **Scenes** with a terrain then load it as a plain `Item3D`.

---

# Item 70: terrain brushes in Quorum Studio (after Item 71)

**Status: plan.** It replaces the earlier terrain-tab plan (2026-10-19),
which kept copies of the Worlds classes in Studio's source. With Item 71,
the classes are in the library, and a terrain is already in the scene
editor, so the brushes go there.

**How it proceeds (the user's rules):**
- **One tool at a time,** each its own reviewed write-up, starting with the
  one the user picks. No batch of new keys.
- **Keys match Studio's.** Where a tool needs a key Studio doesn't have,
  that is asked about first, for that tool alone.
- **Undo and Redo** are Studio's: each brush stroke becomes an `Edit` in the
  scene editor's edit manager (`TerrainStrokeEdit` is already an `Edit`).
- **Saving:** saving the scene also calls `TerrainItem:SaveTerrain` for a
  terrain that was changed.

**What exists to build on** (in the library after Item 71):
- the brush engine (`TerrainBrush`, `TerrainTool`, `TerrainStroke`), with
  raise, lower, smooth, flatten, ramp and paint;
- undo copies (`TerrainStrokeEdit`, `TerrainEditHistory`);
- `TerrainData:RayCast` for the mouse, and `TerrainBrushPreview` for the
  ring;
- `TerrainGenerator` for whole-world generation and erosion;
- the in-game editor `AT/SourceCode/TerrainEditor.quorum`, as the
  reference for each tool's behavior and spoken values.

**Things to settle for the first tool:**
1. **Which tool first:** raise and lower (recommended) or paint.
2. **How the editor enters "sculpting" for a selected terrain:**
   - through the existing selection (the terrain is focused, and the
     brush follows the scene cursor); or
   - a context menu item.
   No new global key either way.
3. **Mouse picking of the terrain** (`TerrainData:RayCast`), which Item 71
   left out.

**Before the first tool, check in Studio (Stage 0 of the earlier plan):**
does 3D under Vulkan draw correctly inside the editor's panel? The 3D
painter's lighting pass reads the G-buffer across the whole window while
drawing into the panel's viewport, which may shrink or offset the scene in
a panel. If so, a small library fix comes first (the lighting shader reads
the G-buffer at the pixel, and the scissor, result copy and sky use the
viewport). It is needed by the scene editor anyway.

**Later, each optional:** ground layers chosen in Studio (texture sets
saved with the terrain), a `Slider` control (T6), streaming terrains in
scenes, sea level and lakes in a scene (after Items 39 to 43).
