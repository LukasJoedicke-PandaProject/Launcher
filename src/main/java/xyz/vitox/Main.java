package xyz.vitox;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import xyz.vitox.launcher.DeviceUtil;
import xyz.vitox.launcher.RequestAPI;
import xyz.vitox.launcher.Validator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class Main {

    public static final String LAUNCHER_VERSION = "1.9.0";
    public static String LICENSE_KEY;
    public static String MAIN_FILE_PATH = "./PandaAssets/";

    public static void main(String[] args) {

        DeviceUtil.setDefaultAuthentication();

        Main main = new Main();
        RequestAPI requestAPI = new RequestAPI();
        Validator validator = new Validator();

        File licenseFile = new File(MAIN_FILE_PATH + "panda_license.json");

        if (licenseFile.exists()) {
            LICENSE_KEY = readLicenseFile(licenseFile);
        } else {
            LICENSE_KEY = main.getInputFromConsole("License Key: ");
        }

        String checkKeyResponse = requestAPI.checkKey(LICENSE_KEY);
        validator.validateCheckKeyResponse(checkKeyResponse, LICENSE_KEY);
    }

    /**
     * Read the input which got typed into the console
     * @param displayText
     * @return
     */
    public String getInputFromConsole(String displayText) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println(displayText);
            return br.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Read the license_key of the license.json file
     * @param licenseFile
     * @return
     */
    public static String readLicenseFile(File licenseFile) {
        try {
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(Paths.get(licenseFile.getAbsolutePath()));
            Map<?, String> map = gson.fromJson(reader, Map.class);
            reader.close();
            return map.get("license_key");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Create the license.json and write the license_key as JsonObject
     * @param licenseKey
     * @param licenseFile
     */
    public static void createLicenseFile(String licenseKey, File licenseFile) {
        try {
            if (licenseFile.createNewFile()) {
                JsonWriter writer = new JsonWriter(new FileWriter(licenseFile.getAbsolutePath()));
                writer.beginObject();
                writer.name("license_key").value(licenseKey);
                writer.endObject();
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
