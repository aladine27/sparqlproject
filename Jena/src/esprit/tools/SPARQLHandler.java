package esprit.tools;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;

import java.io.*;

public class SPARQLHandler implements HttpHandler {
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