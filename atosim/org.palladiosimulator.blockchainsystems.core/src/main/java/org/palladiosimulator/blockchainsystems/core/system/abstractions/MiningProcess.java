package org.palladiosimulator.blockchainsystems.core.system.abstractions;

import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;
import org.palladiosimulator.blockchainsystems.core.common.abstractions.Traceable;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface MiningProcess extends Traceable {
    void setOnBlockMinedCallback(Consumer<Block> onBlockMinedCallback);
    void setOnCreatingBlockCallback(BiFunction<Long, String, Block> onCreatingBlockCallback);
    void setPreviousBlockSelectionCallback(Supplier<String> previousBlockSelectionCallback);
    void startMining();
    void restartMining();
    void stopMining();
    void setIsMiningEnabled(boolean isMiningEnabled);
}
