package automa;

import java.util.StringTokenizer;
import java.util.Vector;

import language.Action;
import language.Argument;
import language.Condition;
import language.Expression;
import language.FieldExpression;
import language.SintaxErrorException;
import language.SubExpression;

public class AutomatonTable {

	public Vector<FieldExpression> fieldExpressions = new Vector<FieldExpression>();
	public States state;
	public Vector<Action> toAssign = new Vector<Action>();

	

	public AutomatonTable() {
		state = States.START;
	}

	//Di seguito vengono definiti gli elementi del linguaggio: (Vedere documentazione)
	
	//Verbi	
	public enum Verbs {
		TAKE;
	}
	//Attributi dell'articolo
	public enum Fields {
		BODY, TITLE, SUMMARY, IMAGE_PATH, FB_LIKES, FB_COMMENTS, RETWEETS, SITE_COMMENTS, TAGS, CATEGORY;
	}
    //Azioni definite nel linguaggio
	public enum Actions {
		DELETE_TAGS, DELETE_HREFS, DELETE_STRING, STORE_IMAGE_IN, GET_VALUE, GET_SCRIPT_VALUES
	}
	//Intervalli
	public enum Intervals {
		ALL, FIRST, LAST;
	}
	//-----------------------------------
	
	//Elementi dell'automa:
	//Input della automation table
	public enum Inputs {
		ATTRIBUTE, VERB, TAG, COND, IN, SEPARATOR, ACTION, ACTION_ARGUMENT, ACTION_CONDITION, ACTION_TARGET, END
	}
	//Stati dell'automation table
	public enum States {
		START, ATTRIBUTE, VERB, TAG, CONDITION, IN, SEPARATOR, ACTION, END
	}


	//In seguito vengono definite le procedure, che si occupano di parsare i vari macroelementi del linguaggio
	
	//Procedura che seleziona l'ATTRIBUTE della sottoespressione
	Procedure attribute = new Procedure() {
		public void execute(String instruction) throws SintaxErrorException {
			name = "attribute";
			//formatting della stringa
			String formatted = removeSpaces(instruction);
			if (formatted.startsWith(";")) {
				formatted = removeSpaces(formatted.substring(1));
			}
			if (formatted.startsWith("ACTION")) {
				output = Inputs.ACTION;
				outputInstruction = formatted;
				return;
			}
			FieldExpression f = new FieldExpression();
			//Verifica dell'esistenza dell'ATTRIBUTE 
			boolean found = false;
			for (int i = 0; i < Fields.values().length; i++) {
				if (formatted.startsWith(Fields.values()[i].name())) {
					f.fieldName = Fields.values()[i].name();
					formatted = formatted.substring(formatted.indexOf(Fields.values()[i].name()) + Fields.values()[i].name().length());
					found = true;
				}
			}
			if (!found) {
				output = Inputs.ACTION;
				outputInstruction = formatted;
				return;
			}
			fieldExpressions.add(f);
			formatted = removeSpaces(formatted);
			if (formatted.startsWith("=")) {
				outputInstruction = formatted.substring(1);
				state = States.ATTRIBUTE;
				output = Inputs.VERB;
			} else {
				throw new SintaxErrorException("MISSING OPERATOR STATEMENT", "attribute");
			}
		}
	};

	//Semplice procedura che verifica il verbo della sotto espressione 
	Procedure verb = new Procedure() {
		public void execute(String instruction) throws SintaxErrorException {
			name = "verb";
			String formatted = removeSpaces(instruction);
			if (formatted.startsWith("[")) {
				formatted = removeSpaces(formatted.substring(1));
			}
			if (formatted.startsWith("(")) {
				formatted = removeSpaces(formatted.substring(1));
			} else {
				throw new SintaxErrorException("MISSING BRACE STATEMENT", "verb");
			}
			formatted = removeSpaces(formatted);
			boolean found = false;
			for (int i = 0; i < Verbs.values().length; i++) {
				if (formatted.startsWith(Verbs.values()[i].name())) {
					outputInstruction = formatted.substring(Verbs.values()[i].name().length());
					state = States.VERB;
					output = Inputs.TAG;
					found = true;
				}
			}
			if (!found)
				throw new SintaxErrorException("MISSING VERB OR VERB NOT RECOGNIZED", "verb");
		}
	};

