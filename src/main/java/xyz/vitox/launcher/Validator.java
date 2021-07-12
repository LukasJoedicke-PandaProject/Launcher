package xyz.vitox.launcher;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import xyz.vitox.Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Validator {

    /**
     * Check if the response from the REST API is valid
     * If yes: Start the Program
     * If no:  Close the Launcher
     * @param apiResponse
     * @param enteredKey
     */
    public void validateCheckKeyResponse(String apiResponse, String enteredKey) {
        try {
            JsonObject responseJson = new Gson().fromJson(apiResponse, JsonObject.class);
            String responseKey = responseJson.get("key").getAsString();
            String responsePCID = responseJson.get("pc_id").getAsString();
            String responseExpiringAt = responseJson.get("expiring_at").getAsString();
            String token = responseJson.get("token").getAsString();
            String timespamp = responseJson.get("timespamp").getAsString();

            if (responseKey.equals(enteredKey) && responsePCID.equals(DeviceUtil.getSerialNumber("C"))) {
                if (!(DeviceUtil.stringToTimestamp(DeviceUtil.getCurrentDate()) > DeviceUtil.stringToTimestamp(responseExpiringAt))) {
                    if (getToken(token, timespamp, enteredKey, responsePCID)) {
                        System.out.println("Successfully logged in. Starting Panda, please wait..");
                        System.out.println("INFO: Your license is valid until: " + responseExpiringAt);
                        System.out.println("");

                        File pandaDir = new File("Panda");
                        if (!pandaDir.exists()) {
                            pandaDir.mkdir();
                        }

                        Main.createLicenseFile(enteredKey, new File(Main.MAIN_FILE_PATH + "panda_license.json"));

                        checkIfUpdaterIsPresent();

                        Thread updateThread = startUpdater();
                        updateThread.start();
                        updateThread.join();

                        System.out.println("");
                        System.out.println("Starting Panda...");
                        PandaClassLoader pandaClassLoader = new PandaClassLoader();
                        pandaClassLoader.startPanda();
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Invalid key");
            File settingsFile = new File(Main.MAIN_FILE_PATH + "panda_license.json");

            if (settingsFile.exists()) {
                settingsFile.delete();
            }
        }
    }

    /**
     * Check if the OTP which got sent from the REST API matches with the local generated one
     * @param token
     * @param timestamp
     * @param key
     * @param pcID
     * @return
     */
    public boolean getToken(String token, String timestamp, String key, String pcID) {
        byte[] decodedBase64Bytes = Base64.getDecoder().decode(token.getBytes(StandardCharsets.UTF_8));
        String decodedBase64String = new String(decodedBase64Bytes);
        String plaintextToken = pcID + timestamp + key + "panda";
        return checkPassword(plaintextToken, decodedBase64String);
    }

    public boolean checkPassword(String password_plaintext, String stored_hash) {
        byte[] hash2y = stored_hash.getBytes(StandardCharsets.UTF_8);
        BCrypt.Result resultStrict = BCrypt.verifyer(BCrypt.Version.VERSION_2Y).verifyStrict(password_plaintext.getBytes(StandardCharsets.UTF_8), hash2y);
        return (resultStrict.verified);
    }

    /**
     * Check if the updater.jar is present
     * if not: Download it
     */
    private void checkIfUpdaterIsPresent() {
        File updater = new File(Main.MAIN_FILE_PATH + "updater.jar");

        if (!updater.exists()) {
            System.out.println("Info: Downloading updater...");
            DeviceUtil.downloadFile("https://localhost.com/files/updater.jar" + DeviceUtil.getFileQuery(), updater.getAbsolutePath(), "PandaLauncher");
            System.out.println("");
            System.out.println("");
        }
    }

    /**
     * Starting the auto updater for the Launcher
     * @return
     */
    private Thread startUpdater() {
        return new Thread(() -> {
            try {
                Process proc = Runtime.getRuntime().exec("java -jar PandaAssets/updater.jar " + Main.LAUNCHER_VERSION);
                InputStream in = proc.getInputStream();

                InputStreamReader isr = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(isr);

                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    if (line.contains("Restart required")) {
                        System.exit(1);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
