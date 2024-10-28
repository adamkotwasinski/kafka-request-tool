package kafkarequesttool.requests;

import org.apache.kafka.common.protocol.ApiKeys;

import kafkarequesttool.BrokerConfig;

/**
 * Handles API_VERSIONS request.
 *
 * @author adam.kotwasinski
 */
public class ApiVersions
    implements Invocable {

    @Override
    public void invoke(final BrokerConfig brokerConfig)
            throws Exception {

        RequestSender.sendRequest(ApiKeys.API_VERSIONS, brokerConfig);
    }

}
