package language;

import java.util.Vector;

public class Expression {
	//classe che descrive la struttura di una Espressione (vedi documentazione)
	//Un'espressione è formata da più sottoespressioni
	public Vector<SubExpression> subexpressions = new Vector<SubExpression>();
	public boolean or = false;

	public void print() {
		for (SubExpression e : subexpressions) {
			e.print();
		}
	}
}
