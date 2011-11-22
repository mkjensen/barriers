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

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

import com.martinkampjensen.thesis.Main;
import com.martinkampjensen.thesis.StatusCode;

/**
 * TODO: Document {@link R}.
 */
public final class R
{
	private static Rengine _re;

	private R()
	{
	}

	public static void open()
	{
		if(_re == null) {
			if(!Rengine.versionCheck()) {
				Main.errorExit(String.format("R version mismatch: %d != %d",
						Rengine.getVersion(), Rengine.rniGetVersion()),
						StatusCode.R);
			}

			_re = new Rengine(new String[] { "--vanilla" }, false, null);

			if(!_re.waitForR()) {
				Main.errorExit("Failed to load R.", StatusCode.R);
			}

			// Attempt to shutdown R gracefully even if the JVM is starting to
			// exit before the close method has been called manually.
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run()
				{
					close();
				}
			});
		}
	}

	public static void close()
	{
		if(_re != null) {
			_re.end();
		}
	}

	public static void plot(double[] x, double[] y, PlotType plotType,
			String title, String xLabel, String yLabel, File file)
	{
		_re.assign("x", x);
		_re.assign("y", y);

		_re.eval("pdf(\"" + getFileName(file));
		_re.eval(String.format("plot(x, y, type=\"%s\", main=\"%s\", "
				+ "xlab=\"%s\", ylab=\"%s\")", plotType, title, xLabel,
				yLabel));
		_re.eval("dev.off()");
	}

	public static void dualPlot(double[] x, double[] y1, double[] y2,
			boolean includeLogPlots, PlotType plotType, String title,
			String xLabel, String y1Label, String y2Label, File file)
	{
		_re.assign("x", x);
		_re.assign("y1", y1);
		_re.assign("y2", y2);

		_re.eval("pdf(\"" + getFileName(file));
		_re.eval("par(mfrow=c(2, 1))");

		if(includeLogPlots) {
			plot(true, plotType, title, xLabel, y1Label, y2Label); 
		}
		plot(false, plotType, title, xLabel, y1Label, y2Label);

		_re.eval("dev.off()");
	}

	public static void boxplot(double[][] values, File file)
	{
		Debug.line("Transfering data to R");

		final int nVariables = values.length;

		for(int i = 0; i < nVariables; i++) {
			_re.assign("values" + i, values[i]);
		}

		final StringBuilder sb = new StringBuilder();

		sb.append("values <- matrix(c(");

		for(int i = 0; i < nVariables - 1; i++) {
			sb.append("values");
			sb.append(i);
			sb.append(", ");
		}

		sb.append("values");
		sb.append(nVariables - 1);
		sb.append("), ncol=");
		sb.append(nVariables);
		sb.append(")");

		_re.eval(sb.toString());

		Debug.line("Creating box plot and writing to \"%s\"", file.getName());
		_re.eval("pdf(\"" + getFileName(file));
		_re.eval("boxplot(values, outline=FALSE)");
		_re.eval("dev.off()");
	}

	public static double[] pca(double[][] values)
	{
		final int nVariables = values.length;

		for(int i = 0; i < nVariables; i++) {
			_re.assign("samples" + i, values[i]);
		}

		final StringBuilder sb = new StringBuilder();

		sb.append("samples <- matrix(c(");

		for(int i = 0; i < nVariables - 1; i++) {
			sb.append("samples");
			sb.append(i);
			sb.append(", ");
		}

		sb.append("samples");
		sb.append(nVariables - 1);
		sb.append("), ncol=");
		sb.append(nVariables);
		sb.append(")");

		_re.eval(sb.toString());
		final REXP prcomp =
			_re.eval("prcomp(samples, center=TRUE, scale=TRUE)");
		final REXP rotation = (REXP)prcomp.asVector().get(1);
		final double[] loadings = rotation.asDoubleArray();

		return loadings;
	}

	private static String getFileName(File file)
	{
		return file.getAbsolutePath().replace('\\', '/') + "\")";
	}

	private static void plot(boolean logY, PlotType plotType, String title,
			String xLabel, String y1Label, String y2Label)
	{
		String log = "";

		if(logY) {
			log = "y";
			y1Label += " (log)";
			y2Label += " (log)";
		}

		_re.eval("par(mar=c(1, 4, 2, 2))"); // bottom, left, top, right
		_re.eval(String.format("plot(x, y1, log=\"%s\", type=\"%s\", "
				+ "main=\"%s\", xaxt=\"n\", ylab=\"%s\")", log, plotType, title,
				y1Label));
		_re.eval("par(mar=c(3.6, 4, 0, 2))");
		_re.eval(String.format("plot(x, y2, log=\"%s\", type=\"%s\", "
				+ "xlab=\"\", ylab=\"%s\")", log, plotType, y2Label));
		_re.eval(String.format("mtext(\"%s\", side=1, line=2.5)", xLabel));
	}

	public enum PlotType
	{
		BOTH("b"),
		LINES("l"),
		POINTS("p");

		private final String _code;

		private PlotType(String code)
		{
			_code = code;
		}

		@Override
		public String toString()
		{
			return _code;
		}
	}
}
