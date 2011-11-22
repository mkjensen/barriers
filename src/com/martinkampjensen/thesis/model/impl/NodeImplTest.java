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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.martinkampjensen.thesis.Constant;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.model.Node;
import com.martinkampjensen.thesis.model.NodeTest;
import com.martinkampjensen.thesis.util.Random;

/**
 * This class defines tests of the {@link NodeImpl} class.
 */
public final class NodeImplTest extends NodeTest
{
	private static final int LEFT_NODE_ID = Random.nextInt();
	private static final int RIGHT_NODE_ID = Random.nextInt();
	private static final Model MODEL = new NodeTestModel();
	private static Node LEFT_NODE = new NodeImpl(LEFT_NODE_ID, MODEL);
	private static Node RIGHT_NODE = new NodeImpl(RIGHT_NODE_ID, MODEL);

	@Override
	protected Node instance(int id, Model model)
	{
		return new NodeImpl(id, model);
	}

	@Override
	protected Node instance(int id, Model model, Node left, Node right)
	{
		return new NodeImpl(id, model, left, right);
	}

	@Test
	public void testNodeImplIntModel()
	{
		final int id = Random.nextInt();
		final int weight = LEAF_NODE_WEIGHT;
		final Node node = new NodeImpl(id, MODEL);

		assertEquals(false, node.hasParent());
		assertEquals(false, node.isInternal());
		assertEquals(true, node.isLeaf());
		assertEquals(id, node.getId());
		assertEquals(MODEL, node.getModel());
		assertEquals(MODEL.evaluate(), node.getValue(),
				Constant.DOUBLE_PRECISION);
		assertEquals(weight, node.getWeight());
		assertEquals(null, node.getLeft());
		assertEquals(null, node.getRight());
		assertEquals(null, node.getParent());
		assertEquals(node, node.clean());
	}

	@Test(expected = NullPointerException.class)
	public void testNodeImplIntNull()
	{
		new NodeImpl(0, null);
	}

	@Test
	public void testNodeImplIntModelNodeNode()
	{
		final int id = Random.nextInt();
		final int weight = 2;
		final Node node = new NodeImpl(id, MODEL, LEFT_NODE, RIGHT_NODE);

		assertEquals(false, node.hasParent());
		assertEquals(true, node.isInternal());
		assertEquals(false, node.isLeaf());
		assertEquals(id, node.getId());
		assertEquals(MODEL, node.getModel());
		assertEquals(MODEL.evaluate(), node.getValue(),
				Constant.DOUBLE_PRECISION);
		assertEquals(weight, node.getWeight());
		assertEquals(LEFT_NODE, node.getLeft());
		assertEquals(RIGHT_NODE, node.getRight());
		assertEquals(null, node.getParent());
		assertEquals(RIGHT_NODE, node.clean());

		assertEquals(node, LEFT_NODE.getParent());
		assertEquals(node, RIGHT_NODE.getParent());
	}

	@Test(expected = NullPointerException.class)
	public void testNodeImplIntNullNodeNode()
	{
		new NodeImpl(0, null, LEFT_NODE, RIGHT_NODE);
	}

	@Test(expected = NullPointerException.class)
	public void testNodeImplIntModelNullNode()
	{
		new NodeImpl(0, MODEL, null, RIGHT_NODE);
	}

	@Test(expected = NullPointerException.class)
	public void testNodeImplIntModelNodeNull()
	{
		new NodeImpl(0, MODEL, LEFT_NODE, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNodeImplIntModelNodeNodeIllegalArgument()
	{
		new NodeImpl(0, MODEL, LEFT_NODE, LEFT_NODE);
	}
}
