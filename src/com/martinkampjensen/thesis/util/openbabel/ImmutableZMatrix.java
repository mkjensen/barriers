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

package com.martinkampjensen.thesis.util.openbabel;

/**
 * An immutable Z-matrix.
 * 
 * @see <a href="http://goo.gl/kCfHN">Z-matrix (chemistry)</a>
 */
public final class ImmutableZMatrix
{
	/**
	 * Index of the bond length atom id in {@link #_matrix}<code>[i]</code>
	 * where {@value #MINIMUM_ID} <code>&le; i &le;</code> {@link #atoms()}.
	 */
	public static final int BOND_LENGTH_ATOM_ID_INDEX = 0;

	/**
	 * Index of the bond length in {@link #_matrix}<code>[i]</code>
	 * where {@value #MINIMUM_ID} <code>&le; i &le;</code> {@link #atoms()}.
	 */
	public static final int BOND_LENGTH_INDEX = 1;

	/**
	 * Index of the bond angle atom id in {@link #_matrix}<code>[i]</code>
	 * where {@value #MINIMUM_ID} <code>&le; i &le;</code> {@link #atoms()}.
	 */
	public static final int BOND_ANGLE_ATOM_ID_INDEX = 2;

	/**
	 * Index of the bond angle in {@link #_matrix}<code>[i]</code>
	 * where {@value #MINIMUM_ID} <code>&le; i &le;</code> {@link #atoms()}.
	 */
	public static final int BOND_ANGLE_INDEX = 3;

	/**
	 * Index of the torsion angle atom id in
	 * {@link #_matrix}<code>[i]</code> where {@value #MINIMUM_ID}
	 * <code>&le; i &le;</code> {@link #atoms()}.
	 */
	public static final int TORSION_ANGLE_ATOM_ID_INDEX = 4;

	/**
	 * Index of the torsion angle in {@link #_matrix}<code>[i]</code>
	 * where {@value #MINIMUM_ID} <code>&le; i &le;</code> {@link #atoms()}.
	 */
	public static final int TORSION_ANGLE_INDEX = 5;

	/**
	 * The length of each of the arrays
	 * {@link #_matrix}<code>[{@value #MINIMUM_ID}]</code> to
	 * {@link #_matrix}<code>[{@link #atoms()}]</code> that are containing atom
	 * data.
	 */
	public static final int ATOM_DATA_LENGTH = TORSION_ANGLE_INDEX + 1;

	/**
	 * The minimum value of the <code>atomId</code> parameter for all
	 * methods in this class. Hence, atoms are {@value #MINIMUM_ID}-indexed.
	 */
	public static final int MINIMUM_ID = 1;

	/**
	 * The minimum value of the <code>atomId</code> parameter for all
	 * methods related to bond lengths.
	 */
	public static final int MINIMUM_LENGTH_ID = MINIMUM_ID + 1;

	/**
	 * The minimum value of the <code>atomId</code> parameter for all
	 * methods related to bond angles.
	 */
	public static final int MINIMUM_ANGLE_ID = MINIMUM_LENGTH_ID + 1;

	/**
	 * The minimum value of the <code>atomId</code> parameter for all
	 * methods related to torsion angles.
	 */
	public static final int MINIMUM_TORSION_ID = MINIMUM_ANGLE_ID + 1;

	/**
	 * The atom data. The array is {@value #MINIMUM_ID}-indexed while the
	 * nested arrays, {@link #_matrix}<code>[i]</code> for {@link #MINIMUM_ID}
	 * <code>&le; i &le;</code> {@link #atoms()}, that
	 * contain atom data are <code>0</code>-indexed with length
	 * {@value #ATOM_DATA_LENGTH} and indexable using
	 * {@link #BOND_LENGTH_ATOM_ID_INDEX}, {@link #BOND_LENGTH_INDEX},
	 * {@link #BOND_ANGLE_ATOM_ID_INDEX}, {@link #BOND_ANGLE_INDEX},
	 * {@link #TORSION_ANGLE_ATOM_ID_INDEX}, and {@link #TORSION_ANGLE_INDEX}.
	 */
	private final double[][] _matrix;

	/**
	 * The atom types. The array is {@value #MINIMUM_ID}-indexed.
	 */
	private final String[] _types;

