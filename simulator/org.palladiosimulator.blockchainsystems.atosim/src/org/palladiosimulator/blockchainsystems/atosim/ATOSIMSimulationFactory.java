package org.palladiosimulator.blockchainsystems.atosim;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;

import org.palladiosimulator.blockchainsystems.bscm.attackmodel.Attack;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.AttackScenario;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.AttackerNode;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.EqualForkStubbornAttack;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.FinneyAttack;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.LeadStubbornAttack;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.MajorityAttack;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.RaceAttack;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.SelfishMiningAttack;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.TrailStubbornAttack;
import org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.BlockchainSystem;
import org.palladiosimulator.blockchainsystems.bscm.p2pnetwork.ConnectedSubgraphsNetworkTopology;
import org.palladiosimulator.blockchainsystems.bscm.p2pnetwork.ExplicitNetworkTopology;
import org.palladiosimulator.pcm.core.entity.Entity;

import org.palladiosimulator.blockchainsystems.core.simulation.MonteCarloSimulationParameters;
import org.palladiosimulator.blockchainsystems.core.simulation.SingleSimulationParameters;
import org.palladiosimulator.blockchainsystems.core.simulation.abstractions.Simulation;
import org.palladiosimulator.blockchainsystems.core.simulation.abstractions.SimulationParameters;
import org.palladiosimulator.blockchainsystems.core.simulation.abstractions.SimulationResult;

import org.palladiosimulator.blockchainsystems.plugin.logging.LogOutputProviderImpl;
import org.palladiosimulator.blockchainsystems.plugin.simulation.MonteCarloSimulationProgressMonitorAdapter;

import org.palladiosimulator.blockchainsystems.threesim.creation.ThreesimBlockchainSystemFactory;
import org.palladiosimulator.blockchainsystems.threesim.creation.network.connectedsubgraphs.ConnectedSubgraphNetworkBlockchainSystemFactory;
import org.palladiosimulator.blockchainsystems.threesim.creation.network.explicit.ExplicitNetworkBlockchainSystemFactory;
import org.palladiosimulator.blockchainsystems.threesim.simulation.AttackType;
import org.palladiosimulator.blockchainsystems.threesim.simulation.ThreesimMonteCarloSimulation;
import org.palladiosimulator.blockchainsystems.threesim.simulation.ThreesimSingleSimulation;
import org.palladiosimulator.blockchainsystems.threesim.simulation.ThreesimSimulationParameters;
import org.palladiosimulator.blockchainsystems.threesim.simulation.TransactionGenerationMode;

public class ATOSIMSimulationFactory implements Simulation {

    private final Simulation simulation;

    public ATOSIMSimulationFactory(
            SimulationParameters simulationParameters,
            Map<String, String> configuration) {

        BlockchainSystemModelLoader loader = new BlockchainSystemModelLoader();

        String attackModelFilePath = configuration.getOrDefault("attackModelFilePath", "");
        
        // Convert configuration to java object
        BlockchainSystem designBlockchainSystem =
                loader.load(simulationParameters.getBlockchainSystemModelFilePath(), attackModelFilePath);
        
        // Extract simulation parameters
        ThreesimSimulationParameters threesimSimulationParameters =
                getThreesimSimulationParametersFromConfiguration(configuration, loader.getAttackScenario());
        
        // Build blockchain system
        ThreesimBlockchainSystemFactory blockchainSystemFactory =
                createBlockchainSystemFactory(designBlockchainSystem);

        int maxAllowedBlockchainLength =
                Math.toIntExact(simulationParameters.getMaxAllowedBlockchainLength());

        LogOutputProviderImpl logOutputProvider;
        try {
            logOutputProvider = LogOutputProviderImpl.fromLaunchConfiguration(null);
        } catch (CoreException e) {
            throw new RuntimeException("Failed to build log output provider", e);
        }
        
        // Assign simulation an actual object (Monte Carlo or Single)
        if (simulationParameters instanceof MonteCarloSimulationParameters) {
            MonteCarloSimulationProgressMonitorAdapter progressMonitor =
                    new MonteCarloSimulationProgressMonitorAdapter(null);

            this.simulation =
                    new ThreesimMonteCarloSimulation(
                            progressMonitor,
                            blockchainSystemFactory,
                            logOutputProvider,
                            maxAllowedBlockchainLength,
                            (MonteCarloSimulationParameters) simulationParameters,
                            threesimSimulationParameters
                    );
        } else {
            this.simulation =
                    new ThreesimSingleSimulation(
                            blockchainSystemFactory,
                            logOutputProvider,
                            maxAllowedBlockchainLength,
                            (SingleSimulationParameters) simulationParameters,
                            threesimSimulationParameters
                    );
        }
    }

