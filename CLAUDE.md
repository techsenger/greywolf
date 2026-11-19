# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

GreyWolf is an integrated file management environment (JavaFX, Java 25) built as a plugin-based,
modular application on top of two sibling Techsenger frameworks that the same author maintains:

- **patternfx** (`com.techsenger.patternfx.*`) — provides the component-tree building blocks GreyWolf is
  built on: shared plumbing in `patternfx-core` (`ComponentDescriptor`, the composer pattern) and the MVVM
  pattern in `patternfx-mvvm` (`ChildViewModel`, `ParentViewModel`, `ChildComposer`, etc.) that every
  GreyWolf ViewModel implements. patternfx's own `demo-mvvm` module is a worked example of the pattern.
- **shellfx** (`com.techsenger.shellfx.*`) — the UI shell framework: tabs (`core.tab.*`), dock hosts and
  layout (`layout.dockhost.*`), menus (`material.menu.*`), history (`core.history.*`), built on
  `patternfx-mvvm`; its own code is a second reference for the MVVM conventions below.

Application bootstrapping, modularity, and plugin loading are provided by a third framework,
**Weaverbird** (`com.techsenger.weaverbird.core.*`), an OSGi-like component/module system: components are
resolved/downloaded from a Maven-style repo, activated via `ModuleActivator` SPI
(`provides com.techsenger.weaverbird.core.spi.module.ModuleActivator with ...` in each `module-info.java`),
and can depend on each other. GreyWolf' own "Plugin" concept (`core.plugin.Plugin`,
`core.plugin.PluginManager`) is a thin frontend over Weaverbird components.

Since patternfx and shellfx evolve in lockstep with GreyWolf, changes here often need matching changes
in those sibling repos — don't assume an unusual-looking API is a bug before checking whether it's a
shellfx/patternfx convention.

Every module follows the same MVVM component pattern (see "Architecture: components" below); modules
without their own UI component tree (`greywolf-boot`, `greywolf-devtools-*`) are structured differently —
check what a module actually contains before assuming its shape from another module.

## Language

Everything in the project is written in English — README, documentation, Javadoc, code comments, commit
messages, etc. Always — regardless of what language the conversation with the assistant happens in.

## Scope of work

It is strictly forbidden to operate outside the project directory. This applies absolutely, to every kind
of operation — including read-only ones like `find` or search/grep — not just edits.

The only exception: the developer may explicitly name a directory outside the project directory for a
specific task. In that case, work must stay confined to the project directory plus exactly the
directory/directories the developer named — nothing else. Proactively suggesting or using any other
outside directory on your own is strictly forbidden.

If some source you need isn't in an allowed directory, ask the developer where to find it — never search
the rest of the machine for it on your own.

## Module Layout

Maven multi-module reactor, root `pom.xml`, Java 25, JPMS (every module has a `module-info.java`):

- `greywolf-storage` — storage modules: `greywolf-storage-base` (storage abstraction: `GreyWolfStorage`, `GreyWolfFile`,
  `FileType`, file icons, and the engines of the file operations in `operation`/`transfer`/`delete`: copy, move, delete,
  scanning; no views or slots), `greywolf-storage-ui` (views, slots, styles of files, and the dialogs of the file
  operations: transfer, delete, rename, properties), `greywolf-storage-system` and `greywolf-storage-recent` (the
  storages themselves).
- `greywolf-shared` — components shared across tools/panels: directory tree tab (`shared/directory`),
  location bar/breadcrumb (`shared/location`). It declares the context menus of a location bar segment and of a
  directory tree node (`shared/slots`: `LocationBarSlots`, `DirectoryTreeSlots`) and registers them with its own
  closed `shared/registrars` through its `ModuleActivatorProvider` (active in the core component, listed after
  `greywolf-core`); plugins fill the groups, and the handlers read the folder the menu was requested for from
  `LocationBarPort#getFile` / `DirectoryTreePort#getFile`. The items that need no navigator - properties and paste
  into a folder - are filled by shared itself: they run the operations of `greywolf-storage-ui` in the window
  container above the component and tell the components above it (`FolderContentListener`) that the content of a
  folder has changed.
