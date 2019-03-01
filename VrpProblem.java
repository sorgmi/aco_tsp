package mtsp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VrpProblem {


    public int numberofCustomers;
    public int capacity;
    public int maximumRouteLenght;
    public int serviceTime; // Delta - sometimes serviceTime - (time needed to unload all goods

    public List<Node> nodeList = new ArrayList<>();


    public VrpProblem(File file){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            loadFile(reader);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public double getLongestDistanceToPoi(){
        double max = 0;
        for(int i =0; i < nodeList.size() -1; i++ ){
            Node n1 = nodeList.get(i);
            Node n2 = nodeList.get(i+1);
            double temp = Node.distance(n1,n2);
            if(temp > max){
                max = temp;
            }
        }
        return max;
    }

    public VrpProblem(){

    }


    /**
     * Format of a file:
     * number of customers, vehicle capacity, maximum route time, drop time
     * depot x-coordinate, depot y-coordinate
     * for each customer in turn: x-coordinate, y-coordinate, quantity
     * @throws Exception
     */
    private void loadFile(BufferedReader reader) throws Exception {
        String line = null;
        int linenumber = 0;
        while ((line = reader.readLine()) != null){
            linenumber++;
            line = line.trim();

            if(linenumber == 1){
                String[] tokens = line.split(" ");
                numberofCustomers = Integer.valueOf(tokens[0].trim());
                capacity = Integer.valueOf(tokens[1].trim());
                maximumRouteLenght = Integer.valueOf(tokens[2].trim());
                serviceTime = Integer.valueOf(tokens[3].trim());
            }

            else if(linenumber == 2){
                // Startdepot
                String[] tokens = line.split(" ");
                Node n = new Node(Integer.valueOf(tokens[0].trim()), Integer.valueOf(tokens[1].trim()), 0);
                n.startDepot = true;
                nodeList.add(n);
            }

            else{
                String[] tokens = line.split(" ");
                nodeList.add(new Node(Integer.valueOf(tokens[0].trim()), Integer.valueOf(tokens[1].trim()), Integer.valueOf(tokens[2].trim())));
            }


        }
    }

}
