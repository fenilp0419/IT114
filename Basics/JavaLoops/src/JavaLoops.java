public class JavaLoops{
	public static void main(String[] args) {
		
		int[] myIntArray = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
										15, 16, 17, 18, 19, 20};
		
		for (int num : myIntArray) {
			
			if (num % 2 == 0){
			System.out.println(num);
			}
			
		}
		
	}
}

/*
TO ACHIEVE THIS OUTPUT:

I made an array of integers between 1 to 20. 

I created a for loop where it picks every number from the array
and the if statement checks to see if each number divided by 2 has a remainder of 0.
If it does, that means it is a even number and it will output that.

*/ 