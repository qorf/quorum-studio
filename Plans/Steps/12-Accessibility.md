# Step 12: accessibility (describing the ground, spoken values)

Part of `../QuorumStudio.md`. **This goes along with every step,** not
after them. Each step's write-up names what it says.

## What exists

The in-game editor (`AT/SourceCode/TerrainEditor.quorum`) describes
everything:
- **`DescribeGround`:** the height, the slope, and the main layer under the
  brush (key I).
- **`DescribeArea`:** the lowest, highest and average height, and the
  layer shares under the whole brush (key A).
- **`AnnounceHelp`:** lists the keys (key H).
- **Every change of tool, size, strength or setting** is spoken, through
  `Accessibility:Notify`.
- **After each stroke,** the change is spoken ("Raised by up to 2.4").

The guide is `Terrain/TerrainEditorInstructions.md`.

## In Studio

- **Speaking:** `SceneEditor:NotifyAccessibility(item, message)` and
  `EditorItem:NotifyAccessibility(message)` already speak through Studio's
  screen-reader support. The terrain uses these.
- **Describing the ground:** the existing "report location" key
  (Ctrl+Shift+1) says, for a terrain with a tool chosen:
  - where the brush is;
  - the ground height and slope there;
  - the main layer.
  This is the in-game editor's I.
- **Describing the area:** "Describe the area under the brush," as a
  Properties panel button. A key is asked about if wanted (the in-game
  editor's A).
- **Help:** Studio's help covers the terrain keys; the guide becomes a
  Studio help page.
- **The Properties panel:** every field has a label that's read with its
  value. Sections are read as groups.
- **Sounds (sonification):** an open idea from `Terrain/TierFour.md` is a
  tone whose pitch follows the height under the brush. It's not in this
  plan, and is listed here so it isn't lost.

## Each step's spoken messages (to fill in with each write-up)

| Step | When | What is said |
|---|---|---|
| 3 | Adding a terrain | "Placed Terrain, flat, 128 by 128 units, saved in Scenes/Terrains/Terrain1." |
| 3 | Trying to move it | "A terrain stays at the origin." |
| 9 | Adding a material | "Added Rocks002 as layer 2, tiles 2.5 units. Paint it with the Paint tool." |
| 7 | Moving the brush | "Height 3.2" (short), or the full report with Ctrl+Shift+1 |
| 8 | Ending a stroke | "Raised by up to 2.4" |
| 10 | Generating | "Generating mountains...", then "Done, heights 0 to 48" |
| 11 | Adding a region | "Added region 1, 0. The terrain is now 192 by 128." |

## Tests

With VoiceOver on Mac and NVDA on Windows, each step's messages are heard,
and every field and button in the terrain's sections can be reached and
understood without the mouse.
