# Navigator

## Marking files

A file tab lets the user mark one or more files (as opposed to merely selecting/focusing a single entry for
navigation). Marking is classified along two axes: manual vs. automatic, and, within manual marking, by how
many files are affected and by which input device is used.

### Manual marking

Manual marking is driven directly by the user pointing at a specific entry (or range of entries).

- **By type**
  - *Single* — marks (or unmarks) exactly one file at a time.
  - *Multiple* — marks a range of files in one action.
- **By method**
  - *Keyboard* — e.g. `Insert` toggles the mark of the current file and advances the selection (single);
    `Shift`+arrow keys/`Home`/`End` extend a marked range (multiple).
  - *Mouse* — e.g. `Ctrl`+Click toggles the mark of the clicked file (single); `Shift`+Click marks the range
    between the anchor and the clicked file (multiple).

### Automatic marking

Automatic marking is driven by a criterion rather than by the user pointing at a specific entry or range:
*Mark All* / *Unmark All*, *Invert Selection*, and marking by mask/pattern (e.g. `*.txt`) all fall into this
category.

Marking by mask/pattern is driven by `MarkDialog`, reachable from the Mark menu (NumPad `+`/`-`/`*`
accelerators) for each of the three `MarkOperation`s &mdash; `MARK`, `UNMARK`, `INVERT`. The dialog lets the
user type a mask (wildcard `*`/`?` by default, or a full regular expression when Regex is toggled on),
choose whether matching is case-sensitive, and restrict it to files, folders, or both; *Apply* runs the
operation without closing the dialog (so it can be re-run with a different mask), *OK* runs it and closes,
*Cancel* closes without running it.

**Unlike manual marking, `MarkOperation` never toggles.** `MARK` always leaves a matching entry marked and
`UNMARK` always leaves it unmarked, regardless of whether it was already in that state &mdash; neither one
flips it the way `Insert`/Ctrl+Click do. `INVERT` does flip each matching entry's own current mark, but only
once, when the operation runs against the whole matched set &mdash; not on repeated key presses/clicks the
way manual marking's toggle is.
