package org.palladiosimulator.blockchainsystems.threesim.simulation;

import org.palladiosimulator.blockchainsystems.core.simulation.MonteCarloSimulationParameters;
import org.palladiosimulator.blockchainsystems.core.simulation.abstractions.MonteCarloSimulation;
import org.palladiosimulator.blockchainsystems.core.simulation.abstractions.MonteCarloSimulationProgressMonitor;
import org.palladiosimulator.blockchainsystems.core.simulation.logoutputs.abstractions.LogOutputProvider;
import org.palladiosimulator.blockchainsystems.threesim.creation.ThreesimBlockchainSystemFactory;
import org.palladiosimulator.blockchainsystems.threesim.metrics.GeographicalDiversity;
import org.palladiosimulator.blockchainsystems.threesim.metrics.NakamotoCoefficient;
import org.palladiosimulator.blockchainsystems.threesim.metrics.utils.OutputMetricsSet;
import org.palladiosimulator.blockchainsystems.threesim.simulation.results.ThreesimMonteCarloSimulationResult;
import org.palladiosimulator.blockchainsystems.threesim.simulation.results.ThreesimSimulationRoundResult;

import java.util.List;
import java.util.stream.Collectors;

public class ThreesimMonteCarloSimulation
        extends MonteCarloSimulation<ThreesimSimulationRoundResult> {

    private final ThreesimBlockchainSystemFactory _blockchainSystemFactory;
    private final LogOutputProvider _logOutputProvider;
    private final long _maxAllowedBlockchainLength;
    private final MonteCarloSimulationParameters _simulationParameters;
    private final ThreesimSimulationParameters _threesimSimulationParameters;

    public ThreesimMonteCarloSimulation(
            MonteCarloSimulationProgressMonitor progressMonitor,
            ThreesimBlockchainSystemFactory blockchainSystemFactory,
            LogOutputProvider logOutputProvider,
            long maxAllowedBlockchainLength,
            MonteCarloSimulationParameters simulationParameters,
            ThreesimSimulationParameters threesimSimulationParameters) {
        super(simulationParameters.getNumberOfMonteCarloRounds(),
                simulationParameters.getMonteCarloParallelism(), progressMonitor);
        _blockchainSystemFactory = blockchainSystemFactory;
        _logOutputProvider = logOutputProvider;
        _maxAllowedBlockchainLength = maxAllowedBlockchainLength;
        _simulationParameters = simulationParameters;
        _threesimSimulationParameters = threesimSimulationParameters;
    }
    
    // Simulation divided in multiple rounds
    @Override
    public ThreesimSimulationRoundResult performSimulationRound(int replicationId) {
        return new ThreesimSimulationRound(
                _blockchainSystemFactory,
                _logOutputProvider.getLogOutputs(),
                _maxAllowedBlockchainLength,
                _threesimSimulationParameters,
                replicationId
        ).run();
    }
    
    // Get output metrics etc. and return a result object
    @Override
    public ThreesimMonteCarloSimulationResult createSimulationResultFromRoundResults(
            List<ThreesimSimulationRoundResult> results) {

        ThreesimSimulationRoundResult generalResults = new ThreesimSimulationRoundResult(
                results.get(0).getOutputMetrics().stream()
                        .filter(m -> m instanceof NakamotoCoefficient || m instanceof GeographicalDiversity)
                        .collect(Collectors.toCollection(OutputMetricsSet::new)),
                List.of());

        List<ThreesimSimulationRoundResult> roundResults = results.stream()
                .map(r -> new ThreesimSimulationRoundResult(
                        r.getOutputMetrics().stream()
                                .filter(m -> !(m instanceof NakamotoCoefficient) && !(m instanceof GeographicalDiversity))
                                .collect(Collectors.toCollection(OutputMetricsSet::new)),
                        r.getChainReorganizations(),
                        r.getTopologyDeterminismInfo(),
                        r.getEpisodeStatus(),
                        r.getDecisiveAttackerReorg()))
                .collect(Collectors.toList());

        return new ThreesimMonteCarloSimulationResult(
                _simulationParameters,
                _threesimSimulationParameters,
                generalResults,
                roundResults);
    }
}
