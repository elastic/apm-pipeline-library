@echo on

docker -v || echo ''
dotnet --info || echo ''
msbuild || echo ''
nuget --help || echo ''
python --version || echo ''
python2 --version || echo ''
python3 --version || echo ''
c:\python2\bin\python.exe || echo ''
c:\python27\bin\python.exe || echo ''
c:\python3\bin\python.exe || echo ''
c:\python38\bin\python.exe || echo ''
vswhere || echo ''
