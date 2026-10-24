#!/usr/bin/env bash
set -euo pipefail
cd /workspace/java-
mkdir -p /workspace/toolchains /workspace/setup /workspace/.gradle /workspace/.cache
export JAVA_HOME=/workspace/toolchains/jdk-21.0.13+9
export GRADLE_USER_HOME=/workspace/.gradle
export XDG_CACHE_HOME=/workspace/.cache
if [ ! -x "$JAVA_HOME/bin/javac" ]; then
  curl -fL --retry 2 -o /workspace/setup/jdk.tar.gz 'https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.13%2B9/OpenJDK21U-jdk_x64_linux_hotspot_21.0.13_9.tar.gz'
  printf '%s  %s\n' d7f5d5cf865a2902b70ff549b97586a7d3f234e016f041ab447ea740c7d66d1c /workspace/setup/jdk.tar.gz | sha256sum -c -
  tar -xzf /workspace/setup/jdk.tar.gz -C /workspace/toolchains
fi
export PATH="$JAVA_HOME/bin:$PATH"
# Trust the platform-issued HTTPS proxy CA without disabling TLS validation.
if [ -f /usr/local/share/ca-certificates/environment-proxy-ca.crt ]; then
  cp "$JAVA_HOME/lib/security/cacerts" /workspace/toolchains/proxy-cacerts
  keytool -importcert -noprompt -trustcacerts -alias cloud-environment-proxy \
    -file /usr/local/share/ca-certificates/environment-proxy-ca.crt \
    -keystore /workspace/toolchains/proxy-cacerts -storepass changeit
fi
# Gradle/Java do not automatically consume HTTPS_PROXY. Keep credentials out of files.
python3 - <<'PY'
import os, pathlib, urllib.parse
path = pathlib.Path('/workspace/.gradle/gradle.properties')
managed = {}
proxy = os.environ.get('HTTPS_PROXY') or os.environ.get('https_proxy')
if proxy:
    p = urllib.parse.urlsplit(proxy)
    if p.username or p.password:
        raise SystemExit('Proxy contains credentials: use the platform proxy binding rather than saving credentials.')
    if not p.hostname:
        raise SystemExit('Invalid HTTPS_PROXY hostname')
    for scheme in ('http', 'https'):
        managed[f'systemProp.{scheme}.proxyHost'] = p.hostname
        managed[f'systemProp.{scheme}.proxyPort'] = str(p.port or 80)
if pathlib.Path('/workspace/toolchains/proxy-cacerts').exists():
    managed['systemProp.javax.net.ssl.trustStore'] = '/workspace/toolchains/proxy-cacerts'
lines = path.read_text().splitlines() if path.exists() else []
lines = [line for line in lines if line.partition('=')[0].strip() not in managed]
path.write_text('\n'.join(lines + [f'{key}={value}' for key,value in managed.items()]) + '\n')
PY
java -version
javac -version
./gradlew -PwithFabric build :fabric:build
java -jar fabric/build/libs/donut-tracker-fabric-1.21.11-1.8.0.jar
java -jar build/libs/donut-tracker-1.8.0.jar --render /workspace/setup/donut-ui.png
