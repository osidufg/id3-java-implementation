import java.io.*;
import java.util.*;

public class ID3DecisionTree{
    private Node root;

    // ------------------------------ Node Library ------------------------------
    public static class Node {
        private String attribute;
        private String label;
        private HashMap<String, Node> children;

        // Parent Node
        public Node(String attribute) {
            this.attribute = attribute;
            this.label = null;
            this.children = new HashMap<>();
        }
        // Leaf Node
        public Node(String label, boolean isLeafNode) {
            this.attribute = null;
            this.label = label;
            this.children = null;
        }

        // Methods buat get/set values
        public String getAttribute() {
            return attribute;
        }
        public String getLabel() {
            return label;
        }
        public boolean isLeafNode() {
            return label != null;
        }
        public void addChild(String value, Node child) {
            children.put(value, child);
        }
        public Node getChild(String value) {
            return children.get(value);
        }
        public ArrayList<String> getChildVal() {
            if (children == null){
                return null;
            } else {
            return new ArrayList<>(children.keySet());
            }
        }
    }
    // --------------------------------------------------------------------------------

    // -------------------- Node Driver --------------------
    public void makeTree(ArrayList<Data> datas, ArrayList<String> attributes) {
        root = makeTreeRecursion(datas, attributes);
    }
    private Node makeTreeRecursion(ArrayList<Data> datas, ArrayList<String> attributes) {
        String majorityLabel = getMostLabel(datas);
        
        if (isLabelEqualsFirst(datas)){
            return new Node(majorityLabel, true);
        }

        String bestAttribute = getBestAttribute(datas, attributes);
        Node node = new Node(bestAttribute);
        attributes.remove(bestAttribute);

        HashMap<String, ArrayList<Data>> subsets = getSubsets(datas, bestAttribute);
        for (String value : subsets.keySet()) {
            ArrayList<Data> subset = subsets.get(value);
            Node child = makeTreeRecursion(subset, new ArrayList<>(attributes));
            node.addChild(value, child);
        }
        return node;
    }
    // ------------------------------------------------------------

    // -------------------- Memilih Attribute dengan Gain Tertinggi --------------------
    private String getBestAttribute(ArrayList<Data> datas, ArrayList<String> attributes) {
        // Placeholder untuk sorting
        String bestAttribute = null;
        double maxGain = -1.0;
        double parentEntropy = calcEntropy(datas);
        for (String attribute : attributes) {
            // Kalkulasi Gain
            System.out.println("Entropy(S): " + parentEntropy);
            double infoGain = parentEntropy - calcSplitEntropy(datas, attribute);
            System.out.println("Gain (" + attribute.toString() + "): " + infoGain + "\n");
            // Komparasi Gain Tertinggi
            if (infoGain > maxGain) {
                maxGain = infoGain;
                bestAttribute = attribute;
            }
        }
        System.out.println("> Gain Tertinggi: " + bestAttribute + "\n--------------------\n");
        return bestAttribute;
    }
    private double calcEntropy(ArrayList<Data> datas) {
        int totalDatas = datas.size();
        // Peluang label yes dan no
        HashMap<String, Integer> countLabel = new HashMap<>();
        for (Data data : datas) {
            String label = data.getLabel();
            countLabel.put(label, countLabel.getOrDefault(label, 0) + 1);
        }
        // System.out.println(countLabel.toString());
        double entropy = 0.0;
        for (String label : countLabel.keySet()) {
            int count = countLabel.get(label);
            double probability = (double) count / totalDatas;
            // Rumus Entropy
            // log₂(count / totalDatas) = logₑ(probability) / logₑ(2)
            entropy -= probability * (Math.log(probability) / Math.log(2));
        }
        return entropy;
    }
    private double calcSplitEntropy(ArrayList<Data> datas, String attribute) {
        int totalDatas = datas.size();
        HashMap<String, ArrayList<Data>> subsets = getSubsets(datas, attribute);
        String attributeList = subsets.keySet().toString().replaceAll("\\[", "").replaceAll("\\]", "");
        double entropy = 0.0;
        for (String value : subsets.keySet()) {
            ArrayList<Data> subset = subsets.get(value);
            double subsetSize = subset.size();
            double subsetEntropy = calcEntropy(subset);
            entropy += (subsetSize / totalDatas) * subsetEntropy;
            // if (value == null){
            //     // Deprecated. gua udah nemu dalang nya
            //     System.out.println("Entropy (" + "null): " + entropy);
            // } else{
            System.out.println("Entropy (" + value.toString() + "): " + entropy);
            // }
        }
        System.out.println("\u03A3 Entropy(" + attributeList + "): "+ entropy);
        return entropy;
    }
    // ----------------------------------------------------------------------

    // -------------------- Helper --------------------
    private HashMap<String, ArrayList<Data>> getSubsets(ArrayList<Data> datas, String attribute) {
        HashMap<String, ArrayList<Data>> subsets = new HashMap<>();
        for (Data data : datas) {
            String value = data.getAttributeValue(attribute);
            if (!subsets.containsKey(value)) {
                subsets.put(value, new ArrayList<>());
            }
            subsets.get(value).add(data);
        }
        return subsets;
    }

    private boolean isLabelEqualsFirst(ArrayList<Data> datas) {
        String firstLabel = datas.get(0).getLabel();
        for (Data data : datas) {
            if (!data.getLabel().equals(firstLabel)) {
                return false;
            }
        }
        return true;
    }
    private String getMostLabel(ArrayList<Data> datas) {
        // Peluang label yes dan no
        HashMap<String, Integer> countLabel = new HashMap<>();
        for (Data data : datas) {
            String label = data.getLabel();
            countLabel.put(label, countLabel.getOrDefault(label, 0) + 1);
            // System.out.println(countLabel.toString());
        }
        String majorityLabel = null;
        int maxCount = -1;
        for (String label : countLabel.keySet()) {
            int count = countLabel.get(label);
            if (count > maxCount) {
                maxCount = count;
                majorityLabel = label;
            }
        }
        // System.out.println(majorityLabel);
        return majorityLabel;
    }

