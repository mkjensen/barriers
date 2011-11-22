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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.martinkampjensen.thesis.Main;
import com.martinkampjensen.thesis.StatusCode;
import com.martinkampjensen.thesis.barriers.neighborhood.Neighborhood;
import com.martinkampjensen.thesis.barriers.neighborhood.RmsdAngleDifferenceNeighborhood;
import com.martinkampjensen.thesis.connection.Connector;
import com.martinkampjensen.thesis.model.BarrierForest;
import com.martinkampjensen.thesis.model.BarrierForestFactory;
import com.martinkampjensen.thesis.model.BarrierTree;
import com.martinkampjensen.thesis.model.BarrierTreeFactory;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.model.Node;
import com.martinkampjensen.thesis.model.NodeFactory;
import com.martinkampjensen.thesis.util.Debug;

/**
 * This class provides a skeletal implementation of the {@link Constructor}
 * interface, to minimize the effort required to implement this interface.
 * <p>
 * An implementation only needs to implement
 * {@link #construct(List, Connector, double)} and/or
 * {@link #construct(File, File, File)}. The implementations is this class
 * always throws an {@link UnsupportedOperationException} when the methods are
 * called.
 */
public abstract class AbstractConstructor implements Constructor
{
	protected AbstractConstructor()
	{
	}

	/**
	 * @throws NullPointerException {@inheritDoc}
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	public final BarrierForest construct(List<Model> minima,
			Connector connector)
	{
		return construct(minima, connector, DEFAULT_THRESHOLD);
	}

	/**
	 * This method is not implemented.
	 * 
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public BarrierForest construct(List<Model> minima, Connector connector,
			double threshold)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public final BarrierForest construct(File moleculeFile, File trajectoryFile,
			File energyFile, double minDistance, double maxDistance)
	{
		return construct(moleculeFile, trajectoryFile, energyFile,
				new RmsdAngleDifferenceNeighborhood(), minDistance,
				maxDistance);
	}

	/**
	 * This method is not implemented.
	 * 
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public BarrierForest construct(File moleculeFile, File trajectoryFile,
			File energyFile, Neighborhood neighborhood, double minDistance,
			double maxDistance)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	protected static final void check(List<Model> minima, Connector connector,
			double threshold)
	{
		if(minima == null) {
			throw new NullPointerException("minima == null");
		}
		else if(connector == null) {
			throw new NullPointerException("connector == null");
		}
		else if(minima.size() < 2) {
			throw new IllegalArgumentException("minima.size() < 2");
		}
	}

	protected static final void check(File moleculeFile, File trajectoryFile,
			File energyFile, Neighborhood neighborhood, double minDistance,
			double maxDistance)
	{
		if(moleculeFile == null) {
			throw new NullPointerException("moleculeFile == null");
		}
		else if(trajectoryFile == null) {
			throw new NullPointerException("trajectoryFile == null");
		}
		else if(energyFile == null) {
			throw new NullPointerException("energyFile == null");
		}
		else if(neighborhood == null) {
			throw new NullPointerException("neighborhood == null");
		}
		else if(!moleculeFile.exists() || !moleculeFile.isFile()) {
			Main.errorExit("moleculeFile does not exist or is not a file",
					StatusCode.IO);
		}
		else if(!trajectoryFile.exists() || !trajectoryFile.isFile()) {
			Main.errorExit("trajectoryFile does not exist or is not a file",
					StatusCode.IO);
		}
		else if(!energyFile.exists() || !energyFile.isFile()) {
			Main.errorExit("energyFile does not exist or is not a file",
					StatusCode.IO);
		}
		else if(minDistance < 0) {
			throw new IllegalArgumentException("minDistance < 0");
		}
		else if(maxDistance < 0 && maxDistance != -1d) {
			throw new IllegalArgumentException("maxDistance < 0 but not -1");
		}
	}

	/**
	 * Creates leaf nodes from a list of minima.
	 * 
	 * @param minima the list of minima.
	 * @return an array of the leaf nodes created.
	 * @throws NullPointerException if <code>minima == null</code>.
	 */
	protected static Node[] createLeaves(List<Model> minima)
	{
		if(minima == null) {
			throw new NullPointerException("minima == null");
		}

		Debug.line("Creating leaf nodes");

		// This ensures that the ids of the created Node objects will start at
		// 0. Hence, the ids of the Node objects to be created will match their
		// location in the following Node array.
		NodeFactory.reset();

		final int nMinima = minima.size();
		final Node[] nodes = new Node[nMinima];

		for(int i = 0; i < nMinima; i++) {
			final Model minimum = minima.get(i);
			final Node node = NodeFactory.create(minimum);
			nodes[i] = node;
		}

		return nodes;
	}
	
