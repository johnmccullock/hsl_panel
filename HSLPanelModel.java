package main.gui.custom;

import java.awt.Color;

public class HSLPanelModel
{
	public static final float PI2 = (float)Math.PI * 2.0F;
	
	private float mCurrentHue = 0F;
	private float mCurrentSat = 0F;
	private float mCurrentLum = 0F;
	
	public void setCurrentHue(float value)
	{
		this.mCurrentHue = value;
		return;
	}
	
	public float getCurrentHue()
	{
		return this.mCurrentHue;
	}
	
	public void setCurrentSat(float value)
	{
		this.mCurrentSat = value;
		return;
	}
	
	public float getCurrentSat()
	{
		return this.mCurrentSat;
	}
	
	public void setCurrentLum(float value)
	{
		this.mCurrentLum = value;
		return;
	}
	
	public float getCurrentLum()
	{
		return this.mCurrentLum;
	}
	
	public Color getRGBfromHSL(float h, float s, float l)
	{
		float r = 0;
		float g = 0;
		float b = 0;
		
		if(s == 0.0F){
			r = l;
			g = l;
			b = l;
		}else{
			float q = l < 0.5F ? l * (1 + s) : l + s - l * s;
			float p = 2 * l - q;
			r = this.hueToRGB(p, q, h + 1.0F/3.0F);
			g = this.hueToRGB(p, q, h);
			b = this.hueToRGB(p, q, h - 1.0F/3.0F);
		}
		
		int rOut = (int)Math.round(r * 255);
		int gOut = (int)Math.round(g * 255);
		int bOut = (int)Math.round(b * 255);
		
		rOut = this.clamp(0, 255, rOut);
		gOut = this.clamp(0, 255, gOut);
		bOut = this.clamp(0, 255, bOut);
		
		return new Color(rOut, gOut, bOut);
	}
	
	public float hueToRGB(float p, float q, float t)
	{
		if(t < 0.0F){
			t += 1.0F;
		}
		if(t > 1.0F){
			t -= 1.0F;
		}
		if(t < 1.0F/6.0F){
			return p + (q - p) * 6 * t;
		}
		if(t < 1.0F/2.0F){
			return q;
		}
		if(t < 2.0F/3.0F){
			return p + (q - p) * (2.0F/3.0F - t) * 6;
		}
		return p;
	}
	
	public int clamp(int lowerBound, int upperBound, int value)
	{
		return (value < lowerBound) ? lowerBound : (value > upperBound) ? upperBound : value;
	}
	
	public float clamp(float lowerBound, float upperBound, float value)
	{
		return (value < lowerBound) ? lowerBound : (value > upperBound) ? upperBound : value;
	}
}
