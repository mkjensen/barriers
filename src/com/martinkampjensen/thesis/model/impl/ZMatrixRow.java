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

import jbcl.data.dict.AtomType;

/**
 * Instances of this class represent rows in a Z-matrix.
 * 
 * @see <a href="http://goo.gl/d4kYT">Z-matrix (chemistry)</a>
 */
final class ZMatrixRow implements Serializable
{
	private static final long serialVersionUID = 5822024771624505933L;

	// Atom ids.
	private final int _atomId;
	private final int _bondLengthAtomId;
	private final int _bondAngleAtomId;
	private final int _torsionAngleAtomId;

	// Cached look-ups using the atom ids.
	private final double[] _atom;
	private final double[] _bondLengthAtom;
	private final double[] _bondAngleAtom;
	private final double[] _torsionAngleAtom;

	// Values and properties.
	private final AtomType _atomType;
	private double _bondLength;
	private double _bondAngle;
	private double _torsionAngle;
	private Backbone _backbone;

	ZMatrixRow(double[][] positions, int atomId, AtomType atomType)
	{
		this(atomId, -1, -1, -1,
				positions[atomId], null, null, null,
				atomType, 0d, 0d, 0d);
	}

	ZMatrixRow(double[][] positions, int atomId, int bondLengthAtomId,
			AtomType atomType, double bondLength)
			{
		this(atomId, bondLengthAtomId, -1, -1,
				positions[atomId], positions[bondLengthAtomId], null,
				null,
				atomType, bondLength, 0d, 0d);
			}

	ZMatrixRow(double[][] positions, int atomId, int bondLengthAtomId,
			int bondAngleAtomId, AtomType atomType, double bondLength,
			double bondAngle)
			{
		this(atomId, bondLengthAtomId, bondAngleAtomId, -1,
				positions[atomId], positions[bondLengthAtomId],
				positions[bondAngleAtomId], null,
				atomType, bondLength, bondAngle, 0d);
			}

	ZMatrixRow(double[][] positions, int atomId, int bondLengthAtomId,
			int bondAngleAtomId, int torsionAngleAtomId, AtomType atomType,
			double bondLength, double bondAngle, double torsionAngle)
			{			
		this(atomId, bondLengthAtomId, bondAngleAtomId, torsionAngleAtomId,
				positions[atomId], positions[bondLengthAtomId],
				positions[bondAngleAtomId], positions[torsionAngleAtomId],
				atomType, bondLength, bondAngle, torsionAngle);
			}

	ZMatrixRow(int atomId, int bondLengthAtomId, int bondAngleAtomId,
			int torsionAngleAtomId, double[] atom, double[] bondLengthAtom,
			double[] bondAngleAtom, double[] torsionAngleAtom,
			AtomType atomType, double bondLength, double bondAngle,
			double torsionAngle)
			{
		_atomId = atomId;
		_bondLengthAtomId = bondLengthAtomId;
		_bondAngleAtomId = bondAngleAtomId;
		_torsionAngleAtomId = torsionAngleAtomId;

		_atom = atom;
		_bondLengthAtom = bondLengthAtom;
		_bondAngleAtom = bondAngleAtom;
		_torsionAngleAtom = torsionAngleAtom;

		_atomType = atomType;
		_bondLength = bondLength;
		_bondAngle = bondAngle;
		_torsionAngle = torsionAngle;

		_backbone = null;
			}

	int atomId()
	{
		return _atomId;
	}

	int bondLengthAtomId()
	{
		return _bondLengthAtomId;
	}

	int bondAngleAtomId()
	{
		return _bondAngleAtomId;
	}

	int torsionAngleAtomId()
	{
		return _torsionAngleAtomId;
	}

	double[] atom()
	{
		return _atom;
	}

	double[] bondLengthAtom()
	{
		return _bondLengthAtom;
	}

	double[] bondAngleAtom()
	{
		return _bondAngleAtom;
	}

	double[] torsionAngleAtom()
	{
		return _torsionAngleAtom;
	}

	/**
	 * Returns the type of the atom defined by this row.
	 * 
	 * @return the atom type.
	 */
	AtomType atomType()
	{
		return _atomType;
	}

	/**
	 * Returns the bond length of the atom defined by this row.
	 * 
	 * @return the bond length in angstroms.
	 */
	double bondLength()
	{
		return _bondLength;
	}

	/**
	 * Sets the bond length of the atom defined by this row.
	 * 
	 * @param bondLength the bond length in angstroms.
	 */
	void setBondLength(double bondLength)
	{
		_bondLength = bondLength;
	}

	/**
	 * Returns the bond angle of the atom defined by this row.
	 * 
	 * @return the bond angle in radians.
	 */
	double bondAngle()
	{
		return _bondAngle;
	}

	/**
	 * Sets the bond angle of the atom defined by this row.
	 * 
	 * @param bondAngle the bond angle in radians.
	 */
	void setBondAngle(double bondAngle)
	{
		_bondAngle = bondAngle;
	}

	/**
	 * Returns the torsion angle of the atom defined by this row.
	 * 
	 * @return the torsion angle in radians.
	 */
	double torsionAngle()
	{
		return _torsionAngle;
	}

	/**
	 * Sets the torsion angle of the atom defined by this row.
	 * 
	 * @param torsionAngle the torsion angle in radians.
	 */
	void setTorsionAngle(double torsionAngle)
	{
		_torsionAngle = torsionAngle;
	}

	/**
	 * Returns whether or not the torsion angle of the atom defined by this row
	 * is a backbone torsion angle.
	 * 
	 * @return <code>true</code> if and only if the torsion angle of the atom
	 *         defined by this row is a backbone torsion angle.
	 */
	boolean isBackbone()
	{
		return _backbone != null;
	}

	/**
	 * Returns the backbone type of the atom defined by this row, if any. This
	 * method returns <code>null</code> if and only if {@link #isBackbone()}
	 * returns <code>false</code>.
	 * 
	 * @return the backbone type or <code>null</code>.
	 */
	Backbone backbone()
	{
		return _backbone;
	}

	/**
	 * Sets the type of backbone torsion angle for the atom defined by this row.
	 * 
	 * @param backbone the backbone torsion angle type.
	 */
	void setBackbone(Backbone backbone)
	{
		_backbone = backbone;
	}

	/**
	 * An enum representing the three different types of backbone torsion
	 * angles.
	 */
	static enum Backbone
	{
		OMEGA("O"),
		PHI("F"),
		PSI("S");

		private final String _identifier;

		private Backbone(String identifier)
		{
			_identifier = identifier;
		}

		@Override
		public String toString()
		{
			return _identifier;
		}
	}
}