- `greywolf-core` — the main application: shell (`GreyWolfShellViewModel`/`GreyWolfShellView`), the
  dual panels tab (`DualPanelsTabView`/`PanelTabDockView` in `core/panels`), slot catalogs shared
  across modules (`core/slots`: `GreyWolfShellSlots`, `PanelTabDockSlots`, `WorkspaceSlots`) plus this module's own
  control wiring (the closed `core/registrars`: registrars and item handlers, see "Menus and registries"),
  plugin management (`core/plugin`, `core/internal/plugin`).
- `greywolf-boot` — the actual entry point (`boot.Boot#main`); starts the Weaverbird `Framework`, resolves
  and activates the `greywolf-core` component, then activates configured plugin components. Has no
  patternfx/shellfx UI component tree of its own (plain AWT splash screen + Weaverbird bootstrapping).
- `greywolf-dist` — assembles the runnable distribution via `weaverbird-assembly-maven-plugin`
  (`assembly/dist.xml`, `assembly/config/**`, `assembly/data/**`); main class is
  `com.techsenger.greywolf.boot/com.techsenger.greywolf.boot.Boot`.
- `greywolf-devtools` — optional DevTools plugin modules for inspecting/debugging GreyWolf itself, each a
  separate Weaverbird component with its own `ModuleActivator` and registrars:
  `greywolf-devtools-base`, `greywolf-devtools-shellfx`, `greywolf-devtools-weaverbird`.
- `greywolf-system` — optional System plugin modules, the foundational tools of the environment (plugin category
  `SYSTEM`): `greywolf-system-navigator` (the dual-panel file browsing tab, `NavigatorTabView`/
  `NavigatorTabViewModel`, contributing to the shell and to its own tool bar through its `slots` (public) and
  `registrars` (closed: registrars and item handlers) packages) and `greywolf-system-terminal` (JediTerm/pty4j-based
  terminal tab), also with its own registrars.
- `greywolf-samples` — sample plugin modules that show how to extend GreyWolf from outside.

