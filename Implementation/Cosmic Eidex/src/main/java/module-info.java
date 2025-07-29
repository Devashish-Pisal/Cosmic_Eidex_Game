module com.group06.cosmiceidex {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires net.bytebuddy;
    requires org.mockito;
    requires net.bytebuddy.agent;
    requires mysql.connector.j;
    requires java.compiler;
    requires org.xerial.sqlitejdbc;

    opens com.group06.cosmiceidex.client to javafx.fxml;
    exports com.group06.cosmiceidex.client;

    exports com.group06.cosmiceidex.controllers;
    opens com.group06.cosmiceidex.controllers to javafx.fxml, javafx.graphics;

    opens com.group06.cosmiceidex.server to org.mockito;
    exports com.group06.cosmiceidex.controllerlogic;
    opens com.group06.cosmiceidex.controllerlogic to javafx.fxml, javafx.graphics;
}