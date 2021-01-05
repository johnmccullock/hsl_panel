package main.gui.custom;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class HSLPanel extends JPanel
{
	private HSLPanelComponent mComp = new HSLPanelComponent();
	
	public HSLPanel()
	{
		this.setLayout(new SquareLayout());
		this.add(this.mComp);
		return;
	}
	
	public void setHue(float hue)
	{
		this.mComp.setHue(hue);
		this.mComp.repaint();
		return;
	}
	
	public float getHue()
	{
		return this.mComp.getHue();
	}
	
	public void setSaturation(float saturation)
	{
		this.mComp.setSaturation(saturation);
		this.mComp.repaint();
		return;
	}
	
	public float getSaturation()
	{
		return this.mComp.getSaturation();
	}
	
	public void setLuminance(float luminance)
	{
		this.mComp.setLuminance(luminance);
		this.mComp.repaint();
		return;
	}
	
	public float getLuminance()
	{
		return this.mComp.getLuminance();
	}
	
	@Override
	public void setBackground(Color color)
	{
		super.setBackground(color);
		if(this.mComp != null){
			this.mComp.setBackground(color);
		}
		return;
	}
	
	@Override
	public Color getBackground()
	{
		if(this.mComp == null){
			return super.getBackground();
		}
		return this.mComp.getBackground();
	}
	
	public void setCaretHighlight(Color color)
	{
		this.mComp.setCaretHighlight(color);
		return;
	}
	
	public Color getCaretHighlight()
	{
		return this.mComp.getCaretHighlight();
	}
	
	public void setCaretShadow(Color color)
	{
		this.mComp.setCaretShadow(color);
	}
	
	public Color getCaretShadow()
	{
		return this.mComp.getCaretShadow();
	}
	
	public boolean getValueIsAdjusting()
	{
		return this.mComp.getValueIsAdjusting();
	}
	
	public void addChangeListener(HSLPanelListener listener)
	{
		this.mComp.addChangeListener(listener);
		return;
	}
	
	public void removeChangeListener(HSLPanelListener listener)
	{
		this.mComp.removeChangeListener(listener);
		return;
	}
}
