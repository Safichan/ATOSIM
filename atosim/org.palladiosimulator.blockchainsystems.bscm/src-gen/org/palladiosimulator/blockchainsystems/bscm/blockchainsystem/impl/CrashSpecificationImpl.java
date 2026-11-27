/**
 */
package org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.BlockchainsystemPackage;
import org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.CrashSpecification;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Crash Specification</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.impl.CrashSpecificationImpl#isCrashEnabled <em>Crash Enabled</em>}</li>
 *   <li>{@link org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.impl.CrashSpecificationImpl#getCrashDuration <em>Crash Duration</em>}</li>
 *   <li>{@link org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.impl.CrashSpecificationImpl#getUnavailableNodeFraction <em>Unavailable Node Fraction</em>}</li>
 *   <li>{@link org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.impl.CrashSpecificationImpl#getCrashStartAfterBlockIntervals <em>Crash Start After Block Intervals</em>}</li>
 * </ul>
 *
 * @generated
 */
public class CrashSpecificationImpl extends MinimalEObjectImpl.Container implements CrashSpecification {
	/**
	 * The default value of the '{@link #isCrashEnabled() <em>Crash Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isCrashEnabled()
	 * @generated
	 * @ordered
	 */
	protected static final boolean CRASH_ENABLED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isCrashEnabled() <em>Crash Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isCrashEnabled()
	 * @generated
	 * @ordered
	 */
	protected boolean crashEnabled = CRASH_ENABLED_EDEFAULT;

	/**
	 * The default value of the '{@link #getCrashDuration() <em>Crash Duration</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCrashDuration()
	 * @generated
	 * @ordered
	 */
	protected static final double CRASH_DURATION_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getCrashDuration() <em>Crash Duration</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCrashDuration()
	 * @generated
	 * @ordered
	 */
	protected double crashDuration = CRASH_DURATION_EDEFAULT;

	/**
	 * The default value of the '{@link #getUnavailableNodeFraction() <em>Unavailable Node Fraction</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnavailableNodeFraction()
	 * @generated
	 * @ordered
	 */
	protected static final double UNAVAILABLE_NODE_FRACTION_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getUnavailableNodeFraction() <em>Unavailable Node Fraction</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnavailableNodeFraction()
	 * @generated
	 * @ordered
	 */
	protected double unavailableNodeFraction = UNAVAILABLE_NODE_FRACTION_EDEFAULT;

	/**
	 * The default value of the '{@link #getCrashStartAfterBlockIntervals() <em>Crash Start After Block Intervals</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCrashStartAfterBlockIntervals()
	 * @generated
	 * @ordered
	 */
	protected static final int CRASH_START_AFTER_BLOCK_INTERVALS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getCrashStartAfterBlockIntervals() <em>Crash Start After Block Intervals</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCrashStartAfterBlockIntervals()
	 * @generated
	 * @ordered
	 */
	protected int crashStartAfterBlockIntervals = CRASH_START_AFTER_BLOCK_INTERVALS_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CrashSpecificationImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BlockchainsystemPackage.Literals.CRASH_SPECIFICATION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isCrashEnabled() {
		return crashEnabled;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCrashEnabled(boolean newCrashEnabled) {
		boolean oldCrashEnabled = crashEnabled;
		crashEnabled = newCrashEnabled;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					BlockchainsystemPackage.CRASH_SPECIFICATION__CRASH_ENABLED, oldCrashEnabled, crashEnabled));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getCrashDuration() {
		return crashDuration;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCrashDuration(double newCrashDuration) {
		double oldCrashDuration = crashDuration;
		crashDuration = newCrashDuration;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					BlockchainsystemPackage.CRASH_SPECIFICATION__CRASH_DURATION, oldCrashDuration, crashDuration));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public double getUnavailableNodeFraction() {
		return unavailableNodeFraction;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setUnavailableNodeFraction(double newUnavailableNodeFraction) {
		double oldUnavailableNodeFraction = unavailableNodeFraction;
		unavailableNodeFraction = newUnavailableNodeFraction;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					BlockchainsystemPackage.CRASH_SPECIFICATION__UNAVAILABLE_NODE_FRACTION, oldUnavailableNodeFraction,
					unavailableNodeFraction));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getCrashStartAfterBlockIntervals() {
		return crashStartAfterBlockIntervals;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCrashStartAfterBlockIntervals(int newCrashStartAfterBlockIntervals) {
		int oldCrashStartAfterBlockIntervals = crashStartAfterBlockIntervals;
		crashStartAfterBlockIntervals = newCrashStartAfterBlockIntervals;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					BlockchainsystemPackage.CRASH_SPECIFICATION__CRASH_START_AFTER_BLOCK_INTERVALS,
					oldCrashStartAfterBlockIntervals, crashStartAfterBlockIntervals));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case BlockchainsystemPackage.CRASH_SPECIFICATION__CRASH_ENABLED:
			return isCrashEnabled();
		case BlockchainsystemPackage.CRASH_SPECIFICATION__CRASH_DURATION:
			return getCrashDuration();
		case BlockchainsystemPackage.CRASH_SPECIFICATION__UNAVAILABLE_NODE_FRACTION:
			return getUnavailableNodeFraction();
		case BlockchainsystemPackage.CRASH_SPECIFICATION__CRASH_START_AFTER_BLOCK_INTERVALS:
			return getCrashStartAfterBlockIntervals();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case BlockchainsystemPackage.CRASH_SPECIFICATION__CRASH_ENABLED:
			setCrashEnabled((Boolean) newValue);
			return;
		case BlockchainsystemPackage.CRASH_SPECIFICATION__CRASH_DURATION:
			setCrashDuration((Double) newValue);
			return;
		case BlockchainsystemPackage.CRASH_SPECIFICATION__UNAVAILABLE_NODE_FRACTION:
			setUnavailableNodeFraction((Double) newValue);
			return;
		case BlockchainsystemPackage.CRASH_SPECIFICATION__CRASH_START_AFTER_BLOCK_INTERVALS:
			setCrashStartAfterBlockIntervals((Integer) newValue);
			return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case BlockchainsystemPackage.CRASH_SPECIFICATION__CRASH_ENABLED:
			setCrashEnabled(CRASH_ENABLED_EDEFAULT);
			return;
		case BlockchainsystemPackage.CRASH_SPECIFICATION__CRASH_DURATION:
			setCrashDuration(CRASH_DURATION_EDEFAULT);
			return;
		case BlockchainsystemPackage.CRASH_SPECIFICATION__UNAVAILABLE_NODE_FRACTION:
			setUnavailableNodeFraction(UNAVAILABLE_NODE_FRACTION_EDEFAULT);
			return;
		case BlockchainsystemPackage.CRASH_SPECIFICATION__CRASH_START_AFTER_BLOCK_INTERVALS:
			setCrashStartAfterBlockIntervals(CRASH_START_AFTER_BLOCK_INTERVALS_EDEFAULT);
			return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case BlockchainsystemPackage.CRASH_SPECIFICATION__CRASH_ENABLED:
			return crashEnabled != CRASH_ENABLED_EDEFAULT;
		case BlockchainsystemPackage.CRASH_SPECIFICATION__CRASH_DURATION:
			return crashDuration != CRASH_DURATION_EDEFAULT;
		case BlockchainsystemPackage.CRASH_SPECIFICATION__UNAVAILABLE_NODE_FRACTION:
			return unavailableNodeFraction != UNAVAILABLE_NODE_FRACTION_EDEFAULT;
		case BlockchainsystemPackage.CRASH_SPECIFICATION__CRASH_START_AFTER_BLOCK_INTERVALS:
			return crashStartAfterBlockIntervals != CRASH_START_AFTER_BLOCK_INTERVALS_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy())
			return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (crashEnabled: ");
		result.append(crashEnabled);
		result.append(", crashDuration: ");
		result.append(crashDuration);
		result.append(", unavailableNodeFraction: ");
		result.append(unavailableNodeFraction);
		result.append(", crashStartAfterBlockIntervals: ");
		result.append(crashStartAfterBlockIntervals);
		result.append(')');
		return result.toString();
	}

} //CrashSpecificationImpl
