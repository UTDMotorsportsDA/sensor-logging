package fsae.da;

import fsae.da.car.CarMain;

/**
 * start a simulation of the car software for pit testing purposes
 */
public class CarSimulator {
    public static void main(String[] args) {
        // start CarMain with specific simulation files
        String[] pass_args = {"config/simulation/general.prop", "config/simulation/sensor.prop"};
        System.out.println("Values shown are for debugging only and are subject to change.\n");
        CarMain.main(pass_args);
    }
}