	/**
	 * Performs pruning by comparing every model against all other models and
	 * only keeping the lowest fitness model when two models are within a
	 * certain distance of each other.
	 * 
	 * @param models the models to prune.
	 * @param neighborhood the neighborhood to use.
	 * @param minDistance the minimum distance between two models for them to
	 *        avoid pruning, as per
	 *        {@link Neighborhood#distance(Model, Model)}.
	 * @return the pruned list.
	 */
	protected static List<Model> prune(List<Model> models,
			Neighborhood neighborhood, double minDistance)
	{
		final int nModels = models.size();
		final boolean[] isDiscarded = new boolean[nModels];

		// For status.
		final long calculationsTotal = (long)nModels * (nModels - 1) / 2;
		final boolean performStatus = (calculationsTotal >= 500000000);
		final long calculationsTwentieth =
			Math.max(1, (long)Math.floor(calculationsTotal / 20d));
		long calculationsMilestone = calculationsTwentieth;
		long calculationsDone = 0;
		if(performStatus) System.err.print("Pruning completed: [0%");

		for(int i = 0; i < nModels; i++) {
			if(!isDiscarded[i]) {
				final Model first = models.get(i);

				for(int j = i + 1; j < nModels; j++) {
					if(isDiscarded[j]) {
						continue;
					}

					final Model second = models.get(j);

					if(neighborhood.distance(first, second) < minDistance) {
						if(first.evaluate() <= second.evaluate()) {
							isDiscarded[j] = true;
						}
						else {
							isDiscarded[i] = true;
							break;
						}
					}
				}
			}

			// For status.
			if(performStatus) {
				calculationsDone += nModels - 1 - i;
				if(calculationsDone >= calculationsMilestone
						&& i != nModels - 1) {
					System.err.print("..." + (int)(100 * calculationsDone /
							(double)calculationsTotal) + "%");
					if(calculationsDone == calculationsTotal)
						System.err.println("]");
					while(calculationsDone >= calculationsMilestone)
						calculationsMilestone += calculationsTwentieth;
					calculationsMilestone =
						Math.min(calculationsMilestone, calculationsTotal);
				}
			}
		}

		final List<Model> pruned = new ArrayList<Model>();
		for(int i = 0; i < nModels; i++) {
			if(!isDiscarded[i]) {
				pruned.add(models.get(i));
			}
		}

		final int nRemain = pruned.size();
		final int nRemoved = nModels - nRemain;
		Debug.line("Pruning discarded %d models (%f%%), %d models remain",
				nRemoved, (nRemoved) / (double)nModels * 100, nRemain);

		return pruned;
	}

	protected static final BarrierForest createForest(Node[] roots,
			int modelsUsed)
	{
		return createForest(Arrays.asList(roots), modelsUsed);
	}

	protected static final BarrierForest createForest(List<Node> roots,
			int modelsUsed)
	{
		return createForest(roots, modelsUsed, -1d, -1d, null, true);
	}

	protected static final BarrierForest createForest(List<Node> roots,
			int modelsUsed, double pruningThreshold, double neighborThreshold,
			Neighborhood neighborhood, boolean allowDebugPrints)
	{
		final int nRoots = roots.size();
		final BarrierTree[] trees = new BarrierTree[nRoots];

		for(int i = 0; i < nRoots; i++) {
			final Node root = roots.get(i).clean();
			trees[i] = BarrierTreeFactory.create(root, allowDebugPrints);
		}

		return BarrierForestFactory.create(trees, modelsUsed, pruningThreshold,
				neighborThreshold, neighborhood, allowDebugPrints);
	}
}
