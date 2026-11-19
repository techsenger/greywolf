# Techsenger GreyWolf

Techsenger GreyWolf is an integrated file management environment that brings together modern, feature-rich tools for
working with files, terminals, and system environments in a single, cohesive application with a carefully designed
interface built for efficient everyday use. It is designed for power users, system administrators, and developers who
need a versatile environment for managing and working with their systems.

GreyWolf is a single, complete system rather than a collection of separate programs: every tool, menu, and dialog,
plugins included, is built on the same foundation, so the whole application looks and behaves consistently.

The project is actively evolving toward becoming a comprehensive suite of tools for managing and working with systems,
with new components being added over time.

The application is structured like a web browser: a main menu on top and, below it, a workspace of top-level tabs.
Unlike web pages, each tab is a self-contained mini-application (Navigator, Terminal, Dual Panels, and so on). Dual
Panels is one of these tabs: it holds two independent panels, and each panel can host any other tool — for example,
Navigator on one side and Terminal on the other.

All of the application's functionality is delivered through plugins. Plugins can be installed, deployed, activated,
updated, and removed dynamically, allowing the application to grow without modifying the host itself.

Plugins can depend on other plugins, forming a plugin dependency graph. The plugin management system resolves these
dependencies and matches compatible component versions, allowing independently developed and versioned plugins to be
composed into a coherent application while remaining loosely coupled to one another and to the host.

GreyWolf is powered by many projects from the Techsenger ecosystem, including:

