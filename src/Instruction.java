/**
 * Created by BrianLin on 4/10/16.
 */
public class Instruction {

	private String myType;
    private String myWriteValue;

    private int myAddress;
    private int myNumBytes;

    public Instruction(int address, int numBytes, String type) {
    	this.myAddress = address;
    	this.myNumBytes = numBytes;
    	this.myType = type; 
    	this.myWriteValue = ""; 
    }

    public Instruction(int address, int numBytes, String type, String writeValue) {
    	this.myAddress = address;
    	this.myNumBytes = numBytes;
    	this.myType = type;
    	this.myWriteValue = writeValue;
    }

	public String getMyType() {
		return myType;
	}

	public void setMyType(String myType) {
		this.myType = myType;
	}

	public String getMyWriteValue() {
		return myWriteValue;
	}

	public void setMyWriteValue(String myWriteValue) {
		this.myWriteValue = myWriteValue;
	}

	public int getMyAddress() {
		return myAddress;
	}

	public void setMyAddress(int myAddress) {
		this.myAddress = myAddress;
	}

	public int getMyNumBytes() {
		return myNumBytes;
	}

	public void setMyNumBytes(int myNumBytes) {
		this.myNumBytes = myNumBytes;
	}

    
}
