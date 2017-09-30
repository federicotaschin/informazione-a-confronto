package language;

import java.util.Vector;

public class FieldExpression {
	//classe che descrive la struttura di una espressione relativa ad un attributo dell'articolo (vedi documentazione)
	//essa è composta da più espressioni ed azioni
	public String fieldName;
	public Vector<Action> actions = new Vector<Action>();
	public Vector<Expression> expressions = new Vector<Expression>();

	public void print() {

		System.out.println("FIELD_NAME=" + fieldName);
		for(Expression ex :expressions){
			ex.print();
		}
		
		for(Action a:actions){
			a.print();
		}

	}

}
