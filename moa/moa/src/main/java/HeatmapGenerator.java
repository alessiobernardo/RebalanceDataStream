import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.xy.AbstractXYZDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;


public class HeatmapGenerator extends ApplicationFrame{
	

	public HeatmapGenerator(double[][] matrix, String title) {
		super("Heatmap");
		
        final JFreeChart chart = createChart(matrix,title);
        
        String dest = "plots/heatMap-" + title + ".png";               
        File imageFile = new File(dest);
	    int width = 1500;
	    int height = 500;
	    
	    try {
	        ChartUtilities.saveChartAsPNG(imageFile,chart, width, height);
	    } catch (IOException ex) {
	        System.err.println(ex);
	    }
	    
	    dest = "plots/heatMap-" + title + "_min.png";               
        imageFile = new File(dest);
	    try {
	        ChartUtilities.saveChartAsPNG(imageFile,chart, 750, height);
	    } catch (IOException ex) {
	        System.err.println(ex);
	    }
        
	}
	
	
	private static JFreeChart createChart(double[][] matrix, String title){
    	
		int[] m = {20000,22500,25000,27500,30000};
		int[] v = {50,100,200,400};
		
		XYZDataset dataset = new XYZArrayDataset(matrix);
    	String t = "HeatMap " + title;
    	    	
    	//x-axis -> row matrix
    	String labelsX[] = new String[5];
    	for (int i = 0; i < m.length; i++)
            labelsX[i] = Integer.toString(m[i]);
        SymbolAxis xAxis = new SymbolAxis("Mean", labelsX);
       
        //y-axis -> column matrix
        String labelsY[] = new String[4];
        for (int i = 0; i < v.length; i++)
            labelsY[i] = Integer.toString(v[i]);
        SymbolAxis yAxis = new SymbolAxis("Variance", labelsY);
        
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, new XYBlockRenderer());
        XYBlockRenderer r = new XYBlockRenderer();
        
        LookupPaintScale ps = new LookupPaintScale(-0.1,0.5,new Color(192,192,255));   
        
		ps.add(-0.04,new Color(255,192,192));
		ps.add(-0.03,new Color(255,128,128));
		ps.add(-0.02,new Color(255,0,0));
		ps.add(-0.01,new Color(192,0,0));
		
		ps.add(0.00,new Color(128,255,128));
		ps.add(0.01,new Color(64,255,64));
		ps.add(0.02,new Color(0,224,0));
		ps.add(0.03,new Color(0,160,0));
		ps.add(0.04,new Color(0,96,0));
		
//		ps.add(0.05,new Color(192,192,255));
//		ps.add(0.03,new Color(128,128,255));
//		ps.add(0.04,new Color(0,0,255));
//		ps.add(0.05,new Color(0,0,192));
//		ps.add(0.06,new Color(0,0,128));
		
        r.setPaintScale(ps);
        r.setBlockHeight(1.0f);
        r.setBlockWidth(1.0f);
        
        // sets paint color for plot outlines
	    plot.setOutlinePaint(Color.BLUE);
	    plot.setOutlineStroke(new BasicStroke(2.0f));
        
        plot.setRenderer(r);        

        JFreeChart chart = new JFreeChart(t,JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        
        NumberAxis scaleAxis = new NumberAxis("Scale");
        scaleAxis.setUpperBound(100);
        scaleAxis.setAxisLinePaint(Color.white);
        scaleAxis.setTickMarkPaint(Color.white);
        scaleAxis.setTickLabelFont(new Font("Dialog", Font.PLAIN, 12));
        
        PaintScaleLegend legend = new PaintScaleLegend(ps,scaleAxis);
        legend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        legend.setPadding(new RectangleInsets(5, 5, 5, 5));
        legend.setStripWidth(50);
        legend.setPosition(RectangleEdge.RIGHT);
        legend.setBackgroundPaint(Color.WHITE);
        chart.addSubtitle(legend);
        chart.setBackgroundPaint(Color.white);
        
        return chart;       
    }
	
	private static class XYZArrayDataset extends AbstractXYZDataset{
		double[][] data;
		int rowCount = 0;
		int columnCount = 0;
		
		XYZArrayDataset(double[][] data){
			this.data = data;
			rowCount = data.length;
			columnCount = data[0].length;
		}
		public int getSeriesCount(){
			return 1;
		}
		public Comparable getSeriesKey(int series){
			return "serie";
		}
		public int getItemCount(int series){
			return rowCount*columnCount;
		}
		public double getXValue(int series,int item){
			return (int)(item/columnCount);
		}
		public double getYValue(int series,int item){
			return item % columnCount;
		}
		public double getZValue(int series,int item){
			return data[(int)(item/columnCount)][item % columnCount];
		}
		public Number getX(int series,int item){
			return new Double((int)(item/columnCount));
		}
		public Number getY(int series,int item){
			return new Double(item % columnCount);
		}
		public Number getZ(int series,int item){
			return new Double(data[(int)(item/columnCount)][item % columnCount]);
		}
	}
	
}
