package utils;

import com.google.gson.*;
import model.RobotConstruction;
import model.RobotPairKey;
import model.RobotStatus;

import java.util.LinkedList;

/**
 * Created by Mateusz on 06.12.2016.
 */
public class JsonUtils {


    public RobotPairKey pairKeyJsonToObject(String rawJson) {
        JsonParser parser = new JsonParser();
        Gson gson = new Gson();

        JsonElement robotStatusElement = parser.parse(rawJson).getAsJsonObject();
        RobotPairKey robotPairKey = gson.fromJson(robotStatusElement, RobotPairKey.class);

        return robotPairKey;
    }

    public RobotStatus statusJsonToObject(String rawJson) {
        JsonParser parser = new JsonParser();
        Gson gson = new Gson();

        JsonElement robotStatusElement = parser.parse(rawJson).getAsJsonObject();
        RobotStatus robotStatus = gson.fromJson(robotStatusElement, RobotStatus.class);

        return robotStatus;
    }

    public String parseJson(String rawJson, boolean isArray) {
        if(isArray) {
            JsonParser parser = new JsonParser();
            JsonArray consctructionJson = parser.parse(rawJson.toString()).getAsJsonArray();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(consctructionJson);
        } else {
            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(rawJson).getAsJsonObject();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(object);
        }
    }

    public LinkedList<RobotConstruction> constructionJsonToObject(String rawJson) {
        JsonParser parser = new JsonParser();
        Gson gson = new Gson();

        JsonArray robotConstructionArray = parser.parse(rawJson).getAsJsonArray();
        LinkedList<RobotConstruction> robotConstructions = new LinkedList<>();
        for (JsonElement robotConstruction : robotConstructionArray) {
            RobotConstruction aRobotConstruction = gson.fromJson(robotConstruction, RobotConstruction.class);
            robotConstructions.add(aRobotConstruction);
        }

        return robotConstructions;
    }
}