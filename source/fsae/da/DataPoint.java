package fsae.da;

/**
 * Created by brian on 11/15/16.
 */
public final class DataPoint implements Comparable<DataPoint> {
    private final String LABEL_DELIMITER = "=";
    private final String VALUE_DELIMITER = "@";

    private String label;
    private String value;
    private long timestamp;

    public DataPoint(String label, String value, long timestamp) {
        this.label = label;
        this.value = value;
        this.timestamp = timestamp;
    }

    public DataPoint(String fullDataPoint) throws NumberFormatException {
        this.label = fullDataPoint.split(LABEL_DELIMITER)[0];
        this.value = fullDataPoint.substring(label.length() + 1, fullDataPoint.length()).split(VALUE_DELIMITER)[0];
        this.timestamp = Integer.parseInt(fullDataPoint.substring(label.length() + value.length() + 2), fullDataPoint.length());
    }

    public String toString() {
        return label + LABEL_DELIMITER + value + VALUE_DELIMITER + timestamp;
    }

    @Override
    public int compareTo(DataPoint dataPoint) {
        return ((this.timestamp < dataPoint.timestamp) ? -1 : ((this.timestamp == dataPoint.timestamp) ? 0 : 1));
    }
}
