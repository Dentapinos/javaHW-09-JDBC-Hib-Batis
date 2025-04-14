package org.example;


import lombok.extern.slf4j.Slf4j;
import org.example.utils.Graph;
import org.jfree.data.category.DefaultCategoryDataset;

@Slf4j
public class Main {
    public static void main(String[] args) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        TimeMeasurement timeMeasurement = new TimeMeasurement(dataset, 100);
        timeMeasurement.run();

        Graph graph = new Graph(dataset);
        graph.show();

        Graph.printDataset(dataset);
    }
}