	//Procedura che si occupa di prelevare il parametro tag e il rispettivo intervallo
	Procedure tag = new Procedure() {
		public void execute(String instruction) throws SintaxErrorException {
			name = "tag";
			String formatted = removeSpaces(instruction);
			if (formatted.startsWith("TAG")) {
				formatted = formatted.substring(3);
			} else {
				throw new SintaxErrorException("MISSING TAG STATEMENT", "tag");
			}
			formatted = removeSpaces(formatted);
			String value;
			if (formatted.startsWith("'")) {
				formatted = formatted.substring(1);
				value = formatted.substring(0, formatted.indexOf("'"));
				formatted = removeSpaces(formatted.substring(formatted.indexOf("'") + 1));
				if (state.equals(States.VERB)) {
					Expression e = new Expression();
					fieldExpressions.lastElement().expressions.add(e);
				}
			} else {
				throw new SintaxErrorException("MISSING '", "tag");
			}
			SubExpression sub = new SubExpression();
			sub.tagValue = value;
			boolean found = false;
			String interval = "";
			for (int i = 0; i < Intervals.values().length; i++) {
				if (formatted.startsWith(Intervals.values()[i].name())) {
					interval = Intervals.values()[i].name();
					found = true;
					formatted = removeSpaces(formatted.substring(formatted.indexOf(Intervals.values()[i].name()) + Intervals.values()[i].name().length()));
				}
			}
			if (!found) {
				formatted = removeSpaces(formatted);
				char[] chars = formatted.toCharArray();
				String intr = "";
				for (int i = 0; i < chars.length; i++) {
					if (chars[i] == ' ' || chars[i] == ')') {
						intr = formatted.substring(0, i);
						formatted = formatted.substring(i);
						break;
					}
				}
				interval = intr;
			}
			sub.interval = interval;
			fieldExpressions.lastElement().expressions.lastElement().subexpressions.add(sub);
			state = States.TAG;
			output = Inputs.COND;
			outputInstruction = formatted;
		}
	};

	//Procedura che estrae le varie condizioni collegate alla selezione del tag
	Procedure condition = new Procedure() {
		public void execute(String instruction) throws SintaxErrorException {
			name = "condtion";
			boolean parseCondition = false;
			String formatted = removeSpaces(instruction);
			if (formatted.startsWith("IN")) {
				state = States.CONDITION;
				output = Inputs.IN;
				outputInstruction = formatted;
				return;
			} else if (formatted.startsWith(")")) {
				state = States.CONDITION;
				output = Inputs.SEPARATOR;
				outputInstruction = formatted.substring(1);
				return;
			}
			formatted = removeSpaces(formatted);
			if (formatted.startsWith("AND")) {
				formatted = removeSpaces(formatted.substring(3));
				parseCondition = true;
			} else if (formatted.startsWith("BUT")) {
				fieldExpressions.lastElement().expressions.lastElement().subexpressions.lastElement().hold = false;
				formatted = removeSpaces(formatted.substring(3));
				parseCondition = true;
			}
			if (formatted.startsWith("WHERE")) {
				formatted = removeSpaces(formatted.substring("WHERE".length()));
				parseCondition = true;
			}
			if (parseCondition) {
				Condition c = new Condition();
				String attr = "";
				char[] chars = formatted.toCharArray();
				for (int i = 0; i < chars.length; i++) {
					if (chars[i] == '!') {
						if (chars[i + 1] == '=') {
							c.operator = "!=";
							attr = formatted.substring(0, i);
							formatted = formatted.substring(i + 2);
							break;
						} else
							throw new SintaxErrorException("INVALID OPERATOR", "condition");
					} else if (chars[i] == '=') {
						c.operator = "=";
						attr = formatted.substring(0, i);
						formatted = formatted.substring(i + 1);
						break;
					}
				}
				attr = attr.replace(" ", "");
				formatted = removeSpaces(formatted);
				String value = "";
				if (formatted.startsWith("'")) {
					formatted = formatted.substring(1);
					value = formatted.substring(0, formatted.indexOf("'"));
					c.attribute = attr;
					c.value = value;
				} else {
					throw new SintaxErrorException("MISSING CLOSING '", "condition");
				}
				fieldExpressions.lastElement().expressions.lastElement().subexpressions.lastElement().conditions.add(c);
				output = Inputs.COND;
				outputInstruction = formatted.substring(formatted.indexOf("'") + 1);
			}
		}
	};

