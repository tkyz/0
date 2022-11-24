@echo off

  reg add "HKEY_CURRENT_USER\Software\Microsoft\Command Processor" ^
    /v AutoRun ^
    /t REG_SZ  ^
    /d "%~0"   ^
    /f > nul

  set "JAVA_HOME=%USERPROFILE%/opt/net.java.jdk/19"
  set "PATH=%PATH%;%JAVA_HOME%/bin"

  set "CLASSPATH=.;./target/classes;./target/dependency/*;%USERPROFILE%/lib/*"

:EOF
echo on
