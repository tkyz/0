umask 0022

chmod    go-rwx "${HOME}"        &> '/dev/null' || true
chmod -R go-rwx "${HOME}/.gnupg" &> '/dev/null' || true
chmod -R go-rwx "${HOME}/.ssh"   &> '/dev/null' || true

# path
if true; then
  PATH="${HOME}/sbin:${PATH}"
  PATH="${HOME}/bin:${PATH}"
  PATH="${HOME}/bin/wrap:${PATH}"
  PATH="${HOME}/.local/bin:${PATH}"
fi

# lib
if false; then
  export LD_LIBRARY_PATH
  LD_LIBRARY_PATH="/usr/local/lib:${LD_LIBRARY_PATH}"
  LD_LIBRARY_PATH="${HOME}/.local/lib:${LD_LIBRARY_PATH}"
fi

# gpg
if true; then
  export GPG_TTY="$(tty)"
fi

# git
if true; then
  export GIT_DISCOVERY_ACROSS_FILESYSTEM=1
fi

# java
if true; then

  export JAVA_HOME="${HOME}/opt/net.java.jdk"
  PATH="${JAVA_HOME}/bin:${PATH}"

  : << 'DISABLED'
export CLASSPATH='.:./*'
while read item; do
  CLASSPATH="${item}:${CLASSPATH}"
done < <(find "${HOME}/.m2/repository/0" -mindepth 3 -maxdepth 3 -type f -name '*-latest.jar')

PATH="${HOME}/opt/org.gradle/bin:${PATH}"
PATH="${HOME}/opt/org.apache.ant/bin:${PATH}"
PATH="${HOME}/opt/org.apache.maven/bin:${PATH}"
DISABLED

fi

# bash
if true; then
  test -n "${BASH_VERSION}" && test -f "${HOME}/.bashrc" && source "${HOME}/.bashrc"
fi

# venv
if true; then
  test -f "${HOME}/.venv/bin/activate" && source "${HOME}/.venv/bin/activate"
fi

test 0 == "$(id -u)" && mesg n &> '/dev/null' || true
