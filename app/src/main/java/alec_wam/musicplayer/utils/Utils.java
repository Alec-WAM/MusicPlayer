package alec_wam.musicplayer.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    public static String convertMillisecondsToTimeString(long millis) {
         long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60)) % 24;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    public static int getDiskNumber(int track) {
        String trackString = "" + track;
        if(trackString.length() >= 4){
            //Has Disk Encoding
            try {
                return Integer.parseInt(trackString.substring(0, 1));
            }
            catch (NumberFormatException e){
                e.printStackTrace();
                return 1;
            }
        }
        return 1;
    }

    public static int getRealTrackNumber(int track){
        String trackString = "" + track;
        if(trackString.length() >= 4){
            //Has Disk Encoding
            try {
                return Integer.parseInt(trackString.substring(1));
            }
            catch (NumberFormatException e){
                e.printStackTrace();
                return track;
            }
        }
        return track;
    }

    public static String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
