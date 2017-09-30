package language;

import java.util.Vector;

import automa.AutomatonTable;

public class Argument {
	//classe che descrive la struttura di un argomento relativo ad una azione (vedi documentazione)
	public String value;
	public String interval;
	public Vector<Condition> conditions = new Vector<Condition>();

	public void print() {
		System.out.println("VALUE: " + value);
		System.out.println("INTERVAL: " + interval);
		for (Condition c : conditions) {
			c.print();
		}
	}

}
