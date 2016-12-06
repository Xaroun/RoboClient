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
        String newRobotSn = "63d198c1-0361-4a7c-80d2-41a65c0b5e4c";

        //PAIR NEW ROBOT
        new Pairing(newRobotSn);




    }


}
