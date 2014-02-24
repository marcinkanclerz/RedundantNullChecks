package submit;

public class TestClass {
	public int commutativity(int y, int z) {
		int x = 1;
		x = y - z;
		x = z + y;
		x = z + y;
		return x;
	}
	
	// This one actually won't work, cause there's a bug:
	
//	public int ifThenElse(int y, int z) {
//		int x = 1, u;
//		
//		if (x == 1) {
//			x = y - z;
//		}
//		
//		u = y - z;
//		
//		return x;
//	}
}
