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

package com.martinkampjensen.thesis.connection;

import com.martinkampjensen.thesis.Constant;
import com.martinkampjensen.thesis.model.Model;

/**
 * A {@link Connector} simulates the steps that needs to be taken for a specific
 * set of angle values of a {@link Model} to become another set of angle values.
 * <p>
 * During the simulation, the fitness values, that is, {@link Model#evaluate()},
 * are monitored so that a copy of the {@link Model} with the angle values that
 * resulted in the greatest fitness value can be returned. This specific
 * {@link Model} is called the barrier between the two sets of angle values
 * which are representing specific configurations of the same {@link Model}.
 */
public interface Connector
{
	// TODO: Introduce heuristic for all connectors that tries to figure out which way to optimally turn each angle.

	/**
	 * The default step size.
	 */
	double DEFAULT_STEP_SIZE = Math.toRadians(5);

	/**
	 * The minimum step size to be used. If the distance between two angles is
	 * less than this value, a step will not be performed in between. That is,
	 * the first angle is simply changed to the second.
	 */
	double MINIMUM_STEP_SIZE = Constant.DOUBLE_PRECISION;

	/**
	 * Computes and returns a {@link Model} with the angle values that resulted
	 * in the greatest fitness value, that is, {@link Model#evaluate()}, while
	 * changing the angle values of <code>from</code> to the angle values of
	 * <code>to</code>.
	 * <p>
	 * The step size is implementation specific.
	 * 
	 * @param from the starting angle values configuration.
	 * @param to the ending angle values configuration.
	 * @return the {@link Model} with the angle values that resulted
	 * in the greatest fitness value.
	 * @throws NullPointerException if <code>from == null</code> or if
	 *         <code>to == null</code>.
	 * @throws IllegalArgumentException if <code>to.size() != from.size</code>.
	 */
	Model connect(Model from, Model to);

	/**
	 * Computes and returns a {@link Model} with the angle values that resulted
	 * in the greatest fitness value, that is, {@link Model#evaluate()}, while
	 * changing the angle values of <code>from</code> to the angle values of
	 * <code>to</code>.
	 * <p>
	 * The effective step size, <code>S</code>, with which angles are changed in
	 * a finite number of steps, is restricted to
	 * <code>-stepSize &le; S &le; stepSize</code>. 
	 * 
	 * @param from the starting angle values configuration.
	 * @param to the ending angle values configuration.
	 * @param stepSize the step size.
	 * @return the {@link Model} with the angle values that resulted in the
	 *         greatest fitness value.
	 * @throws NullPointerException if <code>from == null</code> or if
	 *         <code>to == null</code>.
	 * @throws IllegalArgumentException if <code>to.size() != from.size</code>
	 *         or if <code>stepSize <= 0</code>.
	 */
	Model connect(Model from, Model to, double stepSize);
}
