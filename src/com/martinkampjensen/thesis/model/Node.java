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

import java.io.Serializable;
import java.util.List;

import com.martinkampjensen.thesis.Constant;

/**
 * A {@link Node} represents a node in a full binary tree where each node
 * contains a {@link Model}, that is, a node is either a leaf node or an
 * internal node with exactly two children. Root nodes does not have parent
 * nodes.
 * <p>
 * The weight of a node is defined to be <code>1</code> for leaves, and the
 * number of leaves in the subtrees for internal nodes.
 * <p>
 * A node is an extension of the {@link Point} interface to enable a node to be
 * assigned a position in 2D space for possible visualisation.
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Binary_tree">Binary tree</a>
 */
public interface Node extends Comparable<Node>, Point, Serializable
{
	/**
	 * Returns a textural representation of this node of the from
	 * "id/weight/value."
	 */
	@Override
	String toString();

	/**
	 * For two nodes <code>a</code> and <code>b</code>, <code>a &gt; b</code>
	 * if and only if <code>(a.</code>{@link #getValue()}
	 * <code>- b.</code>{@link #getValue()}<code>) &gt;</code>
	 * {@link Constant#DOUBLE_PRECISION}. When
	 * <code>Math.abs(a.</code>{@link #getValue()} <code> -
	 * b.</code>{@link #getValue()}<code>) &le;</code>
	 * {@link Constant#DOUBLE_PRECISION}, <code>a</code> and <code>b</code> are
	 * equal.
	 * <p>
	 * {@inheritDoc}
	 * @see Math#abs(double)
	 */
	@Override
	int compareTo(Node other);

	/**
	 * Returns whether or not this node has a parent.
	 * 
	 * @return <code>true</code> if this node has a parent, <code>false</code>
	 *         otherwise.
	 */
	boolean hasParent();

	/**
	 * Returns whether or not this node is an internal node.
	 * 
	 * @return <code>true</code> if this node is an internal node,
	 *         <code>false</code> otherwise.
	 */
	boolean isInternal();

	/**
	 * Returns whether or not this node is a leaf node.
	 * 
	 * @return <code>true</code> if this node is a leaf node, <code>false</code>
	 *         otherwise.
	 */
	boolean isLeaf();

	/**
	 * Returns the id that this node was assigned when it was constructed.
	 * 
	 * @return the id of this node.
	 */
	int getId();

	/**
	 * Returns the {@link Model} contained by this node.
	 * 
	 * @return the model contained by this node.
	 */
	Model getModel();

	/**
	 * Returns the value contained by this node, that is, the return value of
	 * the {@link Model#evaluate()} method of the {@link Model} contained by
	 * this node.
	 * 
	 * @return the value contained by this node.
	 */
	double getValue();

	/**
	 * Returns the weight of this node.
	 * <p>
	 * For internal nodes, the weight is defined to be the number of leaves in
	 * the subtrees of the nodes. For leaf nodes, the weight is defined to be
	 * <code>1</code>.
	 * <p>
	 * Note that the value returned may be an old and incorrect value if the
	 * children of this node have changed since the last time the
	 * {@link #calculateWeight()} method was called.
	 * 
	 * @return the weight of this node.
	 */
	int getWeight();

	/**
	 * Returns the left child of this node.
	 * 
	 * @return the left child or <code>null</code> if this node is a leaf node.
	 */
	Node getLeft();

	/**
	 * Sets the left child of this node and updates the weight of this node
	 * based on the weight of the child. {@link #calculateWeight()} is not
	 * called on the child.  
	 * <p>
	 * Note that if this node has a parent, the weight of the parent is not
	 * updated until {@link #calculateWeight()} is called on it.
	 * <p>
	 * Note that this node is not set as the parent of the child node unless the
	 * {@link #setParent(Node)} method is used.
	 * 
	 * @param left the node to assign as the left child.
	 * @throws UnsupportedOperationException if this node is a leaf.
	 * @throws NullPointerException if <code>left</code> is <code>null</code>.
	 * @throws IllegalArgumentException if <code>left</code> is the same node
	 *         as this node.
	 */
	void setLeft(Node left);

