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

import java.io.Serializable;
import java.util.List;

import com.martinkampjensen.thesis.barriers.neighborhood.Neighborhood;

/**
 * A {@link BarrierForest} consists of a number of {@link BarrierTree}s. It
 * supports different measures that can be used to classify it.
 */
public interface BarrierForest extends Serializable
{
	/**
	 * Calculates different measures that can be used to classify this object.
	 * The method should only perform calculations the first time it is called.
	 */
	void calculateMeasures();

	// TODO: Document BarrierForest measures.
	int getNumberOfLeaves();
	double getMinimumValue();
	double getMinimumBarrierValue();
	double getMaximumBarrierValue();
	double getTotalBarrierValue();
	double getTotalConnectionValue();
	Node getMinimum();
	Node getMinimumBarrier();
	Node getMaximumBarrier();

	/**
	 * Returns the number of models used to build this forest.
	 * 
	 * @return the number of models.
	 */
	int modelsUsed();

	/**
	 * Returns the pruning threshold.
	 * 
	 * @return the pruning threshold, or <code>-1</code> if this forest was not
	 *         created using one.
	 */
	double getPruningThreshold();

	/**
	 * Returns the neighbor threshold (maximum distance between two models for
	 * them to be considered neighbors).
	 * 
	 * @return the neighbor threshold, or <code>-1</code> if this forest was not
	 *         created using one.
	 */
	double getNeighborThreshold();

	/**
	 * Returns the neighborhood used when constructing this forest.
	 * 
	 * @return the neighborhood, or <code>null</code> if none was used.
	 */
	Neighborhood getNeighborhood();

	/**
	 * Returns the number of barrier trees in this forest.
	 * 
	 * @return the number of barrier trees.
	 */
	int getNumberOfTrees();

	/**
	 * Returns a specific barrier tree contained in this forest.
	 * 
	 * @param id id of the barrier tree.
	 * @return the barrier tree.
	 */
	BarrierTree getTree(int id);

	/**
	 * Traverses all trees in this forest and returns the node with a specific
	 * id.
	 * 
	 * @param id id of the node to find.
	 * @return the node if it exists, or <code>null</code>.
	 */
	Node find(int id);

	/**
	 * Returns a list of models connecting the models represented by two nodes.
	 * 
	 * @param fromId the starting node.
	 * @param toId the ending node.
	 * @return the models, in order, connecting the model represented by the
	 *         <code>fromId</code> node and the <code>toId</code> node.
	 * @throws IllegalArgumentException if <code>fromId == toId</code> or if
	 *         <code>fromId</code> and <code>toId</code> does not exist in the
	 *         same tree in this forest.
	 */
	List<Model> findConnectingModels(int fromId, int toId);
}
