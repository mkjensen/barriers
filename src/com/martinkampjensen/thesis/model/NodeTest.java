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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.martinkampjensen.thesis.Constant;
import com.martinkampjensen.thesis.model.impl.AbstractModel;
import com.martinkampjensen.thesis.util.Random;
import com.martinkampjensen.thesis.util.Util;

/**
 * This class defines tests for classes that implement the {@link Node}
 * interface.
 */
@Ignore
public abstract class NodeTest
{
	// Ids.
	private static final int LEFT_LEAF_OF_ROOT_ID = Random.nextInt();
	private static final int LEFT_LEAF_OF_LEFT_OF_RIGHT_OF_ROOT_ID =
		Random.nextInt();
	private static final int RIGHT_LEAF_OF_LEFT_OF_RIGHT_OF_ROOT_ID =
		Random.nextInt();
	private static final int RIGHT_LEAF_OF_RIGHT_OF_ROOT_ID = Random.nextInt();
	private static final int LEFT_OF_RIGHT_OF_ROOT_ID = Random.nextInt();
	private static final int RIGHT_OF_ROOT_ID = Random.nextInt();
	private static final int ROOT_ID = Random.nextInt();

	// Models.
	private static final Model LEFT_LEAF_OF_ROOT_MODEL = new NodeTestModel(99);
	private static final Model LEFT_LEAF_OF_LEFT_OF_RIGHT_OF_ROOT_MODEL =
		new NodeTestModel(20);
	private static final Model RIGHT_LEAF_OF_LEFT_OF_RIGHT_OF_ROOT_MODEL =
		new NodeTestModel(1);
	private static final Model RIGHT_LEAF_OF_RIGHT_OF_ROOT_MODEL =
		new NodeTestModel(40);
	private static final Model LEFT_OF_RIGHT_OF_ROOT_MODEL =
		new NodeTestModel(60);
	private static final Model RIGHT_OF_ROOT_MODEL = new NodeTestModel(80);
	private static final Model ROOT_MODEL = new NodeTestModel(99);
	private static Model[] _models;

	// Weights.
	protected static final int LEAF_NODE_WEIGHT = 1;
	private static final int LEFT_OF_RIGHT_OF_ROOT_WEIGHT = 2;
	private static final int RIGHT_OF_ROOT_WEIGHT = 3;
	private static final int ROOT_WEIGHT = 4;
	private static int[] _weights;

	// Nodes.
	private static Node _leftLeafOfRoot;
	private static Node _leftLeafOfLeftOfRightOfRoot;
	private static Node _rightLeafOfLeftOfRightOfRoot;
	private static Node _rightLeafOfRightOfRoot;
	private static Node _leftOfRightOfRoot;
	private static Node _rightOfRoot;
	private static Node _root;
	private static Node[] _nodes;
	private static int _nNodes;
	private static Node[] _sortedNodes; 

