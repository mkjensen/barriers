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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.martinkampjensen.thesis.Constant;
import com.martinkampjensen.thesis.Main;
import com.martinkampjensen.thesis.StatusCode;
import com.martinkampjensen.thesis.model.Model;
import com.martinkampjensen.thesis.model.ZMatrixFactory;
import com.martinkampjensen.thesis.util.openbabel.OpenBabel;
import com.martinkampjensen.thesis.util.openbabel.OpenBabelData;

/**
 * The {@link Util} class contains useful methods that does not fit elsewhere.
 */
public final class Util
{
	private Util()
	{
	}

	/**
	 * Returns whether or not this JVM is running on Linux.
	 * 
	 * @return <code>true</code> if and only if this JVM is running on Linux.
	 */
	public static boolean isLinux()
	{
		return System.getProperty("os.name").contains("Linux");
	}

	/**
	 * Returns whether or not this JVM is running on Mac OS.
	 * 
	 * @return <code>true</code> if and only if this JVM is running on Mac OS.
	 */
	public static boolean isMacOs()
	{
		return System.getProperty("os.name").contains("Mac OS");
	}

	/**
	 * Returns whether or not this JVM is running on Windows.
	 * 
	 * @return <code>true</code> if and only if this JVM is running on Windows.
	 */
	public static boolean isWindows()
	{
		return System.getProperty("os.name").contains("Windows");
	}

	/**
	 * Creates and returns a model instance from a file or a class.
	 * 
	 * @param fileOrClassName name of the file or the class containing the
	 *        model.
	 * @return the model.
	 */
	public static Model instantiateModel(String fileOrClassName)
	{
		return instantiateModel(fileOrClassName, null);
	}

	public static Model instantiateModel(String fileOrClassName,
			File topologyFile)
	{
		final File file = new File(fileOrClassName);

		if(file.exists() && file.isFile()
				&& file.getName().endsWith(Constant.PDB_FILENAME_EXTENSION)) {
			final OpenBabelData obData =
				OpenBabel.createObDataFromPdb(file, topologyFile);
			return ZMatrixFactory.create(obData);
		}
		else {
			Debug.line("Instantiating model");
			return instantiate(fileOrClassName);
		}
	}

	/**
	 * Creates and returns an instance of a class. The JVM will be shut down if
	 * an error occurs.
	 * 
	 * @param <T> type of the class.
	 * @param className name of the class.
	 * @return an instance of the class.
	 */
	public static <T> T instantiate(String className)
	{
		try {
			final Class<?> clazz = Class.forName(className);
			@SuppressWarnings("unchecked")
			final T instance = (T)clazz.newInstance();
			return instance;
		}
		catch(Exception e) {
			Main.errorExit(e, StatusCode.REFLECTION);
		}

		return null;
	}

	/**
	 * Returns whether a <code>double</code> value is (considered) less than
	 * another.
	 * 
	 * @param a the first value.
	 * @param b the second value.
	 * @return <code>true</code> if and only if <code>a</code> is (considered)
	 *         less than <code>b</code>.
	 * @see #compare(double, double)
	 */
	public static boolean isLess(double a, double b)
	{
		return compare(a, b) < 0;
	}

	/**
	 * Returns whether a <code>double</code> value is (considered) less than or
	 * equal to another.
	 * 
	 * @param a the first value.
	 * @param b the second value.
	 * @return <code>true</code> if and only if <code>a</code> is (considered)
	 *         less than or equal to <code>b</code>.
	 * @see #compare(double, double)
	 */
	public static boolean isLessEqual(double a, double b)
	{
		return compare(a, b) <= 0;
	}

	/**
	 * Returns whether a <code>double</code> value is (considered) greater than
	 * another.
	 * 
	 * @param a the first value.
	 * @param b the second value.
	 * @return <code>true</code> if and only if <code>a</code> is (considered)
	 *         greater than <code>b</code>.
	 * @see #compare(double, double)
	 */
	public static boolean isGreater(double a, double b)
	{
		return compare(a, b) > 0;
	}

	/**
	 * Returns whether a <code>double</code> value is (considered) greater than
	 * or equal to another.
	 * 
	 * @param a the first value.
	 * @param b the second value.
	 * @return <code>true</code> if and only if <code>a</code> is (considered)
	 *         greater than or equal to <code>b</code>.
	 * @see #compare(double, double)
	 */
	public static boolean isGreaterEqual(double a, double b)
	{
		return compare(a, b) >= 0;
	}

