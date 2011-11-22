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

import com.martinkampjensen.thesis.model.BarrierForest;
import com.martinkampjensen.thesis.model.BarrierTree;
import com.martinkampjensen.thesis.model.CartesianModel;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.model.Node;

/**
 * Note: This class should not be used as is because the same conformation can
 * exist with different Cartesian coordinates because of rotation. To fix this,
 * some kind of fitting is required.
 * <p>
 * TODO: Document {@link CartesianDeviationColorer}.
 * TODO: Iterative implementation.
 */
public final class CartesianDeviationColorer implements Colorer
{
	private static final int DIMENSIONS = 3;
	private static final double BRIGHTNESS_FACTOR = 0.95f;

	public CartesianDeviationColorer()
	{
	}

	/**
	 * Colors each {@link Node} in a {@link BarrierForest} according to the
	 * Normalized Root Mean Squared Deviation (NRMSD) from the minimum node (the
	 * node with the lowest return value of {@link Node#getValue()}) in the
	 * forest.
	 * <p>
	 * The deviation is based on the Cartesian coordinates resulting from each
	 * angle value in the {@link Model} represented by each node.
	 * <p>
	 * The minimum will be colored black. The more a node deviates in the X
	 * dimension, the more red it will be. For the Y dimension, it will be more
	 * green. For the Z dimension, it will be more blue.
	 * <p>
	 * Note that {@link CartesianDeviationColorer} only works with nodes
	 * containing {@link Model}s that are instances of {@link CartesianModel}.
	 * 
	 * @param forest the forest containing the nodes to color.
	 * @see <a href="http://en.wikipedia.org/wiki/Root_mean_square_deviation">NRMSD</a>
	 * @throws NullPointerException {@inheritDoc}
	 */
	@Override
	public void color(BarrierForest forest)
	{
		if(forest == null) {
			throw new NullPointerException("forest == null");
		}

		final double[] range = findRange(forest);
		color(forest, range);
	}

	/**
	 * Colors each {@link Node} in a {@link BarrierTree} according to the
	 * Normalized Root Mean Squared Deviation (NRMSD) from the minimum node (the
	 * node with the lowest return value of {@link Node#getValue()}) in the
	 * tree.
	 * <p>
	 * The deviation is based on the Cartesian coordinates resulting from each
	 * angle value in the {@link Model} represented by each node.
	 * <p>
	 * The minimum will be colored black. The more a node deviates in the X
	 * dimension, the more red it will be. For the Y dimension, it will be more
	 * green. For the Z dimension, it will be more blue.
	 * <p>
	 * Note that {@link CartesianDeviationColorer} only works with nodes
	 * containing {@link Model}s that are instances of {@link CartesianModel}.
	 * 
	 * @throws NullPointerException {@inheritDoc}
	 */
	@Override
	public final void color(BarrierTree tree)
	{
		if(tree == null) {
			throw new NullPointerException("tree == null");
		}

		final double[] range = findRange(tree);
		color(tree, range);
	}

	/**
	 * Colors a {@link Node} black, that is, {@link Color#BLACK}.
	 * 
	 * @throws NullPointerException {@inheritDoc}
	 */
	@Override
	public final void color(Node node)
	{
		if(node == null) {
			throw new NullPointerException("node == null");
		}

		node.setColor(Color.BLACK);
	}

	private static double[] findRange(BarrierForest forest)
	{
		final double[] min = new double[DIMENSIONS];
		final double[] max = new double[DIMENSIONS];
		final double[] temp = new double[DIMENSIONS];

		for(int i = 0; i < DIMENSIONS; i++) {
			min[i] = Double.POSITIVE_INFINITY;
			max[i] = Double.NEGATIVE_INFINITY;
		}

		final int nTrees = forest.getNumberOfTrees();
		for(int i = 0; i < nTrees; i++) {
			final BarrierTree tree = forest.getTree(i);
			findMinMax(tree, min, max, temp);
		}

		for(int i = 0; i < DIMENSIONS; i++) {
			max[i] -= min[i];
		}

		return max;
	}

	private static double[] findRange(BarrierTree tree)
	{
		final double[] min = new double[DIMENSIONS];
		final double[] max = new double[DIMENSIONS];
		final double[] temp = new double[DIMENSIONS];

		findMinMax(tree, min, max, temp);	

		for(int i = 0; i < DIMENSIONS; i++) {
			max[i] -= min[i];
		}

		return max;
	}

