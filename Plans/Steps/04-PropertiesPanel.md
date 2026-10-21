# Step 4: the Properties panel, docked on the right, for every item

Part of `../QuorumStudio.md`. It doesn't need the terrain steps. Step 5 adds
the terrain's part.

## Status: applied 2026-10-20 (not built or run yet)

Applied exactly as written below:
- **Library (`MakeAWish`):** new SC/`PropertiesPanel.quorum`; changed
  SC/Dialogs/`PropertiesDialog.quorum`, SC/`SceneEditor.quorum`,
  L/Interface/Behaviors/Scenes/Properties/`UnfocusPropertiesBehavior.quorum`.
- **Studio (`MakeAWish`):** new
  QS/Behaviors/Support/`PropertiesPartitionDragBehavior.quorum`; changed
  QS/Interface/Controls/`VisualEditorTab.quorum` and
  QS/Interface/`QuorumStudio.quorum`.

To try it: build the library, then Studio (its
`Run/QuorumStandardLibrary.jar` must come from this library), then run the
tests at the end.

**Repositories** (never switch branches):
- library: `quorum-language`, `MakeAWish`;
- Studio: `quorum-studio`, `MakeAWish`.

## Where this comes from

**Moving the properties to the right-hand side** is the master plan's
Decision 6 (`../QuorumStudio.md`, "Decisions"). The handoff repeats it
("decided 2026-10-20"):
- the panel **replaces the floating "Object Properties" dialog**
  (SC/Dialogs/`Item3DPropertiesDialog`, pinned over the editor's top-right
  corner) and the five other properties dialogs;
- it is **docked on the right, inside the scene editor tab**, not a column
  for all of Studio;
- **for every item** it shows name, transform and physics, as today;
- **for a terrain** it shows more, added by later steps.

**What the later steps put in this panel,** so step 4 builds for them:

| Step | Adds, for a selected terrain | What it needs from the panel |
|---|---|---|
| 5 | **Terrain** (size, regions, heights, folder) and **Physics** (collision, friction) | two sections at once, after the item's own |
| 7 | **Brush**: tool radio group, size, strength, falloff | a section whose fields change as tools are added; the size field follows Alt+= and Alt+- |
| 9 | **Materials**: add, remove and choose materials | buttons, and a section that grows |
| 10 | **Generate**: presets, stamp, "Stamp here", "Erode here", "Auto paint here" | buttons |
| 12 | every field read with its label; **sections read as groups**; a "Describe the area" button | each section a named group for screen readers |

**How step 4 provides that:**
- **A section is a `PropertiesDialog` subclass,** added with the new
  `SceneEditor:AddPropertiesDialog`. Its `CreateInterface` builds its
  fields, buttons and labels; its `SelectionChanged` decides when it
  shows, exactly as the six built-in dialogs do. (The later step files
  name these classes `...Properties` in SC/Properties/; they become
  `...PropertiesDialog`s in SC/Dialogs/, docked.)
