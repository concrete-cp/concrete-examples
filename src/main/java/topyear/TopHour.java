package topyear;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TopHour {

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

		final SortedSet<Game> stats = new TreeSet<Game>();

		final Document doc = constructeur.parse(new URL(
				"http://boardgamegeek.com/xmlapi/collection/scand1sk")
				.openStream());

		final NodeList nl = doc.getElementsByTagName("item");
		for (int i = 0; i < nl.getLength(); i++) {
			Game game = new Game();

			final NodeList infos = nl.item(i).getChildNodes();
			for (int j = 0; j < infos.getLength(); j++) {
				if ("stats".equals(infos.item(j).getNodeName())) {
					game.length = Integer.valueOf(infos.item(j).getAttributes()
							.getNamedItem("playingtime").getNodeValue());
				} else if ("name".equals(infos.item(j).getNodeName())) {
					game.name = infos.item(j).getTextContent();
				} else if ("numplays".equals(infos.item(j).getNodeName())) {
					game.numplays = Integer.valueOf(infos.item(j)
							.getTextContent());
				}
			}
			stats.add(game);

		}

		int i = 1;
		for (Game g : stats) {
			System.out.println(i++ + ". " + g);
		}
	}

	static class Game implements Comparable<Game> {
		String name;
		int length;
		int numplays;

		@Override
		public int compareTo(Game arg0) {
			if (totalLength() == arg0.totalLength()) {
				return name.compareTo(arg0.name);
			}
			return totalLength() - arg0.totalLength();
		}

		private int totalLength() {
			return length * numplays;
		}

		@Override
		public String toString() {
			return totalLength() / 60F + " : " + name;
		}
	}

}
