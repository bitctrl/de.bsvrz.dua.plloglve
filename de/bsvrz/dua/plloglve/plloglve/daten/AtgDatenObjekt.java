package de.bsvrz.dua.plloglve.plloglve.daten;

import java.lang.reflect.Method;

import de.bsvrz.sys.funclib.bitctrl.konstante.Konstante;

/**
 * Allgemeine Klasse für die Beschreibung von Objekten, die <b>nur</b> Daten halten,
 * auf welche über Getter-Methoden (ohne Argumente) zugegriffen werden kann.
 * (z.B. Attributgruppeninhalte)
 * 
 * @author Thierfelder
 *
 */
public class AtgDatenObjekt {
	
	
	/**
	 * Vergleicht dieses Objekt mit dem übergebenen Objekt. Die beiden Objekte
	 * sind dann gleich, wenn sie vom selben Typ sind und wenn alle Getter-Methoden
	 * die gleichen Werte zurückliefern.
	 * 
	 * @param that ein anderes Objekt
	 * @return ob die beiden Objekte inhaltlich gleich sind
	 */
	@Override
	public boolean equals(Object that) {
		if(that != null){
			if(that.getClass().equals(this.getClass())){
				for(Method method:this.getClass().getMethods()){
					if(method.getName().startsWith("get")){  //$NON-NLS-1$
						try {
							Object thisInhalt = method.invoke(this, new Object[0]);
							Object thatInhalt = method.invoke(that, new Object[0]);
							if(!thisInhalt.equals(thatInhalt)){
								return false;
							}
						} catch (Exception ex){
							return false;
						}
					}
				}
				return true;
			}
		}
				
		return false;
	}
	

	/**
	 * Erfragt eine Zeichenkette, welche die aktuellen Werte aller über Getter-Methoden
	 * zugänglichen Member-Variable enthält
	 * 
	 * @return eine Inhaltsangabe dieses Objektes
	 */
	@Override
	public String toString() {
		String s = Konstante.LEERSTRING;
		
		for(Method methode:this.getClass().getMethods()){
			if(methode.getName().startsWith("get") &&  //$NON-NLS-1$
			   methode.getDeclaringClass().equals(this.getClass())){
				s += methode.getName().substring(3) + " = ";  //$NON-NLS-1$
				try {
					s += methode.invoke(this, new Object[0]);
				} catch (Exception ex){
					s += "unbekannt";   //$NON-NLS-1$
				}
				s += "\n";  //$NON-NLS-1$
			}
		}
		
		return s;
	}
	
}
