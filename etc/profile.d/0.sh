#!/bin/sh

umask 0022

#----------------------------------------------------------------
# trap
if [[ -t 0 ]]; then
  trap '_traperr="${?}"; printf "\e[91m%s\e[0m\n" "[$(date "+%F %T %z")] ${$} ${SHLVL} ${USER:-}@$(hostname -f):${PWD} ${BASH_COMMAND}"; unset _traperr' ERR
fi

#----------------------------------------------------------------
# export

PATH="${HOME}/.bin:/.bin:${PATH}"

export LANG=ja_JP.UTF-8

# gpg
export GPG_TTY="$(tty)"

# git
export GIT_DISCOVERY_ACROSS_FILESYSTEM=1

# wsl in docker
if is_wsl && type docker &> /dev/null; then
  export DOCKER_HOST='tcp://localhost:2375'
fi

#----------------------------------------------------------------
# alias

alias ls='ls --color=auto --show-control-chars --full-time '
alias grep=' grep  --color=auto --line-buffered '
alias egrep='egrep --color=auto --line-buffered '
alias fgrep='fgrep --color=auto --line-buffered '
alias curl='curl -fsSL '
alias cp='cp -iv '
alias mv='mv -iv '
alias rm='rm -iv '
alias tree='tree -C '
alias less='less -rn '
alias diff='diff -sq '
alias w='w -ui '
alias who='who -aH '
alias df='df -h '
alias od='od -Ax -tx1z '

alias ..='cd .. '
alias la='ls -a '
alias ll='la -lFhi '
alias gs='git status '
alias vi='vim '
alias timestamp='date --utc "+%Y%m%d_%H%M%S_%N"'

alias sstatus='  systemctl status '
alias sstart='   systemctl start '
alias sstop='    systemctl stop '
alias srestart=' systemctl restart '
alias senable='  systemctl enable '
alias sdisable=' systemctl disable '
alias sreenable='systemctl reenable '

alias reboot='shutdown now -r '
alias relogin='exec "${SHELL:-bash}" -l '

#----------------------------------------------------------------
# functions

# TODO: gpg-agent
function ssh-agent() {

  # 使いまわさず、作り直す
  ps -U "$(id -u)" -u "$(id -u)" -o pid,comm | grep ssh-agent | awk -F ' ' '{print $1}' | xargs --no-run-if-empty kill -9
  eval "$(/usr/bin/ssh-agent)"

  ssh-add ${HOME}/.ssh/id_rsa   || true
  ssh-add ${HOME}/.ssh/id_rsa/* || true

}
export -f ssh-agent

#----------------------------------------------------------------
# prompt

if [[ -t 0 ]]; then

  # 端末オプション
  stty start undef
  stty stop  undef

  # local
  function _tmp() {

    # color
    local txtred='\[\e[31m\]'
    local txtgrn='\[\e[32m\]'
    local txtylw='\[\e[33m\]'
    local txtblu='\[\e[34m\]'
    local txtpur='\[\e[35m\]'
    local txtcyn='\[\e[36m\]'
    local txtwht='\[\e[37m\]'
    local txtrst='\[\e[0m\]'
    local txtgrn_blink='\[\e[5;32m\]'
    local txtylw_blink='\[\e[5;33m\]'

    local cgroup="$(tail -n 1 /proc/1/cgroup)"

    # TODO: IPの範囲で色変えたい
    local scheme=''
    if is_ssh; then
      scheme+="${txtylw_blink}ssh${txtrst}:"
    fi
    if is_wsl; then
      scheme+="${txtgrn_blink}wsl${txtrst}:"
    fi
    if is_k8s; then
      scheme+="${txtgrn_blink}k8s${txtrst}:"
    fi
    if is_container;]; then
      scheme+="${txtgrn_blink}container${txtrst}:"
    fi
    scheme+="tty:$(tty | sed -e 's,^/dev/,,' -e 's,/,,')"

    local host="$(hostname -f 2> /dev/null)"
    if [[ -z "${host}" ]]; then
      host='\h'
    fi

    local user='\u' # "${USER}"
    local path='\w' # "${PWD}"
    local sign=''

    if [[ 0 == "$(id -u)" ]]; then
      sign='#'
      user="${txtred}${user}${txtrst}"
    else
      sign='$'
      user="${txtcyn}${user}${txtrst}"
    fi

    PS1="${scheme}://${user}@${host}:${path} ${sign} "
#   PS2=
#   PS3=
#   PS4=

  }; _tmp; unset -f _tmp

fi

# history
if [[ -t 0 ]]; then

  touch "${HOME}/.bash_history"

  export HISTSIZE=131072
  export HISTFILESIZE="${HISTSIZE}"
  export HISTCONTROL=ignoreboth
  export HISTIGNORE='pwd:top:htop:history:history *:bg:bg *:fg:fg *:cd *:ls:ls *:la:la *:ll:ll *'
  export HISTTIMEFORMAT='%F %T '

  export PROMPT_COMMAND='history -a; history -c; history -r'

  shopt -u histappend

fi

# authorized_keys
function _tmp() {

  mkdir -p "${HOME}/.ssh"
  touch    "${HOME}/.ssh/authorized_keys"
: <<'COMMENT'
  find /home -mindepth 2 -maxdepth 2 -type f -name rsa 2> /dev/null | while read item; do
    cat "${item}" | ssh-keygen -f /dev/stdin -i -m pkcs8 >> "${HOME}/.ssh/authorized_keys"
  done

  sort -u "${HOME}/.ssh/authorized_keys"     > "${HOME}/.ssh/authorized_keys.swp"
  mv   -f "${HOME}/.ssh/authorized_keys.swp"   "${HOME}/.ssh/authorized_keys" > /dev/null
COMMENT
}; _tmp; unset -f _tmp

# chmod
function _tmp() {
  if [ -d "${HOME}/.gnupg" ]; then chmod -R go-rwx "${HOME}/.gnupg"; fi
  if [ -d "${HOME}/.ssh"   ]; then chmod -R go-rwx "${HOME}/.ssh"; fi
}; _tmp; unset -f _tmp

# TODO: gpg-agent
if false; then

  unset SSH_AGENT_PID
  if [ "${gnupg_SSH_AUTH_SOCK_by:-0}" -ne "$$" ]; then
    export SSH_AUTH_SOCK="$(gpgconf --list-dirs agent-ssh-socket)"
  fi

  # TODO: ssh-agentが勝手に起動する
  if false; then

    if [ -f '/etc/xdg/autostart/gnome-keyring-ssh.desktop' ]; then
      mkdir -p "${HOME}/.config/autostart"
      \cp -f '/etc/xdg/autostart/gnome-keyring-ssh.desktop' "${HOME}/.config/autostart/"
      echo 'Hidden=true' >> "${HOME}/.config/autostart/gnome-keyring-ssh.desktop"
    fi

  fi

fi

# skel
find "${HOME}/etc/skel" | while read item; do

  if [[ -d "${item}" ]]; then
    mkdir -p "${item}"
    continue
  fi
  item="$(echo "${item}" | sed "s#^${HOME}/etc/skel/##")"

  if [[ '.gnupg/gpg.conf' == "${item}" ]]; then
    cp -n "${HOME}/etc/skel/${item}" "${HOME}/${item}"
    continue
  fi

  if [[ ! -L "${HOME}/${item}" ]]; then
    rm -f "${HOME}/${item}" &> /dev/null
  fi

  # TODO: サブディレクトリ配下の相対パス
# ln -fs "./etc/skel/${item}" "${HOME}/${item}"
  ln -fs "${HOME}/etc/skel/${item}" "${HOME}/${item}"

done
unset item
