package esprit.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import esprit.service.GetAllEntities;
import org.apache.jena.rdf.model.Model;

import java.io.IOException;
import java.io.OutputStream;

public class GetAllHandler implements HttpHandler {

    private final GetAllEntities getAllEntitiesService;

    private final String filePath;
    public GetAllHandler(Model inferedModel, String[] fields, String filePath) {

        this.getAllEntitiesService = new GetAllEntities(inferedModel,fields);
        this.filePath = filePath;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:4200");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); // Return 204 status for OPTIONS preflight requests
            return;
        }

        if ("GET".equals(exchange.getRequestMethod())) {
            String results = getAllEntitiesService.getAllEntities(filePath);
            System.out.println(results);
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