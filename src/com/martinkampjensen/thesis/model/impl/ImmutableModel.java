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

import jbcl.calc.structural.properties.TorsionalAngle;

import org.openbabel.OBAtom;

import com.martinkampjensen.thesis.barriers.TrajectoryConstructor;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.util.Util;
import com.martinkampjensen.thesis.util.openbabel.OBMol;
import com.martinkampjensen.thesis.util.openbabel.OpenBabel;
import com.martinkampjensen.thesis.util.openbabel.OpenBabelData;

/**
 * An implementation of the {@link Model} interface that supports assigning an
 * id as well as fitness values and torsion angle values at construction time.
 * Instances are immutable.
 */
public final class ImmutableModel extends AbstractComparableModel
implements Serializable
{
	private static final long serialVersionUID = -4765977934526465721L;
	private static final int ATOMS_WITHOUT_TORSION_ANGLE = 3;
	private final int _id;
	private final double _fitness;
	private final double[] _angles;
	private double[][] _coordinates;

	/**
	 * Constructs a new immutable model by copying the torsion angle values (as
	 * in a Z-matrix) from an Open Babel molecule.
	 * 
	 * @param id the id to assign.
	 * @param fitness the fitness value to assign.
	 * @param molecule the Open Babel molecule.
	 * @param torsionMatrix a matrix containing the ids of the atoms defining
	 *        the torsion angles.
	 * @see <a href="http://goo.gl/d4kYT">Z-matrix (chemistry)</a>
	 * @see <a href="http://openbabel.org">Open Babel</a>
	 */
	public ImmutableModel(int id, double fitness, OBMol molecule,
			int[][] torsionMatrix)
	{
		super();

		final int size = (int)molecule.NumAtoms() - ATOMS_WITHOUT_TORSION_ANGLE;

		_id = id;
		_fitness = fitness;
		_angles = createAngles(size, molecule, torsionMatrix);
	}

	/**
	 * Constructs a new immutable model by calculating the torsion angle values
	 * (as in a Z-matrix) from coordinates of the atoms using BioShell.
	 * 
	 * @param id the id to assign.
	 * @param fitness the fitness value to assign.
	 * @param coordinates a matrix containing the coordinates of the atoms.
	 * @param torsionMatrix a matrix containing the ids of the atoms defining
	 *        the torsion angles.
	 * @see <a href="http://goo.gl/d4kYT">Z-matrix (chemistry)</a>
	 * @see <a href="http://bioshell.chem.uw.edu.pl/">BioShell</a>
	 */
	public ImmutableModel(int id, double fitness, double[][] coordinates,
			int[][] torsionMatrix)
	{
		super();

		final int size = coordinates.length - ATOMS_WITHOUT_TORSION_ANGLE;

		_id = id;
		_fitness = fitness;
		_angles = createAngles(size, coordinates, torsionMatrix);
	}

	@Override
	public int size()
	{
		return _angles.length;
	}

	/**
	 * This implementation always returns <code>false</code> because this object
	 * is immutable.
	 * 
	 * @return <code>false</code>.
	 */
	@Override
	public boolean hasChanged()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation never performs any calculations because this object
	 * is immutable.
	 */
	@Override
	public double evaluate()
	{
		return _fitness;
	}

	/**
	 * This implementation always returns this object because this object is
	 * immutable.
	 * 
	 * @return this object.
	 */
	@Override
	public ImmutableModel copy()
	{
		return this;
	}

	@Override
	public double getAngle(int id)
	{
		return _angles[id];
	}

	/**
	 * This implementation always throws an
	 * {@link UnsupportedOperationException} because this object is immutable.
	 * 
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public void setAngle(int id, double value)
	{
		throw new UnsupportedOperationException("This object is immutable");
	}

	/**
	 * Returns the id assigned to this model when it was constructed.
	 */
	@Override
	public int getId()
	{
		return _id;
	}

	/**
	 * This implementation always throws an
	 * {@link UnsupportedOperationException} because this object is immutable.
	 * 
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public void setId(int id)
	{
		throw new UnsupportedOperationException("This object is immutable");
	}

	/**
	 * Returns the attached coordinates.
	 * 
	 * @return the coordinates, or <code>null</code> if none has been attached.
	 */
	public double[][] getCoordinates()
	{
		return _coordinates;
	}

	/**
	 * Copies an array of coordinates and attaches the copy to this model. The
	 * coordinates can be used to create a PDB representation of this model
	 * using {@link #toPdb(OBMol)}.
	 * <p>
	 * Note: This is a convenience method that is provided even though this
	 * instance is immutable. Many instances (that will eventually be unused) of
	 * this class can potentially exist at the same time because of
	 * {@link TrajectoryConstructor} and, hence, it is not feasible to always
	 * attach coordinates.
	 * 
	 * @param coordinates the coordinates to copy.
	 */
	public void attachCoordinates(double[][] coordinates)
	{
		_coordinates = Util.copy(coordinates);
	}

	/**
	 * Returns a PDB representation of this model.
	 * 
	 * @param molecule the exact molecule of which this model is a conformation.
	 * @return the PDB representation.
	 */
	public String toPdb(OBMol molecule)
	{
		final double[][] coordinates = _coordinates;
		final int nAtoms = coordinates.length;

		for(int i = 0; i < nAtoms; i++) {
			final OBAtom atom = molecule.GetAtom(i + 1);
			final double[] xyz = coordinates[i];
			atom.SetVector(xyz[0], xyz[1], xyz[2]);
		}

		return OpenBabel.toPdb(molecule);
	}

	private static double[] createAngles(int size, OBMol molecule,
			int[][] torsionMatrix)
	{
		final double[] angles = new double[size];
		final int torsionOffset = OpenBabelData.TORSION_LENGTH;

		for(int i = 0; i < size; i++) {
			final int[] torsionRow = torsionMatrix[i + torsionOffset];

			final int a = torsionRow[OpenBabelData.TORSION_A];
			final int b = torsionRow[OpenBabelData.TORSION_B];
			final int c = torsionRow[OpenBabelData.TORSION_C];
			final int d = torsionRow[OpenBabelData.TORSION_D];

			final double angleInDegrees = molecule.GetTorsion(a, b, c, d);
			final double angleInRadians = Math.toRadians(angleInDegrees);

			angles[i] = Util.ensureAngleInterval(angleInRadians);
		}

		return angles;
	}

	private static double[] createAngles(int size, double[][] coordinates,
			int[][] torsionMatrix)
	{
		final double[] angles = new double[size];
		final int torsionOffset = OpenBabelData.TORSION_LENGTH;
		final int atomOffset = -1;

		for(int i = 0; i < size; i++) {
			final int[] torsionRow = torsionMatrix[i + torsionOffset];

			final int a = torsionRow[OpenBabelData.TORSION_A] + atomOffset;
			final int b = torsionRow[OpenBabelData.TORSION_B] + atomOffset;
			final int c = torsionRow[OpenBabelData.TORSION_C] + atomOffset;
			final int d = torsionRow[OpenBabelData.TORSION_D] + atomOffset;

			final double[] atom1 = coordinates[a];
			final double[] atom2 = coordinates[b];
			final double[] atom3 = coordinates[c];
			final double[] atom4 = coordinates[d];

			angles[i] = Util.ensureAngleInterval(
					TorsionalAngle.calculateValue(atom1, atom2, atom3, atom4));
		}

		return angles;
	}
}
