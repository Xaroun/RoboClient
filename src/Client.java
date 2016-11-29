import com.google.gson.*;
import enums.Constructions;
import model.RobotPost;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;

/**
 * Created by Mateusz on 11.05.2016.
 */
public class Client {

    private int responseCode;
    private static final String ACCEPT_HEADER = "application/vnd.roboapp.v1+json";
    private static final String CONTENT_TYPE_HEADER = "application/vnd.roboapp.v1+json";
    private RobotPost robot;


    public static void main(String args[]) {
        Client client = new Client();
        client.init();
    }

    private void init() {
        String existingRobot = "4e86790f-b2ab-4945-aaa7-2db83d12b740";
        String newRobot = "3f681ded-0d86-4403-af6a-2c0d23ffc664";

        boolean wasLoggedBefore = checkIfLoggedBefore(newRobot);

        if(wasLoggedBefore) {
            checkConstruction(Constructions.EV3);

        } else {
            checkConstruction(Constructions.EV3);
            registerRobot(newRobot, robot);
        }
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

            Gson gson = new Gson();
            robot = gson.fromJson(response.toString(), RobotPost.class);

            System.out.println(parseJson(response.toString(), true));

        } catch (Exception e) {
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

    private void registerRobot(String robotSerialNumber, RobotPost robot) {
        String url = "http://s396393.vm.wmi.amu.edu.pl/api/robots/register";

        System.out.println(robot.getCurrent_system());
        System.out.println(robot.getLego_construction());
        System.out.println(robot.getRobot_model());
        System.out.println(robot.getSerial_number());

//        System.out.println("Registering " + robotSerialNumber);
//        try {
//
//            URL obj = new URL(url);
//            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//
//            //add request header
//            con.setRequestMethod("POST");
//            con.setRequestProperty("Accept", ACCEPT_HEADER);
//            con.setRequestProperty("Content-Type", CONTENT_TYPE_HEADER);
//
//            String body = "{\n" +
//                    "\"serial_number\": \"" + robotSerialNumber + "\",\n" +
//                    "\"current_system\": \"LEJOS\"\n" +
//                    "\"lego_construction\": \"1\"\n" +
//                    "\"robot_model\": \"EV3\"\n" +
//                    "}";
//
//            System.out.println(body);
//
//
//            //send post request
//            con.setDoOutput(true);
//            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
//            wr.writeBytes(body);
//            wr.flush();
//            wr.close();
//
//            int responseCode = con.getResponseCode();
//
//            System.out.println("Post parameters : " + body);
//            System.out.println("Response Code : " + responseCode);
//
//            BufferedReader in = new BufferedReader(
//                    new InputStreamReader(con.getInputStream()));
//            String inputLine;
//            StringBuffer response = new StringBuffer();
//
//            while ((inputLine = in.readLine()) != null) {
//                response.append(inputLine);
//            }
//            in.close();
//
//            //print result
//            System.out.println(response.toString());
//
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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

    private void sendPost() {
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


    private void sendNewPost() {
        String myRobotId = "1234";
        String myGameId = "TIC_TAC_TOE";

        String url = "http://127.0.0.1:5000/api/robots/" + myRobotId + "/games/" + myGameId + "/new";

        try {

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

            String body = "{\n" +
                                "\"ip\": \"" + "aaaaaa" + "\",\n" +
                                "\"sn\": \"EV3POZ\"\n" +
                           "}";

//            System.out.println(urlParameters);


            //send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(body);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Post parameters : " + body);
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
}
