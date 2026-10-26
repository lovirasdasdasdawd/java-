## Optional Christmas GUI — 1.22.0
Right Shift → Atmosphere → Panels → Christmas GUI enables a festive interface: dark evergreen panels, warm gold controls, cream text, red/gold garlands, frosted trim, a decorated tree and snowy winter scenery. It applies to the shared GUI without replacing custom HUD colours or cape settings.

The toggle saves independently of the normal palette/background. Turn it off to restore those choices; choosing a normal palette or scene also exits Christmas mode. Glass, opacity and existing opening/closing motion remain available. Scene animation can freeze the snowfall. No flashing lights or additional UI transitions are added.

Replace the old combined JAR with `donut-tracker-fabric-1.21.11-1.22.0.jar`. All previous modules and Christmas capes remain.

## Christmas capes — 1.21.0
Three Christmas designs join the collection (14 total): Christmas Tree with fairy lights and presents, Winter Night with moonlit snowfall and snowdrifts, and Gingerbread with icing and candy buttons. Each has three independent Shape controls plus the existing colour, glow and aura settings. Existing cape selections and per-design settings are retained.

Open Right Shift → Capes and browse to the Christmas designs. Replace the old JAR with `donut-tracker-fabric-1.21.11-1.21.0.jar`. The game target remains Minecraft 1.21.11 / Fabric / Java 21. Capes are client-side cosmetics.

## Distinct cape compositions & shape controls — 1.20.0
Three new designs — Dragon, Tidal and Rift — bring the collection to 11. Newer designs now have distinct artwork layouts instead of sharing one frame: Wolf uses an offset portrait, diagonal silk and claw marks; Phoenix has broad wings and a fire hem; Skull hangs from chains; Nova fills the cloth with stars and an off-centre planet; Lotus sits over pond ripples. Dragon coils down the cloth, Tidal is an all-over ocean, and Rift fractures across it.

Right Shift → Capes now separates Shape, Colours and Glow. Shape exposes three controls specific to the selected design, including portrait scale/height/sparkles, wing span/tail/embers, jaw/eyes/chains, orbit tilt/stars/planet size, petals/opening/ripples, dragon coils/wings/runes, wave height/layers/foam, and crack width/branches/shards. Shape settings save independently for every cape. Existing files receive defaults without losing old settings; original Donut/Orbit/Crown remain available.

Replace the old JAR with `donut-tracker-fabric-1.21.11-1.20.0.jar`. Still one client-side mod; no cosmetic uploads or server messages.

## Night Wolf & soft aura — 1.19.0
Five new cape designs join Donut, Orbit and Crown: Night Wolf, Phoenix, Void Skull, Nova and Lotus. Night Wolf uses a detailed purple illustrated emblem inspired by the reference. Cape atlases now render at 1024×512, with a separate emissive artwork pass and a spatially blurred aura around bright pixels. Glow strength adjusts brightness; Aura selects Narrow, Soft or Wide. It works without a shader pack. This is an emissive cosmetic, not a block-light source.

Right Shift → Capes uses previous/next design controls, colour pickers, the glow toggle, glow-strength slider, aura-width selector, real-texture preview and View in F5. Previous cape choices and colours load as before. New designs choose matching accent colours when selected. All cosmetics remain client-side and visible only to you.

Replace the old JAR with `donut-tracker-fabric-1.21.11-1.19.0.jar`; still one mod with all existing modules and atmospheres.

## Donut capes — 1.18.0
Right Shift → Capes adds optional local cape cosmetics to the same mod. Choose Donut, Orbit or Crown, customize cloth/frosting/trim with the colour picker, and adjust glow strength. Trim and sparkle pixels render in a separate emissive pass; the cloth keeps normal lighting. A real-texel rear preview and View in F5 button help inspect the cape. Existing body skin, player model and elytra texture are retained; vanilla cape visibility and elytra rules still apply.

Capes are off by default. Enable Cape in the module. They are visible only on your own client; no texture upload or server message is sent, and other players will not receive your cosmetic. Settings save separately in `config/donut-tracker-capes.properties`. The existing opening/closing menu animation remains the only interface animation.

Replace the old combined JAR with `donut-tracker-fabric-1.21.11-1.18.0.jar`. Still one mod with Tracker, Payments, Auctions, Capes, atmospheres and sounds.

