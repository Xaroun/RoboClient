import com.google.gson.*;
import enums.Constructions;
import enums.RobotSystem;
import model.RobotConstruction;
import model.RobotPairKey;
import model.RobotRegister;
import model.RobotStatus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;
import java.util.LinkedList;

/**
 * Created by Mateusz on 11.05.2016.
 */
public class Client{

    private int responseCode;
    private static final String ACCEPT_HEADER = "application/vnd.roboapp.v1+json";
    private static final String CONTENT_TYPE_HEADER = "application/vnd.roboapp.v1+json";
    private LinkedList<RobotConstruction> listOfRobotConstructions;
    private RobotStatus robotStatus;
    private RobotPairKey robotPairKey;
    private boolean isDebugMode = false;


    public static void main(String args[]) {
        Client client = new Client();
        client.init();
    }

    private void init() {
        String newRobotSn = "8892acef-8345-483b-a431-49e7abd9f0bf";
        String existingRobotSn = "3f681ded-0d86-4403-af6a-2c0d23ffc664";

        boolean wasLoggedBefore = checkIfLoggedBefore(newRobotSn, isDebugMode);

        if(wasLoggedBefore) {
            checkConstruction(Constructions.EV3, isDebugMode);
        } else {
            checkConstruction(Constructions.EV3, isDebugMode);
            RobotRegister robotRegister = prepareRegistrationBody(newRobotSn, listOfRobotConstructions);
            registerRobot(robotRegister, isDebugMode);
        }

        checkStatusByRobotId(robotStatus.getRobot_id(), isDebugMode);
        getPairKey(robotStatus.getRobot_id(), isDebugMode);

        System.out.println("\n PAIR KEY IS: " + robotPairKey.getPair_key());
    }

    private void getPairKey(String robot_id, boolean isDebugMode) {
        String url = "http://s396393.vm.wmi.amu.edu.pl/api/tinder/virgins/" + robot_id;
        System.out.println("Getting pairkey.. ");

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            responseCode = con.getResponseCode();

            if(isDebugMode) {
                System.out.println("Response Code : " + responseCode);
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));

            String inputLine;
            StringBuffer response = new StringBuffer();


            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();


            robotPairKey = pairKeyJsonToObject(response.toString());

