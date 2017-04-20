package edu.utdallas.utdmotorsports.controller;

/**
 * start a simulation of the controller software for pit testing purposes
 */
public class CarSimulator {
    public static void main(String[] args) {
        // start Main with specific simulation files
        String[] pass_args = {"config/simulation/general.prop", "config/simulation/sensor.prop"};
        System.out.println("Values shown are for debugging only and are subject to change.\n");
        Main.main(pass_args);
    }
}