## Rounded panels & HUDs — 1.17.0
GUI panels, buttons, cards and outlines now use rounded corners. Tracker and Payment Queue HUD backgrounds, shadows, borders and accent rails follow the rounded silhouette; Auction panels and HUDs match. Glass surfaces use non-overlapping rows so their transparency stays even. Existing sizes, positions, settings, opening/closing motion and tracking behaviour are retained.

Replace the previous combined JAR with `donut-tracker-fabric-1.21.11-1.17.0.jar`.

## Quiet controls & new atmospheres — 1.16.0
Only entering and leaving the main menu animate: the GUI grows into the centre on opening and shrinks away on closing. Module switches, tabs, hover states, clicks and toggles are instant. Full / Reduced / Off applies to opening and closing; landscape animation is controlled separately.

Five new scenes — Lighthouse, River valley, Cloudscape, Moonlit lake and Bamboo grove — each have their own saved tint, intensity, density and speed, plus a matching original 40-second stereo sound loop. There are now 18 scenes and 20 ambient tracks, with volume, mute and optional in-game playback.

Atmosphere → Panels shows ten interface palettes as real colour swatches, including six new options: Slate, Coffee, Lavender, Moss, Glacier and Rose. Glass and opacity remain adjustable. Choosing a palette preserves custom HUD colours until Match HUD colours is clicked.

Replace the old combined JAR with `donut-tracker-fabric-1.21.11-1.16.0.jar`. Still one mod, with Tracker, Payments and Auctions in the same GUI. Java 21, Minecraft 1.21.11, Fabric Loader and Fabric API required.

## Centre pop opening — 1.15.0
Right Shift now opens the main GUI at 12% size just below its centre, growing smoothly to its normal centred position over 290ms with cubic easing. The actual panel, text and buttons scale together. Reduced motion keeps its short full-size fade, and Off remains instant. Other module transitions and all existing features are retained.

Replace the previous combined JAR with `donut-tracker-fabric-1.21.11-1.15.0.jar`. Still one mod with Auctions inside the same GUI.

## Motion & glass — 1.14.0
One unified mod/JAR. The GUI opens with a short eased pixel reveal; module views wipe in from the sidebar, with border sweeps, hover fades, click stamps and sliding toggle switches. Full / Reduced / Off UI motion modes are saved separately from background animation. Hit boxes stay fixed during animation, and auction deadlines/tracking continue independently.

The main GUI now has quick **Glass** and **Background** controls. **Atmosphere → Panels** offers glass enable/disable, opacity (30–100%) and UI motion settings. The 13 selectable backgrounds retain their own tint, speed, intensity and density; 15 ambient sounds remain available.

**Auctions → HUD settings** offers hide/show, Glass/Solid, a 40–160% size slider, and **Move / drag auction box**. The box defaults to 80% size. The editor supports dragging, mouse-wheel resizing, arrow-key nudging and reset position. A manually placed box stays pinned where you put it; automatic default overlap avoidance is retained until you drag it. Hiding only removes the overlay; Stop auction still cancels an auction. Settings persist in auctionmod.json.

Install only `donut-tracker-fabric-1.21.11-1.14.0.jar` alongside Fabric API, replacing the earlier combined JAR. Java 21 and Minecraft 1.21.11 required.

## Unified mod — 1.13.0
**One JAR, one Fabric mod ID, one initializer:** Donut Tracker + Auctions. Auctions now appear beside Tracker and Payments in the Right Shift module sidebar, inside the same branded GUI shell. O opens that same Auctions module view. Includes the tracker, payment queue, auction HUD, glass menus/HUDs, 13 atmospheres and 15 sounds. Existing tracker and auction configuration paths are retained. When auction and tracker HUD positions would overlap, the auction HUD is placed below the tracker without rewriting saved positions.

Remove both the old Donut Tracker JAR and the separate auctionmod JAR, then install only `donut-tracker-fabric-1.21.11-1.13.0.jar` alongside Fabric API. Do not install the old separate Auction Mod; the loader reports that incompatible combination instead of registering duplicate handlers.

