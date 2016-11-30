package model;

/**
 * Created by Mateusz on 30.11.2016.
 */
public class RobotPairKey {
    private String robot_id;
    private String pair_key;
    private String creation_date;
    private String expiration_date;

    public RobotPairKey(String robot_id, String creation_date, String pair_key, String expiration_date) {
        this.robot_id = robot_id;
        this.creation_date = creation_date;
        this.pair_key = pair_key;
        this.expiration_date = expiration_date;
    }

    public String getRobot_id() {
        return robot_id;
    }

    public void setRobot_id(String robot_id) {
        this.robot_id = robot_id;
    }

    public String getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(String creation_date) {
        this.creation_date = creation_date;
    }

    public String getPair_key() {
        return pair_key;
    }

    public void setPair_key(String pair_key) {
        this.pair_key = pair_key;
    }

    public String getExpiration_date() {
        return expiration_date;
    }

    public void setExpiration_date(String expiration_date) {
        this.expiration_date = expiration_date;
    }
}