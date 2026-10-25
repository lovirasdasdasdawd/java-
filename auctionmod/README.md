## Current distribution: integrated into Donut Tracker 1.13+
Auction features are now compiled directly into the single Donut Tracker + Auctions JAR, under the donuttracker mod ID and its one client entrypoint. Open Right Shift → Auctions or press O. Remove the former separate auctionmod JAR. Build the combined artifact from the repository root with `./gradlew -PwithFabric build :fabric:build`. This directory is the shared auction implementation; the standalone 1.0 project documentation below describes the earlier release.

# Auction Mod

Complete standalone client-only Fabric project for Minecraft Java **1.21.11**, authored in metadata as **claude**, mod ID **auctionmod**. Java **21**. Uses **official Mojang mappings** (GuiGraphics, Component, KeyMapping), **Fabric Loom 1.14.10**, **Fabric Loader 0.18.4**, **Fabric API 0.141.6+1.21.11**, and Gradle **9.2.1**. Exact versions are in gradle.properties.

## Build and install

1. Install Java 21.
2. In this directory run `./gradlew build` (Windows: `gradlew.bat build`). This also runs the auction correctness checks.
3. Install `build/libs/auctionmod-1.21.11-1.0.0.jar` in your Minecraft instance's `mods` folder alongside Fabric API. Use Fabric Loader and Minecraft 1.21.11. The `-sources.jar` is source code, not the installable mod.
4. Join a world/server and press **O**. The key can be changed in **Options → Controls → Key Binds → Auction Mod → Open auction setup**.

## Use

Search by translated item name or registry ID, scroll the live-filtered icon grid, and click an item. The catalog contains all registered non-air inventory items, including block items; blocks that have no inventory item cannot provide an item icon. Hover for the normal Minecraft tooltip. Selected items have a cyan frame.

After selecting, enter Minimum (e.g. `1000`, `1k`, `1.5M`, `2,000`) and Time (seconds or `mm:ss`, e.g. `90` or `01:30`). Minimum must be positive and time is limited to 1 second–24 hours. **Done** saves the settings, starts the auction and closes the menu. An item image is selected; inventory items are never consumed or transferred.

The HUD shows the selected item, top individual qualifying payment, countdown and final winner/No bids. Payments below the minimum and equal/lower payments never replace the top bid. Bids are individual payments, not cumulative per player. **O** while running reopens setup with **Stop auction**; cancelling never announces a winner. Disconnecting clears the auction to avoid bids crossing servers. Closing the screen alone keeps an auction running.

The optional Glass button applies tinted transparency to setup/HUD panels; it is not a blur shader. It is saved with other settings. HUD position can be changed using hudX/hudY in the JSON (restart after editing). If using Donut Tracker too, move its HUDs to avoid overlap with the auction HUD at top-left.

## Payment format and config

On first launch, `config/auctionmod.json` is created in the Minecraft instance. An identical complete example is included at `config/auctionmod.json` in this source project. The default regex is the clearly named **PaymentParser.DEFAULT_PAYMENT_REGEX** constant at the top of PaymentParser.java and is written to JSON. Capture group 1 is the IGN, group 2 the amount. Supports case-insensitive K/M/B/T, decimal amounts, correctly grouped commas, formatting codes, optional trailing punctuation/currency symbol and spaces.

The mod listens to Fabric ClientReceiveMessageEvents **GAME and CHAT**, plus a non-blocking **ALLOW_GAME** observer of the original notice before other mods decorate it. This preserves payment detection when Donut Tracker adds inline return-payment buttons. Duplicate observations are ignored by the equal-bid rule. CHAT uses the server-supplied displayed decoration, so ordinary prefixed player chat cannot impersonate an unprefixed payment line. For a different server format, edit paymentRegex in the instance JSON and restart. Keep it anchored to the exact payment notice, not arbitrary chat text. A malformed config is preserved and reported; repair it and restart before saving settings. Minimum, duration, selected item, glass preference, HUD position and local winner notification preference are persisted.

## Deadline guarantee and local output

AuctionState uses one fixed `System.currentTimeMillis()` end timestamp. Every state read and bid refreshes expiration. Bids recheck the deadline immediately before changing the top bid; a message processed at or after the deadline is ignored even when client ticks lag. At zero, the state is ENDED and the winner is frozen. No tick counter drives the timer. Local winner notification is consumed once. No code sends chat, `/pay`, `/bal`, or any other command to the server.

## Files

```text
auctionmod/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── gradlew
├── gradlew.bat
├── gradle/wrapper/gradle-wrapper.jar
├── gradle/wrapper/gradle-wrapper.properties
├── config/auctionmod.json
├── src/main/resources/fabric.mod.json
├── src/main/resources/assets/auctionmod/lang/en_us.json
├── src/main/java/dev/auctionmod/Amounts.java
├── src/main/java/dev/auctionmod/PaymentParser.java
├── src/main/java/dev/auctionmod/AuctionState.java
├── src/main/java/dev/auctionmod/AuctionConfig.java
├── src/main/java/dev/auctionmod/AuctionModClient.java
├── src/main/java/dev/auctionmod/AuctionScreen.java
├── src/main/java/dev/auctionmod/AuctionHud.java
├── src/main/java/dev/auctionmod/AuctionUi.java
└── src/verification/java/dev/auctionmod/AuctionTest.java
```

All implementation files are complete. The build checks exact-deadline and lagged rejection, minimum/higher/equal bid rules, formatting, suffixes, config reload, cancellation, no-bid endings and one-shot local result notification. The GUI follows the written dark/rounded/cyan/yellow design reference; no auction screenshot attachments were available in the request.

## Validation
The complete project builds successfully with the pinned versions above and passes 30 auction assertions. It was also loaded in actual Minecraft 1.21.11 alongside Donut Tracker. The native item catalog contained 1504 non-air registered items; live search, selection, tooltips, settings, cancellation, no-bid endings and a winning payment were exercised in an offline creative world. A $2M payment decorated by the tracker was accepted through the original-message observer, and a post-deadline $10M message did not change the frozen winner. Test notices were generated only in that local world; no live DonutSMP auction or transfers were performed.
