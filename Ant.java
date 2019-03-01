package tsp;

import java.util.ArrayList;
import java.util.List;

public class Ant {

    public int tourLenght = -1;
    public int startNode = -1;
    public int currentNode = -1;
    public int nextNode = -1;

    public List<Integer> tour;
    public List<Integer> toVisit;

    private int n;

    public Ant(int n) {
        this.n = n;
        tour = new ArrayList<>();

        startNode = (int) (Math.random() * n);
        currentNode = startNode;
        tour.add(startNode); // TODO: 23.02.17 this is not in the algorithm

        // At the beginning all Nodes, except the starting Node, have to be visited
        toVisit = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if( i != startNode){
                toVisit.add(i);
            }
        }
    }

    public void clearMemory(){
        tour.clear();
        tour.add(startNode);
        toVisit.clear();
        for (int i = 0; i < n; i++) {
            if( i != startNode){
                toVisit.add(i);
            }
        }
    }
}
