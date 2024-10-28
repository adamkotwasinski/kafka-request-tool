package kafkarequesttool;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.JCommander.Builder;

import kafkarequesttool.requests.ApiVersions;
import kafkarequesttool.requests.Invocable;
import kafkarequesttool.requests.Metadata;

/**
 * Main entry point.
 *
 * @author adam.kotwasinski
 */
public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args)
            throws Exception {

        final BrokerConfig brokerConfig = new BrokerConfig();
        final Builder builder = JCommander.newBuilder().addObject(brokerConfig);

        final List<Class<? extends Invocable>> requestClasses = List.of(
                Metadata.class,
                ApiVersions.class);

        final Map<String, Invocable> commands = new TreeMap<>();
        for (final Class<?> requestClass : requestClasses) {
            final String name = requestClass.getSimpleName().toLowerCase();
            final Invocable request = (Invocable) requestClass.getConstructor().newInstance();
            commands.put(name, request);
            builder.addCommand(name, request);
        }

        final JCommander parser = builder.build();

        parser.parse(args);
        final String command = parser.getParsedCommand();
        LOG.info("Invoking command [{}]", command);

        final Invocable invocable = commands.get(command);
        invocable.invoke(brokerConfig);
        LOG.info("Finished successfully");
    }

}
