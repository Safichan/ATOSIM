package org.palladiosimulator.blockchainsystems.core.crashes.recovery;

import java.util.List;

import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;

public class BlockSyncResponse {
	
	private final List<Block> blocks;

    public BlockSyncResponse(List<Block> blocks) {
        this.blocks = blocks;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

}
