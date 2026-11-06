package org.palladiosimulator.blockchainsystems.core.mining;

import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;
import org.palladiosimulator.blockchainsystems.core.common.BlockchainNodeObject;
import org.palladiosimulator.blockchainsystems.core.common.abstractions.Event;
import org.palladiosimulator.blockchainsystems.core.stochastics.PoissonProcess;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.MiningProcess;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

public class MiningProcessImpl extends BlockchainNodeObject implements MiningProcess {

    private final PoissonProcess _poissonProcess;

    private BiFunction<Long, String, Block> _onCreatingBlockCallback;
    private Supplier<String> _previousBlockSelectionCallback;
    private Consumer<Block> _onBlockMinedCallback;

    private boolean _isMining = false;
    private boolean _isMiningEnabled = true;

    public MiningProcessImpl(double meanBlockTime, RandomGenerator randomGenerator) {
        _poissonProcess = new PoissonProcess(1.0 / meanBlockTime, randomGenerator);
    }

    @Override
    public void dispatchEvent(Event event) {
        if (!BlockMinedEvent.EVENT_TYPE.equals(event.getEventType())) return;
        if (!_isMining) return;

        BlockMinedEvent blockMinedEvent = (BlockMinedEvent) event;

        if (_onCreatingBlockCallback == null) return;
        Block block = _onCreatingBlockCallback.apply(
                blockMinedEvent.getOccurrenceTime(),
                blockMinedEvent.getPreviousBlockHash()
        );
        if (block == null) return;

        logBlockMined(block);
        notifyBlockMined(block);
        scheduleNewBlockMinedEvent();
    }

    private String scheduleNewBlockMinedEvent() {
        if (_previousBlockSelectionCallback == null) {
            throw new IllegalStateException("Previous block selection callback must be set before scheduling a new block mined event.");
        }
        String previousBlockHash = _previousBlockSelectionCallback.get();

        getSimulationContext().getEventCoordinator().raiseEvent(
                new BlockMinedEvent(
                        getNextBlockMinedEventOccurrenceTimestamp(),
                        previousBlockHash,
                        this
                )
        );
        return previousBlockHash;
    }

    private long getNextBlockMinedEventOccurrenceTimestamp() {
        long eventCurrentTimeOffset = _poissonProcess.nextPointDistance();
        return getSimulationContext().getSystemClock().getCurrentTime() + eventCurrentTimeOffset;
    }

    private void notifyBlockMined(Block block) {
        if (_onBlockMinedCallback != null) _onBlockMinedCallback.accept(block);
    }

    private void cancelPendingEvent() {
        getSimulationContext().getEventCoordinator().cancelEventsFor(this);
    }

    @Override
    public void startMining() {
    	if (!_isMiningEnabled) return;
        if (_isMining) return;
        scheduleNewBlockMinedEvent();
        _isMining = true;
        logMiningStarted();
    }

    @Override
    public void restartMining() {
    	if (!_isMiningEnabled) return;
        if (!_isMining) {
            startMining();
            return;
        }
        cancelPendingEvent();
        String previousHash = scheduleNewBlockMinedEvent();
        logMiningRestarted(previousHash);
    }

    @Override
    public void stopMining() {
        if (!_isMining) return;
        cancelPendingEvent();
        _isMining = false;
        logMiningStopped();
    }

    @Override
    public void setOnCreatingBlockCallback(BiFunction<Long, String, Block> onCreatingBlockCallback) {
        _onCreatingBlockCallback = onCreatingBlockCallback;
    }

    @Override
    public void setPreviousBlockSelectionCallback(Supplier<String> previousBlockSelectionCallback) {
        _previousBlockSelectionCallback = previousBlockSelectionCallback;
    }

    @Override
    public void setOnBlockMinedCallback(Consumer<Block> onBlockMinedCallback) {
        _onBlockMinedCallback = onBlockMinedCallback;
    }

    private void logMiningStarted() {
        if (!getTraceEventLogger().isEventTypeEnabled(BlockMiningStartedTraceEvent.EVENT_TYPE)) return;
        getTraceEventLogger().logEvent(new BlockMiningStartedTraceEvent(
                getSimulationContext().getSystemClock().getCurrentTime()));
    }

    private void logBlockMined(Block block) {
        if (!getTraceEventLogger().isEventTypeEnabled(BlockMinedTraceEvent.EVENT_TYPE)) return;
        getTraceEventLogger().logEvent(new BlockMinedTraceEvent(
                getSimulationContext().getSystemClock().getCurrentTime(), block));
    }

    private void logMiningRestarted(String previousHash) {
        if (!getTraceEventLogger().isEventTypeEnabled(BlockMiningRestartedTraceEvent.EVENT_TYPE)) return;
        getTraceEventLogger().logEvent(new BlockMiningRestartedTraceEvent(
                getSimulationContext().getSystemClock().getCurrentTime(), previousHash));
    }

    private void logMiningStopped() {
        if (!getTraceEventLogger().isEventTypeEnabled(BlockMiningStoppedTraceEvent.EVENT_TYPE)) return;
        getTraceEventLogger().logEvent(new BlockMiningStoppedTraceEvent(
                getSimulationContext().getSystemClock().getCurrentTime()));
    }
    
    @Override
    public void setIsMiningEnabled(boolean isMiningEnabled) {
    	_isMiningEnabled = isMiningEnabled;
    }
}
