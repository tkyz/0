umask 0022

chmod    go-rwx "${HOME}"        &> '/dev/null' || true
chmod -R go-rwx "${HOME}/.gnupg" &> '/dev/null' || true
chmod -R go-rwx "${HOME}/.ssh"   &> '/dev/null' || true

# java
export JAVA_HOME="${HOME}/opt/net.java.jdk"
export CLASSPATH='.:./*'
test -d "${HOME}/.m2/repository/0" && while read item; do
  CLASSPATH="${item}:${CLASSPATH}"
done < <(find "${HOME}/.m2/repository/0" -mindepth 3 -maxdepth 3 -type f -name '*-latest.jar')

# path
#PATH="${HOME}/opt/org.gradle/bin:${PATH}"
PATH="${HOME}/opt/org.apache.ant/bin:${PATH}"
PATH="${HOME}/opt/org.apache.maven/bin:${PATH}"
PATH="${JAVA_HOME}/bin:${PATH}"
PATH="${HOME}/bin:${PATH}"
PATH="${HOME}/.local/bin:${PATH}"

# lib
LD_LIBRARY_PATH="/usr/local/lib:${LD_LIBRARY_PATH}"
LD_LIBRARY_PATH="${HOME}/.local/lib:${LD_LIBRARY_PATH}"
export LD_LIBRARY_PATH

# git
export GIT_DISCOVERY_ACROSS_FILESYSTEM=1

test -n "${BASH_VERSION}" && test -f "${HOME}/.bashrc" && source "${HOME}/.bashrc"

# venv
test -f "${HOME}/.venv/bin/activate" && source "${HOME}/.venv/bin/activate"

test 0 == "$(id -u)" && mesg n &> '/dev/null' || true