Every plugin module (`greywolf-system-navigator`, `greywolf-devtools-*`, `greywolf-system-terminal`) has a
`ModuleActivatorProvider` (Weaverbird's `ModuleActivator` SPI implementation) that registers and unregisters the
module's registrars - see "Menus and registries" for how they are named and where they live. Unlike the
registrars, `ModuleActivatorProvider` is named identically in every module - disambiguated by package, never
imported by FQN across modules.

## Commands

```
mvn install                         # build+install all modules (root reactor)
mvn -pl greywolf-core install      # build a single module (add -am to also build its dependencies)
mvn -pl greywolf-dist -am install  # assemble the runnable dist (greywolf-dist/target/greywolf-<version>)
```

The root POM inherits from `com.techsenger.maven.root:maven-root` and pulls dependency versions from the
`com.techsenger.osp.bom:osp-bom` BOM (both resolved from the `repsy-snapshots` repository configured in
the root `pom.xml`). Compilation runs with `-Werror`, ErrorProne, and NullAway (`-XepOpt:NullAway:OnlyNullMarked`)
enabled — treat compiler warnings as build-breaking.

`maven-root`'s default profile (active by default, so plain `mvn install`/`mvn package` triggers it) also
wires in, per module:

- **Checkstyle** (`maven-checkstyle-plugin`, goal `check`) bound to the `package` phase, config pulled from
  the external `com.techsenger.checkstyle.config:checkstyle-config` artifact (not a file in this repo) —
  see the Code style section below. A violation fails the build.
- **Surefire** (unit tests) and **Failsafe** (integration tests, classes named `*IT.java`), bound to the
  normal test/`integration-test`/`verify` phases.

Two profiles narrow this down: `mvn install -P unit-tests` runs only unit tests (skips Failsafe and
Checkstyle); `mvn verify -P integration-tests` runs only integration tests (skips Surefire and Checkstyle).
There are currently no test sources under any module's `src/test` (directories exist but are empty), so
today these are no-ops — there's no existing pattern to follow for adding the first test, but new
integration tests should be named `*IT.java` to be picked up by Failsafe instead of Surefire.

To run the app after assembling `greywolf-dist`, use the generated `greywolf` launch script under
`greywolf-dist/target/greywolf-<version>`.

## Architecture: components

Following `patternfx-mvvm`/shellfx conventions (see patternfx's `demo-mvvm` module and shellfx's own
code for worked examples), a component is built from five files, illustrated by
`greywolf-core/.../panels/DualPanelsTab*` and `greywolf-system-navigator/.../NavigatorTab*`/`.../area/NavigatorArea*`:

- **`XxxParams`** — plain constructor-injected config/dependencies for a component.
- **`XxxPort`** — the interface a component exposes to its parent/siblings; a `Port` that other components
  reach into also declares a nested `ComposerAccess` (methods callable from outside, e.g. `getCurrentPanel`,
  `getPanelTabDockPort`) plus a `getComposerAccess(): ComposerAccess` accessor.
- **`XxxComposer`** — the interface `XxxView` exposes back to its `XxxViewModel`: extends a
  `patternfx-mvvm` composer interface (`ChildComposer`, `ParentComposer`, etc.) plus, when the component has
  one, the `Port`'s `ComposerAccess`.
- **`XxxView`** — the JavaFX implementation, a `final class` extending a shellfx base view type
  parameterized by the ViewModel (e.g. `AbstractHostTabView<XxxViewModel>`). Contains a nested `Composer`
  class implementing `XxxComposer` that does the actual scene-graph construction in `compose()`, plus
  `createXxx(...)` factory methods for child components — the `Composer` holds child views/state as fields,
  not the outer `View`. The View binds to the ViewModel's properties in `bind()`/`addListeners()` instead of
  receiving pushed updates — see "Architecture: ViewModel state" below.
- **`XxxViewModel`** — the source of truth for the component's state, a `final class` extending a
  `patternfx-mvvm`/shellfx abstract ViewModel parameterized by its `XxxComposer` (e.g.
  `AbstractHostTabViewModel<XxxComposer>`) and implementing the `Port` directly — anything declared on the
  `Port` is automatically part of the ViewModel's public contract too. When deciding whether to promote a
  member from the concrete `XxxViewModel` onto its `Port`: only promote what is already `public` on the
  ViewModel, mirror its exact shape (readonly vs. writable, single value vs. `ObservableList`) as-is, then
  add `@Override` on the implementation — don't invent new visibility or reshape it while promoting.

Construction order for a child component: `new XxxViewModel(params)` → `new XxxView(viewModel, ...)` →
`view.initialize()`. Look at an existing `createXxx()` factory method on a sibling `Composer` before adding a
new child component type — the pattern is consistent everywhere.

## Architecture: ViewModel state

The **ViewModel is the source of truth** for a component's state, exposed as JavaFX properties that the View
binds to directly. Every ViewModel property falls into one of three shapes:

1. **Read and written from outside, fully owned by the ViewModel** (nothing else can change it
independently): a plain `Property`, exposed as three public methods — `xProperty()`, `getX()`, `setX()`:

```java
// ViewModel
private final ObjectProperty<Foo> foo = new SimpleObjectProperty<>();

public ObjectProperty<Foo> fooProperty() {
    return this.foo;
}

public Foo getFoo() {
    return this.foo.get();
}

public void setFoo(Foo foo) {
    this.foo.set(foo);
}
```

2. **Read only from outside, fully owned by the ViewModel:** a `ReadOnlyObjectWrapper` backing field,
exposed as two public methods — `xProperty()`, `getX()`:

```java
// ViewModel
private final ReadOnlyObjectWrapper<Foo> foo = new ReadOnlyObjectWrapper<>();

public ReadOnlyObjectProperty<Foo> fooProperty() {
    return this.foo.getReadOnlyProperty();
}

public Foo getFoo() {
    return this.foo.get();
}

ReadOnlyObjectWrapper<Foo> fooWrapper() {
    return this.foo;
}

```

3. **Read only from outside, with a request setter — for state a View or the platform owns, not the
ViewModel (the `wrapper`/`source` naming pattern, its setter marked `@RequestSetter`).** Some ViewModel
state isn't decided by the ViewModel itself — it's decided by the real widget or the OS (a window's
width/height/x/y, maximized/minimized, a `TableView`'s selection) — and the *requested* value and the
*actual* value can legitimately diverge (the platform clamps, ignores, or rejects the request). This is why
real JavaFX's own `Window#widthProperty()` is `ReadOnlyDoubleProperty` with a separate best-effort
`setWidth(double)`, not a plain `DoubleProperty`. For this kind of state:

- Expose it as a `ReadOnly*Property` on the Port/ViewModel — never a plain writable `Property`. A writable
  property lets a caller write an optimistic value straight in; if the platform then silently rejects the
  request (no compensating change event fires, because nothing about the real state actually changed), that
  optimistic value sits there being wrong forever — a "lying property," which is worse than the
  two-way-binding reentrancy problem it might look like it's avoiding.
- Back it with a `ReadOnly*Wrapper` field, and add a package-private `xWrapper()` accessor — documented as a
  framework contract ("written directly by the View... direct invocation by user code results in undefined
  behavior") — that the paired View `.bind()`s straight to the real widget/`Stage` property. When there is no
  independent widget truth to bind to at all (confirmed only asynchronously, e.g. by a manager after it
  finishes an operation), the View instead writes the wrapper directly, once, at the point the real outcome
  is known.
- Give the public setter (`setWidth`, `setSelectedItem`, ...) a matching package-private `xSource()`
  accessor returning an `ObservableSource<T>`. The setter only calls `xSource.next(value)` — it never touches
  the wrapper itself. The View subscribes to `xSource()` and performs the real widget/platform call; the
  outcome flows back solely through the wrapper. This keeps exactly one writer per property in each
  direction, so there's no reentrancy/cycle to guard against and no way to bypass whatever side effect the
  request needs to trigger. Annotate the setter itself with `@RequestSetter`, marking it as best-effort rather
  than a guaranteed state change.
- Don't reach for `wrapper`/`source` for state the ViewModel fully owns itself, even if its setter carries a
  side effect or validation (a derived flag to update, a check that throws) — as long as the setter is the
  *only* way in, a plain `Property` is correct and a `ReadOnly` wrapper only makes the API harder to use for
  no safety benefit. Reserve `wrapper`/`source` for state a second, independent party (the View, the
  platform) can also change out from under the ViewModel.
- **Naming applies uniformly, with or without a paired wrapper.** A command source with no matching
  `ReadOnly*Wrapper` at all (e.g. a ViewModel unconditionally telling the View "re-render this now," with no
  independent "actual" value to diverge from) is still named `<action>Source`, with a same-named accessor
  `<action>Source()` — never `request<Verb>`/`getRequest<Verb>()`.

```java
// ViewModel
private final ReadOnlyObjectWrapper<Item> selectedItem = new ReadOnlyObjectWrapper<>();

private final ObservableSource<Item> selectedItemSource = new SimpleObservableSource<>();

public ReadOnlyObjectProperty<Item> selectedItemProperty() {
    return this.selectedItem.getReadOnlyProperty();
}

public Item getSelectedItem() {
    return this.selectedItem.get();
}

@RequestSetter
public void setSelectedItem(Item item) {
    this.selectedItemSource.next(item);
}

ReadOnlyObjectWrapper<Item> selectedItemWrapper() {
    return this.selectedItem;
}

ObservableSource<Item> selectedItemSource() {
    return this.selectedItemSource;
}
```

```java
// View
viewModel.selectedItemWrapper().bind(this.table.getSelectionModel().selectedItemProperty());
viewModel.selectedItemSource().addListener((item) -> {
    if (item == null) {
        this.table.getSelectionModel().clearSelection();
    } else {
        this.table.getSelectionModel().select(item);
    }
});
```

4. **Collections fully owned by the ViewModel:** an `ObservableList`/`ObservableSet`/`ObservableMap` follows
the same read-only-from-outside idea without needing a wrapper at all: keep a private modifiable collection,
expose an unmodifiable view over it to the View (`FXCollections.unmodifiableObservableList(...)`) — callers
observe structural changes directly — and, if a subclass needs to mutate the collection itself, expose the
modifiable collection to subclasses through a `protected` accessor rather than widening the public one.

## Menus and registries

GreyWolf uses registries for dynamic, order-independent, plugin-friendly UI composition —
components register contributions by symbolic slot rather than direct object reference, so registration can
happen before the target component (or even its plugin) is loaded/created. shellfx splits this in two registries
(`com.techsenger.shellfx.core.registries`), both reached through the shell's `ShellContext` (`GreyWolf`
implements it, so `greyWolf.getSlotRegistry()`/`getControlRegistry()`):

- `SlotRegistry` holds the structure: `register(parent, position, child)` puts a slot into another one (a menu bar
  holds menus, a menu/context menu holds groups of menu items, a group holds nested menus).
- `ControlRegistry` holds the factories: `register(menuSlot, factory)` binds the control a menu bar/menu/context
  menu slot stands for, and `register(groupSlot, position, factory)` puts a leaf control (a menu item) into a group.

Slots (`com.techsenger.shellfx.material.slot`: `MenuBarSlot`, `MenuSlot`, `ContextMenuSlot`, `ToolBarSlot`,
`GroupSlot<V, L>`) are immutable constants typed by the target view `V` and, for groups, the control type `L`
(`MenuItem` for menus), so a wrong pairing or factory fails to compile. Menus and items are plain JavaFX
(`Menu`, `MenuItem`, `CheckMenuItem`, `RadioMenuItem`, `ContextMenu`) with a handler wired via
`MenuItemHandler.setHandler(item, new XxxItemHandler(...))`. `ManagedControlBuilder` reads both registries and
assembles the final menu tree (a simple `ControlBuilder` does the same for tool bar groups);
`MenuBarManager`/`ContextMenuManager` (`material.menu`) then wire that tree's runtime behavior (action dispatch,
visibility). The slots are declared in `XxxSlots` catalogs named after the component or module that declares them
(`GreyWolfShellSlots`, `PanelTabDockSlots`, `NavigatorShellSlots`), all in the module's exported `slots` package.

A module does not have one registrar: for each component it registers into it has an `XSlotRegistrar` (extends
`AbstractGreyWolfSlotRegistrar`; puts slots into the tree, only needed when the module owns or places menus/groups), an
`XControlRegistrar` (extends `AbstractGreyWolfControlRegistrar`; binds the factories of the menus, groups and items),
or both - each is optional, a component gets only the kind it needs. `X` is the component the registrations go into
(`ShellControlRegistrar`, `ToolBarSlotRegistrar`). The base classes (`AbstractGreyWolfSlotRegistrar`,
`AbstractGreyWolfControlRegistrar`, `AbstractGreyWolfHandler`) live in the exported
`greywolf-core/.../core/registrar`. A module keeps two packages for this: `slots` (exported) holds its
`XxxSlots` catalogs - every slot is an extension point, so every slot is public, whichever component it belongs to -
and `registrars` (closed, never exported) holds all of its registrars together with their item handlers and helpers,
whichever component they register into: the class name already says the target (`ShellControlRegistrar`,
`ToolBarSlotRegistrar`), so no package per target is needed. Registrars are not part of the component they register
into: they use only the public API of its Port, like any other contributor. `greywolf-core` follows the same layout
(`core/slots`, the closed `core/registrars`). The module's `ModuleActivatorProvider` hands its registrars (slot
registrars first) to a `RegistrarManager` in `activate`: `registerAll()` registers them, and `unregisterAll()` in
`deactivate` withdraws them in the reverse order. When adding a new menu item, add a new `XxxItemHandler` and register
it from the matching `XControlRegistrar`.

