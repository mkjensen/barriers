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

import org.openbabel.OBAtom;
import org.openbabel.OBFFConstraints;
import org.openbabel.OBForceField;

import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.util.openbabel.OBMol;
import com.martinkampjensen.thesis.util.openbabel.OpenBabelData;

/**
 * An implementation of the {@link Model} interface. This implementation is
 * using the structures of Open Babel as backing.
 * <p>
 * Note: This was mostly a simple proof-of-concept and there are some serious
 * issues. When setting a torsion angle in a Open Babel molecule, Open Babel
 * automatically rotates bonded neighbors. This means that, for bonded
 * neighbors, no matter which torsion angle is changed, the difference between
 * the torsion angles are unchanged. Also this means that connection algorithms
 * do not work because when changing one torsion angle, another might change
 * with the same amount, thereby creating an infinite loop. Instead, take a look
 * at the {@link OpenBabelZMatrix} class.
 * 
 * @see <a href="http://openbabel.org">Open Babel</a>
 */
public final class OpenBabelModel extends AbstractComparableModel
{
	private static final int ATOM_A = 0;
	private static final int ATOM_B = 1;
	private static final int ATOM_C = 2;
	private static final int ATOM_D = 3;
	private final OBMol _molecule;
	private final OBForceField _forceField;
	private final int _size;
	private final OBAtom[][] _atomMatrix;
	private final int[][] _torsionMatrix;
	private boolean _hasChanged;
	private double _fitness;

	/**
	 * Constructs a new model backed by Open Babel structures.
	 * 
	 * @param obData the data from Open Babel to use.
	 * @throws NullPointerException if <code>obData == null</code>.
	 */
	public OpenBabelModel(OpenBabelData obData)
	{
		super();

		if(obData == null) {
			throw new NullPointerException("obData == null");
		}

		_molecule = copyMolecule(obData.getMolecule());
		_forceField = copyForceField(obData.getForceField());
		_size = (int)_molecule.NumAtoms() - 3;
		_atomMatrix = createAtomMatrix(_size);
		_torsionMatrix = obData.getTorsions();
		_hasChanged = true;

		updateAtomMatrix(_atomMatrix, _size, _torsionMatrix, _molecule);
	}

	/**
	 * Constructs a copy of a model that is backed by Open Babel.
	 * 
	 * @param model the model to copy.
	 * @throws NullPointerException if <code>model == null</code>.
	 */
	public OpenBabelModel(OpenBabelModel model)
	{
		super();

		if(model == null) {
			throw new NullPointerException("model == null");
		}

		_molecule = copyMolecule(model._molecule);
		_forceField = copyForceField(model._forceField);
		_size = model._size;
		_atomMatrix = createAtomMatrix(_size);
		_torsionMatrix = model._torsionMatrix;
		_hasChanged = model._hasChanged;
		_fitness = model._fitness;

		updateAtomMatrix(_atomMatrix, _size, _torsionMatrix, _molecule);
	}

	@Override
	public int size()
	{
		return _size;
	}

	@Override
	public boolean hasChanged()
	{
		return _hasChanged;
	}

	@Override
	public double evaluate()
	{
		if(_hasChanged) {
			_forceField.Setup(_molecule);
			_fitness = _forceField.Energy();
			_hasChanged = false;
		}

		return _fitness;
	}

	@Override
	public OpenBabelModel copy()
	{
		return new OpenBabelModel(this);
	}

	@Override
	public double getAngle(int id)
	{
		final OBAtom[] atomRow = _atomMatrix[id];
		final OBAtom a = atomRow[ATOM_A];
		final OBAtom b = atomRow[ATOM_B];
		final OBAtom c = atomRow[ATOM_C];
		final OBAtom d = atomRow[ATOM_D];

		return Math.toRadians(_molecule.GetTorsion(a, b, c, d));
	}

	@Override
	public void setAngle(int id, double value)
	{
		final OBAtom[] atomRow = _atomMatrix[id];
		final OBAtom a = atomRow[ATOM_A];
		final OBAtom b = atomRow[ATOM_B];
		final OBAtom c = atomRow[ATOM_C];
		final OBAtom d = atomRow[ATOM_D];

		_molecule.SetTorsion(a, b, c, d, value);
		_hasChanged = true;
	}

	private static OBMol copyMolecule(OBMol molecule)
	{
		return new OBMol(molecule);
	}

	private static OBForceField copyForceField(OBForceField forceField)
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

	private static OBAtom[][] createAtomMatrix(int size)
	{
		return new OBAtom[size][OpenBabelData.TORSION_LENGTH];
	}

	/**
	 * Updates a matrix containing references to the atoms in a molecule. Must
	 * be called when the molecule is updated, for example when extracting a
	 * conformer from the force field to the molecule.
	 */
	private static void updateAtomMatrix(OBAtom[][] atomMatrix, int size,
			int[][] torsionMatrix, OBMol molecule)
	{
		final int offset = OpenBabelData.TORSION_LENGTH;

		for(int i = 0; i < size; i++) {
			final int[] torsionRow = torsionMatrix[i + offset];
			final int atomA = torsionRow[OpenBabelData.TORSION_A];
			final int atomB = torsionRow[OpenBabelData.TORSION_B];
			final int atomC = torsionRow[OpenBabelData.TORSION_C];
			final int atomD = torsionRow[OpenBabelData.TORSION_D];

			final OBAtom[] atomRow = atomMatrix[i];
			atomRow[ATOM_A] = molecule.GetAtom(atomA);
			atomRow[ATOM_B] = molecule.GetAtom(atomB);
			atomRow[ATOM_C] = molecule.GetAtom(atomC);
			atomRow[ATOM_D] = molecule.GetAtom(atomD);
		}
	}
}
