# Donut Tracker

Java 21 / Gradle project with a **Fabric client mod for Minecraft 1.21.11** and an executable desktop settings companion.

## Install the Minecraft mod

1. Install Fabric Loader **0.18.4 or later** for Minecraft **1.21.11**.
2. Install Fabric API **0.141.6+1.21.11** in your Minecraft `mods` folder.
3. Copy `fabric/build/libs/donut-tracker-fabric-1.21.11-1.4.3.jar` into that folder.
4. Launch the Fabric profile and press **Right Shift** while in a world/server.

The mod is client-only. It needs no server plugin, account password or API key. The JAR is loaded by Fabric; running it with `java -jar` prints installation instructions.

## New in 1.4.3: live net profit

`/start` starts a live net session; `/stop` freezes and saves it. Incoming payments add to the total, and confirmed outgoing payments subtract. Example: receive 3M, pay 2M → +1M, pay another 2M → -1M. Negative totals use the HUD's loss colour, including the hourly rate. Queue thresholds only filter the queue, not the tracker.

Only matching server confirmations change the total: submitting `/pay` or clicking 2×/3× does not subtract until confirmed. Default expense detection supports `You paid Steve $2M`, `You paid $2M to Steve`, `You successfully paid $2M to Steve!`, `You sent $2M to Steve`, and `You lost $3M`. Actual server wording may differ; adjust Tracker → Messages → Expense pattern (one amount capture). No automatic `/bal` is sent. Other losses without a matching chat message cannot be detected immediately; a manual balance reading reconciles changes if an earlier balance is known. Without a prior balance, the first reading establishes a baseline for later reconciliation.

`/sstart` and `/sstop` remain aliases. Session results stay frozen after stopping. Manual balance readings replace the estimated balance without counting recognized payments twice. With no starting balance, history endpoints are accounting values representing the net result, not verified bank balances.

## New in 1.4.0: clearer settings

The settings menu replaces stone-texture buttons with distinct controls: a visibility switch, separate Clean/Neon style choices, illustrated Compact/Stacked/Minimal cards, and labelled size/opacity sliders with percentage readouts. Navigation uses highlighted text tabs; action buttons have secondary and primary styles. Colours uses compact swatches with HEX labels, and the module chooser and colour picker share the same dark controls.

On wide scaled GUI layouts, Design also shows a live preview of the selected HUD. Narrow layouts keep all controls usable without a preview. Sliders apply their final value on mouse release, including fast drags. Changes save automatically; Back to modules returns to the module chooser and Done closes settings. Use Design → Move HUD... to reposition a box. Payment/chat behaviour is unchanged: inline 2×/3× controls, no automatic /bal, and no branded success confirmation.

## Return controls inside chat

Server payment messages now keep their original chat position and formatting and append clickable **[2×] [3×]** buttons to that same line. Open normal chat with **T** and click a multiplier. Hovering shows the exact return command. There are no return buttons above the HUD and no P overlay.

For example, `1arp_ paid you $1M` gets buttons that immediately send `/pay 1arp_ 2000000` or `/pay 1arp_ 3000000`. Buttons reference that exact receipt, even if another player is at the front of the HUD queue. A removed receipt or a button from an earlier server session cannot pay another player. Successful clicks do not add a mod-branded chat confirmation; only the server response appears. Local errors remain visible without a mod prefix. The server response determines whether a transfer succeeds.

One return is permitted per queue entry: its request marker is persisted before dispatch and survives restart. Repeated clicks cannot send another return; ambiguous/failed sends are never automatically retried. No live funds are used in tests. Old queue files remain readable, known old default patterns upgrade, and custom patterns remain intact.

## Clean HUD design

The gameplay HUDs now use the clean design preview: rounded translucent charcoal cards, subtle borders, large centered session profit, a timer/rate row and balance footer. Payment Queue includes the active player head, name, amount, waiting-count pill and next-player summary. Clean mode has no accent outline or glow; Neon remains available. Each module has Design → Clean preset for the mint/lavender/cyan palette without overwriting saved custom colours automatically. Positions, scaling, layouts and display toggles remain supported. Minecraft’s native pixel font is used rather than the mockup’s smooth font. The arrow in the queue footer is decorative; Next Player is available in the module menu.

## Payment refresh fix

Automatic `/bal` commands are removed in 1.4.0. Receiving payments, clicking 2×/3×, and ticking a running session never request a balance. Old `balance.poll=true` settings are ignored and retired. Incoming payments update active sessions immediately in 1.4.2. In balance-based sessions, manual balance readings replace the estimated balance; in `/start` sessions, later readings reconcile any unreported changes against the last known balance.

## Module controls

Right Shift now opens two module cards: **Tracker** and **Payment Queue**. Open either card to configure that module on its own screen. Detection is scoped to the selected module, and session history belongs to Tracker.