Build the combined mod from this repository root with `./gradlew -PwithFabric build :fabric:build`. The installable output is `fabric/build/libs/donut-tracker-fabric-1.21.11-1.13.0.jar`. Both tracker and auction checks run in this build. Auction implementation sources are shared from the auctionmod directory; the delivered combined artifact contains those classes and language assets directly, with no nested Auction Mod JAR or second mod descriptor.

## Glass & Night Routes — 1.12.0
Three new configurable atmospheres: **Volcanic Night / Ember wind**, **Firefly Marsh / Marsh night**, **Midnight Railway / Night rail**. Thirteen scenes and fifteen original sound loops total. The browsers now have three pages.

**Glass panels** on Atmosphere → Scenes toggles translucent menu panels. Each HUD independently supports **Appearance → Style → Glass**, alongside Clean and Neon. Glass uses tinted transparency and a thin highlight; no blur shader is required. Existing settings retain opaque menus and their saved HUD styles.

## Standalone Auction Mod
The complete independent project is in [auctionmod](auctionmod/README.md). Install its own `auctionmod-1.21.11-1.0.0.jar` to get the O keybind, searchable item picker, timed auctions, minimum/higher-payment detection and final-winner HUD. It can be used without Donut Tracker. Build from that directory with `./gradlew build`. All sources, Gradle wrapper, config example and tests are included.

## Seasons — 1.11.0

Actual Minecraft previews: [New scenes](releases/seasons-catalog-1.11.png) · [Sidebar divider](releases/seasons-divider-1.11.png) · [New sounds](releases/seasons-sound-1.11.png)
Five new atmospheres and matching original 32-second sound loops: **Desert Dusk / Desert wind**, **Snowfall / Snow hush**, **Deep Ocean / Abyssal drift**, **Sakura Garden / Garden chimes**, **Storm Coast / Distant storm**. Ten scenes and twelve sounds total.

Right Shift → Atmosphere → Scenes → Next opens the five new scenes. Scenes and Sound use paged browsers; all existing choices remain available. Each scene supports custom tint, intensity, particle density, speed and animation controls. Sound retains mute, volume and optional in-game playback. Storm Coast uses no flashing lightning.

The sidebar now has an inset divider with an accent diamond between the two modules and the layout/atmosphere controls, matching the requested position.

Install `donut-tracker-fabric-1.21.11-1.11.0.jar` in mods, replacing the previous version. Minecraft 1.21.11, Java 21, Fabric and Fabric API required.

## Pixel ledger — 1.10.0

Actual Minecraft previews: [GUI](releases/pixel-overview-1.10.png) · [HUDs](releases/pixel-hud-1.10.png) · [Negative profit](releases/pixel-loss-1.10.png) · [Placement](releases/pixel-placement-1.10.png)
A new compact session ledger and left module rail replace the floating dashboard. Both in-game HUDs use smaller 180-pixel frames, one-pixel corners, an accent edge, aligned values and plain dividers. Compact, Stacked and Minimal remain available, as do per-HUD colours, opacity, scale, visibility and drag positioning. All five scenes and seven soundtracks remain available under Atmosphere.

Install `donut-tracker-fabric-1.21.11-1.10.0.jar` in mods, replacing the previous version. Requires Minecraft 1.21.11, Java 21, Fabric and Fabric API.

## Worlds — 1.9.0

In-game previews: [Space](releases/space-1.9.png) · [Rainy City](releases/city-1.9.png) · [Autumn Forest](releases/forest-1.9.png) · [Details](releases/details-1.9.png)
Three new selectable menu scenes: **Space** (ringed planet and drifting stars), **Rainy City** (warm windows, rain and wet pavement), and **Autumn Forest** (layered trees, falling leaves and low mist). Aurora and Ocean remain available.

**Right Shift → Atmosphere → Scenes** selects a scene and matching soundtrack. **Details** provides per-scene intensity, density, motion speed, six visible tint swatches, a full HSV/HEX custom tint picker, and four interface palettes. Animation can be turned off. **Sound** offers seven independently selectable tracks, volume, mute and optional in-game playback. Existing settings are retained; choosing a preset does not overwrite your HUD colours or positions. Use Match HUD colours explicitly if desired.

New soundscapes: Deep space, City rain, Forest breeze. Original 32-second seamless DSP loops. Minecraft Master and Ambient volume also apply.

