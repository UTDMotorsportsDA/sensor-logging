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
        String[] params = fullDataPoint.split("[" + LABEL_DELIMITER + VALUE_DELIMITER + "]");
        if(params[2].charAt(params[2].length() - 1) == '\n') params[2] = params[2].substring(0, params[2].length() - 1);
        this.label = params[0];
        this.value = params[1];
        this.timestamp = Long.parseLong(params[2]);
    }

    public String toString() { return label + LABEL_DELIMITER + value + VALUE_DELIMITER + timestamp; }
    public String getLabel() { return label; }
    public String getValue() { return value; }
    public long getTimestamp() { return timestamp; }

    @Override
    public int compareTo(DataPoint dataPoint) {
        return ((this.timestamp < dataPoint.timestamp) ? -1 : ((this.timestamp == dataPoint.timestamp) ? 0 : 1));
    }
}
