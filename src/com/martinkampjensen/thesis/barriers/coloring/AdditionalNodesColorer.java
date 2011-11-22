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
import com.martinkampjensen.thesis.model.Node;

/**
 * An implementation of the {@link Colorer} interface that colors a node based
 * on how many models it represents.
 * <p>
 * A node can represent many models because of the flooding algorithm.
 */
public final class AdditionalNodesColorer implements Colorer
{
	/**
	 * Constructs a new colorer that colors nodes using the following formular:
	 * <p>
	 * <pre>
	 * int max = // maximum number of models represented by one node
	 * int count = node.getAdditionalNodesCount();
	 * Color color = new Color(0f, count / (float)max, 0f);
	 * node.setColor(color);
	 * </pre>
	 */
	public AdditionalNodesColorer()
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

		final int nTrees = forest.getNumberOfTrees();
		int max = 0;

		for(int i = 0; i < nTrees; i++) {
			int count = findMaxCount(forest.getTree(i));
			if(count > max) {
				max = count;
			}
		}

		for(int i = 0; i < nTrees; i++) {
			colorTree(forest.getTree(i), max);
		}
	}

	/**
	 * @throws NullPointerException {@inheritDoc}
	 */
	@Override
	public void color(BarrierTree tree)
	{
		if(tree == null) {
			throw new NullPointerException("tree == null");
		}

		colorTree(tree, findMaxCount(tree));
	}

	/**
	 * @throws UnsupportedOperationException always.
	 */
	@Override
	public void color(Node node)
	{
		throw new UnsupportedOperationException(
		"Use color(BarrierForest) or color(BarrierTree)");
	}

	private static int findMaxCount(BarrierTree tree)
	{
		final Deque<Node> stack = new ArrayDeque<Node>();
		stack.addFirst(tree.getRoot());
		int max = 0;

		while(!stack.isEmpty()) {
			final Node node = stack.removeFirst();

			if(node.isInternal()) {
				stack.addFirst(node.getRight());
				stack.addFirst(node.getLeft());
			}

			final int count = node.getAdditionalModelsCount();
			if(count > max) {
				max = count;
			}
		}

		return max;
	}

	private static void colorTree(BarrierTree tree, int max)
	{
		final Deque<Node> stack = new ArrayDeque<Node>();
		stack.addFirst(tree.getRoot());

		while(!stack.isEmpty()) {
			final Node node = stack.removeFirst();

			if(node.isInternal()) {
				stack.addFirst(node.getRight());
				stack.addFirst(node.getLeft());
			}

			final float red = 0f;
			final float green = node.getAdditionalModelsCount() / (float)max;
			final float blue = 0f;

			node.setColor(new Color(red, green, blue));
		}
	}
}