- **The panel shows every section that applies, at once,** one under
  another (`PropertiesPanel:ShowDialog` adds; it doesn't replace).
- **Order:** sections show in the order they were added to the editor:
  the six built-in dialogs first, then step 5's, 7's, 9's and 10's. The
  selection change reaches the dialogs in that order, and each one adds
  itself to the panel as it decides to show.
- **Groups for screen readers:** each section's fields are shown as a
  named group (the dialog's name, such as "Object Properties", or
  "Terrain"), so they're read as one group (step 12).
- **Keeping fields current:** `UpdateActivePropertiesDialog` refreshes the
  showing section's fields as an item moves. Steps 7 and 10 call it, or
  their section's own `SetFields`, when a value changes from a key.

## Today

- **The window:** selecting an item in the scene editor shows a floating
  dialog over the editor's top-right corner, "Object Properties"
  (SC/Dialogs/`Item3DPropertiesDialog`).
  - It is placed by `PropertiesDialog:Show`.
  - It holds "Basic Properties" (name, position, scale, rotation, color)
    and "Physics" (enabled, responsiveness, mass, friction, restitution).
- **One dialog per kind of item,** six in all: `Item3DPropertiesDialog`,
  `PointLightPropertiesDialog`, `Audio3DPropertiesDialog`,
  `Camera3DPropertiesDialog`, `Item2DPropertiesDialog` and
  `Camera2DPropertiesDialog`.
  - `SceneEditor` makes all six in its `on create`, and initializes each,
    which makes it a selection listener.
  - **Each dialog decides for itself** when to show: its
    `SelectionChanged` calls `Show()` or `Hide()`, and checks
    `IsShowing()`. `SceneEditor:FocusItemProperties` and
    `UpdateActivePropertiesDialog` also go through `IsShowing()`.
  - Each dialog's fields are built by `CreateInterface` into one `Control`,
    which `PropertiesDialog:Initialize` adds to the dialog.
- **Keys:**
  - Cmd/Ctrl+P (`FocusItemProperties`) focuses the showing dialog's first
    field.
  - Escape or Enter in a field returns focus to the editor. That's the
    `"PropertiesDialog"` input table: key presses walk up from the focused
    field through its parents until a parent's input group has a binding.
- **Edits** go straight onto the item, and can't be undone.

## The change

**A Properties panel, docked on the right inside the scene tab,** shows the
same fields the floating dialogs showed. Nothing floats over the scene.

### How: docking the existing dialogs, not rewriting them

The earlier plan split each of the six dialogs into a new "section" class:
about 3,000 lines rewritten. This write-up does less, with the same result
on screen:
- **`PropertiesDialog` gets a docked mode.** Once docked in a panel, its
  `Show`, `Hide` and `IsShowing` put its fields' `Control` into the panel,
  or take them out, instead of opening a floating window.
- **None of the six dialogs change.** Their `SelectionChanged`,
  `SetFields`, `TextChanged` and `FocusField` work as they are, because
  they only ever call `Show`, `Hide` and `IsShowing`.
- **Programs that use `SceneEditor` without a panel** still get the
  floating dialogs.

**Step 5 then works the same way:** a terrain's properties are a new
`PropertiesDialog` subclass, added with `SceneEditor:AddPropertiesDialog`
(below), and docked like the rest. The selection events already tell a
terrain apart: a selected terrain's highlight is a
`TerrainSelectionHighlight`, which none of the six dialogs show for.

**What the panel shows:**
- the selected item's dialog fields, as before;
- "Nothing selected" when no item is selected;
- a scroll bar when the fields are taller than the tab.

**Keys:** none new.
- Cmd/Ctrl+P focuses the panel's first field.
- Tab and Shift+Tab move through the fields, using the dialogs' own focus
  order.
- Escape or Enter returns to the editor. The panel has the
  `"PropertiesDialog"` input group, so the existing bindings apply.

**Width:** 300 pixels to start; the drag bar between the scene and the
panel resizes it, from 150 to 800. It isn't saved between sessions yet.

Paths: **L/** = `quorum-language/Quorum/Library/Standard/Libraries/`,
**SC/** = `L/Interface/Controls/Scenes/`, **QS/** =
`quorum-studio/QuorumStudio/SourceCode/`.

## The code: library

### 1. SC/`PropertiesPanel.quorum` (new)

```
package Libraries.Interface.Controls.Scenes

use Libraries.Interface.Controls.Control
use Libraries.Interface.Controls.ScrollPane
use Libraries.Interface.Layouts.FlowLayout
use Libraries.Game.Graphics.Label
use Libraries.Containers.Array
use Libraries.Interface.Controls.Scenes.Dialogs.PropertiesDialog

/*
    The PropertiesPanel class shows the selected scene item's properties
    beside a SceneEditor, instead of in floating dialogs. Give it to the
    editor with SetPropertiesPanel: each of the editor's properties dialogs
    then shows its fields here when an item it describes is selected. With
    nothing selected, it says so. The panel scrolls when its fields are
    taller than it is.

    Pressing Escape or Enter in one of its fields returns the focus to the
    editor, as in the dialogs.

    Attribute: Example
    use Libraries.Interface.Controls.Scenes.SceneEditor
    use Libraries.Interface.Controls.Scenes.PropertiesPanel

    SceneEditor editor
    PropertiesPanel panel
    editor:SetPropertiesPanel(panel)
*/
class PropertiesPanel is Control
    SceneEditor editor = undefined
    ScrollPane scrollPane
    Control contents
    Label emptyLabel

    // The dialogs whose fields are in the panel now, in the order shown.
    Array<PropertiesDialog> showing

    on create
        SetName("Properties")
        // The dialogs' Escape and Enter bindings, to return to the editor.
        SetInputGroup("PropertiesDialog")
        SetPixelWidth(300)
        SetPercentageHeight(1.0)
        FlowLayout layout
        SetLayout(layout)

        scrollPane:SetName("Properties")
        scrollPane:SetPercentageWidth(1.0)
        scrollPane:SetPercentageHeight(1.0)

        FlowLayout contentsLayout
        contents:SetLayout(contentsLayout)
        contents:SetPercentageWidth(1.0)
        contents:SetVerticalLayoutMode(contents:FIT_CONTENTS)

        emptyLabel:SetText("Nothing selected")
        emptyLabel:SetLeftMargin(20)
        emptyLabel:SetTopMargin(10)
        contents:Add(emptyLabel)

        scrollPane:Add(contents)
        Add(scrollPane)
    end

    /*
        Sets the SceneEditor whose selection this panel describes.

        Attribute: Parameter editor The SceneEditor
    */
    action SetSceneEditor(SceneEditor editor)
        me:editor = editor
    end

    /*
        Returns the SceneEditor whose selection this panel describes.

        Attribute: Returns The SceneEditor, or undefined before one is set
    */
    action GetSceneEditor returns SceneEditor
        return editor
    end

    /*
        Puts a docked dialog's fields into the panel, after any already
        shown. PropertiesDialog:Show calls it once the dialog is docked.

        Attribute: Parameter dialog The dialog whose fields to show
    */
    action ShowDialog(PropertiesDialog dialog)
        if showing:Has(dialog)
            return now
        end
        if showing:IsEmpty()
            contents:Remove(emptyLabel)
        end
        Control content = dialog:GetContent()
        // Each section is read as one named group, such as "Object
        // Properties" or "Terrain" (step 12).
        content:SetName(dialog:GetName())
        content:SetAccessibilityCode(parent:Item:GROUP)
        contents:Add(content)
        content:Show()
        showing:Add(dialog)
        contents:RequestLayout()
    end

    /*
        Takes a docked dialog's fields out of the panel. PropertiesDialog:Hide
        calls it once the dialog is docked.

        Attribute: Parameter dialog The dialog whose fields to take out
    */
    action HideDialog(PropertiesDialog dialog)
        if not showing:Has(dialog)
            return now
        end
        contents:Remove(dialog:GetContent())
        showing:Remove(dialog)
        if showing:IsEmpty()
            contents:Add(emptyLabel)
        end
        contents:RequestLayout()
    end

    /*
        Returns true if a docked dialog's fields are in the panel now.

        Attribute: Parameter dialog The dialog
        Attribute: Returns True if its fields are showing
    */
    action IsShowingDialog(PropertiesDialog dialog) returns boolean
        return showing:Has(dialog)
    end
end
```

**Why the input group is on the panel, not inside the scroll pane:** the
scroll pane has its own group (`"ScrollPane"`). Key presses walk up from
the field, past the scroll pane's table when it has no binding for the key,
to the panel's.

### 2. SC/Dialogs/`PropertiesDialog.quorum`

**New `use`,** after `use Libraries.Interface.Controls.Scenes.SceneEditor`:

```
use Libraries.Interface.Controls.Scenes.PropertiesPanel
```

**New fields.** The lines

```
    boolean initialized = false
    SceneEditor editor = undefined
```

become:

```
    boolean initialized = false
    SceneEditor editor = undefined

    // The control holding the dialog's fields, made by Initialize.
    Control content = undefined

    // The panel the dialog is docked in, or undefined for a floating
    // dialog (step 4).
    PropertiesPanel dock = undefined
```

**`Initialize`.** The lines

```
        // Generate the contents of the panel and the tab-path.
        // Implemented on a per-dialog basis.
        CreateInterface(panel, focusArray)
     
        Add(panel)
```

become:

```
        // Generate the contents of the panel and the tab-path.
        // Implemented on a per-dialog basis.
        CreateInterface(panel, focusArray)

        // A docked dialog's fields go into its panel when it shows.
        content = panel
        if dock = undefined
            Add(panel)
        end
```

**`Show`.** Its first line

```
    action Show
        if editor not= undefined
```

becomes:

```
    action Show
        // Docked: the fields go into the panel, and nothing floats.
        if dock not= undefined
            dock:ShowDialog(me)
            return now
        end

        if editor not= undefined
```

**New actions,** after `Show`:

```
    /*
        Hides the dialog. A docked dialog's fields are taken out of its
        panel instead.
    */
    action Hide
        if dock not= undefined
            dock:HideDialog(me)
            return now
        end
        parent:Dialog:Hide()
    end

    /*
        Returns true if the dialog is showing. A docked dialog is showing
        while its fields are in its panel.

        Attribute: Returns True if the dialog, or its fields in the panel, are showing
    */
    action IsShowing returns boolean
        if dock not= undefined
            return dock:IsShowingDialog(me)
        end
        return parent:Dialog:IsShowing()
    end

    /*
        Docks the dialog in a PropertiesPanel. From then on, showing it
        puts its fields into the panel, and it never floats. A dialog is
        docked once, and stays docked.

        Attribute: Parameter panel The panel to dock in
    */
    action DockIn(PropertiesPanel panel)
        if dock not= undefined
            return now
        end
        if parent:Dialog:IsShowing()
            parent:Dialog:Hide()
        end
        if content not= undefined
            Remove(content)
        end
        dock = panel
    end

    /*
        Returns true if the dialog is docked in a PropertiesPanel.

        Attribute: Returns True if docked
    */
    action IsDocked returns boolean
        return dock not= undefined
    end

    /*
        Returns the control holding the dialog's fields.

        Attribute: Returns The fields' control, or undefined before Initialize
    */
    action GetContent returns Control
        return content
    end
```

**Notes:**
- `Dialog:Show` checks `IsShowing()` first. For a floating dialog that is
  `parent:Dialog:IsShowing()`, as before.
- `Dialog:Hide` hides through `parent:Item:Hide`, not through this `Hide`,
  so there's no loop.
- The six subclasses don't override `Show`, `Hide` or `IsShowing` (checked
  for `Item3DPropertiesDialog`, which calls all three).

### 3. SC/`SceneEditor.quorum`

**New field.** The line

```
    Array<PropertiesDialog> propertiesDialogs
```

becomes:

```
    Array<PropertiesDialog> propertiesDialogs

    // The panel the properties dialogs are docked in, or undefined to show
    // them as floating dialogs (step 4).
    PropertiesPanel propertiesPanel = undefined
```

**`HideDialogs`.** The action

```
    action HideDialogs
        integer counter = 0
        repeat while counter < propertiesDialogs:GetSize()
            propertiesDialogs:Get(counter):Hide()
            counter = counter + 1
        end
    end
```

becomes:

```
    action HideDialogs
        // A docked panel is part of the editor's own view, so it keeps
        // showing the selection, for example when switching tabs.
        if propertiesPanel not= undefined
            return now
        end
        integer counter = 0
        repeat while counter < propertiesDialogs:GetSize()
            propertiesDialogs:Get(counter):Hide()
            counter = counter + 1
        end
    end

    /*
        Shows the properties of the selected item in a panel, instead of in
        floating dialogs. Every properties dialog, including any added later
        with AddPropertiesDialog, docks in the panel. Set it once, before the
        scene is loaded.

        Attribute: Parameter panel The panel
    */
    action SetPropertiesPanel(PropertiesPanel panel)
        propertiesPanel = panel
        panel:SetSceneEditor(me)
        integer counter = 0
        repeat while counter < propertiesDialogs:GetSize()
            propertiesDialogs:Get(counter):DockIn(panel)
            counter = counter + 1
        end
    end

    /*
        Returns the panel showing the selected item's properties.

        Attribute: Returns The panel, or undefined if the editor uses floating dialogs
    */
    action GetPropertiesPanel returns PropertiesPanel
        return propertiesPanel
    end

    /*
        Adds a properties dialog for another kind of item. It is set up as
        the built-in ones are: it listens to the selection, and docks in the
        panel if there is one. Step 5 adds a terrain's dialog this way.

        Attribute: Parameter dialog The dialog
    */
    action AddPropertiesDialog(PropertiesDialog dialog)
        dialog:Initialize(me)
        propertiesDialogs:Add(dialog)
        if propertiesPanel not= undefined
            dialog:DockIn(propertiesPanel)
        end
    end
```

`PropertiesPanel` is in the same package as `SceneEditor`, so no `use` is
needed. `FocusItemProperties` and `UpdateActivePropertiesDialog` don't
change: they find the showing dialog through `IsShowing()`, which now
answers for the panel.

### 4. L/Interface/Behaviors/Scenes/Properties/`UnfocusPropertiesBehavior.quorum`

**New `use`,** after `use Libraries.Interface.Controls.Scenes.SceneEditor`:

```
use Libraries.Interface.Controls.Scenes.PropertiesPanel
```

The lines

```
        if event:GetItem() is PropertiesDialog
            PropertiesDialog dialog = cast(PropertiesDialog, event:GetItem())
            controller = dialog:GetSceneEditor()
        end
```

become:

```
        if event:GetItem() is PropertiesDialog
            PropertiesDialog dialog = cast(PropertiesDialog, event:GetItem())
            controller = dialog:GetSceneEditor()
        elseif event:GetItem() is PropertiesPanel
            PropertiesPanel panel = cast(PropertiesPanel, event:GetItem())
            controller = panel:GetSceneEditor()
        end
```

## The code: Studio

### 5. QS/Interface/Controls/`VisualEditorTab.quorum`

**New `use`s,** after `use Libraries.Interface.Controls.Scenes.SceneEditor`:

```
use Libraries.Interface.Controls.Control
use Libraries.Interface.Layouts.FlowLayout
use Libraries.Interface.Controls.Scenes.PropertiesPanel
```

**New fields.** The lines

```
    File file = undefined
    File projectLocation = undefined
    SceneEditor editor = undefined
    GameStateManager manager
```

become:

```
    File file = undefined
    File projectLocation = undefined
    SceneEditor editor = undefined
    GameStateManager manager

    // The scene editor, a drag bar, and the Properties panel, side by side.
    Control sceneContainer
    Control propertiesPartition
    PropertiesPanel propertiesPanel
    number propertiesWidth = 300
    constant number PARTITION_WIDTH = 4
    constant number MINIMUM_PROPERTIES_WIDTH = 150
    constant number MAXIMUM_PROPERTIES_WIDTH = 800
```

**`Load`.** The action

```
    action Load
        SceneEditor controller
        me:editor = controller
        editor:SetFile(file)
        editor:SetProjectLocation(projectLocation)
        editor:SetName(file:GetPath())

        SetRelatedItem(editor)
        SetFocusTarget(editor)

        editor:Load()
    end
```

becomes:

```
    action Load
        SceneEditor controller
        me:editor = controller
        editor:SetFile(file)
        editor:SetProjectLocation(projectLocation)
        editor:SetName(file:GetPath())

        // The editor on the left, then a drag bar, then the Properties
        // panel on the right. The editor takes the width the other two
        // leave (SetPropertiesWidth).
        FlowLayout layout
        sceneContainer:SetLayout(layout)
        sceneContainer:SetPercentageWidth(1.0)
        sceneContainer:SetPercentageHeight(1.0)
        sceneContainer:SetName("Scene")

        propertiesPartition:SetPixelWidth(PARTITION_WIDTH)
        propertiesPartition:SetVerticalLayoutMode(propertiesPartition:FILL)
        propertiesPartition:SetInputGroup("PropertiesPartition")
        propertiesPartition:SetName("Properties Partition")

        sceneContainer:Add(editor)
        sceneContainer:Add(propertiesPartition)
        sceneContainer:Add(propertiesPanel)
        SetPropertiesWidth(propertiesWidth)

        SetRelatedItem(sceneContainer)
        SetFocusTarget(editor)

        // Docked before loading, so the dialogs never float.
        editor:SetPropertiesPanel(propertiesPanel)
        editor:Load()
    end

    /*
        Sets the width of the Properties panel, in pixels before the
        interface scale, from 150 to 800. The scene takes the rest.

        Attribute: Parameter width The panel's width
    */
    action SetPropertiesWidth(number width)
        if width < MINIMUM_PROPERTIES_WIDTH
            width = MINIMUM_PROPERTIES_WIDTH
        elseif width > MAXIMUM_PROPERTIES_WIDTH
            width = MAXIMUM_PROPERTIES_WIDTH
        end
        propertiesWidth = width
        propertiesPanel:SetPixelWidth(width)
        // The editor's width is the whole width, less the bar and panel.
        editor:SetPixelWidth(-(width + PARTITION_WIDTH))
        sceneContainer:RequestLayout()
    end

    /*
        Returns the width of the Properties panel, in pixels before the
        interface scale.

        Attribute: Returns The panel's width
    */
    action GetPropertiesWidth returns number
        return propertiesWidth
    end

    /*
        Returns the control holding the editor, the drag bar and the panel.

        Attribute: Returns The container
    */
    action GetSceneContainer returns Control
        return sceneContainer
    end

    /*
        Returns the tab's Properties panel.

        Attribute: Returns The panel
    */
    action GetPropertiesPanel returns PropertiesPanel
        return propertiesPanel
    end
```

**`GetSceneEditor`.** The action

```
    action GetSceneEditor returns SceneEditor
        Item2D value = GetRelatedItem()
        if value is SceneEditor
            return cast(SceneEditor, value)
        else
            return undefined
        end
    end
```

becomes:

```
    action GetSceneEditor returns SceneEditor
        // The related item is now the container around the editor.
        return editor
    end
```

**Why the editor's width is "all, less the panel":** `FlowLayout`'s fill
mode gives an item all the width left on its line, so a fill-width editor
would push the panel onto a new line. Instead the editor has 100% of the
width plus a negative pixel width, the panel's and the bar's. The layout
adds the two (`FlowLayout`, "STANDARD": percentage × width + pixels ×
scale).

`FocusEditorBehavior` and the other Studio code reach the editor through
`GetSceneEditor`, so they're unaffected. The editor's own `Resize` sets its
3D and 2D viewports from its place and size, so the scene draws in the
narrower area.

### 6. QS/Behaviors/Support/`PropertiesPartitionDragBehavior.quorum` (new)

```
package Libraries.Development.Environment.Studio.Behaviors

use Libraries.Interface.Events.BehaviorEvent
use Libraries.Interface.Events.DragAndDropEvent
use Libraries.Game.InputSet
use Libraries.Development.Environment.Studio.QuorumStudio
use Libraries.Development.Environment.Studio.CodeEditorTabPane
use Libraries.Development.Environment.Studio.Interface.VisualEditorTab
use Libraries.Interface.Selections.TabPaneSelection
use Libraries.Interface.Controls.Tab
use Libraries.Interface.Controls.Control
use Libraries.Interface.Behaviors.Behavior
use Libraries.Game.GameStateManager

/*
    Resizes the Properties panel of the selected scene tab, as its drag bar
    is dragged. The panel's left edge follows the pointer.
*/
class PropertiesPartitionDragBehavior is Behavior

    GameStateManager manager

    action Run(BehaviorEvent event)
        DragAndDropEvent dragEvent = event:GetDragAndDropEvent()
        InputSet set = event:GetInputSet()
        if set:GetInputTrigger() = set:BEGIN or dragEvent:GetMovementX() = 0
            return now
        end

        QuorumStudio studio = cast(QuorumStudio, manager:GetGame())
        CodeEditorTabPane pane = studio:GetTabPane()
        TabPaneSelection selection = pane:GetSelection()
        if selection = undefined
            return now
        end
        Tab tab = selection:GetTab()
        if not (tab is VisualEditorTab)
            return now
        end

        VisualEditorTab visualTab = cast(VisualEditorTab, tab)
        Control container = visualTab:GetSceneContainer()
        number rightEdge = container:GetGlobalX() + container:GetWidth()
        visualTab:SetPropertiesWidth((rightEdge - dragEvent:GetX()) / container:GetInterfaceScale())
    end
end
```

### 7. QS/Interface/`QuorumStudio.quorum`

**New `use`,** after `use Libraries.Development.Environment.Studio.Behaviors.EditorPartitionDragBehavior`:

```
use Libraries.Development.Environment.Studio.Behaviors.PropertiesPartitionDragBehavior
```

**`SetupInputTables`.** The lines

```
editorPartitionTable:Add(dragSet, editorDrag)
editorPartitionTable:Add(dragContinueSet, editorDrag)
        AddInputTable(editorPartitionTable)
    end
```

become:

```
editorPartitionTable:Add(dragSet, editorDrag)
editorPartitionTable:Add(dragContinueSet, editorDrag)
        AddInputTable(editorPartitionTable)

        // The drag bar between a scene and its Properties panel (step 4).
        InputTable propertiesPartitionTable
        propertiesPartitionTable:SetIdentifier("PropertiesPartition")
        PropertiesPartitionDragBehavior propertiesDrag
        propertiesPartitionTable:Add(dragSet, propertiesDrag)
        propertiesPartitionTable:Add(dragContinueSet, propertiesDrag)
        AddInputTable(propertiesPartitionTable)
    end
```

## What doesn't change

- **The six dialogs' code**, and their fields, labels, focus order and
  edits.
- **Programs that use `SceneEditor` without `SetPropertiesPanel`:** they
  get the floating dialogs, as before.
- **Studio's main layout** (`CreateMainContentPane`) and code tabs.

## Later (not in this step)

- **Undo for property edits** (step 4b): a `SceneEditorPropertyEdit is
  Edit`, recorded when a field's change is committed.
- **With nothing selected:** the scene's own settings (layer physics and
  gravity, the sky, the ambient light), once skyboxes are saved.
- **Remembering the panel's width** in Studio's options.
- **A heading with the selected item's name** at the panel's top. The
  dialogs' first fields already show the name.

## Tests

1. **Every kind of item** (cube, custom model, point light, Audio3D, 3D
   camera, 2D drawable, 2D camera) shows the same fields as the old
   dialog, in the panel on the right. Edits take effect as before.
2. **Nothing is drawn over the scene's top-right corner.** The scene view
   ends at the drag bar, and the 3D view isn't stretched or offset.
3. **With nothing selected,** the panel says "Nothing selected".
4. **A terrain** (step 3): the panel says "Nothing selected" until step 5.
5. **Cmd/Ctrl+P** focuses the first field; Tab moves through the fields;
   Escape returns to the editor; Enter in a field returns too.
6. **With a screen reader,** the panel reads as "Properties", and each
   field reads its label and value as before.
7. **The drag bar** resizes the panel, from 150 to 800 pixels, and the
   scene view resizes with it.
8. **Two scene tabs:** each shows its own selection. Switching away and
   back keeps the panel's fields.
9. **A tall dialog** (Audio3D) scrolls in a short window.
10. **With a screen reader,** moving into the fields says the section's
    name as a group ("Object Properties") before the first field.
11. **Two sections at once** (checked properly in step 5, which adds the
    first): both show, one under the other, in the order they were added.

**Note on test 11:** each dialog's Tab order is its own loop today (its
last field's "next" is its first field). With two sections showing, Tab
stays inside one. Step 5 links the loops across the showing sections, in
`PropertiesPanel`, when it adds the second section.

## Risks to check when trying it

- **Moving a control between parents.** The dialog's field control is made
  inside the dialog, then taken out and added to the panel. If its fields
  don't draw or don't take focus there, the fix is for the dialog to build
  its fields straight into the panel when docked.
- **Scroll pane height.** The fields' control fits its contents; if the
  scroll pane doesn't scroll, it needs the same setup as Studio's palette
  pane (`QuorumStudio.quorum`, `palettePane`).
