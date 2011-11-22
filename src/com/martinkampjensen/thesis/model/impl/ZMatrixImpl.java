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

import jbcl.calc.structural.transformations.ZMatrixToCartesian;
import jbcl.data.dict.AtomType;

import com.martinkampjensen.thesis.Constant;
import com.martinkampjensen.thesis.evaluation.Evaluator;
import com.martinkampjensen.thesis.evaluation.LennardJonesEvaluator;
import com.martinkampjensen.thesis.model.ZMatrix;
import com.martinkampjensen.thesis.model.impl.ZMatrixRow.Backbone;
import com.martinkampjensen.thesis.util.Debug;
import com.martinkampjensen.thesis.util.Util;
import com.martinkampjensen.thesis.util.openbabel.ImmutableZMatrix;
import com.martinkampjensen.thesis.util.openbabel.OpenBabelData;

/**
 * An implementation of the {@link ZMatrix} interface. This class can be used to
 * manually construct a Z-matrix from scratch using methods suchs as
 * {@link #addRow(AtomType, int, int, int, double, double, double)} and by
 * providing a suitable evaluator for calculating the fitness value. Further,
 * this class can function as a superclass for classes that want to construct a
 * Z-matrix based on data from Open Babel and create their own evaluation logic.
 */
public class ZMatrixImpl extends AbstractZMatrix implements Serializable
{
	protected static final int X = 0;
	protected static final int Y = 1;
	protected static final int Z = 2;
	private static final long serialVersionUID = -9123206266708558433L;
	private static final int DIMENSIONS = 3;
	private static final int FIRST_REFERENCE_ROW_ID =
		REF1ID + NUMBER_OF_REFERENCE_ROWS;
	private static final int SECOND_REFERENCE_ROW_ID =
		REF2ID + NUMBER_OF_REFERENCE_ROWS;
	private static final int THIRD_REFERENCE_ROW_ID =
		REF3ID + NUMBER_OF_REFERENCE_ROWS;
	protected final transient OpenBabelData _obData;
	private final double[][] _positions;
	private final ZMatrixRow[] _refRows;
	private final ZMatrixRow[] _rows;
	private final Evaluator _evaluator;
	private final int[] _omegaIds;
	private final int[] _phiIds;
	private final int[] _psiIds;
	private int _nextAtomId;
	private double _fitness;
	private boolean _refHasChanged;
	private boolean _hasChanged;

	/**
	 * Same as {@link #ZMatrixImpl(int)} with <code>size</code> set to
	 * <code>20</code>.
	 */
	public ZMatrixImpl()
	{
		this(20);
	}

	/**
	 * Same as {@link #ZMatrixImpl(int, Evaluator)} with a new
	 * {@link LennardJonesEvaluator} instance as <code>evaluator</code>.
	 * 
	 * @param size the maximum number of rows.
	 * @throws IllegalArgumentException if <code>size &lt; 1</code>.
	 */
	public ZMatrixImpl(int size)
	{
		this(size, new LennardJonesEvaluator());
	}

	/**
	 * Constructs a new Z-matrix with a fixed maximum number of rows (excluding
	 * the three reference rows that do not define torsion angles) and a
	 * specific evaluator.
	 * 
	 * @param size the maximum number of rows.
	 * @throws IllegalArgumentException if <code>size &lt; 1</code>.
	 * @throws NullPointerException if <code>evaluator == null</code>.
	 */
	public ZMatrixImpl(int size, Evaluator evaluator)
	{
		super();

		if(size < 1) {
			throw new IllegalArgumentException("size < 1");
		}
		else if(evaluator == null) {
			throw new NullPointerException("evaluator == null");
		}

		_obData = null;
		_positions = new double[NUMBER_OF_REFERENCE_ROWS + size][DIMENSIONS];
		_refRows = new ZMatrixRow[NUMBER_OF_REFERENCE_ROWS];
		_rows = new ZMatrixRow[size];
		_evaluator = evaluator;
		_omegaIds = null;
		_phiIds = null;
		_psiIds = null;

		setFirstReference(AtomType.Du);
	}

