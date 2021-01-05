package main.gui.custom;

import java.awt.Color;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Version 1.2 strips out functionality for using degrees for input.
 * 
 * A paint class that creates conical gradients around a given center point
 * It could be used in the same way as LinearGradientPaint and RadialGradientPaint
 * and follows the same syntax.
 * Gradients always start at the top with a clockwise direction and you could
 * rotate the gradient around the center by given offset.
 * The offset could also be defined from -0.5 to +0.5 or -180 to +180 degrees.
 *
 * Originally found at: http://www.jug-muenster.de/java2d-conical-gradient-paint-674/
 *
 * @author Gerrit Grunwald
 * @version 1.2 2020-10-10 updates by John McCullock
 */
public class HSLPanelGradient implements Paint
{
	private Point2D.Double mCenter;
	private double[] mFractionAngles = null;
	private double[] mRedLookup;
	private double[] mGreenLookup;
	private double[] mBlueLookup;
	private double[] mAlphaLookup;
	private Color[] mColors;
	private final float INT_TO_FLOAT_CONST = 1f / 255f;
	
	public HSLPanelGradient(Point2D.Double centerParam, float[] fractionsParam, Color[] colorsParam) throws IllegalArgumentException
	{
		this(centerParam, 0.0f, fractionsParam, colorsParam);
	}
	
	public HSLPanelGradient(Point2D.Double centerParam, float offsetParam, float[] fractionsParam, Color[] colorsParam) throws IllegalArgumentException
	{
		// Check that fractions and colors are of the same size
		if(fractionsParam.length != colorsParam.length){
			throw new IllegalArgumentException("Fractions and colors must be equal in size");
		}
		
		ArrayList<Float> fractionList = new ArrayList<Float>(fractionsParam.length);
		float offset;
		
		if(offsetParam == -0.5){
			// This is needed because of problems in the creation of the Raster
			// with a angle offset of exactly -0.5
			offset = -0.49999f;
		}else if(offsetParam == 0.5){
			// This is needed because of problems in the creation of the Raster
			// with a angle offset of exactly +0.5
			offset = 0.499999f;
		}else{
			offset = offsetParam;
		}
		for (float fraction : fractionsParam)
		{
			fractionList.add(fraction);
		}
		
		// Check for valid offset
		if(offset > 0.5f || offset < -0.5f){
			throw new IllegalArgumentException("Offset has to be in the range of -0.5 to 0.5");
		}
		
		// Adjust fractions and colors array in the case where startvalue != 0.0f and/or endvalue != 1.0f
		List<Color> colorList = new ArrayList<Color>(colorsParam.length);
		colorList.addAll(Arrays.asList(colorsParam));
		
		// Assure that fractions start with 0.0f
		if(fractionList.get(0) != 0.0f){
			fractionList.add(0, 0.0f);
			Color tempColor = colorList.get(0);
			colorList.add(0, tempColor);
		}
		
		// Assure that fractions end with 1.0f
		if(fractionList.get(fractionList.size() - 1) != 1.0f){
			fractionList.add(1.0f);
			colorList.add(colorsParam[0]);
		}
		
		// Recalculate the fractions and colors with the given offset
		Map<Float, Color> fractionColors = recalculate(fractionList, colorList, offset);
		
		// Clear the original FRACTION_LIST and COLOR_LIST
		fractionList.clear();
		colorList.clear();
		
		// Sort the HashMap by fraction and add the values to the FRACION_LIST and COLOR_LIST
		SortedSet<Float> sorted = new TreeSet<Float>(fractionColors.keySet());
		Iterator<Float> iter = sorted.iterator();
		while(iter.hasNext())
		{
			float current = iter.next();
			fractionList.add(current);
			colorList.add(fractionColors.get(current));
		}
		
		// Set the values
		this.mCenter = centerParam;
		this.mColors = colorList.toArray(new Color[]{});
		
		// Prepare lookup table for the angles of each fraction
		int count = fractionList.size();
		this.mFractionAngles = new double[count];
		for(int i = 0 ; i < count ; i++)
		{
			this.mFractionAngles[i] = fractionList.get(i) * 360;
		}
		
		// Prepare lookup tables for the color step size of each color
		this.mRedLookup = new double[this.mColors.length];
		this.mGreenLookup = new double[this.mColors.length];
		this.mBlueLookup = new double[this.mColors.length];
		this.mAlphaLookup = new double[this.mColors.length];
		
		for (int i = 0 ; i < (this.mColors.length - 1) ; i++)
		{
			this.mRedLookup[i] = ((this.mColors[i + 1].getRed() - this.mColors[i].getRed()) * INT_TO_FLOAT_CONST) / (this.mFractionAngles[i + 1] - this.mFractionAngles[i]);
			this.mGreenLookup[i] = ((this.mColors[i + 1].getGreen() - this.mColors[i].getGreen()) * INT_TO_FLOAT_CONST) / (this.mFractionAngles[i + 1] - this.mFractionAngles[i]);
			this.mBlueLookup[i] = ((this.mColors[i + 1].getBlue() - this.mColors[i].getBlue()) * INT_TO_FLOAT_CONST) / (this.mFractionAngles[i + 1] - this.mFractionAngles[i]);
			this.mAlphaLookup[i] = ((this.mColors[i + 1].getAlpha() - this.mColors[i].getAlpha()) * INT_TO_FLOAT_CONST) / (this.mFractionAngles[i + 1] - this.mFractionAngles[i]);
		}
	}
	
