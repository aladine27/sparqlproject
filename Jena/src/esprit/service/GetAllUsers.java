package esprit.service;

import esprit.model.User;
import esprit.tools.JenaEngine;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GetAllUsers {
    private final Model inferredModel;

    public GetAllUsers(Model inferredModel) {
        this.inferredModel = inferredModel;
    }

    public String getAllUsers() throws IOException {
        String queryString = readQueryFromFile("data/users/getall.txt");

        List<User> users = new ArrayList<>();
        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, inferredModel)) {
            ResultSet results = qexec.execSelect();
            System.out.println(results);
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                String individual = solution.getResource("individual").getURI();
                String name = solution.getLiteral("name").getString();
                String age = solution.getLiteral("age").getString();
                String email = solution.getLiteral("email").getString();
                String height = solution.getLiteral("height").getString();
                String weight = solution.getLiteral("weight").getString();
                String heartRate = solution.getLiteral("heartRate").getString();
                String bloodPressure = solution.getLiteral("bloodPressure").getString();
                String cholesterol = solution.getLiteral("cholesterol").getString();
                String diabetesStatus = solution.getLiteral("diabetesStatus").getString();

                // Create a User object (you may want to adapt your User class accordingly)
                users.add(new User(name, Integer.parseInt(age), email, Integer.parseInt(height), null,
                        Double.parseDouble(weight), Integer.parseInt(heartRate),
                        bloodPressure, Integer.parseInt(cholesterol), diabetesStatus));
            }
        }

        return formatUsersToJson(users);
    }

    private String readQueryFromFile(String filePath) {
        StringBuilder queryBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                queryBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception as needed
        }
        return queryBuilder.toString();
    }

    private String formatUsersToJson(List<User> users) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{ \"users\": [");
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            jsonBuilder.append("{")
                    .append("\"individual\": \"").append(user.getIndividualUri()).append("\", ")
                    .append("\"name\": \"").append(user.getName()).append("\", ")
                    .append("\"age\": \"").append(user.getAge()).append("\", ")
                    .append("\"email\": \"").append(user.getEmail()).append("\", ")
                    .append("\"height\": \"").append(user.getHeight()).append("\", ")
                    .append("\"weight\": \"").append(user.getWeight()).append("\", ")
                    .append("\"heartRate\": \"").append(user.getHeartRate()).append("\", ")
                    .append("\"bloodPressure\": \"").append(user.getBloodPressure()).append("\", ")
                    .append("\"cholesterol\": \"").append(user.getCholesterol()).append("\", ")
                    .append("\"diabetesStatus\": \"").append(user.getDiabetesStatus()).append("\"")
                    .append("}");
            if (i < users.size() - 1) {
                jsonBuilder.append(", ");
            }
        }
        jsonBuilder.append("]}");
        return jsonBuilder.toString();
    }
}