	// TODO: Consider copying fitness/hasChanged information.
	// TODO: Freeze an instance when it is copied? At the moment, the evaluator and the backbone id arrays are shared.
	// TODO: Too much manual shit, error-prone.
	/**
	 * Constructs a copy of a Z-matrix.
	 * <p>
	 * Note that the {@link Evaluator} as well as some support structures are
	 * shared.
	 * 
	 * @throws NullPointerException if <code>zMatrixImpl == null</code>.
	 */
	public ZMatrixImpl(ZMatrixImpl zMatrixImpl)
	{
		super();

		if(zMatrixImpl == null) {
			throw new NullPointerException("zMatrixImpl == null");
		}

		final int size = zMatrixImpl.size();

		_obData = zMatrixImpl._obData;
		_positions =
			new double[NUMBER_OF_REFERENCE_ROWS + size][DIMENSIONS];
		_refRows = new ZMatrixRow[NUMBER_OF_REFERENCE_ROWS];
		_rows = new ZMatrixRow[size];
		_evaluator = zMatrixImpl._evaluator;
		_omegaIds = zMatrixImpl._omegaIds;
		_phiIds = zMatrixImpl._phiIds;
		_psiIds = zMatrixImpl._psiIds;

		ZMatrixRow r = zMatrixImpl._refRows[FIRST_REFERENCE_ROW_ID];
		setFirstReference(r.atomType());

		r = zMatrixImpl._refRows[SECOND_REFERENCE_ROW_ID];
		setSecondReference(r.atomType(), r.bondLength());

		r = zMatrixImpl._refRows[THIRD_REFERENCE_ROW_ID];
		setThirdReference(r.atomType(),
				r.bondLengthAtomId() - NUMBER_OF_REFERENCE_ROWS,
				r.bondAngleAtomId() - NUMBER_OF_REFERENCE_ROWS, r.bondLength(),
				r.bondAngle());

		for(int i = 0; i < size; i++) {
			r = zMatrixImpl._rows[i];
			addRow(r.atomType(),
					r.bondLengthAtomId() - NUMBER_OF_REFERENCE_ROWS,
					r.bondAngleAtomId() - NUMBER_OF_REFERENCE_ROWS,
					r.torsionAngleAtomId()  - NUMBER_OF_REFERENCE_ROWS,
					r.bondLength(), r.bondAngle(), r.torsionAngle());
			_rows[i].setBackbone(r.backbone());
		}
	}

