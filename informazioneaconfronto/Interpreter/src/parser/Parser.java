package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

import language.Action;
import language.Argument;
import language.Condition;
import language.Expression;
import language.FieldExpression;
import language.SintaxErrorException;
import language.SubExpression;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import variables.Attributes;
import automa.AutomatonTable;
import automa.InterpreterAutomaton;

public class Parser {

	public Article article = new Article();
	Document doc;
	Vector<ActionExecuter> executers = new Vector<ActionExecuter>();
	String siteUrl;
	

	public Parser(String url, String siteUrl, String category) throws MalformedURLException, IOException {
		//Questa classe si occupa di parsare il codice HTML e selezionare gli elementi indicati dalle istruzioni
		article.link = url.replace("/?rss", "");
		doc = Jsoup.parse(new URL(article.link), 3000);
		article.category = category;
		executers.add(store_image_in);
		executers.add(get_value);
		executers.add(delete_string);
		executers.add(delete_tag);
		executers.add(getScriptvalues);
		this.siteUrl = siteUrl;
	}

	public void parse(String instruction) throws SintaxErrorException {
		InterpreterAutomaton instruct = new InterpreterAutomaton(instruction); //Parso l'istruzione
		
		//Questi cicli si occupano di selezionare ciclicamente gli elementi della pagina che rispettano le condizioni
		//imposte dall'istruzione. Man mano il programma estrae sempre più in profondita gli elementi seguendo le clausole IN 
		//definite dall'istruzione. Ad ogni ciclo quindi la selezione degli elementi si restringe fino ad arrivare al risultato
		
		//Per ogni attributo 
		for (FieldExpression fieldExpression : instruct.table.fieldExpressions) {
			Elements html = new Elements();
			for (Expression expression : fieldExpression.expressions) {  //per ogni espressione dell'attributo
				Elements result = new Elements();

				for (int j = expression.subexpressions.size() - 1; j > -1; j--) { //Esegue un ciclo che man mano restringe la selezione
					SubExpression sub = expression.subexpressions.get(j);
					Elements temp = new Elements();
					//a seconda dell'intervallo seleziono il numero di elementi
					if (result.size() == 0) {
						if (sub.tagValue.equalsIgnoreCase("ANY")) {
							temp = doc.body().children();
							for (int i = 0; i < sub.conditions.size(); i++) {
								Condition c = sub.conditions.get(i);
								if (c.operator.equals("=")) {
									temp = doc.getElementsByAttributeValue(c.attribute, c.value);
									i = sub.conditions.size();
								}
							}
						} else {
							temp = doc.getElementsByTag(sub.tagValue);
						}
					} else {
						if (sub.tagValue.equalsIgnoreCase("ANY")) {
							Condition cond = null;
							for (int i = 0; i < sub.conditions.size(); i++) {
								Condition c = sub.conditions.get(i);
								if (c.operator.equals("=")) {
									cond = c;
									i = sub.conditions.size();
								}
							}
							if (cond != null) {
								for (Element e : result) {
									temp.addAll(e.getElementsByAttributeValue(cond.attribute, cond.value));
								}
							} else {
								for (Element e : result) {
									temp.addAll(e.children());
								}
							}
						} else {
							for (Element e : result) {
								Elements es = e.getElementsByTag(sub.tagValue);
								if (es.size() > 0)
									temp.addAll(es);
							}
						}

					}
					for (int i = 0; i<temp.size(); i++) {
						if (!matchConditions(temp.get(i), sub.conditions)) {
							temp.remove(temp.get(i));
						}
					}
					result = new Elements();
					if (sub.interval.equalsIgnoreCase("FIRST")) {
						result.add(temp.get(0));
					} else if (sub.interval.equalsIgnoreCase("LAST")) {
						result.add(temp.last());
					} else if (sub.interval.equalsIgnoreCase("ALL")) {
						result.addAll(temp);
					} else {
						int n;
						try {
							n = Integer.parseInt(sub.interval);
							result.add(temp.get(n));
						} catch (Exception e) {
							try {
								StringTokenizer tok = new StringTokenizer(sub.interval, "-");
								int st = Integer.parseInt(tok.nextToken());
								int end = Integer.parseInt(tok.nextToken());
								for (int i = 0; i < temp.size(); i++) {
									if (i >= st && i <= end)
										result.add(temp.get(i));
								}
							} catch (Exception ex) {
								result.addAll(temp);
							}
						}
					}

				}
				if (expression.or) {
					if (html.size() == 0) {
						html = result;
					}
				} else {
					html.addAll(result);
				}
			}
			for (Action a : fieldExpression.actions) {
				ActionExecuter executer = getExecuter(a.name);
				executer.execute(html, a.arguments);
			}
			article.set(fieldExpression.fieldName, html.html());
		}

	}

