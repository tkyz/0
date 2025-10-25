# trap
if [[ -t 0 ]]; then
  trap '_traperr="${?}"; printf "\e[91m%s\e[0m\n" "[$(date "+%F %T %z")] ${$} ${SHLVL} ${USER:-}@$(hostname -f):${PWD} ${BASH_COMMAND}"; unset _traperr' ERR
fi

# lang
case "${TERM}" in
  linux) LANG=C;;
      *) LANG=ja_JP.UTF-8;;
esac

export GPG_TTY="$(tty)"

if [[ -v DISPLAY ]]; then
  export GTK_IM_MODULE=ibus
  export QT_IM_MODULE=ibus
  export XMODIFIRES=ibus
fi

if [[ -t 0 ]]; then
  stty start undef
  stty stop  undef
fi

# PS1
if [[ -t 0 ]]; then
  function _tmp() {

    local -r txtred='\[\e[31m\]'
    local -r txtgrn='\[\e[32m\]'
    local -r txtylw='\[\e[33m\]'
    local -r txtblu='\[\e[34m\]'
    local -r txtpur='\[\e[35m\]'
    local -r txtcyn='\[\e[36m\]'
    local -r txtwht='\[\e[37m\]'
    local -r txtrst='\[\e[0m\]'
    local -r txtgrn_blink='\[\e[5;32m\]'
    local -r txtylw_blink='\[\e[5;33m\]'

    local scheme=''
    if [[ -v SSH_TTY ]]; then
      scheme+="${txtylw_blink}ssh${txtrst}:"
    fi
    if [[ -d '/mnt/c' ]] && type wslpath &> '/dev/null'; then
      scheme+="${txtgrn_blink}wsl${txtrst}:"
    fi
#   if is_k8s; then
#     scheme+="${txtgrn_blink}k8s${txtrst}:"
#   fi
    if [[ 'podman' == "${container:-}" || -f '/run/.containerenv' || -f '/.dockerenv' || "$(cat '/proc/1/cgroup')" == '0::/' || "$(cat '/proc/1/cgroup')" == '0::/system.slice/containerd.service' ]]; then
      scheme+="${txtgrn_blink}container${txtrst}:"
    fi
    scheme+="tty:$(tty | sed -e 's,^/dev/,,' -e 's,/,,')"

    local fqdn="$(hostname -f 2> '/dev/null')"
    if [[ -z "${fqdn}" ]]; then
      fqdn='\h'
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

    PS1="${scheme}://${user}@${fqdn}:${path} ${sign} "
#   PS2=
#   PS3=
#   PS4=

  }; _tmp; unset -f _tmp
fi

# history
if true; then

  touch "${HOME}/.bash_history"
# sort --unique "${HOME}/.bash_history" --output "${HOME}/.bash_history"

  HISTSIZE=
  HISTFILESIZE=
  HISTCONTROL='ignoredups:ignorespace'
# HISTIGNORE='history:history *'
# HISTTIMEFORMAT='%F %T '

  PROMPT_COMMAND='history -a; history -c; history -r'
  shopt -u histappend

fi

# alias
if true; then

  # wrap
  alias ls='ls -1v --color=auto --show-control-chars --full-time '
  alias cp='cp -v '
  alias mv='mv -v '
  alias rm='rm -v '
  alias lsblk='lsblk --fs --paths '
  alias grep=' grep  --color=auto --line-buffered '
  alias egrep='egrep --color=auto --line-buffered '
  alias fgrep='fgrep --color=auto --line-buffered '
  alias diff=' diff  --color=auto '
  alias curl=' curl -fsSL '

  alias ..='cd ..'
  alias la='ls -a '
  alias ll='la -lF '
  alias vi='vim '
  alias gs='git status '

  alias timestamp='date --utc "+%Y%m%d_%H%M%S_%N" '
  alias clipcopy=' xsel --clipboard --input '
  alias clippaste='xsel --clipboard --output '
  alias relogin='exec "${SHELL:-bash}" -l '

fi

# completion
if true; then
  type podman  &> '/dev/null' && source <(podman  completion bash)
  type kubectl &> '/dev/null' && source <(kubectl completion bash)
  type kind    &> '/dev/null' && source <(kind    completion bash)
  type myna    &> '/dev/null' && source <(myna    completion bash)
fi
