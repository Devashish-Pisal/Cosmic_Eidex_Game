package com.group06.cosmiceidex.controllers;

import com.group06.cosmiceidex.controllerlogic.SaveControllerVariables;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Dient dazu, die Funktionen der Szene Spielregeln zu implementieren.
 */

public class SpielregelnController {
    @FXML
    private ScrollPane spielanleitungScrollPane;

    @FXML
    private ImageView spielanleitung;

    /**
     * Initialisiert die Spielregeln-Szene.
     */
    public void initialize(){
        // Spielregeln Bild laden
        Image image = new Image(getClass().getResource("/com/group06/cosmiceidex/images/gui elements/Spielregeln.png").toExternalForm());
        spielanleitung.setImage(image);

        // Breite automatisch an Fenster anpassen
        spielanleitung.fitWidthProperty().bind(spielanleitungScrollPane.widthProperty());
        spielanleitung.setPreserveRatio(true);
    }

    /**
     * Funktion des "Zurück"-Buttons.
     * Lädt die vorherige Szene anhand der prevScene Variable.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onZurück(ActionEvent event) throws IOException {
        // Aktuelle Stage laden
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Parent root;

        // FXML laden
        if(SaveControllerVariables.getPrevScene() == "Spielraum"){
            root = FXMLLoader.load(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/Spielraum.fxml"));
        }
        else{
            root = FXMLLoader.load(getClass().getResource("/com/group06/cosmiceidex/FXMLFiles/Lobby.fxml"));
        }

        // Aktuelle Fenstergröße zwischenspeichern
        double width = stage.getWidth();
        double height = stage.getHeight();

        // Fenstergröße mitgeben und Szene wechseln
        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.show();
    }
}