	/**
	 * Returns the right child of this node.
	 * 
	 * @return the right child or <code>null</code> if this node is a leaf node.
	 */
	Node getRight();

	/**
	 * Sets the right child of this node and updates the weight of this node
	 * based on the weight of the child. {@link #calculateWeight()} is not
	 * called on the child.  
	 * <p>
	 * Note that if this node has a parent, the weight of the parent is not
	 * updated until {@link #calculateWeight()} is called on it.
	 * <p>
	 * Note that this node is not set as the parent of the child node unless the
	 * {@link #setParent(Node)} method is used.
	 * 
	 * @throws NullPointerException if <code>right</code> is <code>null</code>.
	 * @throws IllegalArgumentException if <code>right</code> is the same node
	 *         as this node.
	 * @throws UnsupportedOperationException if <code>parent</code> is a leaf.
	 */
	void setRight(Node right);

	/**
	 * Returns the parent of this node.
	 * 
	 * @return the parent node or <code>null</code> if this node has no parent
	 *         node.
	 */
	Node getParent();

	/**
	 * Sets the parent of this node.
	 * <p>
	 * Note that this node will not be set as the (left or right) child of the
	 * parent unless the {@link #setLeft(Node)} method or the
	 * {@link #setRight(Node)} method is used. Hence, unless one of those
	 * methods and {@link #calculateWeight()} are used, the weight of the parent
	 * is not changed.
	 * 
	 * @param parent the node to assign as the parent.
	 * @throws UnsupportedOperationException if <code>parent</code> is a leaf.
	 * @throws NullPointerException if <code>parent</code> is <code>null</code>.
	 * @throws IllegalArgumentException if <code>parent</code> is the same node
	 *         as this node.
	 */
	void setParent(Node parent);

	/**
	 * Calculates the weight of this node.
	 * <p>
	 * Note that this operation takes time proportional to the number of nodes
	 * in the subtree of this node.
	 */
	void calculateWeight();

	/**
	 * Removes leaves that have the same value as their parents in the tree
	 * rooted at this node. Also removes internal nodes that only have one child
	 * as a result of removal of one of their children.
	 * 
	 * @return the root after cleaning.
	 */
	Node clean();

	/**
	 * Returns whether or not this node represents additional models.
	 * <p>
	 * <code>{@link #hasAdditionalModels()} == false</code> if and only if
	 * <code>{@link #getAdditionalModelsCount()} == 0</code> if and only if
	 * <code>{@link #getAdditionalModels()} == null</code>.
	 * 
	 * @return <code>true</code> if and only if this node represents additional
	 *         models.
	 */
	boolean hasAdditionalModels();

	/**
	 * Returns the number of additional models represented by this node.
	 * <p>
	 * <code>{@link #hasAdditionalModels()} == false</code> if and only if
	 * <code>{@link #getAdditionalModelsCount()} == 0</code> if and only if
	 * <code>{@link #getAdditionalModels()} == null</code>.
	 * 
	 * @return the number of additional models.
	 */
	int getAdditionalModelsCount();

	/**
	 * Returns a list of additional models represented by this node. The
	 * returned list is independent from the list kept by this node (that is, it
	 * is a copy).
	 * <p>
	 * <code>{@link #hasAdditionalModels()} == false</code> if and only if
	 * <code>{@link #getAdditionalModelsCount()} == 0</code> if and only if
	 * <code>{@link #getAdditionalModels()} == null</code>.
	 * 
	 * @return the list, or <code>null</code> if there is no additional models.
	 */
	List<Model> getAdditionalModels();

	/**
	 * Adds a model to the additional models represented by this node.
	 * 
	 * @param model the model to add.
	 */
	void addAdditionalModel(Model model);
}
