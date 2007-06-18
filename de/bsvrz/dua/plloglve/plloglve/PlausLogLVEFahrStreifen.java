package de.bsvrz.dua.plloglve.plloglve;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import stauma.dav.clientside.ClientReceiverInterface;
import stauma.dav.clientside.Data;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.debug.Debug;
import de.bsvrz.dua.plloglve.plloglve.daten.AtgVerkehrsDatenKZIPlPruefLogisch;
import de.bsvrz.dua.plloglve.plloglve.typen.OptionenPlausibilitaetsPruefungLogischVerkehr;
import de.bsvrz.sys.funclib.bitctrl.daf.Konstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAKonstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAUtensilien;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.modell.AbstractSystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjekt;
import de.bsvrz.sys.funclib.bitctrl.modell.SystemObjektTyp;

/**
 * 
 * @author Thierfelder
 *
 */
public class PlausLogLVEFahrStreifen
extends AbstractSystemObjekt
implements Comparable<PlausLogLVEFahrStreifen>, ClientReceiverInterface{
	
	/**
	 * Debug-Logger
	 */
	private static final Debug LOGGER = Debug.getLogger();
	
	/**
	 * Verbindung zum Verwaltungsmodul
	 */
	protected static IVerwaltung VERWALTUNG = null;
	
	/**
	 * Datenbeschreibung für <code>atg.tlsLveBetriebsParameter</code>
	 */
	private static DataDescription DE_BETRIEBS_PARAMETER = null;
	
	/**
	 * Datenbeschreibung für <code>atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch</code>
	 */
	private static DataDescription KZD_PL_LOG_PARAMETER = null;	
	
	/**
	 * Alle statischen Instanzen dieser Klasse
	 */
	private static Map<SystemObject, PlausLogLVEFahrStreifen> INSTANZEN = Collections.synchronizedMap(
			new TreeMap<SystemObject, PlausLogLVEFahrStreifen>());

	/**
	 * Parametersatz <code>atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch</code>
	 */
	private AtgVerkehrsDatenKZIPlPruefLogisch parameterAtgLog = null;
	
	/**
	 * Die Art der Mittelwertbildung
	 */
	private boolean mittelWertBildungIstArithmethisch = true;
	
	/**
	 * letztes zur Plausibilisierung übergebenes Datum
	 */
	private ResultData letztesKZDatum = null;
	

	/**
	 * Standartdkonstruktor 
	 * 
	 * @param obj das mit dem Fahrstreifen assoziierte Systemobjekt
	 */
	private PlausLogLVEFahrStreifen(final SystemObject obj)
	throws Exception{
		super(obj);
		
		VERWALTUNG.getVerbindung().subscribeReceiver(this, obj, KZD_PL_LOG_PARAMETER,
				ReceiveOptions.normal(), ReceiverRole.receiver());		
		
		/**
		 * Versuche die Daten der assoziierten DE auszulesen
		 * um die Art der Mittelwertbindung zu ermitteln
		 */
		DataDescription konfig = new DataDescription(
				VERWALTUNG.getVerbindung().getDataModel().getAttributeGroup("atg.fahrStreifen"), //$NON-NLS-1$
				VERWALTUNG.getVerbindung().getDataModel().getAspect("asp.eigenschaften"), //$NON-NLS-1$
				(short)0);
		ResultData konfigResult = VERWALTUNG.getVerbindung().getData(obj, konfig, 10000L);
		
		if(konfigResult != null && konfigResult.getData() != null){
			Data.ReferenceValue refValue = konfigResult.getData().getReferenceValue("FahrStreifenQuelle"); //$NON-NLS-1$
			if(refValue != null){
				SystemObject deObj = refValue.getSystemObject();
				if(deObj.isOfType("typ.deLve")){ //$NON-NLS-1$
					VERWALTUNG.getVerbindung().subscribeReceiver(this, deObj, DE_BETRIEBS_PARAMETER,
							ReceiveOptions.normal(), ReceiverRole.receiver());
				}else{
					LOGGER.error("Mit dem Fahrstreifen " +  //$NON-NLS-1$
							this.getPid() + " ist kein DE-LVE assoziiert"); //$NON-NLS-1$
				}
			}else{
				LOGGER.error("Mit dem Fahrstreifen " +  //$NON-NLS-1$
						this.getPid() + " ist keine Quelle assoziiert"); //$NON-NLS-1$
			}
		}else{
			LOGGER.error("Die DE-Konfiguration für den Fahrstreifen " +  //$NON-NLS-1$
					this.getPid() + " konnte nicht ausgelesen werden"); //$NON-NLS-1$
		}
	}
	

	public static final synchronized PlausLogLVEFahrStreifen getInstanz(final SystemObject obj,
																  final IVerwaltung verwaltung)
	throws Exception{
		if(VERWALTUNG == null){
			VERWALTUNG = verwaltung;
			DE_BETRIEBS_PARAMETER = new DataDescription(
					VERWALTUNG.getVerbindung().getDataModel().getAttributeGroup("atg.tlsLveBetriebsParameter"), //$NON-NLS-1$
					VERWALTUNG.getVerbindung().getDataModel().getAspect(Konstanten.ASP_PARAMETER_SOLL),
					(short)0);
			KZD_PL_LOG_PARAMETER = new DataDescription(
					VERWALTUNG.getVerbindung().getDataModel().getAttributeGroup(
							"atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch"), //$NON-NLS-1$
					VERWALTUNG.getVerbindung().getDataModel().getAspect(Konstanten.ASP_PARAMETER_SOLL),
					(short)0);
		}
		
		PlausLogLVEFahrStreifen dummy = INSTANZEN.get(obj);
		
		if(dummy != null){
			dummy = new PlausLogLVEFahrStreifen(obj);
			INSTANZEN.put(obj, dummy);
		}
		
		return dummy;
	}

	
	
	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] parameterFeld) {
		if(parameterFeld != null){
			for(ResultData parameter:parameterFeld){
				if(parameter != null && parameter.getData() != null){
					if(parameter.getDataDescription().getAttributeGroup().equals(
							KZD_PL_LOG_PARAMETER.getAttributeGroup())){
						synchronized (this.parameterAtgLog) {
							this.parameterAtgLog = new AtgVerkehrsDatenKZIPlPruefLogisch(parameter.getData());
						}
					}else if(parameter.getDataDescription().getAttributeGroup().equals(
							DE_BETRIEBS_PARAMETER.getAttributeGroup())){
						this.mittelWertBildungIstArithmethisch = parameter.getData().
								getUnscaledValue("ArtMittelWertBildung").intValue() == 1; //$NON-NLS-1$
					}else{
						LOGGER.warning("Unbekanntes Datum empfangen:\n" + parameter); //$NON-NLS-1$
					}
				}
			}
		}
	}
	

	protected final Data plausibilisiere(final ResultData resultat){
		Data data = null;
//		if(parameter == null){
//			LOGGER.warning("Für das Objekt " + resultat.getObject().getPid() + " liegen keine Parameter der " + //$NON-NLS-1$ //$NON-NLS-2$
//			"Attributgruppe \"atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch\" vor"); //$NON-NLS-1$
//		}else if(resultat.getData() != null){
//			Data copy = resultat.getData().createModifiableCopy();
//			
//			if(copy != null){
//				
//			}else{
//				LOGGER.warning("Es konnte keine Kopie von Datensatz erzeugt werden:\n" //$NON-NLS-1$
//						+ resultat);
//			}
//		}
		this.letztesKZDatum = resultat;

		return data;
	}

	
	private final void berechneQPkw(Data data){
		
		/**
		 * TODO: Güte
		 */
		final int qKfz = data.getItem("qKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int qLkw = data.getItem("qLkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		int qPkw = PLLOGKonstanten.NICHT_ERMITTELBAR;
		if(qKfz >= 0 && qLkw >= 0){
			qPkw = qKfz - qLkw;
		}		
		data.getItem("qPkw").getUnscaledValue("Wert").set(qPkw); //$NON-NLS-1$ //$NON-NLS-2$
		
		final int vPkw = data.getItem("vPkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int vLkw = data.getItem("vLkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		int vKfz = PLLOGKonstanten.NICHT_ERMITTELBAR;
		if(qKfz > 0 && qPkw >= 0 && vPkw >= 0 && qLkw >= 0 && vLkw >= 0){
			vKfz = (qPkw * vPkw + qLkw * vLkw) / qKfz;
		}
		data.getItem("vKfz").getUnscaledValue("Wert").set(vKfz);  //$NON-NLS-1$//$NON-NLS-2$
	}
	

	/**
	 * 
	 * @param data
	 * @param resultat
	 */
	private final void ueberpruefeKontextFehler(Data data, ResultData resultat){

		final int qKfz = data.getItem("qKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int qLkw = data.getItem("qLkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int qPkw = data.getItem("qPkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int vPkw = data.getItem("vPkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int vLkw = data.getItem("vLkw").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int vKfz = data.getItem("vKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final int vgKfz = data.getItem("vgKfz").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final long tNetto = data.getItem("tNetto").getUnscaledValue("Wert").longValue(); //$NON-NLS-1$ //$NON-NLS-2$
		final long T = data.getTimeValue("T").getMillis(); //$NON-NLS-1$
		final int b = data.getItem("b").getUnscaledValue("Wert").intValue(); //$NON-NLS-1$ //$NON-NLS-2$

		int vgKfzLetztesIntervall = -4;
		if(this.letztesKZDatum != null){
			if(this.letztesKZDatum.getData() != null){
				if(T == resultat.getDataTime() - this.letztesKZDatum.getDataTime()){
					vgKfzLetztesIntervall = this.letztesKZDatum.getData().getItem("vgKfz"). //$NON-NLS-1$
					getUnscaledValue("Wert").intValue(); //$NON-NLS-1$
				}
			}
		}

		/**
		 * Regel Nr.1 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if(qKfz == 0 && (qLkw != 0 || qPkw != 0)){
			data.getItem("qKfz").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("qLkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("qPkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

			data.getItem("qKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			data.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			data.getItem("qPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		}

		if(this.mittelWertBildungIstArithmethisch){

			/**
			 * Regel Nr.2 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
			 */
			if(qKfz - qLkw == 0 && (qPkw != 0 || vPkw != PLLOGKonstanten.NICHT_ERMITTELBAR)){
				data.getItem("qKfz").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
				data.getItem("qLkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
				data.getItem("qPkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
				data.getItem("vPkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

				data.getItem("qKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
				data.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
				data.getItem("qPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
				data.getItem("vPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			}

			/**
			 * Regel Nr.3 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
			 */
			if(qLkw == 0 && vLkw != PLLOGKonstanten.NICHT_ERMITTELBAR){
				data.getItem("qLkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
				data.getItem("vLkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

				data.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
				data.getItem("vLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$				
			}

			/**
			 * Regel Nr.4 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
			 */
			if(qPkw == 0 && vPkw != PLLOGKonstanten.NICHT_ERMITTELBAR){
				data.getItem("qPkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
				data.getItem("vPkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

				data.getItem("qPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
				data.getItem("vPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$				
			}		
		}

		/**
		 * Regel Nr.5 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if(qKfz < qLkw){
			data.getItem("qKfz").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("qLkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

			data.getItem("qKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			data.getItem("vLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$							
		}

		/**
		 * Regel Nr.6 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if(qKfz - qLkw > 0 && vPkw <= 0){
			data.getItem("qKfz").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("qLkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("vPkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

			data.getItem("qKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			data.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			data.getItem("vPkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		}

		/**
		 * Regel Nr.7 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if(qKfz > 0 && vKfz <= 0){
			data.getItem("qKfz").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("vKfz").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

			data.getItem("qKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			data.getItem("vKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		}

		/**
		 * Regel Nr.8 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if(qLkw > 0 && vLkw <= 0){
			data.getItem("qLkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$
			data.getItem("vLkw").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

			data.getItem("qLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			data.getItem("vLkw").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		}

		/**
		 * Regel Nr.9 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if( !(0 < tNetto && tNetto <= T) ){
			data.getItem("tNetto").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

			data.getItem("tNetto").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
		}

		/**
		 * Regel Nr.10 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if(vgKfzLetztesIntervall != -4){
			if(qKfz == 0 && vgKfz != vgKfzLetztesIntervall){
				data.getItem("vgKfz").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

				data.getItem("vgKfz").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$
			}
		}

		/**
		 * Regel Nr.11 (aus SE-02.00.00.00.00-AFo-4.0, S.94)
		 */
		if(vKfz > parameterAtgLog.getVKfzGrenz() && b >= parameterAtgLog.getBGrenz() ){
			data.getItem("b").getUnscaledValue("Wert").set(PLLOGKonstanten.FEHLERHAFT); //$NON-NLS-1$ //$NON-NLS-2$

			data.getItem("b").getItem("Status").getItem("MessWertErsetzung").   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			getUnscaledValue("Implausibel").set(DUAKonstanten.JA); //$NON-NLS-1$			
		}

		grenzWertTests(data);
	}
	
	private final void grenzWertTests(Data data){
		if(this.parameterAtgLog != null){
			synchronized (this.parameterAtgLog) {				
				/**
				 * Regel Nr.12 (aus SE-02.00.00.00.00-AFo-4.0, S.95)
				 */
				data = this.untersucheWerteBereich(data, "qKfz", this.parameterAtgLog.getQKfzBereichMin(),  //$NON-NLS-1$
																	this.parameterAtgLog.getQKfzBereichMax());
	
				/**
				 * Regel Nr.13 (aus SE-02.00.00.00.00-AFo-4.0, S.95)
				 */
				data = this.untersucheWerteBereich(data, "qPkw", this.parameterAtgLog.getQPkwBereichMin(),  //$NON-NLS-1$
																	this.parameterAtgLog.getQPkwBereichMax());

				/**
				 * Regel Nr.14 (aus SE-02.00.00.00.00-AFo-4.0, S.95)
				 */
				data = this.untersucheWerteBereich(data, "qLkw", this.parameterAtgLog.getQLkwBereichMin(),  //$NON-NLS-1$
																	this.parameterAtgLog.getQLkwBereichMax());

				/**
				 * Regel Nr.15 (aus SE-02.00.00.00.00-AFo-4.0, S.95)
				 */
				data = this.untersucheWerteBereich(data, "vKfz", this.parameterAtgLog.getVKfzBereichMin(),  //$NON-NLS-1$
																	this.parameterAtgLog.getVKfzBereichMax());

				/**
				 * Regel Nr.16 (aus SE-02.00.00.00.00-AFo-4.0, S.96)
				 */
				data = this.untersucheWerteBereich(data, "vLkw", this.parameterAtgLog.getVLkwBereichMin(),  //$NON-NLS-1$
																	this.parameterAtgLog.getVLkwBereichMax());

				/**
				 * Regel Nr.17 (aus SE-02.00.00.00.00-AFo-4.0, S.96)
				 */
				data = this.untersucheWerteBereich(data, "vPkw", this.parameterAtgLog.getVPkwBereichMin(),  //$NON-NLS-1$
																	this.parameterAtgLog.getVPkwBereichMax());

				/**
				 * Regel Nr.18 (aus SE-02.00.00.00.00-AFo-4.0, S.96)
				 */
				data = this.untersucheWerteBereich(data, "vgKfz", this.parameterAtgLog.getVgKfzBereichMin(),  //$NON-NLS-1$
																	this.parameterAtgLog.getVgKfzBereichMax());

				/**
				 * Regel Nr.19 (aus SE-02.00.00.00.00-AFo-4.0, S.96)
				 */
				data = this.untersucheWerteBereich(data, "b", this.parameterAtgLog.getBelegungBereichMin(),  //$NON-NLS-1$
																	this.parameterAtgLog.getBelegungBereichMax());
			}
		}			
	}
	
	
	private Data untersucheWerteBereich(Data davDatum, final String wertName, final int min, final int max){
		if(davDatum != null && this.parameterAtgLog != null){
			OptionenPlausibilitaetsPruefungLogischVerkehr optionen = this.parameterAtgLog.getOptionen();

			if(!optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.KEINE_PRUEFUNG)){
				int wert = davDatum.getItem(wertName).getUnscaledValue("Wert").intValue(); //$NON-NLS-1$
				
				/**
				 * sonst handelt es sich nicht um einen Messwert
				 */
				if(wert >= 0){
					boolean minVerletzt = wert < min;
					boolean maxVerletzt = wert > max;
		
					if(minVerletzt){
						DUAUtensilien.getAttributDatum(wertName + ".Status.PlLogisch.WertMinLogisch", davDatum). //$NON-NLS-1$
									asUnscaledValue().set(DUAKonstanten.JA);
					}
					if(maxVerletzt){
						DUAUtensilien.getAttributDatum(wertName + ".Status.PlLogisch.WertMaxLogisch", davDatum). //$NON-NLS-1$
									asUnscaledValue().set(DUAKonstanten.JA);
					}
						
					if(optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MAX)){
						if(maxVerletzt){
							DUAUtensilien.getAttributDatum(wertName + ".Wert", davDatum). //$NON-NLS-1$
								asUnscaledValue().set(max);
						}					
					}else if(optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN)){
						if(minVerletzt){
							DUAUtensilien.getAttributDatum(wertName + ".Wert", davDatum). //$NON-NLS-1$
								asUnscaledValue().set(min);
						}															
					}else if(optionen.equals(OptionenPlausibilitaetsPruefungLogischVerkehr.SETZE_MIN_MAX)){
						if(maxVerletzt){
							DUAUtensilien.getAttributDatum(wertName + ".Wert", davDatum). //$NON-NLS-1$
								asUnscaledValue().set(max);
						}else					
						if(minVerletzt){
							DUAUtensilien.getAttributDatum(wertName + ".Wert", davDatum). //$NON-NLS-1$
								asUnscaledValue().set(min);
						}															
					}
				}
			}
		}
		
		return davDatum;
	}


	/**
	 * {@inheritDoc}
	 */
	public SystemObjektTyp getTyp() {
		return new SystemObjektTyp(){

			public Class<? extends SystemObjekt> getKlasse() {
				return PlausLogLVEFahrStreifen.class;
			}

			public String getPid() {
				return getSystemObject().getType().getPid();
			}
			
		};
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(PlausLogLVEFahrStreifen that) {
		return new Long(this.getId()).compareTo(that.getId());
	}
	
}
