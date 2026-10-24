Use the existing isolated checkout /workspace/java-. Do not create a worktree unless requested.

Initialize each shell with `source .cloud/env.sh`. Build and verify with `./gradlew -PwithFabric build :fabric:build`. Refresh prerequisites with `bash .cloud/install.sh`.

Minecraft artifact: fabric/build/libs/donut-tracker-fabric-1.21.11-1.6.0.jar.
Desktop companion: build/libs/donut-tracker-1.6.0.jar.

For a graphical development client, use `./gradlew -PwithFabric :fabric:runClient`. In this snapshot the framebuffer is available at /workspace/toolchains/xvfb/usr/bin/Xvfb. Start it on an unused display with `Xvfb :99 -screen 0 1280x720x24 -nolisten tcp`, export DISPLAY=:99 and start the client. Restart required processes after snapshot restoration, and stop only processes you started. Confirm rendering and the Donut Tracker initialized log, not just a PID.

Right Shift opens independent Tracker and Payment Queue modules. Check their settings separately. Colours opens visible swatches and an HSV picker. Design → Move HUD... lets you drag the selected box; verify saved X/Y changes without changing scale. Escape saves.

Core verification prints 76 passing assertions. The desktop companion can render headlessly with `java -jar build/libs/donut-tracker-1.6.0.jar --render /workspace/setup/donut-ui.png`. No backend service or API key is needed. Live DonutSMP testing needs an authenticated game session. Preserve user files and TLS verification; never print credentials.

The prior environment draft is stale after publication. These updated setup instructions are preserved here and in install.sh for transfer/reconciliation in a new setup chat if the environment configuration needs refreshing.

For chat return controls, simulate a server payment with tellraw only in an offline local world. Open chat with T, click [2×] or [3×] on the original payment line, and verify the exact native /pay command, persisted request marker and duplicate-click feedback. No live funds may be used for tests. Buttons are bound to a receipt in the current connection, not the front HUD player.

Version 1.4.0 sends no automatic /bal after incoming/returned payments or on a timer, including with legacy balance.poll=true. Test with a running manual session in an offline world, simulate an incoming payment, click a chat return and verify no balance command is dispatched. Only explicitly labelled manual /bal controls request a balance.

Successful return clicks add no mod-branded chat confirmation. Native server responses remain visible; local errors use plain text without the Donut Tracker prefix.

Version 1.4.0 uses custom switches, direct style choices, illustrated layout cards and labelled sliders. Validate clicks and exact release values in a narrow GUI, then resize to a wide scaled GUI and inspect the live preview. Done saves and closes to the game; Back to modules returns to the chooser. Existing chat returns, no-auto-/bal behaviour and quiet success chat remain unchanged.

Version 1.5.0 adds module overview controls, fixed per-module navigation, compact HUDs and a placement editor with margin/centre snapping and one-pixel arrow-key nudges. Verify both HUDs move independently without changing scale. Session controls use the same local net tracking as /start and /stop. Confirm +3M − 2M − 2M = −1M with synthetic system messages in an offline test world; no live transfers are needed.

Version 1.6.0: Use an offline creative world with three synthetic payment messages and a real lever. A right-click that visibly toggles it must remove exactly one active receipt, persist the remaining queue and show the next player. Verify both directions, empty queue, and the Payment Queue → Queue → Lever advances queue toggle. This never dispatches pay/bal commands. Core verification covers expiry, duplicate observations, changed queue identity, cancellation and persistence rollback.
