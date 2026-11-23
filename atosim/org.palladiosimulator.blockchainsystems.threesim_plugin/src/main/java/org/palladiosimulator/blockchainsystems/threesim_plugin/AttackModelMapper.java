package org.palladiosimulator.blockchainsystems.threesim_plugin;

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
import org.palladiosimulator.blockchainsystems.threesim.simulation.AttackType;
import org.palladiosimulator.blockchainsystems.threesim.simulation.ThreesimSimulationParameters;

import java.util.HashSet;
import java.util.Set;

public class AttackModelMapper {

    public static final AttackModelMapper INSTANCE = new AttackModelMapper();

    public static ThreesimSimulationParameters applyAttackScenario(
            ThreesimSimulationParameters base,
            BlockchainSystem blockchainSystem,
            AttackScenario scenario) {

        AttackType attackType = mapAttackType(scenario.getAttack());

        AttackType finalAttackType;
        if (base.isCombinedAttackEnabled() && attackType == AttackType.SELFISH_MINING
                && base.getSecondaryAttackType() == AttackType.RACE) {
            finalAttackType = AttackType.COMBINED_SELFISH_RACE;
        } else if (base.isCombinedAttackEnabled() && attackType == AttackType.SELFISH_MINING
                && base.getSecondaryAttackType() == AttackType.FINNEY) {
            finalAttackType = AttackType.COMBINED_SELFISH_FINNEY;
        } else if (base.isCombinedAttackEnabled() && attackType == AttackType.SELFISH_MINING
                && base.getSecondaryAttackType() == AttackType.LEAD_STUBBORN_MINING) {
            finalAttackType = AttackType.COMBINED_SELFISH_LEAD_STUBBORN;
        } else if (base.isCombinedAttackEnabled() && attackType == AttackType.SELFISH_MINING
                && base.getSecondaryAttackType() == AttackType.TRAIL_STUBBORN_MINING) {
            finalAttackType = AttackType.COMBINED_SELFISH_TRAIL_STUBBORN;
        } else {
            finalAttackType = attackType;
        }

        boolean combinedAttackEnabled = finalAttackType == AttackType.COMBINED_SELFISH_RACE
                || finalAttackType == AttackType.COMBINED_SELFISH_FINNEY
                || finalAttackType == AttackType.COMBINED_SELFISH_LEAD_STUBBORN
                || finalAttackType == AttackType.COMBINED_SELFISH_TRAIL_STUBBORN;

        AttackType secondaryAttackType = switch (finalAttackType) {
            case COMBINED_SELFISH_RACE -> AttackType.RACE;
            case COMBINED_SELFISH_FINNEY -> AttackType.FINNEY;
            case COMBINED_SELFISH_LEAD_STUBBORN -> AttackType.LEAD_STUBBORN_MINING;
            case COMBINED_SELFISH_TRAIL_STUBBORN -> AttackType.TRAIL_STUBBORN_MINING;
            default -> AttackType.NONE;
        };

        Set<String> attackerNodeIds = new HashSet<>();
        for (AttackerNode attacker : scenario.getAttackers()) {
            if (attacker.getLinkedNodeSystem() != null
                    && attacker.getLinkedNodeSystem().getId() != null) {
                attackerNodeIds.add(attacker.getLinkedNodeSystem().getId());
            }
        }

        double attackerHashPower = Math.min(1.0, Math.max(0.0,
                scenario.getAttackers().stream().mapToDouble(AttackerNode::getPowerShare).sum()));

        long deltaA, deltaB;
        if (scenario.getAttack() instanceof RaceAttack ra) {
            deltaA = ra.getTransactionADelay();
            deltaB = ra.getTransactionBAcceleration();
        } else if (finalAttackType == AttackType.COMBINED_SELFISH_RACE
                || base.getSecondaryAttackType() == AttackType.RACE) {
            deltaA = base.getDeltaA();
            deltaB = base.getDeltaB();
        } else {
            deltaA = 0L;
            deltaB = 0L;
        }

        return new ThreesimSimulationParameters(
                base.getConfigId(),
                base.getFailureThroughputThreshold(),
                base.getShannonEntropyK(),
                base.getNakamotoCoefficientThreshold(),
                base.getReliabilityObservationTimespan(),
                finalAttackType,
                combinedAttackEnabled,
                secondaryAttackType,
                attackerNodeIds,
                attackerHashPower,
                scenario.getAttack().getGamma(),
                deltaA,
                deltaB,
                base.getConfirmationDepth(),
                base.getBlockInterval(),
                base.getNodeDegree(),
                base.getMaxBlockSize(),
                base.getNetworkBandwidth(),
                base.getTransactionGenerationMode(),
                base.isStaticValidationDelayEnabled(),
                base.getRecoveryTimeoutDuration()
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
}
