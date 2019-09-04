package app;

import structures.Arc;
import structures.Graph;
import structures.PartialTree;

import java.io.IOException;
import java.util.ArrayList;

public class Driver {

    public static void main(String[] args) {
        Graph graph = null;
        try {
            graph = new Graph("graph3.txt");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("Initialization: ");
        PartialTreeList partialTreeList = PartialTreeList.initialize(graph);
        for(PartialTree pt : partialTreeList) {
        	System.out.println(pt.toString());
        }
        System.out.println("\n\n\n");
        
        System.out.println("Execution: ");
        ArrayList<Arc> arcList = PartialTreeList.execute(partialTreeList);
        System.out.println("Output: ");
        System.out.println(arcList);
        
        System.out.println("\n\n\n");
    }
}
