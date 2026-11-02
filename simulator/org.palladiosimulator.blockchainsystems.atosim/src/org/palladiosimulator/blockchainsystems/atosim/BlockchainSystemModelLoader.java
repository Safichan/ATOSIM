package org.palladiosimulator.blockchainsystems.atosim;

import org.palladiosimulator.blockchainsystems.bscm.attackmodel.AttackScenario;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.AttackerNode;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.AttackmodelPackage;
import org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.BlockchainSystem;
import org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.BlockchainsystemPackage;
import org.palladiosimulator.blockchainsystems.bscm.nodeallocation.NodeallocationPackage;
import org.palladiosimulator.blockchainsystems.bscm.nodesystem.BlockchainSystemNodeSystem;
import org.palladiosimulator.blockchainsystems.bscm.p2pnetwork.ConnectedSubgraphsNetworkTopology;
import org.palladiosimulator.blockchainsystems.bscm.p2pnetwork.ExplicitNetworkTopology;
import org.palladiosimulator.blockchainsystems.bscm.p2pnetwork.NetworkTopology;
import org.palladiosimulator.blockchainsystems.bscm.p2pnetwork.P2pnetworkPackage;
import org.palladiosimulator.blockchainsystems.bscm.blockchainsystemComponentRepository.BlockchainsystemComponentRepositoryPackage;

import com.google.common.io.Files;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

/**
 * Loads a {@code BlockchainSystem} design model and, optionally, an {@code AttackScenario}.
 *
 * <p>The two may live in different folders (the two-stage sampling layout generates system
 * models under {@code system_models/sys-<id>/} and attack models under
 * {@code attack_models/<strategy>/atk-<id>/}). All files are mapped into a single logical
 * {@code platform:/plugin/...} namespace derived from the system model's folder name, so
 * relative XMI hrefs inside the attack model (e.g. {@code Net.nodeallocation#<id>}) resolve
 * against the system model's files regardless of where the attack model physically lives.
 */
public class BlockchainSystemModelLoader {

    private static final String[] SYSTEM_EXTENSIONS = {
            "blockchainsystem", "p2pnetwork", "nodeallocation",
            "bscmrepository", "geographicalregions", "linkallocation", "transactions"
    };

    private ResourceSet resourceSet;
    
    // Loads and combines everything to one Blockchain system model (java object)
    public BlockchainSystem load(String systemModelUri) {
        return load(systemModelUri, null);
    }

