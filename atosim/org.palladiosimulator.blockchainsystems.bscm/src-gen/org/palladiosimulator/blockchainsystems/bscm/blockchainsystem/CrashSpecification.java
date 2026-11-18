/**
 */
package org.palladiosimulator.blockchainsystems.bscm.blockchainsystem;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Crash Specification</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.CrashSpecification#isCrashEnabled <em>Crash Enabled</em>}</li>
 *   <li>{@link org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.CrashSpecification#getCrashDuration <em>Crash Duration</em>}</li>
 *   <li>{@link org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.CrashSpecification#getUnavailableNodeFraction <em>Unavailable Node Fraction</em>}</li>
 * </ul>
 *
 * @see org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.BlockchainsystemPackage#getCrashSpecification()
 * @model
 * @generated
 */
public interface CrashSpecification extends EObject {
	/**
	 * Returns the value of the '<em><b>Crash Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Crash Enabled</em>' attribute.
	 * @see #setCrashEnabled(boolean)
	 * @see org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.BlockchainsystemPackage#getCrashSpecification_CrashEnabled()
	 * @model
	 * @generated
	 */
	boolean isCrashEnabled();

	/**
	 * Sets the value of the '{@link org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.CrashSpecification#isCrashEnabled <em>Crash Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Crash Enabled</em>' attribute.
	 * @see #isCrashEnabled()
	 * @generated
	 */
	void setCrashEnabled(boolean value);

	/**
	 * Returns the value of the '<em><b>Crash Duration</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Crash Duration</em>' attribute.
	 * @see #setCrashDuration(double)
	 * @see org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.BlockchainsystemPackage#getCrashSpecification_CrashDuration()
	 * @model
	 * @generated
	 */
	double getCrashDuration();

	/**
	 * Sets the value of the '{@link org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.CrashSpecification#getCrashDuration <em>Crash Duration</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Crash Duration</em>' attribute.
	 * @see #getCrashDuration()
	 * @generated
	 */
	void setCrashDuration(double value);

	/**
	 * Returns the value of the '<em><b>Unavailable Node Fraction</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Unavailable Node Fraction</em>' attribute.
	 * @see #setUnavailableNodeFraction(double)
	 * @see org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.BlockchainsystemPackage#getCrashSpecification_UnavailableNodeFraction()
	 * @model
	 * @generated
	 */
	double getUnavailableNodeFraction();

	/**
	 * Sets the value of the '{@link org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.CrashSpecification#getUnavailableNodeFraction <em>Unavailable Node Fraction</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Unavailable Node Fraction</em>' attribute.
	 * @see #getUnavailableNodeFraction()
	 * @generated
	 */
	void setUnavailableNodeFraction(double value);

} // CrashSpecification
