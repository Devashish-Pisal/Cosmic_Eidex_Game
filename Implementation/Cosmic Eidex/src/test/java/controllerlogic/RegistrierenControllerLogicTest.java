package controllerlogic;

import com.group06.cosmiceidex.controllerlogic.RegistrierenControllerLogic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistrierenControllerLogicTest {
    String SEPERATOR = ":";
    @Test
    public void validateCredentialsTrue(){
        String username = "a";
        String password = "12345678Qq@";
        String repeatedPassword = "12345678Qq@";
        String[] test = RegistrierenControllerLogic.validateCredentials(username, password, repeatedPassword);
        assertEquals(null, test);
    }

    @Test
    public void validateCredentialsUsernameEmpty(){
        String username = "";
        String password = "a";
        String repeatedPassword = "a";
        String[] test = RegistrierenControllerLogic.validateCredentials(username, password, repeatedPassword);
        if(test != null){
            assertEquals("Benutzername Fehler", test[0]);
            assertEquals("Benutzername darf nicht leer sein!", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateCredentialsUsernameSeperator(){
        String username = "a:";
        String password = "a";
        String repeatedPassword = "a";
        String[] test = RegistrierenControllerLogic.validateCredentials(username, password, repeatedPassword);
        if(test != null){
            assertEquals("Benutzername Fehler", test[0]);
            assertEquals("Benutzername darf das Zeichen '\" + SEPARATOR + \"' nicht enthalten.", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateCredentialsUsernameEasyBot(){
        String username = "EasyBot-15";
        String password = "a";
        String repeatedPassword = "a";
        String[] test = RegistrierenControllerLogic.validateCredentials(username, password, repeatedPassword);
        if(test != null){
            assertEquals("Benutzername Fehler", test[0]);
            assertEquals("Benutzername darf nicht '" + username + "' sein. Da es ein reservierte Benutzername ist.", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateCredentialsUsernameHardBot(){
        String username = "HardBot-15";
        String password = "a";
        String repeatedPassword = "a";
        String[] test = RegistrierenControllerLogic.validateCredentials(username, password, repeatedPassword);
        if(test != null){
            assertEquals("Benutzername Fehler", test[0]);
            assertEquals("Benutzername darf nicht '" + username + "' sein. Da es ein reservierte Benutzername ist.", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateCredentialsUsernameSpace(){
        String username = "a ";
        String password = "a";
        String repeatedPassword = "a";
        String[] test = RegistrierenControllerLogic.validateCredentials(username, password, repeatedPassword);
        if(test != null){
            assertEquals("Benutzername Fehler", test[0]);
            assertEquals("Benutzername darf kein Leerzeichen erhalten.", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateCredentialsUsernameSystem(){
        String username = "SYSTEM";
        String password = "a";
        String repeatedPassword = "a";
        String[] test = RegistrierenControllerLogic.validateCredentials(username, password, repeatedPassword);
        if(test != null){
            assertEquals("Benutzername Fehler", test[0]);
            assertEquals("Benutzername darf nicht 'SYSTEM' sein!", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateCredentialsUsernameServer(){
        String username = "SERVER";
        String password = "a";
        String repeatedPassword = "a";
        String[] test = RegistrierenControllerLogic.validateCredentials(username, password, repeatedPassword);
        if(test != null){
            assertEquals("Benutzername Fehler", test[0]);
            assertEquals("Benutzername darf nicht 'SERVER' sein!", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateCredentialsPasswordEmpty(){
        String username = "a";
        String password = "";
        String repeatedPassword = "";
        String[] test = RegistrierenControllerLogic.validateCredentials(username, password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Passwort darf nicht leer sein!", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateCredentialsPasswordEmpty2(){
        String username = "a";
        String password = null;
        String repeatedPassword = null;
        String[] test = RegistrierenControllerLogic.validateCredentials(username, password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Passwort darf nicht leer sein!", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateCredentialsPasswordSeperator(){
        String username = "a";
        String password = "a:";
        String repeatedPassword = "a:";
        String[] test = RegistrierenControllerLogic.validateCredentials(username, password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Passwort darf das Zeichen '\" + SEPARATOR + \"' nicht enthalten.", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateCredentialsPasswordSpace(){
        String username = "a";
        String password = "a ";
        String repeatedPassword = "a ";
        String[] test = RegistrierenControllerLogic.validateCredentials(username, password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Das Passwort darf kein Leerzeichen enthalten.", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateCredentialsPasswordUnequal(){
        String username = "a";
        String password = "a";
        String repeatedPassword = "b";
        String[] test = RegistrierenControllerLogic.validateCredentials(username, password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Die Passwörter sind nicht gleich.", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateCredentialsPasswordTooShort(){
        String username = "a";
        String password = "a";
        String repeatedPassword = "a";
        String[] test = RegistrierenControllerLogic.validateCredentials(username, password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Passwort muss mindestens 8 Zeichen lang sein.", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateCredentialsMin1Number(){
        String username = "a";
        String password = "aaaaaaaaa";
        String repeatedPassword = "aaaaaaaaa";
        String[] test = RegistrierenControllerLogic.validateCredentials(username, password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Passwort muss mindestens eine Zahl enthalten.", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateCredentialsMin1SmallLetter(){
        String username = "a";
        String password = "AAAAAAAAAA1";
        String repeatedPassword = "AAAAAAAAAA1";
        String[] test = RegistrierenControllerLogic.validateCredentials(username, password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Passwort muss mindestens einen Kleinbuchstaben enthalten.", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateCredentialsMin1BigLetter(){
        String username = "a";
        String password = "aaaaaaaaaaa1";
        String repeatedPassword = "aaaaaaaaaaa1";
        String[] test = RegistrierenControllerLogic.validateCredentials(username, password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Passwort muss mindestens einen Großbuchstaben enthalten.", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validateCredentialsMin1Symbol(){
        String username = "a";
        String password = "aaaaaaaaaaA1";
        String repeatedPassword = "aaaaaaaaaaA1";
        String[] test = RegistrierenControllerLogic.validateCredentials(username, password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Passwort muss mindestens ein Sonderzeichen enthalten (z.B. !@#$).", test[1]);
        }
        else assertEquals("string", "null");
    }
}