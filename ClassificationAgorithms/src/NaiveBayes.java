import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class NaiveBayes {
	
	public static String inputFile = "project3_dataset4.txt";
	
	public static String[] testX = {"overcast","mild","normal","weak"};
	
	public static boolean testCaseProvided = true;
	
	public static int kFold = -1;
	public static int kIndex = 0;
	
	public static float Accuracy[] = new float[100];
	public static float Pricision[] = new float[100];
	public static float Recall[] = new float[100];
	public static float FMeasure[] = new float[100];
	
	public static boolean zeroProbability = false;
	public static int zeroProbabilityScaling=0;
	
	public static String[][] matFeatures = new String[1000][100];
	public static String[] classVector = new String [1000];
	
	public static String[][] trainMatFeatures = new String[1000][100];
	public static String[] trainClassVector = new String [1000];
	
	public static String[][] testMatFeatures = new String[1000][100];
	public static String[] testClassVector = new String [1000];
	
	
	public static int rowCount = 0;
	public static int colCount = 0;
	public static int classCount = 0;
	
	public static float prob_h0 = 0;
	public static float prob_h1 = 0;
	
	public static float[] prob_x_given_h0 = new float[100];
	public static float[] prob_x_given_h1 = new float[100];
	
	public static float prob_x_sum = 1;
	
	public static float prob_x_given_h0_sum = 1;
	public static float prob_x_given_h1_sum = 1;
	
	public static float prob_h0_given_x_sum = 1;
	public static float prob_h1_given_x_sum = 1;
	
	
	public static float[] prob_x = new float[100];
	public static float prob_normalization_factor= 10;
	
	public static String predictedClass;
	public static String trueClass;
	
	public static float positiveMatch = 0 ;
	public static float negativeMatch = 0 ;
	
	public static float a=0;
	public static float b=0;
	public static float c=0;
	public static float d=0;
	
	public static int trainSize;
	
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
		    		//System.out.println(matFeatures[rowCount][i]);
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
	
	public static boolean isNumeric(String str)
	{
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
	
	public static void calculate_probability_h() {
		for (int i=0;i<trainSize;i++) {
			if(trainClassVector[i].equals("0")) {
				prob_h0++;
			} else if (trainClassVector[i].equals("1")) {
				prob_h1++;
			}
		}

		System.out.println("Count H0 = "+prob_h0);
		System.out.println("Count H1 = "+prob_h1);
		
		float sum = prob_h0+prob_h1;
		
		prob_h0 = prob_h0/sum;
		prob_h1 = prob_h1/sum;
		
		System.out.println("Probability H0 = "+prob_h0);
		System.out.println("Probability H1 = "+prob_h1);
	
	}
	
	public static void predictTestSet() {
		if(zeroProbability) {
			zeroProbabilityScaling=1;
		}
		
		prob_x_sum = zeroProbabilityScaling;
		prob_x_given_h0_sum = zeroProbabilityScaling;
		prob_x_given_h1_sum = zeroProbabilityScaling;
		
		for(int j=0;j<colCount;j++) {
			String feature = testX[j];
			
			float count_x=zeroProbabilityScaling;
			float countH0=zeroProbabilityScaling;
			float countH1=zeroProbabilityScaling;

			float count_x_H0=zeroProbabilityScaling;
			float count_x_H1=zeroProbabilityScaling;

			
			for(int k=0;k<trainSize;k++){
				
				if(trainClassVector[k].equals("0")) {
					countH0++;
					if (trainMatFeatures[k][j].equals(feature)) {
						count_x_H0++;
						count_x++;
					}
				} else if (trainClassVector[k].equals("1")) {
					countH1++;
					if (trainMatFeatures[k][j].equals(feature)) {
						count_x_H1++;
						count_x++;
					}
				}
				
			} // end of for
			
			prob_x[j] = (count_x/trainSize);
			prob_x_given_h0[j] = (count_x_H0/countH0);
			prob_x_given_h1[j] = (count_x_H1/countH1);
			
		}// end of outer for
		
		System.out.println("--------- Test set -----------");
		for (int k=0;k<colCount;k++) {
			System.out.print(testX[k] + " ");	
		}
		System.out.println();
		prob_h0_given_x_sum = prob_h0*sumAllProbabilities(prob_x_given_h0,colCount)/sumAllProbabilities(prob_x,colCount);
		prob_h1_given_x_sum = prob_h1*sumAllProbabilities(prob_x_given_h1,colCount)/sumAllProbabilities(prob_x,colCount);
		
		System.out.println("Probability(H0_X) = " + prob_h0_given_x_sum);
		System.out.println("Probability(H1_X) = " + prob_h1_given_x_sum);
		
		if (prob_h0_given_x_sum > prob_h1_given_x_sum) {
			predictedClass = "0";
		} else {
			predictedClass = "1";
		}
		System.out.println("Predicted class = " + predictedClass);

	}
	
	public static void predictNoTestSet() {
		
		if(zeroProbability) {
			zeroProbabilityScaling=1;
		}
		
		for(int i=0;i<rowCount-trainSize;i++) {
			
			// Intialize
			prob_x_sum = zeroProbabilityScaling;
			prob_x_given_h0_sum = zeroProbabilityScaling;
			prob_x_given_h1_sum = zeroProbabilityScaling;
			
			for(int j=0;j<colCount;j++) {
				
				String feature = testMatFeatures[i][j];
				
				float count_x=zeroProbabilityScaling;
				float countH0=zeroProbabilityScaling;
				float countH1=zeroProbabilityScaling;

				float count_x_H0=zeroProbabilityScaling;
				float count_x_H1=zeroProbabilityScaling;
				
				for(int k=0;k<trainSize;k++){
					
					if(trainClassVector[k].equals("0")) {
						countH0++;
						if (trainMatFeatures[k][j].equals(feature)) {
							count_x_H0++;
							count_x++;
						}
					} else if (trainClassVector[k].equals("1")) {
						countH1++;
						if (trainMatFeatures[k][j].equals(feature)) {
							count_x_H1++;
							count_x++;
						}
					}
					
				}
				
				prob_x[j] = (count_x/trainSize)*prob_normalization_factor;
				prob_x_given_h0[j] = (count_x_H0/countH0)*prob_normalization_factor;
				prob_x_given_h1[j] = (count_x_H1/countH1)*prob_normalization_factor;
				
			}
			//System.out.print();
			System.out.println("--------- Test set -----------");
			for (int k=0;k<colCount;k++) {
				System.out.print(testMatFeatures[i][k] + " ");	
			}
			System.out.println();
			System.out.println("------- Probabilities -------");
			System.out.print("Prob_x : ");
			for (int k=0;k<colCount;k++) {
				System.out.print(prob_x[k] + " ");	
			}
			System.out.println();
			System.out.print("Prob_X_H0 : ");
			for (int k=0;k<colCount;k++) {
				System.out.print(prob_x_given_h0[k] + " ");	
			}
			System.out.println();
			System.out.print("Prob_X_H1 : ");
			for (int k=0;k<colCount;k++) {
				System.out.print(prob_x_given_h1[k] + " ");	
			}
			System.out.println();
			System.out.println("Probability(X) = " + sumAllProbabilities(prob_x,colCount));
			System.out.println("Probability(X_H0) = " + sumAllProbabilities(prob_x_given_h0,colCount));
			System.out.println("Probability(X_H1) = " + sumAllProbabilities(prob_x_given_h1,colCount));
			
			prob_h0_given_x_sum = prob_h0*sumAllProbabilities(prob_x_given_h0,colCount)/sumAllProbabilities(prob_x,colCount);
			prob_h1_given_x_sum = prob_h1*sumAllProbabilities(prob_x_given_h1,colCount)/sumAllProbabilities(prob_x,colCount);
			
			System.out.println("Probability(H0_X) = " + prob_h0_given_x_sum);
			System.out.println("Probability(H1_X) = " + prob_h1_given_x_sum);
			
			
			if (prob_h0_given_x_sum > prob_h1_given_x_sum) {
				predictedClass = "0";
			} else {
				predictedClass = "1";
			}
			
			trueClass = testClassVector[i];
			
			System.out.println("Predicted class = " + predictedClass);
			System.out.println("True Class = " + testClassVector[i]);
			
			// Code to calculate accuracy
			if(predictedClass.equals(testClassVector[i])) {
				positiveMatch++;
			} else {
				negativeMatch++;
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
			System.out.println("Accuracy = " + positiveMatch/(positiveMatch+negativeMatch)*100);
			
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
	
	public static float sumAllProbabilities(float[] prob,int count) {
		float sumAllProbabilities = 1;
		for(int i=0;i<count;i++) {
			sumAllProbabilities = sumAllProbabilities*prob[i];
		}
		return sumAllProbabilities;
	}
	
	public static void main(String[] args) {
		
		read();
		
		if(testCaseProvided){

			trainMatFeatures = matFeatures;
			trainSize = rowCount;
			
			trainClassVector = classVector;
			
			calculate_probability_h();
			
			predictTestSet();
			
		}  
		else {

			if(kFold==-1) {				
				System.out.println("-------------- Origninal ---------------");
				display(matFeatures,rowCount,colCount,classVector);
				preProces();
				System.out.println("-------------- Preprocessed ----------------");
				display(matFeatures,rowCount,colCount,classVector);
				divide();
				
				System.out.println("-------------- Input ----------------");
				display(matFeatures,rowCount,colCount,classVector);
				
				System.out.println("-------------- Train ----------------");
				display(trainMatFeatures,trainSize,colCount,trainClassVector);
				
				System.out.println("-------------- Test ----------------");
				display(testMatFeatures,rowCount-trainSize,colCount,testClassVector);
				
				calculate_probability_h();
				
				predictNoTestSet();
				
			} else {
				preProces();
				
				System.out.println("-------------- Preprocessed ----------------");
				display(matFeatures,rowCount,colCount,classVector);

				for(int i=0;i<kFold;i++) {
					
					
					divide();
				
					System.out.println("-------------- Train ----------------");
					display(trainMatFeatures,trainSize,colCount,trainClassVector);
					
					System.out.println("-------------- Test ----------------");
					display(testMatFeatures,rowCount-trainSize,colCount,testClassVector);
					
					
					calculate_probability_h();
					predictNoTestSet();
					
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


}
