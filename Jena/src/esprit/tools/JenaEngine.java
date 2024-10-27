package esprit.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import esprit.model.User;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.util.FileManager;
import java.io.IOException;
import java.io.OutputStream;

public class JenaEngine {
    static private final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    static private final Property RDF_TYPE = ResourceFactory.createProperty(RDF_NS + "type");

    /**
     * Load a model from an OWL file.
     * @param inputDataFile the path to the OWL file.
     * @return the Jena model.
     */
    static public Model readModel(String inputDataFile) {
        // Create an empty model
        Model model = ModelFactory.createDefaultModel();
        // Use the FileManager to find the input file
        InputStream in = FileManager.get().open(inputDataFile);
        if (in == null) {
            System.err.println("Ontology file: " + inputDataFile + " not found");
            return null;
        }
        // Read the RDF/XML file
        model.read(in, "");
        try {
            in.close();
        } catch (IOException e) {
            System.err.println("Error closing input stream: " + e.getMessage());
            return null;
        }
        return model;
    }

    /**
     * Perform inference on the model using rules from a file.
     * @param model the Jena model.
     * @param inputRuleFile the path to the rule file.
     * @return the inferred Jena model.
     */
    static public Model readInferencedModelFromRuleFile(Model model, String inputRuleFile) {
        List<Rule> rules = Rule.rulesFromURL(inputRuleFile);
        if (rules.isEmpty()) {
            System.err.println("Rule File: " + inputRuleFile + " not found or no rules defined");
            return null;
        }
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        reasoner.setDerivationLogging(true);
        reasoner.setOWLTranslation(true); // not needed in RDFS case
        reasoner.setTransitiveClosureCaching(true);
        InfModel inf = ModelFactory.createInfModel(reasoner, model);
        return inf;
    }

    /**
     * Execute a SPARQL query.
     * @param model the Jena model.
     * @param queryString the SPARQL query.
     * @return the result of the query as a String.
     */
    static public String executeQuery(Model model, String queryString) {
        Query query = QueryFactory.create(queryString);
        // Execute the query and obtain results
        try (QueryExecution qe = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qe.execSelect();

            // Use StringBuilder to collect the output manually
            StringBuilder outputBuilder = new StringBuilder();
            // Add a header or metadata if needed
            outputBuilder.append("Results:\n");

            // Iterate through the results and append each solution
            while (results.hasNext()) {
                QuerySolution sol = results.nextSolution();
                outputBuilder.append(sol.toString()).append("\n");
            }

            return outputBuilder.toString(); // Convert StringBuilder to a String
        } catch (Exception e) {
            System.err.println("Error executing query: " + e.getMessage());
            return null;
        }
    }


    /**
     * Execute a SPARQL query from a file.
     * @param model the Jena model.
     * @param filepath the path to the query file.
     * @return the result of the query as a String.
     */
    static public String executeQueryFile(Model model, String filepath) {
        File queryFile = new File(filepath);
        // Use the FileManager to find the input file
        InputStream in = FileManager.get().open(filepath);
        if (in == null) {
            System.err.println("Query file: " + filepath + " not found");
            return null;
        }
        String queryString = FileTool.getContents(queryFile);
        return executeQuery(model, queryString);
    }

    /**
     * Add a user to the RDF model and upload it to Fuseki.
     * @param user the user to add.
     * @param fusekiUrl the Fuseki endpoint URL.
     */
    public static void addUserToModel(User user, String fusekiUrl) {
        // Create a model to add the new user
        Model model = ModelFactory.createDefaultModel();
        String userURI = "http://www.semanticweb.org/health-tracker#" + user.getName().replace(" ", "_");

        // Create RDF for the user
        Resource userResource = model.createResource(userURI)
                .addProperty(RDF_TYPE, model.createResource("http://www.semanticweb.org/health-tracker#User"))
                .addProperty(model.createProperty("http://www.semanticweb.org/health-tracker#age"), String.valueOf(user.getAge()))
                .addProperty(model.createProperty("http://www.semanticweb.org/health-tracker#email"), user.getEmail())
                .addProperty(model.createProperty("http://www.semanticweb.org/health-tracker#height"), String.valueOf(user.getHeight()))
                .addProperty(model.createProperty("http://www.semanticweb.org/health-tracker#name"), user.getName())
                .addProperty(model.createProperty("http://www.semanticweb.org/health-tracker#phone"), user.getPhone())
                .addProperty(model.createProperty("http://www.semanticweb.org/health-tracker#weight"), String.valueOf(user.getWeight()))
                .addProperty(model.createProperty("http://www.semanticweb.org/health-tracker#heartRate"), String.valueOf(user.getHeartRate()))
                .addProperty(model.createProperty("http://www.semanticweb.org/health-tracker#bloodPressure"), user.getBloodPressure())
                .addProperty(model.createProperty("http://www.semanticweb.org/health-tracker#cholesterol"), String.valueOf(user.getCholesterol()))
                .addProperty(model.createProperty("http://www.semanticweb.org/health-tracker#diabetesStatus"), user.getDiabetesStatus());

        // Upload the new user data to Fuseki
        uploadToFuseki(model, fusekiUrl);
    }

    /**
     * Upload the RDF model to a Fuseki server.
     * @param model the Jena model to upload.
     * @param fusekiUrl the Fuseki endpoint URL.
     */
    private static void uploadToFuseki(Model model, String fusekiUrl) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(fusekiUrl).openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/rdf+xml");
            try (OutputStream os = conn.getOutputStream()) {
                model.write(os, "RDF/XML");
            }
            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);
        } catch (IOException e) {
            System.err.println("Error uploading to Fuseki: " + e.getMessage());
        }
    }
}
