package mtsp;

import java.io.File;

import tsp.AcsTsp;
import tsplib.TspProblem;

public class Main {


    public static void main(String [] args)
    {
        VrpProblem p = new VrpProblem(new File("vrpnc1.txt"));

        double d = p.getLongestDistanceToPoi();
        System.err.println(d);

        AcsVRP vrp = new AcsVRP(p, 1, Math.pow(524, -1));
        int c = 0;
        while(c < 1){
            vrp.runAlgorithm();
            c++;
        }

        System.out.print("Best Route: ");
        for(Node n : vrp.bestFoundTour){
            System.out.print(p.nodeList.indexOf(n) + " ");
        }
        System.out.println("");
    }

}
