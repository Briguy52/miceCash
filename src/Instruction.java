/**
 * Created by BrianLin on 4/10/16.
 */
public class Instruction {

	private String instrType; // store or load 
    private String writeVal;

    private int address;
    private int numBytes;

    // ex. load 0x000001 4 
    public Instruction(int address, int numBytes, String instrType) {
    	this.address = address;
    	this.numBytes = numBytes;
    	this.instrType = instrType; 
    	this.writeVal = ""; 
    }

    // ex. store 0x000000 4 deadbeef
    public Instruction(int address, int numBytes, String instrType, String writeVal) {
    	this.address = address;
    	this.numBytes = numBytes;
    	this.instrType = instrType;
    	this.writeVal = writeVal;
    }

	public String getInstrType() {
		return instrType;
	}

	public void setInstrType(String instrType) {
		this.instrType = instrType;
	}

	public String getWriteValue() {
		return writeVal;
	}

	public void setWriteValue(String writeValue) {
		this.writeVal = writeValue;
	}

	public int getAddress() {
		return address;
	}

	public void setAddress(int address) {
		this.address = address;
	}

	public int getNumBytes() {
		return numBytes;
	}

	public void setNumBytes(int numBytes) {
		this.numBytes = numBytes;
	}

    
}
