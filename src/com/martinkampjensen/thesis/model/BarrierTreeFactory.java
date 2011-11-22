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

import com.martinkampjensen.thesis.model.impl.BarrierTreeImpl;

/**
 * TODO: Document {@link BarrierTreeFactory}.
 */
public final class BarrierTreeFactory
{
	private BarrierTreeFactory()
	{
	}

	/**
	 * Constructs a new {@link BarrierTree} with the given {@link Node} as its
	 * root.
	 * 
	 * @param root the root of the tree.
	 * @param allowDebugPrints whether or not debug prints are allowed.
	 * @throws NullPointerException if <code>root</code> is <code>null</code>.
	 */
	public static BarrierTree create(Node root, boolean allowDebugPrints)
	{
		return new BarrierTreeImpl(root, allowDebugPrints);
	}
}
