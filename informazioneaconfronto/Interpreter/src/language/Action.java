package language;

import java.util.Vector;

import automa.AutomatonTable;

public class Action {

	//classe che descrive la struttura di una azione (vedi documentazione)
	public AutomatonTable.Actions name;
	public boolean hold = true;
	public Vector<Argument> arguments = new Vector<Argument>();

	public void print() {
		for (Argument a : arguments) {
			a.print();
		}
	}

}