	@Before
	public void setUpBefore()
	{
		_models = new Model[] {
				LEFT_LEAF_OF_ROOT_MODEL,
				LEFT_LEAF_OF_LEFT_OF_RIGHT_OF_ROOT_MODEL,
				RIGHT_LEAF_OF_LEFT_OF_RIGHT_OF_ROOT_MODEL,
				RIGHT_LEAF_OF_RIGHT_OF_ROOT_MODEL,
				LEFT_OF_RIGHT_OF_ROOT_MODEL,
				RIGHT_OF_ROOT_MODEL,
				ROOT_MODEL };

		_weights = new int[] {
				LEAF_NODE_WEIGHT,
				LEAF_NODE_WEIGHT,
				LEAF_NODE_WEIGHT,
				LEAF_NODE_WEIGHT,
				LEFT_OF_RIGHT_OF_ROOT_WEIGHT,
				RIGHT_OF_ROOT_WEIGHT,
				ROOT_WEIGHT };

		// A graphical representation of the tree being working on in this
		// class. The values in parentheses are the (fixed) values of the models
		// contained by each node.
		//
		//                   _root
		//                  / (99) \
		//                 /        -----------
		//  _leftLeafOfRoot                    \
		//        (99)                          _rightOfRoot
		//                                     /    (80)    \
		//                   _leftOfRightOfRoot          _rightLeafOfRightOfRoot
		//                  /         (60)     \                   (40)
		//                 /                    \
		// _leftLeafOfLeftOfRightOfRoot          _rightLeafOfLeftOfRightOfRoot
		//            (20)                                    (1)
		//
		_leftLeafOfRoot =
			instance(LEFT_LEAF_OF_ROOT_ID, LEFT_LEAF_OF_ROOT_MODEL);
		_leftLeafOfLeftOfRightOfRoot =
			instance(LEFT_LEAF_OF_LEFT_OF_RIGHT_OF_ROOT_ID,
					LEFT_LEAF_OF_LEFT_OF_RIGHT_OF_ROOT_MODEL);
		_rightLeafOfLeftOfRightOfRoot =
			instance(RIGHT_LEAF_OF_LEFT_OF_RIGHT_OF_ROOT_ID,
					RIGHT_LEAF_OF_LEFT_OF_RIGHT_OF_ROOT_MODEL);
		_rightLeafOfRightOfRoot = instance(RIGHT_LEAF_OF_RIGHT_OF_ROOT_ID,
				RIGHT_LEAF_OF_RIGHT_OF_ROOT_MODEL);
		_leftOfRightOfRoot = instance(LEFT_OF_RIGHT_OF_ROOT_ID,
				LEFT_OF_RIGHT_OF_ROOT_MODEL, _leftLeafOfLeftOfRightOfRoot,
				_rightLeafOfLeftOfRightOfRoot);
		_rightOfRoot = instance(RIGHT_OF_ROOT_ID, RIGHT_OF_ROOT_MODEL,
				_leftOfRightOfRoot, _rightLeafOfRightOfRoot);
		_root = instance(ROOT_ID, ROOT_MODEL, _leftLeafOfRoot, _rightOfRoot);

		_nodes = new Node[] {
				_leftLeafOfRoot,
				_leftLeafOfLeftOfRightOfRoot,
				_rightLeafOfLeftOfRightOfRoot,
				_rightLeafOfRightOfRoot,
				_leftOfRightOfRoot,
				_rightOfRoot,
				_root };

		_nNodes = _nodes.length;

		_sortedNodes = new Node[] {
				_rightLeafOfLeftOfRightOfRoot,
				_leftLeafOfLeftOfRightOfRoot,
				_rightLeafOfRightOfRoot,
				_leftOfRightOfRoot,
				_rightOfRoot,
				_leftLeafOfRoot,
				_root };
	}

	@Test
	public void testToString()
	{
		for(int i = 0; i < _nNodes; i++) {
			final Node node = _nodes[i];
			final int weight = _weights[i];
			final Model model = _models[i];
			assertEquals(node.getId() + "/" + weight + "/" + model.evaluate(),
					node.toString());
		}
	}

	@Test
	public void testCompareTo()
	{
		for(int i = 0; i < _nNodes; i++) {
			final Node node = _nodes[i];

			for(int j = 0; j < _nNodes; j++) {
				final Node otherNode = _nodes[j];
				assertEquals(node.compareTo(otherNode),
						Util.compare(node.getValue(), otherNode.getValue()));
			}
		}

		Arrays.sort(_nodes);
		Assert.assertArrayEquals(_sortedNodes, _nodes);
	}

	@Test
	public void testHasParent()
	{
		assertEquals(true, _leftLeafOfRoot.hasParent());
		assertEquals(true, _leftLeafOfLeftOfRightOfRoot.hasParent());
		assertEquals(true, _rightLeafOfLeftOfRightOfRoot.hasParent());
		assertEquals(true, _rightLeafOfRightOfRoot.hasParent());
		assertEquals(true, _leftOfRightOfRoot.hasParent());		
		assertEquals(true, _rightOfRoot.hasParent());
		assertEquals(false, _root.hasParent());
	}

	@Test
	public void testIsInternal()
	{
		assertTrue(!_leftLeafOfRoot.isInternal());
		assertTrue(!_leftLeafOfLeftOfRightOfRoot.isInternal());
		assertTrue(!_rightLeafOfLeftOfRightOfRoot.isInternal());
		assertTrue(!_rightLeafOfRightOfRoot.isInternal());
		assertTrue(_leftOfRightOfRoot.isInternal());
		assertTrue(_rightOfRoot.isInternal());
		assertTrue(_root.isInternal());
	}

	@Test
	public void testIsLeaf()
	{
		assertTrue(_leftLeafOfRoot.isLeaf());
		assertTrue(_leftLeafOfLeftOfRightOfRoot.isLeaf());
		assertTrue(_rightLeafOfLeftOfRightOfRoot.isLeaf());
		assertTrue(_rightLeafOfRightOfRoot.isLeaf());
		assertTrue(!_leftOfRightOfRoot.isLeaf());
		assertTrue(!_rightOfRoot.isLeaf());
		assertTrue(!_root.isLeaf());
	}