	/**
	 * Returns whether or not two <code>double</code> values are (considered)
	 * equal.
	 * <p>
	 * This implementation returns whether or not <code>Math.abs(a - b)</code>
	 * is less than or equal to {@link Constant#DOUBLE_PRECISION} or not.
	 * @param a the first value.
	 * @param b the second value.
	 * @return <code>true</code> if and only if <code>a</code> and
	 *         <code>b</code> are (considered) equal.
	 */
	public static boolean isEqual(double a, double b)
	{
		final double absoluteDifference = Math.abs(a - b);

		if(absoluteDifference <= Constant.DOUBLE_PRECISION) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Compares <code>a</code> with <code>b</code> for order. Returns a negative
	 * integer, zero, or a positive integer as <code>a</code> is less than,
	 * equal to, or greater than <code>b</code>.
	 * <p>
	 * This implementation determines order by the following rules. For two
	 * values <code>a</code> and <code>b</code>, <code>a &gt; b</code> if
	 * and only if <code>a - b</code> is greater than
	 * {@link Constant#DOUBLE_PRECISION}. Hence, <code>b &gt; a</code>
	 * if and only if <code>a - b</code> is less than 
	 * -{@link Constant#DOUBLE_PRECISION}.
	 * <p>
	 * Otherwise, when <code>Math.abs(a - b)</code> is less than or equal to
	 * {@link Constant#DOUBLE_PRECISION}, <code>a</code> and <code>b</code> are
	 * equal.
	 * 
	 * @param a the first value.
	 * @param b the second value.
	 * @return a negative integer, zero, or a positive integer as <code>a</code>
	 *         is less than, equal to, or greater than <code>b</code>.
	 * @see Comparable#compareTo(Object)
	 * @see Math#abs(double)
	 */
	public static int compare(double a, double b)
	{
		final double difference = a - b;

		if(difference > Constant.DOUBLE_PRECISION) {
			return 1;
		}
		else if(difference < -Constant.DOUBLE_PRECISION) {
			return -1;
		}
		else {
			return 0;
		}
	}

	/**
	 * Returns the difference between two angles, in radians. The minimum
	 * difference is <code>0</code>. The maximum difference is
	 * {@link Constant#PI}.
	 * 
	 * @param a the first angle, in radians (<code>0 &le; a &lt;
	 *        {@link Constant#TWO_PI}</code>).
	 * @param b the second angle, in radians (<code>0 &le; b &lt;
	 *        {@link Constant#TWO_PI}</code>).
	 * @return the difference.
	 */
	public static double angleDifference(double a, double b)
	{
		double diff = Math.abs(a - b);

		if(diff > Constant.PI) {
			diff = Constant.TWO_PI - diff;
		}

		return diff;
	}

	/**
	 * Returns a value in the interval <code>[0; 2 * PI]</code> from a value
	 * that is cyclic (e.g. an angle in radians).
	 * 
	 * @param value a value.
	 * @return <code>value</code> in the interval <code>[0; 2 * PI]</code>. 
	 */
	public static double ensureAngleInterval(double value)
	{
		value %= Constant.TWO_PI;

		if(value < 0) {
			value += Constant.TWO_PI;
		}

		return value;
	}

	/**
	 * Changes any reference to one of two elements in an array to a third.
	 * 
	 * @param elements the references to change.
	 * @param first the first reference to change.
	 * @param second the second reference to change.
	 * @param successor the new reference that should succeed references to
	 *        <code>first</code> and <code>second</code>.
	 * @return <code>true</code> if all references in <code>elements</code> were
	 *         changed, <code>false</code> otherwise.
	 */
	public static <T> boolean changeReferences(T[] elements, T first, T second,
			T successor)
	{
		final int n = elements.length;
		boolean allChanged = true;

		for(int i = 0; i < n; i++) {
			final T element = elements[i];

			if(element == first || element == second) {
				elements[i] = successor;
			}
			else {
				allChanged = false;
			}
		}

		return allChanged;
	}

	/**
	 * Returns a duplicate-free list of an array. <code>null</code> elements
	 * will not be included in the list.
	 * 
	 * @param <T> the type of elements in the array.
	 * @param elements the array to remove duplicates from.
	 * @return the duplicate-free list.
	 */
	public static <T> List<T> removeDuplicates(T[] elements)
	{
		final int n = elements.length;
		final List<T> list = new ArrayList<T>();

		for(int i = 0; i < n; i++) {
			final T element = elements[i];

			if(element == null) {
				continue;
			}
			else {
				list.add(element);

				// Discard duplicates.
				for(int j = i + 1; j < n; j++) {
					if(elements[j] == element) {
						elements[j] = null;
					}
				}
			}
		}

		return list;
	}

	/**
	 * Returns a deep copy of a <code>double[][]</code>.
	 * 
	 * @param original the array to copy.
	 * @return the (independent) copy.
	 */
	public static double[][] copy(double[][] original)
	{
		final int length = original.length;
		final double[][] copy = new double[length][];

		for(int i = 0; i < length; i++) {
			copy[i] = original[i].clone();
		}

		return copy;
	}
}