    public BlockchainSystem load(String systemModelUri, String attackModelFilePath) {
        Path modelPath = Paths.get(systemModelUri).toAbsolutePath();
        Path folderPath = modelPath.getParent();
        String folderName = folderPath.getFileName().toString();
        String fileName = modelPath.getFileName().toString();
        String baseName = Files.getNameWithoutExtension(fileName);

        Path attackModelPath = (attackModelFilePath != null && !attackModelFilePath.isBlank())
                ? Paths.get(attackModelFilePath).toAbsolutePath()
                : folderPath.resolve(baseName + ".attackmodel");
        boolean hasAttackModel = java.nio.file.Files.exists(attackModelPath);

        ResourceSet resourceSet = new ResourceSetImpl();
        XMIResourceFactoryImpl xmiFactory = new XMIResourceFactoryImpl();

        for (String ext : SYSTEM_EXTENSIONS) {
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(ext, xmiFactory);
        }
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("attackmodel", xmiFactory);

        resourceSet.getPackageRegistry().put(BlockchainsystemPackage.eNS_URI, BlockchainsystemPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(P2pnetworkPackage.eNS_URI, P2pnetworkPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(NodeallocationPackage.eNS_URI, NodeallocationPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(BlockchainsystemComponentRepositoryPackage.eNS_URI,
                BlockchainsystemComponentRepositoryPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(AttackmodelPackage.eNS_URI, AttackmodelPackage.eINSTANCE);

        List<String> relativeNames = new ArrayList<>();
        relativeNames.add(fileName);
        for (String ext : SYSTEM_EXTENSIONS) {
            if (ext.equals("blockchainsystem")) continue; // already added as fileName
            relativeNames.add(baseName + "." + ext);
        }

        String attackRelativeName = baseName + ".attackmodel";

        for (String name : relativeNames) {
            resourceSet.getURIConverter().getURIMap().put(
                    createPluginURI(folderName, name),
                    URI.createFileURI(folderPath.resolve(name).toString()));
        }
        if (hasAttackModel) {
            resourceSet.getURIConverter().getURIMap().put(
                    createPluginURI(folderName, attackRelativeName),
                    URI.createFileURI(attackModelPath.toString()));
            relativeNames.add(attackRelativeName);
        }

        for (String name : relativeNames) {
            resourceSet.getResource(createPluginURI(folderName, name), true);
        }

        ArrayList<Resource> currentResources;
        do {
            currentResources = new ArrayList<>(resourceSet.getResources());
            for (Resource r : currentResources) {
                EcoreUtil.resolveAll(r);
            }
        } while (currentResources.size() != resourceSet.getResources().size());

        this.resourceSet = resourceSet;

        BlockchainSystem designBlockchainSystem =
                (BlockchainSystem) resourceSet.getResources().get(0).getContents().get(0);

        if (hasAttackModel) {
            repairDanglingAttackerLinks(designBlockchainSystem, getAttackScenario());
        }

        return designBlockchainSystem;
    }

    /**
     * Returns the {@link AttackScenario} loaded alongside the blockchain system, or
     * {@code null} if the model folder does not contain an {@code .attackmodel} file.
     * Must be called after {@link #load(String, String)}.
     */
    public AttackScenario getAttackScenario() {
        for (Resource resource : resourceSet.getResources()) {
            if (!resource.getURI().lastSegment().endsWith(".attackmodel")) continue;
            if (resource.getContents().isEmpty()) continue;
            Object root = resource.getContents().get(0);
            if (root instanceof AttackScenario) return (AttackScenario) root;
        }
        return null;
    }

    /**
     * Attack models generated for the two-stage sampling design carry a
     * {@code linkedNodeSystem} href produced against one reference system model's
     * {@code Net.nodeallocation} ids. When paired with a different system model those ids
     * don't exist, leaving the reference unresolved. Since which concrete node carries the
     * attacker is statistically arbitrary (the topology is randomly generated), an unresolved
     * link is repointed at the first node system found in the actually-loaded topology.
     */
    private void repairDanglingAttackerLinks(BlockchainSystem blockchainSystem, AttackScenario attackScenario) {
        if (attackScenario == null) return;

        List<BlockchainSystemNodeSystem> nodeSystems = null;
        for (AttackerNode attacker : attackScenario.getAttackers()) {
            BlockchainSystemNodeSystem linked = attacker.getLinkedNodeSystem();
            if (linked != null && !linked.eIsProxy()) continue;

            if (nodeSystems == null) {
                nodeSystems = collectNodeSystems(blockchainSystem);
            }
            if (nodeSystems.isEmpty()) {
                throw new IllegalStateException(
                        "Attack model references a NodeSystem that does not exist in the paired "
                                + "system model, and the system model's topology has no node systems "
                                + "to fall back to.");
            }
            attacker.setLinkedNodeSystem(nodeSystems.get(0));
        }
    }

    private List<BlockchainSystemNodeSystem> collectNodeSystems(BlockchainSystem blockchainSystem) {
        List<BlockchainSystemNodeSystem> result = new ArrayList<>();
        NetworkTopology topology = blockchainSystem.getNetwork().getTopology();

        if (topology instanceof ExplicitNetworkTopology explicit) {
            explicit.getNodes().forEach(node -> {
                if (node.getAllocation() != null && node.getAllocation().getNodeSystem() != null) {
                    result.add(node.getAllocation().getNodeSystem());
                }
            });
        } else if (topology instanceof ConnectedSubgraphsNetworkTopology connected) {
            connected.getSubgraphs().forEach(subgraph ->
                    subgraph.getNodeTemplates().forEach(template -> {
                        if (template.getAllocation() != null && template.getAllocation().getNodeSystem() != null) {
                            result.add(template.getAllocation().getNodeSystem());
                        }
                    }));
        }

        return result;
    }

    private URI createPluginURI(String folder, String relativePath) {
        String path = Paths.get(
                "org.palladiosimulator.blockchainsystems.atosim/testmodels/" + folder,
                relativePath).toString();
        return URI.createPlatformPluginURI(path, false);
    }
}
