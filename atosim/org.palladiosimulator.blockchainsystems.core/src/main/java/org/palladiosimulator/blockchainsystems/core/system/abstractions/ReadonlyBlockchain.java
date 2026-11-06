package org.palladiosimulator.blockchainsystems.core.system.abstractions;

import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;
import org.palladiosimulator.blockchainsystems.core.blockchain.ChainReorganizedTraceEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface ReadonlyBlockchain {
    Set<Block> getBlocks();
    Set<Block> getLastBlocksOfLongestChains();

    /**
     * Among the tips of the current longest chain(s) (an equal-work tie whenever more than one
     * exists), returns the one that completed validation and was appended to this node's local
     * view earliest -- i.e. deterministic first-validated-wins tie-breaking, never unordered
     * collection iteration.
     */
    Block getPreferredTipOfLongestChains();

    Set<Block> getBlocksAtPosition(long position);
    long getPositionOfBlock(Block block);
    long getLength();
    boolean hasBlockWithHash(String hash);
    Set<String> getPreferredChainBlockHashes();
    Block getBlock(String hash);
    Set<Block> getImmediateSuccessorBlocks(String hash);
    Set<Block> getSuccessorBlocks(String hash);
    long getLongestSuccessorChainLength(String hash);
    List<ArrayList<Block>> getLongestChains();

    /**
     * Returns the most recent chain reorganization (the canonical chain being replaced by a
     * strictly longer competing branch), or {@code null} if none has occurred yet.
     */
    ChainReorganizedTraceEvent getLastChainReorganization();
}