Install `donut-tracker-fabric-1.21.11-1.9.0.jar` in `mods`, replacing the previous version. Requires Fabric, Fabric API, Minecraft 1.21.11 and Java 21.

# Donut Tracker

Java 21 / Gradle project with a **Fabric client mod for Minecraft 1.21.11** and an executable desktop settings companion.

## Install the Minecraft mod

1. Install Fabric Loader **0.18.4 or later** for Minecraft **1.21.11**.
2. Install Fabric API **0.141.6+1.21.11** in your Minecraft `mods` folder.
3. Copy `fabric/build/libs/donut-tracker-fabric-1.21.11-1.8.0.jar` into that folder.
4. Launch the Fabric profile and press **Right Shift** while in a world/server.

The mod is client-only. It needs no server plugin, account password or API key. The JAR is loaded by Fabric; running it with `java -jar` prints installation instructions.

## New in 1.8.0: Aurora atmosphere and floating dashboard

Actual Minecraft previews: [Aurora dashboard](releases/aurora-overview-1.8.png) · [Backdrop controls](releases/aurora-backdrop-1.8.png) · [Sound controls](releases/aurora-sound-1.8.png).

Select Right Shift → Atmosphere → Backdrop → Aurora for animated green/violet curtains, stars, ridgelines and pine silhouettes. The new overview uses floating glass panels with a larger profit display and distinct session, HUD-position and atmosphere controls. Aurora is drawn procedurally with ordinary GUI gradients; no custom shader or external image download is required.

Aurora and Ocean scene presets select a matching menu palette and sound. They preserve volume, mute, playback scope and custom HUD design. Palette choices can then be mixed independently. Match HUD colours is an explicit action; it preserves HUD size, position, opacity and Clean/Neon choice. Animation and Aurora glow are adjustable; disabling animation makes the backdrop static.

Atmosphere has separate Backdrop and Sound tabs. Aurora drift adds an original 32-second synthesized pad/wind/glass-tone loop, alongside the three ocean loops. All four sounds are bundled, with volume, mute and optional in-game playback. This is imagined aurora-themed ambience, not a field recording of northern lights. The optional authoring script is `scripts/make_aurora_audio.py`.

New profiles start with Aurora. Existing profiles keep their saved ocean palette and sound until a scene is explicitly selected. The old ocean.* audio settings keys remain compatible. Financial tracking, payment queue, lever advance, chat payouts and gameplay HUD rendering remain unchanged.

## New in 1.7.0: Tideline coastal HUD and ocean ambience

Actual Minecraft previews: [Overview](releases/tideline-overview-1.7.png) · [Ocean controls](releases/tideline-ocean-1.7.png) · [Dusk](releases/tideline-dusk-1.7.png) · [Warm paper](releases/tideline-paper-1.7.png) · [HUD](releases/tideline-hud-1.7.png).

Right Shift opens a coastal pixel interface with a sea-glass palette, warm cream text, subtle wave contours and a redesigned HUD stat row. Ocean & style offers Sea glass, Dusk and Warm paper menu palettes. Menu motion can be disabled. Changing a menu palette preserves custom HUD colours; Match HUD colours explicitly applies that palette to both modules without changing position, scale, opacity or Clean/Neon selection.

Ocean ambience has three bundled 24-second looping choices: Shoreline, Open sea and Rainy coast. They are original procedurally synthesized surf textures (authored with deterministic DSP), not field recordings. They play through Minecraft's Ambient/Environment channel, with a separate 0–100% ocean volume. Default: enabled at 18%, in mod menus only. In-game audio is an opt-in toggle. Playback fades in/out, crossfades track changes, stops on disconnect/shutdown, and the page reports Playing/Off/Minecraft-muted state. No downloads or network calls are required for audio. Minecraft Master and Ambient volume still apply.

The bundled files and generation script are included; normal Gradle builds do not need audio-generation dependencies. The optional authoring script is `scripts/make_ocean_audio.py` (numpy, scipy, ffmpeg). Audio attribution is bundled as OCEAN-SOUNDS.md.

Existing /start, /stop, income/loss arithmetic, chat 2×/3× controls, lever queue advance, positions, scale, custom colours and session history remain. No automatic /bal or branded success chat is introduced.

## New in 1.6.0: lever queue advance and consistent controls

