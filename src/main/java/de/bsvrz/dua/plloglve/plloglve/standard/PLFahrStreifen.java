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

import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.guete.GWert;
import de.bsvrz.dua.guete.GueteException;
import de.bsvrz.dua.guete.GueteVerfahren;
import de.bsvrz.dua.guete.vorschriften.IGuete;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;
import de.bsvrz.sys.funclib.bitctrl.daf.DaVKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.GanzZahl;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.operatingMessage.MessageGrade;
import de.bsvrz.sys.funclib.operatingMessage.MessageTemplate;
import de.bsvrz.sys.funclib.operatingMessage.MessageType;
import de.bsvrz.sys.funclib.operatingMessage.OperatingMessage;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * Klasse zum Durchführen der Standardplausibilisierung LVE für Kurzzeitddaten.
 * Diese Klasse macht nichts weiter, als sich auf die Grenzwertparameter
 * anzumelden und einige Funktionen zur Plausibilisierung von Kurzzeitddaten zur Verfügung
 * zu stellen.
 * 
 * Folgende Prüfungen werden von dieser Klasse durchgeführt:
 * 
 * -    TLS
 * -    Verkehr
 * -    Grenzwerte
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 */
public final class PLFahrStreifen implements ClientReceiverInterface {

	public static final GWert MAX_GUETE_WERT = GWert.getMaxGueteWert(GueteVerfahren.STANDARD);
	
	/**
	 * Standard-Verfahren der Gueteberechnung.
	 */
	protected static final IGuete G = GueteVerfahren.STANDARD
			.getBerechnungsVorschrift();
	
	/**
	 * Alle Attribute, die innerhalb der PL-Prüfung logisch bzgl. eines KZD
	 * veraendert werden koennen.
	 */
	public static final String[] ATTRIBUT_NAMEN = { "qKfz", 
			"qLkw", 
			"qPkw", 
			"vPkw", 
			"vLkw", 
			"vKfz", 
			"vgKfz", 
			"tNetto", 
			"b" };

	/**
	 * Millisekunden in einer Stunde
	 */
	public static final long MILLIS_PER_HOUR = (long) (60 * 60 * 1000);
	
	/**
	 * Verbindung zum Verwaltungsmodul
	 */
	private final IVerwaltung dieVerwaltung;

	/**
	 * Objekt des Fahrstreifens
	 */
	private final SystemObject _obj;

	/**
	 * Objekt des übergeordneten MQs
	 */
	private SystemObject _mq;
	
	/**
	 * letztes zur Plausibilisierung übergebenes Datum.
	 */
	private ResultData letztesKZDatum = null;
	
	/**
	 * Schnittstelle zu den Parametern der Grenzwertprüfung.
	 */
	private AtgVerkehrsDatenKurzZeitIntervallPlLogisch parameterAtgGrenz = null;

	/**
	 * Debug-Logger
	 */
	private static final Debug _debug = Debug.getLogger();

	/**
	 * Vorlage für Betriebsmeldung bei Prüfung TLS 
	 */
	public static final MessageTemplate TEMPLATE_TLS = new MessageTemplate(
			MessageGrade.INFORMATION, 
			MessageType.APPLICATION_DOMAIN,
			MessageTemplate.fixed("Attribute "),
			MessageTemplate.set("attr", " und "),
			MessageTemplate.fixed(" durch Pl-Prüfung TLS auf fehlerhaft gesetzt am Fahrstreifen "),
			MessageTemplate.object(),
			MessageTemplate.fixed("("),
			MessageTemplate.variable("mq"),
			MessageTemplate.fixed("), da "),
			MessageTemplate.set("values", ", "),
			MessageTemplate.fixed(". "),
			MessageTemplate.ids()
	).withIdFactory(message -> message.getObject().getPidOrId() + " [DUA-PP-VT]");

	/**
	 * Vorlage für Betriebsmeldung bei Prüfung Verkehr
	 */
	public static final MessageTemplate TEMPLATE_VERKEHR = new MessageTemplate(
			MessageGrade.INFORMATION, 
			MessageType.APPLICATION_DOMAIN,
			MessageTemplate.set("attr", " und ", "Attribut ", "Attribute "),
			MessageTemplate.fixed(" durch Pl-Prüfung Verkehr auf fehlerhaft gesetzt am Fahrstreifen "),
			MessageTemplate.object(),
			MessageTemplate.fixed("("),
			MessageTemplate.variable("mq"),
			MessageTemplate.fixed("), da "),
			MessageTemplate.set("values", ", "),
			MessageTemplate.fixed(". "),
			MessageTemplate.ids()
	).withIdFactory(message -> message.getObject().getPidOrId() + " [DUA-PP-VV]");

