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

import com.martinkampjensen.thesis.barriers.neighborhood.Neighborhood;
import com.martinkampjensen.thesis.model.impl.BarrierForestImpl;

/**
 * TODO: Document {@link BarrierForestFactory}.
 */
public final class BarrierForestFactory
{
	private BarrierForestFactory()
	{
	}

	/**
	 * Constructs a new {@link BarrierForest} with the given {@link BarrierTree}
	 * as its only tree.
	 * 
	 * @param tree the barrier tree to be contained in the barrier forest.
	 * @param modelsUsed the number of models used to create this forest.
	 * @param pruningThreshold the pruning threshold.
	 * @param neighborThreshold the neighbor threshold.
	 * @param neighborhood the neighborhood used.
	 * @param allowDebugPrints whether or not debug prints are allowed.
	 * @throws NullPointerException if <code>tree</code> is
	 *         <code>null</code>.
	 * @throws IllegalArgumentException if <code>modelsUsed &lt; 0</code>.
	 */
	public static BarrierForest create(BarrierTree tree, int modelsUsed,
			double pruningThreshold, double neighborThreshold,
			Neighborhood neighborhood, boolean allowDebugPrints)
	{
		return new BarrierForestImpl(tree, modelsUsed, pruningThreshold,
				neighborThreshold, neighborhood, allowDebugPrints);
	}

	/**
	 * Constructs a new {@link BarrierForest} containing the specified
	 * {@link BarrierTree}s.
	 * 
	 * @param trees the barrier trees to be contained in the barrier forest.
	 * @param modelsUsed the number of models used to create this forest.
	 * @param pruningThreshold the pruning threshold.
	 * @param neighborThreshold the neighbor threshold.
	 * @param neighborhood the neighborhood used.
	 * @param allowDebugPrints whether or not debug prints are allowed.
	 * @throws NullPointerException if <code>trees</code> is
	 *         <code>null</code> or if any element in <code>trees</code> is
	 *         <code>null</code>.
	 * @throws IllegalArgumentException if <code>modelsUsed &lt; 0</code>.
	 */
	public static BarrierForest create(BarrierTree[] trees, int modelsUsed,
			double pruningThreshold, double neighborThreshold,
			Neighborhood neighborhood, boolean allowDebugPrints)
	{
		return new BarrierForestImpl(trees, modelsUsed, pruningThreshold,
				neighborThreshold, neighborhood, allowDebugPrints);
	}
}
