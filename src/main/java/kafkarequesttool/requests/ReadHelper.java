package kafkarequesttool.requests;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

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
        try {
            return rh.read(channel);
        }
        catch (final IOException e) {
            throw new IllegalArgumentException("IO failure", e);
        }
    }

    private ByteBuffer read(final SocketChannel channel)
            throws IOException {

        readFullBuffer(channel, this.hb);

        this.hb.flip();
        final int dataLength = this.hb.getInt();

        // Empty response has very little sense.
        if (dataLength <= 0) {
            throw new IOException("Invalid response data length: " + dataLength);
        }

        this.db = ByteBuffer.allocate(dataLength);
        readFullBuffer(channel, this.db);

        this.db.flip();
        return this.db;
    }

    private static void readFullBuffer(final SocketChannel channel, final ByteBuffer target)
            throws IOException {

        while (target.hasRemaining()) {
            final int read = channel.read(target);
            if (-1 == read) {
                // End of stream.
                break;
            }
        }
        if (target.hasRemaining()) {
            throw new IOException("Buffer underflow");
        }
    }

}
