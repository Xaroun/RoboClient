import utils.Pairing;

/**
 * Created by Mateusz on 11.05.2016.
 */
public class Client{

    public static void main(String args[]) {
        Client client = new Client();
        client.init();
    }

    private void init() {
        String newRobotSn = "3a4cd9b6-0685-4813-acfc-424519fb4d45";

        //PAIR NEW ROBOT
        new Pairing(newRobotSn);




    }


}
