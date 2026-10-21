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

## On the second machine (`/Users/stefik/`, checked 2026-10-20)

- **Library and Studio** are at the same commits as above (`2a4bf3a2a`,
  `c57ea108`): steps 1 to 3 and the Terrain icon are all present.
- **The physics checkout had `AT/SourceCode/Worlds`** (the 18 old copies)
  and no `TerrainSceneCheck.quorum`: the first machine's physics changes
  for steps 1 and 2 never reached `origin/MakeAWish`. AllTerrainVehicle
  then failed to build against the new library ("TerrainTool.class was
  missing"). **Fixed 2026-10-20:** the folder is removed with `git rm`
  (staged, not committed). The copies differed from the library's only
  where steps 1 and 2 changed the library. Do a clean and build of
  AllTerrainVehicle. `TerrainSceneCheck.quorum` is still missing here.
- **Step 3's code was read through** (`SceneEditor`, `EditorTerrain`,
  `TerrainSelectionHighlight`, the click, cursor and drag behaviors). No
  problems found before testing.

## Next

1. **Finish testing step 3 in Studio** (`Steps/03-SceneEditorTerrain.md`,
   "Tests"):
   - **activating** Terrain adds a flat 128 × 128 terrain:
     `Scenes/Terrains/Terrain1` appears in the project with `terrain.json` and
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
   steps. **Applied 2026-10-20** as written in `Steps/04-PropertiesPanel.md`
   (library: `PropertiesPanel`, `PropertiesDialog`, `SceneEditor`,
   `UnfocusPropertiesBehavior`; Studio: `VisualEditorTab`, `QuorumStudio`,
   `PropertiesPartitionDragBehavior`). Not built or run yet. It docks the six existing dialogs in a
   new `PropertiesPanel` instead of rewriting them into section classes;
   step 5's terrain properties become a seventh dialog, added with
   `SceneEditor:AddPropertiesDialog`.
   - a panel docked on the right **inside the scene tab** (decided);
   - it replaces the floating "Object Properties" dialog
     (`SC/Dialogs/Item3DPropertiesDialog`) for every item.
3. **Step 5 applied 2026-10-20** (`Steps/05-TerrainProperties.md`): the
   Terrain and Physics sections in the panel, and a fix so a terrain added
   in Studio is saved with collision on.
4. **Steps 6 and 7 applied 2026-10-20** (reviewed by the user first):
   - **6:** every scene-editor key comes from `SceneEditorKeys` (library) and,
     in Studio, from the key map (`StudioSceneEditorKeys`), so a user's key
     map file can change it; a key missing from an old file takes its
     default instead of the file being thrown away;
   - **7:** Alt+1 to Alt+9 (or the Brush section) choose a terrain tool; a
     ring shows the brush, the pointer and arrows move it, Alt+= / Alt+-
     size it, Escape leaves the tool. No strokes yet: step 8, one tool at a
     time.
