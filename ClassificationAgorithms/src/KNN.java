import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class KNN {

	private static Map<List<Double>, Integer> testData = new LinkedHashMap<>();
	private static Map<List<Double>, Integer> trainData = new LinkedHashMap<>();

	private static final int k = 5;
	static double a = 0, b = 0, c = 0, d = 0;

	private static final String testDataFile = "project3_dataset3_test.txt";
	private static final String trainDataFile = "project3_dataset3_train.txt";

	public static void main(String[] args) {
		System.out.println("k ="+k);
		kNearestNeighbours(testDataFile, trainDataFile, k);
	}

	private static void kNearestNeighbours(String testDataFile, String trainDataFile, int k) {
		testData = read(testDataFile);
		trainData = read(trainDataFile);
		classify(testData, trainData);
	}

	private static Map<List<Double>, Integer> read(String inputFile) {
		Map<List<Double>, Integer> dataMap = new LinkedHashMap<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			String line;
			List<Double> rows;
			while (null != (line = br.readLine())) {
				String[] col = line.split("\t");
				rows = new ArrayList<Double>();
				for (int i = 0; i < col.length - 1; i++)
					rows.add(Double.parseDouble(col[i]));
				dataMap.put(rows, Integer.parseInt(col[col.length - 1]));
			}
			br.close();
			return dataMap;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dataMap;

	}

	private static Map<List<Double>, Integer> normalize(Map<List<Double>, Integer> inputMap) {
		Map<List<Double>, Integer> normDataMap = new LinkedHashMap<>();
		List<Double> sum = new ArrayList<>();
		boolean isFirst = true;
		for (List<Double> row : inputMap.keySet()) {
			for (int i = 0; i < row.size(); i++) {
				if (isFirst) {
					sum.add(i, row.get(i));
				} else
					sum.set(i, sum.get(i) + row.get(i));
			}
			isFirst = false;
		}

		List<Double> temp;
		for (List<Double> row : inputMap.keySet()) {
			temp = new ArrayList<>();
			for (int i = 0; i < row.size(); i++) {
				temp.add(formatData(row.get(i) / sum.get(i)));
			}
			normDataMap.put(temp, inputMap.get(row));
		}
		return normDataMap;
	}

	private static void classify(Map<List<Double>, Integer> testData, Map<List<Double>, Integer> trainData) {
		for (List<Double> x : testData.keySet()) {
			Map<List<Double>, Double> localDisMap = new LinkedHashMap<>();
			for (List<Double> y : trainData.keySet()) {
				double dis = eucDistance(x, y);
				localDisMap.put(y, formatData(dis));
			}
			localDisMap = sortByValue(localDisMap);
			// System.out.println(disMapSorted);
			int i = 0, count1 = 0, count0 = 0;
			for (List<Double> row : localDisMap.keySet()) {
				if (i < k) {
					if (trainData.get(row) == 0)
						count0++;
					if (trainData.get(row) == 1)
						count1++;
				}
				i++;
			}

			int clas = count0 > count1 ? 0 : 1;
			if(clas == 1 && testData.get(x) == 1) a++;
			if(clas == 0 && testData.get(x) == 1) b++;
			if(clas == 1 && testData.get(x) == 0) c++;
			if(clas == 0 && testData.get(x) == 0) d++;
		}
		double accuracy = formatData((a + d) / (a + b + c + d));
		double precision = formatData((a) / (a + c));
		double recall = formatData((a) / (a + b));
		double f1 = formatData((2*a) / (2*a + b + c));
		System.out.println("Accuracy = "+accuracy +" Precision = "+precision +" Recall = "+ recall+" F1-Measure = "+ f1);
	}

	private static Map<List<Double>, Double> sortByValue(Map<List<Double>, Double> unsortMap) {

		// 1. Convert Map to List of Map
		List<Map.Entry<List<Double>, Double>> list = new LinkedList<Map.Entry<List<Double>, Double>>(
				unsortMap.entrySet());

		// 2. Sort list with Collections.sort(), provide a custom Comparator
		// Try switch the o1 o2 position for a different order
		Collections.sort(list, new Comparator<Map.Entry<List<Double>, Double>>() {
			public int compare(Entry<List<Double>, Double> o1, Entry<List<Double>, Double> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}

		});

		// 3. Loop the sorted list and put it into a new insertion order Map
		// LinkedHashMap
		Map<List<Double>, Double> sortedMap = new LinkedHashMap<List<Double>, Double>();
		for (Map.Entry<List<Double>, Double> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	private static double eucDistance(List<Double> a, List<Double> b) {
		double dis = 0.0;
		for (int i = 0; i < a.size(); i++) {
			dis += Math.pow(a.get(i) - b.get(i), 2);
		}
		return Math.sqrt(dis);
	}

	private static String NUM_FORMAT = "#.#####";

	private static Double formatData(Double val) {
		DecimalFormat df = new DecimalFormat(NUM_FORMAT);
		return Double.parseDouble(df.format(val));
	}

}
