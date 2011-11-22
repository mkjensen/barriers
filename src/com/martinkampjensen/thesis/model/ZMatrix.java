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

package com.martinkampjensen.thesis.model;

/**
 * A {@link ZMatrix} is represents a Z-matrix which in chemistry is used as an
 * internal coordinate representation.
 * <p>
 * TODO: Document {@link ZMatrix}.
 * 
 * @see <a href="http://goo.gl/d4kYT">Z-matrix (chemistry)</a>
 */
public interface ZMatrix extends AtomModel
{
	/**
	 * The number of atoms defined without using a torsion angle.
	 */
	int NUMBER_OF_REFERENCE_ROWS = 3;

	/**
	 * The id of the first atom defined without using a torsion angle.
	 */
	int REF1ID = 0 - NUMBER_OF_REFERENCE_ROWS;

	/**
	 * The id of the second atom defined without using a torsion angle.
	 */
	int REF2ID = 1 - NUMBER_OF_REFERENCE_ROWS;

	/**
	 * The id of the third atom defined without using a torsion angle.
	 */
	int REF3ID = 2 - NUMBER_OF_REFERENCE_ROWS;

	/**
	 * {@inheritDoc}
	 * <p>
	 * For a Z-matrix, returns the number of (reference) atoms that are defined
	 * without using torsion angles, that is, {@link #NUMBER_OF_REFERENCE_ROWS}.
	 */
	@Override
	int additionalSize();

	// Methods for manipulating and returning bond lengths and bond angles
	// should be defined here.

	/**
	 * Returns the value of a torsion angle.
	 * 
	 * @param id id of the torsion angle.
	 * @return the value of the torsion angle.
	 */
	double getTorsionAngle(int id);

	/**
	 * Sets the value of a torsion angle.
	 * 
	 * @param id id of the torsion angle.
	 * @param value the value to assign to the torsion angle.
	 */
	void setTorsionAngle(int id, double value);
}
