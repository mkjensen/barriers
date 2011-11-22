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

import com.martinkampjensen.thesis.barriers.neighborhood.Neighborhood;
import com.martinkampjensen.thesis.barriers.neighborhood.RmsdAngleDifferenceNeighborhood;
import com.martinkampjensen.thesis.model.BarrierForest;
import com.martinkampjensen.thesis.model.BarrierTree;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.model.Node;

/**
 * TODO: Iterative implementation.
 * 
 * An implementation of the {@link Colorer} interface that colors nodes
 * according to the RMSD difference between the angles of the model of the node
 * and the angles of the model of the node with the minimum value.
 * <p>
 * The minimum node will be black and the more different a node is, the
 * more light green it will be.
 */
public final class RmsdAngleDifferenceColorer implements Colorer
{
	private static final Neighborhood _neighborhood =
		new RmsdAngleDifferenceNeighborhood();

	public RmsdAngleDifferenceColorer()
	{
	}

	/**
	 * Colors each {@link Node} <code>n</code> in each {@link BarrierTree} of a
	 * {@link BarrierForest} according to the RMSD difference between the angles
	 * of the model of the node <code>n</code> and the angles of the model of
	 * the node with the minimum value in the forest.
	 * <p>
	 * The minimum node will be black and the more different a node is, the
	 * more light green it will be.
	 * 
	 * @throws NullPointerException {@inheritDoc}
	 */
	@Override
	public void color(BarrierForest forest)
	{
		if(forest == null) {
			throw new NullPointerException("forest == null");
		}

		final int nTrees = forest.getNumberOfTrees();
		final Node minNode = forest.getMinimum();

		for(int i = 0; i < nTrees; i++) {
			final BarrierTree tree = forest.getTree(i);
			final Node root = tree.getRoot();
			colorHelper(root, minNode);
		}
	}

	/**
	 * Colors each {@link Node} <code>n</code> in a {@link BarrierTree}
	 * according to the RMSD difference between the angles of the model of the
	 * node <code>n</code> and the angles of the model of the node with the
	 * minimum value in the tree.
	 * <p>
	 * The minimum node will be black and the more different a node is, the
	 * more light green it will be.
	 * 
	 * @throws NullPointerException {@inheritDoc}
	 */
	@Override
	public final void color(BarrierTree tree)
	{
		if(tree == null) {
			throw new NullPointerException("tree == null");
		}

		colorHelper(tree.getRoot(), tree.getMinimum());
	}

	/**
	 * Not supported by this colorer, use {@link #color(BarrierForest)} or
	 * {@link #color(BarrierTree)} instead.
	 * 
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public final void color(Node node)
	{
		throw new UnsupportedOperationException();
	}

	private static void colorHelper(Node node, Node minNode)
	{
		if(node == null) {
			return;
		}

		color(node, minNode);
		colorHelper(node.getLeft(), minNode);
		colorHelper(node.getRight(), minNode);
	}

	private static void color(Node node, Node minNode)
	{
		final Model model = node.getModel();
		final Model minModel = minNode.getModel();

		// Distance between the two models.
		double green = _neighborhood.distance(model, minModel);

		// In relation to the maximum distance between two models. 
		green /= _neighborhood.maximumDistance(model);

		node.setColor(new Color(0f, (float)green, 0f));
	}
}
