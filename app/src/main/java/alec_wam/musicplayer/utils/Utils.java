package alec_wam.musicplayer.utils;

public class Utils {

    public static String convertSecondsToTimeString(int totalSeconds) {
        int hours = totalSeconds / 3600;  // 1 hour = 3600 seconds
        int minutes = (totalSeconds % 3600) / 60;  // Remaining minutes
        int seconds = totalSeconds % 60;  // Remaining seconds

        StringBuilder timeString = new StringBuilder();
        if (hours > 0) {
            timeString.append(hours).append("h");
        }
        if (minutes > 0) {
            timeString.append(minutes).append("m");
        }
        if (seconds > 0) {
            timeString.append(seconds).append("s");
        }

        return timeString.toString();
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
