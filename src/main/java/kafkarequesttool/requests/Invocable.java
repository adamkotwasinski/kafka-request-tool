package kafkarequesttool.requests;

import kafkarequesttool.BrokerConfig;

/**
 * @author adam.kotwasinski
 */
public interface Invocable {

    void invoke(BrokerConfig brokerConfig)
            throws Exception;

}
