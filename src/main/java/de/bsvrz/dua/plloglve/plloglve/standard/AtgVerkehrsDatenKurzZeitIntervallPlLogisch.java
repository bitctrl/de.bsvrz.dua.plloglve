/* 
 * Segment Datenübernahme und Aufbereitung (DUA), SWE Pl-Prüfung logisch LVE
 * Copyright (C) 2007 BitCtrl Systems GmbH 
 * Copyright 2016 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dua.plloglve.
 * 
 * de.bsvrz.dua.plloglve is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dua.plloglve is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.dua.plloglve.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dua.plloglve.plloglve.standard;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;
import de.bsvrz.sys.funclib.bitctrl.dua.AllgemeinerDatenContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * Klasse die alle Parameter halten kann, die innerhalb der
 * Standardplausibilisierung LVE für KZD benötigt werden.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 * @version $Id$
 */
public class AtgVerkehrsDatenKurzZeitIntervallPlLogisch extends AllgemeinerDatenContainer {

	/**
	 * Attribute, für die Maximum-Werte parametriert werden können
	 */
	private static final String[] ATTR = {
			"qKfz",
			"qLkw",
			"vPkw",
			"vLkw",
			"vKfz",
			"vgKfz",
			"b"
	};
	/**
	 * Map mit Attribut zu Maximum-Werten
	 */
	private final Map<String, Long> _maxMap = new HashMap<String, Long>();

	/**
	 * vKfzGrenz-Parameterwert
	 */
	private final long _VKfzGrenz;
	/**
	 * bGrenz-Parameterwert
	 */
	private final long _BGrenz;
	/**
	 * Verhalten Grenzwertprüfung Wert
	 */
	private final OptionenPlausibilitaetsPruefungLogischVerkehr _verhalten;

	/**
	 * Ob Betriebsmeldungen im Modul TLS verschickt werden sollen
	 */
	private final boolean _messageTls;
	
	/**
	 * Ob Betriebsmeldungen im Modul Verkehr verschickt werden sollen
	 */
	private final boolean _messageVerkehr;

	/**
	 * Parameter Max Ausfallzeit Pro Tag in Millisekunden
	 */
	private final long _maxAusfallZeitProTag;

	/**
	 * Parameter Prüfintervall Ausfallzeit in Millisekunden
	 */
	private final long _pruefIntervallAusfall;

	/**
	 * Parameter Bezugszeitraum für die Prüfung Vertrauensbereich 
	 */
	private final long _vertrauensbereichBezugsZeitraum;

	/**
	 * Prüfintervall Vertrauensbereich in Millisekunden
	 */
	private final long _pruefIntervallVertrauensbereich;

	/**
	 * Parameter für die Ausfallzeit zum Einschalten der Implausibilisierung bei der Prüfung Vertrauensbereich
	 */
	private final long _maxAusfallProBezugszeitraumEin;

	/**
	 * Parameter für die Ausfallzeit zum Ausschalten der Implausibilisierung bei der Prüfung Vertrauensbereich
	 */
	private final long _maxAusfallProBezugszeitraumAus;

	public AtgVerkehrsDatenKurzZeitIntervallPlLogisch(final Data data) {
		for(String attr : ATTR) {
			_maxMap.put(attr, data.getUnscaledValue(attr + "Max").longValue());
		}

		_verhalten = OptionenPlausibilitaetsPruefungLogischVerkehr.getZustand(data.getUnscaledValue("VerhaltenGrenzwertPrüfung").intValue());
		_VKfzGrenz = data.getUnscaledValue("vKfzGrenz").longValue();
		_BGrenz = data.getUnscaledValue("bGrenz").longValue();
		
		_messageTls = data.getUnscaledValue("ErzeugeBetriebsmeldungPrüfungTLS").intValue() != 0;
		_messageVerkehr = data.getUnscaledValue("ErzeugeBetriebsmeldungPrüfungVerkehr").intValue() != 0;
		
		_maxAusfallZeitProTag = data.getTimeValue("MaxAusfallZeitProTag").getMillis();
		_pruefIntervallAusfall = data.getTimeValue("PrüfintervallAusfallHäufigkeit").getMillis();

		_vertrauensbereichBezugsZeitraum = data.getTimeValue("BezugszeitraumVertrauensbereich").getMillis();
		_pruefIntervallVertrauensbereich = data.getTimeValue("PrüfintervallVertrauensbereich").getMillis();
		_maxAusfallProBezugszeitraumEin = data.getTimeValue("MaxAusfallzeitVertrauensbereichEin").getMillis();
		_maxAusfallProBezugszeitraumAus = data.getTimeValue("MaxAusfallzeitVertrauensbereichAus").getMillis();
	}

