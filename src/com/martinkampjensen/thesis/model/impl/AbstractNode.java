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

package com.martinkampjensen.thesis.model.impl;

import com.martinkampjensen.thesis.model.Node;
import com.martinkampjensen.thesis.util.Util;

/**
 * This class provides a skeletal implementation of the {@link Node} interface,
 * to minimize the effort required to implement this interface.
 */
public abstract class AbstractNode extends AbstractPoint implements Node
{
	private static final long serialVersionUID = -6882917325549014184L;

	protected AbstractNode()
	{
	}

	@Override
	public final String toString()
	{
		return getId() + "/" + getWeight() + "/" + getValue();
	}

	@Override
	public final int compareTo(Node other)
	{
		return Util.compare(this.getValue(), other.getValue());
	}

	@Override
	public final double getValue()
	{
		return getModel().evaluate();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation has the side effect that all weights are calculated.
	 * 
	 * @see #calculateWeight()
	 */
	@Override
	public final Node clean()
	{
		return cleanHelper(this);
	}

	// TODO: Iterative implementation.
	private static final Node cleanHelper(Node node)
	{
		if(node.isLeaf()) {
			return node;
		}

		final Node left = node.getLeft();
		final Node right = node.getRight();

		if(left.isLeaf() && Util.isEqual(left.getValue(), node.getValue())) {
			return cleanHelper(right);
		}
		else if(right.isLeaf()
				&& Util.isEqual(right.getValue(), node.getValue())) {
			return cleanHelper(left);
		}

		node.setLeft(cleanHelper(left));
		node.setRight(cleanHelper(right));

		return node;
	}
}
