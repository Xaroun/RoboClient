package model;

import enums.Constructions;

import java.util.List;

/**
 * Created by Mateusz on 30.11.2016.
 */
public class RobotConstructionResponse {

    private String id;
    private Constructions robot_model;
    private String name;
    private String info;
    private List<String> config;

    //SPRAWDZIC CZY ABY NA PEWNO CONFIG ZAPISAĆ DO LISTY

//    [
//    {
//        "id": "1",
//            "robot_model": "EV3",
//            "name": "Construction#00001",
//            "info": "This construction is for game Tic Tao Doe",
//            "config": {
//                "id": "1",
//                "requirements": "{requirements : in json}",
//                "config": "{config in json}"
//    }
//    }
//    ]
}
