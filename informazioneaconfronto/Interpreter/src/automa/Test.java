package automa;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;

import language.SintaxErrorException;
import management.RSSManagement;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import parser.Parser;
import updater.PointUpdater;

public class Test {

	public static void main(String[] args) {
		Thread t = new Thread() {
			public void run() {
				while (true) {
					try {
						PointUpdater updater = new PointUpdater();
						System.out.println("ALL UPDATED");
						Thread.sleep(3600000);
					} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException | IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		int cont = 0;
		RSSManagement management;
		boolean a = true;
		while (a) {
			try {
				management = new RSSManagement();
				System.out.println("ALL SAVED");
				if (cont == 0) {
					t.start();
					cont++;
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(7200000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (!a) {
			String site = "http://www.repubblica.it/ambiente/2015/05/24/news/bambu_-115147736/?rss";
			String s = "http://www.repubblica.it";

			try {
				Parser p = new Parser(site, s, "");
				String i = "BODY=[(TAKE TAG 'ANY' ALL WHERE ITEMPROP = 'articleBody')] TITLE=[(TAKE TAG 'ANY' 0 WHERE ITEMPROP = 'headline name')] SUMMARY=[(TAKE TAG 'ANY' 0 WHERE ITEMPROP ='description')] IMAGE_PATH=[(TAKE TAG 'ANY' 0 WHERE ITEMPROP ='image')] TAGS=[(TAKE TAG 'a' ALL IN TAG 'dd' ALL IN TAG 'ANY' ALL WHERE CLASS='tags')] ACTION DELETE_STRING '&nbsp' ALL ON BODY,SUMMARY,TITLE; ACTION GET_VALUE 'src' ALL ON IMAGE_PATH; ACTION STORE_IMAGE_IN 'media' REPUBBLICA ON IMAGE_PATH; ACTION DELETE_TAGS 'script' ALL ON BODY;";
				p.parse(i);
				System.out.println(p.article.toString());
				System.out.println(p.article.tags);
				System.out.println(p.article.imagePath);
			} catch (IOException | SintaxErrorException e) {
				e.printStackTrace();
			}
		}

	}

}