	/**
	 * Vorlage für Betriebsmeldung bei Grenzwertprüfung
	 */
	public static final MessageTemplate TEMPLATE_GRENZWERT = new MessageTemplate(
			MessageGrade.INFORMATION,
			MessageType.APPLICATION_DOMAIN,
			MessageTemplate.set("attr", " und ", "Attribut ", "Attribute "),
			MessageTemplate.fixed(" durch Grenzwertprüfung auf fehlerhaft gesetzt am Fahrstreifen "),
			MessageTemplate.fixed("("),
			MessageTemplate.variable("mq"),
			MessageTemplate.fixed("), da "),
			MessageTemplate.set("values", ", "),
			MessageTemplate.fixed(". "),
			MessageTemplate.ids()
	).withIdFactory(message -> message.getObject().getPidOrId() + " [DUA-PP-VGW]");

	@Override
	public String toString() {
		return _obj.toString();
	}

	/**
	 * Standardkonstruktor.
	 * 
	 * @param verwaltung
	 *            Verbindung zum Verwaltungsmodul
	 * @param obj
	 *            das mit dem Fahrstreifen assoziierte Systemobjekt
	 * @param mq
	 */
	public PLFahrStreifen(final IVerwaltung verwaltung,
			final SystemObject obj, final SystemObject mq) {

		_mq = mq;

		dieVerwaltung = verwaltung;
		_obj = obj;

		ClientDavInterface connection = dieVerwaltung.getVerbindung();
		AttributeGroup parameterAtg = getParameterAtg(connection);
		if(parameterAtg != null) {
			connection.subscribeReceiver(
					this,
					obj,
					new DataDescription(parameterAtg, connection.getDataModel()
							.getAspect(DaVKonstanten.ASP_PARAMETER_SOLL)), ReceiveOptions.normal(),
					ReceiverRole.receiver()
			);
		}
		else {
			_debug.warning("Attributgruppe " + AtgVerkehrsDatenKurzZeitIntervallPlLogisch.getPid() + " ist nicht im Datenmodell vorhanden, die Grenzwerte werden nicht geprüft");
		}
	}


	public static MessageAttributeSet formatAttributes(final Data data, final String... attributes) {
		LinkedHashSet<String> param = Arrays.asList(attributes)
				.stream()
				.map(s -> formatAttribute(data, s))
				.collect(Collectors.toCollection(LinkedHashSet::new));
		
		return new MessageAttributeSet(param);
	}

	public static String formatAttribute(final Data data, final String s) {
		return formatAttributeValue(s, data.getItem(s).getItem("Wert"));
	}

	public static String formatAttributeValue(final String name, final Object value) {
		if(value instanceof Data) {
			Data data = (Data) value;
			return name + "=" + data.asTextValue().getText();
		}
		return name + "=" + value;
	}

