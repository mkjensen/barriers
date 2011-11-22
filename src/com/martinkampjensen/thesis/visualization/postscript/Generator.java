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

package com.martinkampjensen.thesis.visualization.postscript;

import java.awt.Color;
import java.util.Date;

import com.martinkampjensen.thesis.barriers.coloring.Colorer;
import com.martinkampjensen.thesis.barriers.structuring.Structurer;
import com.martinkampjensen.thesis.model.BarrierForest;
import com.martinkampjensen.thesis.model.BarrierTree;
import com.martinkampjensen.thesis.model.Node;

/**
 * TODO: Document {@link Generator}.
 */
public final class Generator
{
	// A4 paper should result in the following PostScript document properties:
	// (0, 0) is the bottom left corner of the page and (612, 792) is the top
	// right corner of the page.
	// See: http://www.physics.emory.edu/~weeks/graphics/howtops1.html
	private static final boolean ADD_HEADER = true;
	private static final int HEADER_HEIGHT = (ADD_HEADER ? 90 : 0);
	private static final int X_MIN = 87;
	private static final int X_MAX = 587;
	private static final int Y_MIN = 50;
	private static final int Y_MAX = 742;

	private Generator()
	{
	}

	/**
	 * Generates and returns a graphical representation of a
	 * {@link BarrierForest} as a PostScript string.
	 * <p>
	 * Does not use a structurer nor a colorer. Calling this method is therefore
	 * identical to calling
	 * {@link #barrierForest(BarrierForest, Structurer, Colorer)} with the last
	 * two parameters being <code>null</code>.
	 * 
	 * @return a string in PostScript format.
	 * @see <a href="http://en.wikipedia.org/wiki/PostScript">PostScript</a>
	 */
	public static String barrierForest(BarrierForest forest)
	{
		return barrierForest(forest, null, null);
	}

	/**
	 * Generates and returns a graphical representation of a
	 * {@link BarrierForest} as a PostScript string.
	 * 
	 * @param structurer the structurer to use, or <code>null</code> if node.
	 * @param colorer the colorer to use, or <code>null</code> if none.
	 * @return a string in PostScript format.
	 * @see <a href="http://en.wikipedia.org/wiki/PostScript">PostScript</a>
	 */
	public static String barrierForest(BarrierForest forest,
			Structurer structurer, Colorer colorer)
	{
		if(structurer != null) {
			structurer.structure(forest);
		}

		if(colorer != null) {
			colorer.color(forest);
		}

		final StringBuilder sb = new StringBuilder();
		final int nTrees = forest.getNumberOfTrees();
		final double minValue = forest.getMinimumValue();
		final double maxValue = forest.getMaximumBarrierValue();
		final double valuePerPixel =
			(maxValue - minValue) / (Y_MAX - Y_MIN - HEADER_HEIGHT);
		int xMax = X_MIN;
		int xMin;

		for(int i = 0; i < nTrees; i++) {
			final BarrierTree tree = forest.getTree(i);
			final Node root = tree.getRoot();
			final int totalLeaves = forest.getNumberOfLeaves();
			final int treeLeaves = tree.getNumberOfLeaves();
			final double maxBarrierValue = forest.getMaximumBarrierValue();

			xMin = xMax;
			xMax += (int)((X_MAX - X_MIN) * treeLeaves / (double)totalLeaves);

			calculateNodePositions(root, xMin, xMax, maxBarrierValue,
					valuePerPixel);
		}

		beginPostScript(sb);
		addHeader(sb, forest, structurer, colorer);
		addForest(sb, forest);
		addScale(sb, minValue, valuePerPixel);
		endPostScript(sb);

		return sb.toString();
	}

