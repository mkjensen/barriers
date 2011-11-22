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

import com.martinkampjensen.thesis.model.Node;

/**
 * A {@link WeightStructurer} is an implementation of the {@link Structurer}
 * interface. It structures based on the weights of the subtree of a
 * {@link Node}.
 * <p>
 * If the weight of the left child is smaller than or equal to the right child,
 * nothing is changed. Otherwise, the left and the right child is swapped so
 * that the right child always has at least the same weight as the left child. 
 */
public final class WeightStructurer extends AbstractStructurer
{
	public WeightStructurer()
	{
	}

	/**
	 * @throws NullPointerException {@inheritDoc}
	 */
	@Override
	public void structure(Node node)
	{
		if(node == null) {
			throw new NullPointerException("node == null");
		}

		if(node.isLeaf()) {
			return;
		}

		final Node left = node.getLeft();
		final Node right = node.getRight();
		final int leftWeight = left.getWeight();
		final int rightWeight = right.getWeight();

		if(leftWeight > rightWeight) {
			node.setLeft(right);
			node.setRight(left);
		}
	}
}