	@Test
	public void testGetId()
	{
		assertEquals(LEFT_LEAF_OF_ROOT_ID, _leftLeafOfRoot.getId());
		assertEquals(LEFT_LEAF_OF_LEFT_OF_RIGHT_OF_ROOT_ID,
				_leftLeafOfLeftOfRightOfRoot.getId());
		assertEquals(RIGHT_LEAF_OF_LEFT_OF_RIGHT_OF_ROOT_ID,
				_rightLeafOfLeftOfRightOfRoot.getId());
		assertEquals(RIGHT_LEAF_OF_RIGHT_OF_ROOT_ID,
				_rightLeafOfRightOfRoot.getId());
		assertEquals(LEFT_OF_RIGHT_OF_ROOT_ID, _leftOfRightOfRoot.getId());
		assertEquals(RIGHT_OF_ROOT_ID, _rightOfRoot.getId());
		assertEquals(ROOT_ID, _root.getId());
	}

	@Test
	public void testGetModel()
	{
		for(int i = 0; i < _nNodes; i++) {
			final Node node = _nodes[i];
			final Model model = _models[i];
			assertEquals(model, node.getModel());
		}
	}

	@Test
	public void testGetValue()
	{
		for(int i = 0; i < _nNodes; i++) {
			final Node node = _nodes[i];
			final Model model = _models[i];
			assertEquals(model.evaluate(), node.getValue(),
					Constant.DOUBLE_PRECISION);
		}
	}

	@Test
	public void testGetWeight()
	{
		for(int i = 0; i < _nNodes; i++) {
			final Node node = _nodes[i];
			final int weight = _weights[i];

			assertEquals(weight, node.getWeight());
		}
	}

	@Test
	public void testGetLeft()
	{
		assertEquals(null, _leftLeafOfRoot.getLeft());
		assertEquals(null, _leftLeafOfLeftOfRightOfRoot.getLeft());
		assertEquals(null, _rightLeafOfLeftOfRightOfRoot.getLeft());
		assertEquals(null, _rightLeafOfRightOfRoot.getLeft());
		assertEquals(_leftLeafOfLeftOfRightOfRoot,
				_leftOfRightOfRoot.getLeft());
		assertEquals(_leftOfRightOfRoot, _rightOfRoot.getLeft());
		assertEquals(_leftLeafOfRoot, _root.getLeft());
	}

