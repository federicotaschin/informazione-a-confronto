package updater;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
//Questa classe si occupa di eseguire l'update dei punteggi delle varie notizie appoggiandosi ai servizi di linkedin facebook e twitter
public class PointUpdater { 
	Connection conn;
	Statement stm;
	Statement update;

	public PointUpdater() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, MalformedURLException, IOException {
		String driver = "com.mysql.jdbc.Driver";
		Class.forName(driver).newInstance();
		conn = DriverManager.getConnection("jdbc:mysql://localhost/", "taschin.federico", "Federico1996");
		stm = conn.createStatement();
		stm.execute("use rassegna_stampa");
		ResultSet res = stm.executeQuery("select link from articolo where curdate() - pubdate <= 2 AND (last_update is null or last_update != curdate());");
		while (res.next()) {
			int points = 0;
			String article = res.getString(1);
			// FACEBOOK eseguo richiesta get
			String link = "http://graph.facebook.com/?id=" + article;
			byte[] bytes = Jsoup.connect(link).ignoreContentType(true).execute().bodyAsBytes();
			String html = new String(bytes);
			//parso la stringa per selezionare il valore dei like+condivisioni
			html = html.substring(html.indexOf("\"shares\":") + "\"shares\":".length());
			if (html.contains(",")) {
				html = html.substring(0, html.indexOf(","));
			}
			html = html.replace("}", "");
			html = html.replace(" ", "");
			try {
				points += Integer.parseInt(html);
			} catch (Exception e) {
			}
			// TWITTER eseguo richiesta get
			link = "https://cdn.api.twitter.com/1/urls/count.json?url=" + article;
			bytes = Jsoup.connect(link).ignoreContentType(true).execute().bodyAsBytes();
			html = new String(bytes);
			//parso la stringa per selezionare il valore dei like+condivisioni
			html = html.substring(html.indexOf("\"count\":") + "\"count\":".length());
			html = html.substring(0, html.indexOf(","));
			html.replace(" ", "");
			try {
				points += Integer.parseInt(html);
			} catch (Exception e) {
			}

			// LINKEDIN eseguo richiesta get
			link = "http://www.linkedin.com/countserv/count/share?url=http://stylehatch.co&format=json" + article;
			bytes = Jsoup.connect(link).ignoreContentType(true).execute().bodyAsBytes();
			html = new String(bytes);
			//parso la stringa per selezionare il valore dei like+condivisioni
			html = html.substring(html.indexOf("\"count\":") + "\"count\":".length());
			html = html.substring(0, html.indexOf(","));
			html.replace("\n", "");
			html.replace(" ", "");
			try {
				points += Integer.parseInt(html);
			} catch (Exception e) {
			}
			update = conn.createStatement();
			update.execute("update articolo set Punti=" + points + " where link='" + article + "';");

			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
			Calendar c = Calendar.getInstance();
			String date = dateFormat.format(c.getTime());
			date = date.replace("/", "-");

			update.execute("update articolo set last_update='" + date + "' where link = '" + article + "';");
			System.out.println("UPDATING "+article);
				
			
		}
	}
}
