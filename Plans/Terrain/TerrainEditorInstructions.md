# Terrain Editor: user guide

The Terrain Editor shapes and paints large outdoor worlds: hills, mountains,
cliffs, valleys and the ground textures on them. There is no vehicle and no
physics. You move a brush over the ground and change it.

Everything can be done from the keyboard. Everything you do is written to
the console, shown along the bottom of the screen, and announced to a
screen reader if one is running.

## Starting

1. Make `SourceCode/TerrainEditor.quorum` the project's main file.
2. Build and run.

The editor uses the Vulkan renderer.

- **The first time,** there is no saved world yet. The editor makes a new
  one: 8 by 8 regions of rolling hills, about 256 by 256 world units,
  already painted with grass and rock.
- **After you save** (Control and S), the editor opens your saved world
  instead. Worlds are saved in the `EditorTerrain` folder of the project.

Until you save, every run starts with a fresh world. To start over, delete
or rename the `EditorTerrain` folder.

## What you see

- **The ground,** seen from a camera that circles around a point on it.
- **The brush:** two yellow rings lying on the ground. The outer ring is the
  brush's edge. The inner ring, at half the size, is where the brush works
  at about half strength. Changes are strongest in the middle and fade to
  nothing at the outer ring.
- **The information line,** at the bottom left. It shows:
  - the brush's settings;
  - where the brush is (x and z), how high and steep the ground is there,
    and which ground texture is on it;
  - what Control and Z would undo.

  When you change something, the line shows that message for a moment.
- **The frame rate,** in the corner.

## Moving the brush

- **With the mouse:** point at the ground. The brush follows the pointer.
- **With the keyboard:** the arrow keys move the brush across the ground,
  and the camera follows. Up moves away from the camera, Down toward it, and
  Left and Right sideways.
  - Each press moves a short step. Hold a key to keep moving.
  - Hold Shift for steps eight times longer.
  - After each step, the editor says where the brush is, for example
    "x 12.5, z 30.0, height 14.2, slope 18 degrees, grass".

You can switch between the two at any time. The arrow keys start from
wherever the mouse left the brush, and moving the mouse takes over again.

## Moving the camera

The camera always looks at a point on the ground and circles around it.

| To | Mouse | Keyboard |
|---|---|---|
| Turn around the point | Drag sideways with the right button | Q and E |
| Look down more or less steeply | Drag up and down with the right button | Home and End |
| Come closer or go further | Scroll wheel | Page Up and Page Down |
| Move across the ground | Drag with the middle button | Arrow keys |
| Look at the brush | | C |

When you let go of the camera keys, the editor tells you where the camera
faces, for example "Camera facing 62 degrees, looking down 45 degrees, 70
away".

## Using the brush

Hold the **left mouse button** or **Space** to use the brush. Drag (or hold
an arrow key while holding Space) to make a stroke. Everything from pressing
to letting go is one stroke, and one step of undo.

Press **Escape** during a stroke to cancel it and put the ground back.

**Hold the button or key.** The brush works for as long as it's held, so a
quick click changes very little, especially with smooth, flatten, ramp and
paint. Hold for a second or more, or press `=` for a stronger brush.

When a stroke ends, the editor describes the ground under the brush, for
example: "raise done, 4 regions touched. Under the brush: heights from 14.0
to 18.1, steepest slope 33 degrees, grass 100 percent."

### The tools

Choose a tool with the number keys.

| Key | Tool | What it does |
|---|---|---|
| 1 | Raise | Lifts the ground. |
| 2 | Lower | Digs the ground down. |
| 3 | Smooth | Evens out bumps and sharp edges. A larger brush smooths broader shapes. An even slope stays a slope; smoothing rounds off its top and bottom edges. |
| 4 | Flatten | Pulls the ground toward one height (see below). |
| 5 | Ramp | Pulls the ground onto a straight slope between two points (see below). |
| 6 | Paint | Paints a ground texture: grass, rock, dirt or sand. |
| 7 | Blend paint | Softens the edges between painted textures. |
| 8 | Add region | Adds a new square of flat land where the brush is, to grow the world. Use it past the edge of the land, where there is no ground yet; over land it does nothing. |
| 9 | Remove region | Removes the square of land under the brush. |

### Brush settings

| Keys | Setting |
|---|---|
| `[` and `]` | Smaller and larger brush. |
| `-` and `=` | Weaker and stronger brush. For raise and lower, strength is how many units per second the middle moves. For the other tools, a stronger brush reaches its goal faster. |
| `;` and `'` | Softer and sharper falloff. Softer spreads the effect toward the edge; sharper keeps it near the middle. |
| L | Next texture to paint: grass, rock, dirt, sand. |
| G | Slope limit: any ground, flat ground only (under 15 degrees), or steep ground only (over 25 degrees). For example, choose steep only and paint rock to cover just the cliffs. |
| B | How softly grass and rock blend where they meet: crisp, medium, soft or softest. See "Ground textures" below. |

### Flattening

The flatten tool (4) pulls ground toward a target height.

- **F** switches between flattening to the height where each stroke starts,
  and flattening to a set height.
- **T** sets the height to the ground under the brush.

To make a level terrace: move the brush to the height you want, press T,
then flatten around it.

### Ramps

The ramp tool (5) needs two points first.

1. Put the brush where the ramp should start, and press **R**.
2. Put the brush where the ramp should end, and press **R** again.
3. Choose tool 5 and brush along the line between them.

