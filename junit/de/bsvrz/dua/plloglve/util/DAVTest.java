package de.bsvrz.dua.plloglve.util;

import java.util.Random;

import stauma.dav.clientside.ClientDavInterface;
import sys.funclib.ArgumentList;
import sys.funclib.application.StandardApplication;
import sys.funclib.application.StandardApplicationRunner;

/**
 * Stellt eine Datenverteiler-Verbindung
 * zur Verfügung.
 * 
 * @author Thierfelder
 * 
 */
public class DAVTest {

	/**
	 * Verbindungsdaten
	 */
	private static final String[] CON_DATA = new String[] {
			"-datenverteiler=localhost:8083", //$NON-NLS-1$ 
			"-benutzer=Tester", //$NON-NLS-1$
			"-authentifizierung=c:\\passwd" }; //$NON-NLS-1$

	/**
	 * Verbindung zum Datenverteiler
	 */
	protected static ClientDavInterface VERBINDUNG = null;

	/**
	 * Randomizer
	 */
	private static Random R = new Random(System.currentTimeMillis());

	
	/**
	 * Erfragt bzw. initialisiert eine
	 * Datenverteiler-Verbindung
	 * 
	 * @return die Datenverteiler-Verbindung
	 * @throws Exception falls die Verbindung nicht
	 * hergestellt werden konnte
	 */
	public static final ClientDavInterface getDav()
	throws Exception {
		
		if(VERBINDUNG == null) {
			StandardApplicationRunner.run(new StandardApplication() {
	
				public void initialize(ClientDavInterface connection)
						throws Exception {
					DAVTest.VERBINDUNG = connection;
				}
	
				public void parseArguments(ArgumentList argumentList)
						throws Exception {
					//
				}
	
			}, CON_DATA);
		}
		
		return VERBINDUNG;
	}


	/**
	 * Erfragt einen Array mit zufälligen Zahlen von
	 * 0 bis <code>anzahl</code>. Jede Zahl darf nur 
	 * einmal im Array vorkommen.
	 * 
	 * @param anzahl die Obergrenze
	 * @return Array mit zufälligen Zahlen von
	 * 0 bis <code>anzahl</code>
	 */
	public static final int[] getZufaelligeZahlen(int anzahl){
		int belegt = 0;
		int[] zahlen = new int[anzahl];
		for(int i = 0; i<anzahl; i++)zahlen[i] = -1;
		
		while(belegt < anzahl){
			int index = R.nextInt(anzahl);
			if(zahlen[index] == -1)zahlen[index] = belegt++;
		}
		
		return zahlen;
	}

}
