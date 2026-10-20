# Handoff: terrain in Quorum Studio

Last updated: 2026-10-20 (end of session). Read this first on a new
machine, then `QuorumStudio.md` (the master plan) and the step file for
whatever is next.

## Repositories and branches

**Never switch branches.** Use the one checked out.

| Repository | Branch | What's in it for this work |
|---|---|---|
| `quorum-language` | `MakeAWish` | Standard library: terrain classes, scenes, scene editor, physics and camera changes |
| `quorum-studio` | `MakeAWish` | Studio, and these plans (`Plans/`) |
| `physics` | `MakeAWish` | `AllTerrainVehicle`: DriveOpen, TerrainEditor, tests, `TerrainSceneCheck`, the Open Worlds documents |
| `quorum-natives` | `main` | The native libraries (image loading, fonts) and AutoBuild |

Paths in the plans:
- **L/** = `quorum-language/Quorum/Library/Standard/Libraries/`;
- **W/** = `L/Game/Graphics/Worlds/`;
- **SC/** = `L/Interface/Controls/Scenes/`;
- **QS/** = `quorum-studio/QuorumStudio/SourceCode/`;
- **AT/** = `physics/AllTerrainVehicle/`.

## Working rules

- **The user builds and runs everything.** Never run the programs or the
  compiler yourself.
- **Standard library changes** need the user's go-ahead for each change
  ("go ahead with step N" covers that step). Plans, investigations and
  reviews mean no code changes.
- **The physics test projects** (`physics/...`) can be changed freely.
- **Brushes come one tool at a time,** each its own reviewed write-up.
- **Keys:** use Studio's existing keys where the meaning carries over.
  Every new key must be overridable (step 6).
- **The user's design decisions** are recorded in `QuorumStudio.md`
  ("Decisions") and the step files. Don't reopen them.

## Where things stand

| Step | State |
|---|---|
| 1. Worlds classes into the library | **Done.** 18 classes in W/, removed from AT/; `TerrainData:GetFileInFolder` keeps the folder's working directory |
| 2. `TerrainItem` and terrain in scenes | **Done and confirmed:** `TerrainSceneCheck` reported 4 regions, height 0, collision true, friction 0.8, and no splat images |
| 3. Terrain in the scene editor | **Applied.** Confirmed so far: Terrain shows in the palette ("Basic 3D", with its icon). **Not yet confirmed:** everything else on step 3's test list (below) |
| 4 to 13 | Plans only (`Steps/`) |

**Commits:** the user is committing the library, physics and Studio work
from this session into `MakeAWish`. `quorum-natives` has an uncommitted
README update.

## Next

1. **Finish testing step 3 in Studio** (`Steps/03-SceneEditorTerrain.md`,
   "Tests"):
   - **activating** Terrain adds a flat 128 × 128 terrain:
     `Terrains/Terrain1` appears in the project with `terrain.json` and
     four region images, and no splat images; it's selected and announced;
   - **dragging** it in adds another (`Terrain2`), on drop only. Dragging
     over the scene and back off adds nothing;
   - **the scene tree** lists it once, with no region children;
   - **selecting it:**
     - from the tree, a thin highlight slab appears, with no error;
     - clicking the ground selects it;
     - clicking a cube standing on it selects the cube;
     - the cursor's Enter at ground level selects it;
   - **the move, turn and scale keys** leave it in place, and say "A terrain
     stays at the origin" once per selection;
   - **Delete, Undo and Redo** work;
   - **save, close, reopen:** it's still there;
   - **a scene not in a project:** adding says it can't;
   - **with a screen reader:** "Terrain" with its description in the
     palette, and placing it is announced.
2. **Then step 4 (the Properties panel)**, which doesn't need the terrain
   steps:
   - a panel docked on the right **inside the scene tab** (decided);
   - it replaces the floating "Object Properties" dialog
     (`SC/Dialogs/Item3DPropertiesDialog`) for every item.
3. **Then step 5** (the terrain's info and physics sections in that panel),
   and **step 6** (keys) before any brush.

## How to build and try Studio

1. Build the standard library from `quorum-language` (`MakeAWish`).
2. Build the Studio project (`quorum-studio/QuorumStudio`). Its
   `Run/QuorumStandardLibrary.jar` must come from `MakeAWish`; check that
   it contains `TerrainBlueprint`:

   ```bash
   unzip -l /Users/stefika/Repositories/quorum-studio/QuorumStudio/Run/QuorumStandardLibrary.jar | grep TerrainBlueprint
   ```
3. **Run Studio with the project folder as its working directory,** the
   way the user normally runs it. Studio finds its images by paths relative
   to `quorum-studio/QuorumStudio`, so starting it from inside `Run/` loses
   its icons. The development build starts in Vulkan.
4. **The installed `/Applications/QuorumStudio.app` is old** (jars from
   2026-08-05). It has none of this work.

**Checking step 2 without Studio:** `AT/SourceCode/TerrainSceneCheck.quorum`.
Set it as the main file and run it twice: the first run makes the terrain
and scene, and the second loads them and reports. Delete
`AT/Scenes/TerrainCheck.qs` and `AT/Terrains/TerrainCheck` to start over.

## What changed this session (2026-10-20)

**Natives (`quorum-natives`, committed `948bb43` and `daa3c65`; libraries
in `quorum-language` `d2ffa8518`):**
- **Image loading:** stb_image 2.29. 16-bit PNGs and progressive JPEGs
  load. The crashing jpgd fallback is gone. There's a `requestedChannels`
  argument.
- **Fonts:** `FreeTypeStrategy` `GetFamily` and `GetStyleName` are back.
- **AutoBuild** builds Mac, Windows (MinGW) and iOS from one CMakeLists on
  a Mac, installing CMake and MinGW with Homebrew. See
  `quorum-natives/README.md`, `AutoBuildChanges.md` and `LinuxNatives.md`
  (Linux is plan only).

**Physics and cameras (library, committed `10c6ef617` and `feda793e1`):**
- **Smooth drawing:** physics items are drawn between their last two
  steps (`Item3D:GetDrawnTransform`).
- **A game hook after physics:** `Game:UpdateAfterPhysics(seconds)`.
- **A chase camera:** `Camera:Follow(item, behind, above)`. Angle 90 is the
  item's right.
- **DriveOpen** uses `Follow`, and its jitter is gone. See
  `Terrain/GameLoopAfterPhysics.md` and `Terrain/CameraFollowPlan.md`.

**16-bit textures (project):** DriveOpen and TerrainEditor have
`useOriginal16BitTextures = true` (confirmed on screen).

**Terrain in Studio (steps 1 to 3):**
- **Library, new:** `TerrainItem`, `TerrainBlueprint`, `EditorTerrain`,
  `TerrainSelectionHighlight`.
- **Library, changed:** `TerrainRegion` (`splatPainted`), `TerrainData`,
  `DefaultSceneManager`, `SceneEditor`, `ScenePalette`, `SceneTree`,
  `EditorCursor3D`, `SelectScene3DItemBehavior`,
  `SceneEditorAcceptDragBehavior`, `PaletteItemStartDragBehavior`.
- **Studio:** `TerrainThumbnail.png`, `Resources.TERRAIN_THUMBNAIL`, and
  the `SetDefaultTerrainThumbnail` call in `QuorumStudio.quorum`.
- **Project:** `TerrainSceneCheck.quorum`.

## Decisions made this session (summary; details in `QuorumStudio.md`)

- **Basic terrain first,** no brushes. A terrain stays at the origin.
  Vulkan only for now.
- **A new terrain is flat, with no splat and no materials.** Splat images
  are written only for painted regions.
- **Materials are files** the scene refers to:
  - every file is plain `.json`, with its `"Format"` naming its kind;
  - paths inside a file are relative to that file;
  - the scene names a terrain's material;
  - physics settings stay inline in the scene;
  - all of this comes in step 9, "Materials and painting".
- **The Properties panel** goes inside the scene tab, on the right.

## Open problems found along the way (not yet fixed)

- **Default palette icons don't draw under Vulkan.**
  `ScenePalette:LoadDefaultIcon` draws a pink square with
  `LoadFilledRectangle`, which didn't show. Any palette entry without a
  thumbnail is invisible. (Terrain now has one.)
- **Skyboxes aren't saved in scenes:** `DefaultSceneManager:SaveSkybox` is
  never called.
- **Studio's key map:** its scene-editor entries are never read, and some
  defaults don't match the real bindings (step 6).
- **`SceneEditorInputTable`'s constructor** names it
  `"CodeCompletionMenuRoot"`. It works only because `DefaultInputSets`
  renames it.
- **Property edits in the scene editor can't be undone** (only adding and
  deleting).
- **The keyboard can't move the 3D editor camera:** those behaviors exist
  but aren't bound.
- **`Layer2D`** doesn't interpolate physics drawing, unlike `Layer3D` now.
- **`PixelMap.java`** could ask the loader for 4 channels and skip the
  Vulkan RGBA conversion (`AT/VulkanChanges.md` F and G).
- **TerrainWalk's `CameraFollower`** source is missing from the project. It
  could now use `Camera:Follow`.

## Documents

- **This folder:**
  - `QuorumStudio.md`: the master plan;
  - `Steps/01` to `13`: one file per step;
  - `Terrain/`: copies of the background documents, indexed in
    `README.md`.
- **Elsewhere:**
  - `AT/OpenWorldsHandoff.md`: the Open Worlds handoff, with a 2026-10-20
    section;
  - `AT/VulkanChanges.md`: renderer and library bugs, including G, I and J
    from this session;
  - `physics/QuorumStudio/QuorumStudioPlan.md`: the first Studio plan,
    marked superseded, with exact code used for steps 2 and 3.
