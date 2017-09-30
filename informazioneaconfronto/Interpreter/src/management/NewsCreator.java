package management;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

import parser.Article;
import parser.Parser;
import rss.reader.FeedMessage;

public class NewsCreator {
	public FeedMessage fm;
	Vector<String> istruzione;
	RSSManagement management;
	String date, category;
	Article a;
	Connection conn;
	Statement stm;
	boolean finished = false;
	
	//Questa classe si occupa di recuperare un articolo e trasformare un codice html in una struttura organizzata definita nella classe Article
	
	public NewsCreator(FeedMessage fm, Vector<String> istruzione, RSSManagement management, String category, Connection conn) {
		this.fm = fm;
		this.istruzione = istruzione;
		this.management = management;
		this.category = category;
		this.conn = conn;
		try {
			stm = conn.createStatement();
			stm.executeQuery("use rassegna_stampa");
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		boolean success = false;
		Parser siteParser = null;
		//Istanzia il parser
		try {
			siteParser = new Parser(fm.getLink(), fm.getSite(), fm.getCategory());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//Prova a parsare l'articolo con le varie istruzioni presenti nel database
		for (int i = 0; i < istruzione.size(); i++) {
			try {
				siteParser.parse(istruzione.get(i));
				success = true;
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//Se un tentativo è andato a buon fine:
		if (success) {
			//Prendo l'articolo creato dal parser
			a = siteParser.article;
			a.feed = fm.feedUrl;
			//Effettuo un controllo sulla categoria
			if (a.category.equals("")) {
				a.set("CATEGORY", category);
			}
			//Setto la data
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
			Calendar c = Calendar.getInstance();
			String date = dateFormat.format(c.getTime());
			date = date.replace("/", "-");
			a.date = date;
			a.site = fm.getSite();
			//controllo che la categoria sia definita nel database
			String q = "select count(*) as 'n' from categoria where nome ='" + a.category + "';";
			int n = 0;
			try {
				ResultSet res  = stm.executeQuery(q);
				res.next();
				n = res.getInt("n");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (n>0) {
				//inserisco nel database
				management.insertIntoDatabase(a);
			}else{
			}
			finished = true;
		} else {
		}

	}

	public int month(String m) {
		switch (m) {
		case "Jan":
			return 1;
		case "Feb":
			return 2;
		case "Mar":
			return 3;
		case "Apr":
			return 4;
		case "May":
			return 5;
		case "Jun":
			return 6;
		case "Jul":
			return 7;
		case "Aug":
			return 8;
		case "Sep":
			return 9;
		case "Oct":
			return 10;
		case "Nov":
			return 11;
		case "Dec":
			return 12;
		default:
			return -1;
		}
	}
}