	/**
	 * Constructs a new Z-matrix based on data from Open Babel.
	 * 
	 * @param obData the data from Open Babel to use.
	 * @throws NullPointerException if <code>obData == null</code>.
	 */
	protected ZMatrixImpl(OpenBabelData obData)
	{
		super();

		if(obData == null) {
			throw new NullPointerException("obData == null");
		}

		Debug.line("Constructing internal model");
		final ImmutableZMatrix z = obData.getZMatrix();
		final int offset = -(z.referenceAtoms() + ImmutableZMatrix.MINIMUM_ID);
		final int size = z.torsionAtoms();

		_obData = obData;
		_positions =
			new double[NUMBER_OF_REFERENCE_ROWS + size][DIMENSIONS];
		_refRows = new ZMatrixRow[NUMBER_OF_REFERENCE_ROWS];
		_rows = new ZMatrixRow[size];
		_evaluator = null;

		// First (reference) atom.
		final AtomType fstAtomType = findAtomType(z.atomType(1));
		setFirstReference(fstAtomType);

		// Second (reference) atom.
		final AtomType sndAtomType = findAtomType(z.atomType(2));
		final double sndBondLength = z.bondLength(2);
		setSecondReference(sndAtomType, sndBondLength);

		// Third (reference) atom.
		final AtomType trdAtomType = findAtomType(z.atomType(3));
		final int trdBondLengthAtomId = z.bondLengthAtomId(3) + offset;
		final double trdBondLength = z.bondLength(3);
		final int trdBondAngleAtomId = z.bondAngleAtomId(3) + offset;
		final double trdBondAngle = z.bondAngle(3);
		setThirdReference(trdAtomType, trdBondLengthAtomId, trdBondAngleAtomId,
				trdBondLength, trdBondAngle);

		// Atoms defined using torsion angles.
		final int atoms = z.atoms();
		for(int i = 4; i <= atoms; i++) {
			final AtomType atomType = findAtomType(z.atomType(i));
			final int bondLengthAtomId = z.bondLengthAtomId(i) + offset;
			final double bondLength = z.bondLength(i);
			final int bondAngleAtomId = z.bondAngleAtomId(i) + offset;
			final double bondAngle = z.bondAngle(i);
			final int torsionAngleAtomId = z.torsionAngleAtomId(i) + offset;
			final double torsionAngle = z.torsionAngle(i);
			addRow(atomType, bondLengthAtomId, bondAngleAtomId,
					torsionAngleAtomId, bondLength, bondAngle,
					torsionAngle);
		}

		final int[] backbones = obData.getBackbones();

		if(backbones != null) {
			final int nBackboneTriplets = backbones.length / 3;

			_omegaIds = new int[nBackboneTriplets];
			_phiIds = new int[nBackboneTriplets];
			_psiIds = new int[nBackboneTriplets];

			for(int i = 0; i < nBackboneTriplets; i++) {
				final int omegaId = 3 * i;
				final int phiId = omegaId + 1;
				final int psiId = phiId + 1;

				final int omegaAngleId = backbones[omegaId] + offset;
				final int phiAngleId = backbones[phiId] + offset;
				final int psiAngleId = backbones[psiId] + offset;

				_omegaIds[i] = omegaAngleId;
				_phiIds[i] = phiAngleId;
				_psiIds[i] = psiAngleId;

				_rows[omegaAngleId].setBackbone(Backbone.OMEGA);
				_rows[phiAngleId].setBackbone(Backbone.PHI);
				_rows[psiAngleId].setBackbone(Backbone.PSI);
			}
		}
		else {
			_omegaIds = null;
			_phiIds = null;
			_psiIds = null;
		}
	}

	/**
	 * Same as {@link #toString(boolean)} with <code>markDependencies</code> set
	 * to <code>true</code>.
	 * 
	 * @see #toString(boolean)
	 */
	@Override
	public final String toString()
	{
		return toString(true);
	}

	/**
	 * Returns the number of atoms defined, excluding the (reference) atoms that
	 * are not defined using torsion angles.
	 * 
	 * @return the number of atoms defined using torsion angles.
	 */
	@Override
	public final int size()
	{
		return _nextAtomId;
	}

	@Override
	public final boolean hasChanged()
	{
		return _hasChanged;
	}

	@Override
	public final double evaluate()
	{
		if(_hasChanged || _refHasChanged) {
			calculatePositions();
			_fitness = calculateFitness();
			_hasChanged = false;
		}

		return _fitness;
	}

	@Override
	public ZMatrixImpl copy()
	{
		return new ZMatrixImpl(this);
	}

	@Override
	public void setFitness(double fitness)
	{
		_fitness = fitness;
		_refHasChanged = false;
		_hasChanged = false;
	}

	@Override
	public final double[] get(int id)
	{
		return _rows[id].atom();
	}

	@Override
	public final void get(int id, double[] xyz)
	{
		xyz[X] = _rows[id].atom()[X];
		xyz[Y] = _rows[id].atom()[Y];
		xyz[Z] = _rows[id].atom()[Z];
	}

	@Override
	public final double[] getAdditional(int id)
	{
		return _refRows[id + NUMBER_OF_REFERENCE_ROWS].atom();
	}

	@Override
	public final void getAdditional(int id, double[] xyz)
	{
		xyz[X] = _refRows[id + NUMBER_OF_REFERENCE_ROWS].atom()[X];
		xyz[Y] = _refRows[id + NUMBER_OF_REFERENCE_ROWS].atom()[Y];
		xyz[Z] = _refRows[id + NUMBER_OF_REFERENCE_ROWS].atom()[Z];
	}

	@Override
	public final AtomType getType(int id)
	{
		return _rows[id].atomType();
	}

	@Override
	public final AtomType getAdditionalType(int id)
	{
		return _refRows[id + NUMBER_OF_REFERENCE_ROWS].atomType();
	}

