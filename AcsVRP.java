package mtsp;

import java.util.ArrayList;
import java.util.List;


public class AcsVRP {

    // Results
    public double bestFoundRouteLenght = Integer.MAX_VALUE;
    public List<Node> bestFoundTour;

    // Parameters
    private  int m = -1; // Number of Ants
    private final float q0 = 0.9f;
    private final float beta = 2.0f;
    private final float p = 0.1f;
    private final float alpha = 0.1f; // alpha = evaporation

    // Other
    private VrpProblem problem;
    private int n;
    private double[][] pheromoneMatrix;
    private double[][] distanceMatrix;
    private mtspAnt[] ant;
    private double tau0 = 0;


    public AcsVRP(VrpProblem problem, int numberOfAnts, double tau0) {
        this.problem = problem;
        this.m = numberOfAnts;
        this.tau0 = tau0;
        try {
            initData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Each ant generates a new complete Tour. Local and global Pheromone update is also done here.
     */
    public void runAlgorithm(){
        try {
            buildTour();
            globalPheromoneUpdate();
            for (mtspAnt k : ant) {
                k.reset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initData() throws Exception {

        n = problem.numberofCustomers+1;
        ant = new mtspAnt[n];
        pheromoneMatrix = new double[n][n];
        distanceMatrix = new double[n][n];
        if (tau0 <= 0) throw new Exception(" parameter tau0 is " + tau0);

        // Init Distance Matrix
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                distanceMatrix[row][col] = Node.distance(problem.nodeList.get(row), problem.nodeList.get(col));
            }
        }

        // Set initial Pheromone to tau0
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                pheromoneMatrix[row][col] = tau0;
            }
        }

        // Init Ants
        ant = new mtspAnt[m];
        for (int i = 0; i < m; i++) {
            ant[i] = new mtspAnt(problem);
        }

    }

    /**
     * Each Ant builds a new Tour, local pheromone update and optionally a 2-opt search
     */
    private void buildTour() throws Exception {

        for (mtspAnt k : ant) {
            while(k.getCandidateList().size() > 0){
                Node nextNode = calculateNextNode(k);

                int capacity = k.capacity - nextNode.demand;

                double max = problem.maximumRouteLenght;
                double curr = k.getLengthSinceLastDepotVisit();
                double length = max - curr;
                //double length = problem.maximumRouteLenght - k.getLengthSinceLastDepotVisit();

                //System.err.println(length);
                //System.err.println("");

                /*if(k.tour.get(k.tour.size()-1) == k.startNode && k.getLengthSinceLastDepotVisit() > 10){
                    System.err.println("k.tour.size()-1) == k.startNode && curr > 10: " + k.getLengthSinceLastDepotVisit());
                    throw new Exception("k.tour.size()-1) == k.startNode && curr > 10: " + k.getLengthSinceLastDepotVisit());
                } */

                if(k.tour.get(k.tour.size()-1) == k.startNode && k.getLengthSinceLastDepotVisit() != 0){
                    System.err.println("!= 0: " + k.getLengthSinceLastDepotVisit());
                    throw new Exception("!= 0: " + k.getLengthSinceLastDepotVisit());
                }

                if(capacity > 0 && length > 0){
                    // Next Node satisfies constraints --> go to it
                    k.nextNode = nextNode;
                    k.removeToVisitNode(nextNode);
                    k.capacity -= nextNode.demand;

                    // Local pheromone update
                    double update = (1-p) * pheromoneMatrix[getIndexofNode(k.currentNode)][getIndexofNode(k.nextNode)] + p * tau0;
                    updatePheromoneEntry(getIndexofNode(k.currentNode), getIndexofNode(k.nextNode), update);
                    k.currentNode = k.nextNode;
                    k.tour.add(k.nextNode);
                }
                else{
                    // NextNode is not allowed --> return to startDepot
                    k.tour.remove(nextNode);
                    if(k.currentNode != k.startNode){
                        k.nextNode = k.startNode;

                        // Local pheromone update
                        double update = (1-p) * pheromoneMatrix[getIndexofNode(k.currentNode)][getIndexofNode(k.nextNode)] + p * tau0;
                        updatePheromoneEntry(getIndexofNode(k.currentNode), getIndexofNode(k.nextNode), update);
                        k.currentNode = k.nextNode;
                        k.tour.add(k.nextNode);
                    }
                    k.capacity = problem.capacity;
                }


            }


            if(k.currentNode != k.startNode){
                k.tour.add(k.startNode);
            }

            // Optional 2-opt search two improve the current Tour
            //k.TwoOptOptimization();
        }

    }

    private void globalPheromoneUpdate(){
        // Calculate tour length and get Best Tour
        double bestRoute = Integer.MAX_VALUE;
        mtspAnt bestAnt = null;
        for (mtspAnt k : ant) {
            double lenght = k.getTourLenght();
            if(lenght < bestRoute){
                bestRoute = lenght;
                bestAnt = k;
            }
        }

        // Update global best Tour
        for(int i=0; i < bestAnt.tour.size()-1; i++){
            Node node = bestAnt.tour.get(i);
            double update = (1-alpha) * pheromoneMatrix[getIndexofNode(node)][getIndexofNode(bestAnt.tour.get(i+1))] + alpha * Math.pow(bestRoute, -1);
            updatePheromoneEntry(getIndexofNode(node), getIndexofNode(bestAnt.tour.get(i+1)), update);
        }

        // is this current global best, the best of all global bests so far (the best result this algorithm has produced so far)?
        if(bestRoute < bestFoundRouteLenght){
            bestFoundRouteLenght = bestRoute;
            bestFoundTour = new ArrayList<>(bestAnt.tour);
            System.out.println("Found new Best: " + bestFoundRouteLenght);
        }
    }

    /**
     * State Transition Rule
     * @param k current Ant
     */
    private Node calculateNextNode(mtspAnt k) {
        double q = Math.random();
        if (q <= q0) {
            return getBestNode(k);
        } else {
            return getNextNode(k);
        }
    }

    /**
     * Exploitation
     */
    private Node getBestNode(mtspAnt k) {

        double max = -5;
        Node nextNode = null;

        for (Node node : k.getCandidateList()) {
            double current = pheromoneMatrix[getIndexofNode(k.currentNode)][getIndexofNode(node)] * Math.pow(1 / distanceMatrix[getIndexofNode(k.currentNode)][getIndexofNode(node)], beta);
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
    private Node getNextNode(mtspAnt k) {
        double total = 0;
        for (Node node : k.getCandidateList()) {
            total += pheromoneMatrix[getIndexofNode(k.currentNode)][getIndexofNode(node)] * Math.pow(1 / distanceMatrix[getIndexofNode(k.currentNode)][getIndexofNode(node)], beta);
        }

        for (Node node : k.getCandidateList()) {
            double probability = pheromoneMatrix[getIndexofNode(k.currentNode)][getIndexofNode(node)] * Math.pow(1 / distanceMatrix[getIndexofNode(k.currentNode)][getIndexofNode(node)], beta);
            probability = probability / total;
            if (Math.random() <= probability) {
                return node;
            }
        }

        return k.getCandidateList().get((int) (Math.random()*k.getCandidateList().size())); // TODO: change this - this is probably wrong!!!
        //return getNextNode(k);
    }


    private int getIndexofNode(Node node){
        return problem.nodeList.indexOf(node);
    }

    private void updatePheromoneEntry(int x, int y, double newValue){
        // We have a symetric Problem/Matrix
        pheromoneMatrix[x][y] = newValue;
        pheromoneMatrix[y][x] = newValue;
    }


}