5. **Step 8, raise and lower, applied 2026-10-20:** Space held or a left
   drag makes a stroke; Alt+] / Alt+[ change the strength; Escape cancels
   a stroke; Studio's Undo and Redo take a stroke back and forward; saving
   the scene saves the terrain. Two fixes went in first: the terrain's
   highlight is a border instead of a solid slab, and the Vulkan lighting
   pass no longer shrinks a 3D view that is smaller than the window.
6. **Step 9 applied 2026-10-20:** material files (`Material:Load/Save`),
   `TerrainMaterial` files, the scene's `"Material"`, a Materials section
   in the panel (add, choose, remove layers, tile size, blend softness, with
   undo), and the Paint tool with a paint layer choice. Blend paint is next.
   **First test:** add one texture set in Studio and check the ground shows
   it (texture places, the risk in the step).
7. **Steps 10 and 11 applied 2026-10-20:** a Generate section (presets
   with seed and height, auto paint, erode and auto paint the whole
   terrain; at the brush: stamp, erode, auto paint), the Add and Remove
   region tools (Alt+8, Alt+9; add region works past the ground's edge),
   and "Grow by one region on every side" in the Terrain section. All
   undoable. Saving leaves a removed region out of `terrain.json` but
   keeps its files (the user's decision).

## How to build and try Studio

1. Build the standard library from `quorum-language` (`MakeAWish`).
2. Build the Studio project (`quorum-studio/QuorumStudio`). Studio
   manages its own copy of the library; it isn't part of this plan.
3. **Run Studio with the project folder as its working directory,** the
   way the user normally runs it. Studio finds its images by paths relative
   to `quorum-studio/QuorumStudio`, so starting it from inside `Run/` loses
   its icons. The development build starts in Vulkan.
4. **The installed `/Applications/QuorumStudio.app` is old** (jars from
   2026-08-05). It has none of this work.

8. **Step 12 applied 2026-10-20:** fuller report location for the brush,
   the height on each arrow move, "Describe the area" in the Brush section,
   and stroke results such as "Raised by up to 2.4". No Help menu changes.

9. **Step 13 applied 2026-10-20:** `TerrainItem` updates itself each
   frame; collision is Full, Dynamic or Disabled; a terrain can stream in
   games; the Physics section sets both. QuincyWorld's `Main.quorum` drives
   the toy car (copied to `QuincyWorld/Car`) on the scene's terrain.

10. **Step 14 applied 2026-10-20:** Details... on each material layer
    shows and changes each map's file, the normal map's convention,
    roughness, metallic and the name, with undo; materials are shared by
    file across a scene; adding a material knows Poly Haven's and other
    common names and says which maps it found.

**The test project for terrain** is
`/Users/stefik/Repositories/worlds/QuincyWorld` (from 2026-10-20). Its scene
had two terrain entries naming the same `Terrains/Terrain1` folder, so two
terrains drew in one place; the duplicate entry was removed, the folder
moved to `Scenes/Terrains/Terrain1`, and `Main.quorum` now calls
`SetMultisampling3D`. The scene loader now leaves out a second terrain
naming the same files.

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
- **Studio, palette icons (2026-10-20):** new cube, cylinder, sphere, point
  light and audio icons, and new Custom 3D icons (Custom Model: a pawn with
  a document; Custom Cube, Sphere and Cylinder: the shapes wearing a
  picture), all in the terrain icon's soft "clay" style, replacing the old
  ones. Same names and 80 × 80 size, so no code changed.
  They're made by `QuorumStudio/Resources/Graphics/SceneEditor/MakePaletteIcons.py`
  (plain Python 3; rerun it to change them).
- **Studio, project, Quorum-file and main-file icons (2026-10-20):** made
  from the bunny-at-a-laptop art (cleared for Studio): the bunny's head on
  its own for projects (a disc around it distracted at this size), the head
  on a page for Quorum files, and with a red bar under it for the main file. All nine theme
  copies (`Interface/QuorumSmall*.png`, `Trees/QuorumFile*.png`,
  `Trees/MainFile*.png`) are now **40 pixels high** (40 × 40 and 32 × 40), the size the tree and tabs draw them
  on a high-density screen. The old 500-pixel images looked pixelated
  because Studio shrinks them without mipmaps; and under OpenGL, loading
  with mipmaps (`LoadFromFile(path, true)`) leaves a texture blank today
  (the mipmap branch of `Texture:UploadImageData` is unfinished). Right-sized
  images avoid both. Source and scripts:
  `QuorumStudio/Resources/Graphics/Interface/BunnyIconSource/`.
- **Studio, folder icons (2026-10-20):** an open 3D folder with papers in
  it, in the flat icons' colors (front panel measured to match): tan
  `FolderDark.png` (dark theme), bright blue `FolderLight.png` (light and
  basic), dark blue `Folder2.png` (default). 40 × 40, like the bunny icons.
  Made by `QuorumStudio/Resources/Graphics/Interface/Folder3D.py`, which
  uses `../SceneEditor/MakePaletteIcons.py`; run it with no arguments to
  remake all three.
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
