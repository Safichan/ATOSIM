/**
 */
package org.palladiosimulator.blockchainsystems.bscm.attackmodel.impl;

import de.uka.ipd.sdq.identifier.IdentifierPackage;

import de.uka.ipd.sdq.probfunction.ProbfunctionPackage;

import de.uka.ipd.sdq.stoex.StoexPackage;

import de.uka.ipd.sdq.units.UnitsPackage;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.palladiosimulator.blockchainsystems.bscm.attackmodel.Attack;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.AttackParameter;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.AttackResult;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.AttackScenario;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.AttackerNode;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.AttackmodelFactory;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.AttackmodelPackage;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.EqualForkStubbornAttack;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.FinneyAttack;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.ForkDepthEntry;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.LeadStubbornAttack;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.MajorityAttack;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.RaceAttack;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.SelfishMiningAttack;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.StubbornMiningAttack;
import org.palladiosimulator.blockchainsystems.bscm.attackmodel.TrailStubbornAttack;

import org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.BlockchainsystemPackage;

import org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.impl.BlockchainsystemPackageImpl;

import org.palladiosimulator.blockchainsystems.bscm.blockchainsystemComponentRepository.BlockchainsystemComponentRepositoryPackage;

import org.palladiosimulator.blockchainsystems.bscm.blockchainsystemComponentRepository.impl.BlockchainsystemComponentRepositoryPackageImpl;

import org.palladiosimulator.blockchainsystems.bscm.geographicalregions.GeographicalregionsPackage;

import org.palladiosimulator.blockchainsystems.bscm.geographicalregions.impl.GeographicalregionsPackageImpl;

import org.palladiosimulator.blockchainsystems.bscm.linkallocation.LinkallocationPackage;

import org.palladiosimulator.blockchainsystems.bscm.linkallocation.impl.LinkallocationPackageImpl;

import org.palladiosimulator.blockchainsystems.bscm.nodeallocation.NodeallocationPackage;

import org.palladiosimulator.blockchainsystems.bscm.nodeallocation.impl.NodeallocationPackageImpl;

import org.palladiosimulator.blockchainsystems.bscm.nodeenvironment.NodeenvironmentPackage;

import org.palladiosimulator.blockchainsystems.bscm.nodeenvironment.impl.NodeenvironmentPackageImpl;

import org.palladiosimulator.blockchainsystems.bscm.nodesystem.NodesystemPackage;

import org.palladiosimulator.blockchainsystems.bscm.nodesystem.impl.NodesystemPackageImpl;

import org.palladiosimulator.blockchainsystems.bscm.p2pnetwork.P2pnetworkPackage;

import org.palladiosimulator.blockchainsystems.bscm.p2pnetwork.impl.P2pnetworkPackageImpl;

import org.palladiosimulator.blockchainsystems.bscm.transactions.TransactionsPackage;

import org.palladiosimulator.blockchainsystems.bscm.transactions.impl.TransactionsPackageImpl;

import org.palladiosimulator.pcm.PcmPackage;

