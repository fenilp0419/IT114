public class RecursionToLoopHW {
	public static int sum(int numb) {
		
		while (numb != 0) {
			
			int number = numb - numb;
			
			if(numb == 0) {
				return number + numb;
			}
			break;
		}
		return numb;
		
	}
	public static void main(String[] args) {
		System.out.println(sum(3));
	}

}
