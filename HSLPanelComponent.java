package main.gui.custom;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.UIManager;

@SuppressWarnings("serial")
public class HSLPanelComponent extends JPanel
{
	private static final int BORDER_SPACE = 1;
	private static final int BORDER_THICKNESS = 1;
	private static final float[] CONE_FRACTIONS = new float[]{0.083333F,
																0.166666F,
																0.25F,
																0.333333F,
																0.416666F,
																0.5F,
																0.583333F,
																0.666666F,
																0.75F,
																0.833333F,
																0.916666F,
																1.0F};
	
	private static final Color[] CONE_COLORS = new Color[]{new Color(255, 255,   0, 255),
															new Color(255, 128,   0, 255),
															new Color(255,   0,   0, 255),
															new Color(255,   0, 128, 255),
															new Color(255,   0, 255, 255),
															new Color(128,   0, 255, 255),
															new Color(  0,   0, 255, 255),
															new Color(  0, 128, 255, 255),
															new Color(  0, 255, 255, 255),
															new Color(  0, 255, 128, 255),
															new Color(  0, 255,   0, 255),
															new Color(128, 255,   0, 255)};
	private static final float RING_THICKNESS_FACTOR = 0.11F;
	private static final int DEFAULT_BACKGROUND = UIManager.getColor("Panel.background").getRGB();
	private static final int DEFAULT_CARET_HIGHLIGHT = 0xFFFFFF;
	private static final int DEFAULT_CARET_SHADOW = 0x000000;
	