	@Override
	public final double[] getBond(int id)
	{
		return _rows[id].bondLengthAtom();
	}

	@Override
	public final double[] getAdditionalBond(int id)
	{
		return _refRows[id + NUMBER_OF_REFERENCE_ROWS].bondLengthAtom();
	}

	@Override
	public final double getTorsionAngle(int id)
	{
		return _rows[id].torsionAngle();
	}

	@Override
	public final void setTorsionAngle(int id, double value)
	{
		// TODO: Avoid ensuring the angle interval all the time?
		_rows[id].setTorsionAngle(Util.ensureAngleInterval(value));
		_hasChanged = true;
	}

	/**
	 * Returns a textual representation of this object in a format that looks a
	 * lot like the Fenske-Hall Z-Matrix format, possibly with added backbone
	 * torsion information and dependencies. Note that angular values are
	 * returned in degrees even though they are internally represented in
	 * radians.
	 * <p>
	 * The textual representation is generated at each call to this method even
	 * if {@link #hasChanged()} returns <code>false</code> immediately before a
	 * call.
	 * 
	 * @param markDependencies whether or not to mark atoms that
	 *        are dependencies or dependent.
	 * @return a textual representation of this object.
	 * @see <a href="http://goo.gl/NcM0D">Fenske-Hall Z-Matrix</a>
	 */
	public final String toString(boolean markDependencies)
	{
		final int offset = 1;
		final int totalSize = additionalSize() + size();
		final StringBuilder sb = new StringBuilder(25 * totalSize);

		String s = String.format("%d\n", totalSize);
		sb.append(s);

		ZMatrixRow r = _refRows[FIRST_REFERENCE_ROW_ID];
		s = String.format("%s\t%d\n", r.atomType(), r.atomId() + offset);
		sb.append(s);

		r = _refRows[SECOND_REFERENCE_ROW_ID];
		s = String.format("%s\t%d\t%f\n", r.atomType(),
				r.bondLengthAtomId() + offset, r.bondLength());
		sb.append(s);

		r = _refRows[THIRD_REFERENCE_ROW_ID];
		s = String.format("%s\t%d\t%f\t%s\t%f\n", r.atomType(),
				r.bondLengthAtomId() + offset, r.bondLength(),
				r.bondAngleAtomId() + offset, Math.toDegrees(r.bondAngle()));
		sb.append(s);

		final int size = size();
		for(int i = 0; i < size; i++) {
			r = _rows[i];
			s = String.format("%s\t%d\t%f\t%d\t%f\t%d\t%f",
					r.atomType(), r.bondLengthAtomId() + offset, r.bondLength(),
					r.bondAngleAtomId() + offset, Math.toDegrees(r.bondAngle()),
					r.torsionAngleAtomId() + offset,
					Math.toDegrees(r.torsionAngle()));
			sb.append(s);

			if(markDependencies && r.isBackbone()) {
				sb.append(String.format("\t[%s%d]", r.backbone(), i));
			}

			sb.append('\n');
		}

		return sb.toString();
	}

	public final int setFirstReference(AtomType atomType)
	{
		final int assignedAtomId = FIRST_REFERENCE_ROW_ID;

		_refRows[assignedAtomId] =
			new ZMatrixRow(_positions, assignedAtomId, atomType);
		_refHasChanged = true;

		return assignedAtomId;
	}

	public final int setSecondReference(double bondLength)
	{
		return setSecondReference(AtomType.Du, bondLength);
	}

	public final int setSecondReference(AtomType atomType, double bondLength)
	{
		final int assignedAtomId = SECOND_REFERENCE_ROW_ID; 

		_refRows[assignedAtomId] = new ZMatrixRow(
				_positions,
				assignedAtomId,
				FIRST_REFERENCE_ROW_ID,
				atomType, bondLength);
		_refHasChanged = true;

		return assignedAtomId;
	}

	public final int setThirdReference(int bondLengthAtomId,
			int bondAngleAtomId, double bondLength, double bondAngle)
	{
		return setThirdReference(AtomType.Du, bondLengthAtomId, bondAngleAtomId,
				bondLength, bondAngle);
	}

