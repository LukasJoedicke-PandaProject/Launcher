package xyz.vitox.launcher;

import at.favre.lib.crypto.bcrypt.BCrypt;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;

public class DeviceUtil {

    public static String DEVICE_ID = getSerialNumber("C");

    /**
     * This method creates a vbs script, which will get executed and then return the ID of the C: drive
     * @param drive
     * @return
     */
    public static String getSerialNumber(String drive) {
        String result = "";
        try {
            File file = File.createTempFile("realhowto", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);

            String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
                    + "Set colDrives = objFSO.Drives\n" + "Set objDrive = colDrives.item(\"" + drive + "\")\n"
                    + "Wscript.Echo objDrive.SerialNumber"; // see note
            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.trim();
    }

    /**
     * Method to download a file
     * @param fromUrl
     * @param localFileName
     * @param agent
     */
    public static void downloadFile(String fromUrl, String localFileName, String agent) {
        try {
            File localFile = new File(localFileName);
            if (localFile.exists()) {
                localFile.delete();
            }
            localFile.createNewFile();
            URL url = new URL(fromUrl);
            OutputStream out = new BufferedOutputStream(new FileOutputStream(localFileName));
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", agent);
            InputStream in = conn.getInputStream();
            byte[] buffer = new byte[25 * 1024 * 1024];

            int numRead;
            int totalProccessed = 0;
            while ((numRead = in.read(buffer)) != -1) {
                totalProccessed += numRead;
                out.write(buffer, 0, numRead);
                PandaClassLoader.printProgress(System.currentTimeMillis(), conn.getContentLength(), totalProccessed);
            }
            in.close();
            out.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Set the default auth for the .htpasswd
     */
    public static void setDefaultAuthentication() {
        Authenticator.setDefault (new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication ("launcher", "bwtepCoeE43lcIl6omWzTvskcmHWS1D9IynOcJdh6D2HFSQl1IvMVFJr".toCharArray());
            }
        });
    }

    /**
     * Getting the current Date
     * @return
     */
    public static String getCurrentDate() {
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date = new Date();
        return dt.format(date);
    }

    /**
     * Get the PC Name of the PC
     * @return
     */
    public static String getPCName() {
        String hostname = "Unknown";

        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException ex) {

        }
        return hostname;
    }

    /**
     * Converting a Date-String to an timestamp
     * @param dateString
     * @return
     */
    public static long stringToTimestamp(String dateString) {
        dateString = dateString.replaceAll("\\s+", "T");
        LocalDateTime dateTime = LocalDateTime.parse(dateString);
        return Timestamp.valueOf(dateTime).getTime() / 1000L;
    }

    /**
     * Arguments (which includes a OTP) to be able to get files on the Server.
     * @return
     */
    public static String getFileQuery() {
        String deviceID =  DeviceUtil.getSerialNumber("C");
        long timestamp = DeviceUtil.stringToTimestamp(DeviceUtil.getCurrentDate());
        String otp = DeviceUtil.generateOTP(deviceID, Long.toString(timestamp));
        return "?driveID=" + deviceID + "&timestamp=" + timestamp + "&token=" + otp;
    }

    /**
     * A method which generates a OTP
     * The OTP is protected with a BCrypt hash and Base64 encoding
     * @param deviceID
     * @param currentTimestamp
     * @return
     */
    public static String generateOTP(String deviceID, String currentTimestamp) {
        String oneTimePassword = deviceID + "panda" + currentTimestamp + "security";
        String bCryptedPassword = BCrypt.with(BCrypt.Version.VERSION_2Y).hashToString(12, oneTimePassword.toCharArray());
        return Base64.getEncoder().encodeToString(bCryptedPassword.getBytes(StandardCharsets.UTF_8));
    }
}
