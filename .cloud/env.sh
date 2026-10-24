# Source this in each new cloud shell. Use the existing isolated checkout.
export JAVA_HOME=/workspace/toolchains/jdk-21.0.13+9
export GRADLE_USER_HOME=/workspace/.gradle
export XDG_CACHE_HOME=/workspace/.cache
export PATH="$JAVA_HOME/bin:$PATH"
