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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.martinkampjensen.thesis.barriers.neighborhood.Neighborhood;
import com.martinkampjensen.thesis.model.BarrierForest;
import com.martinkampjensen.thesis.model.BarrierTree;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.model.Node;
import com.martinkampjensen.thesis.util.Debug;
import com.martinkampjensen.thesis.util.Util;

/**
 * TODO: Document {@link BarrierForestImpl}.
 */
public final class BarrierForestImpl implements BarrierForest
{
	private static final long serialVersionUID = -8231812914581372432L;
	private final BarrierTree[] _trees;
	private final int _modelsUsed;
	private final double _pruningThreshold;
	private final double _neighborTreshold;
	private final Neighborhood _neighborhood;
	private final boolean _allowDebugPrints;
	private boolean _measuresCalculated;
	private int _leaves;
	private Node _min;
	private Node _minBarrier;
	private Node _maxBarrier;
	private double _totalBarrierValue;
	private double _totalConnectionValue;

	/**
	 * Constructs a new {@link BarrierForest} with the given {@link BarrierTree}
	 * as its only tree.
	 * 
	 * @param tree the barrier tree to be contained in the barrier forest.
	 * @param modelsUsed the number of models used to create this forest.
	 * @throws NullPointerException if <code>tree</code> is
	 *         <code>null</code>.
	 * @throws IllegalArgumentException if <code>modelsUsed &lt; 0</code>.
	 */
	public BarrierForestImpl(BarrierTree tree, int modelsUsed)
	{
		this(tree, modelsUsed, -1d, -1d, null, true);
	}

	public BarrierForestImpl(BarrierTree tree, int modelsUsed,
			double pruningThreshold, double neighborThreshold,
			Neighborhood neighborhood, boolean allowDebugPrints)
	{
		if(tree == null) {
			throw new NullPointerException("tree == null");
		}
		else if(modelsUsed < 0) {
			throw new IllegalArgumentException("modelsUsed < 0");
		}

		_trees = new BarrierTree[] { tree };
		_modelsUsed = modelsUsed;
		_pruningThreshold = pruningThreshold;
		_neighborTreshold = neighborThreshold;
		_neighborhood = neighborhood;
		_allowDebugPrints = allowDebugPrints;
	}

	/**
	 * Constructs a new {@link BarrierForest} containing the specified
	 * {@link BarrierTree}s.
	 * 
	 * @param trees the barrier trees to be contained in the barrier forest.
	 * @param modelsUsed the number of models used to create this forest.
	 * @throws NullPointerException if <code>trees</code> is
	 *         <code>null</code> or if any element in <code>trees</code> is
	 *         <code>null</code>.
	 * @throws IllegalArgumentException if <code>modelsUsed &lt; 0</code>.
	 */
	public BarrierForestImpl(BarrierTree[] trees, int modelsUsed)
	{
		this(trees, modelsUsed, -1d, -1d, null, true);
	}

	public BarrierForestImpl(BarrierTree[] trees, int modelsUsed,
			double pruningThreshold, double neighborThreshold,
			Neighborhood neighborhood, boolean allowDebugPrints)
	{
		if(trees == null) {
			throw new NullPointerException("trees == null");
		}
		else if(modelsUsed < 0) {
			throw new IllegalArgumentException("modelsUsed < 0");
		}

		for(int i = 0; i < trees.length; i++) {
			if(trees[i] == null) {
				throw new NullPointerException("trees[" + i + "] == null");
			}
		}

		_trees = Arrays.copyOf(trees, trees.length);
		_modelsUsed = modelsUsed;
		_pruningThreshold = pruningThreshold;
		_neighborTreshold = neighborThreshold;
		_neighborhood = neighborhood;
		_allowDebugPrints = allowDebugPrints;
	}

