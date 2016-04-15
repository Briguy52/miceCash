
public class oldBlock {
	
	private String myValue; 
	private int blockSize; 
	private int tag; 
	
	// Blocks are basically just a String that has 8 chars 
	
	public oldBlock() {};
	
	public oldBlock(int tag, int size) {
		this.blockSize = size; 
		this.tag = tag; 
		this.initValue(this.blockSize);
	}
	
	@Override
	public boolean equals(Object potentialBlock) {
		return this.tag == ((oldBlock) potentialBlock).tag;
	}
	
	private void initValue(int size) {
		for (int i = 0; i < size * 8; i++) {
			this.myValue += "0";
		}	
	}
	
	public String read(int blkOff, int numBytes) {
		int start = blkOff * 8;
		int end = numBytes * 8;
		return myValue.substring(start, end + start);
	}
	
	public void writeByte(String value, int blkOff) {
		this.write(value, blkOff, 1);
	}
	
	public void write(String value, int blkOff, int numBytes) {
		int scaledOffset = blkOff * 8; 
		String leftSide = myValue.substring(0, scaledOffset);
		String rightSide = myValue.substring(scaledOffset + value.length()); // to end
		this.myValue = leftSide + value + rightSide;
	}
	
}