	/**
	 * Constructs a new immutable Z-matrix by copying the input arrays.
	 * <p>
	 * The <code>matrix</code> and <code>types</code> arrays must be
	 * {@value #MINIMUM_ID}-indexed such that
	 * <code>matrix.length == types.length == atoms</code> where
	 * <code>atoms</code> is the number of atoms defined.
	 * 
	 * @param matrix atom data.
	 * @param types atom types.
	 * @throws NullPointerException if <code>matrix == null</code> or if
	 *         <code>types == null</code> or if
	 *         <code>matrix[i] == null</code> for any <code>i</code> where
	 *         {@link #MINIMUM_ID} <code>&le; i &le;</code> {@link #atoms()}.
	 * @throws IllegalArgumentException if
	 *         <code>matrix.length != types.length</code> or if
	 *         <code>matrix[i].length !=</code> {@value #ATOM_DATA_LENGTH} for
	 *         any <code>i</code> where {@link #MINIMUM_ID}
	 *         <code>&le; i &le;</code> {@link #atoms()}.
	 */
	ImmutableZMatrix(double[][] matrix, String[] types)
	{
		check(matrix, types);

		_matrix = copy(matrix);
		_types = types.clone();
	}

	/**
	 * Returns the number of atoms in this Z-matrix. Note that atoms are
	 * {@value #MINIMUM_ID}-indexed.
	 * <p>
	 * The atoms have ids from {@value #MINIMUM_ID} to {@link #atoms()},
	 * inclusive.
	 * 
	 * @return the number of atoms.
	 */
	public int atoms()
	{
		return _matrix.length - MINIMUM_ID;
	}

	/**
	 * Returns the number of reference atoms in this Z-matrix. That is, the
	 * number of atoms defined without using a torsion angle. Note that
	 * atoms are {@value #MINIMUM_ID}-indexed.
	 * <p>
	 * The reference atoms have ids from {@value #MINIMUM_ID} to
	 * {@link #referenceAtoms()}, inclusive.
	 * 
	 * @return the number of reference atoms.
	 */
	public int referenceAtoms()
	{
		return Math.min(MINIMUM_TORSION_ID - MINIMUM_ID, atoms());
	}

	/**
	 * Returns the number of torsion atoms in this Z-matrix. That is, the
	 * number of atoms defined using a torsion angle. Note that atoms are
	 * {@value #MINIMUM_ID}-indexed.
	 * <p>
	 * The torsion atoms have ids from {@value #MINIMUM_TORSION_ID} to
	 * {@link #atoms()}, inclusive.
	 * 
	 * @return the number of torsion atoms.
	 */
	public int torsionAtoms()
	{
		return atoms() - referenceAtoms();
	}

	/**
	 * Returns the atom type of an atom.
	 * 
	 * @param atomId id of the atom.
	 * @return the atom type.
	 * @throws IndexOutOfBoundsException if
	 *         <code>atomId &lt;</code> {@value #MINIMUM_ID} or
	 *         <code>atomId &gt;</code> {@link #atoms()}.
	 */
	public String atomType(int atomId)
	{
		if(atomId < MINIMUM_ID) {
			throw new IndexOutOfBoundsException("atomId < " + MINIMUM_ID);
		}

		return _types[atomId];
	}

	/**
	 * Returns the id of the atom <code>b</code> that an atom <code>a</code>
	 * is bonded to.
	 * 
	 * @param atomId id of atom <code>a</code>.
	 * @return id of atom <code>b</code>.
	 * @throws IndexOutOfBoundsException if
	 *         <code>atomId &lt;</code> {@value #MINIMUM_LENGTH_ID} or
	 *         <code>atomId &gt;</code> {@link #atoms()}.
	 */
	public int bondLengthAtomId(int atomId)
	{
		if(atomId < MINIMUM_LENGTH_ID) {
			throw new IndexOutOfBoundsException("atomId < "
					+ MINIMUM_LENGTH_ID);
		}

		return (int)_matrix[atomId][BOND_LENGTH_ATOM_ID_INDEX];
	}

	/**
	 * Returns the length of the bond connecting atom <code>a</code> to atom
	 * <code>b</code>. Atom <code>b</code> is the bond length atom.
	 * 
	 * @param atomId id of the atom.
	 * @return the bond length.
	 * @throws IndexOutOfBoundsException if
	 *         <code>atomId &lt;</code> {@value #MINIMUM_LENGTH_ID} or
	 *         <code>atomId &gt;</code> {@link #atoms()}.
	 */
	public double bondLength(int atomId)
	{
		if(atomId < MINIMUM_LENGTH_ID) {
			throw new IndexOutOfBoundsException("atomId < "
					+ MINIMUM_LENGTH_ID);
		}

		return _matrix[atomId][BOND_LENGTH_INDEX];
	}

	/**
	 * Returns the id of the atom <code>c</code> that an atom <code>a</code>
	 * is connected to via an atom <code>b</code>. Atom <code>b</code> is
	 * the bond length atom, and atom <code>c</code> is the bond angle atom.
	 * 
	 * @param atomId id of atom <code>a</code>.
	 * @return id of atom <code>c</code>.
	 * @throws IndexOutOfBoundsException if
	 *         <code>atomId &lt;</code> {@value #MINIMUM_ANGLE_ID} or
	 *         <code>atomId &gt;</code> {@link #atoms()}.
	 */
	public int bondAngleAtomId(int atomId)
	{
		if(atomId < MINIMUM_ANGLE_ID) {
			throw new IndexOutOfBoundsException("atomId < "
					+ MINIMUM_ANGLE_ID);
		}

		return (int)_matrix[atomId][BOND_ANGLE_ATOM_ID_INDEX];
	}

