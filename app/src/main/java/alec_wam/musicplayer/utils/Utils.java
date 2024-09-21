package alec_wam.musicplayer.utils;

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

}
