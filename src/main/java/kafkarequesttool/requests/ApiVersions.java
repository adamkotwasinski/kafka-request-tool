package kafkarequesttool.requests;

import static kafkarequesttool.requests.RequestSender.sendRequest;

import org.apache.kafka.common.message.ApiVersionsRequestData;
import org.apache.kafka.common.message.ApiVersionsResponseData.ApiVersion;
import org.apache.kafka.common.message.ApiVersionsResponseData.ApiVersionCollection;
import org.apache.kafka.common.protocol.ApiKeys;
import org.apache.kafka.common.requests.ApiVersionsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kafkarequesttool.BrokerConfig;

/**
 * Handles API_VERSIONS request.
 *
 * @author adam.kotwasinski
 */
public class ApiVersions
    implements Invocable {

    private static final Logger LOG = LoggerFactory.getLogger(ApiVersions.class);

    @Override
    public void invoke(final BrokerConfig brokerConfig)
            throws Exception {

        final ApiVersionsResponse response = sendRequest(
                new ApiVersionsRequestData(), ApiKeys.API_VERSIONS.oldestVersion(), brokerConfig);
        final ApiVersionCollection apiVersions = response.data().apiKeys();
        for (final ApiVersion av : apiVersions) {
            LOG.info("{}: {}..{}", ApiKeys.forId(av.apiKey()), av.minVersion(), av.maxVersion());
        }
    }

}
