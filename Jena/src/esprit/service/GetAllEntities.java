package esprit.service;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetAllEntities {
    private final Model inferredModel;
    private final String[] fieldNames;
    public GetAllEntities(Model inferredModel,String[] fields) {
        this.inferredModel = inferredModel;
        this.fieldNames = fields;
    }

    public String getAllEntities(String queryFilePath) throws IOException {
        String queryString = readQueryFromFile(queryFilePath);
        List<Map<String, Object>> entities = new ArrayList<>();

        Query query = QueryFactory.create(queryString);

        try (QueryExecution qexec = QueryExecutionFactory.create(query, inferredModel)) {
            ResultSet results = qexec.execSelect();

            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                Map<String, Object> entity = new HashMap<>();

                for (String field : fieldNames) {
                    if (solution.contains(field)) {
                        entity.put(field, getFieldValue(solution, field));
                    }
                }
                entities.add(entity);
            }
        }
        return formatEntitiesToJson(entities);
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

    private Object getFieldValue(QuerySolution solution, String field) {
        if (solution.get(field).isLiteral()) {
            return solution.getLiteral(field).getValue();
        } else if (solution.get(field).isResource()) {
            return solution.getResource(field).getURI();
        }
        return null;
    }

    private String formatEntitiesToJson(List<Map<String, Object>> entities) {

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{ \"entities\": [");

        for (int i = 0; i < entities.size(); i++) {
            Map<String, Object> entity = entities.get(i);
            jsonBuilder.append("{");
            int j = 0;
            for (Map.Entry<String, Object> entry : entity.entrySet()) {
                jsonBuilder.append("\"").append(entry.getKey()).append("\": \"").append(entry.getValue()).append("\"");
                if (j < entity.size() - 1) {
                    jsonBuilder.append(", ");
                }
                j++;
            }
            jsonBuilder.append("}");
            if (i < entities.size() - 1) {
                jsonBuilder.append(", ");
            }
        }

        jsonBuilder.append("]}");

        System.out.println(jsonBuilder.toString());
        return jsonBuilder.toString();
    }
}

