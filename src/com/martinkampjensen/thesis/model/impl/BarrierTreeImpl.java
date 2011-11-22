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

import java.util.ArrayDeque;
import java.util.Deque;

import com.martinkampjensen.thesis.model.BarrierForest;
import com.martinkampjensen.thesis.model.BarrierTree;
import com.martinkampjensen.thesis.model.Node;
import com.martinkampjensen.thesis.util.Debug;
import com.martinkampjensen.thesis.util.Util;

/**
 * TODO: Document {@link BarrierTreeImpl}.
 */
public final class BarrierTreeImpl implements BarrierTree
{
	private static final long serialVersionUID = 1195454365783421859L;
	private final Node _root;
	private final boolean _allowDebugPrints;
	private boolean _measuresCalculated;
	private int _leaves;
	private Node _min;
	private Node _minBarrier;
	private Node _maxBarrier;
	private double _totalBarrierValue;
	private double _totalConnectionValue;

	/**
	 * Constructs a new {@link BarrierTree} with the given {@link Node} as its
	 * root.
	 * 
	 * @param root the root of the tree.
	 * @param allowDebugPrints whether or not debug prints are allowed.
	 * @throws NullPointerException if <code>root</code> is <code>null</code>.
	 */
	public BarrierTreeImpl(Node root, boolean allowDebugPrints)
	{
		if(root == null) {
			throw new NullPointerException("root == null");
		}

		_root = root;
		_allowDebugPrints = allowDebugPrints;
	}

	@Override
	public void calculateMeasures()
	{
		if(_measuresCalculated) {
			return;
		}

		// TODO: Measures are not calculated correctly if _root is a leaf.

		_leaves = findNumberOfLeaves(_root);
		_min = findMinimum(_root);
		_minBarrier = findMinimumBarrier(_root);
		_maxBarrier = _root;
		_totalBarrierValue = findTotalBarrierValue(_root);
		_totalConnectionValue =
			findTotalConnectionValue(_root, _min.getValue());

		_measuresCalculated = true;

		if(_allowDebugPrints) {
			Debug.line("[BarrierTree] leaves: %d, minValue: %f, "
					+ "minBarrierValue: %f, maxBarrierValue: %f, "
					+ "totalBarrierValue: %f, totalConnectionValue: %f",
					_leaves, _min.getValue(), _minBarrier.getValue(),
					_maxBarrier.getValue(), _totalBarrierValue,
					_totalConnectionValue);
		}

		// TODO: Remove this check when confident that weights have been set correctly.
		if(_root.getWeight() != _leaves) {
			throw new IllegalStateException("_root() != _leaves");
		}
	}

	@Override
	public void recalculateMeasures(BarrierForest forest)
	{
		final double minValue = forest.getMinimumValue();
		_totalConnectionValue = findTotalConnectionValue(_root, minValue);
	}

	@Override
	public int getNumberOfLeaves()
	{
		calculateMeasures();
		return _leaves;
	}

	@Override
	public double getMinimumValue()
	{
		return getMinimum().getValue();
	}

	@Override
	public double getMinimumBarrierValue()
	{
		return getMinimumBarrier().getValue();
	}

	@Override
	public double getMaximumBarrierValue()
	{
		return getMaximumBarrier().getValue();
	}

	@Override
	public double getTotalBarrierValue()
	{
		calculateMeasures();
		return _totalBarrierValue;
	}

	@Override
	public double getTotalConnectionValue()
	{
		calculateMeasures();
		return _totalConnectionValue;
	}

	@Override
	public Node getMinimum()
	{
		calculateMeasures();
		return _min;
	}

	@Override
	public Node getMinimumBarrier()
	{
		calculateMeasures();
		return _minBarrier;
	}

	@Override
	public Node getMaximumBarrier()
	{
		calculateMeasures();
		return _maxBarrier;
	}

	@Override
	public Node getRoot()
	{
		return _root;
	}

	@Override
	public Node find(int id)
	{
		final Deque<Node> stack = new ArrayDeque<Node>();
		stack.addFirst(_root);

		while(!stack.isEmpty()) {
			final Node node = stack.removeFirst();

			if(node.getId() == id) {
				return node;
			}

			if(node.isInternal()) {
				stack.addFirst(node.getRight());
				stack.addFirst(node.getLeft());
			}
		}

		return null;
	}

	private static int findNumberOfLeaves(Node root)
	{
		final Deque<Node> stack = new ArrayDeque<Node>();
		int count = 0;
		stack.addFirst(root);

		while(!stack.isEmpty()) {
			Node node = stack.removeFirst();
			Node left = node.getLeft();

			while(left != null) {
				stack.addFirst(node);
				node = left;
				left = node.getLeft();
			}

			count++;

			if(!stack.isEmpty()) {
				node = stack.removeFirst();
				stack.addFirst(node.getRight());
			}
		}

		return count;
	}

	private static Node findMinimum(Node root)
	{
		final Deque<Node> stack = new ArrayDeque<Node>();
		Node minNode = root;
		stack.addFirst(root);

		while(!stack.isEmpty()) {
			Node node = stack.removeFirst();
			Node left = node.getLeft();

			// Go as far to the left as possible.
			while(left != null) {
				stack.addFirst(node);
				node = left;
				left = node.getLeft();
			}

			if(Util.isLess(node.getValue(), minNode.getValue())) {
				minNode = node;
			}

			// We are as far left as possible and that node does not have a
			// right child since it is a leaf. Go one step up and one step to
			// the right to continue.
			if(!stack.isEmpty()) {
				node = stack.removeFirst();
				stack.addFirst(node.getRight());
			}
		}

		return minNode;
	}

	private static Node findMinimumBarrier(Node root)
	{
		final Node[] minNode = new Node[] { root };
		findMinimumBarrier(root, minNode);
		return minNode[0];
	}

	private static void findMinimumBarrier(Node root, Node[] minNode)
	{
		// TODO: Iterative implementation.
		if(root.isLeaf()) {
			return;
		}

		findMinimumBarrier(root.getLeft(), minNode);

		if(Util.isLess(root.getValue(), minNode[0].getValue())) {
			minNode[0] = root;
		}

		findMinimumBarrier(root.getRight(), minNode);
	}

	private static double findTotalBarrierValue(Node root)
	{
		// TODO: Iterative implementation.
		if(root.isLeaf()) {
			return 0d;
		}

		final double leftValue = findTotalBarrierValue(root.getLeft());
		final double rightValue = findTotalBarrierValue(root.getRight());
		final double rootValue = root.getValue();

		return leftValue + rightValue + rootValue;
	}

	private static double findTotalConnectionValue(Node root, double minValue)
	{
		// TODO: Iterative implementation.
		if(root.isLeaf()) {
			return 0d;
		}

		final Node left = root.getLeft();
		final double leftConnectionValue =
			findTotalConnectionValue(left, minValue);
		final int leftWeight = left.getWeight();

		final Node right = root.getRight();
		final double rightConnectionValue =
			findTotalConnectionValue(right, minValue);
		final int rightWeight = right.getWeight();

		final double rootValue = root.getValue();
		final double rootConnectionValue =
			(rootValue - minValue) * leftWeight * rightWeight;

		final double totalConnectionValue =
			leftConnectionValue + rightConnectionValue + rootConnectionValue;

		return totalConnectionValue;
	}
}
