package xyz.vitox.launcher;

import com.google.gson.JsonObject;
import okhttp3.*;

import java.util.Arrays;

public class RequestAPI {

    OkHttpClient client = new OkHttpClient().newBuilder().connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS)).build();

    public static String lastCurrentDate = DeviceUtil.getCurrentDate();

    /**
     * This request serves to determin if the license and every other stuff is fine.
     * @param key
     * @return
     */
    public String checkKey(String key) {
        JsonObject authObject = new JsonObject();
        authObject.addProperty("key", key);
        authObject.addProperty("cid", DeviceUtil.DEVICE_ID);
        authObject.addProperty("requestDate", lastCurrentDate);
        authObject.addProperty("client", "Launcher");
        authObject.addProperty("pcName", DeviceUtil.getPCName());
        authObject.addProperty("token", DeviceUtil.generateOTP(DeviceUtil.DEVICE_ID, lastCurrentDate));

        return makePOSTRequest("/v1/checkKey", authObject.toString());
    }

    /**
     * Abstraction to make a POST request to our REST API
     * @param restRoute
     * @param bodyText
     * @return
     */
    public String makePOSTRequest(String restRoute, String bodyText) {
        try {
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, bodyText);
            Request request = new Request.Builder()
                    .url("https://api.localhost.com" + restRoute)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("User-Agent", "PandaLauncher")
                    .addHeader("Authorization", "Basic cGFuZGFsYXVuY2hlcjpid3RlcENvZUU0M2xjSWw2b21XelR2c2tjbUhXUzFEOUl5bk9jSmRoNkQySEZTUWwxSXZNVkZKcg==")
                    .build();
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            return responseBody;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error";
    }

}