**Colours** displays actual coloured swatches. Click one for a saturation/brightness square, a rainbow hue strip, preset colour tiles, a live swatch and optional HEX input. Changes apply immediately and save.

Use **Move this HUD** in either module, or **Move HUD boxes** on the module chooser. Click and drag the box itself; Escape saves its new position. Design → Move HUD... also provides numeric X/Y coordinates and reset controls. Dragging now sets the screen drag state correctly.

## Features and controls

- **Profit Tracker:** start/end sessions, total profit or loss, elapsed time, money/hour, and the latest 100 completed sessions.
- **Payment Queue:** one active player, up to 499 waiting payments, IGN and amount, a manual Next Player button, and a configurable payment minimum. Online players' heads use Minecraft's existing skin data; unavailable skins show an initial.
- **Independent HUD designs:** Clean or Neon, custom HEX accent/text/profit/loss/background colours, colour presets, 50–200% size, background opacity, compact/stacked/minimal layouts, and display toggles.
- **Move HUDs:** drag either card, then Escape to save. Settings, completed history, and pending payments persist under Minecraft's `config` folder. A live session is not resumed after restarting.
- Money uses **K / M / B / T**, including decimals and negative values. Calculations use decimal arithmetic; abbreviations affect display only.

The default design is **Clean**, dark purple with pink/purple and cyan accents. **Neon** adds an accent-coloured glow; it does not flash or animate.

### Balances and payments

The parser matches these user-provided server messages, ignoring Minecraft colour formatting and extra spaces after `$`:

```text
You Have $ 2,311,178,968
Imunknown67 paid you $ 1
```

Only server-generated chat is parsed; ordinary player chat is ignored. Patterns can be changed in **Detection**. Server prefixes or different wording may require a pattern update. Matching has been tested with the supplied examples; live DonutSMP behaviour needs verification on your client.

**Start tracking** and **End tracking** send `/bal` and wait up to 10 seconds for a fresh matching balance before recording their endpoint. Timeout preserves the existing session. There is no periodic polling or automatic refresh after incoming or outgoing payments. Incoming payments now update the HUD immediately; run `/bal` yourself to reconcile a balance-based session after spending or other changes. Start/End buttons labelled `/bal` and Read /bal now are explicit manual requests; Start manual and End at last balance send no command. Manual balance/start/end controls are available as a fallback. Profit is ending balance minus starting balance, so spending and losses count too. Incoming payments update the live session independently of the queue minimum.

The default minimum payment is **100K**. Set it to **1** in Payment Queue → Queue to include the sample `$1` payment. The minimum affects future arrivals, not payments already queued. On disconnect, an active session ends at its last observed balance, which may be stale.

Files: `config/donut-tracker.properties` and `config/donut-tracker-sessions.properties`. Invalid files are preserved and saving is blocked until corrected and the client restarted.

## Build

On a machine with JDK 21:

```sh
./gradlew -PwithFabric build :fabric:build
```

Windows: use `gradlew.bat`. The checksum-pinned wrapper downloads Gradle **9.2.1**. Versions are pinned for Minecraft, Fabric Loader, Fabric API, and Loom.

Outputs:

- `fabric/build/libs/donut-tracker-fabric-1.21.11-1.4.3.jar` — installable mod.
- `build/libs/donut-tracker-1.4.3.jar` — executable desktop companion, not a Minecraft mod.

For only the companion and core verification (no Minecraft dependencies): `./gradlew build`.

`coreTest` runs an assertion-based verification runner with formatting, supplied chat examples, profit/rate/timer calculations, losses, FIFO queue behaviour, filtering, and design/history persistence checks. There is no JUnit suite; Gradle's `test` tasks report `NO-SOURCE` on a clean build.

## Run

```sh
# Minecraft development client
./gradlew -PwithFabric :fabric:runClient

# Desktop companion (requires a graphical desktop)
java -jar build/libs/donut-tracker-1.4.3.jar

# Render the actual companion settings screen without a display
java -jar build/libs/donut-tracker-1.4.3.jar --render settings-preview.png
```

The companion has manual balance/payment controls and a parser test panel. It does **not** connect to Minecraft or read a running game's chat. Its settings/history live separately in `~/.donut-tracker`, or a directory supplied with `--config DIRECTORY`.

## Cloud environment

The task is already isolated; use `/workspace/java-` directly and do not create a worktree.

```sh
bash .cloud/install.sh       # repeatable setup, build and verification
source .cloud/env.sh         # initialize each fresh shell
```

Cloud setup installs a checksum-verified Temurin JDK under `/workspace/toolchains`, configures Gradle for the existing HTTPS proxy and its platform CA, and keeps TLS verification enabled. It does not install system packages or require root. Cached dependencies and tools are retained in the published snapshot; running processes must be restarted. A graphical Minecraft client additionally needs a display or virtual framebuffer.
