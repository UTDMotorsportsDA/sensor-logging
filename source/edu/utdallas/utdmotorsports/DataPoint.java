package edu.utdallas.utdmotorsports;

/**
 * nicer packaging for data
 */
public final class DataPoint implements Comparable<DataPoint> {
    private final String VALUE_DELIMITER = "=";
    private final String TIMESTAMP_DELIMITER = "@";
    private final String CRITICAL_DELIMITER = "!";

    private String label;
    private String value;
    private long timestamp;
    private boolean critical;

    public DataPoint(String label, String value, long timestamp, boolean critical) {
        this.label = label;
        this.value = value;
        this.timestamp = timestamp;
        this.critical = critical;
    }

    /**
     * Create a DataPoint object from its string representation.
     *
     * @param fullDataPoint
     * @throws NumberFormatException
     */
    public DataPoint(String fullDataPoint) throws NumberFormatException {
        String[] params = fullDataPoint.split("[" + VALUE_DELIMITER + TIMESTAMP_DELIMITER + CRITICAL_DELIMITER + "]");
        if(params[2].charAt(params[2].length() - 1) == '\n') params[2] = params[2].substring(0, params[2].length() - 1);
        this.label = params[0];
        this.value = params[1];
        this.timestamp = Long.parseLong(params[2]);
        this.critical = params[3].equals("critical"); // false if "OK", true if "critical"
    }

    public String toString() {
        return label + VALUE_DELIMITER + value + TIMESTAMP_DELIMITER + timestamp + CRITICAL_DELIMITER + (critical ? "critical" : "OK"); }
    public String getLabel() { return label; }
    public String getValue() { return value; }
    public long getTimestamp() { return timestamp; }
    public boolean getCritical() { return critical; }

    @Override
    public int compareTo(DataPoint dataPoint) {
        return ((this.timestamp < dataPoint.timestamp) ? -1 : ((this.timestamp == dataPoint.timestamp) ? 0 : 1));
    }
}
