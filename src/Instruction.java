/**
 * Created by BrianLin on 4/10/16.
 */
public class Instruction {

	private String type; // store or load 
    private String address;
    private int numBytes;
    private String writeVal;

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

	public String getInstrType() {
		return type;
	}

	public void setInstrType(String instrType) {
		this.type = instrType;
	}

	public String getWriteValue() {
		return writeVal;
	}

	public void setWriteValue(String writeValue) {
		this.writeVal = writeValue;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getNumBytes() {
		return numBytes;
	}

	public void setNumBytes(int numBytes) {
		this.numBytes = numBytes;
	}

    
}
