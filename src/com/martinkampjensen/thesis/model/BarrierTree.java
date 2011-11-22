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

/**
 * A {@link BarrierTree} is a full binary tree that has a {@link Node} as its
 * root. The internal {@link Node}s represent barrier values between leaf
 * {@link Node}s.
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Binary_tree">Binary tree</a>
 */
public interface BarrierTree extends Serializable
{
	/**
	 * Calculates different measures that can be used to classify this object.
	 * The method should only perform calculations the first time it is called.
	 */
	void calculateMeasures();

	/**
	 * Recalculates measures that depend on a value from a {@link BarrierForest}
	 * which this object has been made a part of.
	 * 
	 * @see #calculateMeasures() 
	 */
	void recalculateMeasures(BarrierForest forest);

	// TODO: Document BarrierTree measures.
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
	 * Returns the root of this tree.
	 * 
	 * @return the root of this tree.
	 */
	Node getRoot();

	/**
	 * Traverses the tree and returns the node with a specific id.
	 * 
	 * @param id id of the node to find.
	 * @return the node if it exists, or <code>null</code>.
	 */
	Node find(int id);
}
