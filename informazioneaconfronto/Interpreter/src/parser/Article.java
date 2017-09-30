package parser;

import java.util.StringTokenizer;

public class Article {
	//Classe che definisce la struttura di un articolo
	public String title, summary, body, author, imagePath, category = "", feed, site;
	public String link;
	public String tags = "";
	public String date;
	public int punti = 0;

	public String toString() {
		return "HEADER: " + title + "\nSUMMARY: " + summary + "\nBODY: " + body;
	}

	public void set(String name, String value) { //Metodo unico per settare i valori 
		switch (name) {
		case "BODY":
			body = value;
			break;
		case "TITLE":
			title = value;
			break;
		case "SUMMARY":
			summary = value;
			break;
		case "IMAGE_PATH":
			imagePath = value;
			break;
		case "TAGS":
			tags = value.replace("\n", ",");
			break;
		case "CATEGORY":
			switch (value) {
			case "Politica":
				category = "POLITICA";
				break;
			case "Palazzi & Potere":
				category = "POLITICA";
				break;
			case "Giustizia":
				category = "GIUSTIZIA";
				break;
			case "Media & Regime":
				category = "POLITICA";
				break;
			case "Zonaeuro ":
				category = "ECONOMIA";
				break;
			case "Economia & Lobby":
				category = "ECONOMIA";
				break;
			case "Economia":
				category = "ECONOMIA";
				break;
			case "Lobby":
				category = "ECONOMIA";
				break;
			case "Cronaca":
				category = "CRONACA";
				break;
			case "Ambiente":
				category = "AMBIENTE";
				break;
			case "Sport":
				category = "SPORT";
				break;
			case "Cultura":
				category = "CULTURA";
				break;
			default:
				category = "NON CATEGORIZZATO";
			}
			break;
		}
	}

}
