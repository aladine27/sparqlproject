package esprit.service;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;

import java.util.ArrayList;
import java.util.List;

public class GetHeartRateService {

    private static final String RDF_DATA_PATH = "path/to/your/data.rdf"; // Specify the path to your RDF data file

    public List<HeartRateResult> getHeartRates() {
        List<HeartRateResult> results = new ArrayList<>();

        // Load RDF data into a model
        Model model = ModelFactory.createDefaultModel();
        FileManager.get().readModel(model, RDF_DATA_PATH);

        // Define the SPARQL query
        String queryString =
                "PREFIX ns: <http://www.semanticweb.org/health-tracker#>\n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "SELECT ?individual ?heartRate\n" +
                        "WHERE {\n" +
                        "  ?individual rdf:type ns:User .\n" +
                        "  ?individual ns:heartRate ?heartRate .\n" +
                        "  FILTER(?heartRate > 80)\n" +
                        "}";

        // Create the query
        Query query = QueryFactory.create(queryString);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet resultSet = qexec.execSelect();
            while (resultSet.hasNext()) {
                QuerySolution solution = resultSet.nextSolution();
                String individual = solution.getResource("individual").getURI();
                int heartRate = solution.getLiteral("heartRate").getInt();
                results.add(new HeartRateResult(individual, heartRate));
            }
        }

        return results;
    }

    // Inner class to hold results
    public static class HeartRateResult {
        private String individual;
        private int heartRate;

        public HeartRateResult(String individual, int heartRate) {
            this.individual = individual;
            this.heartRate = heartRate;
        }

        public String getIndividual() {
            return individual;
        }

        public int getHeartRate() {
            return heartRate;
        }
    }
}
