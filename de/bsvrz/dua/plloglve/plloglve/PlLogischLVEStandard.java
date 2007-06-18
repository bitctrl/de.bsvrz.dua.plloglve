package de.bsvrz.dua.plloglve.plloglve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import stauma.dav.clientside.Data;
import stauma.dav.clientside.DataDescription;
import stauma.dav.clientside.ReceiveOptions;
import stauma.dav.clientside.ReceiverRole;
import stauma.dav.clientside.ResultData;
import stauma.dav.configuration.interfaces.SystemObject;
import sys.funclib.debug.Debug;
import de.bsvrz.sys.funclib.bitctrl.daf.Konstanten;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.av.DAVEmpfangsAnmeldungsVerwaltung;
import de.bsvrz.sys.funclib.bitctrl.dua.av.DAVObjektAnmeldung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

public class PlLogischLVEStandard
extends AbstraktBearbeitungsKnotenAdapter{
		
	/**
	 * Debug-Logger
	 */
	private static final Debug LOGGER = Debug.getLogger(); 
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(IVerwaltung dieVerwaltung)
	throws DUAInitialisierungsException {
		super.initialisiere(dieVerwaltung);
			
		DataDescription anmeldungsBeschreibungPara = new DataDescription(
				dieVerwaltung.getVerbindung().getDataModel().getAttributeGroup(
						"atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch"), //$NON-NLS-1$
				dieVerwaltung.getVerbindung().getDataModel().getAspect(Konstanten.ASP_PARAMETER_SOLL),
				(short)0);
	
		Collection<DAVObjektAnmeldung> anmeldungen = new TreeSet<DAVObjektAnmeldung>();
		for(SystemObject obj:dieVerwaltung.getSystemObjekte()){
			try{
				anmeldungen.add(new DAVObjektAnmeldung(obj, anmeldungsBeschreibungPara));
			}catch(Exception ex){
				throw new DUAInitialisierungsException(
						"Es konnten nicht alle Parameteranmeldungen durchgeführt werden", ex); //$NON-NLS-1$
			}
		}
		
//		DAVEmpfangsAnmeldungsVerwaltung empfangsVerwaltung = new DAVEmpfangsAnmeldungsVerwaltung(
//				dieVerwaltung.getVerbindung(),
//				ReceiverRole.receiver(),
//				ReceiveOptions.normal(),
//				this);
		
//		empfangsVerwaltung.modifiziereObjektAnmeldung(anmeldungen);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {
		if(resultate != null){
			Collection<ResultData> weiterzuleitendeResultate = new ArrayList<ResultData>();
			for(ResultData resultat:resultate){
				if(resultat != null){
					Data pData = null;
//					pData = this.plausibilisiere(resultat, this.parameterMap.get(resultat.getObject()));

					if(pData != null){
						ResultData ersetztesResultat = new ResultData(
								resultat.getObject(),
								resultat.getDataDescription(),
								resultat.getDataTime(),
								pData);
						weiterzuleitendeResultate.add(ersetztesResultat);
					}else{
						weiterzuleitendeResultate.add(resultat);						
					}
				}
			}
			
			if(this.knoten != null && !weiterzuleitendeResultate.isEmpty()){
				this.knoten.aktualisiereDaten(weiterzuleitendeResultate.toArray(new ResultData[0]));
			}
		}
	}

	

	/**
	 * {@inheritDoc}
	 */
	public ModulTyp getModulTyp() {
		return ModulTyp.PL_PRUEFUNG_LOGISCH_LVE;
	}

	/**
	 * {@inheritDoc}
	 */
	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// hier findet keine Publikation statt
	}

}