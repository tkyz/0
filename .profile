umask 0022

# bin
if true; then
  test -d "${HOME}/bin"                          && PATH="${HOME}/bin:${PATH}"
  test -d "${HOME}/.local/bin"                   && PATH="${HOME}/.local/bin:${PATH}"
fi

# opt
if true; then
  test -d "${HOME}/opt/net.java.jdk"             && export JAVA_HOME="${HOME}/opt/net.java.jdk"
  test -f "${JAVA_HOME}/bin/java"                && export CLASSPATH='.:./*'
  test -f "${JAVA_HOME}/bin/java"                && PATH="${JAVA_HOME}/bin:${PATH}"
  test -f "${HOME}/opt/org.apache.ant/bin/ant"   && PATH="${HOME}/opt/org.apache.ant/bin:${PATH}"
  test -f "${HOME}/opt/org.apache.maven/bin/mvn" && PATH="${HOME}/opt/org.apache.maven/bin:${PATH}"
  test -f "${JAVA_HOME}/.local/bin"              && PATH="${HOME}/.local/bin:${PATH}"
fi

test -n "${BASH_VERSION}" && test -f "${HOME}/.bashrc" && source "${HOME}/.bashrc"

test 0 == "$(id -u)" && mesg n 2> /dev/null || true
