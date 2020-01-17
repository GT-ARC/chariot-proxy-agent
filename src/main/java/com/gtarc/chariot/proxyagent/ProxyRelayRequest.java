package com.gtarc.chariot.proxyagent;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class ProxyRelayRequest {

    private String command;
    private String uuid;
    private HashMap<String, String> inputs;

    public ProxyRelayRequest() { }

    public ProxyRelayRequest(String command, String uuid, HashMap<String, String> inputs) {
        this.command = command;
        this.uuid = uuid;
        this.inputs = inputs;
    }

    /**
     * Adds an mapping between uuid and agent id to the input map
     * the device uuid with the agent id to be invoked
     */
    public void addAgentIDMappingToInput() throws Exception {
        // Getting an iterator
        Iterator<Entry<String, String>> inputIterator = inputs.entrySet().iterator();

        // Iterate through the hashmap
        // and add some bonus marks for every student
        System.out.println("HashMap after adding bonus marks:");

        while (inputIterator.hasNext()) {
            Entry<String, String> input = inputIterator.next();
            if (input.getKey().contains("device-uuid")) {
                String aID = Util.getAgentIDByUUID(input.getValue());
                this.inputs.put(input.getValue(), aID);
            }
        }
    }

    public String getJson() {
        return new Gson().toJson(this, ProxyRelayRequest.class);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public HashMap<String, String> getInputs() {
        return inputs;
    }

    public void setInputs(HashMap<String, String> inputs) {
        this.inputs = inputs;
    }
}
