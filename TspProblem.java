package tsplib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Loads a .tsp file and the .opt.tour file
 */

public class TspProblem {

    public List<double[]> nodeList = new ArrayList<>();
    public List<Integer> optimaleTourList = new ArrayList<>();
    public String name, comment;
    public int dimension = -1;
    public int optimalTourLenght = -1;



    public TspProblem(File file){
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

        // Now get the best route
        String filename = file.getAbsolutePath();
        filename = filename.substring(0, filename.length()- 4);
        filename += ".opt.tour";
        try {
            reader = new BufferedReader(new FileReader(filename));
            loadBestRoute(reader);
            calculateBestTourLenght(optimaleTourList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFile(BufferedReader reader) throws Exception {
        String line = null;
        while ((line = reader.readLine()) != null){

            if(line.equals("EOF")){
                return;
            }
            else if (line.equals("NODE_COORD_SECTION")) {
                loadNodeList(reader);
            }

            else{
                String[] tokens = line.split(":");
                String key = tokens[0].trim();
                String value = tokens[1].trim();

                if (key.equals("NAME")) {
                    name = value;
                } else if (key.equals("COMMENT")) {
                    if (comment == null) {
                        comment = value;
                    } else {
                        comment = comment + "\n" + value;
                    }
                } else if (key.equals("TYPE")) {
                    if(!value.equals("TSP")){
                        throw new Exception("Only TSP Problems supported");
                    }
                } else if (key.equals("DIMENSION")) {
                    dimension = Integer.parseInt(value);
                } else if (key.equals("CAPACITY")) {
                    //capacity = Integer.parseInt(value);
                } else if (key.equals("EDGE_WEIGHT_TYPE")) {
                    if(!value.equals("EUC_2D")){
                        throw new Exception("Only EUC_2D EDGE_WEIGHT_TYPE supported");
                    }
                } else if (key.equals("EDGE_WEIGHT_FORMAT")) {
                    //edgeWeightFormat = EdgeWeightFormat.valueOf(value);
                } else if (key.equals("EDGE_DATA_FORMAT")) {
                    //edgeDataFormat = EdgeDataFormat.valueOf(value);
                } else if (key.equals("NODE_COORD_FORMAT")) {
                    //nodeCoordinateType = NodeCoordType.valueOf(value);
                } else if (key.equals("DISPLAY_DATA_TYPE")) {
                    //displayDataType = DisplayDataType.valueOf(value);
                }
            }
        }
    }

    private void loadBestRoute(BufferedReader reader) throws IOException {
        String line = null;
        while ((line = reader.readLine()) != null){
            if(line.equals("TOUR_SECTION")){
                while ((line = reader.readLine()) != null && !line.equals("-1") && !line.equals("EOF")){
                    optimaleTourList.add(Integer.valueOf(line));
                }
                return;
            }
        }
    }

    private void calculateBestTourLenght(List<Integer> optimaleTourList){
        int lenght = 0;
        for(int i=0; i < optimaleTourList.size()-1; i++){
            lenght += Distance.distance(nodeList.get(optimaleTourList.get(i)-1), nodeList.get(optimaleTourList.get(i+1)-1));
        }
        lenght += Distance.distance(nodeList.get(optimaleTourList.get(optimaleTourList.size()-1)-1), nodeList.get(optimaleTourList.get(0)-1));
        optimalTourLenght = lenght;
    }

    private void loadNodeList(BufferedReader reader) throws IOException {
        String line = null;
        while ((line = reader.readLine()) != null && !line.equals("EOF")){
            line = line.trim();
            String[] tokens = line.split(" ");
            // dopellte Leerzeichen entfernen
            List<Double> list = new ArrayList<>();
            for(int i=0; i < tokens.length; i++){
                if(tokens[i].length() > 0){
                    list.add(Double.valueOf(tokens[i]));
                }
            }
            //String number = tokens[0].trim();
            //String x = tokens[1].trim();
            //String y = tokens[2].trim();

            nodeList.add(new double[]{list.get(1), list.get(2)});
        }
    }
}