	public boolean matchConditions(Element e, Vector<Condition> con) { //metodo che verifica se un elemento rispetta determinate condizioni
		for (Condition c : con) {
			if (c.operator.equals("=")) {
				if (e.attr(c.attribute).equals(c.value)) {
					return true;
				} else
					return false;
			} else {
				if (e.attr(c.attribute).equals(c.value)) {
					return false;
				} else
					return true;
			}
		}
		return true;
	}

	public int[] getInterval(String s) {
		int interval[] = new int[2];
		StringTokenizer tok = new StringTokenizer(s, "-");
		interval[0] = Integer.parseInt(tok.nextToken());
		interval[1] = Integer.parseInt(tok.nextToken());
		return interval;
	}

	public abstract class ActionExecuter {  //Classe astratta che definisce un esecuore di azioni
		public AutomatonTable.Actions name;

		public abstract void execute(Elements e, Vector<Argument> a);

		public ActionExecuter(AutomatonTable.Actions name) {
			this.name = name;
		}
	}

	public ActionExecuter getExecuter(AutomatonTable.Actions name) {
		for (ActionExecuter executer : executers) {
			if (executer.name.equals(name)) {
				return executer;
			}
		}
		return null;
	}

	//Azione che elimina una stringa definita dal risultato
	ActionExecuter delete_string = new ActionExecuter(AutomatonTable.Actions.DELETE_STRING) {
		public void execute(Elements e, Vector<Argument> a) {
			for (Argument arg : a) {
				for (Element el : e) {
					if (matchConditions(el, arg.conditions))
						el.html(el.html().replace(arg.value, ""));
				}
			}
		}
	};

	//Azione che elimina un determinato tag dal risultato
	ActionExecuter delete_tag = new ActionExecuter(AutomatonTable.Actions.DELETE_TAGS) {
		public void execute(Elements e, Vector<Argument> a) {
			for (Argument arg : a) {
				for (Element el : e) {
					Elements toRemove = el.getElementsByTag(arg.value);
					removeElements(toRemove, arg);
				}
			}
		}
	};

	
	//Metodo che rimuove gli elementi che non rispettano le condizioni imposte
	public void removeElements(Elements toRemove, Argument a) {
		for (int i = 0; i < toRemove.size(); i++) {
			if (matchConditions(toRemove.get(i), a.conditions)) {
				toRemove.get(i).remove();
			}
		}
	}

	//Azione che restituisce il valore di uno specifico attributo in un tag
	ActionExecuter get_value = new ActionExecuter(AutomatonTable.Actions.GET_VALUE) {
		public void execute(Elements e, Vector<Argument> a) {
			Argument arg = a.firstElement();
			if (matchConditions(e.first(), arg.conditions)) {
				e.html(e.first().attr(arg.value));
			}
		}
	};

