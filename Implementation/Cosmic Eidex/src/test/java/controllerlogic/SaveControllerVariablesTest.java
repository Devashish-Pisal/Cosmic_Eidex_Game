package controllerlogic;

import com.group06.cosmiceidex.controllerlogic.SaveControllerVariables;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SaveControllerVariablesTest {

    @Test
    void testSetPrevSceneTrue(){
        SaveControllerVariables.setPrevScene("Test");
        assertEquals("Test", SaveControllerVariables.getPrevScene());
    }

    @Test
    void testSetSelectedSpielraum(){
        SaveControllerVariables.setSelectedSpielraum("LobbyTest");
        assertEquals("LobbyTest", SaveControllerVariables.getSelectedSpielraum());
    }
}