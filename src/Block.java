import java.util.List;

public class Block {
	boolean validBit;
	String tag; 
	int address; 
	List<String> myValue;

	public Block (boolean valid, String tagIn, int addressIn, List<String> value){
		validBit = valid; // was this just initialized or not? 
		tag = tagIn;
		address = addressIn; 
		myValue = value; 
	}
}
