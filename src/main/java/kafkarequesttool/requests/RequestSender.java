package kafkarequesttool.requests;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.kafka.common.protocol.ApiKeys;
import org.apache.kafka.common.protocol.ApiMessage;
import org.apache.kafka.common.protocol.ByteBufferAccessor;
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

    private static final String CLIENT_ID = "kafka-request-tool";
    private static final int CORRELATION_ID = 0; // We use connection per request so CID does not matter.

    private RequestSender() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractResponse> T sendRequest(final ApiMessage message,
                                                             final short version,
                                                             final BrokerConfig broker)
            throws Exception {

        final ApiKeys apiKey = ApiKeys.forId(message.apiKey());
        final SocketAddress address = new InetSocketAddress(broker.host, broker.port);
        try (SocketChannel channel = SocketChannel.open(address)) {

            LOG.info("Sending {}/{} to {}", apiKey, version, broker);
            generateAndSendRequest(channel, message, version, CORRELATION_ID);

            LOG.info("Receiving {}", apiKey);
            return (T) receiveResponse(channel, apiKey, version);
        }
    }

    private static void generateAndSendRequest(final SocketChannel channel,
                                               final ApiMessage message,
                                               final short version,
                                               final int correlationId)
            throws Exception {

        final ApiKeys apiKey = ApiKeys.forId(message.apiKey());
        final RequestHeader header = new RequestHeader(apiKey, version, CLIENT_ID, correlationId);
        final ByteBuffer bytes = toBytes(header, message);
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

    private static AbstractResponse receiveResponse(final SocketChannel channel,
                                                    final ApiKeys apiKey,
                                                    final short version) {

        final ByteBuffer data = ReadHelper.receive(channel);
        @SuppressWarnings("unused") // Need to consume the var-len bytes present inside.
        final ResponseHeader header = ResponseHeader.parse(data, apiKey.responseHeaderVersion(version));
        final AbstractResponse response = AbstractResponse.parseResponse(apiKey, data, version);
        return response;
    }

}
