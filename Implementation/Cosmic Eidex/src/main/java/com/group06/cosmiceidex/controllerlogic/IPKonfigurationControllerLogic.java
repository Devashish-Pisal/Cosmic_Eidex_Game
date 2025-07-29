package com.group06.cosmiceidex.controllerlogic;

import java.util.regex.Pattern;

public class IPKonfigurationControllerLogic {
    private static final String IP_PATTERN = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    /**
     * Validiert die IP anhand eines Patterns
     * @param ip Die angegebene IP
     * @return Boolean, ob diese valide ist
     */
    public static boolean isValidIP(String ip) {
        return Pattern.matches(IP_PATTERN, ip);
    }

    /**
     * Validiert den Port
     * @param port Der gegebene Port
     * @return Boolean, ob dieser valide ist
     */
    public static boolean isValidPort(int port) {
        return port >= 0 && port <= 65535;
    }
}
