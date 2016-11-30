package fsae.da;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by brian on 11/29/16.
 */
// needed to control timing delays to smooth out error
public final class PIDLoop {
    // tuning parameters
    private double P_GAIN, I_GAIN, D_GAIN;
    private static final int DEFAULT_BUFFER_SIZE = 10; // store 10 inputs by default
    private Queue<Double> inputBuffer = new ArrayDeque<>();
    private double setPoint;

    public PIDLoop(double pGain, double iGain, double dGain) {
        this(pGain, iGain, dGain, 0.0);
    }

    public PIDLoop(double pGain, double iGain, double dGain, double setPoint) {

    }

    public void setDesiredOutput(double setPoint) {

    }

    public double getOutput(double input) {

    }

    public void clearBuffer() {

    }
}
