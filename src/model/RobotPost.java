package model;

import enums.Constructions;
import enums.RobotSystem;

/**
 * Created by Mateusz on 29.11.2016.
 */
public class RobotPost {

    private String serial_number;
    private RobotSystem current_system;
    private int lego_construction;
    private Constructions robot_model;

    public RobotPost(String serial_number, RobotSystem current_system, int lego_construction, Constructions robot_model) {
        this.serial_number = serial_number;
        this.current_system = current_system;
        this.lego_construction = lego_construction;
        this.robot_model = robot_model;
    }

    public String getSerial_number() {
        return serial_number;
    }

    public void setSerial_number(String serial_number) {
        this.serial_number = serial_number;
    }

    public RobotSystem getCurrent_system() {
        return current_system;
    }

    public void setCurrent_system(RobotSystem current_system) {
        this.current_system = current_system;
    }

    public int getLego_construction() {
        return lego_construction;
    }

    public void setLego_construction(int lego_construction) {
        this.lego_construction = lego_construction;
    }

    public Constructions getRobot_model() {
        return robot_model;
    }

    public void setRobot_model(Constructions robot_model) {
        this.robot_model = robot_model;
    }
}
