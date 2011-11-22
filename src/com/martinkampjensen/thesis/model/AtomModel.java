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

import jbcl.data.dict.AtomType;

/**
 * A {@link AtomModel} is an extension of the {@link CartesianModel} interface.
 * <p>
 * This interface specifies that Cartesian points have a type and that they can
 * be connected. Effectively, this results in a simple model of atoms.
 */
public interface AtomModel extends CartesianModel
{
	/**
	 * Returns the {@link AtomType} of an atom defined by a specific angle.
	 * 
	 * @param id the atom id.
	 * @return the type of the atom.
	 */
	AtomType getType(int id);

	/**
	 * Returns the {@link AtomType} of an atom that is not defined by a
	 * specific angle.
	 * 
	 * @param id the atom id.
	 * @return the type of the atom.
	 */
	AtomType getAdditionalType(int id);

	// TODO: Change the method so that implementing classes are not required to let internal data structures escape.
	/**
	 * Returns the internal array representing the atom which an atom defined by
	 * a specific angle is bonded to.
	 * <p>
	 * Note: This is so because it makes it easy to visualize a
	 * {@link CartesianModel} while it changes. However, it should be changed to
	 * work in a better way so that the internal arrays do not escape the
	 * implementing class.
	 * 
	 * @param id the atom id.
	 * @return a <code>double</code> array of length <code>3</code>.
	 */
	double[] getBond(int id);

	// TODO: Change the method so that implementing classes are not required to let internal data structures escape.
	/**
	 * Returns the internal array representing the atom which an atom that is
	 * not defined by a specific angle is bonded to.
	 * <p>
	 * Note: This is so because it makes it easy to visualize a
	 * {@link CartesianModel} while it changes. However, it should be changed to
	 * work in a better way so that the internal arrays do not escape the
	 * implementing class.
	 * 
	 * @param id the atom id.
	 * @return a <code>double</code> array of length <code>3</code>.
	 */
	double[] getAdditionalBond(int id);
}
