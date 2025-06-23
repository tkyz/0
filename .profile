umask 0022

# bin
test -d "${HOME}/.local/bin" && PATH="${HOME}/.local/bin:${PATH}"
test -d "${HOME}/bin"        && PATH="${HOME}/bin:${PATH}"

test -n "${BASH_VERSION}" && test -f "${HOME}/.bashrc" && source "${HOME}/.bashrc"

test 0 == "$(id -u)" && mesg n 2> /dev/null || true

#----------------------------------------------------------------
# ...

# python
test -f "${HOME}/.venv/bin/activate" && source "${HOME}/.venv/bin/activate"

# java
if [[ -d "${HOME}/opt/net.java.jdk" ]]; then

  export JAVA_HOME="${HOME}/opt/net.java.jdk"
  export CLASSPATH='.:./*'

  test -d "${HOME}/opt/org.apache.ant"   && PATH="${HOME}/opt/org.apache.ant/bin:${PATH}"
  test -d "${HOME}/opt/org.apache.maven" && PATH="${HOME}/opt/org.apache.maven/bin:${PATH}"
  PATH="${JAVA_HOME}/bin:${PATH}"

fi

# sbin
test -d "${HOME}/sbin" && PATH="${HOME}/sbin:${PATH}"