    // ------------------------------------------------------------

    // ------------------------------ Menguji Node (Prediction) ------------------------------
        public String classifyPrediction(Data data) {
            return classifyPredictionRecursion(data, root);
        }

        private String classifyPredictionRecursion(Data data, Node node) {
            if (node.isLeafNode()) {
                return node.getLabel();
            }

            String attributeValue = data.getAttributeValue(node.getAttribute());
            Node child = node.getChild(attributeValue);
            if (child == null) {
                return "Not sure...";
            }
            return classifyPredictionRecursion(data, child);
        }
    // --------------------------------------------------------------------------------

    // -------------------- Print Tree ke Terminal --------------------
        public void printTree() {
            System.out.println("[Decicion Tree]");
            printTreeRecursion(root, "");
        }

        private void printTreeRecursion(Node node, String indent) {
            if (node.isLeafNode()) {
                System.out.println(indent + "└──label: " + node.getLabel());
                return;
            }
            System.out.println(indent + "└──attr: " + node.getAttribute());
            ArrayList<String> childValues = node.getChildVal();
            int lastChildIndex = childValues.size() - 1;
            for (int i = 0; i < lastChildIndex; i++) {
                String value = childValues.get(i);
                System.out.println(indent + "│  ├──val: " + value);
                Node child = node.getChild(value);
                printTreeRecursion(child, indent + "│  │  ");
            }

            String lastValue = childValues.get(lastChildIndex);
            System.out.println(indent + "│  └──val: " + lastValue);
            Node lastChild = node.getChild(lastValue);
            printTreeRecursion(lastChild, indent + "│     ");
        }
    // ------------------------------------------------------------------

    // ------------------------------ Data Class ------------------------------
    public static class Data {
        private HashMap<String, String> attributes;
        private String label;

        public Data(HashMap<String, String> attributes, String label) {
            this.attributes = attributes;
            this.label = label;
        }

        public String getAttributeValue(String attribute) {
            return attributes.get(attribute);
        }

        public String getLabel() {
            return label;
        }
    }
    // ----------------------------------------------------------------------

    public static void main(String[] args) {
        // Inisialisasi dataset
        ArrayList<Data> dataset = new ArrayList<>();
        ArrayList<String> attributes = new ArrayList<>();
        String csvPath = "BuyCar.csv";
        // Baca CSV
        String line;
        String delimiter = ";";
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            // Demi apapun gua ga tau kenapa tapi gua harus skip 1 char
            // ato ga ni decision tree ga beres
            // yang edit itu buat testing doang
            if (csvPath == "BuyCar.csv"){
            br.read();
            }
            // Read header
            String headerLine = br.readLine();
            String [] header = headerLine.split(delimiter);
            // Read data
            while ((line = br.readLine()) != null) {
                String[] attr = line.split(delimiter);

                String age = attr[0];
                String income = attr[1];
                String employee = attr[2];
                String creditrating = attr[3];
                String label = attr[4];

                // Masuk2in ke class Data
                createDataset(dataset, age, income, employee, creditrating, label);
            }
            // Attribute BuyCar jangan jadi header. length-1
            // Bisa juga di pop tapi gatau kalo di java gimana
            for (int i = 0; i < header.length-1; i++){
                addHeader(attributes, header[i]);
                // System.out.print(header[i] + " ,");
            }
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Jalankan program lezgowww
        ID3DecisionTree id3Tree = new ID3DecisionTree();
        try{
        id3Tree.makeTree(dataset, attributes);
        id3Tree.printTree();
        } catch(Exception e){
            System.out.println(
                ">>> Error bro, dah lah <<<\n(Error: " + 
                e + 
                " At Line: " + 
                e.getStackTrace()[0].getLineNumber() + 
                ")"
                );
        }

        // Menguji node dan predict output
        Data predict = new Data(createAttrValue(
                            "Young", 
                            "Low", 
                            "No", 
                            "Fair"), 
                            null);
        String prediction = id3Tree.classifyPrediction(predict);
        System.out.println(
            "\nPrediction (" + 
            predict.getAttributeValue("Age") + ", " + 
            predict.getAttributeValue("Income") + ", " + 
            predict.getAttributeValue("Employee") + ", " + 
            predict.getAttributeValue("CreditRating") + 
            "): " + 
            prediction);
    }
    private static HashMap<String, String> createAttrValue(String age, String income, String employee, String creditrating) {
        HashMap<String, String> attrValue = new HashMap<>();
        attrValue.put("Age", age);
        attrValue.put("Income", income);
        attrValue.put("Employee", employee);
        attrValue.put("CreditRating", creditrating);
        // System.out.println(attrValue);
        return attrValue;
    }  
    private static void createDataset(ArrayList<Data> dataset, String age, String income, String employee, String creditrating, String label){
            dataset.add(new Data(
                createAttrValue(age, income, employee, creditrating), 
                label));
            // Logging
            System.out.println("Added attribute: " + 
            age + "; " + 
            income + "; " + 
            employee + "; " + 
            creditrating + "; " + 
            label);
    }

    private static void addHeader(ArrayList<String> attributes, String attr){
            attributes.add(attr);
            // Logging
            System.out.println("Added header: " + attr);
    }
}