The ramp runs from the height of the ground at its start to the height at
its end. Past either end it stays level. The editor tells you the ramp's
slope when you set its end.

Put the ends at **different heights**, such as the bottom and top of a hill.
If both ends are at about the same height, the ramp is level, and brushing
along it just flattens the ground. Pressing R again starts a new ramp.

## Ground textures

Grass and rock are full texture sets: mossy ground and pebbles, with
their own bumps, shine and shading in the cracks. Dirt and sand are plain
textures.

Where grass and rock meet, they blend **by height**: the pebbles poke up
through the grass first, rather than the two fading evenly into each other.
**B** changes how softly they blend: crisp shows sharp-edged stones in the
grass, softest is close to an even fade.

## Making worlds

These tools change a lot at once. Each one is still a single step of undo.

### Presets: whole new landscapes

- **N** chooses a preset: rolling hills, mountains, plateaus, canyons or
  flat.
- **Shift and N** remakes the whole world with that preset, and paints it
  by the auto paint rules.

Each time you press Shift and N, a new seed gives a different landscape of
the same kind. Mountains are twice as tall as the others.

### Stamps: shapes in one press

- **K** chooses a stamp: hill, mountain, mesa, crater or ridge.
- **Comma** and **period** make the stamp lower and higher.
- **Slash** turns the stamp 45 degrees (useful for the ridge).
- **Enter** presses the stamp into the ground at the brush. The stamp is as
  wide as the brush.

Most stamps stand on the ground under the brush's center, and never lower
ground that is already higher. The crater is cut in: a rim around a bowl.

Tips:
- **Stamps stack.** Pressing Enter again in the same place stands the new
  stamp on top of the last one. Repeat it for a taller peak; press it once
  for a single one.
- A tall stamp on a small brush makes cliffs. For gentle shapes, make the
  brush larger (`]`) or the stamp lower (comma).
- Stamps don't paint. Press U afterward to paint the new shape (see below).

### Erosion: weathering

- **X** weathers the ground under the brush. Drops of rain run downhill,
  carrying ground from steep places and leaving it in hollows. Then
  over-steep slopes settle. The rain starts under the brush and may run one
  brush size past it, so ground is carried away downhill.
- **Shift and X** weathers the whole world. This takes a while.

Erosion moves ground but never adds or removes it. Press X a few times to
soften a fresh stamp into something that looks weathered.

### Auto paint

- **U** paints the ground under the brush by rules:
  - rock on slopes, starting at 25 degrees and fully rock by 40;
  - sand on low ground, below a height of 3;
  - dirt in hollows, where the ground lies lower than the land around it;
  - grass everywhere else.
- **Shift and U** paints the whole world by the same rules.

Auto paint replaces hand painting where it's used. If the ground already
matches the rules, it says it changed nothing.

## Undo, redo and saving

| Keys | What |
|---|---|
| Control and Z | Undo the last stroke or tool. |
| Control and Y, or Control, Shift and Z | Redo. |
| Control and S | Save the world to `EditorTerrain`. |

On a Mac, Command works in place of Control.

- Undo goes back through your strokes, and the information line says what
  is next to undo.
- Starting a new stroke after undoing clears what could be redone.
- Undo keeps up to about 256 MB of history. Past that, the oldest steps are
  forgotten.
- Saving doesn't clear the undo history.
- Regions you removed stay removed after saving and loading again.

## Hearing and asking about the ground

| Key | What it says |
|---|---|
| I | The ground under the brush, the brush's settings, and what Control and Z would undo. |
| A | The area under the brush: lowest and highest heights, steepest slope, and how much of each texture. |
| H | The main keys. The full list is written to the console. |

## Things to know

- **Removing a region** removes it from memory and from the saved list. Its
  old image files stay in the `EditorTerrain` folder, unused.
- **Adding regions outside the current land's square** makes the ground
  texture map bigger. A bigger map is a new texture, which the renderer
  can't yet give a place the terrain shader can use, so the ground turns
  grey. Save and restart the editor, and it looks right again. Adding
  regions inside the square (filling a gap) doesn't do this.
- **Big brushes over many regions** are slower, since every region they
  touch is redrawn as you go.
- **Remaking or eroding the whole world** can take some seconds, during
  which the editor doesn't respond.

## Key reference

| Key | Action |
|---|---|
| Left mouse or Space (hold) | Use the brush |
| Mouse pointer | Move the brush |
| Arrow keys (Shift for long steps) | Move the brush and camera |
| Right mouse drag, or Q, E, Home, End | Turn and tilt the camera |
| Scroll wheel, or Page Up, Page Down | Zoom |
| Middle mouse drag | Move the camera |
| C | Look at the brush |
| 1 to 9 | Raise, lower, smooth, flatten, ramp, paint, blend paint, add region, remove region |
| `[` `]` | Brush size |
| `-` `=` | Brush strength |
| `;` `'` | Brush falloff |
| L | Paint texture |
| F, T | Flatten to stroke start, or set the flatten height |
| G | Slope limit |
| B | How softly grass and rock blend |
| R | Ramp start, then ramp end |
| Escape | Cancel the stroke |
| N, Shift N | Choose a preset; remake the world with it |
| K | Choose a stamp |
| Comma, period, slash | Stamp lower, higher, turn |
| Enter | Press the stamp |
| X, Shift X | Erode under the brush; erode the world |
| U, Shift U | Auto paint under the brush; auto paint the world |
| Control Z, Control Y, Control S | Undo, redo, save |
| I, A, H | Describe here, describe the area, list the keys |
