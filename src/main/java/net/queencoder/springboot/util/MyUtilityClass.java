package net.queencoder.springboot.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MyUtilityClass {

    // Private constructor to prevent instantiation
    private MyUtilityClass() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static boolean isValidFilenameFormat(String filename) {
        // Define a regular expression for the expected filename format
        String regex = "^[a-zA-Z0-9_-]+_[a-zA-Z0-9_-]+_others\\.csv$";
    
        // Use Pattern and Matcher classes to validate the filename
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(filename);
    
        return matcher.matches();
    }

}