	private static void findMinMax(BarrierTree tree, double[] min, double[] max,
			double[] temp)
	{
		final Node root = tree.getRoot();
		final Model model = root.getModel();
		final int size = model.size();

		findMinMax(root, min, max, temp, size);
	}

	private static void findMinMax(Node root, double[] min, double[] max,
			double[] temp, int size)
	{
		if(root == null) {
			return;
		}

		for(int i = 0; i < size; i++) {
			final Model model = root.getModel();

			if(!(model instanceof CartesianModel)) {
				throw new ClassCastException(
				"CartesianDeviationColorer only supports CartesianModel");
			}

			final CartesianModel cModel = (CartesianModel)root.getModel();
			cModel.get(i, temp);

			for(int j = 0; j < DIMENSIONS; j++) {
				final double coordinate = temp[j];
				if(coordinate < min[j]) {
					min[j] = coordinate;
				}
			}

			for(int j = 0; j < DIMENSIONS; j++) {
				final double coordinate = temp[j];
				if(coordinate > max[j]) {
					max[j] = coordinate;
				}
			}
		}

		findMinMax(root.getLeft(), min, max, temp, size);
		findMinMax(root.getRight(), min, max, temp, size);
	}

	private static void color(BarrierForest forest, double[] range)
	{
		final Node goal = forest.getMinimum(); 
		final double[] temp1 = new double[DIMENSIONS];
		final double[] temp2 = new double[DIMENSIONS];
		final double[] temp3 = new double[DIMENSIONS];

		final int nTrees = forest.getNumberOfTrees();
		for(int i = 0; i < nTrees; i++) {
			final BarrierTree tree = forest.getTree(i);
			final Node root = tree.getRoot();
			final Model model = root.getModel();
			final int size = model.size();
			color(root, goal, range, temp1, temp2, temp3, size);
		}
	}

	private static void color(BarrierTree tree, double[] range)
	{
		final Node goal = tree.getMinimum(); 
		final double[] temp1 = new double[DIMENSIONS];
		final double[] temp2 = new double[DIMENSIONS];
		final double[] temp3 = new double[DIMENSIONS];

		final Node root = tree.getRoot();
		final Model model = root.getModel();
		final int size = model.size();
		color(root, goal, range, temp1, temp2, temp3, size);
	}

	private static void color(Node root, Node goal, double[] range,
			double[] temp1, double[] temp2, double[] temp3, int size)
	{
		if(root == null) {
			return;
		}

		doColor(root, goal, range, temp1, temp2, temp3, size);
		color(root.getLeft(), goal, range, temp1, temp2, temp3, size);
		color(root.getRight(), goal, range, temp1, temp2, temp3, size);
	}

	private static void doColor(Node node, Node goal, double[] range,
			double[] temp1, double[] temp2, double[] temp3, int size)
	{
		final CartesianModel goalModel = (CartesianModel)goal.getModel();
		final CartesianModel model = (CartesianModel)node.getModel();

		// "temp3" is used for calculating NRMSDs for the different dimensions
		// multiple times so reset it before using it.
		for(int i = 0; i < DIMENSIONS; i++) {
			temp3[i] = 0d;
		}

		// Squared deviation.
		for(int i = 0; i < size; i++) {
			goalModel.get(i, temp1);
			model.get(i, temp2);

			for(int j = 0; j < DIMENSIONS; j++) {
				final double delta = temp1[j] - temp2[j];
				temp3[j] += delta * delta;
			}
		}

		// Mean square deviation.
		for(int i = 0; i < DIMENSIONS; i++) {
			temp3[i] /= size;
		}

		// Root mean square deviation.
		for(int i = 0; i < DIMENSIONS; i++) {
			temp3[i] = Math.sqrt(temp3[i]);
		}

		// Normalized root mean squared deviation.
		for(int i = 0; i < DIMENSIONS; i++) {
			temp3[i] /= range[i];
		}

		float red = 0f;
		if(DIMENSIONS >= 1) {
			red = (float)(BRIGHTNESS_FACTOR * temp3[0]);
		}

		float green = 0f;
		if(DIMENSIONS >= 2) {
			green = (float)(BRIGHTNESS_FACTOR * temp3[1]);
		}

		float blue = 0f;
		if(DIMENSIONS >= 3) {
			blue = (float)(BRIGHTNESS_FACTOR * temp3[2]);
		}

		node.setColor(new Color(red, green, blue));
	}
}
