package ua.stu.view.scpview;

import ua.stu.scplib.attribute.GraphicAttribute;
import ua.stu.scplib.attribute.GraphicAttributeBase;
import ua.stu.scplib.data.DataHandler;
import ua.stu.view.fragments.ECGPanelFragment;
import and.awt.Color;
import and.awt.geom.GeneralPath;
import and.awt.geom.Line2D;
import and.awt.geom.Rectangle2D;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.Scroller;
import net.pbdavey.awt.AwtView;
import net.pbdavey.awt.Font;
import net.pbdavey.awt.Graphics2D;
import net.pbdavey.awt.RenderingHints;

public class GraphicView extends AwtView {
	private DataHandler h=null;
	public void setH(DataHandler h) {
		this.h = h;
	}

	private final GestureDetector gestureDetector;	
	private final Scroller scroller;
	private int W;
	private int H;
	private int SW;
	public void setSW(int sW) {
		SW = sW;
	}

	public void setSH(int sH) {
		SH = sH;
	}

	private int SH;
	public int getW() {
		return W;
	}

	public void setW(int w) {
		W = w;
	}

	public int getH() {
		return H;
	}

	public void setH(int h) {
		H = h;
	}

	//duim constant
	private float duim = (float) 25.4;
	// screen size in dpi
	private int sizeScreen = 240;
	//set of graphic attributes
	private GraphicAttributeBase g;
	//the number of tiles to display per column
	private int nTilesPerColumn = 12;
	//the number of tiles to display per row (if 1, then nTilesPerColumn should == numberOfChannels)
	private int nTilesPerRow = 1;
	//how may pixels to use to represent one millivolt
	private float yPixelsInMillivolts;
	//how may pixels to use to represent one millisecond
	private float xPixelsInMilliseconds;
	//how much of the sample data to skip, specified in milliseconds from the start of the samples
	private float timeOffsetInMilliSeconds;
	//offset for graphic
	private int xTitlesOffset;
	Color backgroundColor = Color.white;
	Color curveColor = Color.blue;
	Color boxColor = Color.black;
	Color gridColor = Color.black;
	Color channelNameColor = Color.black;
	//Basic font
	Font font = new Font("Ubuntu",0,14);
	private boolean fillBackgroundFirst;
	////////////////////
	////scroll
	//////////////////////////////////////////////////
	public GraphicView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
				gestureDetector = new GestureDetector(context, new MyGestureListener());
				 scroller = new Scroller(context);
				// init scrollbars
		        setHorizontalScrollBarEnabled(true);
		        setVerticalScrollBarEnabled(true);

