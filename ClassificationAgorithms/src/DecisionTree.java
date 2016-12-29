import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class DecisionTree {
	
	public static class Node {
		Node left;
		Node right;
		String value;
		int feature;
		
		public Node(String value,int feature) {
			this.value = value;
			this.feature=feature;
			this.left = null;
			this.right = null;
		}
	}

	public static String inputFile = "project3_dataset1.txt";
	
	public static String[][] matFeatures = new String[1000][100];
	public static String[] classVector = new String [1000];
	
	public static String[][] trainMatFeatures = new String[1000][100];
	public static String[] trainClassVector = new String [1000];
	
	public static String[][] testMatFeatures = new String[1000][100];
	public static String[] testClassVector = new String [1000];
	
	public static int kFold = -1;
	public static int kIndex = 0;
	
	
	public static int rowCount = 0;
	public static int colCount = 0;
	public static int classCount = 0;
	
	
	public static String predictedClass =null;
	public static String trueClass;
	
	public static float positiveMatch = 0 ;
	public static float negativeMatch = 0 ;
	
	public static float Accuracy[] = new float[100];
	public static float Pricision[] = new float[100];
	public static float Recall[] = new float[100];
	public static float FMeasure[] = new float[100];
	
	
	public static float a=0;
	public static float b=0;
	public static float c=0;
	public static float d=0;

	
	public static int trainSize;
	
	
	public static void ordinalDataSetPreprocess() {
		
	}
	
	public static void read() {
		try {
		    BufferedReader in = new BufferedReader(new FileReader(inputFile));
		    String string;
		    while ((string = in.readLine()) != null) {
		    	String[] features = string.split("\t"); 
		    	
		    	colCount = features.length-1;
		    	
		    	// Populate feature matrix
		    	for(int i =0; i<features.length-1;i++) {
		    		matFeatures[rowCount][i] = features[i]; 
		    	//	System.out.println(matFeatures[rowCount][i]);
		    	}
		    	
		    	// Populate class vector
		    	classVector[rowCount] = features[features.length-1];
		    	
		    	rowCount++;
		    }
		        
		    in.close();
		} catch (IOException e) {
			System.err.println("Input file not found");
		}
	}
	
	public static void display(String[][] matFeaturesDisplay, int row, int col, String[] classVectorDisplay) {
		for(int i=0;i<row;i++) {
			for(int j=0;j<col;j++) {
				System.out.print(matFeaturesDisplay[i][j] + " ");
			}
			System.out.print(" --> " + classVectorDisplay[i]);
			System.out.println();
		}
	}
	
	public static boolean isNumeric(String str) {
	  if(str==null)
		  System.out.println("Null Value");
		
	  return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}
	
	public static void preProces() {
		for(int i=0;i<rowCount;i++) {
			for(int j=0;j<colCount;j++) {
			
				if(isNumeric(matFeatures[i][j])) {
					
					float roundOfValue = roundOfValue(Float.valueOf(matFeatures[i][j]),i,j);
					matFeatures[i][j] = String.valueOf(roundOfValue);
					
				}
				
			}
		}
	}
	
	public static float roundOfValue(float value,int i,int j) {
		
		while (!(value >= 10 && value <= 99)) {
			
			if((value>9&&value<10)||(value>99&&value<100)||(value==0)) {
				break;
			}
			
		    if (value < 10) {
		    	value=value*10;
		    }
		    else if (value > 99) {
		    	value=value/10;
		    }
		}
		//System.out.println(java.lang.Math.round(value)+" "+i+" "+j);
		return  java.lang.Math.round(value);
	}
	
	public static void divide() {
	
		if(kFold==-1) {			
			trainSize = rowCount - rowCount/10;
			
			
			for(int i=0;i<trainSize;i++) {
				for(int j=0;j<colCount;j++) {
					trainMatFeatures[i][j] = matFeatures[i][j];
				}
				trainClassVector[i] = classVector[i];
			}
			
			for(int i=trainSize, k=0 ;i<rowCount;i++,k++) {
				for(int j=0;j<colCount;j++) {
					testMatFeatures[k][j] = matFeatures[i][j];
				}
				testClassVector[k] = classVector[i];
			}
		} else {
			
			int testSize = rowCount/kFold;
			trainSize = rowCount-testSize;
			
			for(int i=testSize*kIndex,l=0;i<(testSize*kIndex)+testSize;i++,l++) {
				for(int j=0;j<colCount;j++) {
					testMatFeatures[l][j] = matFeatures[i][j];
				}
				testClassVector[l] = classVector[i];
			}
			int k=0;
			for(int i=(testSize*kIndex)+testSize ;i<rowCount;i++,k++) {
				for(int j=0;j<colCount;j++) {
					trainMatFeatures[k][j] = matFeatures[i][j];
				}
				trainClassVector[k] = classVector[i];
			}
			
			// check if anything is remaining
			if(kIndex!=0) {
				
				for(int i=0;i<testSize*kIndex;i++,k++) {
					for(int j=0;j<colCount;j++) {
						trainMatFeatures[k][j] = matFeatures[i][j];
					}
					trainClassVector[k] = classVector[i];
				}}
			
			
		}
	}
	
	public static void displayNode(Node node) {
		if (node == null) {
			return;
		}
		System.out.println("Node (value) : "+ node.value);
		System.out.println("Node (feature) : "+ node.feature);
		
		displayNode(node.left);
		displayNode(node.right);
	}
	
	public static Node split(String[][] trainMatFeatures, int row, int col, String[] trainClassVector) {
	
		// Check if all records are of same classs (if yes return)
		String classVector;
		boolean leafnodeReached = true;
		classVector = trainClassVector[0];
		for (int k=1;k<row;k++){
			if (!classVector.equals(trainClassVector[k])) {
				leafnodeReached = false;
				break;
			}
		}
		
		// If leaf node reached
		if(leafnodeReached == true) {
			Node node = new Node(classVector, -1);
			return node;
		}
		
		//Calculate mean of column
		
		float sum;
		float mean=0;
		
		float[] giniIndexArray = new float[100];
		float[] meanArray = new float[100];
		
		for(int k=0;k<col;k++) {
			
			sum = 0;
			
			for (int i=0;i<row;i++) {
				if(trainMatFeatures[i][k]!= null && isNumeric(trainMatFeatures[i][k])) {
					sum = sum + Float.valueOf(trainMatFeatures[i][k]);
				}
			}
			
			mean = sum/row;
			
			// Calclate child gini index
			float N1_C0 = 0;
			float N1_C1 = 0;
			
			float N2_C0 = 0;
			float N2_C1 = 0;
			
			for (int i=0;i<row;i++) {
				if(isNumeric(trainMatFeatures[i][k])) {
					if(Float.valueOf(trainMatFeatures[i][k])>= mean) {
						if (trainClassVector[i].equals("0")) {
							N1_C0++;
						} else if(trainClassVector[i].equals("1")) {
							N1_C1++;
						}
					} else {
						if (trainClassVector[i].equals("0")) {
							N2_C0++;
						} else if(trainClassVector[i].equals("1")) {
							N2_C1++;
						}
					}
				}
			}

			float giniIndexN1 = calculateGiniIndex(N1_C0, N1_C1);
			float giniIndexN2 = calculateGiniIndex(N2_C0, N2_C1);
			
			float giniIndexChilderen = ((N1_C0+N1_C1)/(N1_C0+N1_C1+N2_C0+N2_C1)*giniIndexN1) + ((N2_C0+N2_C1)/(N1_C0+N1_C1+N2_C0+N2_C1)*giniIndexN2);
			
			// calculate parent gini index 
			
			float giniIndexParent = calculateGiniIndex(N1_C0+N2_C0, N1_C1+N2_C1);
			
			giniIndexArray[k]=giniIndexChilderen;
			meanArray[k]=mean;
			
		}
		
		
		// Display all gini indices
		System.out.println("Gini index : ");
		for(int k=0;k<col;k++) {
			System.out.print(giniIndexArray[k]+ " ");
		}
		
		float smallestGiniIndex = 10000;
		int indexInArray = -1;
		
		for(int k=0;k<col;k++) {
			if(smallestGiniIndex>giniIndexArray[k]) {
				smallestGiniIndex = giniIndexArray[k];
				indexInArray = k;
			}
		}
		System.out.println();
		System.out.println("Gini index smallest : "+ smallestGiniIndex +" "+ indexInArray);
		System.out.println("Mean : "+meanArray[indexInArray]);
		
		
		String[][] trainMatFeaturesLeft = new String[1000][100];
		String[] trainClassVectorLeft = new String [1000];
		
		String[][] trainMatFeaturesRight = new String[1000][100];
		String[] trainClassVectorRight = new String [1000];
		
		
		int rowCountLeft = 0;
		int colCountLeft = 0;
		int classCountLeft = 0;

		int rowCountRight = 0;
		int colCountRight = 0;
		int classCountRight = 0;

		// Split
		
		int i,j,k1=0,k2=0;
		
		for(i=0;i<row;i++) {
			if(Float.valueOf(trainMatFeatures[i][indexInArray])<meanArray[indexInArray]) {
				for (j=0;j<col;j++) {
					trainMatFeaturesLeft[k1][j]=trainMatFeatures[i][j];
					trainClassVectorLeft[k1]=trainClassVector[i];
				}
				k1++;
			} else {
				for (j=0;j<col;j++) {
					trainMatFeaturesRight[k2][j]=trainMatFeatures[i][j];
					trainClassVectorRight[k2]=trainClassVector[i];
				}
				k2++;
			}
		}
		
		
		rowCountLeft=classCountLeft=k1;
		colCountLeft=col;
		
		rowCountRight=classCountRight=k2;
		colCountRight=col;
		
		// create a node
		Node node = new Node(String.valueOf(meanArray[indexInArray]), indexInArray);
		node.left = split(trainMatFeaturesLeft,rowCountLeft,colCountLeft,trainClassVectorLeft); 
		node.right = split(trainMatFeaturesRight,rowCountRight,colCountRight,trainClassVectorRight); 
		return node;
		
	}
	
	//public static void createTree() {}
	
	public static float calculateGiniIndex(float C0, float C1) {
		float giniIndex;
		
		float sum = C0+C1;
		
		giniIndex= 1 - ((C0/sum)*(C0/sum)) - ((C1/sum)*(C1/sum));
		
		return giniIndex;
	}
	
		
	public static void predict(Node root,String[][] testMatFeatures, int row,int col, String[] testClassVector) {
		Node tmp;
		//String predictedClass;
		
		float positive=0;
		float negative=0;
		
		for(int i=0;i<row;i++) {
			tmp=root;
			while (tmp.feature != -1){
				int index = tmp.feature;
				String value = tmp.value;
				
				//System.out.println("tree raversal (feature) : "+index);
				//System.out.println("tree raversal (value) : "+value);
				
				String testValue = testMatFeatures[i][index];
				
				if(Float.valueOf(testValue)<Float.valueOf(value)){
					tmp=tmp.left;
				} else {
					tmp=tmp.right;
				}
				
			}
			
			predictedClass=tmp.value;
		
			trueClass = testClassVector[i];
			
			System.out.println("Predicted Class : " + predictedClass);
			System.out.println("True Class : " + testClassVector[i]);
			
			if(predictedClass.equals(testClassVector[i])) {
				positive++;
			} else {
				negative++;
			}
		
			// calculate TP(a), FN(b), FP(c), TN(d)
			if(predictedClass.equals("1")&&trueClass.equals("1")){
				a++;
			} else if(predictedClass.equals("0")&&trueClass.equals("1")) {
				b++;
			} else if(predictedClass.equals("1")&&trueClass.equals("0")) {
				c++;
			} else if(predictedClass.equals("0")&&trueClass.equals("0")) {
				d++;
			}
		}
		
		if(kFold==-1) {			
			float accuracytmp= positive/(positive+negative);
			System.out.println("Accuracy : " + accuracytmp);
			
			
			float accuracy = (a+d)/(a+b+c+d)*100;
			float pricision = (a)/(a+c)*100;
			float recall = (a)/(a+b)*100;
			float fMeasure = (2*a)/((2*a)+b+c)*100;
			
			// Display results
			System.out.println("-------- Results ---------");
			System.out.println("Accuracy : "+ accuracy);
			System.out.println("Precision : "+ pricision);
			System.out.println("Recall : "+ recall);
			System.out.println("F Measure : "+ fMeasure);
			
		} else {
			
			float accuracy = (a+d)/(a+b+c+d)*100;
			float pricision = (a)/(a+c)*100;
			float recall = (a)/(a+b)*100;
			float fMeasure = (2*a)/((2*a)+b+c)*100;
			
			Accuracy[kIndex]= accuracy;
			Pricision[kIndex]= pricision;
			Recall[kIndex] = recall;
			FMeasure[kIndex] = fMeasure;
			
		}
	}
	
	public static void processOrdinalValues() {
		
		
		for (int j=0;j<colCount;j++) {
			
			ArrayList<String> tmpList = new ArrayList<String>();
			
			for(int i=0;i<rowCount;i++) {
				if (!isNumeric(matFeatures[i][j])) {					
					if(!tmpList.contains(matFeatures[i][j])) {
						tmpList.add(matFeatures[i][j]);
					}
				}else {
					return;
				}
			}
			
			// Convert
			for(int i=0;i<rowCount;i++) {
				String feature = matFeatures[i][j];
				matFeatures[i][j]=String.valueOf(tmpList.indexOf(feature));
			}
		}
	}
	
	
	public static void levelPrint(Node root){
		int h = height(root);
		for(int i=1;i<=h;i++){
 			printLevels(root,i);
 			System.out.println("");
 		}
 	}
 	public static void printLevels(Node root, int h){
 		if(root==null) return;
 		if(h==1) System.out.print(" "+"(" + root.value+","+root.feature+")");
 		else{
 			printLevels(root.left,h-1);
 			printLevels(root.right,h-1);
 		} 	
       }
       public static int height(Node root){
 		if (root==null) return 0;
 		return 1 + Math.max(height(root.left),height(root.right));
 	}

	
	public static void main(String[] args) {
		read();
		if(kFold==-1) {			
			System.out.println("-------------- Origninal ---------------");
			display(matFeatures,rowCount,colCount,classVector);
			processOrdinalValues();
			System.out.println("-------------- Preprocessed ----------------");
			display(matFeatures,rowCount,colCount,classVector);
			//divide();
			
			
			divide();
			
			
			System.out.println("-------------- Input ----------------");
			display(matFeatures,rowCount,colCount,classVector);
			
			System.out.println("-------------- Train ----------------");
			display(trainMatFeatures,trainSize,colCount,trainClassVector);
			
			System.out.println("-------------- Test ----------------");
			display(testMatFeatures,rowCount-trainSize,colCount,testClassVector);
			
			//createTree();
			
			Node root = split(trainMatFeatures,trainSize,colCount,trainClassVector);
			System.out.println("------------- Tree -----------------");
			levelPrint(root);
			
			predict(root,testMatFeatures,rowCount-trainSize,colCount,testClassVector);
	
		} else {

			preProces();
			
			System.out.println("-------------- Preprocessed ----------------");
			display(matFeatures,rowCount,colCount,classVector);

			for(int i=0;i<kFold;i++) {
				
				
				divide();
			
				Node root = split(trainMatFeatures,trainSize,colCount,trainClassVector);
				System.out.println("------------- Tree -----------------");
				levelPrint(root);
				
				predict(root,testMatFeatures,rowCount-trainSize,colCount,testClassVector);
				
				
				System.out.println("-------------- Train ----------------");
				display(trainMatFeatures,trainSize,colCount,trainClassVector);
				
				System.out.println("-------------- Test ----------------");
				display(testMatFeatures,rowCount-trainSize,colCount,testClassVector);
				
					
				kIndex++;
				
			}
			
			float accuracy = 0;
			float pricision = 0;
			float recall = 0;
			float fMeasure = 0;
			
			for(int i=0;i<kFold;i++) {
				accuracy = accuracy+Accuracy[i];
				pricision = pricision+Pricision[i];
				recall = recall+Recall[i];
				fMeasure = fMeasure+FMeasure[i];
			}
			
			System.out.println("-------- Results Kfold---------");
			System.out.println("Accuracy : "+ accuracy/kFold);
			System.out.println("Precision : "+ pricision/kFold);
			System.out.println("Recall : "+ recall/kFold);
			System.out.println("F Measure : "+ fMeasure/kFold);
		
		}

	}
	
	

	
}