	public final int setThirdReference(AtomType atomType, int bondLengthAtomId,
			int bondAngleAtomId, double bondLength, double bondAngle)
	{
		final int assignedAtomId = THIRD_REFERENCE_ROW_ID;

		_refRows[assignedAtomId] = new ZMatrixRow(
				_positions,
				assignedAtomId,
				bondLengthAtomId + NUMBER_OF_REFERENCE_ROWS,
				bondAngleAtomId + NUMBER_OF_REFERENCE_ROWS,
				atomType, bondLength, Util.ensureAngleInterval(bondAngle));
		_refHasChanged = true;

		return assignedAtomId;
	}

	public final int addRow(int bondLengthAtomId, int bondAngleAtomId,
			int torsionAngleAtomId, double bondLength, double bondAngle,
			double torsionAngle)
	{
		return addRow(AtomType.Du, bondLengthAtomId, bondAngleAtomId,
				torsionAngleAtomId, bondLength, bondAngle, torsionAngle);
	}

	public final int addRow(AtomType atomType, int bondLengthAtomId,
			int bondAngleAtomId, int torsionAngleAtomId, double bondLength,
			double bondAngle, double torsionAngle)
	{
		final int assignedAtomId = _nextAtomId++;

		_rows[assignedAtomId] = new ZMatrixRow(
				_positions,
				assignedAtomId + NUMBER_OF_REFERENCE_ROWS,
				bondLengthAtomId + NUMBER_OF_REFERENCE_ROWS,
				bondAngleAtomId + NUMBER_OF_REFERENCE_ROWS,
				torsionAngleAtomId + NUMBER_OF_REFERENCE_ROWS,
				atomType, bondLength,
				Util.ensureAngleInterval(bondAngle),
				Util.ensureAngleInterval(torsionAngle));
		_hasChanged = true;

		return assignedAtomId;
	}

	/**
	 * Calculates the Cartesian coordinates of the atoms defined by this
	 * ZMatrix.
	 * <p>
	 * The reference atoms are placed as follows. The first reference atom
	 * <code>a</code> is placed at the origin. The second reference atom
	 * <code>b</code> is placed on the z-axis according to the bond length
	 * between <code>a</code> and <code>b</code>. The third reference atom
	 * <code>c</code> is placed in the xz plane according to its bond length and
	 * bond angle.
	 * <p>
	 * The remaining atoms are placed according to their bond lengths, bond
	 * angles, and torsion angles.
	 */
	protected final void calculatePositions()
	{
		if(_refHasChanged) {
			calculateReferenceAtoms();
			_refHasChanged = false;
		}

		final int size = size();
		for(int i = 0; i < size; i++) {
			calculateAtom(i);
		}
	}

	/**
	 * Overrides the values of the variables specifying whether or not the
	 * reference atoms as well as the atoms defined by torsion angles have
	 * changed. Use with care.
	 * 
	 * @param hasChanged the new value for whether of not atoms have changed.
	 */
	protected final void setHasChanged(boolean hasChanged)
	{
		_refHasChanged = _hasChanged = hasChanged;
	}

	/**
	 * Calculates the fitness value of this object. Can be overridden by
	 * subclasses that want to calculate the fitness value using a specific
	 * approach.
	 * 
	 * @return the fitness value.
	 */
	protected double calculateFitness()
	{
		return _evaluator.evaluate(this);
	}

	protected final void setBondLength(int id, double value)
	{
		if(id >= 0) {
			_rows[id].setBondLength(value);
			_hasChanged = true;
		}
		else {
			_refRows[id + NUMBER_OF_REFERENCE_ROWS].setBondLength(value);
			_refHasChanged = true;
		}
	}

	protected final void setBondAngle(int id, double value)
	{
		// TODO: Avoid ensuring the angle interval all the time?
		if(id >= 0) {
			_rows[id].setBondAngle(Util.ensureAngleInterval(value));
			_hasChanged = true;
		}
		else {
			_refRows[id + NUMBER_OF_REFERENCE_ROWS].setBondAngle(
					Util.ensureAngleInterval(value));
			_refHasChanged = true;
		}
	}

