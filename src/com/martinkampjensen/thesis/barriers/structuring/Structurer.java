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

import com.martinkampjensen.thesis.model.BarrierForest;
import com.martinkampjensen.thesis.model.BarrierTree;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.model.Node;

/**
 * A {@link Structurer} (re)structures how {@link Node}s are connected based on
 * properties of the nodes and the {@link Model}s they contain.
 */
public interface Structurer
{
	/**
	 * Structures each {@link Node} in each {@link BarrierTree} of a
	 * {@link BarrierForest}.
	 * 
	 * @param forest the forest containing the nodes to structure.
	 * @throws NullPointerException if <code>forest</code> is <code>null</code>.
	 */
	void structure(BarrierForest forest);

	/**
	 * Structures each {@link Node} in a {@link BarrierTree}.
	 * 
	 * @param tree the tree containing the nodes to structure.
	 * @throws NullPointerException if <code>tree</code> is <code>null</code>.
	 */
	void structure(BarrierTree tree);

	/**
	 * Structures a {@link Node}.
	 * 
	 * @param node the node to structure.
	 * @throws NullPointerException if <code>node</code> is <code>null</code>.
	 */
	void structure(Node node);
}