	private static void calculateNodePositions(Node root, int from, int to,
			double maxBarrierValue, double valuePerPixel)
	{
		root.setY((int)(Y_MAX - HEADER_HEIGHT
				- (maxBarrierValue - root.getValue()) / valuePerPixel));

		if(root.isLeaf()) {
			root.setX((int)(from + (to - from) / 2.0));
			return;
		}

		final Node left = root.getLeft();
		final Node right = root.getRight();
		final int width = to - from;
		final int rootLeaves = root.getWeight();
		final int leftLeaves = left.getWeight();
		final int leftMargin = (int)(width * leftLeaves / (double)rootLeaves);

		calculateNodePositions(left, from, from + leftMargin, maxBarrierValue,
				valuePerPixel);
		calculateNodePositions(right, from + leftMargin + 1, to,
				maxBarrierValue, valuePerPixel);

		final int leftX = left.getX();
		final int rightX = right.getX();

		root.setX((int)(leftX + (rightX - leftX) / 2.0));
	}

	private static void addHeader(StringBuilder sb, BarrierForest forest,
			Structurer structurer, Colorer colorer)
	{
		if(!ADD_HEADER) {
			return;
		}

		final String structurerName = (structurer == null ? "Unknown" :
			structurer.getClass().getSimpleName());
		final String colorerName = (structurer == null ? "Unknown" :
			colorer.getClass().getSimpleName());

		final String line1 = String.format("Barrier forest generated %s",
				new Date().toString());
		final String line2 = String.format("Pruning threshold: %f, "
				+ "Neighbor threshold: %f", forest.getPruningThreshold(),
				forest.getNeighborThreshold());
		final String line3 =
			String.format("Number of trees: %d, Number of leaves: %d, "
					+ "Minimum value: %s",
					forest.getNumberOfTrees(), forest.getNumberOfLeaves(),
					formatValue(forest.getMinimumValue()));
		final String line4 =
			String.format("Minimum barrier value: %s, "
					+ "Maximum barrier value: %s",
					formatValue(forest.getMinimumBarrierValue()),
					formatValue(forest.getMaximumBarrierValue()));
		final String line5 =
			String.format("Total barrier value: %s, "
					+ "Total connection value: %s",
					formatValue(forest.getTotalBarrierValue()),
					formatValue(forest.getTotalConnectionValue()));
		final String line6 = String.format("Structurer: %s, Colorer: %s",
				structurerName, colorerName);

		setFont(sb, 10);
		addText(sb, X_MIN, Y_MAX, line1);
		addText(sb, X_MIN, Y_MAX - 15, line2);
		addText(sb, X_MIN, Y_MAX - 30, line3);
		addText(sb, X_MIN, Y_MAX - 45, line4);
		addText(sb, X_MIN, Y_MAX - 60, line5);
		addText(sb, X_MIN, Y_MAX - 75, line6);
	}

	private static void addForest(StringBuilder sb, BarrierForest forest)
	{
		final int nTrees = forest.getNumberOfTrees();
		for(int i = 0; i < nTrees; i++) {
			final BarrierTree tree = forest.getTree(i);
			final Node root = tree.getRoot();
			addTree(sb, root);
		}
	}

	private static void addTree(StringBuilder sb, Node root)
	{
		final int rootX = root.getX();
		final int rootY = root.getY();
		final Color rootColor = root.getColor();

		if(root.isLeaf()) {
			setFont(sb, 8);

			// Add the node id below the line coming from the barrier line.
			addText(sb, rootX - 2, rootY - 9, root.getId());

			// Add the number of additional nodes represented by this node, if
			// any.
			if(root.hasAdditionalModels()) {
				addText(sb, rootX - 2, rootY - 18, "("
						+ root.getAdditionalModelsCount() + ")");
			}

			return;
		}

		final Node left = root.getLeft();
		final int leftX = left.getX();
		final int leftY = left.getY();
		final Color leftColor = left.getColor();

		final Node right = root.getRight();
		final int rightX = right.getX();
		final int rightY = right.getY();
		final Color rightColor = right.getColor();

		// Add barrier line.
		addLine(sb, leftX, rootY, rightX, rootY, rootColor);

		// Add barrier id and the number of additional nodes represented by this
		// node, if any.
		String barrierIdAdditionals = "" + root.getId();
		if(root.hasAdditionalModels()) {
			barrierIdAdditionals += " ("
				+ root.getAdditionalModelsCount() + ")";
		}
		setFont(sb, 6);
		addText(sb, rightX + 2, rootY, barrierIdAdditionals);

		// Add line to left child.
		addLine(sb, leftX, rootY, leftX, leftY, leftColor);

		// Add line to right child.
		addLine(sb, rightX, rootY, rightX, rightY, rightColor);

		addTree(sb, left);
		addTree(sb, right);
	}

