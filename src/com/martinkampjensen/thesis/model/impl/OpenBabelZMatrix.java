/**
 * Copyright 2010-2011 Martin Kamp Jensen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.martinkampjensen.thesis.model.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openbabel.OBAtom;
import org.openbabel.OBConformerSearch;
import org.openbabel.OBConversion;
import org.openbabel.OBFFConstraints;
import org.openbabel.OBForceField;
import org.openbabel.OBRMSDConformerScore;

import com.martinkampjensen.thesis.Main;
import com.martinkampjensen.thesis.StatusCode;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.model.ZMatrix;
import com.martinkampjensen.thesis.util.Debug;
import com.martinkampjensen.thesis.util.openbabel.ImmutableZMatrix;
import com.martinkampjensen.thesis.util.openbabel.OBMol;
import com.martinkampjensen.thesis.util.openbabel.OpenBabel;
import com.martinkampjensen.thesis.util.openbabel.OpenBabelData;
import com.martinkampjensen.thesis.util.openbabel.OptimizationAlgorithm;

/**
 * An implementation of the {@link ZMatrix} interface. This implementation is
 * using the structures of Open Babel to calculate the fitness value.
 * <p>
 * This class works in one of two possible ways:
 * <ol>
 * <li>Cartesian based: When the fitness value is evaluated, the Cartesian
 * coordinates of all atoms are calculated and assigned to the Open Babel
 * structures representing the atoms. Then the Open Babel molecule is assigned
 * to a force field that calculates the energy value which is the fitness value.
 * <li>Torsion based: When the fitness value is evaluated, the torsion angles
 * are assigned to the Open Babel molecule. Then the Open Babel molecule is
 * assigned to a force field that calculates the energy value which is the
 * fitness value. Note: When assigning torsion angles to an Open Babel molecule,
 * bonded neighbors are automatically rotated. This means that the actual
 * torsion angles of the Open Babel molecule are not the same as the torsion
 * angles of the Z-matrix. Be careful!
 * </ol>
 * Note that this implementation is incomplete with regard to ignoring the use
 * of methods such as {@link ZMatrixImpl#addRow(jbcl.data.dict.AtomType, int,
 * int, int, double, double, double)} which is not supposed to be used for this
 * class. That is, this class does not support constructing a Z-matrix from
 * scratch, it only supports constructing a Z-matrix from input data from Open
 * Babel.
 * 
 * @see <a href="http://openbabel.org">Open Babel</a>
 */
