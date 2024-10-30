package kafkarequesttool;

/**
 * An exception encompassing any kind of networking-related exception.
 *
 * @author adam.kotwasinski
 */
public class NetworkLayerException
    extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NetworkLayerException(final String message) {
        super(message);
    }

    public NetworkLayerException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
