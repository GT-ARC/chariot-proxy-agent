package com.gtarc.chariot.proxyagent;

import com.sun.net.httpserver.HttpExchange;
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

public class MyHttpServer {

    static final int PORT = 8080;
    private ProxyAgent proxyAgent;

    MyHttpServer(ProxyAgent proxyAgent) {
        this.proxyAgent = proxyAgent;
        try
        {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            createServerContext(server);
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

        httpExchange.getResponseHeaders().add("Content-Type", contentType);
        httpExchange.sendResponseHeaders(code, response.length);

        OutputStream out = httpExchange.getResponseBody();
        out.write(response);
        out.close();
    }

    private void receiveSendRequest(HttpExchange httpExchange) throws IOException {
        String body = "";
        try {
            StringWriter writer = new StringWriter();
            IOUtils.copy(httpExchange.getRequestBody(), writer, Charset.defaultCharset());
            body = writer.toString();
        } catch (IOException e) {
            sendResponse(httpExchange, 400, "Message Parse Exception", "text/plain; charset=UTF-8");
        }

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
    }

}
