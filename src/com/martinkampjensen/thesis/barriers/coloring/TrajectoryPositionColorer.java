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

package com.martinkampjensen.thesis.barriers.coloring;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Deque;

import com.martinkampjensen.thesis.model.BarrierForest;
import com.martinkampjensen.thesis.model.BarrierTree;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.model.Node;

/**
 * An implementation of the {@link Colorer} interface that colors a node
 * according to where its contained model originates in a trajectory.
 * <p>
 * This colorer relies on {@link BarrierForest#modelsUsed()} being equal to the
 * number of conformations in the trajectory. Further, the id of a model, as
 * returned by {@link Model#getId()}, must be the position (in the trajectory)
 * of the conformation used to create the model.
 * <p>
 * Ids must be in the range <code>0, 1, ..., {@link BarrierForest#modelsUsed()}
 * - 1</code>.
 * <p>
 * This implementation only supports {@link #color(BarrierForest)}. Other
 * methods always throw an {@link UnsupportedOperationException}.
 */
public final class TrajectoryPositionColorer implements Colorer
{
	/**
	 * Constructs a new colorer that colors nodes using the following formular:
	 * <p>
	 * <pre>
	 * int id = node.getModel().getId();
	 * int modelsUsed = {@link BarrierForest#modelsUsed()};
	 * Color color = new Color(id / (float)modelsUsed, 0f, 0f)
	 * node.setColor(color);
	 * </pre>
	 */
	public TrajectoryPositionColorer()
	{
	}

	/**
	 * @throws NullPointerException {@inheritDoc}
	 */
	@Override
	public void color(BarrierForest forest)
	{
		if(forest == null) {
			throw new NullPointerException("forest == null");
		}

		final int modelsUsed = forest.modelsUsed();
		final int nTrees = forest.getNumberOfTrees();

		for(int i = 0; i < nTrees; i++) {
			final BarrierTree tree = forest.getTree(i);
			colorTree(tree, modelsUsed);
		}

	}

	/**
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public void color(BarrierTree tree)
	{
		throw new UnsupportedOperationException("Use color(BarrierForest)");
	}

	/**
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public void color(Node node)
	{
		throw new UnsupportedOperationException("Use color(BarrierForest)");
	}

	private void colorTree(BarrierTree tree, int modelsUsed)
	{
		final Deque<Node> stack = new ArrayDeque<Node>();
		stack.addFirst(tree.getRoot());

		while(!stack.isEmpty()) {
			final Node node = stack.removeFirst();

			if(node.isInternal()) {
				stack.addFirst(node.getRight());
				stack.addFirst(node.getLeft());
			}

			final float red = node.getModel().getId() /(float) modelsUsed;
			final float green = 0f;
			final float blue = 0f;

			node.setColor(new Color(red, green, blue));
		}
	}
}