	private HashMap<Float, Color> recalculate(List<Float> fractionList, List<Color> colorList, float offset)
	{
		// Recalculate the fractions and colors with the given offset
		int numFractions = fractionList.size();
		HashMap<Float, Color> results = new HashMap<Float, Color>(numFractions);
		for (int i = 0 ; i < numFractions ; i++)
		{
			// Add offset to fraction
			float tempFactor = fractionList.get(i) + offset;
			
			// Color related to current fraction
			Color tempColor = colorList.get(i);
			
			// Check each fraction for limits (0...1)
			if(tempFactor <= 0){
				results.put(1.0f + tempFactor + 0.0001f, tempColor);
				
				float nextFactor;
				Color nextColor;
				if(i < numFractions - 1){
					nextFactor = fractionList.get(i + 1) + offset;
					nextColor = colorList.get(i + 1);
				}else{
					nextFactor = 1 - fractionList.get(0) + offset;
					nextColor = colorList.get(0);
				}
				if(nextFactor > 0){
					Color newColor = getColorFromFraction(tempColor, nextColor, (int)((nextFactor - tempFactor) * 10000), (int)((-tempFactor) * 10000));
					results.put(0.0f, newColor);
					results.put(1.0f, newColor);
				}
			}else if(tempFactor >= 1){
				results.put(tempFactor - 1.0f - 0.0001f, tempColor);
				
				float prevFactor;
				Color prevColor;
				if(i > 0){
					prevFactor = fractionList.get(i - 1) + offset;
					prevColor = colorList.get(i - 1);
				}else{
					prevFactor = fractionList.get(numFractions - 1) + offset;
					prevColor = colorList.get(numFractions - 1);
				}
				if(prevFactor < 1){
					Color newColor = getColorFromFraction(tempColor, prevColor, (int)((tempFactor - prevFactor) * 10000), (int)(tempFactor - 1.0f) * 10000);
					results.put(1.0f, newColor);
					results.put(0.0f, newColor);
				}
			}else{
				results.put(tempFactor, tempColor);
			}
		}
		
		fractionList.clear();
		colorList.clear();
		
		return results;
	}
	
	public Color getColorFromFraction(Color start, Color end, int range, int value)
	{
		final float startRed = start.getRed() * INT_TO_FLOAT_CONST;
		final float startGreen = start.getGreen() * INT_TO_FLOAT_CONST;
		final float startBlue = start.getBlue() * INT_TO_FLOAT_CONST;
		final float startAlpha = start.getAlpha() * INT_TO_FLOAT_CONST;
		
		final float endRed = end.getRed() * INT_TO_FLOAT_CONST;
		final float endGreen = end.getGreen() * INT_TO_FLOAT_CONST;
		final float endBlue = end.getBlue() * INT_TO_FLOAT_CONST;
		final float endAlpha = end.getAlpha() * INT_TO_FLOAT_CONST;
		
		final float redDiff = endRed - startRed;
		final float greenDiff = endGreen - startGreen;
		final float blueDiff = endBlue - startBlue;
		final float alphaDiff = endAlpha - startAlpha;
		
		final float redFactor = redDiff / range;
		final float greenFactor = greenDiff / range;
		final float blueFactor = blueDiff / range;
		final float alphaFactor = alphaDiff / range;
		
		return new Color(startRed + redFactor * value,
				startGreen + greenFactor * value,
				startBlue + blueFactor * value,
				startAlpha + alphaFactor * value);
	}
	