	/**
	 * Returns the bond angle between atoms <code>a</code>, <code>b</code>,
	 * and <code>c</code> where <code>a</code> is connected to
	 * <code>c</code> via <code>b</code>. Atom <code>b</code> is the bond
	 * length atom, and atom <code>c</code> is the bond angle atom.
	 * 
	 * @param atomId id of atom <code>a</code>.
	 * @return the bond angle.
	 * @throws IndexOutOfBoundsException if
	 *         <code>atomId &lt;</code> {@value #MINIMUM_ANGLE_ID} or
	 *         <code>atomId &gt;</code> {@link #atoms()}.
	 */
	public double bondAngle(int atomId)
	{
		if(atomId < MINIMUM_ANGLE_ID) {
			throw new IndexOutOfBoundsException("atomId < "
					+ MINIMUM_ANGLE_ID);
		}

		return _matrix[atomId][BOND_ANGLE_INDEX];
	}

	/**
	 * Returns the id of the atom <code>d</code> that an atom <code>a</code>
	 * is connected to via atoms <code>b</code> and <code>c</code>, in that
	 * order. Atom <code>b</code> is the bond length atom, atom
	 * <code>c</code> is the bond angle atom, and atom <code>d</code> is the
	 * torsion angle atom.
	 * 
	 * @param atomId id of atom <code>a</code>.
	 * @return id of atom <code>d</code>.
	 * @throws IndexOutOfBoundsException if
	 *         <code>atomId &lt;</code> {@value #MINIMUM_TORSION_ID} or
	 *         <code>atomId &gt;</code> {@link #atoms()}.
	 */
	public int torsionAngleAtomId(int atomId)
	{
		if(atomId < MINIMUM_TORSION_ID) {
			throw new IndexOutOfBoundsException("atomId < "
					+ MINIMUM_TORSION_ID);
		}

		return (int)_matrix[atomId][TORSION_ANGLE_ATOM_ID_INDEX];
	}

	/**
	 * Returns the torsion angle between atoms <code>a</code>,
	 * <code>b</code>, <code>c</code>, and <code>d</code> where the atoms
	 * are connected to each other in that order. Atom <code>b</code> is the
	 * bond length atom, atom <code>c</code> is the bond angle atom, and
	 * atom <code>d</code> is the torsion angle atom.
	 * 
	 * @param atomId id of atom <code>a</code>.
	 * @return the torsion angle.
	 * @throws IndexOutOfBoundsException if
	 *         <code>atomId &lt;</code> {@value #MINIMUM_TORSION_ID} or
	 *         <code>atomId &gt;</code> {@link #atoms()}.
	 */
	public double torsionAngle(int atomId)
	{
		if(atomId < MINIMUM_TORSION_ID) {
			throw new IndexOutOfBoundsException("atomId < "
					+ MINIMUM_TORSION_ID);
		}

		return _matrix[atomId][TORSION_ANGLE_INDEX];
	}

	/**
	 * Checks the conditions set by
	 * {@link #ImmutableZMatrix(double[][], String[])} and throws an exception
	 * if all conditions are not met.
	 */
	private static void check(double[][] matrix, String[] types)
	{
		if(matrix == null) {
			throw new NullPointerException("matrix == null");
		}
		else if(types == null) {
			throw new NullPointerException("types == null");
		}
		else if(matrix.length != types.length) {
			throw new IllegalArgumentException(
			"matrix.length != types.length");
		}

		for(int i = MINIMUM_ID; i < matrix.length; i++) {
			if(matrix[i] == null) {
				throw new NullPointerException(
						String.format("matrix[%d] == null", i));
			}
			else if(matrix[i].length != ATOM_DATA_LENGTH) {
				throw new IllegalArgumentException(
						String.format("matrix[%d].length != %d", i,
								ATOM_DATA_LENGTH));
			}
		}
	}

	/**
	 * Creates a deep copy of a matrix. Its first row is ignored.
	 *  
	 * @param matrix the matrix to copy.
	 * @return a copy where the first row is set to <code>null</code>.
	 */
	private static double[][] copy(double[][] matrix)
	{
		final int size = matrix.length;
		final double[][] copy = new double[size][];

		for(int i = MINIMUM_ID; i < size; i++) {
			copy[i] = matrix[i].clone();
		}

		return copy;
	}
}
