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
 * A {@link CartesianModel} is an extension of the {@link Model} interface.
 * <p>
 * This interface specifies that each angle has a resulting point with
 * three-dimensional Cartesian coordinates. Additional points not resulting from
 * any specific angle may also exist.
 * <p>
 * The points in a {@link CartesianModel} must be assigned ids from
 * <code>-{@link #additionalSize()}</code> to <code>-1</code>.
 * 
 * @see <a href="http://goo.gl/64ez">Cartesian coordinate system</a>
 * @see <a href="http://goo.gl/6yzw9">Three-dimensional space</a>
 */
public interface CartesianModel extends Model
{
	/**
	 * Returns the number of additional points in three-dimensional Cartesian
	 * space that are not defined using specific angles.
	 * 
	 * @return the number of additional points.
	 */
	int additionalSize();

	// TODO: Change the method so that implementing classes are not required to let internal data structures escape.
	/*
	 * Creates and fills an array with the Cartesian coordinates resulting from
	 * a specific angle.
	 * 
	 * @param id the atom id.
	 * @return a <code>double</code> array of length <code>3</code>.
	 */
	/**
	 * Returns the internal array used for storing the Cartesian coordinates
	 * resulting from a specific angle.
	 * <p>
	 * Note: This is so because it makes it easy to visualize a
	 * {@link CartesianModel} while it changes. However, it should be changed to
	 * work in a better way so that the internal arrays do not escape the
	 * implementing class. Hence, use {@link #get(int, double[])} if the
	 * intention is to not have a new array created.
	 * 
	 * @param id the angle id.
	 * @return a <code>double</code> array of length <code>3</code>.
	 */
	double[] get(int id);

	/**
	 * Fills an array with the Cartesian coordinates resulting from a specific
	 * angle.
	 * 
	 * @param id the angle id.
	 * @param xyz the array to fill.
	 * @throws NullPointerException if <code>xyz</code> is <code>null</code>.
	 * @throws ArrayIndexOutOfBoundsException if <code>xyz.length < 3</code>.
	 */
	void get(int id, double[] xyz);

	// TODO: Change the method so that implementing classes are not required to let internal data structures escape.
	/*
	 * Creates and fills an array with the Cartesian coordinates of a point that
	 * is not the result of a specific angle.
	 * 
	 * @param id the atom id.
	 * @return a <code>double</code> array of length <code>3</code>.
	 */
	/**
	 * Returns the internal array used for storing the Cartesian coordinates of
	 * a point that is not the result of a specific angle.
	 * <p>
	 * Note: This is so because it makes it easy to visualize a
	 * {@link CartesianModel} while it changes. However, it should be changed to
	 * work in a better way so that the internal arrays do not escape the
	 * implementing class. Hence, use {@link #getAdditional(int, double[])} if
	 * the intention is to not have a new array created.
	 * 
	 * @param id the point id.
	 * @return a <code>double</code> array of length <code>3</code>.
	 */
	double[] getAdditional(int id);

	/**
	 * Fills an array with the Cartesian coordinates of a point that is not the
	 * result of a specific angle.
	 * 
	 * @param id the point id.
	 * @param xyz the array to fill.
	 * @throws NullPointerException if <code>xyz</code> is <code>null</code>.
	 * @throws ArrayIndexOutOfBoundsException if <code>xyz.length < 3</code>.
	 */
	void getAdditional(int id, double[] xyz);
}
