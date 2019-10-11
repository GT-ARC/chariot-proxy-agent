package com.gtarc.chariot.proxyagent;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class MyHttpServer {

    static final int PORT = 8080;
    private ProxyAgent proxyAgent;

    MyHttpServer(ProxyAgent proxyAgent) {
        this.proxyAgent = proxyAgent;
        try
        {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            createServerContext(server);
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
        }
        catch (Throwable tr)
        {
            tr.printStackTrace();
        }
    }

    private void createServerContext(HttpServer server) {
        server.createContext("/", httpExchange ->
        {
            sendResponse(httpExchange, 200, "Pls use /chariot/sendAction", "text/plain; charset=UTF-8");
        });

        server.createContext("/chariot/sendAction", this::receiveSendRequest);
    }

    private void sendResponse(HttpExchange httpExchange, int code, String message, String contentType) throws IOException {
        byte[] response = message.getBytes(StandardCharsets.UTF_8);

        Headers headers = httpExchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Headers","x-prototype-version,x-requested-with,Content-Type,Authorization");
        headers.add("Access-Control-Allow-Methods","GET,POST");
        headers.add("Access-Control-Allow-Origin","*");
        headers.add("Content-Type", contentType);

        if (httpExchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            httpExchange.sendResponseHeaders(204, -1);
            return;
        }

        httpExchange.sendResponseHeaders(code, response.length);

        OutputStream out = httpExchange.getResponseBody();
        out.write(response);
        out.close();
        httpExchange.close();
    }

    private void receiveSendRequest(HttpExchange httpExchange) throws IOException {
        System.out.println("Request: " + Thread.currentThread().getName());
        String body = "";
        try {
            StringWriter writer = new StringWriter();
            IOUtils.copy(httpExchange.getRequestBody(), writer, Charset.defaultCharset());
            body = writer.toString();
        } catch (IOException e) {
            System.err.println("Massage Parse Excepiton");
            sendResponse(httpExchange, 400, "Message Read Exception", "text/plain; charset=UTF-8");
        }

        System.out.println(body);
        if (httpExchange.getRequestMethod().equalsIgnoreCase("OPTIONS"))
            sendResponse(httpExchange, 200, "","");

        JSONParser parser = new JSONParser();
        String device_id = "";
        String jsonObject = "";
        try {
            JSONObject object = (JSONObject) parser.parse(body);
            device_id = (String) object.get("device_id");
            jsonObject = ((JSONObject) object.get("value")).toJSONString();
        } catch (ParseException e) {
            sendResponse(httpExchange, 400, "Message Parse Exception", "text/plain; charset=UTF-8");
        }

        try {
            proxyAgent.relayPropertyDelegation(device_id, jsonObject);
        } catch (Exception e) {
            sendResponse(httpExchange, 404, e.getMessage(), "text/plain; charset=UTF-8");
        }
        sendResponse(httpExchange, 200, "OK", "text/plain; charset=UTF-8");
    }

}
