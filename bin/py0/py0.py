#!/usr/bin/env python3

import os
import socket

def _domain() -> str:
  fqdn = socket.getfqdn()
  return fqdn.partition(".")[2]

def fread(file, encoding="utf8") -> str:
  content = None
  with open(file, "r", encoding=encoding) as f:
    content = f.read()
  return content

def fwrite(file, content) -> None:
  with open(file, "w") as f:
    f.write(content)

pwd       = os.getcwd()
home      = os.environ.get("HOME")
doc_dir   = home + "/doc"
dat_dir   = home + "/dat"
enc_dir   = home + "/enc"
lib_dir   = home + "/lib"
mnt_dir   = home + "/mnt"
opt_dir   = home + "/opt"
src_dir   = home + "/src"
tmp_dir   = home + "/tmp"

index_dir = dat_dir + "/index"
ref_dir   = dat_dir + "/ref"
text_dir  = dat_dir + "/train.text"
image_dir = dat_dir + "/train.image"
audio_dir = dat_dir + "/train.audio"
video_dir = dat_dir + "/train.video"

domain    = _domain()