            if(isDebugMode) {
                System.out.println(parseJson(response.toString(), false));
            }

        } catch (Exception e) {
            switch(responseCode) {
                case 404:
                    System.err.println("Robot with this uuid not found");
                    break;
                case 500:
                    System.err.println("Check your Internet connection.");
                    break;
                default:
                    System.err.println("Something went wrong.");
                    break;
            }
        }
    }

    private RobotPairKey pairKeyJsonToObject(String rawJson) {
        JsonParser parser = new JsonParser();
        Gson gson = new Gson();

        JsonElement robotStatusElement = parser.parse(rawJson).getAsJsonObject();
        RobotPairKey robotPairKey = gson.fromJson(robotStatusElement, RobotPairKey.class);

        return robotPairKey;
    }

    private RobotStatus statusJsonToObject(String rawJson) {
        JsonParser parser = new JsonParser();
        Gson gson = new Gson();

        JsonElement robotStatusElement = parser.parse(rawJson).getAsJsonObject();
        RobotStatus robotStatus = gson.fromJson(robotStatusElement, RobotStatus.class);

        return robotStatus;
    }


    private void checkStatusByRobotId(String robot_id, boolean isDebugMode) {
        String url = "http://s396393.vm.wmi.amu.edu.pl/api/robots/" + robot_id+ "/status";
        System.out.println("Checking status by id.. " + robot_id);

        String response = doGETquery(url, isDebugMode);
        robotStatus = statusJsonToObject(response.toString());

        if(isDebugMode) {
            System.out.println(parseJson(response.toString(), false));
        }
    }

    private RobotRegister prepareRegistrationBody(String robotSn, LinkedList<RobotConstruction> listOfRobotConstructions) {

        for (RobotConstruction robotConstruction : listOfRobotConstructions) {
            if(robotConstruction.getId().equals(getConstructionBasedOnSensors())) {
                RobotRegister robotRegister = new RobotRegister(robotSn, RobotSystem.LEJOS, robotConstruction.getId(), robotConstruction.getRobot_model());
                return robotRegister;
            }
        }
        return null;
    }

    private String getConstructionBasedOnSensors() {
        //THIS METHOD CHECK ROBOT CONSTRUCTION BASED ON SENSORS
        return "1";
    }

    private void checkConstruction(Constructions construction, boolean isDebugMode) {
        String url = "http://s396393.vm.wmi.amu.edu.pl/api/robots/config/" + construction.toString();
        System.out.println("Checking construction..");

        String response = doGETquery(url, isDebugMode);
        listOfRobotConstructions = constructionJsonToObject(response.toString());

        if(isDebugMode) {
            System.out.println(parseJson(response.toString(), true));
        }
    }

    private String doGETquery(String url, boolean isDebugMode) {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestProperty("Accept", ACCEPT_HEADER);
            con.setRequestProperty("Content-Type", CONTENT_TYPE_HEADER);

            responseCode = con.getResponseCode();

            if (isDebugMode) {
                System.out.println("Response Code : " + responseCode);
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));

            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } catch (Exception e) {
            switch(responseCode) {
                case 404:
                    System.err.println("Robot with this uuid wasn't logged before");
                    return null;
                case 500:
                    System.err.println("Check your Internet connection.");
                    return null;
                default:
                    System.err.println("Something went wrong.");
                    return null;
            }
        }
    }

    private LinkedList<RobotConstruction> constructionJsonToObject(String rawJson) {
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

    private boolean checkIfLoggedBefore(String robotSerialNumber, boolean isDebugMode) {

        String url = "http://s396393.vm.wmi.amu.edu.pl/api/robots/" + robotSerialNumber+ "/me";
        System.out.println("Checking if logged before..");
        String response = doGETquery(url, isDebugMode);

        if(response.equals(null) || response == null) {
            return false;
        }

        robotStatus = statusJsonToObject(response.toString());

        if(isDebugMode) {
            System.out.println(parseJson(response.toString(), false));
        }

        return true;
    }

    private void registerRobot(RobotRegister robotRegister, boolean isDebugMode) {
        String url = "http://s396393.vm.wmi.amu.edu.pl/api/robots/register";

        System.out.println("Registering... " + robotRegister.getSerial_number());
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add request header
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept", ACCEPT_HEADER);
            con.setRequestProperty("Content-Type", CONTENT_TYPE_HEADER);

            String body = "{\n" +
                    "\"serial_number\": \"" + robotRegister.getSerial_number() + "\",\n" +
                    "\"current_system\": \"" + robotRegister.getCurrent_system() + "\",\n" +
                    "\"lego_construction\": \"" + robotRegister.getLego_construction() + "\",\n" +
                    "\"robot_model\": \"" + robotRegister.getRobot_model() + "\"\n" +
                    "}";

            //send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(body);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();

            if(isDebugMode) {
                System.out.println("Post parameters : \n" + body);
                System.out.println("Response Code : " + responseCode);
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            robotStatus = statusJsonToObject(response.toString());

            if(isDebugMode) {
                System.out.println(parseJson(response.toString(), false));
            }

        } catch (Exception e) {
            switch(responseCode) {
                case 400:
                    System.err.println("This serial number is already used");
                    break;
                case 500:
                    System.err.println("Check your Internet connection.");
                    break;
                default:
                    System.err.println("Something went wrong.");
                    break;
            }
        }
    }

    private String parseJson(String rawJson, boolean isArray) {
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

    private void sendOldPostWithIp() {
        String url = "http://127.0.0.1:5000/robots";

        try {
//            System.out.println(Inet4Address.getLocalHost().getHostAddress());
            String ip = getIp();
//            System.out.println(getIp());


            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add request header
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Content-Type", "application/json");

//            String urlParameters = "{\n" +
//                    "    \"ip\": \"178.206.211.09\",\n" +
//                    "    \"sn\": \"EV3POZNAN\"\n" +
//                    "}";

            String urlParameters = "{\n" +
                    "    \"ip\": \"" + ip + "\",\n" +
                    "    \"sn\": \"EV3POZ\"\n" +
                    "}";

//            System.out.println(urlParameters);


            //send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Post parameters : " + urlParameters);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println(response.toString());



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getIp() {
        String ip = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual() || iface.isPointToPoint())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    ip = addr.getHostAddress();
//                    System.out.println(iface.getDisplayName() + " " + ip);
                    if(Inet4Address.class == addr.getClass()) return ip;
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return ip;
    }

}
