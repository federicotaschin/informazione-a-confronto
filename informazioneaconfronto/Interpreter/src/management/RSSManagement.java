package management;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import parser.Article;
import rss.reader.Feed;
import rss.reader.RSSFeedParser;
import updater.ImageCleaner;
import updater.PointUpdater;
import variables.Attributes;

public class RSSManagement {
	//Classe principale: si occupa di recuperare le newsfeed relative agli articoli, istanziare gli oggetti adibiti alla 
	//creazione degli articoli e gestire l'inserimento nel database.
	
	//inizialmente il lavoro di creazione degli articoli (download e parsing pagine html) veniva effettuato in multi-threading 
	//successivamente ho modificato il progetto che adesso funziona a thread singolo a causa di limitazioni hardware del server
	//in cui era hostato.
	Connection conn;
	Vector<String[]> feeds = new Vector<String[]>(); //Feeds da cui raccogliere gli articoli
	Statement stm;
	Vector<String> articoliPresenti; //Articoli già presenti nel database
	Vector<String> tags; //tags già presenti nel database
	LinkedBlockingQueue<NewsCreator> queue = new LinkedBlockingQueue<NewsCreator>();

	public RSSManagement() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
	    //Connessione al database
		String driver = "com.mysql.jdbc.Driver";
		Class.forName(driver).newInstance();
		conn = DriverManager.getConnection("jdbc:mysql://"+Attributes.DATABASE_IP+"/", Attributes.DATABASE_ID, Attributes.DATABASE_PASSWORD);
		stm = conn.createStatement();
		stm.executeQuery("use rassegna_stampa");
		
		//seleziono i feed da cui recuperare gli articoli
		String feedsQuery = "select *  from feed";
		ResultSet feeds = stm.executeQuery(feedsQuery);

		while (feeds.next()) {
			String s[] = { feeds.getString("link"), feeds.getString("sito"), feeds.getString("categoria") };
			this.feeds.add(s);
		}
		
		//Seleziono gli articoli già presenti
		articoliPresenti = new Vector<String>();
		ResultSet artc = stm.executeQuery("select link from articolo");
		while (artc.next()) {
			articoliPresenti.add(artc.getString("link"));
		}

		//Seleziono i tag già presenti
		tags = new Vector<String>();
		String query = "select * from tag";
		ResultSet tag = stm.executeQuery(query);
		while (tag.next()) {
			tags.add(tag.getString("tag"));
		}

		System.out.println("Starting ");
		//Per ogni feed
		for (int i = 0; i < this.feeds.size(); i++) {
			//Recupero le istruzioni relative al feed
			ResultSet instruction = stm.executeQuery("select istruzione from istruzione where sito =" + "'" + this.feeds.get(i)[1] + "';");
			try{
			RSSFeedParser parser = new RSSFeedParser(this.feeds.get(i)[0], this.feeds.get(i)[1], this.feeds.get(i)[2]);
			Feed f = parser.readFeed();
			Vector<String> istruzioni = new Vector<String>();
			while (instruction.next()) {
				istruzioni.add(instruction.getString("istruzione"));
			}
			//Per ogni articolo contenuto nel feed
			for (int k = 0; k < f.getMessages().size(); k++) {
				//Se non è già presente nel database 
				if (!articoliPresenti.contains(f.getMessages().get(k).getLink().replace("/?rss", ""))) {
					try {
						//aggiungo alla coda l'article retriever
						queue.put(new NewsCreator(f.getMessages().get(k), istruzioni, this, this.feeds.get(i)[2], conn));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		while (!queue.isEmpty()) {//Estraggo un article retriever alla volta e lo avvio
			NewsCreator ar = queue.poll();
			ar.run();
		}
		
		//funzioni di controllo nei casi in cui vi siano errori negli articoli ottenuti
		stm.execute("delete from articolo where title='' or summary='' or body = '';");
		
		Vector<String> links = new Vector<String>();
		String q = "select articolo from contiene;";
		ResultSet rs = stm.executeQuery(q);
		while(rs.next()){
			links.add(rs.getString("articolo"));
		}
		
		for(String l : links){
			ResultSet num = stm.executeQuery("select count(*) as 'n' from articolo where link = '"+l+"';");
			num.next();
			int n = num.getInt("n");
			if(n==0){
				stm.execute("delete from contiene where articolo = '"+l+"';");
			}
		}
		
		ImageCleaner cleaner = new ImageCleaner();	
	}

	public void wait(NewsCreator ar, long timeout) {
		long time = System.currentTimeMillis();
		while (!ar.finished) {
			if (System.currentTimeMillis() >= time + timeout) {
				break;
			}
		}
	}
public int added = 0;
	public synchronized void insertIntoDatabase(Article article) {
		//Questo metodo inserisce l'articolo nel database
		try {
			PreparedStatement stmp = conn.prepareStatement("insert into articolo values(?,?,?,?,?,?,?,?,?,?)");
			stmp.setString(1, article.link);
			stmp.setString(2, article.body);
			stmp.setString(3, article.title);
			stmp.setString(4, article.summary);
			stmp.setDouble(5, article.punti);
			stmp.setString(6, article.imagePath);
			stmp.setString(7, article.category);
			stmp.setString(8, article.date);
			stmp.setString(9, article.site);
			stmp.setDate(10, null);
			if (stmp.execute()) {
				articoliPresenti.add(article.link);
			}
			if (article.tags.length() == 0) {
				return;
			}
			//Separo i tag dell'articolo
			StringTokenizer tok = new StringTokenizer(article.tags, ",");

			while (tok.hasMoreTokens()) {//Per ogni tag
				String tag = tok.nextToken();
				if (!tags.contains(tag)) {//Se non esiste nel database lo inserisco
					PreparedStatement insert = conn.prepareStatement("insert into tag values(?)");
					insert.setString(1, tag);
					tags.addElement(tag);
					try {
						insert.executeUpdate();
					} catch (Exception e) {

					}
				}
				
				//Aggiungo il record che collega l'articolo ai suoi tags
				PreparedStatement tagstatement = conn.prepareStatement("insert into contiene values(?,?)");
				tagstatement.setString(1, article.link);
				tagstatement.setString(2, tag);
				try {
					tagstatement.execute();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			added++;
			System.out.println("Insert: "+article.link);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	int finished = 0;
	public boolean allFinished = false;


}