	/**
	 * Erfragt eine Schnittstelle zu den Parametern der logischen
	 * Plausibilisierung.
	 * 
	 * @param resultat ein Parameter-Resultat
	 * @return eine Schnittstelle zu den Parametern der logischen
	 *         Plausibilisierung oder <code>null</code>, wenn diese nicht
	 *         ausgelesen werden konnten
	 */
	public static AtgVerkehrsDatenKurzZeitIntervallPlLogisch getInstance(
			final ResultData resultat) {
		Data data = resultat.getData();
		if(data == null) return null;
		return new AtgVerkehrsDatenKurzZeitIntervallPlLogisch(data);
	}

	/** 
	 * Gibt den Max-Grenzwert zurück
	 * @param attribut einer aus {@link #ATTR}
	 * @return den Max-Grenzwert
	 */
	public final Long getMax(String attribut){
		return _maxMap.get(attribut);
	}

	/** 
	 * Gibt die Pid der Attributgruppe zurück
	 * @return die Pid der Attributgruppe
	 */
	public static String getPid() {
		return "atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch2";
	}

	/** 
	 * Gibt das Verhalten Grenzwertprüfung zurück
	 * @return das Verhalten Grenzwertprüfung
	 */
	public OptionenPlausibilitaetsPruefungLogischVerkehr getVerhalten() {
		return _verhalten;
	}

	/** 
	 * Gibt vKfzGrenz zurück
	 * @return vKfzGrenz
	 */
	public long getVKfzGrenz() {
		return _VKfzGrenz;
	}

	/** 
	 * Gibt bGrenz zurück
	 * @return bGrenz
	 */
	public long getBGrenz() {
		return _BGrenz;
	}

	/** 
	 * Gibt <tt>true</tt> zurück, wenn Betriebsmeldungen TLS erzeugt werden sollen
	 * @return <tt>true</tt>, wenn Betriebsmeldungen TLS erzeugt werden sollen, sonst <tt>false</tt>
	 */
	public boolean isMessageTls() {
		return _messageTls;
	}

	/** 
	 * Gibt <tt>true</tt> zurück, wenn Betriebsmeldungen Verkehr erzeugt werden sollen
	 * @return <tt>true</tt>, wenn Betriebsmeldungen Verkehr erzeugt werden sollen, sonst <tt>false</tt>
	 */
	public boolean isMessageVerkehr() {
		return _messageVerkehr;
	}

	/**
	 * Maximale Ausfallzeit pro Tag in Millisekunden
	 * @return
	 */
	public long getMaxAusfallZeitProTag() {
		return _maxAusfallZeitProTag;
	}
	
	/**
	 * Prüfintervall in Millisekunden
	 * @return
	 */
	public long getPruefIntervallAusfall() {
		return _pruefIntervallAusfall;
	}
	
	/**
	 * Bezugszeitraum VB in Millisekunden
	 * @return
	 */
	public long getVertrauensbereichBezugsZeitraum() {
		return _vertrauensbereichBezugsZeitraum;
	}
	
	/**
	 * Prüfintervall in Millisekunden
	 * @return
	 */
	public long getPruefIntervallVertrauensbereich() {
		return _pruefIntervallVertrauensbereich;
	}

	/**
	 * Einschaltschwelle VB
	 * @return
	 */
	public long getMaxAusfallProBezugszeitraumEin() {
		return _maxAusfallProBezugszeitraumEin;
	}

	/**
	 * Ausschaltschwelle VB
	 * @return
	 */
	public long getMaxAusfallProBezugszeitraumAus() {
		return _maxAusfallProBezugszeitraumAus;
	}
}