	private void ueberpruefe(final Data data, final ResultData resultat) {

		Data origDatum = data.createUnmodifiableCopy();

		long vgKfzLetztesIntervall = -4;
		Object vgKfzLetztesIntervallDisplay = "Unbekannt";
		if (letztesKZDatum != null) {
			if (letztesKZDatum.getData() != null) {
				if (data.getTimeValue("T").getMillis() == resultat.getDataTime() - letztesKZDatum.getDataTime()) {
					vgKfzLetztesIntervall = getValue(letztesKZDatum.getData(), "vgKfz");
					vgKfzLetztesIntervallDisplay = letztesKZDatum.getData().getItem("vgKfz").getItem("Wert");
				}
			}
		}

		OperatingMessage messageTls = TEMPLATE_TLS.newMessage(_obj);
		OperatingMessage messageVerkehr = TEMPLATE_VERKEHR.newMessage(_obj);
		OperatingMessage messageGrenzwert = TEMPLATE_GRENZWERT.newMessage(_obj);
		messageTls.put("mq", _mq == null ? "Unbekannter MQ" : _mq);
		messageVerkehr.put("mq", _mq == null ? "Unbekannter MQ" : _mq);
		messageGrenzwert.put("mq", _mq == null ? "Unbekannter MQ" : _mq);

		// DUA-13
		if (data.getUnscaledValue("ArtMittelwertbildung").longValue() == DUAKonstanten.MWB_ARITHMETISCH) { 

			/**
			 * Regel B7
			 */
			if (getValue(origDatum, "qKfz") == 0) {
				if (getValue(origDatum, "vKfz") != DUAKonstanten.NICHT_ERMITTELBAR && getValue(origDatum, "vKfz") != DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT) {

					messageTls.add("attr", "v");
					messageTls.add("values", formatAttributes(origDatum, "qKfz", "vKfz"));
					messageTls.addId("[DUA-PP-VT01]");

					setzeFehlerhaft(data, "vKfz", MAX_GUETE_WERT);
					setzeFehlerhaft(data, "vPkw", MAX_GUETE_WERT);
					setzeFehlerhaft(data, "vLkw", MAX_GUETE_WERT);
				}
			}

			/**
			 * Regel B8
			 */
			if (getValue(origDatum, "qPkw") == 0) {
				if (getValue(origDatum, "vPkw") != DUAKonstanten.NICHT_ERMITTELBAR && getValue(origDatum, "vPkw") != DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT) {

					messageTls.add("attr", "v");
					messageTls.add("values", formatAttributes(origDatum, "qPkw", "vPkw"));
					messageTls.addId("[DUA-PP-VT02]");

					setzeFehlerhaft(data, "vKfz", MAX_GUETE_WERT);
					setzeFehlerhaft(data, "vPkw", MAX_GUETE_WERT);
					setzeFehlerhaft(data, "vLkw", MAX_GUETE_WERT);
				}
			}

			/**
			 * Regel B9
			 */
			if (getValue(origDatum, "qLkw") == 0) {
				if (getValue(origDatum, "vLkw") != DUAKonstanten.NICHT_ERMITTELBAR && getValue(origDatum, "vLkw") != DUAKonstanten.NICHT_ERMITTELBAR_BZW_FEHLERHAFT) {
					messageTls.add("attr", "v");
					messageTls.add("values", formatAttributes(origDatum, "qLkw", "vLkw"));
					messageTls.addId("[DUA-PP-VT03]");

					setzeFehlerhaft(data, "vKfz", MAX_GUETE_WERT);
					setzeFehlerhaft(data, "vPkw", MAX_GUETE_WERT);
					setzeFehlerhaft(data, "vLkw", MAX_GUETE_WERT);
				}
			}
			
			/**
			 * Regel B10
			 */
			if (getValue(origDatum, "vKfz") > 0) {
				if (getValue(origDatum, "qKfz") <= 0) {
					messageTls.add("attr", "q");
					messageTls.add("values", formatAttributes(origDatum, "vKfz", "qKfz"));
					messageTls.addId("[DUA-PP-VT04]");

					setzeFehlerhaft(data, "qKfz", MAX_GUETE_WERT);
					setzeFehlerhaft(data, "qPkw", MAX_GUETE_WERT);
					setzeFehlerhaft(data, "qLkw", MAX_GUETE_WERT);
				}
			}
			
			/**
			 * Regel B11
			 */
			if (getValue(origDatum, "vPkw") > 0) {
				if (getValue(origDatum, "qPkw") <= 0) {
					messageTls.add("attr", "q");
					messageTls.add("values", formatAttributes(origDatum, "vPkw", "qPkw"));
					messageTls.addId("[DUA-PP-VT05]");

					setzeFehlerhaft(data, "qKfz", MAX_GUETE_WERT);
					setzeFehlerhaft(data, "qPkw", MAX_GUETE_WERT);
					setzeFehlerhaft(data, "qLkw", MAX_GUETE_WERT);
				}
			}		
			
			/**
			 * Regel B12
			 */
			if (getValue(origDatum, "vLkw") > 0) {
				if (getValue(origDatum, "qLkw") <= 0) {
					messageTls.add("attr", "q");
					messageTls.add("values", formatAttributes(origDatum, "vLkw", "qLkw"));
					messageTls.addId("[DUA-PP-VT06]");

					setzeFehlerhaft(data, "qKfz", MAX_GUETE_WERT);
					setzeFehlerhaft(data, "qPkw", MAX_GUETE_WERT);
					setzeFehlerhaft(data, "qLkw", MAX_GUETE_WERT);
				}
			}
		}

		// DUA-14
		/**
		 * Regel B13
		 */
		if (getValue(origDatum, "qKfz") == 0) {
			if (getValue(origDatum, "vgKfz") >= 0 && vgKfzLetztesIntervall >= 0) {
				if (getValue(origDatum, "vgKfz") != vgKfzLetztesIntervall) {
					messageVerkehr.add("attr", "vgKfz");
					messageVerkehr.add("values",
					                    formatAttributes(origDatum, "qKfz", "vgKfz")
							                .append(formatAttributeValue("vgKfzAlt", vgKfzLetztesIntervallDisplay))
					);
					messageVerkehr.addId("[DUA-PP-VV01]");

					setzeFehlerhaft(data, "vgKfz", MAX_GUETE_WERT);
				}
			}
		}

		/**
		 * Regel B14
		 */
		if (parameterAtgGrenz != null) {
			synchronized (parameterAtgGrenz) {
				if (getValue(origDatum, "vKfz") >= 0 && parameterAtgGrenz.getVKfzGrenz() >= 0) {
					if (getValue(origDatum, "vKfz") > parameterAtgGrenz.getVKfzGrenz()) {
						if (getValue(origDatum, "b") >= 0 && parameterAtgGrenz.getBGrenz() >= 0) {
							if (!(getValue(origDatum, "b") < parameterAtgGrenz.getBGrenz())) {
								messageVerkehr.add("attr", "b");
								messageVerkehr.add("values",
								                    formatAttributes(origDatum, "b")
								                .append(formatAttributeValue("bGrenz", parameterAtgGrenz.getBGrenz() + " %"))
								                .append(formatAttributes(origDatum, "vKfz"))
								                .append(formatAttributeValue("vGrenz", parameterAtgGrenz.getVKfzGrenz() + " km/h"))
								);
								messageVerkehr.addId("[DUA-PP-VV02]");

								setzeFehlerhaft(data, "b", MAX_GUETE_WERT);
							}
						}
					}
				}

				/**
				 * Regel B15
				 */
				untersucheAufMaxVerletzung(data, "qKfz", messageGrenzwert, "[DUA-PP-VGW01]");
				
				/**
				 * Regel B16
				 */
				untersucheAufMaxVerletzung(data, "qLkw", messageGrenzwert, "[DUA-PP-VGW02]");
				
				/**
				 * Regel B17
				 */
				untersucheQPkw(data);
				
				/**
				 * Regel B18
				 */
				untersucheAufMaxVerletzung(data, "vKfz", messageGrenzwert, "[DUA-PP-VGW04]");

				/**
				 * Regel B19
				 */
				untersucheAufMaxVerletzung(data, "vLkw", messageGrenzwert, "[DUA-PP-VGW05]");

				/**
				 * Regel B20
				 */
				untersucheAufMaxVerletzung(data, "vPkw", messageGrenzwert, "[DUA-PP-VGW06]");

				/**
				 * Regel B21
				 */
				untersucheAufMaxVerletzung(data, "vgKfz", messageGrenzwert, "[DUA-PP-VGW07]");

				/**
				 * Regel B22
				 */
				untersucheAufMaxVerletzung(data, "b", messageGrenzwert, "[DUA-PP-VGW08]");
			}
			if(parameterAtgGrenz.isMessageTls()) {
				messageTls.send();
			}
			if(parameterAtgGrenz.isMessageVerkehr()) {
				messageVerkehr.send();
			}
			messageGrenzwert.send();
		}

	}

