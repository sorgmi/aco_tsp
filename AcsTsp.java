package tsp;

import tsplib.Distance;
import tsplib.TspProblem;


/**
 * Algorithm of Dorigo and Gambardella (1997) - "Ant colony system: a cooperative learning approach to the traveling salesman problem"
 * ieeexplore.ieee.org/iel1/4235/12703/00585892.pdf
 */
public class AcsTsp {

    // Parameters
    private final int m = 10; // Number of Ants
    private final float q0 = 0.9f;
    private final float beta = 2;
    private final float p = 0.1f;
    private final float alpha = 0.1f;


    private TspProblem problem = null;
    private int n;
    private double[][] pheromoneMatrix;
    private double[][] distanceMatrix;
    private Ant[] ant;
    private double tau0 = 0;

    public AcsTsp(TspProblem problem) {
        this.problem = problem;
        try {
            initData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int runAlgorithm(){
        try {
            buildTour();
            int best = globalPheromoneUpdate();
            for (Ant k : ant) {
                k.clearMemory();
            }
            return best;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    private void initData() throws Exception {

        // Init Parameters and Variables
        n = problem.dimension;
        ant = new Ant[n];
        pheromoneMatrix = new double[n][n];
        distanceMatrix = new double[n][n];
        tau0 = Math.pow(n * problem.optimalTourLenght, -1);
        if (tau0 <= 0) throw new Exception(" parameter tau0 is " + tau0);

        // Init Distance Matrix
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                distanceMatrix[row][col] = Distance.distance(problem.nodeList.get(row), problem.nodeList.get(col));
            }
        }

        // Set initial Pheromone to tau0
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                pheromoneMatrix[row][col] = tau0;
            }
        }

        // Init Ants
        ant = new Ant[m];
        for (int i = 0; i < m; i++) {
            ant[i] = new Ant(n);
        }

    }

    /**
     * Each Ant builds a new Tour
     */
    private void buildTour() throws Exception {
        for (int i = 0; i < n; i++) {
            if (i < n-1) {
                for (Ant k : ant) {
                    k.nextNode = calculateNextNode(k);
                    if (k.nextNode < 0) throw new Exception("nextNode is " + k.nextNode);

                    //System.out.println("I is: " + i);
                    //System.out.println("NextNode ist: " + nextNode);
                    //System.out.println("Index ist: " + k.toVisit.indexOf(nextNode));
                    int removed = k.toVisit.remove(k.toVisit.indexOf(k.nextNode)); // TODO: 23.02.17 could be wrong
                    if (removed != k.nextNode) throw new Exception("Wrong remove!");
                    k.tour.add(k.nextNode);
                    //System.out.println(" ");
                }
            } else {
                //System.err.println("in else");
                // Go back to initial city
                for (Ant k : ant) {
                    k.nextNode = k.startNode;
                    k.tour.add(k.startNode);
                }
            }

            // Local pheromone update
            for (Ant k : ant) {
                double update = (1-p) * pheromoneMatrix[k.currentNode][k.nextNode] + p*tau0;
                updatePheromoneEntry(k.currentNode, k.nextNode, update);
                k.currentNode = k.nextNode;
            }
        }
    }

    private int globalPheromoneUpdate(){

        // Calculate tour lenght and get Best Tour
        int bestRoute = Integer.MAX_VALUE;
        Ant bestAnt = null;
        for (Ant k : ant) {
            int lenght = 0;
            //System.out.println("size: " + k.tour.size());
            for(int i=0; i < k.tour.size()-1; i++){
                int index = k.tour.get(i);
                int index2 = k.tour.get(i+1);
                lenght += Distance.distance(problem.nodeList.get(index), problem.nodeList.get(index2));
            }

            k.tourLenght = lenght;
            if(k.tourLenght < bestRoute){
                bestRoute = k.tourLenght;
                bestAnt = k;
            }
            //System.out.println(k.tour);
            //System.out.println(lenght);
        }

        // Update global best Tour
        for(int i=0; i < bestAnt.tour.size()-1; i++){
            int node = bestAnt.tour.get(i);
            double update = (1-alpha) * pheromoneMatrix[node][bestAnt.tour.get(i+1)] + alpha * Math.pow(bestAnt.tourLenght, -1);
            updatePheromoneEntry(node, bestAnt.tour.get(i+1), update);
        }

        return bestAnt.tourLenght;
    }

    /**
     * State Transition Rule
     * @param k current Ant
     */
    private int calculateNextNode(Ant k) {
        double q = Math.random();
        if (q <= q0) {
            //System.out.println("Best Node");
            return getBestNode(k);
        } else {
            //System.out.println("Prob Node");
            return getNextNode(k);
        }
    }

    /**
     * Exploitation
     */
    private int getBestNode(Ant k) {

        double max = -5;
        int nextNode = -5;

        //System.out.println(k.toVisit);
        for (int node : k.toVisit) {
            double current = pheromoneMatrix[k.currentNode][node] * Math.pow(1 / distanceMatrix[k.currentNode][node], beta);
            if (current > max) {
                max = current;
                nextNode = node;
            }
        }
        return nextNode;
    }

    /**
     * Exploration: random-proportional rule
     * @param k current Ant
     */
    private int getNextNode(Ant k) {
        //System.out.println("-- start --");
        double total = 0;
        for (int node : k.toVisit) {
            total += pheromoneMatrix[k.currentNode][node] * Math.pow(1 / distanceMatrix[k.currentNode][node], beta);
        }

        for (int node : k.toVisit) {
            double probability = pheromoneMatrix[k.currentNode][node] * Math.pow(1 / distanceMatrix[k.currentNode][node], beta);
            probability = probability / total;
            if (Math.random() <= probability) {
                //System.out.println("Random selected " + node);
                return node;
            }
            else{
                //System.out.println("Not random ");
            }
        }

        //System.out.println("-- end --");

        //return -666; //// TODO: 23.02.17 wenn keine ausgewahlt wurde nochmal durchlaufen oder doch einfach den mit der hochsten wahrscheinlichkeit nehmen?
        //return k.toVisit.get((int) (Math.random()*k.toVisit.size())); // TODO: 23.02.17 change !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
        return getNextNode(k);
    }

    private void updatePheromoneEntry(int x, int y, double newValue){
        // We have a symetric Problem/Matrix
        pheromoneMatrix[x][y] = newValue;
        pheromoneMatrix[y][x] = newValue;
    }

}
