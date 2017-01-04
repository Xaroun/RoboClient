package utils;

import enums.Constructions;
import enums.RobotSystem;
import model.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;
import java.util.LinkedList;

/**
 * Created by Mateusz on 06.12.2016.
 */
public class Pairing {

    private int responseCode;
    private LinkedList<RobotConstruction> listOfRobotConstructions;
    private RobotInfo robotInfo;
    private RobotPairKey robotPairKey;
    private boolean isDebugMode = true;
    private JsonUtils jsonUtils;
    private HttpQueries httpQueries;
    private RobotInfoWithStatus robotInfoWithStatus;

    public Pairing(String newRobotSn) {
        this.jsonUtils = new JsonUtils();
        this.httpQueries = new HttpQueries(responseCode);

        init(newRobotSn);
    }

    private void init(String newRobotSn) {
        try {
            boolean wasLoggedBefore = checkIfLoggedBefore(newRobotSn, isDebugMode);
            if (wasLoggedBefore) {
                checkConstruction(Constructions.EV3, isDebugMode);
                loginRobot();
            } else {
                checkConstruction(Constructions.EV3, isDebugMode);
                RobotRegister robotRegister = prepareRegistrationBody(newRobotSn, listOfRobotConstructions);
                registerRobot(robotRegister, isDebugMode);
            }
            getPairKey(robotInfoWithStatus.getRobot_id(), isDebugMode);
            System.out.println("\n PAIR KEY IS: " + robotPairKey.getPair_key());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void loginRobot() {
        String url = "http://s396393.vm.wmi.amu.edu.pl/api/robots/" + robotInfo.getRobot_id() + "/login";
        System.out.println("Logging.. ");
        System.out.println(url);

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add request header
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept", Headers.ACCEPT);
            con.setRequestProperty("Content-Type", Headers.CONTENT_TYPE);

            String body = "{\n" +
                    "\"id\": \"" + robotInfo.getRobot_id() + "\",\n" +
                    "\"current_system\": \"" + robotInfo.getCurrent_system() + "\",\n" +
                    "\"lego_construction\": \"" + robotInfo.getCurrent_lego_construction().getId() + "\",\n" +
                    "\"robot_model\": \"" + robotInfo.getRobot_model() + "\",\n" +
                    "\"robot_ip\": \"" + getIp()+ "\"\n" +
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
            robotInfoWithStatus = jsonUtils.infoWithStatusJsonToObject(response.toString());

            if(isDebugMode) {
                System.out.println(jsonUtils.parseJson(response.toString(), false));
            }

        } catch (Exception e) {
            switch(responseCode) {
                case 400:
                    System.err.println("Problem");
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


            robotPairKey = jsonUtils.pairKeyJsonToObject(response.toString());

            if(isDebugMode) {
                System.out.println(jsonUtils.parseJson(response.toString(), false));
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

    @Deprecated
    private void checkStatusByRobotId(String robot_id, boolean isDebugMode) {
        String url = "http://s396393.vm.wmi.amu.edu.pl/api/robots/" + robot_id+ "/status";
        System.out.println("Checking status by id.. " + robot_id);

        String response = httpQueries.doGETquery(url, isDebugMode);
        robotInfo = jsonUtils.statusJsonToObject(response);

        if(isDebugMode) {
            System.out.println(jsonUtils.parseJson(response, false));
        }
    }

    private RobotRegister prepareRegistrationBody(String robotSn, LinkedList<RobotConstruction> listOfRobotConstructions) {

        for (RobotConstruction robotConstruction : listOfRobotConstructions) {
            if(robotConstruction.getId().equals(getConstructionBasedOnSensors())) {
                RobotRegister robotRegister = new RobotRegister(robotSn, RobotSystem.LEJOS, robotConstruction.getId(), robotConstruction.getRobot_model(), getIp());
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

        String response = httpQueries.doGETquery(url, isDebugMode);
        listOfRobotConstructions = jsonUtils.constructionJsonToObject(response);

        if(isDebugMode) {
            System.out.println(jsonUtils.parseJson(response, true));
        }
    }

    private boolean checkIfLoggedBefore(String robotSerialNumber, boolean isDebugMode) {

        String url = "http://s396393.vm.wmi.amu.edu.pl/api/robots/" + robotSerialNumber+ "/me";
        System.out.println("Checking if logged before..");
        String response = httpQueries.doGETquery(url, isDebugMode);

        if(response == null || response.equals(null)) {
            return false;
        }

        robotInfo = jsonUtils.statusJsonToObject(response);

        if(isDebugMode) {
            System.out.println(jsonUtils.parseJson(response, false));
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
            con.setRequestProperty("Accept", Headers.ACCEPT);
            con.setRequestProperty("Content-Type", Headers.CONTENT_TYPE);

            String body = "{\n" +
                    "\"serial_number\": \"" + robotRegister.getSerial_number() + "\",\n" +
                    "\"current_system\": \"" + robotRegister.getCurrent_system() + "\",\n" +
                    "\"lego_construction\": \"" + robotRegister.getLego_construction() + "\",\n" +
                    "\"robot_model\": \"" + robotRegister.getRobot_model() + "\",\n" +
                    "\"robot_ip\": \"" + robotRegister.getRobot_ip() + "\"\n" +
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
            robotInfoWithStatus = jsonUtils.infoWithStatusJsonToObject(response.toString());

            if(isDebugMode) {
                System.out.println(jsonUtils.parseJson(response.toString(), false));
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
