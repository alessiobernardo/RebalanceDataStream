import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

public class ResultsVisualizator extends JFrame{
	
	private static final long serialVersionUID = 1L;

	// the constructor will contain the panel of a certain size and the close operations
	public ResultsVisualizator(ArrayList<ArrayList<Double>> rebalanceStreamActualResults, 
							   ArrayList<ArrayList<Double>> baseActualResults, 
							   ArrayList<ArrayList<Double>> finalActualResults,
							   ArrayList<ArrayList<Double>> levelActualResults,
			  				   int mean, int var){    
		
		// calls the super class constructor
	    super("XY Line Chart Example with JFreechart"); 
	    
	    createChartPanel(rebalanceStreamActualResults,baseActualResults,finalActualResults,levelActualResults,mean,var);
	    
	}
	
	// this method will create the chart panel containing the graph
	private void createChartPanel(ArrayList<ArrayList<Double>> rebalanceStreamActualResults, 
			   					  ArrayList<ArrayList<Double>> baseActualResults, 
			   					  ArrayList<ArrayList<Double>> finalActualResults,
			   					  ArrayList<ArrayList<Double>> levelActualResults,
			   					  int mean, int var) {  
	    
		String chartTitle = "K statistic graph";
	    String xAxisLabel = "N elements";
	    String yAxisLabel = "K statistic T";
		
//		String chartTitle = "";
//	    String xAxisLabel = "";
//	    String yAxisLabel = "";
	    
	    XYDataset dataset = createDataset(rebalanceStreamActualResults,baseActualResults,finalActualResults,levelActualResults);
	    								 	    
	    JFreeChart chart = ChartFactory.createXYLineChart(chartTitle,xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, false, false);
	    
	    //chart.removeLegend();	 
	    
	    customizeChart(chart);
	    
	    // saves the chart as an image files
	    File imageFile = new File("plots/" + mean + "_" + var + ".png");
	    int width = 1500;
	    int height = 500;
	    
	    try {
	        ChartUtilities.saveChartAsPNG(imageFile, chart, width, height);
	    } catch (IOException ex) {
	        System.err.println(ex);
	    }
	}
	
	// this method creates the data as time series
	private XYDataset createDataset(ArrayList<ArrayList<Double>> rebalanceStreamActualResults, 
				  					ArrayList<ArrayList<Double>> baseActualResults, 
				  					ArrayList<ArrayList<Double>> finalActualResults,
				  					ArrayList<ArrayList<Double>> levelActualResults) {  
					
		YIntervalSeriesCollection dataset = new YIntervalSeriesCollection();
		
	    YIntervalSeries series1 = new YIntervalSeries("Base");
	    YIntervalSeries series2 = new YIntervalSeries("RebalanceStream");
	    YIntervalSeries series3 = new YIntervalSeries("RebalanceStreamPlus");
	    YIntervalSeries series4 = new YIntervalSeries("Imbalance level");
	   
		for (int i = 0; i < rebalanceStreamActualResults.get(0).size(); i++) {
			series1.add((i + 1) * 2000, baseActualResults.get(0).get(i), baseActualResults.get(1).get(i), baseActualResults.get(2).get(i));
			series2.add((i + 1) * 2000, rebalanceStreamActualResults.get(0).get(i), rebalanceStreamActualResults.get(1).get(i), rebalanceStreamActualResults.get(2).get(i));			
			series3.add((i + 1) * 2000, finalActualResults.get(0).get(i), finalActualResults.get(1).get(i), finalActualResults.get(2).get(i));
		}
		
		for (int i = 0; i < levelActualResults.get(0).size(); i++) {
			series4.add((i + 1), levelActualResults.get(0).get(i), levelActualResults.get(1).get(i), levelActualResults.get(2).get(i));			
		}

		dataset.addSeries(series1);
		dataset.addSeries(series2);
		dataset.addSeries(series3);
		dataset.addSeries(series4);
	    
	    return dataset;
	}

	// here we make some customization
	private void customizeChart(JFreeChart chart) {  
	    XYPlot plot = chart.getXYPlot();
	    XYErrorRenderer renderer = new XYErrorRenderer();
	    
	    renderer.setSeriesLinesVisible(0, false);
		renderer.setSeriesShapesVisible(0, true);
	    
	    // set y range
	    ValueAxis rangeAxis = plot.getRangeAxis();
	    rangeAxis.setRange(-0.20, 1.05);
	    ((NumberAxis) rangeAxis).setTickUnit(new NumberTickUnit(0.05));
	    
//	    rangeAxis.setVisible(false);
//	    ValueAxis domain = plot.getDomainAxis();
//	    domain.setVisible(false);
	    
	    // sets paint color for each series
	    renderer.setSeriesPaint(0, Color.RED);
	    renderer.setSeriesPaint(1, Color.BLACK);
	    renderer.setSeriesPaint(2, Color.BLUE);	  
	    renderer.setSeriesPaint(3, Color.WHITE);
	
	    // sets thickness for series (using strokes)	 
	    renderer.setSeriesStroke(0, new BasicStroke(0.5f));
	    renderer.setSeriesStroke(1, new BasicStroke(0.5f));
	    renderer.setSeriesStroke(2, new BasicStroke(0.5f));
	    renderer.setSeriesStroke(3, new BasicStroke(0.5f));
	   	   
	    // sets paint color for plot outlines
	    plot.setOutlinePaint(Color.BLUE);
	    plot.setOutlineStroke(new BasicStroke(2.0f));
	    
	    // sets renderer for lines
	    plot.setRenderer(renderer);
	    
	    // sets plot background
	    plot.setBackgroundPaint(Color.LIGHT_GRAY);
	    
	    // sets paint color for the grid lines
	    plot.setRangeGridlinesVisible(true);
	    plot.setRangeGridlinePaint(Color.BLACK);
	    
	    plot.setDomainGridlinesVisible(true);
	    plot.setDomainGridlinePaint(Color.BLACK);
	    
	}
	
}
