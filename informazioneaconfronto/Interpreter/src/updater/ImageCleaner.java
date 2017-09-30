package updater;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class ImageCleaner {

	public  ImageCleaner() {
		Connection conn = null;
		Statement stm = null;
		String driver = "com.mysql.jdbc.Driver";
		try {
			Vector<String> img = new Vector<String>();
			String loc = "/opt/lampp/htdocs";
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection("jdbc:mysql://localhost/", "taschin.federico", "Federico1996");
			stm = conn.createStatement();
			stm.executeQuery("use rassegna_stampa");
			ResultSet images = stm.executeQuery("select image_path from articolo;");
			while (images.next()) {
				img.add(images.getString("image_path").replace("/informazioneaconfronto/media/", ""));
			}

			File f = new File(loc + "/informazioneaconfronto/media/");
			Vector<String> files = new Vector<String>();
			for (int i = 0; i < f.list().length; i++) {
				files.add(f.list()[i]);
			}
			
			for(String s : files){
				if(!img.contains(s)){
				File fl = new File("/opt/lampp/htdocs/informazioneaconfronto/media/"+s);
				fl.delete();
				System.out.println("Deleting: "+s);
				}
			}

		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

	}

}