	private static void addScale(StringBuilder sb, double minValue,
			double valuePerPixel)
	{
		final int stepSize = (Y_MAX - Y_MIN - HEADER_HEIGHT) / 18;
		setFont(sb, 8);

		// Add the vertical line representing the scale.
		addLine(sb, X_MIN - 12, Y_MIN, X_MIN - 12, Y_MAX - HEADER_HEIGHT);	

		for(int i = Y_MIN; i < Y_MAX - HEADER_HEIGHT; i += stepSize) {
			// Add ticks to the right side of the scale.
			addLine(sb, X_MIN - 12, i, X_MIN - 3, i);

			// Add values to the left of the scale/ticks.
			addText(sb, X_MIN - 52, i - 2,
					minValue + (i - Y_MIN) * valuePerPixel);	 
		}

		// Last point (because stepSize is calculated with integer division).
		addLine(sb, X_MIN - 12, Y_MAX - HEADER_HEIGHT, X_MIN - 3,
				Y_MAX - HEADER_HEIGHT);
		addText(sb, X_MIN - 52, Y_MAX - 2 - HEADER_HEIGHT,
				minValue + (Y_MAX - HEADER_HEIGHT - Y_MIN) * valuePerPixel);
	}

	private static void beginPostScript(StringBuilder sb)
	{
		sb.append("%!PS-Adobe-3.0 EPSF-3.0\n");
		sb.append("%%BoundingBox: 0 0 " + (X_MAX + 25) + " " + (Y_MAX + 25)
				+ "\n");
		sb.append("%%EndComments\n");
	}

	private static void setFont(StringBuilder sb, int size)
	{
		sb.append("/Times-Roman findfont\n" + size + " scalefont\nsetfont\n");
	}

	private static void setColor(StringBuilder sb, Color color)
	{
		final float[] rgb = color.getRGBColorComponents(null);
		final float red = rgb[0];
		final float green = rgb[1];
		final float blue = rgb[2];

		sb.append(String.format("%f %f %f setrgbcolor\n", red, green, blue));
	}

	private static void addText(StringBuilder sb, int x, int y, int value)
	{
		addText(sb, x, y, "" + value);
	}

	private static void addText(StringBuilder sb, int x, int y, double value)
	{
		addText(sb, x, y, "" + formatValue(value));
	}

	private static String formatValue(double value)
	{
		return String.format("%,.3f", value);
	}

	private static void addText(StringBuilder sb, int x, int y, String text)
	{
		addText(sb, x, y, Color.BLACK, text);
	}

	private static void addText(StringBuilder sb, int x, int y, Color color,
			String text)
	{
		sb.append(x + " " + y + " moveto\n");
		setColor(sb, color);
		sb.append("(" + text + ") show\n");
	}

	private static void addLine(StringBuilder sb, int x1, int y1, int x2,
			int y2)
	{
		addLine(sb, x1, y1, x2, y2, Color.BLACK);
	}

	private static void addLine(StringBuilder sb, int x1, int y1, int x2,
			int y2, Color color)
	{
		sb.append("newpath\n");
		sb.append(x1 + " " + y1 + " moveto\n");
		sb.append(x2 + " " + y2 + " lineto\n");
		sb.append("closepath\n");
		setColor(sb, color);
		sb.append("stroke\n");
	}

	private static void endPostScript(StringBuilder sb)
	{
		sb.append("showpage\n");
	}
}
