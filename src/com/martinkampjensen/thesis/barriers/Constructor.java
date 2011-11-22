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

package com.martinkampjensen.thesis.barriers;

import java.io.File;
import java.util.List;

import com.martinkampjensen.thesis.barriers.neighborhood.Neighborhood;
import com.martinkampjensen.thesis.barriers.neighborhood.RmsdAngleDifferenceNeighborhood;
import com.martinkampjensen.thesis.connection.Connector;
import com.martinkampjensen.thesis.model.BarrierForest;
import com.martinkampjensen.thesis.model.Model;

/**
 * TODO: Document {@link Constructor}.
 */
public interface Constructor
{
	/**
	 * The default threshold value.
	 */
	double DEFAULT_THRESHOLD = Double.MAX_VALUE;

	/**
	 * Constructs a barrier forest using {@link #DEFAULT_THRESHOLD} as the
	 * threshold.
	 * 
	 * @param minima the minimized models to use.
	 * @param connector the connector to use.
	 * @return the barrier forest.
	 * @throws NullPointerException if <code>minima == null</code> or if
	 *         <code>connector == null</code>.
	 * @throws IllegalArgumentException if <code>minima.size() < 2</code>.
	 */
	BarrierForest construct(List<Model> minima, Connector connector);

	/**
	 * Constructs a barrier forest. Depending on the fitness value threshold and
	 * the fitness values of the minima and the barriers, the constructed forest
	 * may consist of several trees.
	 * 
	 * @param minima the minimized models to use.
	 * @param threshold the fitness value threshold.
	 * @return the barrier forest.
	 * @throws NullPointerException if <code>minima == null</code> or if
	 *         <code>connector == null</code>.
	 * @throws IllegalArgumentException if <code>minima.size() < 2</code>.
	 */
	BarrierForest construct(List<Model> minima, Connector connector,
			double threshold);

	/**
	 * As {@link #construct(File, File, File, Neighborhood, double, double)},
	 * but with the neighborhood set as {@link RmsdAngleDifferenceNeighborhood}.
	 */
	BarrierForest construct(File moleculeFile, File trajectoryFile,
			File energyFile, double minDistance, double maxDistance);

	/**
	 * Constructs a barrier forest. Depending on the energy values of the
	 * conformations in the trajectory, the constructed forest may consist of
	 * several trees.
	 * 
	 * @param moleculeFile the file containing the molecule (PDB format).
	 * @param trajectoryFile the file containing the trajectory (XTC format).
	 * @param energyFile the file containing energies for the exact frames in
	 *        the trajectory.
	 * @param neighborhood the neighborhood to use for pruning and neighbor
	 *        calculation (used with <code>minDistance</code> and
	 *        <code>maxDistance</code>).
	 * @param minDistance the pruning threshold, that is, how distant (to some
	 *        other conformation) a conformation must be to be accepted when
	 *        pruning. The value <code>0</code> disables pruning.
	 * @param maxDistance the neighborhood threshold, that is, how close to
	 *        another conformation a conformation must be for them to be
	 *        considered neighbors. The value <code>-1</code> enables
	 *        calculation of which <code>maxDistance</code> value that is the
	 *        limit between constructing a tree or a forest (multiple trees). 
	 * @return the barrier forest.
	 * @throws NullPointerException if <code>moleculeFile == null</code> or if
	 *         <code>trajectoryFile == null</code> or if
	 *         <code>energyFile == null</code> or if
	 *         <code>neighborhood == null</code>.
	 * @throws IllegalArgumentException if <code>minDistance &lt; 0</code> or if
	 *         <code>maxDistance &lt; 0 && maxDistance != 1</code>.
	 */
	BarrierForest construct(File moleculeFile, File trajectoryFile,
			File energyFile, Neighborhood neighborhood, double minDistance,
			double maxDistance);
}