	private static long getValue(final Data data, final String name) {
		return data
				.getItem(name).getUnscaledValue("Wert").longValue();
	}

	/**
	 * Erfragt das Systemobjekt der Attributgruppe, unter der die Parameter für die Grenzwertprüfung stehen.
	 *
	 * @param dav die Datenverteiler-Verbindung
	 * @return die Parameter-Attributgruppe
	 */
	public static AttributeGroup getParameterAtg(
			final ClientDavInterface dav) {
		return dav.getDataModel().getAttributeGroup(
				AtgVerkehrsDatenKurzZeitIntervallPlLogisch.getPid());
	}

	/**
		 * Erfragt eine Liste aller Attributnamen, dieninnerhalb eines bestimmten Datensatzes enthalten sind (KZD bzw. LZD)
		 *
		 * @return eine Liste aller Attributnamen, dieninnerhalb eines bestimmten Datensatzes enthalten sind (KZD bzw. LZD)
		 */
	protected String[] getAttributNamen() {
		return ATTRIBUT_NAMEN;
	}

	public void update(final ResultData[] parameterFeld) {
		if(parameterFeld != null) {
			for(final ResultData parameter : parameterFeld) {
				if(parameter != null && parameter.getData() != null) {
					if(parameter
							.getDataDescription()
							.getAttributeGroup()
							.equals(getParameterAtg(dieVerwaltung.getVerbindung()))) {
						synchronized(this) {
							parameterAtgGrenz = AtgVerkehrsDatenKurzZeitIntervallPlLogisch.getInstance(parameter);
						}
					}	
				}
			}
		}
	}

