package esprit.application;

import esprit.handler.GetAllHandler;
import esprit.handler.AddUserHandler;
import esprit.tools.JenaEngine;
import org.apache.jena.rdf.model.*;

import java.io.*;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import static esprit.tools.JenaEngine.uploadToFuseki;

public class Main {
    private static final String FUSEKI_URL = "http://localhost:3030/sante2/data"; // Fuseki endpoint URL

    public static void main(String[] args) {
        try {
            // Load model from ontology
            Model model = JenaEngine.readModel("data/healthDev.rdf");
            if (model != null) {
                // Apply rules on the owlInferencedModel
                Model inferedModel = JenaEngine.readInferencedModelFromRuleFile(model, "data/rules.txt");
                uploadToFuseki(inferedModel,FUSEKI_URL);

                // Create an HTTP server
                HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

                // Handle requests to various endpoints
                server.createContext("/addUser", new AddUserHandler(inferedModel)); // Handle requests to /addUser
                server.createContext("/getHealthActivity", new GetAllHandler(inferedModel, healthActivityFieldNames, "data/healthactivity.txt"));
                server.createContext("/getAllAdmins", new GetAllHandler(inferedModel, userFieldNames, "data/admin.txt"));
                server.createContext("/getAllPractitioners", new GetAllHandler(inferedModel, userFieldNames, "data/practitioner.txt"));
                server.createContext("/getAllExperts", new GetAllHandler(inferedModel, userFieldNames, "data/expert.txt"));
                server.createContext("/getAllUsers", new GetAllHandler(inferedModel, userFieldNames, "data/getAllUsers.txt"));

                server.createContext("/getExpertPlans", new GetAllHandler(inferedModel, planFieldNames, "data/expertPlans.txt"));
                server.createContext("/getUsersAddedByAdmin", new GetAllHandler(inferedModel, userFieldNames, "data/users-added-by-admin.txt"));
                server.createContext("/getExpertsAffectedByAdmin", new GetAllHandler(inferedModel, userFieldNames, "data/experts-added-by-admin.txt"));
                server.createContext("/getGoalsAffectedByExpert", new GetAllHandler(inferedModel, goalFieldNames, "data/goals-affected-by-expert.txt"));
                server.createContext("/getUserHealthActivity", new GetAllHandler(inferedModel, healthActivityFieldNames, "data/users-activities.txt"));

                /*   */
                server.createContext("/getExercisePlan", new GetAllHandler(inferedModel, exercisePlanFieldNames, "data/exerciseplan.txt"));
                server.createContext("/getNutritionPlan", new GetAllHandler(inferedModel, nutritionPlanFieldNames, "data/nutritionplan.txt"));
                server.createContext("/getPlan", new GetAllHandler(inferedModel, planFieldNames, "data/plan.txt"));
                server.createContext("/getGoal", new GetAllHandler(inferedModel, goalFieldNames, "data/goal.txt"));
                server.createContext("/getPromotionCampaign", new GetAllHandler(inferedModel, promotionCampaignFieldNames, "data/promotioncampaign.txt"));
                server.createContext("/getHealthRecord", new GetAllHandler(inferedModel, healthRecordFieldNames, "data/healthrecord.txt"));


                server.setExecutor(null); // creates a default executor

                server.start();

                System.out.println("Server started on port 8081. Send requests to http://localhost:8081");
            } else {
                System.out.println("Error when reading model from ontology");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String[] userFieldNames = {
            "name",
            "email",
            "phone",
            "password",
            "age",
            "weight",
            "targetWeight",
            "height",
            "heartRate",
            "activityLevel",
            "role"
    };

    static String[] healthActivityFieldNames = {
            "type",
            "stepsCount",
            "caloriesBurned",
            "duration",
    };
    static String[] exercisePlanFieldNames = {
            "muscleGroupsTargeted",
            "equipmentRequired"
    };
    static String[] nutritionPlanFieldNames = {
            "caloriesPerDay"
    };
    static String[] planFieldNames = {
            "dietPlanType",
            "intensity",
            "startDate",
            "endDate"
    };
    static String[] goalFieldNames = {
            "target",
            "progress",
            "startDate",
            "endDate"
    };
    static String[] promotionCampaignFieldNames = {
            "budget",
            "startDate",
            "endDate"
    };
    static String[] healthRecordFieldNames = {
            "unit",
            "value"
    };

}
