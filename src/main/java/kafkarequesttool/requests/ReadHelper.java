package kafkarequesttool.requests;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import kafkarequesttool.NetworkLayerException;

/**
 * Reads a response (its 4-byte length header and N following bytes).
 *
 * @author adam.kotwasinski
 */
public class ReadHelper {

    private static final int HEADER_SIZE = 4;

    public final ByteBuffer hb;
    public ByteBuffer db;

    private ReadHelper() {
        this.hb = ByteBuffer.allocate(HEADER_SIZE);
        this.db = null;
    }

    public static ByteBuffer receive(final SocketChannel channel) {
        final ReadHelper rh = new ReadHelper();
        return rh.read(channel);
    }

    private ByteBuffer read(final SocketChannel channel) {

        readFullBuffer(channel, this.hb);

        this.hb.flip();
        final int dataLength = this.hb.getInt();

        // Empty response has very little sense.
        if (dataLength <= 0) {
            throw new NetworkLayerException("Invalid response data length: " + dataLength);
        }

        this.db = ByteBuffer.allocate(dataLength);
        readFullBuffer(channel, this.db);

        this.db.flip();
        return this.db;
    }

    private static void readFullBuffer(final SocketChannel channel, final ByteBuffer target) {
        while (target.hasRemaining()) {
            try {
                final int read = channel.read(target);
                if (-1 == read) { // End of stream.
                    break;
                }
            }
            catch (final IOException e) {
                throw new NetworkLayerException("Read failure", e);
            }
        }
        if (target.hasRemaining()) {
            throw new NetworkLayerException("Buffer underflow");
        }
    }

}
