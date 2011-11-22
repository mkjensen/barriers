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

package com.martinkampjensen.thesis.barriers;

import com.martinkampjensen.thesis.model.Model;

/**
 * TODO: Document {@link Barrier}.
 */
final class Barrier implements Comparable<Barrier>
{
	private final int _fromId;
	private final int _toId;
	private final Model _model;

	Barrier(int fromId, int toId, Model model)
	{
		if(model == null) {
			throw new NullPointerException("fromId = " + fromId  + ", toId = "
					+ toId + ", model == null");
		}
		else if(fromId == toId) {
			throw new IllegalArgumentException("fromId == toId (== " + fromId
					+ ")");
		}

		_fromId = fromId;
		_toId = toId;
		_model = model;
	}

	@Override
	public int compareTo(Barrier other)
	{
		final double thisValue = this.getValue();
		final double otherValue = other.getValue();

		if(thisValue > otherValue) {
			return 1;
		}
		else if(thisValue < otherValue) {
			return -1;
		}
		else {
			return 0;
		}
	}

	int getFromId()
	{
		return _fromId;
	}

	int getToId()
	{
		return _toId;
	}

	Model getModel()
	{
		return _model;
	}

	double getValue()
	{
		return _model.evaluate();
	}
}
