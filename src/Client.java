import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;

/**
 * Created by Mateusz on 11.05.2016.
 */
public class Client {

    public static void main(String args[]) {
        Client client = new Client();
        client.sendPost();
    }

    private void sendPost() {
        String url = "http://192.168.2.6:8080/robots";

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