### Accelerators

An accelerator is the hotkey of an `Action` shown by a `MenuItem` (`item.acceleratorProperty()` is bound to
`action.hotkeyProperty()`). The same action can have items in several menus (the main menu, the context menu of an
area), and several instances of a component can be open at once (two navigators), so their items would put the same
key combinations into the one map of the accelerators of the scene and overwrite each other. Therefore the shell keeps
the accelerators of the scene with the `AcceleratorManager` of shellfx (see its Javadoc) through
`GreyWolfShellView#updateAccelerators()`: the manager walks an iterator over the components to ask, in the order of
their priority: the shell (`GreyWolfShellView` is `AcceleratorAware` itself and its items are those of the main menu),
the workspace and the selected workspace tab with its components, depth-first, and takes the items of every one that
implements `AcceleratorAware`. A component decides itself whether it is in use: the area of a navigator that is not
selected, or whose panel is not active, returns an empty list, so the shell knows nothing about the dual panels. The
entries of the manager are the only ones it puts into and removes from the accelerators of the scene. The manager puts
its entry back if JavaFX replaces or removes it, so the items of the menus may be installed by JavaFX as usual. The
shell updates the accelerators when the selected workspace tab or the menu bar changes; a component that changes its
items or becomes used or unused (the dual panels tab does it when the active panel or a selected tab changes), and
whatever reassigns the hotkeys of the actions, calls `updateAccelerators()` itself. `ActionRegistry` checks only that
the names of the actions are unique, not their hotkeys: keeping the hotkeys of different actions different is up to
whoever assigns them.

