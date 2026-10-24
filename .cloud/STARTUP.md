Use the existing isolated checkout /workspace/java-. Do not create a worktree unless requested.

Initialize each shell with `source .cloud/env.sh`. Build and verify with `./gradlew -PwithFabric build :fabric:build`. Refresh prerequisites with `bash .cloud/install.sh`.

Minecraft artifact: fabric/build/libs/donut-tracker-fabric-1.21.11-1.2.0.jar.
Desktop companion: build/libs/donut-tracker-1.2.0.jar.

For a graphical development client, use `./gradlew -PwithFabric :fabric:runClient`. In this snapshot the framebuffer is available at /workspace/toolchains/xvfb/usr/bin/Xvfb. Start it on an unused display with `Xvfb :99 -screen 0 1280x720x24 -nolisten tcp`, export DISPLAY=:99 and start the client. Restart required processes after snapshot restoration, and stop only processes you started. Confirm rendering and the Donut Tracker initialized log, not just a PID.

Right Shift opens independent Tracker and Payment Queue modules. Check their settings separately. Colours opens visible swatches and an HSV picker. Move this HUD lets you drag the selected box; verify saved X/Y changes without changing scale. Escape saves.

Core verification prints 63 passing assertions. The desktop companion can render headlessly with `java -jar build/libs/donut-tracker-1.2.0.jar --render /workspace/setup/donut-ui.png`. No backend service or API key is needed. Live DonutSMP testing needs an authenticated game session. Preserve user files and TLS verification; never print credentials.

The prior environment draft is stale after publication. These updated setup instructions are preserved here and in install.sh for transfer/reconciliation in a new setup chat if the environment configuration needs refreshing.