public abstract class OpenBabelZMatrix extends ZMatrixImpl
implements Serializable
{
	private static final long serialVersionUID = 6569871238913007279L;
	private static final int ATOM_A = 0;
	private static final int ATOM_B = 1;
	private static final int ATOM_C = 2;
	private static final int ATOM_D = 3;
	private static final OBConversion _pdbConverter;
	private final transient OBMol _molecule;
	private final transient OBForceField _forceField;
	private final transient ImmutableZMatrix _imz;
	private final transient OBAtom[][] _atomMatrix;
	private final transient OBAtom[][] _additionalAtomMatrix;

	static
	{
		_pdbConverter = new OBConversion();
		_pdbConverter.SetInAndOutFormats(OpenBabel.FORMAT_PDB,
				OpenBabel.FORMAT_PDB);
	}

	protected OpenBabelZMatrix(OpenBabelData obData)
	{
		super(obData);

		final int size = size();
		final int additionalSize = additionalSize();

		_molecule = copyMolecule(obData.getMolecule());
		_forceField = copyForceField(obData.getForceField());
		_imz = obData.getZMatrix();
		_atomMatrix = createAtomMatrix(size);
		_additionalAtomMatrix = createAdditionalAtomMatrix(additionalSize);

		updateAtomMatrix(_atomMatrix, _imz, _molecule);
		updateAdditionalAtomMatrix(_additionalAtomMatrix, _imz, _molecule);
	}

	protected OpenBabelZMatrix(OpenBabelZMatrix zMatrix)
	{
		this(zMatrix, null);
	}

	protected OpenBabelZMatrix(OpenBabelZMatrix zMatrix, OBMol molecule)
	{
		super(zMatrix);

		final int size = size();
		final int additionalSize = additionalSize();

		_molecule = (molecule == null ?
				copyMolecule(zMatrix._molecule) : molecule);
		_forceField = copyForceField(zMatrix._forceField);
		_imz = zMatrix._imz;
		_atomMatrix = createAtomMatrix(size);
		_additionalAtomMatrix = createAdditionalAtomMatrix(additionalSize);

		updateAtomMatrix(_atomMatrix, _imz, _molecule);
		updateAdditionalAtomMatrix(_additionalAtomMatrix, _imz, _molecule);

		if(molecule != null) {
			updateModel(_atomMatrix, _additionalAtomMatrix, _molecule);
		}
	}

	/**
	 * Constructs a new Z-matrix backed by Open Babel structures. Calling this
	 * method is equal to calling {@link #createCartesianBased(OpenBabelData)}.
	 * 
	 * @param obData the data from Open Babel to use.
	 * @throws NullPointerException if <code>obData == null</code>.
	 */
	public static final OpenBabelZMatrix create(OpenBabelData obData)
	{
		return createCartesianBased(obData);
	}

	/**
	 * Constructs a new Cartesian based Z-matrix backed by Open Babel
	 * structures.
	 * 
	 * @param obData the data from Open Babel to use.
	 * @throws NullPointerException if <code>obData == null</code>.
	 */
	public static final OpenBabelZMatrix createCartesianBased(
			OpenBabelData obData)
	{
		return new CartesianBasedOpenBabelZMatrix(obData);
	}

	/**
	 * Constructs a new torsion based Z-matrix backed by Open Babel structures.
	 * 
	 * @param obData the data from Open Babel to use.
	 * @throws NullPointerException if <code>obData == null</code>.
	 */
	public static final OpenBabelZMatrix createTorsionBased(
			OpenBabelData obData)
	{
		return new TorsionBasedOpenBabelZMatrix(obData);
	}

	@Override
	public final String toPdb()
	{
		// TODO: It would of course be smart to detect whether or not the follow two call are necessary.
		calculatePositions();
		updateOpenBabel();
		return _pdbConverter.WriteString(_molecule);
	}

	/**
	 * Returns a PDB representation of this model.
	 * 
	 * @param molecule the exact molecule of which this model is a conformation.
	 * @return the PDB representation.
	 */
	public String toPdb(OBMol molecule)
	{
		final int offset = NUMBER_OF_REFERENCE_ROWS + 1;

		for(int i = -NUMBER_OF_REFERENCE_ROWS; i < 0; i++) {
			final OBAtom atom = molecule.GetAtom(i + offset);
			final double[] xyz = getAdditional(i);
			atom.SetVector(xyz[0], xyz[1], xyz[2]);
		}

		for(int i = 0, n = size(); i < n; i++) {
			final OBAtom atom = molecule.GetAtom(i + offset);
			final double[] xyz = get(i);
			atom.SetVector(xyz[0], xyz[1], xyz[2]);
		}

		return OpenBabel.toPdb(molecule);
	}

	/**
	 * Conformer searching using a genetic algorithm.
	 * <p>
	 * The genetic algorithm starts by generating the initial population of
	 * rotor keys. A rotor key is simply an array of values specifying the
	 * rotations around rotatable bonds. The initial population contains up to
	 * <code>nConformers</code>, duplicate keys are ignored.
	 * <p>
	 * For each generation, <code>nChildren</code> children are created by
	 * permuting the parent rotor keys. The <code>mutability</code> setting
	 * determines how frequent a permutation is made (e.g. 5 means 1/5 bonds are
	 * permuted, 10 means 1/10). Again, duplicated and filtered molecules are
	 * ignored. The population now contains up to <code>nConformers * (1 +
	 * nChildren)</code>.
	 * <p>
	 * New generations are generated until the specified number of generations
	 * (i.e. <code>convergence</code>) don't improve the score.
	 * 
	 * @param nConformers the number of conformers that should be generated.
	 *        This is also the number of conformers selected for each
	 *        generation.
	 * @param nChildren when a new generation is generated, for each of the
	 *        <code>nConformers</code> conformers, <code>nChildren</code>
	 *        children are created.
	 * @param mutability the mutability determines how frequent a permutation
	 *        occurs when generating the next generation.
	 * @param convergence the number of identical generations before considering
	 *        the process converged.
	 * @return a list of generated models.
	 * @throws IllegalArgumentException if <code>nConformers &lt; 1</code> or if
	 *         <code>nChildren &lt; 1</code> or if
	 *         <code>mutability &lt; 1</code> or if
	 *         <code>convergence &lt; 1</code>.
	 * @see <a href="http://goo.gl/45qlR">Conformer Searching</a>
	 * @see <a href="http://goo.gl/FQx0n">OBConformerSearch</a>
	 */
	public List<Model> conformerSearch(int nConformers, int nChildren,
			int mutability, int convergence)
	{
		if(nConformers < 1) {
			throw new IllegalArgumentException("nConformers < 1");
		}
		else if(nChildren < 1) {
			throw new IllegalArgumentException("nChildren < 1");
		}
		else if(mutability < 1) {
			throw new IllegalArgumentException("mutability < 1");
		}
		else if(convergence < 1) {
			throw new IllegalArgumentException("convergence < 1");
		}

		final OBConformerSearch cs = new OBConformerSearch();
		final OBMol copy = new OBMol(_molecule);

		if(!cs.Setup(copy, nConformers, nChildren, mutability, convergence)) {
			Main.errorExit("Could not initialize conformer generation using "
					+ "Open Babel", StatusCode.CONFORMERS);
		}

		Debug.line("calculateConformers (%d conformers, %d children, "
				+ "%d mutability, %d convergence)", nConformers, nChildren,
				mutability, convergence);

		cs.SetScore(new OBRMSDConformerScore());
		cs.Search();
		cs.GetConformers(copy);
		final List<Model> conformers = new ArrayList<Model>(nConformers);

		for(int i = 0; i < nConformers; i++) {
			final OBMol conformer = new OBMol(copy);
			conformer.SetConformer(i);

			// Delete remaining conformers. Note that conformers seem to be
			// stored internally by Open Babel in a way that reorders conformer
			// ids to always be >= 0 and < numConformers.
			for(int j = nConformers - 1; j > i; j--) {
				conformer.DeleteConformer(j);
			}
			for(int j = i - 1; j >= 0; j--) {
				conformer.DeleteConformer(j);
			}

			conformers.add(createConformer(conformer));
		}

		copy.delete();
		cs.delete();

		return conformers;
	}

	/**
	 * Minimizes this Z-matrix with regard to fitness value using one of the
	 * built-in methods of Open Babel.
	 * 
	 * @param algorithm the algorithm to use.
	 * @param steps the number of steps to take.
	 * @param convergence the convergence criterion.
	 */
	public void minimize(OptimizationAlgorithm algorithm, int steps,
			double convergence)
	{
		updateMolecule(_molecule, _atomMatrix, _additionalAtomMatrix);
		updateForceField(_forceField, _molecule);

		switch(algorithm) {
		case STEEPEST_DESCENT:
			_forceField.SteepestDescent(steps, convergence);
			break;
		case CONJUGATE_GRADIENTS:
			_forceField.ConjugateGradients(steps, convergence);
			break;
		}

		updateMolecule(_molecule, _forceField);
		updateModel(_atomMatrix, _additionalAtomMatrix, _molecule);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation uses Open Babel and depends on how this object was
	 * constructed. Subclasses that override this method should remember to call
	 * {@link #updateOpenBabel()} if the latest state should be reflected in
	 * Open Babel structures.
	 * 
	 * @see <a href="http://openbabel.org">Open Babel</a>
	 * @see #createCartesianBased(OpenBabelData)
	 * @see #createTorsionBased(OpenBabelData)
	 */
	@Override
	protected double calculateFitness()
	{
		updateOpenBabel();
		return _forceField.Energy();
	}

	/**
	 * Updates the Open Babel structures.
	 */
	protected final void updateOpenBabel()
	{
		updateMolecule(_molecule, _atomMatrix, _additionalAtomMatrix);
		updateForceField(_forceField, _molecule);
	}

	/**
	 * Sets the values of this object to those of a specific conformation. Note
	 * that the behavior of this method and subsequent methods calls on this
	 * object is undefined if the conformation is not a conformation of the
	 * molecule represented by this object.
	 * 
	 * @param pdb the conformation.
	 */
	protected final void fromPdb(String pdb)
	{
		_pdbConverter.ReadString(_molecule, pdb);

		updateAtomMatrix(_atomMatrix, _imz, _molecule);
		updateAdditionalAtomMatrix(_additionalAtomMatrix, _imz, _molecule);
		updateModel(_atomMatrix, _additionalAtomMatrix, _molecule);

		// Force recalculation of fitness next time evaluate() is called.
		setHasChanged(true);
	}

	/**
	 * Updates a Open Babel molecule, for example using the torsion angles or
	 * the Cartesian coordinates of the atoms of the Z-matrix.
	 */
	protected abstract void updateMolecule(OBMol molecule,
			OBAtom[][] atomMatrix, OBAtom[][] additionalAtomMatrix);

	/**
	 * Creates and returns a new Z-matrix based on the same molecule as this
	 * object, but with bond lengths, bond angles, and torsion angles set to
	 * those in a specific conformation of the molecule.
	 * 
	 * @param molecule the molecule (the conformation).
	 * @return the new Z-matrix.
	 */
	protected abstract OpenBabelZMatrix createConformer(OBMol molecule);

	private static final OBMol copyMolecule(OBMol molecule)
	{
		return new OBMol(molecule);
	}

	private static final OBForceField copyForceField(OBForceField forceField)
	{
		// Implementation note: The MakeNewInstance() method of class
		// OBForceField eventually results in a crash in native code. Hence,
		// the following implementation is used.

		final String id = forceField.GetID();
		final OBFFConstraints constraints = forceField.GetConstraints();

		final OBForceField copy = OBForceField.FindForceField(id);
		copy.SetConstraints(constraints);

		return copy;
	}

	private static final OBAtom[][] createAtomMatrix(int size)
	{
		return new OBAtom[size][OpenBabelData.TORSION_LENGTH];
	}

	private static final OBAtom[][] createAdditionalAtomMatrix(
			int additionalSize)
	{
		return new OBAtom[additionalSize][OpenBabelData.TORSION_LENGTH - 1];
	}

	/**
	 * Updates a matrix containing references to the atoms in a molecule. Must
	 * be called when the molecule is updated, for example when extracting a
	 * conformer from the force field to the molecule.
	 */
	private static final void updateAtomMatrix(OBAtom[][] atomMatrix,
			ImmutableZMatrix imz, OBMol molecule)
	{
		final int size = imz.torsionAtoms();
		final int offset = ImmutableZMatrix.MINIMUM_TORSION_ID;

		for(int i = 0; i < size; i++) {
			final int atomA = i + offset;
			final int atomB = imz.bondLengthAtomId(atomA);
			final int atomC = imz.bondAngleAtomId(atomA);
			final int atomD = imz.torsionAngleAtomId(atomA);

			final OBAtom[] row = atomMatrix[i];
			row[ATOM_A] = molecule.GetAtom(atomA);
			row[ATOM_B] = molecule.GetAtom(atomB);
			row[ATOM_C] = molecule.GetAtom(atomC);
			row[ATOM_D] = molecule.GetAtom(atomD);
		}
	}

	/**
	 * Updates references to the additional atoms in a molecule (that is, the
	 * atoms defined without using a torsion angle in a Z-matrix). Must be
	 * called when the molecule is updated, for example when extracting a
	 * conformer from the force field to the molecule.
	 */
	private static final void updateAdditionalAtomMatrix(
			OBAtom[][] additionalAtomMatrix, ImmutableZMatrix imz,
			OBMol molecule)
	{
		final int nReferenceAtoms = imz.referenceAtoms();

		for(int i = ImmutableZMatrix.MINIMUM_ID; i <= nReferenceAtoms; i++) {
			final OBAtom[] row =
				additionalAtomMatrix[i - ImmutableZMatrix.MINIMUM_ID];

			row[ATOM_A] = molecule.GetAtom(i);

			if(i >= ImmutableZMatrix.MINIMUM_LENGTH_ID) {
				row[ATOM_B] = molecule.GetAtom(imz.bondLengthAtomId(i));

				if(i >= ImmutableZMatrix.MINIMUM_ANGLE_ID) {
					row[ATOM_C] = molecule.GetAtom(imz.bondAngleAtomId(i));
				}
			}
		}
	}

	/**
	 * Updates a force field with the information of a molecule. Must be called
	 * before extracting information from the force field about a molecule.
	 */
	private static final void updateForceField(OBForceField forceField,
			OBMol molecule)
	{
		forceField.Setup(molecule);
	}

	/**
	 * Updates a molecule with the information of a force field, that is,
	 * extracts the conformers contained in the force field (for example
	 * originating from a minimization).
	 */
	private static final void updateMolecule(OBMol molecule,
			OBForceField forceField)
	{
		forceField.GetConformers(molecule);
	}

	/**
	 * Updates the Z-matrix (the superclass) with the information of a molecule.
	 * Must be called after manipulating a molecule through other means than
	 * by changing the angles of the Z-matrix, for example after minimizing the
	 * molecule.
	 */
	private final void updateModel(OBAtom[][] atomMatrix,
			OBAtom[][] additionalAtomMatrix, OBMol molecule)
	{
		final int size = size();
		for(int i = 0; i < size; i++) {
			final OBAtom[] row = atomMatrix[i];

			final OBAtom a = row[ATOM_A];
			final OBAtom b = row[ATOM_B];
			final double ab = molecule.GetBond(a, b).GetLength();
			setBondLength(i, ab);

			final OBAtom c = row[ATOM_C];
			final double abcInDegrees = molecule.GetAngle(a, b, c); 
			setBondAngle(i, Math.toRadians(abcInDegrees));

			final OBAtom d = row[ATOM_D];
			final double abcdInDegrees = molecule.GetTorsion(a, b, c, d); 
			setTorsionAngle(i, Math.toRadians(abcdInDegrees));
		}

		final int additionalSize = additionalSize();
		for(int i = REF2ID; i < 0; i++) {
			final OBAtom[] row = additionalAtomMatrix[i + additionalSize];

			final OBAtom a = row[ATOM_A];
			final OBAtom b = row[ATOM_B];
			final double ab = molecule.GetBond(a, b).GetLength();
			setBondLength(i, ab);

			if(i == REF3ID) {
				final OBAtom c = row[ATOM_C];
				final double abcInDegrees = molecule.GetAngle(a, b, c); 
				setBondAngle(i, Math.toRadians(abcInDegrees));
			}
		}
	}

	/**
	 * Cartesian based {@link OpenBabelZMatrix}. When the fitness value is
	 * evaluated, the Cartesian coordinates of all atoms are calculated and
	 * assigned to the Open Babel structures representing the atoms. Then the
	 * Open Babel molecule is assigned to a force field that calculates the
	 * energy value which is the fitness value.
	 */
	protected static class CartesianBasedOpenBabelZMatrix
	extends OpenBabelZMatrix
	{
		private static final long serialVersionUID = -1102237962022045547L;

		protected CartesianBasedOpenBabelZMatrix(OpenBabelData obData)
		{
			super(obData);
		}

		protected CartesianBasedOpenBabelZMatrix(
				CartesianBasedOpenBabelZMatrix zMatrix)
		{
			super(zMatrix);
		}

		protected CartesianBasedOpenBabelZMatrix(
				CartesianBasedOpenBabelZMatrix zMatrix, OBMol molecule)
		{
			super(zMatrix, molecule);
		}

		@Override
		public CartesianBasedOpenBabelZMatrix copy()
		{
			return new CartesianBasedOpenBabelZMatrix(this);
		}

		@Override
		protected final void updateMolecule(OBMol molecule,
				OBAtom[][] atomMatrix, OBAtom[][] additionalAtomMatrix)
		{
			final int size = size();
			for(int i = 0; i < size; i++) {
				final OBAtom atom = atomMatrix[i][ATOM_A];
				final double[] pos = get(i);
				atom.SetVector(pos[X], pos[Y], pos[Z]);
			}

			final int additionalSize = additionalSize();
			for(int i = 0; i < additionalSize; i++) {
				final OBAtom atom = additionalAtomMatrix[i][ATOM_A];
				final double[] pos = getAdditional(i - additionalSize);
				atom.SetVector(pos[X], pos[Y], pos[Z]);
			}
		}

		@Override
		protected OpenBabelZMatrix createConformer(OBMol molecule)
		{
			return new CartesianBasedOpenBabelZMatrix(this, molecule);
		}
	}

	/**
	 * Torsion based {@link OpenBabelZMatrix}. When the fitness value is
	 * evaluated, the torsion angles are assigned to the Open Babel molecule.
	 * Then the Open Babel molecule is assigned to a force field that calculates
	 * the energy value which is the fitness value. Note: When assigning torsion
	 * angles to an Open Babel molecule, bonded neighbors are automatically
	 * rotated. This means that the actual torsion angles of the Open Babel
	 * molecule are not the same as the torsion angles of the Z-matrix. Be
	 * careful!
	 */
	protected static class TorsionBasedOpenBabelZMatrix extends OpenBabelZMatrix
	{
		private static final long serialVersionUID = 5715566698022163616L;

		protected TorsionBasedOpenBabelZMatrix(OpenBabelData obData)
		{
			super(obData);
		}

		protected TorsionBasedOpenBabelZMatrix(
				TorsionBasedOpenBabelZMatrix zMatrix)
		{
			super(zMatrix);
		}

		protected TorsionBasedOpenBabelZMatrix(
				TorsionBasedOpenBabelZMatrix zMatrix, OBMol molecule)
		{
			super(zMatrix, molecule);
		}

		@Override
		public TorsionBasedOpenBabelZMatrix copy()
		{
			return new TorsionBasedOpenBabelZMatrix(this);
		}

		@Override
		protected final void updateMolecule(OBMol molecule,
				OBAtom[][] atomMatrix, OBAtom[][] additionalAtomMatrix)
		{
			final int size = size();
			for(int i = 0; i < size; i++) {
				final OBAtom[] atomRow = atomMatrix[i];
				final OBAtom a = atomRow[ATOM_A];
				final OBAtom b = atomRow[ATOM_B];
				final OBAtom c = atomRow[ATOM_C];
				final OBAtom d = atomRow[ATOM_D];
				final double value = getTorsionAngle(i);
				molecule.SetTorsion(a, b, c, d, value);
			}
		}

		@Override
		protected TorsionBasedOpenBabelZMatrix createConformer(
				OBMol molecule)
		{
			return new TorsionBasedOpenBabelZMatrix(this, molecule);
		}
	}
}