    @Override
    public SimulationResult run() {
        return simulation.run();
    }
    
    
    // Parse concrete configuration values
    private ThreesimSimulationParameters getThreesimSimulationParametersFromConfiguration(
            Map<String, String> configuration, AttackScenario attackScenario) {

        double failureThroughputThreshold =
                Double.parseDouble(configuration.getOrDefault("failureThroughputThreshold", "1.0"));
        double shannonEntropyK =
                Double.parseDouble(configuration.getOrDefault("shannonEntropyK", "1.0"));
        double nakamotoCoefficientThreshold =
                Double.parseDouble(configuration.getOrDefault("nakamotoCoefficientThreshold", "50.0"));
        double reliabilityObservationTimespan =
                Double.parseDouble(configuration.getOrDefault("reliabilityObservationTimespan", "24.0"));
        double blockInterval =
                Double.parseDouble(configuration.getOrDefault("block_creation_interval", "600.0"));
        int maxBlockSize =
                Integer.parseInt(configuration.getOrDefault("max_block_size", "1000000"));
        double networkBandwidth =
                Double.parseDouble(configuration.getOrDefault("bandwidth", "100.0"));
        int nodeDegree =
                Integer.parseInt(configuration.getOrDefault("node_degree", "8"));
        TransactionGenerationMode transactionGenerationMode =
                "deterministicFullBlock".equalsIgnoreCase(
                        configuration.getOrDefault("transactionGenerationMode", "mempool").strip())
                        ? TransactionGenerationMode.DETERMINISTIC_FULL_BLOCK
                        : TransactionGenerationMode.MEMPOOL;
        boolean staticValidationDelayEnabled =
                Boolean.parseBoolean(configuration.getOrDefault("staticValidationDelayEnabled", "true"));
        
        // For attack scenarios
        AttackType attackType;
        Set<String> attackerNodeIds;
        double attackerHashPower;
        double gamma;
        long deltaA;
        long deltaB;

        if (attackScenario != null) {
            Attack attack = attackScenario.getAttack();
            attackType = mapAttackType(attack);
            attackerNodeIds = attackScenario.getAttackers().stream()
                    .map(AttackerNode::getLinkedNodeSystem)
                    .filter(Objects::nonNull)
                    .map(Entity::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            attackerHashPower = Math.min(1.0, Math.max(0.0,
                    attackScenario.getAttackers().stream().mapToDouble(AttackerNode::getPowerShare).sum()));
            gamma = attack.getGamma();
            if (attack instanceof RaceAttack raceAttack) {
                deltaA = raceAttack.getTransactionADelay();
                deltaB = raceAttack.getTransactionBAcceleration();
            } else {
                deltaA = 0L;
                deltaB = 0L;
            }
        } else {
            attackerHashPower = Double.parseDouble(configuration.getOrDefault("attacker_hash_power", "0.0"));
            String attackTypeName = configuration.getOrDefault("attack_type", "").strip();

            if (!attackTypeName.isEmpty()) {
                attackType = switch (attackTypeName) {
                    case "SELFISH_MINING"              -> AttackType.SELFISH_MINING;
                    case "LEAD_STUBBORN_MINING"        -> AttackType.LEAD_STUBBORN_MINING;
                    case "TRAIL_STUBBORN_MINING"       -> AttackType.TRAIL_STUBBORN_MINING;
                    case "EQUAL_FORK_STUBBORN_MINING"  -> AttackType.EQUAL_FORK_STUBBORN_MINING;
                    case "FINNEY"                      -> AttackType.FINNEY;
                    case "RACE"                        -> AttackType.RACE;
                    case "MAJORITY"                    -> AttackType.MAJORITY;
                    case "COMBINED_SELFISH_RACE"           -> AttackType.COMBINED_SELFISH_RACE;
                    case "COMBINED_SELFISH_FINNEY"         -> AttackType.COMBINED_SELFISH_FINNEY;
                    case "COMBINED_SELFISH_LEAD_STUBBORN"  -> AttackType.COMBINED_SELFISH_LEAD_STUBBORN;
                    case "COMBINED_SELFISH_TRAIL_STUBBORN" -> AttackType.COMBINED_SELFISH_TRAIL_STUBBORN;
                    default -> (attackerHashPower > 0.0) ? AttackType.SELFISH_MINING : AttackType.NONE;
                };
            } else {
                attackType = (attackerHashPower > 0.0) ? AttackType.SELFISH_MINING : AttackType.NONE;
            }

            attackerNodeIds = Collections.emptySet();
            gamma = Double.parseDouble(configuration.getOrDefault("tie_breaking_parameter", "0.0"));
            deltaA = 0L;
            deltaB = 0L;
        }

        attackType = applyCombinedAttackMode(attackType, configuration);

        if (deltaA == 0L && deltaB == 0L
                && (attackType == AttackType.RACE || attackType == AttackType.COMBINED_SELFISH_RACE)) {
            deltaA = Long.parseLong(configuration.getOrDefault("transaction_delay", "0"));
            deltaB = Long.parseLong(configuration.getOrDefault("transaction_acceleration", "0"));
        }

        boolean combinedAttackEnabled = switch (attackType) {
            case COMBINED_SELFISH_RACE, COMBINED_SELFISH_FINNEY,
                 COMBINED_SELFISH_LEAD_STUBBORN, COMBINED_SELFISH_TRAIL_STUBBORN -> true;
            default -> false;
        };

        AttackType secondaryAttackType = switch (attackType) {
            case COMBINED_SELFISH_RACE           -> AttackType.RACE;
            case COMBINED_SELFISH_FINNEY         -> AttackType.FINNEY;
            case COMBINED_SELFISH_LEAD_STUBBORN  -> AttackType.LEAD_STUBBORN_MINING;
            case COMBINED_SELFISH_TRAIL_STUBBORN -> AttackType.TRAIL_STUBBORN_MINING;
            default                              -> AttackType.NONE;
        };

        return new ThreesimSimulationParameters(
                // Item 1b: this feeds DeterministicSeeds.topologySeed(configId, replicationId)
                // (ThreesimBlockchainSystemFactory.createBlockchainSystem), which must be keyed
                // on the topology-defining design, not the per-row config_id (unique per row --
                // using it gives every row its own distinct topology instead of reusing one
                // across bandwidth/attacker/strategy conditions, confirmed by measurement to be
                // the current behavior). system_config_id alone is NOT sufficient here despite
                // looking like the natural candidate: lhs_generate_sample_two_stage.py assigns a
                // *fresh* system_config_id to every (core_config_id x bandwidth_variant_id) pair
                // (core_df.merge(bandwidth_df, how="cross"), then a flat sequential id over the
                // cross-joined rows) -- so it varies *with* bandwidth and would only reuse
                // topology across attacker_config_id/attack_strategy, not bandwidth, missing part
                // of the checklist's "across all bandwidth, alpha, and strategy conditions"
                // requirement. core_config_id (the actually bandwidth-invariant id) never reaches
                // this CSV at all -- generate_run_configurations.py's output only carries the
                // columns ATOSIMSimulator.validateCsvColumns requires, which excludes it, and
                // adding it would mean touching the Python generator (out of scope here). Instead,
                // this reconstructs the same invariant from the four already-present columns that
                // -- together -- define a core config 1:1 (validator_count, node_degree,
                // block_creation_interval, max_block_size; verified across the real 250-row
                // sampling design: exactly 250 distinct 4-tuples, each mapping to exactly 10
                // distinct system_config_id values, one per bandwidth variant, zero collisions).
                // The per-row config_id is untouched everywhere else -- still echoed unchanged in
                // the output JSON's inputParameters, sourced directly from the raw CSV row,
                // independent of this parameter.
                String.join("|",
                        configuration.getOrDefault("validator_count", ""),
                        configuration.getOrDefault("node_degree", ""),
                        configuration.getOrDefault("block_creation_interval", ""),
                        configuration.getOrDefault("max_block_size", "")),
                failureThroughputThreshold,
                shannonEntropyK,
                nakamotoCoefficientThreshold,
                reliabilityObservationTimespan,
                attackType,
                combinedAttackEnabled,
                secondaryAttackType,
                attackerNodeIds,
                attackerHashPower,
                gamma,
                deltaA,
                deltaB,
                6,
                blockInterval,
                nodeDegree,
                maxBlockSize,
                networkBandwidth,
                transactionGenerationMode,
                staticValidationDelayEnabled
        );
    }

    private static AttackType mapAttackType(Attack attack) {
        if (attack instanceof SelfishMiningAttack) return AttackType.SELFISH_MINING;
        if (attack instanceof LeadStubbornAttack) return AttackType.LEAD_STUBBORN_MINING;
        if (attack instanceof EqualForkStubbornAttack) return AttackType.EQUAL_FORK_STUBBORN_MINING;
        if (attack instanceof TrailStubbornAttack) return AttackType.TRAIL_STUBBORN_MINING;
        if (attack instanceof FinneyAttack) return AttackType.FINNEY;
        if (attack instanceof RaceAttack) return AttackType.RACE;
        if (attack instanceof MajorityAttack) return AttackType.MAJORITY;
        return AttackType.NONE;
    }

    private static AttackType applyCombinedAttackMode(AttackType attackType, Map<String, String> configuration) {
        if (attackType != AttackType.SELFISH_MINING) {
            return attackType;
        }

        String combinedAttackMode = configuration.getOrDefault("combined_attack_mode", "NONE").strip();

        return switch (combinedAttackMode) {
            case "SELFISH_RACE"           -> AttackType.COMBINED_SELFISH_RACE;
            case "SELFISH_FINNEY"         -> AttackType.COMBINED_SELFISH_FINNEY;
            case "SELFISH_LEAD_STUBBORN"  -> AttackType.COMBINED_SELFISH_LEAD_STUBBORN;
            case "SELFISH_TRAIL_STUBBORN" -> AttackType.COMBINED_SELFISH_TRAIL_STUBBORN;
            default -> attackType;
        };
    }

    private ThreesimBlockchainSystemFactory createBlockchainSystemFactory(
            BlockchainSystem designBlockchainSystem) {
    	
    	// Decide which topology is used, null if nothing is specified
        var networkTopology =
                designBlockchainSystem.getNetwork().getTopology();

        if (networkTopology instanceof ConnectedSubgraphsNetworkTopology) {
            return new ConnectedSubgraphNetworkBlockchainSystemFactory(
                    designBlockchainSystem,
                    (ConnectedSubgraphsNetworkTopology) networkTopology);
        }

        if (networkTopology instanceof ExplicitNetworkTopology) {
            return new ExplicitNetworkBlockchainSystemFactory(
                    designBlockchainSystem,
                    (ExplicitNetworkTopology) networkTopology);
        }

        throw new IllegalStateException(
                "Unsupported network topology: "
                        + networkTopology.getClass().getName());
    }
}