Actual Minecraft screenshots: [Queue controls](releases/queue-1.6.png) · [Colour picker](releases/colours-1.6.png) · [Settings](releases/settings-1.6.png).

Right-click a lever to complete the active payment and show the next player in Payment Queue. Both on → off and off → on count. One mouse press completes at most one receipt; holding Use cannot skip multiple players. An empty queue is a no-op. Enable/disable this in Payment Queue → Queue → Lever advances queue (enabled by default). This applies to any lever you use; other blocks and other players' lever changes without your own pending interaction do not advance the queue.

The client waits for the lever's visible powered state to change, for up to three seconds. Unchanged/rejected attempts do not advance. Changing the active receipt manually, disconnecting, changing worlds or disabling the feature cancels a pending attempt. A save failure restores the removed receipt. Lever use never sends a payment or balance command and does not alter session profit.

Settings, shared controls, HUD typography and history now share the quieter overview style. History uses aligned dates and colour-coded results. The colour picker shows Before/After swatches and uses smooth GPU gradients with fewer draw calls than the previous tiled square. Existing saved custom colours, positions and scale are preserved.

## New in 1.5.0: compact HUDs and module navigation

Actual Minecraft screenshots: [Overview](releases/overview-1.5.png) · [Settings](releases/settings-1.5.png) · [Placement editor](releases/placement-1.5.png). Values shown come from synthetic payment messages in an offline test world.

Right Shift opens two separate module cards showing your real session total, duration and queue. Start/End session is available directly on this overview and uses the same local net tracking as `/start` and `/stop`, without requesting `/bal`. Each module has its own fixed navigation rail: Appearance, Colours, Visibility and the module's session/queue pages. Colours still open the visual picker; existing saved colours and positions are preserved.

The gameplay HUDs use quieter surfaces, smaller headers and left-aligned values. Compact shows net profit, duration and hourly rate; Stacked additionally shows the balance. Payment Queue puts the player's name and amount together, with the next player below. The former decorative next arrow is removed. Clean and Neon remain selectable.

Arrange HUDs has a selected outline, live coordinates, centre guides, gentle snapping to margins/centre, one-pixel arrow-key nudges, and a Done button. Dragging and keyboard nudges only move the selected HUD; scale is changed separately in Appearance. All changes are saved locally.

## New in 1.4.3: live net profit

`/start` starts a live net session; `/stop` freezes and saves it. Incoming payments add to the total, and confirmed outgoing payments subtract. Example: receive 3M, pay 2M → +1M, pay another 2M → -1M. Negative totals use the HUD's loss colour, including the hourly rate. Queue thresholds only filter the queue, not the tracker.

Only matching server confirmations change the total: submitting `/pay` or clicking 2×/3× does not subtract until confirmed. Default expense detection supports `You paid Steve $2M`, `You paid $2M to Steve`, `You successfully paid $2M to Steve!`, `You sent $2M to Steve`, and `You lost $3M`. Actual server wording may differ; adjust Tracker → Messages → Expense pattern (one amount capture). No automatic `/bal` is sent. Other losses without a matching chat message cannot be detected immediately; a manual balance reading reconciles changes if an earlier balance is known. Without a prior balance, the first reading establishes a baseline for later reconciliation.

`/sstart` and `/sstop` remain aliases. Session results stay frozen after stopping. Manual balance readings replace the estimated balance without counting recognized payments twice. With no starting balance, history endpoints are accounting values representing the net result, not verified bank balances.

## New in 1.4.0: clearer settings

The settings menu replaces stone-texture buttons with distinct controls: a visibility switch, separate Clean/Neon style choices, illustrated Compact/Stacked/Minimal cards, and labelled size/opacity sliders with percentage readouts. Navigation uses highlighted text tabs; action buttons have secondary and primary styles. Colours uses compact swatches with HEX labels, and the module chooser and colour picker share the same dark controls.

On wide scaled GUI layouts, Appearance also shows a live preview of the selected HUD. Narrow layouts keep all controls usable without a preview. Sliders apply their final value on mouse release, including fast drags. Changes save automatically; Modules returns to the module chooser and Done closes settings. Use Appearance → Move HUD... to reposition a box. Payment/chat behaviour is unchanged: inline 2×/3× controls, no automatic /bal, and no branded success confirmation.

