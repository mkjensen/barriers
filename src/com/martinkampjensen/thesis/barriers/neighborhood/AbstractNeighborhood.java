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

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.util.Debug;

/**
 * This class provides a skeletal implementation of the {@link Neighborhood}
 * interface, to minimize the effort required to implement this interface.
 * <p>
 * An implementation only needs to implement {@link #distance(Model, Model)} and
 * {@link #maximumDistance(Model)}.
 */
public abstract class AbstractNeighborhood implements Neighborhood
{
	protected AbstractNeighborhood()
	{
	}

	public static final int[][] calculateNeighbors(List<Model> models,
			Neighborhood neighborhood, double maxDistance,
			boolean allowDebugPrints)
	{
		final int nModels = models.size();
		final IntList[] lists = new ArrayIntList[nModels];
		final int[][] arrays = new int[nModels][];

		for(int i = 0; i < nModels; i++) {
			lists[i] = new ArrayIntList();
		}

		// For status.
		final long calculationsTotal = (long)nModels * (nModels - 1) / 2;
		final boolean performStatus = (calculationsTotal >= 2500000);
		final long calculationsTwentieth =
			Math.max(1, (long)Math.floor(calculationsTotal / 20d));
		long calculationsMilestone = calculationsTwentieth;
		long calculationsDone = 0;
		if(performStatus) System.err.print("Neighbors calculated: [0%");

		for(int i = 0; i < nModels; i++) {
			final IntList list = lists[i];
			final Model first = models.get(i);

			for(int j = i + 1; j < nModels; j++) {
				final Model second = models.get(j);

				if(neighborhood.isNeighbors(first, second, maxDistance)) {
					list.add(j);
					lists[j].add(i);
				}
			}

			arrays[i] = list.toArray();
			lists[i] = null; // For garbage collection.

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

		if(allowDebugPrints) calculateNeighborMeasures(arrays);
		return arrays;
	}

	@Override
	public final double minimumDistance(Model model)
	{
		return 0d;
	}

	@Override
	public final boolean isNeighbors(Model first, Model second,
			double maxDistance)
	{
		return distance(first, second) <= maxDistance;
	}

	@Override
	public final int[][] calculateNeighbors(List<Model> models,
			double maxDistance, boolean allowDebugPrints)
	{
		return calculateNeighbors(models, this, maxDistance, allowDebugPrints);
	}

	private static final double calculateNeighborMeasures(int[][] neighbors)
	{
		final int nModels = neighbors.length;
		int sum = 0;
		int max = Integer.MIN_VALUE;
		int min = Integer.MAX_VALUE;

		for(int i = 0; i < nModels; i++) {
			final int nNeighbors = neighbors[i].length;

			sum += nNeighbors;

			// TODO: This is wrong: 1) max is set, 2) max is set again, but min is not set to former max
			if(nNeighbors > max) {
				max = nNeighbors;
			}
			else if(nNeighbors < min) {
				min = nNeighbors;
			}
		}

		// Avoid insane measures in borderline cases.
		double avg;
		double inPercent;
		if(nModels == 0) {
			avg = min = max = 0;
			inPercent = 1;
		}
		else if(nModels == 1) {
			avg = min = max;
			inPercent = 1;
		}
		else {
			avg = sum / (double)nModels;
			inPercent = (double)(nModels - 1) / 100;
		}

		Debug.line("Neighbors per model: min. %d (%f%%), max. %d (%f%%), "
				+ "avg. %f (%f%%)", min, min / inPercent, max, max / inPercent,
				avg, avg / inPercent);

		return avg;
	}
}
