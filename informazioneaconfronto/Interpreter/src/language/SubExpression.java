package language;

import java.util.Vector;

import automa.AutomatonTable;

public class SubExpression {
	//classe che descrive la struttura di una sottoespressione (vedi documentazione)
	//Struttura: TAG INTERVAL [CONDITIONS]
	public String tagValue;
	public String interval;
	public boolean hold = true;
	public Vector<Condition> conditions = new Vector<Condition>();

	public void print() {
		System.out.println("TAG VALUE = " + tagValue);
		System.out.println("INTERVAL = " + interval);
		System.out.println("HOLD = " + hold);

		for (Condition c : conditions) {
			c.print();
		}
	}

}