	@Override
	public void calculateMeasures()
	{
		if(_measuresCalculated) {
			return;
		}

		final int size = getNumberOfTrees();
		final BarrierTree firstTree = _trees[0];

		_leaves = firstTree.getNumberOfLeaves();
		_min = firstTree.getMinimum();
		_minBarrier = firstTree.getMinimumBarrier();
		_maxBarrier = firstTree.getMaximumBarrier();
		_totalBarrierValue = firstTree.getTotalBarrierValue();

		for(int i = 1; i < size; i++) {
			final BarrierTree tree = _trees[i];

			final int treeLeaves = tree.getNumberOfLeaves();
			_leaves += treeLeaves;

			final Node treeMin = tree.getMinimum();
			if(Util.isLess(treeMin.getValue(), _min.getValue())) {
				_min = tree.getMinimum();
			}

			final Node treeMinBarrier = tree.getMinimumBarrier();
			if(Util.isLess(treeMinBarrier.getValue(), _minBarrier.getValue())) {
				_minBarrier = treeMinBarrier;
			}

			final Node treeMaxBarrier = tree.getMaximumBarrier();
			if(Util.isLess(_maxBarrier.getValue(), treeMaxBarrier.getValue())) {
				_maxBarrier = treeMaxBarrier;
			}

			final double treeTotalBarrierValue = tree.getTotalBarrierValue();
			_totalBarrierValue += treeTotalBarrierValue;
		}

		// Set to true now to avoid the recalculateMeasures method entering an
		// infinite loop because it calls the getMinimumValue method of this
		// class.
		_measuresCalculated = true;

		_totalConnectionValue = 0d;

		for(int i = 0; i < size; i++) {
			final BarrierTree tree = _trees[i];

			// Note that the total connection value of each tree depends on
			// the minimum value of the forest. Hence, individual trees must
			// recalculate the total connection value after the minimum value of
			// the forest has been calculated above.
			tree.recalculateMeasures(this);
			_totalConnectionValue += tree.getTotalConnectionValue();
		}

		if(_allowDebugPrints) {
			Debug.line("[BarrierForest] trees: %d, leaves: %d, minValue: %f, "
					+ "minBarrierValue: %f, maxBarrierValue: %f, "
					+ "totalBarrierValue: %f, totalConnectionValue: %f",
					_trees.length, _leaves, _min.getValue(),
					_minBarrier.getValue(), _maxBarrier.getValue(),
					_totalBarrierValue, _totalConnectionValue);
		}
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
	public int modelsUsed()
	{
		return _modelsUsed;
	}

	@Override
	public double getPruningThreshold()
	{
		return _pruningThreshold;
	}

	@Override
	public double getNeighborThreshold()
	{
		return _neighborTreshold;
	}

	@Override
	public Neighborhood getNeighborhood()
	{
		return _neighborhood;
	}

	@Override
	public int getNumberOfTrees()
	{
		return _trees.length;
	}

	@Override
	public BarrierTree getTree(int id)
	{
		return _trees[id];
	}

	@Override
	public Node find(int id)
	{
		final int nTrees = getNumberOfTrees();

		for(int i = 0; i < nTrees; i++) {
			final Node node = _trees[i].find(id);

			if(node != null) {
				return node;
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation traverses all trees in the forest to find the tree
	 * that contains the two nodes. Then the path from the root of the tree to
	 * both nodes are found. This means that the barrier separating the two
	 * nodes can then be found. Finally, the models (and additional models) on
	 * the path is found and returned.
	 * 
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	public List<Model> findConnectingModels(int fromId, int toId)
	{
		if(fromId == toId) {
			throw new IllegalArgumentException("fromId == toId");
		}

		final int nTrees = getNumberOfTrees();
		Node fromNode = null;
		Node toNode = null;

		for(int i = 0; i < nTrees; i++) {
			final BarrierTree tree = _trees[i];
			fromNode = tree.find(fromId);

			if(fromNode != null) {
				toNode = tree.find(toId);
				break;
			}
		}

		if(fromNode == null || toNode == null) {
			throw new IllegalArgumentException(
			"fromId and toId not found in the same BarrierTree");
		}

		final List<Model> models = shortestPath(fromNode, toNode);
		Debug.line("Conformations in trajectory: %d", models.size());

		return models;
	}

	/**
	 * Finds and returns the splitting node, that is, the lowest node that the
	 * paths from the root to <code>from</code> and <code>to</code>,
	 * respectively, share.
	 * 
	 * @param from the first node.
	 * @param to the second node.
	 * @return the splitting node.
	 */
	private static Node findSplitNode(Node from, Node to)
	{
		final List<Node> fromNodes = fromRootToNode(from);
		final List<Node> toNodes = fromRootToNode(to);
		List<Node> shortestList;
		Node splitNode;

		if(fromNodes.size() < toNodes.size()) {
			shortestList = fromNodes;
		}
		else {
			shortestList = toNodes;
		}

		for(int depth = 0, smallestSize = shortestList.size();; depth++) {
			if(depth == smallestSize) {
				// from and to nodes are on the same path, i.e., one of them or
				// both is a barrier.
				splitNode = shortestList.get(smallestSize - 1);
				break;
			}
			else if(fromNodes.get(depth) != toNodes.get(depth)) {
				splitNode = fromNodes.get(depth - 1);
				break;
			}
		}

		return splitNode;
	}

	/**
	 * Returns a list of nodes from the root node to a specific node, inclusive.
	 * 
	 * @param node the specific node.
	 * @return the list of nodes.
	 * @throws NullPointerException if <code>node == null</code>. 
	 */
	private static List<Node> fromRootToNode(Node node)
	{
		if(node == null) {
			throw new NullPointerException("node == null");
		}

		final List<Node> nodes = new ArrayList<Node>();

		do {
			nodes.add(node);
			node = node.getParent();
		} while(node != null);

		Collections.reverse(nodes);

		return nodes;
	}

	/**
	 * Finds and returns a list of all nodes in a tree rooted at
	 * <code>root</code>.
	 * 
	 * @param root the root.
	 * @return all nodes in the tree.
	 */
	private static List<Node> findNodesInTree(Node root)
	{
		final Deque<Node> stack = new ArrayDeque<Node>();
		final List<Node> nodes = new ArrayList<Node>();
		stack.addFirst(root);

		while(!stack.isEmpty()) {
			Node node = stack.removeFirst();
			nodes.add(node);

			if(node.isInternal()) {
				stack.addFirst(node.getRight());
				stack.addFirst(node.getLeft());
			}
		}

		return nodes;
	}

	/**
	 * Calculates and returns a list of models that is a shortest path between
	 * <code>source</code> and <code>destination</code> by navigating between
	 * models that are neighbors according to the neighborhood definition of
	 * this forest.
	 * 
	 * @param source the source node (model).
	 * @param destination the destination node (model.
	 * @return the shortest path.
	 */
	private List<Model> shortestPath(Node source, Node destination)
	{
		final Node splitNode = findSplitNode(source, destination);
		final List<Node> nodes = findNodesInTree(splitNode);
		final List<Model> models = new ArrayList<Model>();
		final Map<Model, Node> modelToNode = new HashMap<Model, Node>();

		// Extract models from nodes. The shortest path is to be found through
		// the models.
		for(int i = 0, n = nodes.size(); i < n; i++) {
			final Node node = nodes.get(i);
			final Model model = node.getModel();
			models.add(model);
			modelToNode.put(model, node);

			if(node != splitNode && node.hasAdditionalModels()) {
				final List<Model> additionals = node.getAdditionalModels();

				for(int j = 0, m = additionals.size(); j < m; j++) {
					final Model additional = additionals.get(j);
					models.add(additional);
					modelToNode.put(additional, node);
				}
			}
		}

		// Create neighborhood structures.
		final int nModels = models.size();
		final int hashMapCapacity = (int)Math.ceil(nModels / 0.75) + 1;
		final int[][] neighborIds =
			_neighborhood.calculateNeighbors(models, _neighborTreshold, true);
		final Map<Model, Model[]> neighbors = new HashMap<Model, Model[]>(
				hashMapCapacity);

		for(int i = 0; i < nModels; i++) {
			final int[] neighborIdsI = neighborIds[i];
			final int nNeighbors = neighborIdsI.length;
			final Model[] neighborsI = new Model[nNeighbors];

			for(int j = 0; j < nNeighbors; j++) {
				neighborsI[j] = models.get(neighborIdsI[j]);
			}

			neighbors.put(models.get(i), neighborsI);
		}

		final ArrayDeque<Model> queue = new ArrayDeque<Model>();
		final Set<Model> visited = new HashSet<Model>(hashMapCapacity);
		final Map<Model, Model> previous = new HashMap<Model, Model>();
		final Model srcModel = source.getModel();
		final Model dstModel = destination.getModel();

		queue.add(srcModel);
		visited.add(srcModel);
		Model current = null;

		// Perform a variation of Dijkstra's algorithm. 
		while(!queue.isEmpty()) {
			current = queue.poll();

			if(current == dstModel) {
				break;
			}

			final Model[] curNeighbors = neighbors.get(current);

			for(int i = 0, n = curNeighbors.length; i < n; i++) {
				final Model next = curNeighbors[i];

				if(!visited.contains(next)) {
					queue.add(next);
					visited.add(next);
					previous.put(next, current);
				}
			}
		}

		// Sanity check.
		if(current != dstModel) {
			throw new IllegalStateException(
					"Could not connect " + source.getId() + " and " + destination.getId());
		}

		// Create the shortest path using the previous pointers.
		final List<Model> path = new ArrayList<Model>();
		for(Model m = dstModel; m != null; m = previous.get(m)) {
			path.add(m);
		}
		Collections.reverse(path);

		// TODO: Remove debug.
		Debug.line("Splitting barrier: %d(%d)", splitNode.getId(),
				splitNode.getModel().getId());
		String debug = "Path:";
		for(int i = 0, n = path.size(); i < n; i++) {
			final Model model = path.get(i);
			debug += String.format(" %d(%d)", modelToNode.get(model).getId(),
					model.getId());
		}
		Debug.line(debug);

		return path;
	}
}
