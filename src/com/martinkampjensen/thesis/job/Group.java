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

package com.martinkampjensen.thesis.job;

import javax.xml.bind.annotation.XmlTransient;

/**
 * TODO: Document {@link Group}.
 */
@XmlTransient
public abstract class Group
{
	protected Group()
	{
	}

	protected void add(StringBuilder sb, String name, String value)
	{
		add(sb, name, value, "");
	}

	protected void add(StringBuilder sb, String name, long value,
			long defaultValue)
	{
		add(sb, name, Long.toString(value), Long.toString(defaultValue));
	}

	protected void add(StringBuilder sb, String name, double value,
			double defaultValue)
	{
		add(sb, name, Double.toString(value), Double.toString(defaultValue));
	}

	protected void add(StringBuilder sb, String name, String value,
			String defaultValue)
	{
		final String category = getClass().getSimpleName().toLowerCase();

		sb.append(category);
		sb.append(' ');
		sb.append(name);
		sb.append(": ");
		sb.append(value);

		if(value.equals(defaultValue)) {
			sb.append(" (default)");
		}

		sb.append('\n');
	}

	protected abstract void toString(StringBuilder sb);
}
