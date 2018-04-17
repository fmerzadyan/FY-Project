package com.merzadyan.ui;

import com.drew.lang.annotations.NotNull;
import com.merzadyan.FileOp;
import com.merzadyan.stock.Stock;
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
import java.util.ArrayList;
import java.util.Objects;

@SuppressWarnings("unchecked")
public class ChartWindow extends Application {
    
    @FXML
    private LineChart<String, Number> lineChart;
    
    @FXML
    private BarChart<Number, Number> barChart;
    private final CategoryAxis barChartXAxis = new CategoryAxis();
    private final NumberAxis barChartYAxis = new NumberAxis();
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        setUserAgentStylesheet(STYLESHEET_MODENA);
        
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/layout/chart.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setScene(scene);
        // Disable resizing ability of the window.
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    
    @FXML
    void initData(@NotNull ArrayList<Stock> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        
        XYChart.Series sentimentValueSeries = new XYChart.Series();
        sentimentValueSeries.setName("Sentiment Value");
        
        Stock latestStockData = null;
        for (Stock stock : list) {
            if (latestStockData == null) {
                latestStockData = stock;
            } else if (stock.getEndDate().isAfter(latestStockData.getEndDate())) {
                latestStockData = stock;
            }
            
            if (FileOp.isNullOrEmpty(stock.getCompany()) || stock.getStartDate() == null ||
                    stock.getEndDate() == null) {
                // Skip iteration if the required values are null.
                // For precaution - should not be the case that this ever happens.
                continue;
            }
            
            try {
                LocalDate sld = stock.getStartDate(),
                        eld = stock.getEndDate();
                String s = sld.getDayOfMonth() + "-" + sld.getMonthValue() + "-" + sld.getYear(),
                        e = eld.getDayOfMonth() + "-" + eld.getMonthValue() + "-" + eld.getYear(),
                        interval = s.concat(" to " + e);
                
                sentimentValueSeries.getData().add(new XYChart.Data(interval, stock.getLatestSentimentScore()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        lineChart.getData().addAll(sentimentValueSeries);
        
        
        barChart.setTitle(Objects.requireNonNull(latestStockData).getCompany());
        barChartXAxis.setLabel("Distribution");
        barChartYAxis.setLabel("Frequency");
        barChart.setBarGap(0);
        barChart.setCategoryGap(0);
        
        XYChart.Series series = new XYChart.Series();
        series.setName("Sentiment Value");
        
        series.getData().add(new XYChart.Data<>("Negative", latestStockData.getHistogram()[0]));
        series.getData().add(new XYChart.Data<>("Somewhat negative", latestStockData.getHistogram()[1]));
        series.getData().add(new XYChart.Data<>("Neutral", latestStockData.getHistogram()[2]));
        series.getData().add(new XYChart.Data<>("Somewhat positive", latestStockData.getHistogram()[3]));
        series.getData().add(new XYChart.Data<>("Positive", latestStockData.getHistogram()[4]));
        barChart.getData().addAll(series);
    }
}
