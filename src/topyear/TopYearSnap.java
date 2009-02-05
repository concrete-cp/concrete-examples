package topyear;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TopYearSnap {

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
			IOException, ParserConfigurationException, SAXException,
			XPathExpressionException, XPathFactoryConfigurationException {

		final SortedMap<Integer, Integer> stats = new TreeMap<Integer, Integer>();

		final Document doc = constructeur.parse(new URL(
				"file:///tmp/bgg-snapshot-20080617.xml").openStream());

		XPath path = XPathFactory.newInstance(
				XPathFactory.DEFAULT_OBJECT_MODEL_URI).newXPath();

		final NodeList nl = (NodeList) path
				.compile(
						"/games/game[descendant::ratings/bayesaverage>=7]/yearpublished")
				.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nl.getLength(); i++) {
			int year;
			try {
				year = Integer.valueOf(nl.item(i).getTextContent());
				if (year < 1980) {
					year = 1979;
				}
			} catch (NumberFormatException e) {
				year = 1978;
			}

			final Integer count = stats.get(year);

			if (count == null) {
				stats.put(year, 1);
			} else {
				stats.put(year, count + 1);
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

}
