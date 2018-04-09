package com.merzadyan.ui;

import com.merzadyan.Stock;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class ChartWindow extends Application {
    @FXML
    private LineChart<String, Number> lineChart;
    private CategoryAxis lineChartXAxis = new CategoryAxis();
    private NumberAxis lineChartYAxis = new NumberAxis();
    
    @FXML
    private BarChart<Number, Number> barChart;
    private CategoryAxis barChartXAxis = new CategoryAxis();
    private NumberAxis barChartYAxis = new NumberAxis();
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        setUserAgentStylesheet(STYLESHEET_MODENA);
        
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("chart.fxml")));
        primaryStage.setScene(scene);
        // Disable resizing ability of the window.
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    
    void initData(Stock stock) {
        lineChart.setTitle(stock.getCompany());
        
        // IMPORTANT NOTE: The time measurement unit is undecided - waiting to determine the life-span of a prediction.
        lineChartXAxis.setLabel("Month");
        
        // Define the series.
        XYChart.Series marketValueSeries = new XYChart.Series();
        marketValueSeries.setName("Market Value");
        marketValueSeries.getData().add(new XYChart.Data("Jan", 23));
        marketValueSeries.getData().add(new XYChart.Data("Feb", 14));
        marketValueSeries.getData().add(new XYChart.Data("Mar", 15));
        marketValueSeries.getData().add(new XYChart.Data("Apr", 24));
        marketValueSeries.getData().add(new XYChart.Data("May", 34));
        marketValueSeries.getData().add(new XYChart.Data("Jun", 36));
        marketValueSeries.getData().add(new XYChart.Data("Jul", 22));
        marketValueSeries.getData().add(new XYChart.Data("Aug", 45));
        marketValueSeries.getData().add(new XYChart.Data("Sep", 43));
        marketValueSeries.getData().add(new XYChart.Data("Oct", 17));
        marketValueSeries.getData().add(new XYChart.Data("Nov", 29));
        marketValueSeries.getData().add(new XYChart.Data("Dec", 25));
        
        XYChart.Series sentimentValueSeries = new XYChart.Series();
        sentimentValueSeries.setName("Sentiment Value");
        sentimentValueSeries.getData().add(new XYChart.Data("Jan", 33));
        sentimentValueSeries.getData().add(new XYChart.Data("Feb", 34));
        sentimentValueSeries.getData().add(new XYChart.Data("Mar", 25));
        sentimentValueSeries.getData().add(new XYChart.Data("Apr", 44));
        sentimentValueSeries.getData().add(new XYChart.Data("May", 39));
        sentimentValueSeries.getData().add(new XYChart.Data("Jun", 16));
        sentimentValueSeries.getData().add(new XYChart.Data("Jul", 55));
        sentimentValueSeries.getData().add(new XYChart.Data("Aug", 54));
        sentimentValueSeries.getData().add(new XYChart.Data("Sep", 48));
        sentimentValueSeries.getData().add(new XYChart.Data("Oct", 27));
        sentimentValueSeries.getData().add(new XYChart.Data("Nov", 37));
        sentimentValueSeries.getData().add(new XYChart.Data("Dec", 29));
        
        lineChart.getYAxis().setTickLabelsVisible(false);
        lineChart.getYAxis().setOpacity(0);
        lineChart.getData().addAll(marketValueSeries, sentimentValueSeries);
        
        barChart.setTitle(stock.getCompany());
        barChartXAxis.setLabel("Distribution");
        barChartYAxis.setLabel("Frequency");
        barChart.setBarGap(0);
        barChart.setCategoryGap(0);
        
        XYChart.Series series = new XYChart.Series();
        series.setName("Sentiment Value");
        
        series.getData().add(new XYChart.Data<>("Negative", stock.getHistogram()[0]));
        series.getData().add(new XYChart.Data<>("Somewhat negative", stock.getHistogram()[1]));
        series.getData().add(new XYChart.Data<>("Neutral", stock.getHistogram()[2]));
        series.getData().add(new XYChart.Data<>("Somewhat positive", stock.getHistogram()[3]));
        series.getData().add(new XYChart.Data<>("Positive", stock.getHistogram()[4]));
        barChart.getData().addAll(series);
    }
    
    
    @FXML
    public void initialize() {
    
    }
}
