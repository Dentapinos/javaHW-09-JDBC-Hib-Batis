package org.example.utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;

public class Graph {

    DefaultCategoryDataset dataset;

    public Graph(DefaultCategoryDataset dataset) {
        this.dataset = dataset;
    }

    public void show() {
        // Создаем JFrame
        JFrame frame = new JFrame("График работы JDBC, Hibernate, MayBatis");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 400); // Размер окна

        // Создаем JPanel
        JPanel panel = createDemoPanel();

        // Добавляем JPanel в JFrame
        frame.add(panel);

        // Устанавливаем видимость окна
        frame.setVisible(true);
    }

    public JPanel createDemoPanel() {
        JFreeChart chart = createChart();
        chart.setPadding(new RectangleInsets(4, 8, 2, 2));
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        panel.setPreferredSize(new Dimension(600, 300));
        return panel;
    }

    private JFreeChart createChart() {
        DefaultCategoryDataset dataset = this.dataset;
        JFreeChart chart = ChartFactory.createLineChart(
                "Время сохранения и получения 100 сущностей", // Заголовок графика
                "Объекты", // Метка оси X
                "Время (мс)", // Метка оси Y
                dataset, // Данные
                PlotOrientation.VERTICAL, // Ориентация графика
                true, // Включить легенду
                true, // Включить подсказки
                false // Включить URL
        );

        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        plot.setRenderer(renderer);

        return chart;
    }

    public static void printDataset(DefaultCategoryDataset dataset) {
        // Получение всех рядов (series)
        int seriesCount = dataset.getRowCount();
        int columnCount = dataset.getColumnCount();

        // Вывод заголовков столбцов
        System.out.printf("%-17s", "Методы");
        for (int column = 0; column < columnCount; column++) {
            System.out.printf("%-10s", dataset.getColumnKey(column) + "");
        }
        System.out.println();

        // Вывод данных
        for (int series = 0; series < seriesCount; series++) {
            System.out.printf("%-17s", dataset.getRowKey(series));
            for (int column = 0; column < columnCount; column++) {
                Number value = dataset.getValue(series, column);
                System.out.printf("%-10s", value);
            }
            System.out.println();
        }
    }
}