	//procedura che si occupa dell'elemento IN
	Procedure in = new Procedure() {
		public void execute(String instruction) throws SintaxErrorException {
			name = "in";
			String formatted = removeSpaces(instruction.substring(2));
			state = States.IN;
			output = Inputs.TAG;
			outputInstruction = formatted;
		}
	};

	//Procedura che si occupa di gestire gli elementi separatori
	Procedure separator = new Procedure() {
		public void execute(String instruction) throws SintaxErrorException {
			name = "separator";
			String formatted = removeSpaces(instruction);
			if (formatted.startsWith(")")) {
				formatted = removeSpaces(formatted.substring(1));
			}
			if (formatted.startsWith("+")) {
				formatted = formatted.substring(1);
				removeSpaces(formatted);
				if (formatted.startsWith("(")) {
					output = Inputs.VERB;
					outputInstruction = formatted;
					state = States.SEPARATOR;
				} else {
					throw new SintaxErrorException("MISSING CLOSING BRACE", "separator");
				}
			} else if (formatted.startsWith("OR")) {
				formatted = formatted.substring(2);
				fieldExpressions.lastElement().expressions.lastElement().or = true;
				removeSpaces(formatted);
				if (formatted.startsWith("(")) {
					output = Inputs.VERB;
					outputInstruction = formatted;
					state = States.SEPARATOR;
				} else {
					throw new SintaxErrorException("MISSING CLOSING BRACE", "separator");
				}
			} else if (formatted.startsWith("]")) {
				formatted = removeSpaces(formatted.substring(1).replace("\n", " "));
				output = Inputs.ATTRIBUTE;
				outputInstruction = formatted;
				state = States.SEPARATOR;
			} else {
				formatted = removeSpaces(formatted);
				output = Inputs.ACTION;
				outputInstruction = formatted;
				state = States.SEPARATOR;
			}
		}
	};

	//Procedura che si occupa di estrarre dall'istruzione le azioni
	Procedure action = new Procedure() {
		public void execute(String instruction) throws SintaxErrorException {
			name = "action";
			String formatted = removeSpaces(instruction);
			if (formatted.length() == 0) {
				state = States.END;
				output = Inputs.END;
				outputInstruction = "";
			}

			if (formatted.startsWith("ACTION")) {
				formatted = removeSpaces(formatted.substring("ACTION".length()));
				Action a = new Action();
				boolean found = false;
				for (int i = 0; i < Actions.values().length; i++) {
					if (formatted.startsWith(Actions.values()[i].name())) {
						a.name = Actions.values()[i];
						found = true;
						formatted = removeSpaces(formatted.substring(a.name.name().length()));
					}
				}
				if (!found) {
					throw new SintaxErrorException("MISSING ACTION OR ACTION NOT RECOGNIZED", "action");
				}
				toAssign.add(a);
				output = Inputs.ACTION_ARGUMENT;
				outputInstruction = formatted;
				state = States.ACTION;
			} else if (formatted.startsWith("&")) {
				formatted = removeSpaces(formatted.substring(1));
				boolean found = false;
				Action a = new Action();
				for (int i = 0; i < Actions.values().length; i++) {
					if (formatted.startsWith(Actions.values()[i].name())) {
						a.name = Actions.values()[i];
						found = true;
					}
				}
				if (!found) {
					throw new SintaxErrorException("MISSING ACTION OR ACTION NOT RECOGNIZED", "action");
				}
				formatted = removeSpaces(formatted.substring(a.name.name().length()));
				toAssign.add(a);
				output = Inputs.ACTION_ARGUMENT;
				outputInstruction = formatted;
				state = States.ACTION;
			}
		}
	};

