package esprit.application;

import esprit.model.User;
import esprit.service.GetAllUsers;
import esprit.tools.JenaEngine;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Main {
    private static final String FUSEKI_URL = "http://localhost:3030/sante/data"; // Fuseki endpoint URL

    public static void main(String[] args) {
        try {
            // Load model from ontology
            Model model = JenaEngine.readModel("data/healthDev.rdf");
            if (model != null) {
                // Apply rules on the owlInferencedModel
                Model inferedModel = JenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");
                uploadToFuseki(inferedModel);

                // Create an HTTP server
                HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

                // Handle requests to various endpoints
                server.createContext("/addUser", new AddUserHandler(inferedModel)); // Handle requests to /addUser
                server.createContext("/queryHeartRate", new SPARQLHandler(inferedModel, "data/heartRate.txt"));
                server.createContext("/queryBloodPressure", new SPARQLHandler(inferedModel, "data/bloodPressure.txt"));
                server.createContext("/queryHighHeartRateAndBloodPressure", new SPARQLHandler(inferedModel, "data/highHeartRateAndBloodPressure.txt"));
                server.createContext("/getAllUsers", new AllUsersHandler(inferedModel)); // Use the new handler for getting all users
                server.setExecutor(null); // creates a default executor
                server.createContext("/test", new SimpleHandler());

                server.start();

                System.out.println("Server started on port 8080. Send requests to http://localhost:8080");
            } else {
                System.out.println("Error when reading model from ontology");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class AddUserHandler implements HttpHandler {
        private final Model inferedModel;

        public AddUserHandler(Model inferedModel) {
            this.inferedModel = inferedModel;
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

    // New SPARQL Handler for getting all users
    static class AllUsersHandler implements HttpHandler {
        private final GetAllUsers getAllUsersService;

        public AllUsersHandler(Model inferedModel) {
            this.getAllUsersService = new GetAllUsers(inferedModel);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String results = getAllUsersService.getAllUsers();
                exchange.sendResponseHeaders(200, results.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(results.getBytes());
                }
            } else {
                String response = "Method Not Allowed";
                exchange.sendResponseHeaders(405, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }


    static class SPARQLHandler implements HttpHandler {
        private final Model inferedModel;
        private final String queryFilePath;

        public SPARQLHandler(Model inferedModel, String queryFilePath) {
            this.inferedModel = inferedModel;
            this.queryFilePath = queryFilePath;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                // Read the SPARQL query from the file
                String queryString = readQueryFromFile(queryFilePath);
                if (queryString != null) {
                    // Execute the query and get results
                    String results = executeQuery(queryString);
                    exchange.sendResponseHeaders(200, results.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(results.getBytes());
                    }
                } else {
                    String response = "Query not found";
                    exchange.sendResponseHeaders(404, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            } else {
                String response = "Method Not Allowed";
                exchange.sendResponseHeaders(405, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }

        private String readQueryFromFile(String filePath) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(filePath)))) {
                StringBuilder queryBuilder = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    queryBuilder.append(line).append("\n");
                }
                return queryBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        private String executeQuery(String queryString) {
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, inferedModel);
            StringBuilder resultsBuilder = new StringBuilder();

            try {
                ResultSet results = qexec.execSelect();
                // Use ByteArrayOutputStream to capture JSON output
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ResultSetFormatter.outputAsJSON(outputStream, results);
                resultsBuilder.append(outputStream.toString("UTF-8")); // Convert to String with UTF-8 encoding
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            } finally {
                qexec.close();
            }

            return resultsBuilder.toString();
        }
    }

    static class SimpleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Welcome to the Health Monitoring Service!";
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    private static void uploadToFuseki(Model model) {
        try {
            // Write model to RDF/XML
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            model.write(outputStream, "RDF/XML");
            String rdfData = outputStream.toString("UTF-8");

            // Send POST request to Fuseki
            URL url = new URL(FUSEKI_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/rdf+xml");
            connection.setRequestProperty("Accept", "application/json");

            try (OutputStream os = connection.getOutputStream()) {
                os.write(rdfData.getBytes("UTF-8"));
                os.flush();
            }

            // Check the response
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("Error uploading data to Fuseki: " + connection.getResponseMessage());
            } else {
                System.out.println("Data uploaded successfully to Fuseki.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
