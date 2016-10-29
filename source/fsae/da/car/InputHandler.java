package fsae.da.car;

import java.time.Duration;

public final class InputHandler {
    // no instantiation allowed
    private InputHandler() { }

    private static Sensor getSensorByName(String name, Sensor[] sensors) {
        for(Sensor s : sensors)
            if(s.getLabel().equals(name))
                return s;

        return null;
    }

    public static void ohShit(Sensor[] sensors) {
        for(Sensor s : sensors)
            s.critical = true;
    }

    public static void zzz(Sensor[] sensors) {
        for(Sensor s : sensors)
            s.critical = false;
    }

    public static void toggleCriticalState(char c, Sensor[] sensors) {
        try {
            switch (c) {
                case '0':
                    getSensorByName("accelerometer_vector", sensors).critical = !getSensorByName("accelerometer_vector", sensors).critical;
                    break;
                case '1':
                    getSensorByName("gyroscope_vector", sensors).critical = !getSensorByName("gyroscope_vector", sensors).critical;
                    break;
                case '2':
                    getSensorByName("magnetometer", sensors).critical = !getSensorByName("magnetometer", sensors).critical;
                    break;
                case '3':
                    getSensorByName("engine_rpm", sensors).critical = !getSensorByName("engine_rpm", sensors).critical;
                    break;
                case '4':
                    getSensorByName("throttle_position", sensors).critical = !getSensorByName("throttle_position", sensors).critical;
                    break;
                case '5':
                    getSensorByName("brake_pressure_front", sensors).critical = !getSensorByName("brake_pressure_front", sensors).critical;
                    break;
                case '6':
                    getSensorByName("brake_pressure_rear", sensors).critical = !getSensorByName("brake_pressure_rear", sensors).critical;
                    break;
                case '7':
                    getSensorByName("steering_angle", sensors).critical = !getSensorByName("steering_angle", sensors).critical;
                    break;
                case '8':
                    getSensorByName("oil_pressure", sensors).critical = !getSensorByName("oil_pressure", sensors).critical;
                    break;
                case '9':
                    getSensorByName("engine_coolant_temperature", sensors).critical = !getSensorByName("engine_coolant_temperature", sensors).critical;
                    break;
                case 'a':
                    getSensorByName("vehicle_speed", sensors).critical = !getSensorByName("vehicle_speed", sensors).critical;
                    break;
                case 'b':
                    getSensorByName("tire_air_pressure", sensors).critical = !getSensorByName("tire_air_pressure", sensors).critical;
                    break;
                case 'c':
                    getSensorByName("tire_temperature", sensors).critical = !getSensorByName("tire_temperature", sensors).critical;
                    break;
                case 'd':
                    getSensorByName("intake_air_temperature", sensors).critical = !getSensorByName("intake_air_temperature", sensors).critical;
                    break;
                case 'e':
                    getSensorByName("body_roll_distances", sensors).critical = !getSensorByName("body_roll_distances", sensors).critical;
                    break;
                case 'f':
                    getSensorByName("battery_voltage", sensors).critical = !getSensorByName("battery_voltage", sensors).critical;
                    break;
                case 'g':
                    getSensorByName("gear_selection", sensors).critical = !getSensorByName("gear_selection", sensors).critical;
                    break;
                default:
                    break;
            }
        } catch (NullPointerException e) { System.err.println("sensor name match failed"); }
    }
}