	//Procedura che si occupa di estrarre dall'azione gli argomenti relativi
	Procedure actionArgument = new Procedure() {
		public void execute(String instruction) throws SintaxErrorException {
			name = "actionArgument";
			String formatted = removeSpaces(instruction);
			if (formatted.startsWith("'")) {
				// ARGUMENT NAME
				formatted = formatted.substring(1);
				String name = formatted.substring(0, formatted.indexOf("'"));
				formatted = removeSpaces(formatted.substring(name.length() + 1));
				Argument arg = new Argument();
				arg.value = name;
				// ACTION INTERVAL
				try {
					String interval = formatted.substring(0, formatted.indexOf(" "));
					arg.interval = interval;
				} catch (Exception e) {
				}
				toAssign.lastElement().arguments.add(arg);
				formatted = removeSpaces(formatted.substring(arg.interval.length()));
				output = Inputs.ACTION_CONDITION;
				outputInstruction = formatted;
			} else {
				throw new SintaxErrorException("MISSING CLOSING '", "actionArgument");
			}
		}
	};

	//Procedura che si occupa di estrarre dall'azione le condizioni relative ad essa
	Procedure actionCondition = new Procedure() {
		public void execute(String instruction) throws SintaxErrorException {
			name = "actionCondition";
			String formatted = removeSpaces(instruction);
			if (formatted.startsWith("|")) {
				output = Inputs.ACTION_ARGUMENT;
				outputInstruction = removeSpaces(formatted.substring(1));
				return;
			}
			if (formatted.startsWith("ON")) {
				output = Inputs.ACTION_TARGET;
				outputInstruction = formatted;
				return;
			}
			if (formatted.startsWith("&")) {
				output = Inputs.ACTION;
				outputInstruction = formatted;
				return;
			}
			if (formatted.startsWith("BUT")) {
				toAssign.lastElement().hold = false;
				formatted = removeSpaces(formatted.substring(3));
			}
			if (formatted.startsWith("WHERE") || formatted.startsWith("AND")) {
				formatted = removeSpaces(formatted.substring("WHERE".length()));
				String attr = "";
				String operator = "";
				String value = "";
				char[] chars = formatted.toCharArray();
				for (int i = 0; i < chars.length; i++) {
					if (chars[i] == ' ') {
						attr = formatted.substring(0, formatted.indexOf(" "));
					} else if (chars[i] == '!') {
						if (i + 1 < chars.length) {
							if (chars[i + 1] == '=') {
								attr = formatted.substring(0, formatted.indexOf("!"));
								operator = "!=";
							}
						}
					} else if (chars[i] == '=') {
						attr = formatted.substring(0, formatted.indexOf("="));
						operator = "=";
					}
				}
				formatted = removeSpaces(formatted.substring(formatted.indexOf("=")));
				if (formatted.startsWith("=")) {
					operator = "=";
					formatted = formatted.substring(1);
				} else if (formatted.startsWith("!=")) {
					operator = "!=";
					formatted = formatted.substring(2);
				}
				formatted = removeSpaces(formatted);
				if (formatted.startsWith("'")) {
					formatted = formatted.substring(1);
					value = formatted.substring(0, formatted.indexOf("'"));
				} else {
					throw new SintaxErrorException("MISSING ''", "actionCondition");
				}
				Condition c = new Condition();
				c.attribute = attr;
				c.operator = operator;
				c.value = value;
				toAssign.lastElement().arguments.lastElement().conditions.add(c);
				formatted = formatted.substring(formatted.indexOf(value) + value.length() + 1);
				output = Inputs.ACTION_CONDITION;
				outputInstruction = removeSpaces(formatted);
				return;
			}
		}
	};

