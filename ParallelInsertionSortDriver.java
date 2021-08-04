package p1;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * 
* This class parrallel sorts an array using insertion sort. The array is sorted
* by each fork which will eventually join into one, therefore the entire array is
* not sorted. It is impossible to parrallel sort using insertion sort because it has
* dependencies on the previous elements.
* 
* This is not a good example to showcase parallel sort, this is just for fun.
*
* @author Alan Dobrzelecki
*
 */
public class ParallelInsertionSortDriver {
	int numberOfForks = 0;
	public static void main(String[] args) {
		
		int[] list = new int[100000];
		
		// Fill array with random ints
		for (int i = 0; i < list.length; i++) {
			list[i] = (int) (Math.random() * 50);
		}
		
		long startTime = System.currentTimeMillis();
		parallelInsertionSort(list);
		long endTime = System.currentTimeMillis();
		System.out.println("Parallel Insertion Sort took " + (endTime - startTime) + "ms");
		
		int[] list1 = new int[10000];
		
		// Fill array with random ints
		for (int i = 0; i < list.length; i++) {
			list[i] = (int) (Math.random() * 50);
		}
		
		long startTime1 = System.currentTimeMillis();
		Arrays.sort(list1);
		long endTime1 = System.currentTimeMillis();
		System.out.print("Quick Sort using Arrays class took " + (endTime1 - startTime1) + "ms");
	}
	
	public static void parallelInsertionSort(int[] list) {	
		ParallelInsertionSort insertSort = new ParallelInsertionSort(list, 0, list.length);
		RecursiveAction task = insertSort;
		ForkJoinPool pool = new ForkJoinPool();
		pool.invoke(task);
		System.out.println("Number of forks: " + insertSort.getNumberOfForks());
	}
	
	/**
	 * Inner class will sort an array using insertion sort
	 */
	public static class ParallelInsertionSort extends RecursiveAction {
		private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();
		private static final int THRESHOLD = 1000;
		public static int numberOfForks = 0;
		private int[] list;
		private int low, high;
		
		/**
		 * Constructor for creating an instance of ParallelInsertionSort. Takes in a int array.
		 * Also sets the threshold for sorting depending on the length of the list and the 
		 * available processors.
		 * 
		 * @param list
		 */
		ParallelInsertionSort(int[] list, int low, int high) {
			this.list = list;
			this.low = low;
			this.high = high;
		}
		
		/**
		 * Insertion sort algorithim else splits the array
		 */
		@Override
		public void compute() {
			// If the array list is small then just do normal sort
			if (list.length < THRESHOLD)
		        Arrays.sort(list);
			/* If the array can be split equally into all processors then run the sort
			 * 
			 * For example: 
			 * 		If list has a length of 10000 elements and you have 8 processors the 
			 * 		condition will run when the forked length is about 12,500 in order
			 * 		to be effecient. 
			 */
			else if (high - low <= list.length / PROCESSORS) {
				for (int i = low + 1; i < high; i++) {
					int current = list[i];
					for (int j = i - 1; j >= low; j--) {
						if (list[j] > current) {
							list[j + 1] = list[j];	
							list[j] = current;
						}
					}
				}
			}
			else {
				numberOfForks++;
				// Recursive sort
				int mid = (low + high) / 2;
				// Wait for all forks to come back and join them 
				invokeAll(new ParallelInsertionSort(list, low, mid), new ParallelInsertionSort(list, mid, high));
			}
		}

		/**
		 * @return the numberOfForks
		 */
		public int getNumberOfForks() {
			return numberOfForks;
		}
	}
}

