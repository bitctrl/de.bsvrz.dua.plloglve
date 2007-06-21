package de.bsvrz.dua.plloglve.plloglve;

import java.text.SimpleDateFormat;

import de.bsvrz.dua.plloglve.plloglve.ausfall.AusfallDatum;

public class AbstraktDAVZeitEinzelDatum {
	
	/**
	 * 
	 */
	protected static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");  //$NON-NLS-1$
	
	/**
	 * 
	 */
	protected long datenZeit = -1;
		
	/**
	 * die Intervalllänge des Datums
	 */
	protected long intervallLaenge = -1;

	
	/**
	 * 
	 * @return
	 */
	public final long getIntervallLaenge(){
		return this.intervallLaenge;
	}

	public long getDatenZeit(){
		return this.datenZeit;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public int compareTo(AbstraktDAVZeitEinzelDatum that) {
		return new Long(this.getDatenZeit()).compareTo(that.getDatenZeit());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean resultat = false;
		
		if(obj instanceof AusfallDatum){
			AbstraktDAVZeitEinzelDatum that = (AbstraktDAVZeitEinzelDatum)obj;
			resultat = this.getDatenZeit() == that.getDatenZeit();
		}
		
		return resultat;
	}

}