[PatternFX](https://github.com/techsenger/patternfx) is a framework with architectural pattern templates that provides
GreyWolf with its underlying component-tree architecture. GreyWolf uses the MVVM template, and every part of the
application is composed of components from this tree.

[ShellFX](https://github.com/techsenger/shellfx) is a platform for building JavaFX applications structured as a tree
of MVVM components. It builds on PatternFX and provides both the abstract component types (window, tab, area, page,
dialog, popup) and ready-to-use concrete ones (a docking layout, a universal file chooser, the dynamic main menu,
workspace models, etc.) that GreyWolf' shell and plugins are assembled from.

[Weaverbird](https://github.com/techsenger/weaverbird) is a framework, built on top of JPMS (the Java Platform Module
System), for managing modular systems through dynamic module layers. It manages the application's boot, installation,
and upgrades, while providing comprehensive lifecycle management for JPMS-based plugins, including their
installation, deployment, activation, updating, and removal. During deployment, it resolves plugin dependencies and
assembles compatible component versions according to their declared requirements.

## Table of Contents
* [Demo](#demo)
    * [Navigator](#demo-navigator)
    * [Terminal](#demo-terminal)
    * [Dual Panels](#demo-dual-panels)
* [Features](#features)
* [Dual Panels](#dual-panels)
* [Plugins](#plugins)
    * [Navigator](#plugins-navigator)
    * [Terminal](#plugins-terminal)
    * [Base DevTools](#plugins-base-devtools)
    * [ShellFX DevTools](#plugins-shellfx-devtools)
    * [Weaverbird DevTools](#plugins-weaverbird-devtools)
* [Quick Start](#quick-start)
* [Requirements](#requirements)
* [Code Building](#code-building)
* [License](#license)
* [Contributing](#contributing)
* [Support Us](#support-us)

## Demo <a name="demo"></a>

### Navigator <a name="demo-navigator"></a>
<p><img width="1200" height="800" alt="GreyWolf Navigator" src="https://github.com/user-attachments/assets/2b2fc9bb-4f19-459a-b938-4081fee19f90" /></p>

### Terminal <a name="demo-terminal"></a>
<p><img width="1200" height="800" alt="GreyWolf Terminal" src="https://github.com/user-attachments/assets/33788f8d-bb20-417d-acf3-5883d14e01fa" /></p>

### Dual Panels <a name="demo-dual-panels"></a>
<p><img width="1200" height="800" alt="GreyWolf Dual Panels" src="https://github.com/user-attachments/assets/94230ebd-b593-4ff9-b9e3-556099971dc7" /></p>
<p><img width="1200" height="800" alt="GreyWolf Dual Panels" src="https://github.com/user-attachments/assets/95ce29c8-19f0-45fa-a603-03951c4f245e" /></p>

## Features <a name="features"></a>

**One consistent system**
- A unified UI foundation for the entire application: tools, menus, toolbars, and even dialogs, including the file
  chooser, are provided by the application rather than the operating system, ensuring a consistent look and feel across
  platforms.
- A wide selection of light and dark themes that apply consistently across the entire application, including all plugins.
- Plugins are an integral part of the system, not separate add-ons. They use the same UI components, making
  plugin-provided tools indistinguishable from built-in ones.

**Workspace**
- A browser-like workspace with a main menu at the top and tabs below, each containing a self-contained tool.
- A docking layout for auxiliary components that can be independently arranged, resized, and collapsed around the main
  content.
- The main menu, context menus, and toolbars are assembled dynamically from contributions by installed plugins, with
  support for keyboard shortcuts.

**Navigator**
- Three view modes: list, tiles, and details.
- A sidebar with a directory tree, a list of places (Personal and Storages), or both, alongside a location bar.
- Back, forward, and up navigation with history.
- File marking: individually, by range, by wildcard mask, or by regular expression, with support for inverting the
  selection.
- File operations: copy, move, delete, rename, create, and view properties, as well as clipboard cut, copy, and paste.
- A storage abstraction supporting the local file system and recently used files out of the box, with additional
  storage providers available through plugins.

**Terminal**
- An embedded, cross-platform terminal supporting Bash, CMD, PowerShell, and other shells. It can run in its own tab
  or in a panel and open directly in the currently browsed folder.
- Switchable color palettes (16 colors, or 32 colors with high or low contrast), selected right from the terminal's
  tool bar, which is convenient for working with text-based (TUI) applications.

**Dual Panels**
- Two independent panels side by side, providing the familiar layout of classic dual-pane file managers. Unlike
  traditional file managers, either panel can host any tool, not just a file browser.
- Convenient transfer between panels: a single shortcut (`Ctrl+K` or `Ctrl+Shift+K`) sends the selected file or current
  folder to the tool in the opposite panel. The receiving tool decides how to handle it. For example, sending a folder
  from Navigator to Terminal changes the terminal's current directory.
- Copy and move operations in Navigator use the opposite panel as the default destination.
- Any tool can be promoted from a panel to a top-level workspace tab.

**Plugins**
- All application functionality is delivered through plugins, which can be installed and removed dynamically.
- Plugins can depend on one another, with dependencies resolved and compatible versions matched automatically.
- Plugins can extend each other's menus, toolbars, and context menus through extension points, without modifying the
  original plugins.

**DevTools**
- Built-in tools for developing GreyWolf, its plugins, and the frameworks it is built on, available in the Tools /
  DevTools menu.
- Base DevTools for the application and its plugins: opens plugin dialogs directly, populated with dummy data and
  outside their normal triggers, so styling and layout can be worked on in isolation.
- ShellFX DevTools: inspection of the running application at two levels, the MVVM component tree and the JavaFX scene
  graph.
- Weaverbird DevTools: a console for executing text commands, and diagrams for analyzing components, layers, modules,
  and their properties.

## Dual Panels <a name="dual-panels"></a>

Dual Panels is the central component for combining tools: a top-level tab that provides two independent panels for
using tools side by side. Each panel can host any plugin that implements the panel-tab contract. Panel-level
operations can also coordinate the tools in both panels, such as copying or moving files, synchronized navigation,
and directory comparison.

Beyond the standard combination of Navigator in both panels, either panel can just as well hold a different tool
— for example, Navigator in one panel and Terminal in the other.

A Navigator can send a folder to the tool in the other panel, so the two panels stay in sync without retyping
paths. `Ctrl+K` sends the folder selected in the Navigator, while `Ctrl+Shift+K` sends the Navigator's current
folder instead. Both are also available in the File menu and in the context
menu of the Navigator. The receiving tool decides what to do with the folder: a Navigator opens it, and a
Terminal changes its current directory to it. If the other panel holds a tool that cannot use the folder, or the
selected entry is not a folder, nothing happens. Focus stays in the panel the folder was sent from.

As a rule, the main application tools, such as Navigator and Terminal, can be displayed either as top-level tabs
in the Shell workspace (`Placement#WORKSPACE`) or as embedded tabs inside Dual Panels (`Placement#DUAL_PANELS`).

When a tool is displayed in the workspace placement, it normally has its own docking layout. This provides a number
of advantages when working with auxiliary components, allowing them to be arranged, resized, collapsed, and managed
independently of the main content.

When a tool is displayed in the dual panels placement, it does not create its own docking layout. Instead, a
simplified layout is used. A single auxiliary `TabDock` is placed next to the main content using a `SplitPane`, and
its tab header area is hidden. As a result, the user sees a simple auxiliary panel in which only one tab is displayed
at a time, while the underlying tab-based structure is preserved.

This simplified layout also provides a natural way to promote a tool from Dual Panels to the Shell workspace. When
such a tab is moved to the Shell workspace and its placement changes from `DUAL_PANELS` to `WORKSPACE`, its auxiliary
`TabDock` is added to the tool's docking layout. The auxiliary tab is therefore preserved, while the simplified layout
is replaced with the full-featured docking layout.

Promotion from `DUAL_PANELS` to `WORKSPACE` is supported, while the reverse transition is not. There is no need to
move a tool from the workspace placement back into Dual Panels, as creating a new instance there is simpler. In
addition, the layouts are inherently asymmetric: the simplified layout can be expanded into a full-featured docking
layout, but a full-featured docking layout cannot be reduced back to the simplified layout while preserving its
structure. Therefore, placement transformation is intentionally supported only from `DUAL_PANELS` to `WORKSPACE`.

## Plugins <a name="plugins"></a>

All built-in plugins below are published by Techsenger under the Apache License 2.0 and are free to use. Every one
of them depends on `greywolf-core` itself (matched by patch version); a dependency on another plugin is called out
explicitly where it exists.

### Navigator <a name="plugins-navigator"></a>

Navigator is GreyWolf' file management tool: browsing directories and performing everyday file operations such
as copying, moving, renaming, and deleting. It can be opened as its own top-level tab for single-location browsing,
or embedded inside Dual Panels, where it gains an implicit destination for copy and move operations — the sibling
panel.

### Terminal <a name="plugins-terminal"></a>

Terminal is a cross-platform terminal plugin built on JediTermFX, supporting Bash, CMD, PowerShell, and other system
shells, so commands can be run and the operating system interacted with directly inside GreyWolf. Like Navigator, it
can run as its own top-level tab or be embedded inside Dual Panels, most commonly to get a shell open in the same
directory the sibling panel is currently browsing.

### Base DevTools <a name="plugins-base-devtools"></a>

Base DevTools opens other plugins' dialogs directly, populated with dummy data and outside their normal triggers, so
their styling and layout can be worked on in isolation. It optionally depends on Navigator to also expose that
plugin's own dialogs the same way.

### ShellFX DevTools <a name="plugins-shellfx-devtools"></a>

ShellFX DevTools inspects and analyzes the application at two levels: the MVVM component tree and the underlying
JavaFX scene graph.

### Weaverbird DevTools <a name="plugins-weaverbird-devtools"></a>

Weaverbird DevTools provides two tools for working with the Weaverbird framework that GreyWolf' plugin system is
built on: a console for executing text commands, and diagrams for analyzing components, layers, modules, and their
properties.

## Quick Start <a name="quick-start"></a>

Getting started takes three steps:

1. **Download** the latest build, `greywolf-<version>.zip`, from the
   [Snapshot release](https://github.com/techsenger/greywolf/releases/tag/snapshot) on GitHub.
2. **Unpack** the archive anywhere you like.
3. **Run** the launch script from the `bin` directory: `greywolf.sh` on Linux and macOS, or `greywolf.bat` on
   Windows.

The only prerequisite is Java (see [Requirements](#requirements)). On the first launch the application downloads its
components, including JavaFX, so an internet connection is needed.

## Requirements <a name="requirements"></a>

The project requires Java 27.

## Code Building <a name="code-building"></a>

To build the project use standard Git and Maven commands:

    git clone https://github.com/techsenger/greywolf
    cd greywolf
    mvn clean install

After the build, the ready-to-run application is in `greywolf-dist/target/greywolf-<version>`, for example
`greywolf-dist/target/greywolf-1.0.0-SNAPSHOT`.

## License <a name="license"></a>

Techsenger GreyWolf is licensed under the Apache License, Version 2.0.

## Contributing <a name="contributing"></a>

We welcome all contributions. You can help by reporting bugs, suggesting improvements, or submitting pull requests
with fixes and new features. If you decide to submit a pull request, please read the following first.

**Important:** Before starting work on a pull request, please create an issue and agree on the task with us. GreyWolf
has a clear vision and development plan, and every contribution needs to align with them. Discussing the idea in an
issue allows us to confirm that it fits the project's direction and suggest the best approach. It also helps avoid
duplicated work and unnecessary effort. Pull requests that have not been discussed beforehand may be declined,
regardless of their implementation quality. We would not want you to spend your time on work that cannot be accepted.

If you have any questions, feel free to reach out — we'll be happy to assist you.

## Support Us <a name="support-us"></a>

You can support our open-source work through [GitHub Sponsors](https://github.com/sponsors/techsenger).
Your contribution helps us maintain projects, develop new features, and provide ongoing improvements.
Multiple sponsorship tiers are available, each offering different levels of recognition and benefits.

