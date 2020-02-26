package com.gtarc.chariot.proxyagent;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;

public class HttpClient {
    private static final String startUrl = "http://chariot-km.dai-lab.de:8080/v1/monitoringservice/";
    private static final String postfix = "?format=json";
    private static String mappingsURL = "";
    private OkHttpClient client = new OkHttpClient();

    public HashMap<String, String> establishConnection() {

        System.out.println("Get monitoring map");

        HashMap<String, String> retMap = new HashMap<>();
        Request request = new Request.Builder()
                .url(startUrl + postfix)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            if (response.code() != 404 && !body.equals("[]")) {

                Object receivedO = JsonParser.parseString(body);

                JsonObject monService;
                if (receivedO instanceof JsonArray) monService = ((JsonObject) ((JsonArray) receivedO).get(0));
                else monService = ((JsonObject) receivedO);

                JsonObject mapping = ((JsonObject) monService.get("agentlist"));
                mappingsURL = mapping.getAsJsonPrimitive("url").getAsString();
                JsonArray jsonArray = (JsonArray) mapping.get("mappings");
                jsonArray.forEach(o -> {
                    JsonObject element = o.getAsJsonObject();
                    retMap.put(
                            element.getAsJsonPrimitive("device_id").getAsString(),
                            element.getAsJsonPrimitive("agent_id").getAsString()
                    );
                });
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return retMap;
    }
}
