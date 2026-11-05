#!/usr/bin/env python3

import os
import sys
import ssl
import subprocess
import urllib.request
import json

from datetime import datetime

def _env(name) -> str:

  ret = None

  with open(env_file, "r", encoding="utf-8") as f:
    for line in f:

      line = line.strip()
      if "" == line or line.startswith("#"):
        continue

      key, val = line.split("=", 1)

      if key.strip() != name:
        continue

      ret = val.strip()
      break

  return ret

def ymdhms() -> str:
  return datetime.now().strftime("%Y-%m-%d %H:%M:%S")

def fread(file, encoding="utf8") -> str:
  content = None
  with open(file, "r", encoding=encoding) as f:
    content = f.read()
  return content

def fwrite(file, content, encoding="utf8") -> None:
  with open(file, "w", encoding=encoding) as f:
    f.write(content)

def call(*command) -> str:
  return subprocess.run(command, capture_output=True, text=True, check=True).stdout.strip()

def assistant(messages, thinking=False) -> str:

  payload = {
    "model": "default",
    "stream": True,
    "messages": messages,
    "chat_template_kwargs": {"enable_thinking": thinking},
  }

  url = f"https://chat.{domain}/v1/chat/completions"
  req = urllib.request.Request(
    url,
    data    = json.dumps(payload).encode("utf-8"),
    headers = {"Content-Type": "application/json"},
    method  = "POST",
  )

# ctx = ssl.create_default_context(cafile=crt_file)
  ctx = ssl.create_default_context()
  ctx.check_hostname = False
  ctx.verify_mode    = ssl.CERT_NONE

  content = None
  with urllib.request.urlopen(req, context=ctx) as resp:

    content = []
    for raw in resp:

      line = raw.decode("utf-8").strip()
      if not line.startswith("data:"):
        continue

      data = line[len("data:"):].strip()
      if data == "[DONE]":
        break

      chunk   = json.loads(data)
      choices = chunk.get("choices", [])

      for choice in choices:

        delta = choice.get("delta", {})

        # reasoning
        data = delta.get("reasoning_content") or delta.get("reasoning")
        if data:
          sys.stdout.write(data)
          sys.stdout.flush()
#         content.append(data)

        # content
        data = delta.get("content")
        if data:
          sys.stdout.write(data)
          sys.stdout.flush()
          content.append(data)

    sys.stdout.write("\n")
    sys.stdout.flush()

    content = "".join(content)

  return content

pwd       = os.getcwd()
user      = os.environ.get("USER")
home      = os.environ.get("HOME")

bin_dir   = home + "/bin"
dat_dir   = home + "/dat"
doc_dir   = home + "/doc"
enc_dir   = home + "/enc"
lib_dir   = home + "/lib"
mnt_dir   = home + "/mnt"
opt_dir   = home + "/opt"
src_dir   = home + "/src"
sbin_dir  = home + "/sbin"
tmp_dir   = home + "/tmp"

# dat
index_dir = dat_dir + "/index"
ref_dir   = dat_dir + "/ref"
text_dir  = dat_dir + "/train.text"
image_dir = dat_dir + "/train.image"
audio_dir = dat_dir + "/train.audio"
video_dir = dat_dir + "/train.video"

# .env
env_file    = home + "/.env"
openpgp4fpr = _env("openpgp4fpr")
jpki_id     = _env("jpki_id")
machine_id  = _env("machine_id")
boot_id     = _env("boot_id")
setup_id    = _env("setup_id")
upstream_ip = _env("upstream_ip")
dns_ip      = _env("dns_ip")
wan_ip      = _env("wan_ip")
lan_ip      = _env("lan_ip")
domain      = _env("domain")
namespace   = _env("namespace")
tag         = _env("tag")

crt_file = "/usr/local/share/ca-certificates/" + namespace + ".crt"
