package de.bsvrz.dua.plloglve.plloglve;

import stauma.dav.clientside.ResultData;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktBearbeitungsKnotenAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;

public class Ausfallhaeufigkeit
extends AbstraktBearbeitungsKnotenAdapter{

	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {
		// TODO Automatisch erstellter Methoden-Stub
		
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
		// TODO Automatisch erstellter Methoden-Stub
		
	}


}
