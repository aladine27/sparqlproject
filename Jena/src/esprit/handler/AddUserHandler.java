package esprit.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import esprit.model.User;
import esprit.tools.JenaEngine;
import org.apache.jena.rdf.model.Model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.stream.Collectors;

public class AddUserHandler implements HttpHandler {
    private static final String FUSEKI_URL = "http://localhost:3030/sante2/data"; // Fuseki endpoint URL

    public AddUserHandler(Model inferedModel) {
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            // Read the request body
            String jsonData = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                    .lines().collect(Collectors.joining("\n"));

            System.out.println("Received data: " + jsonData); // Debugging line

            // Parse the user information from the request body (manual JSON parsing)
            User user = parseUserFromJson(jsonData);
            System.out.println("Parsed User: " + user); // Debugging line

            // Add user to model and upload to Fuseki
            JenaEngine.addUserToModel(user, FUSEKI_URL);

            // Send a response
            String response = "User added successfully!";
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } else {
            String response = "Method Not Allowed";
            exchange.sendResponseHeaders(405, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    private User parseUserFromJson(String jsonData) {
        String name = extractValue(jsonData, "name");
        int age = Integer.parseInt(extractValue(jsonData, "age"));
        String email = extractValue(jsonData, "email");
        int height = Integer.parseInt(extractValue(jsonData, "height"));
        String phone = extractValue(jsonData, "phone");
        double weight = Double.parseDouble(extractValue(jsonData, "weight"));
        int heartRate = Integer.parseInt(extractValue(jsonData, "heartRate"));
        String bloodPressure = extractValue(jsonData, "bloodPressure");
        int cholesterol = Integer.parseInt(extractValue(jsonData, "cholesterol"));
        String diabetesStatus = extractValue(jsonData, "diabetesStatus");

        return new User(name, age, email, height, phone, weight, heartRate, bloodPressure, cholesterol, diabetesStatus);
    }

    private String extractValue(String jsonData, String key) {
        String keyValuePair = "\"" + key + "\":";
        int startIndex = jsonData.indexOf(keyValuePair) + keyValuePair.length();
        int endIndex = jsonData.indexOf(",", startIndex);
        if (endIndex == -1) {
            endIndex = jsonData.indexOf("}", startIndex);
        }
        return jsonData.substring(startIndex, endIndex).replaceAll("\"", "").trim();
    }
}