	//Azione che salva l'immagine definita dal risultato in un determinato percorso con un determinato nome
	ActionExecuter store_image_in = new ActionExecuter(AutomatonTable.Actions.STORE_IMAGE_IN) {
		public void execute(Elements e, Vector<Argument> a) {
			Element url = e.first();
			Argument arg = a.firstElement();
			String location = arg.value;
			String fileLocation;
			String name = arg.interval;

			if (!location.startsWith("/")) {
				location = "/" + location;
			}
			fileLocation = Attributes.DEFAULT_LOCATION +"/informazioneaconfronto"+ location;
			location = "/informazioneaconfronto"+location;
			File f = new File(fileLocation);
			f.mkdirs();

			String[] names = f.list();
			Vector<String> images = new Vector<String>();
			for (int i = 0; i < names.length; i++) {
				if (names[i].startsWith(name)) {
					images.add(names[i]);
				}
			}
			String finalName;
			if (images.size() == 0) {
				finalName = name + 0;
			} else {
				int max = 0;
				for (int i = 0; i < images.size(); i++) {
					String num = images.get(i).substring(name.length());
					num = num.substring(0, num.indexOf("."));
					int n = Integer.parseInt(num);
					if (n > max) {
						max = n;
					}
				}
				max++;
				finalName = name + max;
			}
			byte[] image = null;
			try {
				image = Jsoup.connect(url.html()).ignoreContentType(true).execute().bodyAsBytes();
			} catch (Exception e1) {
			}
			if (image == null) {
				try {
					String link = url.html();
					if (!link.startsWith("/")) {
						link = "/" + link;
					}
					link = siteUrl + link;
					if (!link.startsWith("http://")) {
						link = "http://" + link;
					}
					image = Jsoup.connect(link).ignoreContentType(true).execute().bodyAsBytes();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			FileOutputStream fos;
			try {
				String extension = getExtension(url.html());
				extension = getRecognizedExtension(extension);
				fos = new FileOutputStream(new File(fileLocation + "/" + finalName + extension));
				fos.write(image);
				fos.flush();
				fos.close();
				e.html(location + "/" + finalName + extension);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	};

	//azione che ritorna il valore degli argomenti di uno script 
	ActionExecuter getScriptvalues = new ActionExecuter(AutomatonTable.Actions.GET_SCRIPT_VALUES) {
		@Override
		public void execute(Elements e, Vector<Argument> a) {
			String temp = "";
			Argument arg = a.firstElement();
			for (Element el : e) {
				Elements script = el.getElementsByTag("script");
				for (Element e1 : script) {
					String inner = e1.html();
					inner = inner.substring(inner.indexOf(arg.value) + arg.value.length());
					inner = inner.substring(inner.indexOf("(") + 1);
					inner = inner.substring(0, inner.indexOf(")"));
					inner = inner.replace("\"", "");
					StringTokenizer tok = new StringTokenizer(inner, arg.interval.replace("'", ""));
					while (tok.hasMoreTokens()) {
						String t = tok.nextToken();
						t = AutomatonTable.removeSpaces(t);
						temp = temp + t + ",";
					}
				}
			}
			for (Condition c : arg.conditions) {
				temp = temp.replace(c.value, "");
			}
			e.html(temp);
		}

	};

	public String getExtension(String s) {
		String st = s;
		if (s.toCharArray()[s.length() - 1] == '/') {
			st = s.substring(0, s.length() - 1);
		}
		char[] ch = st.toCharArray();
		for (int i = ch.length - 1; i > 0; i--) {
			if (ch[i] == '.') {
				return s.substring(i, s.length());
			} else if (ch[i] == '/') {
				return null;
			}
		}
		return null;
	}
	
	public String getRecognizedExtension(String ex){
		for(String s :RECOGNIZED_EXTENSIONS){
			if(ex.startsWith(s)){
				return s.substring(0, s.length());
			}
		}
		return ex;
	}
	
	String[] RECOGNIZED_EXTENSIONS = {".jpg",".png",".bmp",".gif"};

}
