package topyear;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class TopYear {

	static DocumentBuilder constructeur;

	static {

		// création d'une fabrique de documents
		final DocumentBuilderFactory fabrique = DocumentBuilderFactory
				.newInstance();
		// création d'un constructeur de documents
		try {
			constructeur = fabrique.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String args[]) throws MalformedURLException,
			IOException, ParserConfigurationException, SAXException {

		final SortedMap<Integer, Integer> stats = new TreeMap<Integer, Integer>();

		final Pattern p = Pattern
				.compile("<A href=\"/game/(\\d*)\" style='font-size:125%;'>([^<]*)</A>\\s*</td>\\s*<td align='right'>([.\\d]*)</td><td align='right'>");
		boolean end = false;
		for (int page = 1; !end; page++) {
			final StringBuilder stb = new StringBuilder();

			final BufferedReader r = new BufferedReader(new InputStreamReader(
					new URL(
							"http://www.boardgamegeek.com/browser.php?itemtype=game&sortby=rank&pageID="
									+ page).openStream()));

			String s;
			while ((s = r.readLine()) != null) {
				stb.append(s);
			}

			final Matcher m = p.matcher(stb);
			int count = 0;
			while (m.find()) {
				if (Float.valueOf(m.group(3)) < 7) {
					end = true;
					break;
				}
				System.out.print(m.group(2) + ": "
						+ Integer.valueOf(m.group(1)) + " "
						+ Float.valueOf(m.group(3)) + " ");
				updateStats(Integer.valueOf(m.group(1)), stats);

				count++;
			}
			if (!end && count != 50) {
				throw new InvalidParameterException(Integer.toString(count));
			}
		}

		 for (int i = 1979; i <= stats.lastKey(); i++) {
			if (stats.containsKey(i)) {
				System.out.println(stats.get(i));
			} else {
				System.out.println(0);
			}
		}

	}

	private static void updateStats(int id, SortedMap<Integer, Integer> stats)
			throws MalformedURLException, SAXException, IOException {
		// lecture du contenu d'un fichier XML avec DOM

		final Document document = constructeur.parse(new URL(
				"http://www.boardgamegeek.com/xmlapi/game/" + id).openStream());

		int year;
		try {
			year = Integer.valueOf(document.getElementsByTagName(
					"yearpublished").item(0).getTextContent());
			if (year < 1980) {
				year = 1979;
			}
		} catch (NumberFormatException e) {
			year = 1978;
		}

		System.out.println(year);

		final Integer count = stats.get(year);

		if (count == null) {
			stats.put(year, 1);
		} else {
			stats.put(year, count + 1);
		}
	}
}
