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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.martinkampjensen.thesis.barriers.neighborhood.RmsdAngleDifferenceNeighborhood;
import com.martinkampjensen.thesis.connection.Connector;
import com.martinkampjensen.thesis.model.BarrierForest;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.model.Node;
import com.martinkampjensen.thesis.model.NodeFactory;
import com.martinkampjensen.thesis.util.Debug;
import com.martinkampjensen.thesis.util.Util;

/**
 * An implementation of the {@link Constructor} interface that connects all
 * pairs of minima.
 */
public final class AllMinimaPairsConstructor extends AbstractConstructor
{
	public AllMinimaPairsConstructor()
	{
		super();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation connects all pairs of minima in order to create the
	 * barrier forest.
	 * 
	 * @throws NullPointerException {@inheritDoc}
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	public BarrierForest construct(List<Model> minima, Connector connector,
			double threshold)
	{
		check(minima, connector, threshold);

		// TODO: Why 0.5 as pruning threshold?
		minima = prune(minima, new RmsdAngleDifferenceNeighborhood(), 0.5);

		final List<Barrier> barriers = connectAllMinimaPairs(minima, connector);
		final Node[] nodes = createLeaves(minima);
		final int nBarriers = barriers.size();

		Debug.line("Constructing barrier forest");

		for(int i = 0; i < nBarriers; i++) {
			final Barrier barrier = barriers.get(i);
			final int fromId = barrier.getFromId();
			final int toId = barrier.getToId();
			final Node from = nodes[fromId];
			final Node to = nodes[toId];
			final double value = barrier.getValue();

			if(value > threshold) {
				// Barriers are sorted in ascending order.
				break;
			}
			else if(from == to) {
				// Already processed.
				continue;
			}

			final Node node = NodeFactory.create(barrier.getModel(), from, to);

			// Change references to "from" or "to" nodes to their new parent.
			if(Util.changeReferences(nodes, from, to, node)) {
				// When all references point to the same node, the barrier tree
				// has been constructed.
				break;
			}
		}

		final List<Node> roots = Util.removeDuplicates(nodes);
		return createForest(roots, minima.size());
	}

	/**
	 * Connects all pairs of minima in a list and returns the resulting list of
	 * barriers.
	 * <p>
	 * Note that the ids contained in the returned list of barriers match the
	 * order of the minima where the first minima has id <code>0</code> and the
	 * last minima has id <code>minima.size() - 1</code>.
	 * 
	 * @param minima the list of minima.
	 * @param connector the connector to use for connecting the minima.
	 * @return the sorted list of barriers connecting the minima.
	 */
	private static final List<Barrier> connectAllMinimaPairs(List<Model> minima,
			Connector connector)
	{
		Debug.line("Connecting all pairs of minima");

		final int nMinima = minima.size();
		final int nBarriers = nMinima * (nMinima - 1) / 2;
		final List<Barrier> barriers = new ArrayList<Barrier>(nBarriers);

		// For status.
		final int calculationsTotal = nBarriers;
		final boolean performStatus = (calculationsTotal >= 10);
		final int calculationsTwentieth =
			Math.max(1, (int)Math.floor(calculationsTotal / 20d));
		int calculationsMilestone = calculationsTwentieth;
		int calculationsDone = 0;
		if(performStatus) System.err.print("Connections performed: [0%");

		for(int i = 0; i < nMinima; i++) {
			final Model from = minima.get(i);

			for(int j = i + 1; j < nMinima; j++) {
				final Model to = minima.get(j);
				final Model barrierModel = connector.connect(from, to);
				barriers.add(new Barrier(i, j, barrierModel));
			}

			// For status.
			if(performStatus) {
				calculationsDone += nMinima - 1 - i;
				if(calculationsDone >= calculationsMilestone
						&& i != nMinima - 1) {
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

		Collections.sort(barriers);

		return barriers;
	}
}
