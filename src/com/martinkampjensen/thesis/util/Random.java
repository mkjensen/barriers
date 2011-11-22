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
 * TODO: Document {@link Random}.
 */
public final class Random
{
	private static final java.util.Random RANDOM = new java.util.Random(42);

	private Random()
	{
	}

	/**
	 * @see java.util.Random#nextBoolean()
	 */
	public static boolean nextBoolean()
	{
		return RANDOM.nextBoolean();
	}

	/**
	 * @see java.util.Random#nextInt()
	 */
	public static int nextInt()
	{
		return RANDOM.nextInt();
	}

	/**
	 * @see java.util.Random#nextInt(int)
	 */
	public static int nextInt(int n)
	{
		return RANDOM.nextInt(n);
	}

	/**
	 * @see java.util.Random#nextDouble()
	 */
	public static double nextDouble()
	{
		return RANDOM.nextDouble();
	}

	/**
	 * Returns the next pseudorandom, uniformly distributed {@code double} value
	 * between {@code min} (inclusive) and {@code max} (exclusive).
	 *  
	 * @param min the lower bound on the random number to be returned
	 * @param max the upper bound on the random number to be returned
	 * @return the next pseudorandom, uniformly distributed {@code double} value
	 * between {@code min} (inclusive) and {@code max} (exclusive)
	 * @see java.util.Random#nextDouble()
	 */
	public static double nextDouble(double min, double max)
	{
		return min + nextDouble() * (max - min); 
	}

	/**
	 * @see java.util.Random#setSeed(long)
	 */
	public static void setSeed(long seed)
	{
		RANDOM.setSeed(seed);

		Debug.line("Random generator seeded with %d", seed);
	}
}
