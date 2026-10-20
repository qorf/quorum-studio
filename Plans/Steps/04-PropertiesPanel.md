# Step 4: the Properties panel, docked on the right, for every item

Part of `../QuorumStudio.md`. It doesn't need the terrain steps, so it can
be done in parallel with steps 2 and 3. Step 5 adds the terrain's sections.

## Today

- **The window:** selecting an item in the scene editor shows a floating
  dialog over the editor's top-right corner, "Object Properties"
  (SC/Dialogs/`Item3DPropertiesDialog`).
  - It is pinned with `PropertiesDialog:Show` (SC/Dialogs/PropertiesDialog.quorum:105-130).
  - It holds "Basic Properties" (name, position, scale, rotation, color)
    and "Physics" (enabled, responsiveness, mass, friction, restitution).
- **One dialog per kind of item,** six in all: `Item3DPropertiesDialog`,
  `PointLightPropertiesDialog`, `Audio3DPropertiesDialog`,
  `Camera3DPropertiesDialog`, `Item2DPropertiesDialog` and
  `Camera2DPropertiesDialog`.
  - `SceneEditor` makes all six and registers them as selection listeners
    (SC/SceneEditor.quorum:176-224).
  - Each one shows itself when the selection is its kind of item, and
    hides otherwise.
- **Keys:**
  - Cmd/Ctrl+P (`FocusItemProperties`) moves the focus into the showing
    dialog.
  - Escape or Enter in a field returns the focus (the `"PropertiesDialog"`
    input table).
- **Edits** go straight onto the item, and can't be undone.

## The change

A **Properties panel docked on the right of the scene tab** replaces the
floating dialogs:
- it always shows the selected item's properties, and nothing is drawn
  over the scene;
- every item gets the same sections as today;
- a terrain gets more (step 5);
- with nothing selected, it shows the scene's own settings (optional; see
  "Later").

### Library (SC/)

1. **The contents and the window are separated.** Each `PropertiesDialog`
   subclass already builds its fields with `CreateInterface(panel,
   focusArray)`, fills them with `SetFields`, and applies edits in
   `TextChanged` and `SelectionChanged`.
   - That content moves into a **`PropertiesSection`** class. There's one
     per kind of item: `Item3DProperties`, `PointLightProperties`,
     `Audio3DProperties`, `Camera3DProperties`, `Item2DProperties`,
     `Camera2DProperties`.
   - Each section has:
     - `CreateInterface(Control panel, Array<Control> focusArray)`;
     - `AppliesTo(Item item) returns boolean`, which is the test each
       dialog's `SelectionChanged` makes today;
     - `SetFields(Item item)`;
     - `GetTitle returns text`.
   - The existing dialogs keep working by holding their section, so any
     other program that uses `SceneEditor` without the panel behaves as
     before.
2. **New `PropertiesPanel is Control, SelectionListener`:**
   - a vertical `FlowLayout` in a `ScrollPane`, named "Properties" for
     screen readers;
   - it holds an ordered list of sections, and on each selection change
     shows every section that applies to the selected item, in order;
   - its own sections come before an item's added ones, so a terrain shows
     "Basic", then "Terrain", "Ground", "Physics";
   - the tab order runs through the visible fields, as the dialogs' does
     (`PropertiesDialog.quorum:73-82`), and Escape or Enter returns focus
     to the editor (the existing `"PropertiesDialog"` input group, kept).
3. **`SceneEditor`:**
   - `SetPropertiesPanel(PropertiesPanel)`. When a panel is set, the
     floating dialogs are never shown, and the panel listens to the
     selection instead.
   - `FocusItemProperties` (Cmd/Ctrl+P) focuses the panel's first field.
   - `UpdateActivePropertiesDialog` refreshes the panel's fields. It's
     called as items move, so the position fields follow.
   - `HideDialogs` leaves the panel alone.
4. **Sections can be added from outside:** `PropertiesPanel:Add(PropertiesSection)`.
   That's how step 5 adds the terrain sections, and how a later item type
   adds its own.

### Studio (QS/)

**Decided (the user, 2026-10-20): the panel lives inside the scene tab,**
to the right of the editor, so code tabs don't change:
- **`VisualEditorTab:Load`** (QS/Interface/Controls/VisualEditorTab.quorum:60-71)
  wraps the editor in a container with a horizontal `FlowLayout`:
  - `SceneEditor`, filling the width;
  - a 4-pixel drag bar, input group `"PropertiesPartition"`, built like
    `navigationPartition` (QuorumStudio.quorum:709-716), with a drag
    behavior like `NavigationPartitionDragBehavior`;
  - `PropertiesPanel`, 300 pixels wide by default, filling the height.

  The container becomes the tab's related item, and the focus target stays
  the editor. The tab then calls `editor:SetPropertiesPanel(panel)`.
- **`SetupInputTables`** (QuorumStudio.quorum:538-609) gets the
  `"PropertiesPartition"` drag table, next to the navigation and editor
  partitions.
- **Width:** the panel's width is saved in Studio's options, as other
  layout settings are (optional).

**Why inside the tab rather than a third column for all of Studio:** it's
only for scenes, and it sits beside the scene it describes. A column for
all of Studio, beside the code tabs and shown only for scene tabs, would
also work, but it changes Studio's main layout
(QuorumStudio.quorum:676-707) and the resizing of every tab.

## Keys

None new:
- Cmd/Ctrl+P focuses the panel (today it focuses the dialog);
- Tab and Shift+Tab move through the fields;
- Escape or Enter returns to the editor.

## Undo (optional, recommended)

Property edits can't be undone today. With one panel, they can:
- a **`SceneEditorPropertyEdit is Edit`** keeps the item, the property's
  name, and the old and new values;
- the panel records one when a field's change is committed (Enter, or the
  focus leaving the field), not on every keystroke.

This could be its own small step (4b).

## Later

- **With nothing selected:** the scene's own settings, which are saved in
  the `.qs` but only editable in the file today. That covers the layer's
  physics and gravity, the sky, and the ambient light. It needs the
  skybox-saving fix (`DefaultSceneManager:SaveSkybox` is never called).
- **New controls:** a slider, a combo box and a color picker would help
  (the scene editor has none: `TierFour.md`). Text fields and radio groups
  are enough for now.

## Tests

- **Every kind of item** (cube, custom model, point light, Audio3D, camera,
  2D drawable, 2D camera) shows the same fields as today's dialog, in the
  panel. Edits take effect as before.
- **Nothing is drawn over the scene's top-right corner.**
- **Cmd/Ctrl+P** focuses the first field, and Escape returns. With a
  screen reader, the panel is read as "Properties" with the item's name.
- **The drag bar** resizes the panel, and the scene view resizes with it.
- **Switching scene tabs** shows each tab's own selection.
- **A program using `SceneEditor` without a panel** still gets the
  floating dialogs.
