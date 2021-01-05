package main.gui.custom;

public interface HSLPanelListener
{
	abstract void hueChanged(float value);
	abstract void saturationChanged(float value);
	abstract void luminanceChanged(float value);
}
