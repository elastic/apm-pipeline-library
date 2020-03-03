@echo on

docker -v || echo ''
dotnet --info || echo ''
msbuild || echo ''
nuget --help || echo ''
python --version || echo ''
python2 --version || echo ''
python3 --version || echo ''
vswhere || echo ''
