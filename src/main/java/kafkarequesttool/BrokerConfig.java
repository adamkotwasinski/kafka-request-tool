package kafkarequesttool;

import com.beust.jcommander.Parameter;

/**
 * Container for Kafka host / port.
 *
 * @author adam.kotwasinski
 */
public class BrokerConfig {

    @Parameter(names = { "host", "h" },
               description = "Kafka broker host, e.g. localhost")
    public String host;

    @Parameter(names = { "port", "p" },
               description = "Kafka broker port, e.g. 9092")
    public int port;

    @Override
    public String toString() {
        return String.format("[%s, %s]", this.host, this.port);
    }

}
