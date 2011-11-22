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

package com.martinkampjensen.thesis.barriers.structuring;

import java.util.ArrayDeque;
import java.util.Deque;

import com.martinkampjensen.thesis.model.BarrierForest;
import com.martinkampjensen.thesis.model.BarrierTree;
import com.martinkampjensen.thesis.model.Node;

/**
 * This class provides a skeletal implementation of the {@link Structurer}
 * interface, to minimize the effort required to implement this interface.
 * <p>
 * An implementation only needs to implement {@link Structurer#structure(Node)}.
 */
public abstract class AbstractStructurer implements Structurer
{
	protected AbstractStructurer()
	{
	}

	/**
	 * @throws NullPointerException {@inheritDoc}
	 */
	@Override
	public final void structure(BarrierForest forest)
	{
		if(forest == null) {
			throw new NullPointerException("forest == null");
		}

		final int nTrees = forest.getNumberOfTrees();
		for(int i = 0; i < nTrees; i++) {
			final BarrierTree tree = forest.getTree(i);
			structure(tree);
		}
	}

	/**
	 * @throws NullPointerException {@inheritDoc}
	 */
	@Override
	public final void structure(BarrierTree tree)
	{
		if(tree == null) {
			throw new NullPointerException("tree == null");
		}

		final Node root = tree.getRoot();
		final Deque<Node> stack = new ArrayDeque<Node>();
		stack.addFirst(root);

		while(!stack.isEmpty()) {
			Node node = stack.removeFirst();

			if(node.isInternal()) {
				stack.addFirst(node.getRight());
				stack.addFirst(node.getLeft());
			}

			structure(node);
		}
	}
}
