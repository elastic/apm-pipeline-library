set "str=%COMPUTERNAME%"
for /f "usebackq delims=" %%I in (`powershell "\"%str%\".toLower()"`) do set "lower=%%~I"
java -jar c:\\Users\\vagrant\\swarm-client.jar -labels "windows %lower% %lower%-immutable windows-immutable" -master http://10.0.2.2:18080 -fsroot c:\\jenkins -deleteExistingClients
