package com.gtarc.chariot.proxyagent;

import de.dailab.jiactng.agentcore.action.IMethodExposingBean;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;

import java.util.HashMap;

public class Util {

    private static HttpClient httpClient = new HttpClient();
    private static HashMap<String, String> deviceIDToAgentID = new HashMap<>();


    public static void setDeviceIDToAgentID(HashMap<String, String> deviceIDToAgentID) {
        Util.deviceIDToAgentID = deviceIDToAgentID;
    }

    public static void addAgent(String agentID, String deviceID) {
        deviceIDToAgentID.put(deviceID, agentID);
    }

    public static void removeAgent(String deviceID) {
        deviceIDToAgentID.remove(deviceID);
    }

    /**
     * Return the agent id from the device
     *
     * @param uuid The device uuid
     * @return The agent id
     */
    public static String getAgentIDByUUID(String uuid) throws Exception {
        // Check if device id is in agent list
        String agentID = deviceIDToAgentID.get(uuid);
        if (agentID == null) {
            // Query new updates
            deviceIDToAgentID = httpClient.establishConnection();

            agentID = deviceIDToAgentID.get(uuid);
            if (agentID == null) {
                System.err.println("Agent not found for device: " + uuid);
                throw new Exception("Agent not found for device: " + uuid);
            }
        }

        return agentID;
    }
}
