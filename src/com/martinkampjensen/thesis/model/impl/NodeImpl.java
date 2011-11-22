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

import java.util.ArrayList;
import java.util.List;

import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.model.Node;

/**
 * An implementation of the {@link Node} interface.
 */
public final class NodeImpl extends AbstractNode
{
	private static final long serialVersionUID = 72481224910059548L;
	private final int _id;
	private final Model _model;
	private int _weight;
	private Node _left;
	private Node _right;
	private Node _parent;
	private List<Model> _additionalModels;

	/**
	 * Constructs a new leaf node with a weight of <code>1</code>.
	 * 
	 * @param id the id assigned to the node.
	 * @param model the {@link Model} the node will contain.
	 * @throws NullPointerException if <code>model</code> is <code>null</code>.
	 */
	public NodeImpl(int id, Model model)
	{
		if(model == null) {
			throw new NullPointerException("model == null");
		}

		_id = id;
		_model = model;
		_weight = 1;
	}

	/**
	 * Constructs a new internal node with two children.
	 * <p>
	 * The weight of the new node will be set to the sum of the weights of the
	 * two children. The node will be set as the parent of the children.
	 * 
	 * @param id the id assigned to the node.
	 * @param model the {@link Model} the node will contain.
	 * @param left the left child.
	 * @param right the right child.
	 * @throws NullPointerException if <code>model</code>, <code>left</code> or
	 * <code>right</code> is <code>null</code>.
	 * @throws IllegalArgumentException if <code>left == right</code>.
	 */
	public NodeImpl(int id, Model model, Node left, Node right)
	{
		if(model == null) {
			throw new NullPointerException("model == null");
		}
		else if(left == null) {
			throw new NullPointerException("left == null");
		}
		else if(right == null) {
			throw new NullPointerException("right == null");
		}
		else if(left == right) {
			throw new IllegalArgumentException("first == second");
		}

		_id = id;
		_model = model;
		_left = left;
		_right = right;
		updateWeight();

		_left.setParent(this);
		_right.setParent(this);
	}

	@Override
	public boolean hasParent()
	{
		return _parent != null;
	}

	@Override
	public boolean isInternal()
	{
		return _left != null;
	}

	@Override
	public boolean isLeaf()
	{
		return _left == null;
	}

	@Override
	public int getId()
	{
		return _id;
	}

	@Override
	public Model getModel()
	{
		return _model;
	}

	@Override
	public int getWeight()
	{
		return _weight;
	}

	@Override
	public Node getLeft()
	{
		return _left;
	}

	/**
	 * @throws UnsupportedOperationException {@inheritDoc}
	 * @throws NullPointerException {@inheritDoc}
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	public void setLeft(Node left)
	{
		if(this.isLeaf()) {
			throw new UnsupportedOperationException("this.isLeaf() == true");
		}
		else if(left == null) {
			throw new NullPointerException("left == null");
		}
		else if(left == this) {
			throw new IllegalArgumentException("left == this");
		}

		_left = left;
		updateWeight();
	}

	@Override
	public Node getRight()
	{
		return _right;
	}

	/**
	 * @throws UnsupportedOperationException {@inheritDoc}
	 * @throws NullPointerException {@inheritDoc}
	 * @throws IllegalArgumentException {@inheritDoc} 
	 */
	@Override
	public void setRight(Node right)
	{
		if(this.isLeaf()) {
			throw new UnsupportedOperationException("this.isLeaf() == true");
		}
		else if(right == null) {
			throw new NullPointerException("right == null");
		}
		else if(right == this) {
			throw new IllegalArgumentException("right == this");
		}

		_right = right;
		updateWeight();
	}

	@Override
	public Node getParent()
	{
		return _parent;
	}

	/**
	 * @throws NullPointerException {@inheritDoc}
	 * @throws IllegalArgumentException {@inheritDoc}
	 * @throws UnsupportedOperationException {@inheritDoc}
	 */
	@Override
	public void setParent(Node parent)
	{
		if(parent == null) {
			throw new NullPointerException("parent == null");
		}
		if(parent == this) {
			throw new IllegalArgumentException("parent == this");
		}
		else if(parent.isLeaf()) {
			throw new UnsupportedOperationException("parent.isLeaf() == true");
		}

		_parent = parent;
	}

	@Override
	public void calculateWeight()
	{
		// TODO: Iterative implementation.
		if(isInternal()) {
			_left.calculateWeight();
			_right.calculateWeight();
			updateWeight();
		}
	}

	@Override
	public boolean hasAdditionalModels()
	{
		return _additionalModels != null && _additionalModels.size() > 0;
	}

	@Override
	public int getAdditionalModelsCount()
	{
		if(_additionalModels == null) {
			return 0;
		}
		else {
			return _additionalModels.size();
		}
	}

	public List<Model> getAdditionalModels()
	{
		if(_additionalModels == null) {
			return null;
		}
		else {
			return new ArrayList<Model>(_additionalModels);
		}
	}

	@Override
	public void addAdditionalModel(Model model)
	{
		if(_additionalModels == null) {
			_additionalModels = new ArrayList<Model>();
		}

		_additionalModels.add(model);
	}

	private void updateWeight()
	{
		_weight = _left.getWeight() + _right.getWeight();
	}
}