import org.palladiosimulator.pcm.core.entity.EntityPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class AttackmodelPackageImpl extends EPackageImpl implements AttackmodelPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass attackEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass attackScenarioEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass attackerNodeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass attackResultEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass attackParameterEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass forkDepthEntryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass raceAttackEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass finneyAttackEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass selfishMiningAttackEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass stubbornMiningAttackEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass equalForkStubbornAttackEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass leadStubbornAttackEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass trailStubbornAttackEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass majorityAttackEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.palladiosimulator.blockchainsystems.bscm.attackmodel.AttackmodelPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private AttackmodelPackageImpl() {
		super(eNS_URI, AttackmodelFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link AttackmodelPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static AttackmodelPackage init() {
		if (isInited)
			return (AttackmodelPackage) EPackage.Registry.INSTANCE.getEPackage(AttackmodelPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredAttackmodelPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		AttackmodelPackageImpl theAttackmodelPackage = registeredAttackmodelPackage instanceof AttackmodelPackageImpl
				? (AttackmodelPackageImpl) registeredAttackmodelPackage
				: new AttackmodelPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		EcorePackage.eINSTANCE.eClass();
		IdentifierPackage.eINSTANCE.eClass();
		ProbfunctionPackage.eINSTANCE.eClass();
		StoexPackage.eINSTANCE.eClass();
		UnitsPackage.eINSTANCE.eClass();
		PcmPackage.eINSTANCE.eClass();

		// Obtain or create and register interdependencies
		Object registeredPackage = EPackage.Registry.INSTANCE.getEPackage(NodeallocationPackage.eNS_URI);
		NodeallocationPackageImpl theNodeallocationPackage = (NodeallocationPackageImpl) (registeredPackage instanceof NodeallocationPackageImpl
				? registeredPackage
				: NodeallocationPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(NodesystemPackage.eNS_URI);
		NodesystemPackageImpl theNodesystemPackage = (NodesystemPackageImpl) (registeredPackage instanceof NodesystemPackageImpl
				? registeredPackage
				: NodesystemPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(BlockchainsystemComponentRepositoryPackage.eNS_URI);
		BlockchainsystemComponentRepositoryPackageImpl theBlockchainsystemComponentRepositoryPackage = (BlockchainsystemComponentRepositoryPackageImpl) (registeredPackage instanceof BlockchainsystemComponentRepositoryPackageImpl
				? registeredPackage
				: BlockchainsystemComponentRepositoryPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(P2pnetworkPackage.eNS_URI);
		P2pnetworkPackageImpl theP2pnetworkPackage = (P2pnetworkPackageImpl) (registeredPackage instanceof P2pnetworkPackageImpl
				? registeredPackage
				: P2pnetworkPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(BlockchainsystemPackage.eNS_URI);
		BlockchainsystemPackageImpl theBlockchainsystemPackage = (BlockchainsystemPackageImpl) (registeredPackage instanceof BlockchainsystemPackageImpl
				? registeredPackage
				: BlockchainsystemPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(NodeenvironmentPackage.eNS_URI);
		NodeenvironmentPackageImpl theNodeenvironmentPackage = (NodeenvironmentPackageImpl) (registeredPackage instanceof NodeenvironmentPackageImpl
				? registeredPackage
				: NodeenvironmentPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(GeographicalregionsPackage.eNS_URI);
		GeographicalregionsPackageImpl theGeographicalregionsPackage = (GeographicalregionsPackageImpl) (registeredPackage instanceof GeographicalregionsPackageImpl
				? registeredPackage
				: GeographicalregionsPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(LinkallocationPackage.eNS_URI);
		LinkallocationPackageImpl theLinkallocationPackage = (LinkallocationPackageImpl) (registeredPackage instanceof LinkallocationPackageImpl
				? registeredPackage
				: LinkallocationPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(TransactionsPackage.eNS_URI);
		TransactionsPackageImpl theTransactionsPackage = (TransactionsPackageImpl) (registeredPackage instanceof TransactionsPackageImpl
				? registeredPackage
				: TransactionsPackage.eINSTANCE);

		// Create package meta-data objects
		theAttackmodelPackage.createPackageContents();
		theNodeallocationPackage.createPackageContents();
		theNodesystemPackage.createPackageContents();
		theBlockchainsystemComponentRepositoryPackage.createPackageContents();
		theP2pnetworkPackage.createPackageContents();
		theBlockchainsystemPackage.createPackageContents();
		theNodeenvironmentPackage.createPackageContents();
		theGeographicalregionsPackage.createPackageContents();
		theLinkallocationPackage.createPackageContents();
		theTransactionsPackage.createPackageContents();

		// Initialize created meta-data
		theAttackmodelPackage.initializePackageContents();
		theNodeallocationPackage.initializePackageContents();
		theNodesystemPackage.initializePackageContents();
		theBlockchainsystemComponentRepositoryPackage.initializePackageContents();
		theP2pnetworkPackage.initializePackageContents();
		theBlockchainsystemPackage.initializePackageContents();
		theNodeenvironmentPackage.initializePackageContents();
		theGeographicalregionsPackage.initializePackageContents();
		theLinkallocationPackage.initializePackageContents();
		theTransactionsPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theAttackmodelPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(AttackmodelPackage.eNS_URI, theAttackmodelPackage);
		return theAttackmodelPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAttack() {
		return attackEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttack_Type() {
		return (EAttribute) attackEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttack_RequiredHashPower() {
		return (EAttribute) attackEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttack_DependsOnNetworkDelay() {
		return (EAttribute) attackEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttack_BaselineSuccessProbability() {
		return (EAttribute) attackEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAttack_Parameters() {
		return (EReference) attackEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttack_Gamma() {
		return (EAttribute) attackEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAttackScenario() {
		return attackScenarioEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAttackScenario_Attack() {
		return (EReference) attackScenarioEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAttackScenario_Attackers() {
		return (EReference) attackScenarioEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAttackScenario_BlockchainSystem() {
		return (EReference) attackScenarioEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAttackScenario_Result() {
		return (EReference) attackScenarioEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAttackerNode() {
		return attackerNodeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttackerNode_PowerShare() {
		return (EAttribute) attackerNodeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAttackerNode_LinkedNodeSystem() {
		return (EReference) attackerNodeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAttackResult() {
		return attackResultEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttackResult_Success() {
		return (EAttribute) attackResultEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttackResult_Duration() {
		return (EAttribute) attackResultEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttackResult_AffectedBlocks() {
		return (EAttribute) attackResultEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttackResult_SuccessProbabilityObserved() {
		return (EAttribute) attackResultEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttackResult_AvgForkRate() {
		return (EAttribute) attackResultEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttackResult_StaleBlockRate() {
		return (EAttribute) attackResultEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttackResult_DoubleSpendCount() {
		return (EAttribute) attackResultEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttackResult_TimeToTakeover() {
		return (EAttribute) attackResultEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAttackResult_ForkDepthDistribution() {
		return (EReference) attackResultEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttackResult_AttackerRevenueRatio() {
		return (EAttribute) attackResultEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAttackParameter() {
		return attackParameterEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttackParameter_Key() {
		return (EAttribute) attackParameterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttackParameter_Value() {
		return (EAttribute) attackParameterEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getForkDepthEntry() {
		return forkDepthEntryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getForkDepthEntry_Depth() {
		return (EAttribute) forkDepthEntryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getForkDepthEntry_Count() {
		return (EAttribute) forkDepthEntryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRaceAttack() {
		return raceAttackEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRaceAttack_PropagationAdvantage() {
		return (EAttribute) raceAttackEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRaceAttack_TransactionADelay() {
		return (EAttribute) raceAttackEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRaceAttack_TransactionBAcceleration() {
		return (EAttribute) raceAttackEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRaceAttack_FeeDifferenceThreshold() {
		return (EAttribute) raceAttackEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRaceAttack_ZeroConfAcceptanceRisk() {
		return (EAttribute) raceAttackEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRaceAttack_ObservedDoubleSpends() {
		return (EAttribute) raceAttackEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRaceAttack_AvgPropagationAdvMs() {
		return (EAttribute) raceAttackEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getFinneyAttack() {
		return finneyAttackEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFinneyAttack_NumberOfPreminedBlocks() {
		return (EAttribute) finneyAttackEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFinneyAttack_WithholdTime() {
		return (EAttribute) finneyAttackEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFinneyAttack_MerchantAcceptsZeroConf() {
		return (EAttribute) finneyAttackEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFinneyAttack_DoubleSpendSuccessCount() {
		return (EAttribute) finneyAttackEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFinneyAttack_ObservedTimeToReleaseMs() {
		return (EAttribute) finneyAttackEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSelfishMiningAttack() {
		return selfishMiningAttackEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSelfishMiningAttack_PrivateChainLeadTarget() {
		return (EAttribute) selfishMiningAttackEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSelfishMiningAttack_WithholdProbability() {
		return (EAttribute) selfishMiningAttackEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSelfishMiningAttack_PublishOnCatchup() {
		return (EAttribute) selfishMiningAttackEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSelfishMiningAttack_ExpectedForksPerHour() {
		return (EAttribute) selfishMiningAttackEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSelfishMiningAttack_MonitoredNodes() {
		return (EReference) selfishMiningAttackEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getStubbornMiningAttack() {
		return stubbornMiningAttackEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStubbornMiningAttack_LeadThreshold() {
		return (EAttribute) stubbornMiningAttackEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStubbornMiningAttack_AbandonThreshold() {
		return (EAttribute) stubbornMiningAttackEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEqualForkStubbornAttack() {
		return equalForkStubbornAttackEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEqualForkStubbornAttack_TieResolutionBias() {
		return (EAttribute) equalForkStubbornAttackEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getLeadStubbornAttack() {
		return leadStubbornAttackEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getLeadStubbornAttack_MinLeadToPublish() {
		return (EAttribute) leadStubbornAttackEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getTrailStubbornAttack() {
		return trailStubbornAttackEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTrailStubbornAttack_ContinueOnTrail() {
		return (EAttribute) trailStubbornAttackEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMajorityAttack() {
		return majorityAttackEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMajorityAttack_HashPowerShare() {
		return (EAttribute) majorityAttackEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMajorityAttack_ExpectedTakeoverTime() {
		return (EAttribute) majorityAttackEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMajorityAttack_TargetConfirmationsToBreak() {
		return (EAttribute) majorityAttackEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getMajorityAttack_ColludingPools() {
		return (EReference) majorityAttackEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AttackmodelFactory getAttackmodelFactory() {
		return (AttackmodelFactory) getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated)
			return;
		isCreated = true;

		// Create classes and their features
		attackEClass = createEClass(ATTACK);
		createEAttribute(attackEClass, ATTACK__TYPE);
		createEAttribute(attackEClass, ATTACK__REQUIRED_HASH_POWER);
		createEAttribute(attackEClass, ATTACK__DEPENDS_ON_NETWORK_DELAY);
		createEAttribute(attackEClass, ATTACK__BASELINE_SUCCESS_PROBABILITY);
		createEReference(attackEClass, ATTACK__PARAMETERS);
		createEAttribute(attackEClass, ATTACK__GAMMA);

		attackScenarioEClass = createEClass(ATTACK_SCENARIO);
		createEReference(attackScenarioEClass, ATTACK_SCENARIO__ATTACK);
		createEReference(attackScenarioEClass, ATTACK_SCENARIO__ATTACKERS);
		createEReference(attackScenarioEClass, ATTACK_SCENARIO__BLOCKCHAIN_SYSTEM);
		createEReference(attackScenarioEClass, ATTACK_SCENARIO__RESULT);

		attackerNodeEClass = createEClass(ATTACKER_NODE);
		createEAttribute(attackerNodeEClass, ATTACKER_NODE__POWER_SHARE);
		createEReference(attackerNodeEClass, ATTACKER_NODE__LINKED_NODE_SYSTEM);

		attackResultEClass = createEClass(ATTACK_RESULT);
		createEAttribute(attackResultEClass, ATTACK_RESULT__SUCCESS);
		createEAttribute(attackResultEClass, ATTACK_RESULT__DURATION);
		createEAttribute(attackResultEClass, ATTACK_RESULT__AFFECTED_BLOCKS);
		createEAttribute(attackResultEClass, ATTACK_RESULT__SUCCESS_PROBABILITY_OBSERVED);
		createEAttribute(attackResultEClass, ATTACK_RESULT__AVG_FORK_RATE);
		createEAttribute(attackResultEClass, ATTACK_RESULT__STALE_BLOCK_RATE);
		createEAttribute(attackResultEClass, ATTACK_RESULT__DOUBLE_SPEND_COUNT);
		createEAttribute(attackResultEClass, ATTACK_RESULT__TIME_TO_TAKEOVER);
		createEReference(attackResultEClass, ATTACK_RESULT__FORK_DEPTH_DISTRIBUTION);
		createEAttribute(attackResultEClass, ATTACK_RESULT__ATTACKER_REVENUE_RATIO);

		attackParameterEClass = createEClass(ATTACK_PARAMETER);
		createEAttribute(attackParameterEClass, ATTACK_PARAMETER__KEY);
		createEAttribute(attackParameterEClass, ATTACK_PARAMETER__VALUE);

		forkDepthEntryEClass = createEClass(FORK_DEPTH_ENTRY);
		createEAttribute(forkDepthEntryEClass, FORK_DEPTH_ENTRY__DEPTH);
		createEAttribute(forkDepthEntryEClass, FORK_DEPTH_ENTRY__COUNT);

		raceAttackEClass = createEClass(RACE_ATTACK);
		createEAttribute(raceAttackEClass, RACE_ATTACK__PROPAGATION_ADVANTAGE);
		createEAttribute(raceAttackEClass, RACE_ATTACK__TRANSACTION_ADELAY);
		createEAttribute(raceAttackEClass, RACE_ATTACK__TRANSACTION_BACCELERATION);
		createEAttribute(raceAttackEClass, RACE_ATTACK__FEE_DIFFERENCE_THRESHOLD);
		createEAttribute(raceAttackEClass, RACE_ATTACK__ZERO_CONF_ACCEPTANCE_RISK);
		createEAttribute(raceAttackEClass, RACE_ATTACK__OBSERVED_DOUBLE_SPENDS);
		createEAttribute(raceAttackEClass, RACE_ATTACK__AVG_PROPAGATION_ADV_MS);

		finneyAttackEClass = createEClass(FINNEY_ATTACK);
		createEAttribute(finneyAttackEClass, FINNEY_ATTACK__NUMBER_OF_PREMINED_BLOCKS);
		createEAttribute(finneyAttackEClass, FINNEY_ATTACK__WITHHOLD_TIME);
		createEAttribute(finneyAttackEClass, FINNEY_ATTACK__MERCHANT_ACCEPTS_ZERO_CONF);
		createEAttribute(finneyAttackEClass, FINNEY_ATTACK__DOUBLE_SPEND_SUCCESS_COUNT);
		createEAttribute(finneyAttackEClass, FINNEY_ATTACK__OBSERVED_TIME_TO_RELEASE_MS);

		selfishMiningAttackEClass = createEClass(SELFISH_MINING_ATTACK);
		createEAttribute(selfishMiningAttackEClass, SELFISH_MINING_ATTACK__PRIVATE_CHAIN_LEAD_TARGET);
		createEAttribute(selfishMiningAttackEClass, SELFISH_MINING_ATTACK__WITHHOLD_PROBABILITY);
		createEAttribute(selfishMiningAttackEClass, SELFISH_MINING_ATTACK__PUBLISH_ON_CATCHUP);
		createEAttribute(selfishMiningAttackEClass, SELFISH_MINING_ATTACK__EXPECTED_FORKS_PER_HOUR);
		createEReference(selfishMiningAttackEClass, SELFISH_MINING_ATTACK__MONITORED_NODES);

		stubbornMiningAttackEClass = createEClass(STUBBORN_MINING_ATTACK);
		createEAttribute(stubbornMiningAttackEClass, STUBBORN_MINING_ATTACK__LEAD_THRESHOLD);
		createEAttribute(stubbornMiningAttackEClass, STUBBORN_MINING_ATTACK__ABANDON_THRESHOLD);

		equalForkStubbornAttackEClass = createEClass(EQUAL_FORK_STUBBORN_ATTACK);
		createEAttribute(equalForkStubbornAttackEClass, EQUAL_FORK_STUBBORN_ATTACK__TIE_RESOLUTION_BIAS);

		leadStubbornAttackEClass = createEClass(LEAD_STUBBORN_ATTACK);
		createEAttribute(leadStubbornAttackEClass, LEAD_STUBBORN_ATTACK__MIN_LEAD_TO_PUBLISH);

		trailStubbornAttackEClass = createEClass(TRAIL_STUBBORN_ATTACK);
		createEAttribute(trailStubbornAttackEClass, TRAIL_STUBBORN_ATTACK__CONTINUE_ON_TRAIL);

		majorityAttackEClass = createEClass(MAJORITY_ATTACK);
		createEAttribute(majorityAttackEClass, MAJORITY_ATTACK__HASH_POWER_SHARE);
		createEAttribute(majorityAttackEClass, MAJORITY_ATTACK__EXPECTED_TAKEOVER_TIME);
		createEAttribute(majorityAttackEClass, MAJORITY_ATTACK__TARGET_CONFIRMATIONS_TO_BREAK);
		createEReference(majorityAttackEClass, MAJORITY_ATTACK__COLLUDING_POOLS);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized)
			return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		EntityPackage theEntityPackage = (EntityPackage) EPackage.Registry.INSTANCE.getEPackage(EntityPackage.eNS_URI);
		BlockchainsystemPackage theBlockchainsystemPackage = (BlockchainsystemPackage) EPackage.Registry.INSTANCE
				.getEPackage(BlockchainsystemPackage.eNS_URI);
		NodesystemPackage theNodesystemPackage = (NodesystemPackage) EPackage.Registry.INSTANCE
				.getEPackage(NodesystemPackage.eNS_URI);
		EcorePackage theEcorePackage = (EcorePackage) EPackage.Registry.INSTANCE.getEPackage(EcorePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		attackEClass.getESuperTypes().add(theEntityPackage.getEntity());
		attackScenarioEClass.getESuperTypes().add(theEntityPackage.getEntity());
		attackerNodeEClass.getESuperTypes().add(theEntityPackage.getEntity());
		attackResultEClass.getESuperTypes().add(theEntityPackage.getEntity());
		attackParameterEClass.getESuperTypes().add(theEntityPackage.getEntity());
		forkDepthEntryEClass.getESuperTypes().add(theEntityPackage.getEntity());
		raceAttackEClass.getESuperTypes().add(this.getAttack());
		finneyAttackEClass.getESuperTypes().add(this.getAttack());
		selfishMiningAttackEClass.getESuperTypes().add(this.getAttack());
		stubbornMiningAttackEClass.getESuperTypes().add(this.getAttack());
		equalForkStubbornAttackEClass.getESuperTypes().add(this.getStubbornMiningAttack());
		leadStubbornAttackEClass.getESuperTypes().add(this.getStubbornMiningAttack());
		trailStubbornAttackEClass.getESuperTypes().add(this.getStubbornMiningAttack());
		majorityAttackEClass.getESuperTypes().add(this.getAttack());

		// Initialize classes and features; add operations and parameters
		initEClass(attackEClass, Attack.class, "Attack", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getAttack_Type(), ecorePackage.getEString(), "type", null, 0, 1, Attack.class, !IS_TRANSIENT,
				!IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttack_RequiredHashPower(), ecorePackage.getEDouble(), "requiredHashPower", null, 0, 1,
				Attack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				!IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttack_DependsOnNetworkDelay(), ecorePackage.getEBoolean(), "dependsOnNetworkDelay", null, 0,
				1, Attack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				!IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttack_BaselineSuccessProbability(), ecorePackage.getEDouble(), "baselineSuccessProbability",
				null, 0, 1, Attack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				!IS_DERIVED, IS_ORDERED);
		initEReference(getAttack_Parameters(), this.getAttackParameter(), null, "parameters", null, 0, -1, Attack.class,
				!IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE,
				IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttack_Gamma(), ecorePackage.getEDouble(), "gamma", "0", 0, 1, Attack.class, !IS_TRANSIENT,
				!IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(attackScenarioEClass, AttackScenario.class, "AttackScenario", !IS_ABSTRACT, !IS_INTERFACE,
				IS_GENERATED_INSTANCE_CLASS);
		initEReference(getAttackScenario_Attack(), this.getAttack(), null, "attack", null, 1, 1, AttackScenario.class,
				!IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE,
				IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAttackScenario_Attackers(), this.getAttackerNode(), null, "attackers", null, 0, -1,
				AttackScenario.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES,
				!IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAttackScenario_BlockchainSystem(), theBlockchainsystemPackage.getBlockchainSystem(), null,
				"blockchainSystem", null, 1, 1, AttackScenario.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE,
				!IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAttackScenario_Result(), this.getAttackResult(), null, "result", null, 0, 1,
				AttackScenario.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES,
				!IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(attackerNodeEClass, AttackerNode.class, "AttackerNode", !IS_ABSTRACT, !IS_INTERFACE,
				IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getAttackerNode_PowerShare(), ecorePackage.getEDouble(), "powerShare", null, 0, 1,
				AttackerNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				!IS_DERIVED, IS_ORDERED);
		initEReference(getAttackerNode_LinkedNodeSystem(), theNodesystemPackage.getBlockchainSystemNodeSystem(), null,
				"linkedNodeSystem", null, 0, 1, AttackerNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE,
				!IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(attackResultEClass, AttackResult.class, "AttackResult", !IS_ABSTRACT, !IS_INTERFACE,
				IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getAttackResult_Success(), ecorePackage.getEBoolean(), "success", null, 0, 1, AttackResult.class,
				!IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttackResult_Duration(), ecorePackage.getEDouble(), "duration", null, 0, 1,
				AttackResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				!IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttackResult_AffectedBlocks(), ecorePackage.getEInt(), "affectedBlocks", null, 0, 1,
				AttackResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				!IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttackResult_SuccessProbabilityObserved(), ecorePackage.getEDouble(),
				"successProbabilityObserved", null, 0, 1, AttackResult.class, !IS_TRANSIENT, !IS_VOLATILE,
				IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttackResult_AvgForkRate(), ecorePackage.getEDouble(), "avgForkRate", null, 0, 1,
				AttackResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttackResult_StaleBlockRate(), ecorePackage.getEDouble(), "staleBlockRate", null, 0, 1,
				AttackResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttackResult_DoubleSpendCount(), ecorePackage.getEInt(), "doubleSpendCount", null, 0, 1,
				AttackResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttackResult_TimeToTakeover(), ecorePackage.getEDouble(), "timeToTakeover", null, 0, 1,
				AttackResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				IS_DERIVED, IS_ORDERED);
		initEReference(getAttackResult_ForkDepthDistribution(), this.getForkDepthEntry(), null, "forkDepthDistribution",
				null, 0, -1, AttackResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE,
				!IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttackResult_AttackerRevenueRatio(), theEcorePackage.getEDouble(), "attackerRevenueRatio",
				null, 0, 1, AttackResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
				IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(attackParameterEClass, AttackParameter.class, "AttackParameter", !IS_ABSTRACT, !IS_INTERFACE,
				IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getAttackParameter_Key(), ecorePackage.getEString(), "key", null, 0, 1, AttackParameter.class,
				!IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttackParameter_Value(), ecorePackage.getEString(), "value", null, 0, 1,
				AttackParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				!IS_DERIVED, IS_ORDERED);

		initEClass(forkDepthEntryEClass, ForkDepthEntry.class, "ForkDepthEntry", !IS_ABSTRACT, !IS_INTERFACE,
				IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getForkDepthEntry_Depth(), ecorePackage.getEInt(), "depth", null, 0, 1, ForkDepthEntry.class,
				!IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getForkDepthEntry_Count(), ecorePackage.getEInt(), "count", null, 0, 1, ForkDepthEntry.class,
				!IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(raceAttackEClass, RaceAttack.class, "RaceAttack", !IS_ABSTRACT, !IS_INTERFACE,
				IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRaceAttack_PropagationAdvantage(), ecorePackage.getELong(), "propagationAdvantage", null, 0,
				1, RaceAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				!IS_DERIVED, IS_ORDERED);
		initEAttribute(getRaceAttack_TransactionADelay(), ecorePackage.getELong(), "transactionADelay", null, 0, 1,
				RaceAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				!IS_DERIVED, IS_ORDERED);
		initEAttribute(getRaceAttack_TransactionBAcceleration(), ecorePackage.getELong(), "transactionBAcceleration",
				null, 0, 1, RaceAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
				IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRaceAttack_FeeDifferenceThreshold(), ecorePackage.getEDouble(), "feeDifferenceThreshold",
				null, 0, 1, RaceAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
				IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRaceAttack_ZeroConfAcceptanceRisk(), ecorePackage.getEDouble(), "zeroConfAcceptanceRisk",
				null, 0, 1, RaceAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
				IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRaceAttack_ObservedDoubleSpends(), ecorePackage.getEInt(), "observedDoubleSpends", null, 0, 1,
				RaceAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				IS_DERIVED, IS_ORDERED);
		initEAttribute(getRaceAttack_AvgPropagationAdvMs(), ecorePackage.getELong(), "avgPropagationAdvMs", null, 0, 1,
				RaceAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				IS_DERIVED, IS_ORDERED);

		initEClass(finneyAttackEClass, FinneyAttack.class, "FinneyAttack", !IS_ABSTRACT, !IS_INTERFACE,
				IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getFinneyAttack_NumberOfPreminedBlocks(), ecorePackage.getEInt(), "numberOfPreminedBlocks", null,
				0, 1, FinneyAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				!IS_DERIVED, IS_ORDERED);
		initEAttribute(getFinneyAttack_WithholdTime(), ecorePackage.getELong(), "withholdTime", null, 0, 1,
				FinneyAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				!IS_DERIVED, IS_ORDERED);
		initEAttribute(getFinneyAttack_MerchantAcceptsZeroConf(), ecorePackage.getEBoolean(), "merchantAcceptsZeroConf",
				null, 0, 1, FinneyAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
				IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFinneyAttack_DoubleSpendSuccessCount(), ecorePackage.getEInt(), "doubleSpendSuccessCount",
				null, 0, 1, FinneyAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
				IS_UNIQUE, IS_DERIVED, IS_ORDERED);
		initEAttribute(getFinneyAttack_ObservedTimeToReleaseMs(), ecorePackage.getELong(), "observedTimeToReleaseMs",
				null, 0, 1, FinneyAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
				IS_UNIQUE, IS_DERIVED, IS_ORDERED);

		initEClass(selfishMiningAttackEClass, SelfishMiningAttack.class, "SelfishMiningAttack", !IS_ABSTRACT,
				!IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSelfishMiningAttack_PrivateChainLeadTarget(), ecorePackage.getEInt(),
				"privateChainLeadTarget", null, 0, 1, SelfishMiningAttack.class, !IS_TRANSIENT, !IS_VOLATILE,
				IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSelfishMiningAttack_WithholdProbability(), ecorePackage.getEDouble(), "withholdProbability",
				null, 0, 1, SelfishMiningAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE,
				!IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSelfishMiningAttack_PublishOnCatchup(), ecorePackage.getEBoolean(), "publishOnCatchup", null,
				0, 1, SelfishMiningAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
				IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSelfishMiningAttack_ExpectedForksPerHour(), ecorePackage.getEDouble(), "expectedForksPerHour",
				null, 0, 1, SelfishMiningAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE,
				!IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
		initEReference(getSelfishMiningAttack_MonitoredNodes(), this.getAttackerNode(), null, "monitoredNodes", null, 0,
				-1, SelfishMiningAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE,
				IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(stubbornMiningAttackEClass, StubbornMiningAttack.class, "StubbornMiningAttack", IS_ABSTRACT,
				!IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getStubbornMiningAttack_LeadThreshold(), ecorePackage.getEInt(), "leadThreshold", null, 0, 1,
				StubbornMiningAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
				IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStubbornMiningAttack_AbandonThreshold(), ecorePackage.getEInt(), "abandonThreshold", null, 0,
				1, StubbornMiningAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
				IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(equalForkStubbornAttackEClass, EqualForkStubbornAttack.class, "EqualForkStubbornAttack",
				!IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getEqualForkStubbornAttack_TieResolutionBias(), ecorePackage.getEDouble(), "tieResolutionBias",
				null, 0, 1, EqualForkStubbornAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE,
				!IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(leadStubbornAttackEClass, LeadStubbornAttack.class, "LeadStubbornAttack", !IS_ABSTRACT,
				!IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getLeadStubbornAttack_MinLeadToPublish(), ecorePackage.getEInt(), "minLeadToPublish", null, 0, 1,
				LeadStubbornAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				!IS_DERIVED, IS_ORDERED);

		initEClass(trailStubbornAttackEClass, TrailStubbornAttack.class, "TrailStubbornAttack", !IS_ABSTRACT,
				!IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTrailStubbornAttack_ContinueOnTrail(), ecorePackage.getEBoolean(), "continueOnTrail", null, 0,
				1, TrailStubbornAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
				IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(majorityAttackEClass, MajorityAttack.class, "MajorityAttack", !IS_ABSTRACT, !IS_INTERFACE,
				IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMajorityAttack_HashPowerShare(), ecorePackage.getEDouble(), "hashPowerShare", null, 0, 1,
				MajorityAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
				!IS_DERIVED, IS_ORDERED);
		initEAttribute(getMajorityAttack_ExpectedTakeoverTime(), ecorePackage.getEDouble(), "expectedTakeoverTime",
				null, 0, 1, MajorityAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
				IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMajorityAttack_TargetConfirmationsToBreak(), ecorePackage.getEInt(),
				"targetConfirmationsToBreak", null, 0, 1, MajorityAttack.class, !IS_TRANSIENT, !IS_VOLATILE,
				IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getMajorityAttack_ColludingPools(), this.getAttackerNode(), null, "colludingPools", null, 0, -1,
				MajorityAttack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES,
				!IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);
	}

} //AttackmodelPackageImpl
