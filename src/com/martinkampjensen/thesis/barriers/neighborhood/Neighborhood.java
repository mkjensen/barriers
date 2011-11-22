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

package com.martinkampjensen.thesis.barriers.neighborhood;

import java.util.List;

import com.martinkampjensen.thesis.model.Model;

/**
 * Defines a neighborhood for models. Two models are neighbors if and only if
 * the {@link #isNeighbors(Model, Model, double)} method returns
 * <code>true</code>.
 */
public interface Neighborhood
{
	/**
	 * Calculates and returns the distance between two models according to the
	 * neighborhood defined by this object.
	 * 
	 * @param first the first model.
	 * @param second the second model.
	 * @return the distance.
	 */
	double distance(Model first, Model second);

	/**
	 * Calculates and returns the minimum distance between two models that has
	 * the same size as a model.
	 * <p>
	 * The minimum distance is defined to be <code>0</code> which means that two
	 * models are identical with regard to this neighborhood.
	 * 
	 * @param model the model.
	 * @return <code>0</code>.
	 */
	double minimumDistance(Model model);

	/**
	 * Calculates and returns the maximum distance between two models that has
	 * the same size as a model.
	 * 
	 * @param model the model.
	 * @return the maximum distance.
	 */
	double maximumDistance(Model model);

	/**
	 * Calculates and returns whether two models are neighbors in the
	 * neighborhood defined by this object.
	 * 
	 * @param first the first model.
	 * @param second the second model.
	 * @param maxDistance the maximum distance between the two models for them
	 *        to be neighbors.
	 * @return <code>true</code> if and only if
	 *         <code>distance(first, second) &le; maxDistance</code>.
	 */
	boolean isNeighbors(Model first, Model second, double maxDistance);

	/**
	 * Calculates and returns the neighbors for a list of models.
	 * 
	 * @param models the models.
	 * @param maxDistance the maximum distance between the two models for them
	 *        to be neighbors.
	 * @param allowDebugPrints whether or not debug prints are allowed.
	 * @return the neighbors.
	 */
	int[][] calculateNeighbors(List<Model> models, double maxDistance,
			boolean allowDebugPrints);
}
