package com.merzadyan.ui;

import com.drew.lang.annotations.NotNull;
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
import org.apache.log4j.Logger;

import java.time.LocalDate;

public class ChartWindow extends Application {
    private static final Logger LOGGER = Logger.getLogger(ChartWindow.class.getName());
    
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
    
    @FXML
    void initData(@NotNull Stock stock) {
        if (lineChart == null) {
            LOGGER.debug("lineChart: null");
            lineChart = new LineChart<>(lineChartXAxis, lineChartYAxis);
        }
        
        lineChart.setTitle(stock.getCompany());
        
        // IMPORTANT NOTE: The time measurement unit is undecided - waiting to determine the life-span of a prediction.
        lineChartXAxis.setLabel("Date");
        
        // Define the series.
        // TODO: with each crawl for date period, add the data to this chart.
        // TODO: maybe look into doing a trend line using historic values?
        XYChart.Series sentimentValueSeries = new XYChart.Series();
        sentimentValueSeries.setName("Sentiment Value");
        LocalDate sld = stock.getStartDate(),
                eld = stock.getEndDate();
        String s = sld.getDayOfMonth() + "-" + sld.getMonthValue() + "-" + sld.getYear(),
                e = eld.getDayOfMonth() + "-" + eld.getMonthValue() + "-" + eld.getYear(),
                interval = s.concat(" to " + e);
        
        sentimentValueSeries.getData().add(new XYChart.Data(interval, stock.getLatestSentimentScore()));
        
        lineChart.getData().add(sentimentValueSeries);
        
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
        // Stock s = new Stock();
        // s.setCompany("Bae Systems");
        // s.setSymbol("BA.");
        // s.setStockExchange("LSE");
        // s.setLatestSentimentScore(4);
        // s.setHistogram(new int[]{0, 4, 0, 0, 0});
        // s.setStartDate(LocalDate.parse("2018-03-01"));
        // s.setEndDate(LocalDate.parse("2018-03-08"));
        // initData(s);
    }
}
