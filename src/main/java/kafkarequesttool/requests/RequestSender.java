package kafkarequesttool.requests;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Set;

import org.apache.kafka.common.message.ApiMessageType;
import org.apache.kafka.common.protocol.ApiKeys;
import org.apache.kafka.common.protocol.ApiMessage;
import org.apache.kafka.common.protocol.ByteBufferAccessor;
import org.apache.kafka.common.protocol.Errors;
import org.apache.kafka.common.protocol.MessageSizeAccumulator;
import org.apache.kafka.common.protocol.ObjectSerializationCache;
import org.apache.kafka.common.requests.AbstractResponse;
import org.apache.kafka.common.requests.RequestHeader;
import org.apache.kafka.common.requests.ResponseHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kafkarequesttool.BrokerConfig;

/**
 * @author adam.kotwasinski
 */
public class RequestSender {

    private static final Logger LOG = LoggerFactory.getLogger(RequestSender.class);

    private RequestSender() {
    }

    public static void sendRequest(final ApiKeys apiKey, final BrokerConfig broker)
            throws Exception {

        final SocketAddress address = new InetSocketAddress(broker.host, broker.port);
        try (SocketChannel channel = SocketChannel.open(address)) {

            final short version = 0;
            LOG.info("Sending {}/{}", apiKey, version);
            generateAndSendRequest(channel, apiKey, version, 0);

            LOG.info("Receiving {}", apiKey);
            receiveResponse(channel, apiKey, version);
        }
    }

    private static void generateAndSendRequest(final SocketChannel channel,
                                               final ApiKeys apiKey,
                                               final short version,
                                               final int correlationId)
            throws Exception {

        final ApiMessageType apiMessageType = ApiMessageType.fromApiKey(apiKey.id);
        final ApiMessage data = apiMessageType.newRequest();
        final RequestHeader header = new RequestHeader(apiKey, version, "", correlationId);

        final ByteBuffer bytes = toBytes(header, data);
        channel.write(bytes);
    }

    // === MISC ========================================================================================================

    private static ByteBuffer toBytes(final RequestHeader header, final ApiMessage data) {

        final short headerVersion = header.headerVersion();
        final short apiVersion = header.apiVersion();

        final ObjectSerializationCache serializationCache = new ObjectSerializationCache();

        final MessageSizeAccumulator ms = new MessageSizeAccumulator();
        header.data().addSize(ms, serializationCache, headerVersion);
        data.addSize(ms, serializationCache, apiVersion);

        final ByteBuffer bb = ByteBuffer.allocate(ms.sizeExcludingZeroCopy() + 4);
        final ByteBufferAccessor bba = new ByteBufferAccessor(bb);

        bba.writeInt(ms.totalSize());
        header.data().write(bba, serializationCache, headerVersion);
        data.write(bba, serializationCache, apiVersion);

        bb.flip();

        return bb;
    }

    private static Set<Errors> receiveResponse(final SocketChannel channel, final ApiKeys apiKey, final short version) {
        final ByteBuffer data = ReadHelper.receive(channel);
        final ResponseHeader header = ResponseHeader.parse(data, apiKey.responseHeaderVersion(version));
        final AbstractResponse response = AbstractResponse.parseResponse(apiKey, data, version);
        LOG.info("Response: {}/{}", header, response);
        return response.errorCounts().keySet();
    }

}
