import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Gauge;

/**
 * NoOp mapper function which acts as a pass through for the records. It would also publish the custom metric for
 * the number of received records.
 */
class NoOpMapperFunction extends RichMapFunction<String, String> {
    private transient final int valueToExpose;
    private transient final String customMetricName;

    public NoOpMapperFunction(final String customMetricName, int valueToExpose) {
        this.customMetricName = customMetricName;
        this.valueToExpose = valueToExpose;
    }

    @Override
    public void open(Configuration config) {
        getRuntimeContext().getMetricGroup()
                .addGroup("kinesisanalytics")
                .addGroup("Program", "Flink-Operator-Parallelism")
                .addGroup("Capacity")
                .gauge(customMetricName, (Gauge<Integer>) () -> valueToExpose);
    }

    @Override
    public String map(String value) throws Exception {
        return value;
    }
}