	//Procedura che estrae il target su cui applicare l'azione
	Procedure actionTarget = new Procedure() {
		public void execute(String instruction) throws SintaxErrorException {
			name = "actionTarget";
			String formatted = removeSpaces(instruction);
			if (formatted.startsWith("ON")) {
				formatted = removeSpaces(formatted.substring(2));
			} else {
				throw new SintaxErrorException("MISSING 'ON' CLAUSE", "actionTarget");
			}
			formatted = removeSpaces(formatted);
			String[] targets = getActionTargets(formatted);
			for (int i = 0; i < targets.length; i++) {
				FieldExpression fe = getFieldExpression(removeSpaces(targets[i]));
				fe.actions.add(toAssign.lastElement());
			}
			toAssign.remove(toAssign.size() - 1);
			formatted = removeSpaces(formatted.substring(formatted.indexOf(targets[targets.length - 1]) + targets[targets.length - 1].length()));
			if (formatted.startsWith(";")) {
				output = Inputs.ACTION;
				outputInstruction = removeSpaces(formatted.substring(1));
			}
			if (formatted.startsWith("ACTION")) {
				output = Inputs.ACTION;
				outputInstruction = formatted;
			} else if (formatted.length() == 0) {
				output = Inputs.END;
				outputInstruction = "";
			} else {
				output = Inputs.ATTRIBUTE;
				outputInstruction = formatted;
			}
		}
	};

	Procedure end = new Procedure() {
		public void execute(String instruction) throws SintaxErrorException {
			name = "end";
			state = States.END;
			output = Inputs.END;
			outputInstruction = instruction;
		}
	};

	public static String removeSpaces(String instruction) {
		char[] instr = instruction.toCharArray();
		int cont = 0;
		for (int i = 0; i < instr.length; i++) {
			if (instr[i] == ' ') {
				cont++;
			} else {
				break;
			}
		}
		return instruction.substring(cont, instruction.length());
	}

	public FieldExpression getFieldExpression(String name) {
		for (int i = 0; i < fieldExpressions.size(); i++) {
			if (fieldExpressions.get(i).fieldName.equals(name)) {
				return fieldExpressions.get(i);
			}
		}
		return null;
	}

	abstract class Procedure {
		String outputInstruction;
		Inputs output;
		String name;

		public abstract void execute(String instruction) throws SintaxErrorException;

		public String getOutputInstruction() {
			return outputInstruction;
		}
	}

	public String[] getActionTargets(String s) {
		Vector<String> targets = new Vector<String>();
		String formatted = removeSpaces(s);
		while (true) {
			if (formatted.startsWith("ACTION") || formatted.length() == 0 || formatted.startsWith(";")) {
				break;
			}
			if (formatted.startsWith(",")) {
				formatted = removeSpaces(formatted.substring(1));
			}
			char[] chars = formatted.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				if (chars[i] == ' ' || chars[i] == ',' || chars[i] == ';') {
					targets.add(removeSpaces(formatted.substring(0, i)));
					formatted = removeSpaces(formatted.substring(i + 1));
					i = chars.length;
				}
			}

		}
		String[] ret = new String[targets.size()];
		for (int i = 0; i < targets.size(); i++) {
			ret[i] = targets.get(i);
		}
		return ret;
	}

	public String getStartingTarget(String s) {
		for (int i = 0; i < AutomatonTable.Fields.values().length; i++) {
			if (s.startsWith(AutomatonTable.Fields.values()[i].name())) {
				return AutomatonTable.Fields.values()[i].name();
			}
		}
		return null;
	}

	//Tabella di automazione
	//righe = stati
	//colonne = input
	public Procedure[][] table = { { attribute }, { null, verb }, { null, null, tag }, { null, null, tag, condition, in }, { null, null, null, null, in, separator }, { null, null, tag }, { attribute, verb, null, null, null, null, action }, { attribute, null, null, null, null, null, action, actionArgument, actionCondition, actionTarget, end } };

	public int getStateIndex(States state) {
		for (int i = 0; i < States.values().length; i++) {
			if (state.equals(States.values()[i])) {
				return i;
			}
		}
		return -1;
	}

	public int getInputIndex(Inputs in) {
		for (int i = 0; i < Inputs.values().length; i++) {
			if (in.equals(Inputs.values()[i])) {
				return i;
			}
		}
		return -1;
	}
}