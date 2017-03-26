if exist "%ProgramFiles%\Java\jre1.8.0_121" goto else
set "JRE_HOME=%ProgramFiles%\Java\jre1.8.0_45"
set "JAVA_HOME=%ProgramFiles%\Java\jdk1.7.0_17"
exit /b 0
:else
set "JRE_HOME=%ProgramFiles%\Java\jre1.8.0_121"
set "JAVA_HOME=%ProgramFiles%\Java\jdk1.8.0_111"
exit /b 0