
public class Block {
	
	private String myValue; 
	private int blockSize; 
	private int tag; 
	
	public Block() {};
	
	public Block(int tag, int size) {
		this.blockSize = size; 
		this.tag = tag; 
		this.initValue(this.blockSize);
	}
	
	private void initValue(int size) {
		for (int i = 0; i < size * 8; i++) {
			this.myValue += "0";
		}	
	}
	
	public void writeToEnd() {
		
	}
	
}