	protected Rectangle mSLBounds = new Rectangle();
	protected HSLPanelModel mModel = new HSLPanelModel();
	private int mBackground = DEFAULT_BACKGROUND;
	private int mCaretHighlight = DEFAULT_CARET_HIGHLIGHT;
	private int mCaretShadow = DEFAULT_CARET_SHADOW;
	private BasicStroke mBottomStroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private BasicStroke mTopStroke = new BasicStroke(1.25f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private float mRingThickness = RING_THICKNESS_FACTOR;
	private Cursor mNormalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	private Cursor mHoverCursor = new Cursor(Cursor.HAND_CURSOR);
	private boolean mHueGripped = false;
	private boolean mSLGripped = false;
	private Vector<HSLPanelListener> mListeners = new Vector<HSLPanelListener>();
	
	public HSLPanelComponent()
	{
		this.addMouseListener(this.createPrimaryMouseListener());
		this.addMouseMotionListener(this.createPrimaryMouseMotionListener());
		return;
	}
	
	@Override
	public void doLayout()
	{
		super.doLayout();
		this.mSLBounds.width = (int)Math.round(this.getWidth() / 2.0);
		this.mSLBounds.height = (int)Math.round(this.getHeight() / 2.0);
		this.mSLBounds.x = (int)Math.round((this.getWidth() - this.mSLBounds.width) / 2.0);
		this.mSLBounds.y = (int)Math.round((this.getHeight() - this.mSLBounds.height) / 2.0);
		return;
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		
		float centerX = this.getWidth() / 2.0F;
		float centerY = this.getHeight() / 2.0F;
		int offset = BORDER_THICKNESS + BORDER_SPACE;
		int thickness = (int)Math.round(this.mRingThickness * this.getWidth());
		float radius = centerX - (offset + (thickness / 2.0F));
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		this.renderBackground(g2d);
		this.renderHueRing(g2d, centerX, centerY, offset, thickness);
		this.renderSLGraph(g2d);
		this.renderHueCaret(g2d, centerX, centerY, radius);
		this.renderSLCaret(g2d);
		
		g2d.dispose();
		return;
	}
	
	private void renderBackground(Graphics2D g2d)
	{
		g2d.setPaint(new Color((this.mBackground >> 16) & 0xff, (this.mBackground >> 8) & 0xff, this.mBackground & 0xff));
		g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
		return;
	}
	
	private void renderHueRing(Graphics2D g2d, float centerX, float centerY, int offset, int thickness)
	{
		Ellipse2D.Float cone = new Ellipse2D.Float(offset, offset, this.getWidth() - (offset * 2), this.getWidth() - (offset * 2));
		Ellipse2D.Float clip = new Ellipse2D.Float(offset + thickness,
													offset + thickness,
													this.getWidth() - ((offset + thickness) * 2),
													this.getWidth() - ((offset + thickness) * 2));
		HSLPanelGradient gradient = new HSLPanelGradient(new Point2D.Double(centerX, centerY), CONE_FRACTIONS, CONE_COLORS);
		g2d.setPaint(gradient);
		g2d.fill(cone);
		g2d.setPaint(new Color((this.mBackground >> 16) & 0xff, (this.mBackground >> 8) & 0xff, this.mBackground & 0xff));
		g2d.fill(clip);
		return;
	}
	
	protected void renderSLGraph(Graphics2D g2d)
	{
		for(int y = 0; y < this.mSLBounds.height; y++)
		{
			for(int x = 0; x < this.mSLBounds.width; x++)
			{
				float hue = mModel.getCurrentHue();
				float sat = x / (float)this.mSLBounds.width;
				float lum = y / (float)this.mSLBounds.height;
				g2d.setPaint(mModel.getRGBfromHSL(hue, sat, lum));
				g2d.fillRect(this.mSLBounds.x + x, this.mSLBounds.y + y, 1, 1);
			}
		}
		return;
	}
	
	private void renderHueCaret(Graphics2D g2d, float centerX, float centerY, float radius)
	{
		float dirX = (float)Math.cos(mModel.getCurrentHue() * HSLPanelModel.PI2);
		float dirY = -(float)Math.sin(mModel.getCurrentHue() * HSLPanelModel.PI2);
		int x = (int)Math.round(centerX + (dirX * radius));
		int y = (int)Math.round(centerY + (dirY * radius));
		g2d.setStroke(mBottomStroke);
		g2d.setPaint(new Color((this.mCaretHighlight >> 16) & 0xff, (this.mCaretHighlight >> 8) & 0xff, this.mCaretHighlight & 0xff));
		g2d.drawOval(x - 5, y - 5, 10, 10);
		g2d.setStroke(mTopStroke);
		g2d.setPaint(new Color((this.mCaretShadow >> 16) & 0xff, (this.mCaretShadow >> 8) & 0xff, this.mCaretShadow & 0xff));
		g2d.drawOval(x - 4, y - 4, 8, 8);
		return;
	}
	
	private void renderSLCaret(Graphics2D g2d)
	{
		int x = this.mSLBounds.x + (int)Math.round(mModel.getCurrentSat() * (float)this.mSLBounds.width);
		int y = this.mSLBounds.y + (int)Math.round(mModel.getCurrentLum() * (float)this.mSLBounds.height);
		
		//System.out.println(x + ", " + y + ", " + mModel.getCurrentSat() + ", " + mModel.getCurrentLum());
		
		g2d.setStroke(mBottomStroke);
		g2d.setPaint(new Color((this.mCaretHighlight >> 16) & 0xff, (this.mCaretHighlight >> 8) & 0xff, this.mCaretHighlight & 0xff));
		g2d.drawOval(x - 5, y - 5, 10, 10);
		g2d.setStroke(mTopStroke);
		g2d.setPaint(new Color((this.mCaretShadow >> 16) & 0xff, (this.mCaretShadow >> 8) & 0xff, this.mCaretShadow & 0xff));
		g2d.drawOval(x - 4, y - 4, 8, 8);
		return;
	}
	
	public void setHue(int x, int y)
	{
		float angle = Math.abs(this.norm(this.getAngle((float)this.getBounds().getCenterX(), (float)this.getBounds().getCenterY(), x, y)));
		this.mModel.setCurrentHue(angle / HSLPanelModel.PI2);
		this.notifyAllHueChanged();
		return;
	}
	
	public void setHue(float hue)
	{
		this.mModel.setCurrentHue(hue);
		this.notifyAllHueChanged();
		return;
	}
	
	public float getHue()
	{
		return this.mModel.getCurrentHue();
	}
	
	public void setSaturation(int x)
	{
		x = this.mModel.clamp(this.mSLBounds.x, this.mSLBounds.x + this.mSLBounds.width, x);
		this.mModel.setCurrentSat((x - this.mSLBounds.x) / (float)this.mSLBounds.width);
		this.notifyAllSatChanged();
		return;
	}
	
	public void setSaturation(float saturation)
	{
		if(saturation < 0F || saturation > 1F){
			throw new IllegalArgumentException("Expecting normalized value.  Received: " + saturation);
		}
		this.mModel.setCurrentSat(saturation);
		this.notifyAllSatChanged();
		return;
	}
	
	public float getSaturation()
	{
		return this.mModel.getCurrentSat();
	}
	
	public void setLuminance(int y)
	{
		y = this.mModel.clamp(this.mSLBounds.y, this.mSLBounds.y + this.mSLBounds.height, y);
		this.mModel.setCurrentLum((y - this.mSLBounds.y) / (float)this.mSLBounds.height);
		this.notifyAllLumChanged();
		return;
	}
	
	public void setLuminance(float luminance)
	{
		if(luminance < 0F || luminance > 1F){
			throw new IllegalArgumentException("Expecting normalized value.  Received: " + luminance);
		}
		this.mModel.setCurrentLum(luminance);
		this.notifyAllLumChanged();
		return;
	}
	
	public float getLuminance()
	{
		return this.mModel.getCurrentLum();
	}
	
	public float getAngle(float x1, float y1, float x2, float y2)
	{
		return (float)Math.atan2(-(y2 - y1), x2 - x1);
	}
	
	public float norm(float angle)
	{
		angle = angle % HSLPanelModel.PI2;
		return angle = angle < 0 ? angle + HSLPanelModel.PI2 : angle;
	}
	
	public float distance(float x1, float y1, float x2, float y2)
	{
		return (float)Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
	}
	
	@Override
	public void setBackground(Color color)
	{
		super.setBackground(color);
		this.mBackground = color.getRed() << 16 | color.getGreen() << 8 | color.getBlue();
	}
	
	@Override
	public Color getBackground()
	{
		return new Color((this.mBackground >> 16) & 0xff, (this.mBackground >> 8) & 0xff, this.mBackground & 0xff);
	}
	
	public void setCaretHighlight(Color color)
	{
		this.mCaretHighlight = color.getRed() << 16 | color.getGreen() << 8 | color.getBlue();
	}
	
	public Color getCaretHighlight()
	{
		return new Color((this.mCaretHighlight >> 16) & 0xff, (this.mCaretHighlight >> 8) & 0xff, this.mCaretHighlight & 0xff);
	}
	
	public void setCaretShadow(Color color)
	{
		this.mCaretShadow = color.getRed() << 16 | color.getGreen() << 8 | color.getBlue();
	}
	
	public Color getCaretShadow()
	{
		return new Color((this.mCaretShadow >> 16) & 0xff, (this.mCaretShadow >> 8) & 0xff, this.mCaretShadow & 0xff);
	}
	
	public void setHueRingThicknessFactor(float factor)
	{
		if(factor < 0F || factor > 1F){
			throw new IllegalArgumentException("Expecting normalized value.  Received: " + factor);
		}
		this.mRingThickness = factor;
		return;
	}
	
	public float getHueRingThicknessFactor()
	{
		return this.mRingThickness;
	}
	
	public boolean getValueIsAdjusting()
	{
		return this.mHueGripped || this.mSLGripped;
	}
	
	public void addChangeListener(HSLPanelListener listener)
	{
		this.mListeners.add(listener);
		return;
	}
	
	public void removeChangeListener(HSLPanelListener listener)
	{
		this.mListeners.remove(listener);
		return;
	}
	
	public void notifyAllHueChanged()
	{
		for(HSLPanelListener listener : this.mListeners)
		{
			listener.hueChanged(this.mModel.getCurrentHue());
		}
		return;
	}
	
	public void notifyAllSatChanged()
	{
		for(HSLPanelListener listener : this.mListeners)
		{
			listener.saturationChanged(this.mModel.getCurrentSat());
		}
		return;
	}
	
	public void notifyAllLumChanged()
	{
		for(HSLPanelListener listener : this.mListeners)
		{
			listener.luminanceChanged(this.mModel.getCurrentLum());
		}
		return;
	}
	
	private MouseListener createPrimaryMouseListener()
	{
		return new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if(e.getPoint() == null){
					return;
				}
				if(mSLBounds.contains(e.getPoint())){
					mSLGripped = true;
					mHueGripped = false;
					setSaturation(e.getPoint().x);
					setLuminance(e.getPoint().y);
				}else{
					mHueGripped = true;
					mSLGripped = false;
					setHue(e.getX(), e.getY());
				}
				HSLPanelComponent.this.repaint();
				return;
			}
			
			@Override
			public void mouseReleased(MouseEvent e)
			{
				mHueGripped = false;
				mSLGripped = false;
				notifyAllHueChanged();
				notifyAllSatChanged();
				notifyAllLumChanged();
				HSLPanelComponent.this.repaint();
				return;
			}
		};
	}
	
	private MouseMotionListener createPrimaryMouseMotionListener()
	{
		return new MouseMotionListener()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{
				if(e.getPoint() == null){
					return;
				}
				if(mSLGripped){
					setSaturation(e.getPoint().x);
					setLuminance(e.getPoint().y);
				}else if(mHueGripped){
					setHue(e.getX(), e.getY());
				}
				HSLPanelComponent.this.repaint();
				return;
			}
			
			@Override
			public void mouseMoved(MouseEvent e)
			{
				if(e.getPoint() == null){
					return;
				}
				float centerX = HSLPanelComponent.this.getWidth() / 2.0F;
				float centerY = HSLPanelComponent.this.getHeight() / 2.0F;
				int offset = BORDER_THICKNESS + BORDER_SPACE;
				int thickness = (int)Math.round(mRingThickness * HSLPanelComponent.this.getWidth());
				float radius = centerX - (offset + (thickness / 2.0F));
				float dirX = (float)Math.cos(mModel.getCurrentHue() * HSLPanelModel.PI2);
				float dirY = -(float)Math.sin(mModel.getCurrentHue() * HSLPanelModel.PI2);
				int hueX = (int)Math.round(centerX + (dirX * radius));
				int hueY = (int)Math.round(centerY + (dirY * radius));
				int slX = mSLBounds.x + (int)Math.round(mModel.getCurrentSat() * (float)mSLBounds.width);
				int slY = mSLBounds.y + (int)Math.round(mModel.getCurrentLum() * (float)mSLBounds.height);
				if(distance(e.getX(), e.getY(), hueX, hueY) <= 5 || distance(e.getX(), e.getY(), slX, slY) <= 5){
					HSLPanelComponent.this.setCursor(mHoverCursor);
				}else{
					HSLPanelComponent.this.setCursor(mNormalCursor);
				}
				return;
			}
		};
	}
	
	
}
