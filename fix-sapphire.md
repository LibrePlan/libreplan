# Add mvnw wrapper to sapphire2breeze, push, then bring the commit back to phase5-jakarta-migration

Context checked before writing this:
- `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties` are committed and clean on
  `phase5-jakarta-migration`, and are **not tracked at all** on `sapphire2breeze` (or `main`).
  That's why origin's `sapphire2breeze` doesn't build via `./mvnw`.
- `mvnw` is stored with the executable bit (mode 100755) in git, so restoring it via
  `git checkout <branch> -- mvnw` brings the executable bit with it — no manual `chmod` needed.
- Local `sapphire2breeze` and `origin/sapphire2breeze` are currently in sync (0 ahead / 0 behind),
  so step 4's push will be a plain fast-forward push, not a force-push.
- Your current branch (`phase5-jakarta-migration`) has no uncommitted/staged changes to tracked
  files, so switching away and back is safe.

Run these from the repo root: `/home/jeroen/src/libreplan-vibe-cli/claude/libreplan`

## 1. Switch to sapphire2breeze

```bash
git checkout sapphire2breeze
```

## 2. Bring in ONLY the mvnw wrapper files from phase5-jakarta-migration — nothing else

```bash
git checkout phase5-jakarta-migration -- mvnw mvnw.cmd .mvn/wrapper/maven-wrapper.properties
```

This stages exactly those 3 paths from that branch's tree into your current (sapphire2breeze)
index and working directory. It does not touch any other file.

## 3. Verify only those 3 files are staged

```bash
git status
```

You should see exactly:

```
new file:   .mvn/wrapper/maven-wrapper.properties
new file:   mvnw
new file:   mvnw.cmd
```

everything else in `git status` (screenshots, css review files, build scripts, etc.) should
still show as untracked, **not** staged — leave those alone, don't `git add` them.

## 4. Commit and push to the existing PR branch on origin

```bash
git commit -m "Add Maven wrapper (mvnw) so this branch builds standalone"
git push origin sapphire2breeze
```

This updates the already-open PR for `sapphire2breeze` with this one commit.

## 5. Switch back to phase5-jakarta-migration

```bash
git checkout phase5-jakarta-migration
```

## 6. Bring that same commit into phase5-jakarta-migration

```bash
git cherry-pick sapphire2breeze
```

Note: since the commit's content was copied *from* phase5-jakarta-migration in step 2, this
branch already has identical file contents. Git will most likely report the cherry-pick as
empty (nothing to apply). If that happens:

```bash
git cherry-pick --allow-empty sapphire2breeze
```

records an empty marker commit so the commit is still present in phase5-jakarta-migration's
history (useful if you want a clean lineage / to avoid future merge conflicts on these 3 files).
If you'd rather just skip recording anything here since the files are already identical:

```bash
git cherry-pick --skip
```

Either is fine — pick based on whether you want the history marker.

## Push phase5-jakarta-migration (only if/when you're ready)

Not included above since you didn't ask for it — after step 6 your local
`phase5-jakarta-migration` will be ahead of `origin/phase5-jakarta-migration` by the cherry-pick
commit (if not skipped). Push it yourself when ready:

```bash
git push origin phase5-jakarta-migration
```