	@Test
	public void testSetLeft()
	{
		changePreconditions(_rightOfRoot);

		// Alter the tree and check that it was altered correctly.
		_rightOfRoot.setLeft(_leftLeafOfRoot);
		assertEquals(_leftLeafOfRoot, _rightOfRoot.getLeft());

		changePostconditions(_rightOfRoot, 1);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetLeftUnsupportedOperation()
	{
		_leftLeafOfRoot.setLeft(null);
	}

	@Test(expected = NullPointerException.class)
	public void testSetLeftNull()
	{
		_root.setLeft(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetLeftIllegalArgument()
	{
		_root.setLeft(_root);
	}

	@Test
	public void testGetRight()
	{
		assertEquals(null, _leftLeafOfRoot.getRight());
		assertEquals(null, _leftLeafOfLeftOfRightOfRoot.getRight());
		assertEquals(null, _rightLeafOfLeftOfRightOfRoot.getRight());
		assertEquals(null, _rightLeafOfRightOfRoot.getRight());
		assertEquals(_rightLeafOfLeftOfRightOfRoot,
				_leftOfRightOfRoot.getRight());
		assertEquals(_rightLeafOfRightOfRoot, _rightOfRoot.getRight());
		assertEquals(_rightOfRoot, _root.getRight());
	}

	@Test
	public void testSetRight()
	{
		changePreconditions(_rightOfRoot);

		// Alter the tree and check that it was altered correctly.
		_rightOfRoot.setRight(_leftOfRightOfRoot);
		assertEquals(_leftOfRightOfRoot, _rightOfRoot.getRight());

		changePostconditions(_rightOfRoot, -1);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetRightUnsupportedOperation()
	{
		_leftLeafOfRoot.setRight(null);
	}

	@Test(expected = NullPointerException.class)
	public void testSetRightNull()
	{
		_root.setRight(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetRightIllegalArgument()
	{
		_root.setRight(_root);
	}

	@Test
	public void testGetParent()
	{
		assertEquals(_root, _leftLeafOfRoot.getParent());
		assertEquals(_leftOfRightOfRoot,
				_leftLeafOfLeftOfRightOfRoot.getParent());
		assertEquals(_leftOfRightOfRoot,
				_rightLeafOfLeftOfRightOfRoot.getParent());
		assertEquals(_rightOfRoot, _rightLeafOfRightOfRoot.getParent());
		assertEquals(_rightOfRoot, _leftOfRightOfRoot.getParent());		
		assertEquals(_root, _rightOfRoot.getParent());
		assertEquals(null, _root.getParent());
	}

	@Test
	public void testSetParent()
	{
		changePreconditions(_rightOfRoot);

		// Alter the tree and check that it was altered correctly.
		_leftLeafOfRoot.setParent(_rightOfRoot);
		assertEquals(_rightOfRoot, _leftLeafOfRoot.getParent());

		// setParent does not change the weight of the parent of _rightOfRoot.
		changePostconditions(_rightOfRoot, 0);
	}

	@Test(expected = NullPointerException.class)
	public void testSetParentNull()
	{
		_root.setParent(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetParentIllegalArgument()
	{
		_root.setParent(_root);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetParentUnsupportedOperation()
	{
		_root.setParent(_leftLeafOfRoot);
	}

	@Test
	public void testClean()
	{
		assertEquals(_leftLeafOfRoot, _leftLeafOfRoot.clean());
		assertEquals(_leftLeafOfLeftOfRightOfRoot,
				_leftLeafOfLeftOfRightOfRoot.clean());
		assertEquals(_rightLeafOfLeftOfRightOfRoot,
				_rightLeafOfLeftOfRightOfRoot.clean());
		assertEquals(_rightLeafOfRightOfRoot, _rightLeafOfRightOfRoot.clean());
		assertEquals(_leftOfRightOfRoot, _leftOfRightOfRoot.clean());
		assertEquals(_rightOfRoot, _rightOfRoot.clean());
		assertEquals(_rightOfRoot, _root.clean());
	}

	protected void changePreconditions(Node node)
	{
		// Check that the weight of the node is correct before altering the
		// tree.
		assertEquals(node.getLeft().getWeight()
				+ node.getRight().getWeight(),
				node.getWeight());

		// Check that the weight of the parent of the node is correct before
		// altering the tree.
		assertEquals(node.getParent().getLeft().getWeight()
				+ node.getParent().getRight().getWeight(),
				node.getParent().getWeight());
	}

	protected void changePostconditions(Node node, int deltaWeight)
	{
		// Check that the weight of the node has been be changed correctly.
		assertEquals(node.getLeft().getWeight() + node.getRight().getWeight(),
				node.getWeight());

		// Check that the weight of the parent of the node has not been changed
		// after altering the tree.
		assertEquals(node.getParent().getLeft().getWeight()
				+ node.getParent().getRight().getWeight() + deltaWeight,
				node.getParent().getWeight());

		// Calculate the weight of the parent of the node and check that it has
		// been changed correctly.
		node.getParent().calculateWeight();
		assertEquals(node.getParent().getLeft().getWeight() +
				node.getParent().getRight().getWeight(),
				node.getParent().getWeight());
	}

	/**
	 * Constructs and returns a new leaf node instance.
	 * 
	 * @param id the id assigned to the node.
	 * @param model the model contained in the node.
	 * @return a new leaf node instance.
	 */
	protected abstract Node instance(int id, Model model);

	/**
	 * Constructs and returns a new internal node instance.
	 * 
	 * @param id the id assigned to the node.
	 * @param model the model contained in the node.
	 * @param left the left child.
	 * @param right the right child.
	 * @return a new internal node instance.
	 */
	protected abstract Node instance(int id, Model model, Node left,
			Node right);

	protected static final class NodeTestModel extends AbstractModel
	{
		static final int _size = 1;
		static final int _angleId = 0;
		static int _nextValue = Integer.MIN_VALUE;

		public NodeTestModel()
		{
			this(_nextValue++);
		}

		public NodeTestModel(int value)
		{
			super(_size);
			setAngle(_angleId, value);
		}

		@Override
		protected double calculateFitness()
		{
			return getAngle(_angleId);
		}
	}
}