	@Override
	public PaintContext createContext(ColorModel model, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform transform, RenderingHints hints)
	{
		Point2D transformedCenter = transform.transform(this.mCenter, null);
		return new ConicalGradientPaintContext(transformedCenter);
	}
	
	@Override
	public int getTransparency()
	{
		return java.awt.Transparency.TRANSLUCENT;
	}
	
	private class ConicalGradientPaintContext implements PaintContext
	{
		private Point2D.Double center = null;
		
		public ConicalGradientPaintContext(Point2D center)
		{
			this.center = new Point2D.Double(center.getX(), center.getY());
		}
		
		@Override
		public void dispose()
		{
		}
		
		@Override
		public ColorModel getColorModel()
		{
			return ColorModel.getRGBdefault();
		}
		
		@Override
		public Raster getRaster(int x, int y, int tileWidth, int tileHeight)
		{
			double rotationCenterX = -x + this.center.x;
			double rotationCenterY = -y + this.center.y;
			
			int max = HSLPanelGradient.this.mFractionAngles.length;
			
			// Create raster for given ColorModel
			WritableRaster raster = getColorModel().createCompatibleWritableRaster(tileWidth, tileHeight);
			
			// Create data array with place for red, green, blue and alpha values
			int[] data = new int[(tileWidth * tileHeight * 4)];
			
			double dx;
			double dy;
			double distance;
			double angle;
			double currentRed = 0;
			double currentGreen = 0;
			double currentBlue = 0 ;
			double currentAlpha = 0;
			
			for(int py = 0; py < tileHeight; py++)
			{
				for(int px = 0; px < tileWidth; px++)
				{
					// Calculate the distance between the current position and the rotation angle
					dx = px - rotationCenterX;
					dy = py - rotationCenterY;
					distance = Math.sqrt(dx * dx + dy * dy);
					
					// Avoid division by zero
					if(distance == 0){
						distance = 1;
					}
					
					// 0 degree on top
					angle = Math.abs(Math.toDegrees(Math.acos(dx / distance)));
					
					if(dx >= 0 && dy <= 0){
						angle = 90.0 - angle;
					}else if(dx >= 0 && dy >= 0){
						angle += 90.0;
					}else if(dx <= 0 && dy >= 0){
						angle += 90.0;
					}else if(dx <= 0 && dy <= 0){
						angle = 450.0 - angle;
					}
					
					// Check for each angle in fractionAngles array
					for(int i = 0; i < (max - 1); i++)
					{
						if(angle >= HSLPanelGradient.this.mFractionAngles[i]){
							currentRed = HSLPanelGradient.this.mColors[i].getRed() * INT_TO_FLOAT_CONST + (angle - HSLPanelGradient.this.mFractionAngles[i]) * HSLPanelGradient.this.mRedLookup[i];
							currentGreen = HSLPanelGradient.this.mColors[i].getGreen() * INT_TO_FLOAT_CONST + (angle - HSLPanelGradient.this.mFractionAngles[i]) * HSLPanelGradient.this.mGreenLookup[i];
							currentBlue = HSLPanelGradient.this.mColors[i].getBlue() * INT_TO_FLOAT_CONST + (angle - HSLPanelGradient.this.mFractionAngles[i]) * HSLPanelGradient.this.mBlueLookup[i];
							currentAlpha = HSLPanelGradient.this.mColors[i].getAlpha() * INT_TO_FLOAT_CONST + (angle - HSLPanelGradient.this.mFractionAngles[i]) * HSLPanelGradient.this.mAlphaLookup[i];
							continue;
						}
					}
					
					// Fill data array with calculated color values
					int base = (py * tileWidth + px) * 4;
					data[base + 0] = (int) (currentRed * 255);
					data[base + 1] = (int) (currentGreen * 255);
					data[base + 2] = (int) (currentBlue * 255);
					data[base + 3] = (int) (currentAlpha * 255);
				}
			}
			
			// Fill the raster with the data
			raster.setPixels(0, 0, tileWidth, tileHeight, data);
			
			return raster;
		}
	}
}