## Return controls inside chat

Server payment messages now keep their original chat position and formatting and append clickable **[2×] [3×]** buttons to that same line. Open normal chat with **T** and click a multiplier. Hovering shows the exact return command. There are no return buttons above the HUD and no P overlay.

For example, `1arp_ paid you $1M` gets buttons that immediately send `/pay 1arp_ 2000000` or `/pay 1arp_ 3000000`. Buttons reference that exact receipt, even if another player is at the front of the HUD queue. A removed receipt or a button from an earlier server session cannot pay another player. Successful clicks do not add a mod-branded chat confirmation; only the server response appears. Local errors remain visible without a mod prefix. The server response determines whether a transfer succeeds.

One return is permitted per queue entry: its request marker is persisted before dispatch and survives restart. Repeated clicks cannot send another return; ambiguous/failed sends are never automatically retried. No live funds are used in tests. Old queue files remain readable, known old default patterns upgrade, and custom patterns remain intact.

## Clean HUD design

The gameplay HUDs use translucent charcoal cards, subtle borders, left-aligned session profit and a timer/rate row. Stacked layout also shows the balance. Payment Queue includes the active player head, name, amount, waiting count and next-player summary. Clean mode has no accent outline or glow; Neon remains available. Each module has Appearance → Clean preset for the mint/lavender/cyan palette without overwriting saved custom colours automatically. Positions, scaling, layouts and display toggles remain supported. Minecraft’s native pixel font is used rather than the mockup’s smooth font. Next Player is available in the module menu.

## Payment refresh fix

Automatic `/bal` commands are removed in 1.4.0. Receiving payments, clicking 2×/3×, and ticking a running session never request a balance. Old `balance.poll=true` settings are ignored and retired. Incoming payments update active sessions immediately in 1.4.2. In balance-based sessions, manual balance readings replace the estimated balance; in `/start` sessions, later readings reconcile any unreported changes against the last known balance.

## Module controls

Right Shift now opens two module cards: **Tracker** and **Payment Queue**. Open either card to configure that module on its own screen. Detection is scoped to the selected module, and session history belongs to Tracker.

**Colours** displays actual coloured swatches. Click one for a saturation/brightness square, a rainbow hue strip, preset colour tiles, a live swatch and optional HEX input. Changes apply immediately and save.

Use **Move this HUD** in either module, or **Move HUD boxes** on the module chooser. Click and drag the box itself; Escape saves its new position. Appearance → Move HUD... also provides numeric X/Y coordinates and reset controls. Dragging now sets the screen drag state correctly.

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

- `fabric/build/libs/donut-tracker-fabric-1.21.11-1.8.0.jar` — installable mod.
- `build/libs/donut-tracker-1.8.0.jar` — executable desktop companion, not a Minecraft mod.

For only the companion and core verification (no Minecraft dependencies): `./gradlew build`.

`coreTest` runs an assertion-based verification runner with formatting, supplied chat examples, profit/rate/timer calculations, losses, FIFO queue behaviour, filtering, and design/history persistence checks. There is no JUnit suite; Gradle's `test` tasks report `NO-SOURCE` on a clean build.

## Run

```sh
# Minecraft development client
./gradlew -PwithFabric :fabric:runClient

# Desktop companion (requires a graphical desktop)
java -jar build/libs/donut-tracker-1.8.0.jar

# Render the actual companion settings screen without a display
java -jar build/libs/donut-tracker-1.8.0.jar --render settings-preview.png
```

The companion has manual balance/payment controls and a parser test panel. It does **not** connect to Minecraft or read a running game's chat. Its settings/history live separately in `~/.donut-tracker`, or a directory supplied with `--config DIRECTORY`.

## Cloud environment

The task is already isolated; use `/workspace/java-` directly and do not create a worktree.

```sh
bash .cloud/install.sh       # repeatable setup, build and verification
source .cloud/env.sh         # initialize each fresh shell
```

Cloud setup installs a checksum-verified Temurin JDK under `/workspace/toolchains`, configures Gradle for the existing HTTPS proxy and its platform CA, and keeps TLS verification enabled. It does not install system packages or require root. Cached dependencies and tools are retained in the published snapshot; running processes must be restarted. A graphical Minecraft client additionally needs a display or virtual framebuffer.
