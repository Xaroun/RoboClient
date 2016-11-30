import com.google.gson.*;
import enums.Constructions;
import enums.RobotSystem;
import model.RobotConstruction;
import model.RobotRegister;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;
import java.util.LinkedList;

/**
 * Created by Mateusz on 11.05.2016.
 */
public class Client {

    private int responseCode;
    private static final String ACCEPT_HEADER = "application/vnd.roboapp.v1+json";
    private static final String CONTENT_TYPE_HEADER = "application/vnd.roboapp.v1+json";
//    private RobotRegister robotRegister;
    private LinkedList<RobotConstruction> listOfRobotConstructions;


    public static void main(String args[]) {
        Client client = new Client();
        client.init();
    }

    private void init() {
        String existingRobotSn = "4e86790f-b2ab-4945-aaa7-2db83d12b740";
        String newRobotSn = "3f681ded-0d86-4403-af6a-2c0d23ffc664";

        boolean wasLoggedBefore = checkIfLoggedBefore(newRobotSn);

        if(wasLoggedBefore) {
            checkConstruction(Constructions.EV3);

        } else {
            checkConstruction(Constructions.EV3);
            RobotRegister robotRegister = prepareRegistrationBody(newRobotSn, listOfRobotConstructions);
            registerRobot(robotRegister);
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

    private void checkConstruction(Constructions construction) {
        String url = "http://s396393.vm.wmi.amu.edu.pl/api/robots/config/" + construction.toString();
        System.out.println("Checking construction..");

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestProperty("Accept", ACCEPT_HEADER);
            con.setRequestProperty("Content-Type", CONTENT_TYPE_HEADER);

            responseCode = con.getResponseCode();

            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));

            String inputLine;
            StringBuffer response = new StringBuffer();


            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();


            listOfRobotConstructions = constructionJsonToObject(response.toString());
            System.out.println(parseJson(response.toString(), true));

        } catch (Exception e) {
            e.printStackTrace();
            switch(responseCode) {
                case 404:
                    System.err.println("Robot with this uuid wasn't logged before");
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

    private boolean checkIfLoggedBefore(String robotSerialNumber) {

        String url = "http://s396393.vm.wmi.amu.edu.pl/api/robots/" + robotSerialNumber+ "/me";
        System.out.println("Checking if logged before..");

            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();


                responseCode = con.getResponseCode();

                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));

                String inputLine;
                StringBuffer response = new StringBuffer();


                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                System.out.println(parseJson(response.toString(), false));


            } catch (Exception e) {
                switch(responseCode) {
                    case 404:
                        System.err.println("Robot with this uuid wasn't logged before");
                        return false;
                    case 500:
                        System.err.println("Check your Internet connection.");
                        return false;
                    default:
                        System.err.println("Something went wrong.");
                        return false;
                }
            }
        return true;
    }

    private void registerRobot(RobotRegister robotRegister) {
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

            System.out.println("Post parameters : \n" + body);
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
