package com.gtarc.chariot.proxyagent;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;

public class HttpClient {
    private static final String startUrl = "http://chariot-km.dai-lab.de:81/monitoringservice/";
    private static final String postfix = "?format=json";
    private static String mappingsURL = "";
    private OkHttpClient client = new OkHttpClient();

    public HashMap<String, String> establishConnection() {
        HashMap<String, String> retMap = new HashMap<>();
        Request request = new Request.Builder()
                .url(startUrl + postfix)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            JSONParser parser = new JSONParser();

            String body = response.body().string();
            if (response.code() != 404 && !body.equals("[]")) {

                Object receivedO = parser.parse(body);

                JSONObject monService;
                if (receivedO instanceof JSONArray) monService = ((JSONObject) ((JSONArray) receivedO).get(0));
                else monService = ((JSONObject) receivedO);

                JSONObject mapping = ((JSONObject) monService.get("agentlist"));
                mappingsURL = (String) mapping.get("url");
                JSONArray jsonArray = (JSONArray) mapping.get("mappings");
                jsonArray.forEach(o -> {
                    JSONObject element = ((JSONObject) o);
                    retMap.put((String) element.get("device_id"), (String) element.get("agent_id"));
                });
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return retMap;
    }
}
