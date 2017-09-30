package language;

public class SintaxErrorException extends Exception {

	private static final long serialVersionUID = 1L;
	String where, procName;
	//Eccezione generata da un errore nella scrittura del codice
	public SintaxErrorException(String where, String procName) {
		this.where = where;
		this.procName = procName;
	}

	@Override
	public void printStackTrace() {
		System.out.println("Sintax error on " + where + " in procedure " + procName);

	}

}
