package controllerlogic;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.group06.cosmiceidex.controllerlogic.ProfileinstellungenControllerLogic.validatePassword;
import static org.junit.jupiter.api.Assertions.*;

class ProfileinstellungenControllerLogicTest {
    @Test
    public void validatePasswordTrue(){
        String password = "12345678Qq@";
        String repeatedPassword = "12345678Qq@";
        assertEquals(null, validatePassword(password, repeatedPassword));
    }

    @Test
    public void validatePasswordEmptyPassword(){
        String password = "";
        String repeatedPassword = "";
        String[] test = validatePassword(password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Passwort darf nicht leer sein!", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validatePasswordPasswordRepeatedPasswordUnequal(){
        String password = "12345678Qq@";
        String repeatedPassword = "12345678Qq";
        String[] test = validatePassword(password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Die Passwörter sind nicht gleich.", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validatePasswordMin8Letters(){
        String password = "1234";
        String repeatedPassword = "1234";
        String[] test = validatePassword(password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Passwort muss mindestens 8 Zeichen lang sein.", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validatePasswordSpace(){
        String password = "aAaAa AaA";
        String repeatedPassword = "aAaAa AaA";
        String[] test = validatePassword(password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Das Passwort darf kein Leerzeichen enthalten.", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validatePasswordMin1Number(){
        String password = "aAaAaAaA";
        String repeatedPassword = "aAaAaAaA";
        String[] test = validatePassword(password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Passwort muss mindestens einen Zahl enthalten.", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validatePasswordMin1SmallLetter(){
        String password = "AAAAAAA1";
        String repeatedPassword = "AAAAAAA1";
        String[] test = validatePassword(password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Passwort muss mindestens einen Kleinbuchstaben enthalten.", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validatePasswordMin1BigLetter(){
        String password = "aaaaaaa1";
        String repeatedPassword = "aaaaaaa1";
        String[] test = validatePassword(password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Passwort muss mindestens einen Großbuchstaben enthalten.", test[1]);
        }
        else assertEquals("string", "null");
    }

    @Test
    public void validatePasswordMin1Symbol(){
        String password = "Aaaaaaa1";
        String repeatedPassword = "Aaaaaaa1";
        String[] test = validatePassword(password, repeatedPassword);
        if(test != null){
            assertEquals("Passwort Fehler", test[0]);
            assertEquals("Passwort muss mindestens ein Sonderzeichen enthalten (z.B. !@#$).", test[1]);
        }
        else assertEquals("string", "null");
    }
}