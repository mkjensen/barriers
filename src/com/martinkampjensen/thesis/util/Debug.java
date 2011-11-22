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

package com.martinkampjensen.thesis.util;

/**
 * TODO: Document {@link Debug}.
 */
public final class Debug
{
	private static final boolean IS_DEBUG = true;

	private Debug()
	{
	}

	public static boolean isDebug()
	{
		return IS_DEBUG;
	}

	public static void line(String x)
	{
		if(isDebug()) {
			System.err.println(stamp() + x);
		}
	}

	public static void line(String format, Object... args)
	{
		if(isDebug()) {
			System.err.println(String.format(stamp() + format, args));
		}
	}

	private static String stamp()
	{
		final double time = Timer.getElapsedTime();
		final long thread = Thread.currentThread().getId();

		return String.format("[%.3f %d] ", time, thread);
	}
}