	private static final AtomType findAtomType(String atomTypeString)
	{
		final AtomType atomType = AtomType.get(atomTypeString);
		return (atomType != null ? atomType : AtomType.Du);
	}

	private final void calculateReferenceAtoms()
	{
		calculateFirstReferenceAtom();
		calculateSecondReferenceAtom();
		calculateThirdReferenceAtom();
	}

	private final void calculateFirstReferenceAtom()
	{
		final ZMatrixRow row = _refRows[FIRST_REFERENCE_ROW_ID];
		final double[] a = row.atom();

		a[X] = a[Y] = a[Z] = 0d;
	}

	private final void calculateSecondReferenceAtom()
	{
		final ZMatrixRow row = _refRows[SECOND_REFERENCE_ROW_ID];
		final double[] a = row.atom();
		final double ab = row.bondLength();

		a[X] = a[Y] = 0d;
		a[Z] = ab;
	}

	/**
	 * @see <a href="http://goo.gl/K23eg">First bond angle cases</a>
	 */
	private final void calculateThirdReferenceAtom()
	{
		final ZMatrixRow row = _refRows[THIRD_REFERENCE_ROW_ID];
		final double[] a = row.atom();
		final double[] b = row.bondLengthAtom();
		final double ab = row.bondLength();
		double abc = row.bondAngle();		
		double angle;
		double z;

		// Avoid 1st bond angle being below 0, too close to zero, above PI, or
		// too close to PI.
		if(abc < 0) {
			throw new IllegalStateException("1st bond angle is < 0 degrees");
		}
		else if(abc < Constant.DOUBLE_PRECISION) {
			abc = Constant.DOUBLE_PRECISION;
		}
		else if(abc > Constant.PI) {
			throw new IllegalStateException("1st bond angle is > 180 degrees");
		}
		else if(abc > Constant.PI - Constant.DOUBLE_PRECISION) {
			abc = Constant.PI - Constant.DOUBLE_PRECISION;
		}

		// The atom is placed in the xz plane so the y coordinate is always 0.
		a[Y] = 0d;

		if(b == _refRows[SECOND_REFERENCE_ROW_ID].atom()) {
			if(abc > Constant.HALF_PI) {
				// Case 1, e.g. BAtomModel.
				angle = Constant.PI - abc;
				z = b[Z] + Math.cos(angle) * ab;
			}
			else if(abc < Constant.HALF_PI) {
				// Case 2.
				angle = abc;
				z = b[Z] - Math.cos(angle) * ab;
			}
			else {
				a[X] = ab;
				a[Z] = b[Z];
				return;
			}
		}
		else if(b == _refRows[FIRST_REFERENCE_ROW_ID].atom()) {
			if(abc > Constant.HALF_PI) {
				// Case 3, e.g. "AcAlaNMe_gmx.pdb".
				angle = Constant.PI - abc;
				z = -Math.cos(angle) * ab;
			}
			else if(abc < Constant.HALF_PI) {
				// Cases 4 and 5.
				angle = abc;
				z = Math.cos(angle) * ab;
			}
			else {
				a[X] = ab;
				a[Z] = 0d;
				return;
			}
		}
		else {
			throw new IllegalStateException(
			"3rd atom is not connected to either 1st atom or 2nd atom");
		}

		a[X] = Math.sin(angle) * ab;
		a[Z] = z;
	}

	private final void calculateAtom(int id)
	{
		final ZMatrixRow row = _rows[id];
		final double[] a1 = row.torsionAngleAtom();
		final double[] a2 = row.bondAngleAtom();
		final double[] a3 = row.bondLengthAtom();
		final double[] a4 = row.atom();
		final double a3a4 = row.bondLength();
		final double a2a3a4 = row.bondAngle();
		final double a1a2a3a4 = row.torsionAngle();

		ZMatrixToCartesian.transform(a1, a2, a3, a3a4, a2a3a4, a1a2a3a4, a4);
	}

	// TODO: Enable generation of some Cartesian coordinates format that is easy to work with in GROMACS (probably .pdb)
}
