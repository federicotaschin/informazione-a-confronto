package language;

public class Condition {
	//classe che descrive la struttura di una condizione (vedi documentazione)
	public String attribute, operator, value;

	public void print() {
		System.out.println("CONDITION: " + attribute + "=" + value);
	}
}
