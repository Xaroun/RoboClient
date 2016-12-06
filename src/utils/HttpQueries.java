package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Mateusz on 06.12.2016.
 */
public class HttpQueries {

    public int responseCode;

    public HttpQueries(int responseCode) {
        this.responseCode = responseCode;
    }

    public String doGETquery(String url, boolean isDebugMode) {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestProperty("Accept", Headers.ACCEPT);
            con.setRequestProperty("Content-Type", Headers.CONTENT_TYPE);

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
}
