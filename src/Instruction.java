
public class Instruction {

	String type; // store or load 
    String address;
    int numBytes;
    String writeVal;

    // ex. load 0x000001 4 
    public Instruction(String type, int numBytes, String address) {
    	this.type = type; 
    	this.numBytes = numBytes;
    	this.address = address;
    	this.writeVal = ""; 
    }

    // ex. store 0x000000 4 deadbeef
    public Instruction(String type, int numBytes, String address, String writeVal) {
    	this.type = type; 
    	this.numBytes = numBytes;
    	this.address = address;
    	this.writeVal = writeVal;
    }
}
