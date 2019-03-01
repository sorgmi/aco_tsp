package mtsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class mtspAnt {


    public Node startNode;
    public Node currentNode;
    public Node nextNode;

    public int capacity = -1;

    public List<Node> tour;
    private List<Node> toVisit;

    private int n;
    private VrpProblem problem;


    public mtspAnt(VrpProblem problem) {
        this.n = problem.numberofCustomers + 1;
        this.capacity = problem.capacity;
        this.problem = problem;

        tour = new ArrayList<>();

        startNode = problem.nodeList.get(0); // Start always at the depot, which is Node 0
        currentNode = startNode;
        tour.add(startNode);

        // At the beginning all Nodes, except the starting Node, have to be visited
        toVisit = new ArrayList<>();
        for (int i = 1; i < n; i++) {
            toVisit.add(problem.nodeList.get(i));
        }
    }

    /**
     * Return the toVisit, if a candidate list shouldn't be used
     */
    public List<Node> getCandidateList() {
       ArrayList<Node> list = new ArrayList<>();

        for (Node n : toVisit) {
            double distance = Node.distance(n, currentNode);
            if (distance < problem.numberofCustomers / 4) {
                list.add(n);
            }
        }

        if (list.size() > 0) {
            return list;
        } else {
            return toVisit;
        }
       //return toVisit;
    }

    public void removeToVisitNode(Node n) {
        toVisit.remove(n);
    }


    public double getLengthSinceLastDepotVisit() {
        double lenght = 0;

        if(tour.get(tour.size()-1) == startNode){
            return 0;
        }

        for (int i = 0; i < tour.size() - 1; i++) {
            if (tour.get(i) == startNode) {
                lenght = 0;
                //System.out.println("reset");
            }
            lenght = lenght + Node.distance(tour.get(i), tour.get(i + 1));
            //System.out.println("curr l : " + lenght);
        }
        //System.out.println("returning l : " + lenght);
        return lenght;
    }

    public double getTourLenght() {
        double lenght = 0;
        for (int i = 0; i < tour.size() - 1; i++) {
            lenght += Node.distance(tour.get(i), tour.get(i + 1));
        }
        return lenght;
    }

    public void reset() {
        tour.clear();
        tour.add(startNode);

        toVisit.clear();
        toVisit = new ArrayList<>();
        for (int i = 1; i < n; i++) {
            toVisit.add(problem.nodeList.get(i));
        }

        capacity = problem.capacity;
    }


    public void TwoOptOptimization(){
        // Get Subtours
        List<Node> subTourList = new ArrayList<>();
        List<Node> current = new ArrayList<>();
        subTourList.add(startNode);
        for( int i=1; i < tour.size(); i++){
            if(tour.get(i) == startNode){
                //System.out.println("New Subtour before 2opt: " + printTour(current));
                current = new ArrayList<>(TwoOpt(current));
                //System.out.println("New Subtour after 2opt: " + printTour(current));

                subTourList.addAll(current);
                current = new ArrayList<>();
                subTourList.add(startNode);
            }
            else{
                current.add(tour.get(i));
            }
        }

        if(subTourList.get(subTourList.size()-1) != startNode){
            System.err.println("SubtourList does not end with start-Depot");
            //subTourList.add(startNode);
        }

        if(subTourList.size() != tour.size()){
            System.err.println("subTourList.size() != tour.size(): " + subTourList.size() + " vs. " + tour.size());
            //System.err.println(printTour(subTourList));
            //System.err.println(printTour(tour));
        }
        else {
            tour = new ArrayList<>(subTourList);
        }

    }


    private List<Node> TwoOpt(List<Node> subTour) {

        int size = subTour.size();
        List<Node> newTour = new ArrayList<>(subTour);

        // repeat until no improvement is made
        int improve = 0;

        while (improve < 20) {
            double best_distance = getTourLenght(subTour);

            for (int i = 0; i < size - 1; i++) {
                for (int k = i + 1; k < size; k++) {
                    TwoOptSwap(i, k, newTour, subTour);

                    double new_distance = getTourLenght(newTour);

                    if (new_distance < best_distance) {
                        improve = 0;
                        subTour = new ArrayList<>(newTour);
                        //System.out.println("Tour optimized: -" + (best_distance-new_distance));
                        best_distance = new_distance;
                    }
                }
            }

            improve++;
        }

        return subTour;
    }

    public double getTourLenght(List<Node> tourList) {
        double lenght = 0;
        for (int i = 0; i < tourList.size() - 1; i++) {
            lenght += Node.distance(tourList.get(i), tourList.get(i + 1));
        }
        return lenght;
    }


    private void TwoOptSwap(int i, int k, List<Node> newTour, List<Node> subTour) {

        int size = subTour.size();
        newTour.clear();

        // 1. take route[0] to route[i-1] and add them in order to new_route
        for (int c = 0; c <= i-1; ++c) {
            newTour.add(subTour.get(c));
        }

        // 2. take route[i] to route[k] and add them in reverse order to new_route
        int dec = 0;
        for (int c = i; c <= k; ++c) {
            newTour.add(subTour.get(k-dec));
            dec++;
        }

        // 3. take route[k+1] to end and add them in order to new_route
        for (int c = k + 1; c < size; ++c) {
            newTour.add(subTour.get(c));
        }

        if(newTour.size() != subTour.size()) System.err.println("not the same size");
    }


    public String printTour(List<Node> tour){
        String s = "";
        for(Node n : tour){
            s += (problem.nodeList.indexOf(n) + " ");
        }
        return s;
    }


}