	/**
	 * <b>Nach Afo 5.2</b><br> Untersucht die obere Grenze des Wertebereichs eines Verkehrs-Datums und markiert ggf. Verletzungene nichts<br>
	 *
	 * @param davDatum ein zu veränderndes Verkehrs-Datums (darf nicht <code>null</code> sein)
	 * @param wertName der Name des final Attributs
	 * @param messageText
	 */
	protected void untersucheAufMaxVerletzung(final Data davDatum, final String wertName, final OperatingMessage messageText, final String id) {
		final GanzZahl sweGueteWert = GanzZahl.getGueteIndex();
		sweGueteWert.setSkaliertenWert(0.8);
		final GWert sweGuete = new GWert(sweGueteWert,
		                                 GueteVerfahren.STANDARD, false);
		
		if(parameterAtgGrenz != null) {
			
			long maxStunde = parameterAtgGrenz.getMax(wertName);
			OptionenPlausibilitaetsPruefungLogischVerkehr verhalten = parameterAtgGrenz.getVerhalten();

			if(verhalten.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG)) {
				return;
			}

			final long wert = getValue(davDatum, wertName);



			final long t = davDatum.getTimeValue("T").getMillis();
			
			final long wertStunde;
			final long maxIntervall;
			if(wertName.startsWith("q")){
				wertStunde = wert * MILLIS_PER_HOUR / t;
				maxIntervall = maxStunde * t / MILLIS_PER_HOUR;
			}
			else {
				wertStunde = wert;
				maxIntervall = maxStunde;
			}
			
			/**
			 * sonst handelt es sich nicht um einen Messwert
			 */
			if(wert >= 0 && maxStunde >= 0) {
				final boolean maxVerletzt = wertStunde > maxStunde;

				if(maxVerletzt) {
					if(verhalten.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.WERT_REDUZIEREN)) {
						
						davDatum.getItem(wertName).getUnscaledValue("Wert").set(maxIntervall); 
						davDatum.getItem(wertName).getItem("Status").getItem("PlLogisch").getUnscaledValue("WertMaxLogisch").set(DUAKonstanten.JA);
						
					} else if(verhalten.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.AUF_FEHLERHAFT_SETZEN)) {

						messageText.add("values", formatMax(wertName, wertStunde, maxStunde));
						if(wertName.startsWith("v")){
							setzeFehlerhaft(davDatum, "vKfz", sweGuete, messageText, id);
							setzeFehlerhaft(davDatum, "vPkw", sweGuete, messageText, id);
							setzeFehlerhaft(davDatum, "vLkw", sweGuete, messageText, id);
							setzeFehlerhaft(davDatum, "vgKfz", sweGuete, messageText, id);
						}
						else if(wertName.startsWith("q")){
							setzeFehlerhaft(davDatum, "qKfz", sweGuete, messageText, id);
							setzeFehlerhaft(davDatum, "qPkw", sweGuete, messageText, id);
							setzeFehlerhaft(davDatum, "qLkw", sweGuete, messageText, id);
						}
						else {
							setzeFehlerhaft(davDatum, wertName, sweGuete, messageText, id);
						}
					}
				}
			}
		}
	}
	
	protected void untersucheQPkw(final Data davDatum) {
		final GanzZahl sweGueteWert = GanzZahl.getGueteIndex();
		sweGueteWert.setSkaliertenWert(0.8);
		
		if(parameterAtgGrenz != null) {
			
			OptionenPlausibilitaetsPruefungLogischVerkehr verhalten = parameterAtgGrenz.getVerhalten();

			if(verhalten.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG)) {
				return;
			}

			final long qPkw = getValue(davDatum, "qPkw");
			final long qLkw = getValue(davDatum, "qLkw");
			final long qKfz = getValue(davDatum, "qKfz");

			final boolean bedingungVerletzt = qPkw >= 0 && qLkw >= 0 && qKfz >= 0 && (qPkw + qLkw != qKfz);

			if(bedingungVerletzt) {
				davDatum.getItem("qPkw").getUnscaledValue("Wert").set(qKfz - qLkw); 
				davDatum.getItem("qPkw").getItem("Status").getItem("PlLogisch").getUnscaledValue("WertMaxLogisch").set(DUAKonstanten.JA);
			}
		}
	}

	private String formatMax(final String attr, final long wertStunde, final long maxStunde) {
		if(attr.startsWith("q")){
			return attr + "=" + wertStunde + " Fz/h > " + maxStunde + " Fz/h";
		}
		if(attr.startsWith("v")){
			return attr + "=" + wertStunde + " km/h > " + maxStunde + " km/h";
		}
		if(attr.startsWith("b")){
			return attr + "=" + wertStunde + " % > " + maxStunde + " %";
		}
		return attr + "=" + wertStunde + " > " + maxStunde;

	}

	private void setzeFehlerhaft(final Data davDatum, final String wertName, final GWert sweGuete) {
		davDatum.getItem(wertName).getUnscaledValue("Wert").set(DUAKonstanten.FEHLERHAFT);
		davDatum.getItem(wertName).getItem("Status").getItem("PlLogisch").getUnscaledValue("WertMaxLogisch").set(DUAKonstanten.JA);
		davDatum.getItem(wertName).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(DUAKonstanten.JA);
		GWert guete = new GWert(davDatum, wertName);
		try {
			GWert produkt = GueteVerfahren.produkt(guete, sweGuete);
			produkt.exportiere(davDatum, wertName);
		}
		catch(GueteException e) {
			Debug.getLogger().warning("Güte nicht berechenbar", e);
		}
	}

	private void setzeFehlerhaft(final Data davDatum, final String wertName, final GWert sweGuete, OperatingMessage messageText, final String id) {
		setzeFehlerhaft(davDatum, wertName, sweGuete);

		messageText.add("attr", wertName);
		messageText.addId(id);
	}

	/**
	 * Plausibilisiert ein übergebenes Datum.
	 *
	 * @param resultat ein Originaldatum
	 * @return das veränderte Datum oder <code>null</code>, wenn keine Veränderungen vorgenommen werden mussten
	 */
	protected Data plausibilisiere(final ResultData resultat) {
		Data copy = null;

		if(resultat.getData() != null) {
			try {
				copy = resultat.getData().createModifiableCopy();
				ueberpruefe(copy, resultat);
			}
			catch(final IllegalStateException e) {
				Debug.getLogger().error(
						"Es konnte keine Kopie von Datensatz erzeugt werden:\n" 
								+ resultat, e);
			}
		}
		letztesKZDatum = resultat;

		return copy;
	}

	public static class MessageAttributeSet {
		private final LinkedHashSet<String> _param;

		public MessageAttributeSet(final LinkedHashSet<String> param) {
			_param = param;
		}

		@Override
		public String toString() {
			return OperatingMessage.formatCollection(_param, " und ", "", "").toString();
		}

		public MessageAttributeSet append(MessageAttributeSet other){
			LinkedHashSet<String> set = new LinkedHashSet<>(_param.size() + other._param.size());
			set.addAll(_param);
			set.addAll(other._param);
			return new MessageAttributeSet(set);
		}
		
		public MessageAttributeSet append(String other){
			LinkedHashSet<String> set = new LinkedHashSet<>(_param.size() + 1);
			set.addAll(_param);
			set.add(other);
			return new MessageAttributeSet(set);
		}
		
		@Override
		public boolean equals(final Object o) {
			if(this == o) return true;
			if(!(o instanceof MessageAttributeSet)) return false;

			final MessageAttributeSet that = (MessageAttributeSet) o;

			return _param.equals(that._param);

		}

		@Override
		public int hashCode() {
			return _param.hashCode();
		}
	}
}