## Nullability

Types use `com.techsenger.annotations.Nullable`/`@Unmodifiable`; NullAway is enforced at compile time
(`OnlyNullMarked` mode) — only annotate/guard packages that are explicitly null-marked, don't add blanket
null checks elsewhere.

## Member ordering

Not caught by Checkstyle (no `DeclarationOrder` module in the config) — must be applied by hand on every
edit, not just when writing new files.

Within a class/interface, members are sorted by three nested keys, each answering a different question and
breaking ties in the one before it:

1. **Scope — static vs. instance.** Does this member belong to the class or to the object? All static
   members form one block, placed entirely before all instance members. This is absolute — e.g. a
   `private static` method is placed above a `public` constructor, because scope outranks visibility.
2. **Role — types → fields → (constructors →) methods.** This is a dependency order, not an arbitrary
   bucket: nested types define the vocabulary that fields are declared with, fields hold the state that
   methods/constructors operate on. So within the static block: nested static types → static fields →
   static methods. Within the instance block: nested instance types → instance fields → constructors →
   instance methods.
3. **Visibility — `public` → `protected` → package-private → `private`.** Within one role (e.g. "instance
   methods"), the public contract comes before implementation detail.

So the full sequence in one class is: static nested types (public→private) → static fields
(public→private) → static methods (public→private) → nested instance types (public→private) → instance
fields (public→private) → constructors (public→private) → instance methods (public→private).

**Field grouping, within one role+visibility bucket.** The three criteria above leave ties: in a `Port` or
`ViewModel` interface/class, `getX()`/`isX()`, `setX()`, and `xProperty()` for the same field are normally
all `public` instance methods, so nothing above orders them relative to each other or to the next field's
trio. **When they share the same access modifier, group them by field** instead of batching all getters,
then all setters, then all property accessors as three separate blocks — `getX()` → `setX()` (if present) →
`xProperty()` (if present), then move on to the next field's trio:

```java
double getWidth();

ReadOnlyDoubleProperty widthProperty();

double getHeight();

ReadOnlyDoubleProperty heightProperty();
```

not

```java
double getWidth();

double getHeight();

ReadOnlyDoubleProperty widthProperty();

ReadOnlyDoubleProperty heightProperty();
```

Field order otherwise follows whichever order is already established (typically the backing fields'
declaration order in the concrete `XxxViewModel`).

If the field's methods don't share one access modifier — e.g. a `wrapper()`/`source()` pair is
package-private while `getX()`/`xProperty()` are public (see the `wrapper`/`source` pattern above), or a
setter is `protected` while its getter is `public` — visibility still wins: keep each method in its own
visibility block, don't pull a lower-visibility method up next to a public one just to keep the trio
together. The package-private `xWrapper()`/`xSource()` pair still groups with each other, just in the
package-private block further down the class, not next to the public `getX()`/`xProperty()`.

## Naming convention

Component classes follow: `[UniqueName][Role][Element]`, e.g. `AlertDialogView`, `EditorTabViewModel`,
`InfoPopupParams`, `ToolBarPort`. Role examples: `Tab`, `Window`, `Popup`, `Area`, `Panel`, `ToolBar`. Element
examples: `View`, `ViewModel`, `Params`, `Port`, `History`.

`Composer` methods split into two categories — keep this distinction when adding new component types:
- Lifecycle-managing: `open*`/`close*` (create+add / remove+destroy) and `show*`/`hide*`.
- Structural-only (no lifecycle): `add*`/`remove*`/`replace*`.

| Component | Create + Add | Remove + Destroy | Add Only | Remove Only |
|---|---|---|---|---|
| Window | `openWindow(params)` | `closeWindow(window)` | `addWindow(window)` | `removeWindow(window)` |
| Tab | `openTab(params)` | `closeTab(tab)` | `addTab(tab)` | `removeTab(tab)` |
| Dialog | `openDialog(params)` | `closeDialog(dialog)` | `addDialog(dialog)` | `removeDialog(dialog)` |
| Popup | `openPopup(params)` | `closePopup(popup)` | `addPopup(popup)` | `removePopup(popup)` |
| Page | `openPage(params)` | `closePage(page)` | `addPage(page)` | `removePage(page)` |
| Area | `openArea(params)` | `closeArea(area)` | `addArea(area)` | `removeArea(area)` |

Prefer `create*`-delegating implementations of `open*`/`show*` where the pattern already uses them — it keeps
the created component swappable.

A `Map`-typed field/parameter/local/getter/setter is named `<values>By<key>` (e.g. `Map<FileType, Boolean>
selectionsByType`, `getSelectionsByType()`/`setSelectionsByType(...)`), not `<key><values>` (e.g.
`typeSelections`). The `by`-form reads directly as "which value, keyed by which type of key" at the
declaration site, without having to look at the generic type arguments to tell which side is the key.

State mirroring from ViewModel to View goes through the View binding to the ViewModel's properties directly,
in `bind()` (a plain `.bind()`) or `addListeners()` (`ValueUtils.callAndAddListener`/manual listeners, and any
`wrapper`/`source` property — see Architecture above). The private method that actually pushes a value onto
the real widget/platform uses the `update<X>` verb, e.g. `updateWidth`, `updateMaximized`, `updateTheme` — it
is private and triggered by the View's own listener/binding, never called publicly from outside the View:

- Plain property: `ValueUtils.callAndAddListener(viewModel.xProperty(), (ov, oldV, newV) -> updateX(newV));`
- `wrapper`/`source` property: `updateX(viewModel.getX()); viewModel.xSource().addListener((value) ->
  updateX(value));` — the explicit initial `updateX(...)` call is required because an `ObservableSource` has no
  "current value" to replay to a new subscriber the way a `Property` does.

Command methods — View methods that perform an action rather than mirror ViewModel state — still use an
appropriate action verb, such as `showX`, `hideX`, `scrollToFile`, `selectFile`, `clearX`, or `refreshMenu`.

## Javadoc

Document the contract — what the member does and why a caller would use it — never how it's implemented.
If a sentence just narrates the method body in prose (the steps it performs, the fields it touches along
the way), that's implementation detail the reader can already see by opening the method; cut it. A reader
deciding whether/how to call the member should get what they need without opening its body: what it
returns or does, plus any non-obvious constraint, side effect, or precondition — not a walkthrough.

Target 120-240 characters for the main description (the summary sentence plus any `<p>` continuation)
only — this range does not apply to `@param`/`@return`/`@throws`/`@see`/`{@link}` tags at all. Under 120
characters a javadoc rarely earns its place over just reading the signature; over 240 it has usually
drifted into narrating implementation or into a multi-paragraph essay — shorten it, or split the method
instead of padding its doc further. Avoid `{@link}`/cross-references to other classes or methods where
possible, especially to types from a different module/dependency (patternfx, shellfx, toolkit) — those
references rot silently when the referenced API changes and are harder to notice/fix than one in the same
file.

Accessor methods (get, set, is, xxxProperty) — should not have Javadoc unless they provide information
that is not obvious from the method name and type.

Tags follow a different, simpler rule: keep each tag's description as short as it can be while still
saying what it needs to; the only hard limit on it is the 120-char line length itself (wrap per the
indentation rule below if one line isn't enough). There is no minimum length for a tag.

**Tag coverage.** On `public`/`protected` methods, document every element that appears in the signature —
each `@param`, every checked exception via `@throws`, and so on — as tersely as the 120-char line allows;
a missing tag reads as an oversight on API surface other code depends on. Add `@return` only when the
return value carries semantics beyond what the method name and return type already say — nullability, a
sentinel/special value, which object state it reflects, resource ownership, mutability, a specific format,
or a constraint on the value. Skip `@return` when it would just restate the method name and type (e.g.
`getName()` returning `String` needs no `@return the name`).

On package-private/private methods, add `@param`/`@throws`/etc. only when that specific tag is actually
worth calling out (a non-obvious constraint, a surprising exception) — omitting the routine ones is fine.

Each `@param`, `@return`, `@throws`, and similar tag starts on a new line. The first line holds the tag,
the parameter name (if any), and the start of the description. When the description doesn't fit the
120-char line limit, continue it on the following line(s) using a **fixed 4-space indent relative to the
leading `*`**, not aligned under the start of the description — a fixed indent stays correct regardless of
how long the tag/parameter name is, so it never needs re-indenting when a name changes length:

```java
/**
 * Resolves a file.
 *
 * @param path controls whether the operation should be performed in a
 *     lightweight mode. If {@code false}, additional validation is performed.
 * @param destination specifies the destination used to resolve relative
 *     paths and determine where the resulting files should be stored.
 * @return the resolved file.
 * @throws IOException if the file cannot be resolved.
 */
```

Do not write javadoc on an overriding (`@Override`) method — rule of thumb: the comment belongs only on
the interface method or the parent class method being overridden, not duplicated on every override.

## Code style (Checkstyle)

Checkstyle runs on every `mvn package`/`install` (see Commands above) via the `com.techsenger.checkstyle.config`
artifact (Sun checks-derived, `severity=error`) — a violation fails the build, not just a lint warning. Treat
every rule below as binding when writing or editing Java. `module-info.java` files are exempt from all of it.
Run just this check with `mvn checkstyle:check`, or skip it entirely with `-Dcheckstyle.plugin.skip=true`
or `-P unit-tests`/`-P integration-tests`.

- **Layout**: 120-char line limit, no tabs, no trailing whitespace, file must end with a newline, exactly
  one blank line between the license header and the `package` declaration, no more than 5000 lines/file.
- **Methods**: max 250 non-empty lines, max 8 parameters — both signal you should split the method/introduce
  a parameter object rather than push past them.
- **Imports**: no star imports, no unused imports, no redundant imports.
- **Naming**: standard Java conventions — `PascalCase` types, `camelCase` methods/fields/params/locals,
  `UPPER_SNAKE_CASE` for non-private constants (private constants are exempt, so `private static final` in
  `camelCase`/mixed case is fine).
- **Braces & blocks**: braces required on every `if`/`for`/`while`/etc. (no single-statement bodies without
  `{}`), no empty blocks, no nested blocks, standard left/right-curly placement.
- **Whitespace**: standard spacing around operators/generics/casts/parens (`GenericWhitespace`, `ParenPad`,
  `TypecastParenPad`, `WhitespaceAround`, `WhitespaceAfter`, `NoWhitespaceBefore`/`After`, `OperatorWrap`).
- **Coding**: no empty statements, `equals`/`hashCode` always overridden together, no assignments inside
  expressions (`InnerAssignment`), one variable declaration per statement (no `int a, b;`), simplify boolean
  expressions/returns (`return x == y;` not `if (x == y) return true; else return false;`).
- **Class design**: a class with only private constructors must be `final`; utility classes (only static
  members) must have a private constructor (matches the existing `private Foo() { // empty }` pattern
  already used throughout, e.g. `GreyWolfFileIconProvider`); fields should be `private` with accessors
  (`VisibilityModifier`), not exposed directly.
- **Misc**: array brackets on the type, not the variable (`String[] args`, not `String args[]`); long
  literals use uppercase `L` (`100L`, not `100l`).
- **No `TODO` comments** (`TodoComment` module) — since severity is `error`, a matching comment fails the
  build. Don't add new ones; open a tracked issue or just do the work instead.