		        TypedArray a = context.obtainStyledAttributes(R.styleable.View);
		        initializeScrollbars(a);
		        a.recycle();
	}
	
	public GraphicView(Context context, AttributeSet attribSet) {
		super(context, attribSet);
		// TODO Auto-generated constructor stub
				gestureDetector = new GestureDetector(context, new MyGestureListener());
				 scroller = new Scroller(context);
				// init scrollbars
		        setHorizontalScrollBarEnabled(true);
		        setVerticalScrollBarEnabled(true);

		        TypedArray a = context.obtainStyledAttributes(R.styleable.View);
		        initializeScrollbars(a);
		        a.recycle();
	}
	@Override
    public boolean onTouchEvent(MotionEvent event)
    {
	
		  // check for tap and cancel fling
	    if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN)
	    {
	        if (!scroller.isFinished()) scroller.abortAnimation();
	    }

	    if (gestureDetector.onTouchEvent(event)) return true;

	    // check for pointer release 
	    if ((event.getPointerCount() == 1) && ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP))
	    {
	        int newScrollX = getScrollX();
	        if (getScrollX() < 0) newScrollX = 0;
	        else if (getScrollX() > SW - getWidth()) newScrollX = SW - getWidth();

	        int newScrollY = getScrollY();
	        if (getScrollY() < 0) newScrollY = 0;
	        else if (getScrollY() > SH - getHeight()) newScrollY = SH - getHeight();

	        if ((newScrollX != getScrollX()) || (newScrollY != getScrollY()))
	        {
	            scroller.startScroll(getScrollX(), getScrollY(), newScrollX - getScrollX(), newScrollY - getScrollY());
	            awakenScrollBars(scroller.getDuration());
	        }
	    }

	    return true;
    }
	 @Override
	    protected int computeHorizontalScrollRange()
	    {
	        return SW;
	    }

	    @Override
	    protected int computeVerticalScrollRange()
	    {
	        return SH;
	    }
	    @Override
	    public void computeScroll()
	    {
	        if (scroller.computeScrollOffset())
	        {
	            int oldX = getScrollX();
	            int oldY = getScrollY();
	            int x = scroller.getCurrX();
	            int y = scroller.getCurrY();
	            scrollTo(x, y);
	            if (oldX != getScrollX() || oldY != getScrollY())
	            {
	                onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);
	            }

	            postInvalidate();
	        }
	    }

	    private class MyGestureListener extends SimpleOnGestureListener
	    {
	        @Override
	        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
	        {
	        	 boolean scrollBeyondImage = ((getScrollX() < 0) || (getScrollX() > SW) || (getScrollY() < 0) || (getScrollY() > SH));
	        	    if (scrollBeyondImage) return false;

	        	    scroller.fling(getScrollX(), getScrollY(), -(int)velocityX, -(int)velocityY, 0, SW - getWidth(), 0, SH - getHeight());
	        	    awakenScrollBars(scroller.getDuration());

	        	    return true;
	        }
	    }
	////////
	//end scroll
	///////////////////////////////////////////////////
	void setGraphicParameters(GraphicAttribute g) {
		this.g = g;
	}
	
	//default = 25
	public void setXScale(int millimetersPerSecond) {
		this.xPixelsInMilliseconds = millimetersPerSecond/(1000*duim/sizeScreen);
		this.xTitlesOffset = millimetersPerSecond*3;
	}
	
	//default = 10
	public void setYScale(int millimetersPerMillivolt) {
		this.yPixelsInMillivolts = millimetersPerMillivolt/(duim/sizeScreen);
	}
	
	public int getXSCale() {
		return 0;
	}
	
	public int getYScale() {
		return 0;
	}
	
	public void init() {
		
		//DataHandler h = new DataHandler("/mnt/sdcard/Example.scp");
		//g = h.getGraphic();
		if (h!=null)
		g=h.getGraphic();
		setXScale(12);
		setYScale(5);
	}

	/**
	 * @param	g2
	 * @param	r
	 * @param	fillBackgroundFirst
	 */
	@Override
	public void paint(Graphics2D g2) {
		init();
		int channelNameXOffset = 10;
		int channelNameYOffset = 20;
		
		g2.setBackground(backgroundColor);
		g2.setColor(backgroundColor);
		setBackground(backgroundColor);
		//setW(getWidth());
		//setH(getHeight());
		setW(600);
		setH(600);
		setSH(getH());
		setSW(getW());
		if (fillBackgroundFirst) {
			g2.fill(new Rectangle2D.Float(0,0,getW(),getH()));
		}
		
		float widthOfTileInPixels = getW()/nTilesPerRow;
		float heightOfTileInPixels = getH()/nTilesPerColumn;
		
		float widthOfTileInMilliSeconds = widthOfTileInPixels/xPixelsInMilliseconds;
		float heightOfTileInMilliVolts =  heightOfTileInPixels/yPixelsInMillivolts;

		// first draw boxes around each tile, with anti-aliasing turned on (only way to get consistent thickness)
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setColor(gridColor);
		float drawingOffsetY = 0;
		for (int row=0;row<nTilesPerColumn;++row) {
			float drawingOffsetX = xTitlesOffset;
			for (int col=0;col<nTilesPerRow;++col) {
				//g2.setStroke(new BasicStroke(gridWidth));				
				for (float time=0; time<widthOfTileInMilliSeconds; time+=200) {
					float x = drawingOffsetX+time*xPixelsInMilliseconds;
					g2.draw(new Line2D.Float(x,drawingOffsetY,x,drawingOffsetY+heightOfTileInPixels));
				}

				//g2.setStroke(new BasicStroke(gridWidth));
				for (float milliVolts=-heightOfTileInMilliVolts/2; milliVolts<=heightOfTileInMilliVolts/2; milliVolts+=0.5) {
					float y = drawingOffsetY + heightOfTileInPixels/2 + milliVolts/heightOfTileInMilliVolts*heightOfTileInPixels;
					//g2.draw(new Line2D.Float(drawingOffsetX,y,drawingOffsetX+widthOfTileInPixels,y));
				}
				drawingOffsetX+=widthOfTileInPixels;
			}
			drawingOffsetY+=heightOfTileInPixels;
		}

		g2.setColor(boxColor);
		 //setForeground(boxColor);
		//g2.setStroke(new BasicStroke(boxWidth));

		drawingOffsetY = 0;
		int channel=0;
		for (int row=0;row<nTilesPerColumn;++row) {
			float drawingOffsetX = 0;
			for (int col=0;col<nTilesPerRow;++col) {
				// Just drawing each bounding line once doesn't seem to help them sometimes
				// being thicker than others ... is this a stroke width problem (better if anti-aliasing on, but then too slow) ?
				//g2d.draw(new Rectangle2D.Double(drawingOffsetX,drawingOffsetY,drawingOffsetX+widthOfTile-1,drawingOffsetY+heightOfTile-1));
				if (row == 0)
					g2.draw(new Line2D.Float(drawingOffsetX,drawingOffsetY,drawingOffsetX+widthOfTileInPixels,drawingOffsetY));					// top
				if (col == 0)
					g2.draw(new Line2D.Float(drawingOffsetX,drawingOffsetY,drawingOffsetX,drawingOffsetY+heightOfTileInPixels));					// left
				g2.draw(new Line2D.Float(drawingOffsetX,drawingOffsetY+heightOfTileInPixels,drawingOffsetX+widthOfTileInPixels,drawingOffsetY+heightOfTileInPixels));	// bottom
				g2.draw(new Line2D.Float(drawingOffsetX+widthOfTileInPixels,drawingOffsetY,drawingOffsetX+widthOfTileInPixels,drawingOffsetY+heightOfTileInPixels));	// right
				
				if (g.getChannelNames() != null && channel < g.getDisplaySequence().length && 
						g.getDisplaySequence()[channel] < g.getChannelNames().length) {
					String channelName=g.getChannelNames()[g.getDisplaySequence()[channel]];
					if (channelName != null) {
						g2.setColor(channelNameColor);
						g2.setFont(font);
						g2.drawString(channelName,drawingOffsetX+channelNameXOffset,drawingOffsetY+channelNameYOffset);
					}
				}
				
				drawingOffsetX+=widthOfTileInPixels;
				++channel;
			}
			drawingOffsetY+=heightOfTileInPixels;
		}

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);	// ugly without

		g2.setColor(curveColor);
		//setForeground(curveColor);
		//g2.setStroke(new BasicStroke(curveWidth));
		float interceptY = heightOfTileInPixels/2;
		System.out.println("SamplingIntervalInMilliSeconds");
		System.out.println(g.getSamplingIntervalInMilliSeconds());
		System.out.println("timeOffsetInMilliSeconds");
		System.out.println(timeOffsetInMilliSeconds);
		float widthOfSampleInPixels=g.getSamplingIntervalInMilliSeconds()*xPixelsInMilliseconds;
		int timeOffsetInSamples = (int)(timeOffsetInMilliSeconds/g.getSamplingIntervalInMilliSeconds());
		int widthOfTileInSamples = (int)(widthOfTileInMilliSeconds/g.getSamplingIntervalInMilliSeconds());
		int usableSamples = g.getNumberOfSamplesPerChannel()-timeOffsetInSamples;
		if (usableSamples <= 0) {
			//usableSamples=0;
			return;
		}
		else if (usableSamples > widthOfTileInSamples) {
			usableSamples=widthOfTileInSamples-1;
		}

		drawingOffsetY = 10;
	 channel = 0;
		GeneralPath thePath = new GeneralPath();
		for (int row=0;row<nTilesPerColumn && channel<g.getNumberOfChannels();++row) {
			float drawingOffsetX = xTitlesOffset;
			System.out.println("samplesForThisChannel ");
			for (int col=0;col<nTilesPerRow && channel<g.getNumberOfChannels();++col) {
				float yOffset = drawingOffsetY + interceptY;
				short[] samplesForThisChannel = g.getSamples()[g.getDisplaySequence()[channel]];				
				
				int i = timeOffsetInSamples;
				
				float rescaleY = g.getAmplitudeScalingFactorInMilliVolts()[g.getDisplaySequence()[channel]]*yPixelsInMillivolts;
				float fromXValue = drawingOffsetX;
				System.out.print(samplesForThisChannel[i]);
				System.out.print(";");
				System.out.print(i);
				System.out.print(";");
				float fromYValue = yOffset - samplesForThisChannel[i]*rescaleY;
				thePath.reset();
				thePath.moveTo(fromXValue,fromYValue);
				++i;
				for (int j=1;j<usableSamples;++j) {
					float toXValue = fromXValue + widthOfSampleInPixels;
					float toYValue = yOffset - samplesForThisChannel[i]*rescaleY;
					i++;
					if ((int)fromXValue != (int)toXValue || (int)fromYValue != (int)toYValue) {
						thePath.lineTo(toXValue,toYValue);
					}
					fromXValue=toXValue;
					fromYValue=toYValue;
				}
				g2.draw(thePath);
				drawingOffsetX+=widthOfTileInPixels;
				++channel;
			}
			drawingOffsetY+=heightOfTileInPixels;
		}
		
		return;
	}

	public GraphicAttributeBase getG() {
		return g;
	}

	public void setG(GraphicAttributeBase g) {
		this.g = g;
	}

	public int getnTilesPerColumn() {
		return nTilesPerColumn;
	}

	public void setnTilesPerColumn(int nTilesPerColumn) {
		this.nTilesPerColumn = nTilesPerColumn;
	}

	public int getnTilesPerRow() {
		return nTilesPerRow;
	}

	public void setnTilesPerRow(int nTilesPerRow) {
		this.nTilesPerRow = nTilesPerRow;
	}

	public float getyPixelsInMillivolts() {
		return yPixelsInMillivolts;
	}

	public void setyPixelsInMillivolts(int yPixelsInMillivolts) {
		this.yPixelsInMillivolts = yPixelsInMillivolts;
	}

	public float getxPixelsInMilliseconds() {
		return xPixelsInMilliseconds;
	}

	public void setxPixelsInMilliseconds(int xPixelsInMilliseconds) {
		this.xPixelsInMilliseconds = xPixelsInMilliseconds;
	}

	public float getTimeOffsetInMilliSeconds() {
		return timeOffsetInMilliSeconds;
	}

	public void setTimeOffsetInMilliSeconds(float timeOffsetInMilliSeconds) {
		this.timeOffsetInMilliSeconds = timeOffsetInMilliSeconds;
	}

	/*public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}*/

	public boolean isFillBackgroundFirst() {
		return fillBackgroundFirst;
	}

	public void setFillBackgroundFirst(boolean fillBackgroundFirst) {
		this.fillBackgroundFirst = fillBackgroundFirst;
	}
	
	public void setFont(Font font) {
		this.font = font;
	}
	
	public void setFont(String fontName) {
		this.font = new Font(fontName,0,14);
	}
	
}