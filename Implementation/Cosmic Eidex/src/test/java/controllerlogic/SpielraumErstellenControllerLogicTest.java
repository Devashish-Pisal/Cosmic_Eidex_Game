package controllerlogic;

import com.group06.cosmiceidex.controllerlogic.SpielraumErstellenControllerLogic;
import org.junit.jupiter.api.Test;

import static com.group06.cosmiceidex.controllerlogic.SpielraumErstellenControllerLogic.validateGameroom;
import static org.junit.jupiter.api.Assertions.*;

class SpielraumErstellenControllerLogicTest {
    @Test
    public void validateGameroomTrue(){
        String gameroomname = "a";
        String password = "a";
        String repeatedPassword = "a";
        String[] test = SpielraumErstellenControllerLogic.validateGameroom(gameroomname, password, repeatedPassword);
        assertEquals(null, test);
    }

    @Test
    public void validateGameroomNameEmpty(){
        String gameroomname = "";
        String password = "a";
        String repeatedPassword = "a";
        String[] test = SpielraumErstellenControllerLogic.validateGameroom(gameroomname, password, repeatedPassword);
        if(test != null){
            assertEquals("Spielraum Name Fehler", test[0]);
            assertEquals("Spielraum Name darf nicht leer sein!", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateGameroomNameSpace(){
        String gameroomname = " a ";
        String password = "a";
        String repeatedPassword = "a";
        String[] test = SpielraumErstellenControllerLogic.validateGameroom(gameroomname, password, repeatedPassword);
        if(test != null){
            assertEquals("Spielraum Name Fehler", test[0]);
            assertEquals("Spielraum Name darf kein Leerzeichen erhalten!", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateGameroomNameLobby(){
        String gameroomname = "LOBBY";
        String password = "a";
        String repeatedPassword = "a";
        String[] test = SpielraumErstellenControllerLogic.validateGameroom(gameroomname, password, repeatedPassword);
        if(test != null){
            assertEquals("Spielraum Name Fehler", test[0]);
            assertEquals("Spielraum Name darf nicht 'LOBBY' sein!", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateGameroomNameLobby2(){
        String gameroomname = "Lobby";
        String password = "a";
        String repeatedPassword = "a";
        String[] test = SpielraumErstellenControllerLogic.validateGameroom(gameroomname, password, repeatedPassword);
        if(test != null){
            assertEquals("Spielraum Name Fehler", test[0]);
            assertEquals("Spielraum Name darf nicht 'LOBBY' sein!", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateGameroomNameTooLong(){
        String gameroomname = "aaaaaaaaaaaaaaa";
        String password = "a";
        String repeatedPassword = "a";
        String[] test = SpielraumErstellenControllerLogic.validateGameroom(gameroomname, password, repeatedPassword);
        if(test != null){
            assertEquals("Spielraum Name Fehler", test[0]);
            assertEquals("Spielraum Name ist zu groß!", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateGameroomPasswordEmpty(){
        String gameroomname = "a";
        String password = "";
        String repeatedPassword = "";
        String[] test = SpielraumErstellenControllerLogic.validateGameroom(gameroomname, password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Passwort Feld darf nicht leer sein!", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateGameroomRepeatedPasswordEmpty(){
        String gameroomname = "a";
        String password = "a";
        String repeatedPassword = "";
        String[] test = SpielraumErstellenControllerLogic.validateGameroom(gameroomname, password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Das wiederholte Passwort Feld darf nicht leer sein!", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateGameroomPasswordSpace(){
        String gameroomname = "a";
        String password = "a ";
        String repeatedPassword = "a ";
        String[] test = SpielraumErstellenControllerLogic.validateGameroom(gameroomname, password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Das Passwort Feld darf kein Leerzeichen enthalten!", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateGameroomPasswordUnequal(){
        String gameroomname = "a";
        String password = "a";
        String repeatedPassword = "b";
        String[] test = SpielraumErstellenControllerLogic.validateGameroom(gameroomname, password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Die Passwörter sind nicht gleich!", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateGameroomPasswordTooLong(){
        String gameroomname = "a";
        String password = "aaaaaaaaaaaaaaa";
        String repeatedPassword = "aaaaaaaaaaaaaaa";
        String[] test = SpielraumErstellenControllerLogic.validateGameroom(gameroomname, password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Passwort ist zu groß!", test[1]);
        }
        else assertEquals("string", "null");
    }
}