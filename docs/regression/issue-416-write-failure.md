# Regression plan — upstream issue #416

Target: Minecraft 26.3 / Litematica fork / MaLiLib 0.30.2 / Java 25.

## Failure reproduction (Unix)
1. Prepare a `.litematic` file, load it in Litematica, and modify a block in memory.
2. Record the original file checksum and make the file non-writable for the current (non-root) user: `chmod a-w <file.litematic>`.
3. Invoke `writeToFile(dir, name, true)` for normal and for `downgrade=true` variants.
4. Verify it returns **false**, displays an error, and does not reset the schematic's modified marker in the GUI.
5. Verify the on-disk checksum is unchanged; restore write permissions.
6. Repeat with writable target: returns **true**, checksum changes, saved file loads with edits.

## Other failure paths
- Missing parent directory that can be created normally.
- Parent directory without write permission.
- Existing file with `override=false` (must still return false).
- Filesystem out of space or I/O failure (if a safe disposable test environment is available).
- Non-ASCII filename: must still resolve to the existing target path.

## Acceptance criteria
The return value mirrors MaLiLib's `writeCompoundDataToCompressedNbtFile` boolean for both current-format and downgraded schematics. The GUI must never display success or clear modified state after a failed write.

**Note:** `./gradlew build` tests compilation, not filesystem failure behavior.
