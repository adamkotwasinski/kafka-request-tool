package kafkarequesttool.requests;

import static kafkarequesttool.requests.RequestSender.sendRequest;

import java.util.List;

import org.apache.kafka.common.message.ApiVersionsRequestData;
import org.apache.kafka.common.message.MetadataRequestData;
import org.apache.kafka.common.message.MetadataRequestData.MetadataRequestTopic;
import org.apache.kafka.common.protocol.ApiKeys;
import org.apache.kafka.common.requests.ApiVersionsResponse;
import org.apache.kafka.common.requests.MetadataResponse;
import org.apache.kafka.common.requests.MetadataResponse.TopicMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;

import kafkarequesttool.BrokerConfig;

/**
 * Handles METADATA request.
 *
 * @author adam.kotwasinski
 */
public class Metadata
    implements Invocable {

    private static final Logger LOG = LoggerFactory.getLogger(Metadata.class);

    @Parameter(variableArity = true)
    private List<String> topics;

    @Override
    public void invoke(final BrokerConfig brokerConfig)
            throws Exception {

        final ApiVersionsResponse avResponse = sendRequest(
                new ApiVersionsRequestData(), ApiKeys.API_VERSIONS.oldestVersion(), brokerConfig);
        final short maxMetadataVersion = avResponse.apiVersion(ApiKeys.METADATA.id).maxVersion();

        final MetadataRequestData data = new MetadataRequestData();
        if (null != this.topics) {
            data.setTopics(this.topics.stream().map(x -> new MetadataRequestTopic().setName(x)).toList());
        }
        else {
            // This is meaningful - all topics will be returned then (as opposite to empty list).
            data.setTopics(null);
        }
        final MetadataResponse response = sendRequest(data, maxMetadataVersion, brokerConfig);
        for (final TopicMetadata topicMetadata : response.topicMetadata()) {
            LOG.info("Topic: {} / {}", topicMetadata.topic(), topicMetadata.topicId());
            LOG.info("Partitions: {}", topicMetadata.partitionMetadata().size());
        }
    }

}
