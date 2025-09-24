umask 0022

# bin
test -d "${HOME}/.local/bin" && PATH="${HOME}/.local/bin:${PATH}"
test -d "${HOME}/bin"        && PATH="${HOME}/bin:${PATH}"

test -n "${BASH_VERSION}" && test -f "${HOME}/.bashrc" && source "${HOME}/.bashrc"

test 0 == "$(id -u)" && mesg n &> /dev/null || true

#----------------------------------------------------------------
# ...

# perm
chmod    go-rwx "${HOME}"        &> /dev/null || true
chmod -R go-rwx "${HOME}/.gnupg" &> /dev/null || true
chmod -R go-rwx "${HOME}/.ssh"   &> /dev/null || true

# git
export GIT_DISCOVERY_ACROSS_FILESYSTEM=1

# java
if [[ -d "${HOME}/opt/net.java.jdk" ]]; then

# test -d "${HOME}/opt/org.apache.ant"   && PATH="${HOME}/opt/org.apache.ant/bin:${PATH}"
# test -d "${HOME}/opt/org.apache.maven" && PATH="${HOME}/opt/org.apache.maven/bin:${PATH}"
# test -d "${HOME}/opt/org.gradle"       && PATH="${HOME}/opt/org.gradle/bin:${PATH}"
  true                                   && PATH="${JAVA_HOME}/bin:${PATH}"

  export JAVA_HOME="${HOME}/opt/net.java.jdk"

  export CLASSPATH='.:./*'
  test -d "${HOME}/.m2/repository/0" && while read item; do
    CLASSPATH="${item}:${CLASSPATH}"
  done < <(find "${HOME}/.m2/repository/0" -mindepth 3 -maxdepth 3 -type f -name '*-latest.jar')

fi

# python
test -f "${HOME}/.venv/bin/activate" && source "${HOME}/.venv/bin/activate"
