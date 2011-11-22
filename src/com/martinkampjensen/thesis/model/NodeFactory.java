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

package com.martinkampjensen.thesis.model;

import com.martinkampjensen.thesis.model.impl.NodeImpl;

/**
 * TODO: Document {@link NodeFactory}.
 */
public final class NodeFactory
{
	private static int _nextId;

	private NodeFactory()
	{
	}

	/**
	 * Resets the id assignment so that the next {@link Node} constructed will
	 * be assigned id <code>0</code>. Note that this means that {@link Node}
	 * objects with the same id can exist.
	 */
	public static void reset()
	{
		_nextId = 0;
	}

	/**
	 * Constructs a new leaf {@link Node} with a weight of <code>1</code>.
	 * 
	 * @param model the {@link Model} the node will contain.
	 * @throws NullPointerException if <code>model</code> is <code>null</code>.
	 */
	public static Node create(Model model)
	{
		return new NodeImpl(_nextId++, model);
	}

	/**
	 * Constructs a new internal {@link Node} with two children.
	 * <p>
	 * The weight of the new node will be set to the sum of the weights of the
	 * two children. The node will be set as the parent of the children.
	 * 
	 * @param model the {@link Model} the node will contain.
	 * @param first the first child.
	 * @param second the second child.
	 * @throws NullPointerException if <code>model</code>, <code>first</code> or
	 * <code>second</code> is <code>null</code>.
	 * @throws IllegalArgumentException if <code>first == second</code>.
	 */
	public static Node create(Model model, Node first, Node second)
	{
		return new NodeImpl(_nextId++, model, first, second);
	}
}
