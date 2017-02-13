package fsae.da;

import fsae.da.car.CarMain;

/**
 * start a simulation of the car software for pit testing purposes
 */
public class CarSimulator {
    public static void main(String[] args) {
        // start CarMain with specific simulation files
        String[] pass_args = {"config/simulation/general.prop", "config/simulation/sensors.prop"};
        CarMain.main(pass_args);
    }
}
