package org.palladiosimulator.blockchainsystems.core.propagation;

import org.palladiosimulator.blockchainsystems.core.common.abstractions.Traceable;
import org.palladiosimulator.blockchainsystems.core.crashes.recovery.P2PMessageHandler;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.BlockchainSystemNodeContext;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.NodeP2PNetworkInterface;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.P2PNetworkEndpoint;

import java.util.Set;
import java.util.function.Consumer;

public interface PropagationStrategy<E extends Propagatable> extends Traceable, P2PMessageHandler {
    void distribute(E element);
    void distribute(E element, Set<P2PNetworkEndpoint> neighborEndpoints);
    void setNetworkInterface(NodeP2PNetworkInterface networkInterface);
    void setBlockchainSystemNodeContext(BlockchainSystemNodeContext context);
    void setOnReceivedCallback(Consumer<E> onReceivedCallback);
    void requestElement(String elementId, P2PNetworkEndpoint peer);
}
