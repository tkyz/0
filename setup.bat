@echo off
setlocal

  set dist=debian

  wsl --set-default-version 2

  wsl --unregister %dist%
  wsl --install --distribution %dist%

  echo.
  echo 1. このwindow(a)とは別にwslのwindow(b)が起動します。
  echo 2. window(a)はこのままにして、window(b)に"Enter new UNIX username:"が表示されるまで待機してください。
  echo 3. 2のメッセージが表示されたらwindow(b)で[ctrl+c]を押して中断してください。xボタンを押して閉じてもよいです。
  echo 4. window(a)で何かキーを押してください。
  echo 待機中...
  pause > nul

  wsl --terminate %dist%
  wsl --set-version %dist% 2

  wsl --distribution %dist% -- sed -i 's#:/root:#:/mnt/c/Users/%USERNAME%:#' /etc/passwd
  wsl --terminate %dist%

  wsl --distribution %dist% -- ^
    apt update; ^
    apt upgrade -y; ^
    apt install -y curl; ^
    ^( curl https://setup.0 ^|^| curl https://raw.githubusercontent.com/tkyz/0/main/setup ^) ^| bash
  wsl --terminate %dist%

endlocal
