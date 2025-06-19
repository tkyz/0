umask 0022

# java
if [[ -d "${HOME}/opt/net.java.jdk" ]]; then

  export JAVA_HOME="${HOME}/opt/net.java.jdk"
  export CLASSPATH='.:./*'

  test -d "${HOME}/opt/org.apache.ant"   && PATH="${HOME}/opt/org.apache.ant/bin:${PATH}"
  test -d "${HOME}/opt/org.apache.maven" && PATH="${HOME}/opt/org.apache.maven/bin:${PATH}"
  PATH="${JAVA_HOME}/bin:${PATH}"

fi

# bin
if true; then
  test -d "${HOME}/bin"                  && PATH="${HOME}/bin:${PATH}"
  test -d "${HOME}/.local/bin"           && PATH="${HOME}/.local/bin:${PATH}"
fi

test -n "${BASH_VERSION}" && test -f "${HOME}/.bashrc" && source "${HOME}/.bashrc"

test 0 == "$(id -u)" && mesg n 2> /dev/null || true
