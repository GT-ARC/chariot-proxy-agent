package com.gtarc.chariot.proxyagent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PlanRequestTest {

    @Test
    public void testParsing() {
        Type listType = new TypeToken<ArrayList<PlanRequest>>(){}.getType();
        List<PlanRequest> planRequestList = new Gson().fromJson(getJson(), listType);

        System.out.println(planRequestList.toString());
    }



    public static String getJson() {
        return "\n" +
                "[\n" +
                "  {\n" +
                "    \"command\": \"stop\",\n" +
                "    \"uuid\": \"example-uuid\",\n" +
                "    \"inputs\": {\n" +
                "      \"device-uuid\": \"device-example-uuid\",\n" +
                "      \"second-parameter\": \"example-parameter\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"command\": \"disconnect\",\n" +
                "    \"uuid\": \"example-uuid-2\",\n" +
                "    \"inputs\": {\n" +
                "      \"device-uuid\": \"device-example-uuid-2\",\n" +
                "      \"second-parameter\": \"example-parameter-2\"\n" +
                "    }\n" +
                "  }\n" +
                "]";
    }
}
