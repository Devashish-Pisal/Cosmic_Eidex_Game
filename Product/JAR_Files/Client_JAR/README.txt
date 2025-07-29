To start client run following command in terminal :


java --module-path "<lib-folder-path-of-javaFX-sdk>" --add-modules javafx.controls,javafx.fxml -jar "<path-of-Client-jar-file>"






e.g 

java --module-path "D:\Folder-1\Folder-2\javaFX_sdk_windows_x64\javafx-sdk-24.0.2\lib" --add-modules javafx.controls,javafx.fxml -jar "D:\Folder-1\Folder-2\Client.jar"



Note : 
1) Default Server IP : localhost & default Port : 1234 
2) SQLite Database will be created in home directory of user. (e.g C:/Users/TestUser/CosmicEidex/Tables.db)
3) while starting client choose sdk based on operating system. SDKs for common operating systems are already provided in this folder. If sdk for your system is not provided, can be found on following URL : https://gluonhq.com/products/javafx/