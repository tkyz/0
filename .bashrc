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
    if [[ -d '/mnt/c' ]] && type wslpath &> /dev/null; then
      scheme+="${txtgrn_blink}wsl${txtrst}:"
    fi
#   if is_k8s; then
#     scheme+="${txtgrn_blink}k8s${txtrst}:"
#   fi
    if [[ 'podman' == "${container:-}" || -f '/run/.containerenv' || -f '/.dockerenv' || "$(cat '/proc/1/cgroup')" == '0::/' || "$(cat '/proc/1/cgroup')" == '0::/system.slice/containerd.service' ]]; then
      scheme+="${txtgrn_blink}container${txtrst}:"
    fi
    scheme+="tty:$(tty | sed -e 's,^/dev/,,' -e 's,/,,')"

    local fqdn="$(hostname -f 2> /dev/null)"
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
# cat "${HOME}/.bash_history" | sort -u > "${HOME}/.bash_history.tmp"; mv "${HOME}/.bash_history.tmp" "${HOME}/.bash_history"

  HISTSIZE=131072
  HISTFILESIZE="${HISTSIZE}"
  HISTCONTROL=ignorespace:erasedups
  HISTIGNORE='pwd:top:htop:history:history *:bg:bg *:fg:fg *:cd *:ls:ls *:la:la *:ll:ll *:fpr:fpr *'
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
  alias gs='git status '
  alias vi='vim '
  alias timestamp='date --utc "+%Y%m%d_%H%M%S_%N" '
  alias clipcopy=' xsel --clipboard --input '
  alias clippaste='xsel --clipboard --output '

fi

# tmux
if [[ -t 0 && -t 1 && -t 2 ]] && type tmux &> /dev/null; then

  scnt="$(tmux list-sessions 2> /dev/null | wc -l)"
  wcnt="$(tmux list-windows  2> /dev/null | wc -l)"
  pcnt="$(tmux list-panes    2> /dev/null | wc -l)"

  if   [[ '0' == "${scnt}" && '0' == "${wcnt}" && '0' == "${pcnt}" && ! -v TMUX ]]; then
    tmux new-session
  elif [[ '1' == "${scnt}" && '1' == "${wcnt}" && '1' == "${pcnt}" ]]; then
    tmux split-window -h
    tmux split-window -v
    tmux select-pane -t 0
  fi
# tmux attach-session

  unset scnt
  unset wcnt
  unset pcnt

fi

: <<'EXAMPLE'
# ~/.bashrc: executed by bash(1) for non-login shells.
# see /usr/share/doc/bash/examples/startup-files (in the package bash-doc)
# for examples

# If not running interactively, don't do anything
case $- in
    *i*) ;;
      *) return;;
esac

# don't put duplicate lines or lines starting with space in the history.
# See bash(1) for more options
HISTCONTROL=ignoreboth

# append to the history file, don't overwrite it
shopt -s histappend

# for setting history length see HISTSIZE and HISTFILESIZE in bash(1)
HISTSIZE=1000
HISTFILESIZE=2000

# check the window size after each command and, if necessary,
# update the values of LINES and COLUMNS.
shopt -s checkwinsize

# If set, the pattern "**" used in a pathname expansion context will
# match all files and zero or more directories and subdirectories.
#shopt -s globstar

# make less more friendly for non-text input files, see lesspipe(1)
#[ -x /usr/bin/lesspipe ] && eval "$(SHELL=/bin/sh lesspipe)"

# set variable identifying the chroot you work in (used in the prompt below)
if [ -z "${debian_chroot:-}" ] && [ -r /etc/debian_chroot ]; then
    debian_chroot=$(cat /etc/debian_chroot)
fi

# set a fancy prompt (non-color, unless we know we "want" color)
case "$TERM" in
    xterm-color|*-256color) color_prompt=yes;;
esac

# uncomment for a colored prompt, if the terminal has the capability; turned
# off by default to not distract the user: the focus in a terminal window
# should be on the output of commands, not on the prompt
#force_color_prompt=yes

if [ -n "$force_color_prompt" ]; then
    if [ -x /usr/bin/tput ] && tput setaf 1 >&/dev/null; then
	# We have color support; assume it's compliant with Ecma-48
	# (ISO/IEC-6429). (Lack of such support is extremely rare, and such
	# a case would tend to support setf rather than setaf.)
	color_prompt=yes
    else
	color_prompt=
    fi
fi

if [ "$color_prompt" = yes ]; then
    PS1='${debian_chroot:+($debian_chroot)}\[\033[01;32m\]\u@\h\[\033[00m\]:\[\033[01;34m\]\w\[\033[00m\]\$ '
else
    PS1='${debian_chroot:+($debian_chroot)}\u@\h:\w\$ '
fi
unset color_prompt force_color_prompt

# If this is an xterm set the title to user@host:dir
case "$TERM" in
xterm*|rxvt*)
    PS1="\[\e]0;${debian_chroot:+($debian_chroot)}\u@\h: \w\a\]$PS1"
    ;;
*)
    ;;
esac

# enable color support of ls and also add handy aliases
if [ -x /usr/bin/dircolors ]; then
    test -r ~/.dircolors && eval "$(dircolors -b ~/.dircolors)" || eval "$(dircolors -b)"
    alias ls='ls --color=auto'
    #alias dir='dir --color=auto'
    #alias vdir='vdir --color=auto'

    #alias grep='grep --color=auto'
    #alias fgrep='fgrep --color=auto'
    #alias egrep='egrep --color=auto'
fi

# colored GCC warnings and errors
#export GCC_COLORS='error=01;31:warning=01;35:note=01;36:caret=01;32:locus=01:quote=01'

# some more ls aliases
#alias ll='ls -l'
#alias la='ls -A'
#alias l='ls -CF'

# Alias definitions.
# You may want to put all your additions into a separate file like
# ~/.bash_aliases, instead of adding them here directly.
# See /usr/share/doc/bash-doc/examples in the bash-doc package.

if [ -f ~/.bash_aliases ]; then
    . ~/.bash_aliases
fi

# enable programmable completion features (you don't need to enable
# this, if it's already enabled in /etc/bash.bashrc and /etc/profile
# sources /etc/bash.bashrc).
if ! shopt -oq posix; then
  if [ -f /usr/share/bash-completion/bash_completion ]; then
    . /usr/share/bash-completion/bash_completion
  elif [ -f /etc/bash_completion ]; then
    . /etc/bash_completion
  fi
fi
EXAMPLE
