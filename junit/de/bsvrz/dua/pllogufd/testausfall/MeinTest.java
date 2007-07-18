package de.bsvrz.dua.pllogufd.testausfall;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;

import stauma.dav.clientside.ClientDavInterface;
import stauma.dav.clientside.ClientReceiverInterface;
import stauma.dav.clientside.ClientSenderInterface;
import stauma.dav.clientside.Data;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import de.bsvrz.dua.pllogufd.DAVTest;
import de.bsvrz.dua.pllogufd.PlPruefungLogischUFDTest;
import de.bsvrz.dua.pllogufd.TestUtensilien;
import de.bsvrz.dua.pllogufd.UmfeldDatenSensorDatum;
import de.bsvrz.dua.pllogufd.typen.UmfeldDatenArt;
import de.bsvrz.sys.funclib.bitctrl.app.Pause;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;

public class MeinTest
implements ClientSenderInterface, ClientReceiverInterface{
	/**
	 * Die Zeit (in ms) die die erwartete Eintreffzeit eines Datums von
	 * der tatsächlichen Eintreffzeit differieren darf
	 */
	protected static final long ERGEBNIS_TOLERANZ = 2000;
	
	/**
	 * Parameter <code>maxZeitVerzug</code> für Sensoren xxx1
	 */
	private static final long MAX_VERZUG_1 = 500L;
	
	/**
	 * Parameter <code>maxZeitVerzug</code> für Sensoren xxx2
	 */
	private static final long MAX_VERZUG_2 = 501L;
	
	/**
	 * Parameter <code>maxZeitVerzug</code> für Sensoren xxx3
	 */
	private static final long MAX_VERZUG_3 = 1500L;
	
	/**
	 * Datenverteiler-Verbindung
	 */
	private ClientDavInterface dav = null;
	
	private Boolean erg1 = null;
	
	private Boolean erg2 = null;

	boolean gesendet1 = false;
	
	boolean gesendet2 = false;

	boolean ist1 = false;
	
	boolean ist2 = false;

	
	/**
	 * {@inheritDoc}
	 */
	@Before
	public void setUp() throws Exception {
		this.dav = DAVTest.getDav();
		PlPruefungLogischUFDTest.initialisiere();
		
		/**
		 * Warte bis Anmeldung sicher durch ist
		 */
		Pause.warte(1000L);
		
		/**
		 * Parameter setzen auf 10s (für Sensoren xxx1), 15s (für Sensoren xxx2) und 20s (für Sensoren xxx3)
		 */
		for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
			if(sensor.getPid().endsWith("1")){ //$NON-NLS-1$
				PlPruefungLogischUFDTest.SENDER.setMaxAusfallFuerSensor(sensor, MAX_VERZUG_1);
			}else
			if(sensor.getPid().endsWith("2")){ //$NON-NLS-1$
				PlPruefungLogischUFDTest.SENDER.setMaxAusfallFuerSensor(sensor, MAX_VERZUG_2);
			}else
			if(sensor.getPid().endsWith("3")){ //$NON-NLS-1$
				PlPruefungLogischUFDTest.SENDER.setMaxAusfallFuerSensor(sensor, MAX_VERZUG_3);
			}
		}
		
		/**
		 * Warte eine Sekunde bis die Parameter sicher da sind
		 */
		
				
		/**
		 * Anmeldung auf alle Daten die aus der Applikation Pl-Prüfung logisch UFD kommen
		 */
		for(SystemObject sensor:PlPruefungLogischUFDTest.SENSOREN){
			UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);
			DataDescription datenBeschreibung = new DataDescription(
					dav.getDataModel().getAttributeGroup("atg.ufds" + datenArt.getName()), //$NON-NLS-1$
					dav.getDataModel().getAspect("asp.plausibilitätsPrüfungLogisch"), //$NON-NLS-1$
					(short)0);
			dav.subscribeReceiver(this, sensor, datenBeschreibung,
					ReceiveOptions.delayed(), ReceiverRole.receiver());
		}
		
		/**
		 * Warte eine Sekunde bis Datenanmeldung durch ist
		 */
		Pause.warte(1000L);		
	}
	
	
	/**
	 * Erzeugt einen Messwert mit der Datenbeschreibung <code>asp.externeErfassung</code>
	 * 
	 * @param sensor ein Umfelddatensensor, für den ein Messwert erzeugt werden soll
	 * @return ein (ausgefüllter) Umfelddaten-Messwert der zum übergebenen Systemobjekt passt.
	 * Alle Pl-Prüfungs-Flags sind auf <code>NEIN</code> gesetzt. Der Daten-Intervall beträgt
	 * 1 min.
	 */
	public static final ResultData getExterneErfassungDatum(SystemObject sensor) {
		UmfeldDatenArt datenArt = UmfeldDatenArt.getUmfeldDatenArtVon(sensor);		
		DataDescription datenBeschreibung = new DataDescription(
				PlPruefungLogischUFDTest.DAV.getDataModel().getAttributeGroup("atg.ufds" + datenArt.getName()), //$NON-NLS-1$
				PlPruefungLogischUFDTest.DAV.getDataModel().getAspect("asp.externeErfassung"), //$NON-NLS-1$
				(short)0);
		Data datum = PlPruefungLogischUFDTest.DAV.createData(
				PlPruefungLogischUFDTest.DAV.getDataModel().getAttributeGroup("atg.ufds" + datenArt.getName())); //$NON-NLS-1$
		datum.getTimeValue("T").setMillis(1L * 1000L); //$NON-NLS-1$
		datum.getItem(datenArt.getName()).getUnscaledValue("Wert").set(0); //$NON-NLS-1$
		datum.getItem(datenArt.getName()).getItem("Status").getItem("Erfassung"). //$NON-NLS-1$ //$NON-NLS-2$
				getUnscaledValue("NichtErfasst").set(DUAKonstanten.NEIN); //$NON-NLS-1$
		datum.getItem(datenArt.getName()).getItem("Status").getItem("PlFormal"). //$NON-NLS-1$ //$NON-NLS-2$
				getUnscaledValue("WertMax").set(DUAKonstanten.NEIN); //$NON-NLS-1$
		datum.getItem(datenArt.getName()).getItem("Status").getItem("PlFormal"). //$NON-NLS-1$ //$NON-NLS-2$
				getUnscaledValue("WertMin").set(DUAKonstanten.NEIN); //$NON-NLS-1$

		datum.getItem(datenArt.getName()).getItem("Status").getItem("MessWertErsetzung"). //$NON-NLS-1$ //$NON-NLS-2$
				getUnscaledValue("Implausibel").set(DUAKonstanten.NEIN); //$NON-NLS-1$
		datum.getItem(datenArt.getName()).getItem("Status").getItem("MessWertErsetzung"). //$NON-NLS-1$ //$NON-NLS-2$
				getUnscaledValue("Interpoliert").set(DUAKonstanten.NEIN); //$NON-NLS-1$

		datum.getItem(datenArt.getName()).getItem("Güte").getUnscaledValue("Index").set(10000); //$NON-NLS-1$ //$NON-NLS-2$
		datum.getItem(datenArt.getName()).getItem("Güte").getUnscaledValue("Verfahren").set(0); //$NON-NLS-1$ //$NON-NLS-2$
		
		return new ResultData(sensor, datenBeschreibung, System.currentTimeMillis(), datum);
	}	

	
	/**
	 * Anzahl der Intervalle, die der Test der Ausfallüberwachung laufen soll
	 */
	private static final long TEST_AUSFALL_UEBERWACHUNG_LAEUFE = 1000000;
	
	
	/**
	 * der eigentliche Test
	 */
	@Test
	public void test()
	throws Exception{
						
		
		GregorianCalendar kal = new GregorianCalendar();
		kal.setTimeInMillis(System.currentTimeMillis());
		kal.set(Calendar.MILLISECOND, 0);
		int fSekunden = kal.get(Calendar.SECOND)/1;
		kal.set(Calendar.SECOND, fSekunden * 1 + 1);
		long startAlles = kal.getTimeInMillis();
		
		
		/**
		 * Sende initiale Daten
		 */
		long ersteDatenZeit = startAlles - 1000;
		
		ResultData resultat1 = TestUtensilien.getExterneErfassungDatum(PlPruefungLogischUFDTest.gt1);
		ResultData resultat2 = TestUtensilien.getExterneErfassungDatum(PlPruefungLogischUFDTest.gt2);
//		resultat1.setDataTime(ersteDatenZeit);
//		resultat2.setDataTime(ersteDatenZeit);
//		PlPruefungLogischUFDTest.SENDER.sende(resultat1);
//		PlPruefungLogischUFDTest.SENDER.sende(resultat2);
//		System.out.println(ct() + ", Initiales Datum: " + DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(ersteDatenZeit))); //$NON-NLS-1$
		
		/**
		 * Test-Schleife
		 */
		for(int testZaehler = 0; testZaehler < TEST_AUSFALL_UEBERWACHUNG_LAEUFE; testZaehler++){
	

			kal = new GregorianCalendar();
			kal.setTimeInMillis(System.currentTimeMillis());
			kal.set(Calendar.MILLISECOND, 0);
			fSekunden = kal.get(Calendar.SECOND)/1;
			kal.set(Calendar.SECOND, fSekunden * 1 + 1);
			startAlles = kal.getTimeInMillis();
			
			ersteDatenZeit = startAlles - 1000;
			/**
			 * Warte bis zum Anfang des nächsten Intervalls
			 */
			while(startAlles > System.currentTimeMillis()){
				Pause.warte(10);	
			}	
			
			if(testZaehler > 1){
				if(!ist1){
					System.out.println("---------------------------------------11111111111111--------------------------------------------"); //$NON-NLS-1$
				}
				if(!ist2){
					System.out.println("---------------------------------------22222222222222--------------------------------------------"); //$NON-NLS-1$
				}

//				Assert.assertTrue("1: ", ist1); //$NON-NLS-1$
//				Assert.assertTrue("2: ", ist2); //$NON-NLS-1$
			}
			erg1 = null;
			erg2 = null;
			gesendet1 = false;
			gesendet2 = false;

			System.out.println("\n-------\n" + //$NON-NLS-1$
					ct() + ", " + (testZaehler +1) + ". Datum: " + DUAKonstanten.NUR_ZEIT_FORMAT_GENAU.format(new Date(startAlles))); //$NON-NLS-1$ //$NON-NLS-2$

			if(testZaehler%2 == 0){
				if(testZaehler%10 == 0)continue;
				Pause.warte(DAVTest.R.nextInt(700));
				
				resultat1 = getExterneErfassungDatum(PlPruefungLogischUFDTest.gt1);
				resultat1.setDataTime(ersteDatenZeit);
				synchronized (this) {
					gesendet1 = true;
					PlPruefungLogischUFDTest.SENDER.sende(resultat1);
					System.out.println(ct() + ", Sende: " + resultat1.getObject().getPid() + ", " + DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(resultat1.getDataTime())));  //$NON-NLS-1$//$NON-NLS-2$
				}				
				
				if(testZaehler%7 == 0)continue;
				Pause.warte(DAVTest.R.nextInt(10));
				resultat2 = getExterneErfassungDatum(PlPruefungLogischUFDTest.gt2);
				resultat2.setDataTime(ersteDatenZeit);
				synchronized (this) {
					gesendet2 = true;
					PlPruefungLogischUFDTest.SENDER.sende(resultat2);
					System.out.println(ct() + ", Sende: " + resultat2.getObject().getPid() + ", " + DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(resultat2.getDataTime())));  //$NON-NLS-1$//$NON-NLS-2$
				}
			}else{
				if(testZaehler%13 == 0)continue;
				Pause.warte(DAVTest.R.nextInt(700));
				resultat2 = getExterneErfassungDatum(PlPruefungLogischUFDTest.gt2);
				resultat2.setDataTime(ersteDatenZeit);
				synchronized (this) {
					gesendet2 = true;
					PlPruefungLogischUFDTest.SENDER.sende(resultat2);
					System.out.println(ct() + ", Sende: " + resultat2.getObject().getPid() + ", " + DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(resultat2.getDataTime())));  //$NON-NLS-1$//$NON-NLS-2$
				}
				
				Pause.warte(DAVTest.R.nextInt(5));
				
				if(testZaehler%9 == 0)continue;
				resultat1 = getExterneErfassungDatum(PlPruefungLogischUFDTest.gt1);
				resultat1.setDataTime(ersteDatenZeit);
				synchronized (this) {
					gesendet1 = true;
					PlPruefungLogischUFDTest.SENDER.sende(resultat1);
					System.out.println(ct() + ", Sende: " + resultat1.getObject().getPid() + ", " + DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(resultat1.getDataTime())));  //$NON-NLS-1$//$NON-NLS-2$
				}
			}
			
		}		
	}


	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(SystemObject object, DataDescription dataDescription, byte state) {
		// 		
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean isRequestSupported(SystemObject object, DataDescription dataDescription) {
		return false;
	}


	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] resultate) {
		if(resultate != null){
			for(ResultData resultat:resultate){
				if(resultat != null && resultat.getData() != null){
					synchronized(this){
						UmfeldDatenSensorDatum ufdDatum = new UmfeldDatenSensorDatum(resultat);
						if(resultat.getObject().equals(PlPruefungLogischUFDTest.gt1)){
							String implausibel = (ufdDatum.getStatusErfassungNichtErfasst() == DUAKonstanten.JA?"nicht erfasst":"erfasst"); //$NON-NLS-1$ //$NON-NLS-2$
							if(erg1 == null){
								erg1 = ufdDatum.getStatusErfassungNichtErfasst() == DUAKonstanten.JA;
								
								
								this.ist1 = gesendet1 == (ufdDatum.getStatusErfassungNichtErfasst() != DUAKonstanten.JA);
							}
							System.out.println(ct() + ", Empfange: " + resultat.getObject() + ", " +  //$NON-NLS-1$ //$NON-NLS-2$
									DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(resultat.getDataTime())) + " --> " + implausibel); //$NON-NLS-1$						 
	
						}
						if(resultat.getObject().equals(PlPruefungLogischUFDTest.gt2)){
							String implausibel = (ufdDatum.getStatusErfassungNichtErfasst() == DUAKonstanten.JA?"nicht erfasst":"erfasst"); //$NON-NLS-1$ //$NON-NLS-2$
							if(erg2 == null){
								erg2 = ufdDatum.getStatusErfassungNichtErfasst() == DUAKonstanten.JA;
								
								this.ist2 = gesendet2 == (ufdDatum.getStatusErfassungNichtErfasst() != DUAKonstanten.JA);
							}						
							System.out.println(ct() + ", Empfange: " + resultat.getObject() + ", " +  //$NON-NLS-1$ //$NON-NLS-2$
									DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(resultat.getDataTime())) + " --> " + implausibel); //$NON-NLS-1$	
						}
					}
				}
			}
		}
	}


	
	private String ct(){
		return "(NOW:" + DUAKonstanten.ZEIT_FORMAT_GENAU.format(new Date(System.currentTimeMillis())) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
