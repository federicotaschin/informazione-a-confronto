package automa;

import java.util.Vector;

import language.Expression;
import language.FieldExpression;
import language.SintaxErrorException;
import language.SubExpression;

import org.jsoup.select.Elements;

import automa.AutomatonTable.Procedure;

public class InterpreterAutomaton {

	public AutomatonTable table = new AutomatonTable();
    // Questa classe si occupa di parsare il linguaggio eseguendo le procedure dettate dalla tabella di automazione
	public InterpreterAutomaton(String instr) throws SintaxErrorException {
		String instruction = instr.replace("\n", " ");
		int input = table.getInputIndex(AutomatonTable.Inputs.ATTRIBUTE);
		while (!table.state.equals(AutomatonTable.States.END)) {
			int state = table.getStateIndex(table.state);
			Procedure  p=null;
			try{
				//chiamo la procedura relativa all'input ricevuto e allo stato attuale
			p = table.table[state][input];			
			p.execute(instruction);
			//setto il nuovo input
			input = table.getInputIndex(p.output);
			instruction = p.outputInstruction;
			}catch(Exception e){
				e.printStackTrace();
				break;
			}
		}
	}

	public Vector<FieldExpression> getFieldExpressions() {
		return table.fieldExpressions;
	}

}
