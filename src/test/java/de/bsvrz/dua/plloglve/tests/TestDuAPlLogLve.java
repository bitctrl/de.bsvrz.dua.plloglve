/*
 * Copyright 2016 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dua.plloglve.tests.
 * 
 * de.bsvrz.dua.plloglve.tests is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dua.plloglve.tests is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.dua.plloglve.tests.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dua.plloglve.tests;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.tests.ColumnLayout;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeSet;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 */
public class TestDuAPlLogLve extends DuAPlLogLveTestBase {

	private DataDescription _ddIn;
	private DataDescription _ddOut;
	private SystemObject[] _testFs;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		_testFs = new SystemObject[]{_dataModel.getObject("fs.mq.1.hfs")};

		for(SystemObject obj : _testFs) {
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenKurzZeitAnalyseFs",
			                          "{" +
					                          "kKfz:{Grenz:'48',Max:'68'}," +
					                          "kLkw:{Grenz:'28',Max:'38'}," +
					                          "kPkw:{Grenz:'48',Max:'68'}," +
					                          "kB:{Grenz:'58',Max:'77'}," +
					                          "fl:{k1:'2,2',k2:'0,02'}" +
					                          "}"
			);

			publishPrognoseParamsFs(obj);

		}

		AttributeGroup atg = _dataModel.getAttributeGroup("atg.verkehrsDatenKurzZeitIntervall");
		Aspect aspInput = _dataModel.getAspect("asp.externeErfassung");
		Aspect aspOutput = _dataModel.getAspect("asp.plausibilitätsPrüfungLogisch");
		_ddIn = new DataDescription(atg, aspInput);
		_ddOut = new DataDescription(atg, aspOutput);

		Thread.sleep(1000);
	}

	@Test
	public void testDua13() throws Exception {
		for(SystemObject obj : _testFs) {
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch2",
			                          "{" +
					                          "ErzeugeBetriebsmeldungPrüfungTLS:'Ja'," +
					                          "ErzeugeBetriebsmeldungPrüfungVerkehr:'Ja'," +
					                          "VerhaltenGrenzwertPrüfung:'Keine Prüfung'," +
					                          "PrüfintervallAusfallHäufigkeit:'15 Minuten'," +
					                          "MaxAusfallZeitProTag:'2 Stunden'," +
					                          "BezugszeitraumVertrauensbereich:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichEin:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichAus:'0 Minuten'," +
					                          "PrüfintervallVertrauensbereich:'0 Minuten'," +
					                          "vKfzGrenz:'100 km/h'," +
					                          "bGrenz:'80 %'," +
					                          "qKfzMax:'4000'," +
					                          "qLkwMax:'4000'," +
					                          "vKfzMax:'254'," +
					                          "vLkwMax:'254'," +
					                          "vPkwMax:'254'," +
					                          "vgKfzMax:'254'," +
					                          "bMax:'100'" +
					                          "}"
			);
		}
		startTestCase("DUA13.csv", _testFs, _testFs, _ddIn, _ddOut, new PlColumnLayout() {
			@Override
			public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
				if(fullRow.get(22).equals("X")) return "[DUA-PP-VT01]";
				if(fullRow.get(23).equals("X")) return "[DUA-PP-VT02]";
				if(fullRow.get(24).equals("X")) return "[DUA-PP-VT03]";
				if(fullRow.get(25).equals("X")) return "[DUA-PP-VT04]";
				if(fullRow.get(26).equals("X")) return "[DUA-PP-VT05]";
				if(fullRow.get(27).equals("X")) return "[DUA-PP-VT06]";
				if(fullRow.get(28).equals("X")) return "[DUA-PP-VT07]";
				return null;
			}
		});
	}

	@Test
	public void testDua13Nomessage() throws Exception {
		for(SystemObject obj : _testFs) {
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch2",
			                          "{" +
					                          "ErzeugeBetriebsmeldungPrüfungTLS:'Nein'," +
					                          "ErzeugeBetriebsmeldungPrüfungVerkehr:'Ja'," +
					                          "VerhaltenGrenzwertPrüfung:'Keine Prüfung'," +
					                          "PrüfintervallAusfallHäufigkeit:'15 Minuten'," +
					                          "MaxAusfallZeitProTag:'2 Stunden'," +
					                          "BezugszeitraumVertrauensbereich:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichEin:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichAus:'0 Minuten'," +
					                          "PrüfintervallVertrauensbereich:'0 Minuten'," +
					                          "vKfzGrenz:'100 km/h'," +
					                          "bGrenz:'80 %'," +
					                          "qKfzMax:'4000'," +
					                          "qLkwMax:'4000'," +
					                          "vKfzMax:'254'," +
					                          "vLkwMax:'254'," +
					                          "vPkwMax:'254'," +
					                          "vgKfzMax:'254'," +
					                          "bMax:'100'" +
					                          "}"
			);
		}
		startTestCase("DUA13.csv", _testFs, _testFs, _ddIn, _ddOut, new PlColumnLayout());
	}

	@Test
	public void testDua14() throws Exception {
		for(SystemObject obj : _testFs) {
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch2",
			                          "{" +
					                          "ErzeugeBetriebsmeldungPrüfungTLS:'Ja'," +
					                          "ErzeugeBetriebsmeldungPrüfungVerkehr:'Ja'," +
					                          "VerhaltenGrenzwertPrüfung:'Keine Prüfung'," +
					                          "PrüfintervallAusfallHäufigkeit:'15 Minuten'," +
					                          "MaxAusfallZeitProTag:'2 Stunden'," +
					                          "BezugszeitraumVertrauensbereich:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichEin:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichAus:'0 Minuten'," +
					                          "PrüfintervallVertrauensbereich:'0 Minuten'," +
					                          "vKfzGrenz:'100 km/h'," +
					                          "bGrenz:'80 %'," +
					                          "qKfzMax:'4000'," +
					                          "qLkwMax:'4000'," +
					                          "vKfzMax:'254'," +
					                          "vLkwMax:'254'," +
					                          "vPkwMax:'254'," +
					                          "vgKfzMax:'254'," +
					                          "bMax:'100'" +
					                          "}"
			);
		}
		startTestCase("DUA14-1.csv", _testFs, _testFs, _ddIn, _ddOut, new PlColumnLayout() {
			@Override
			public Collection<String> getIgnored() {
				return ImmutableSet.of("T", "b", "tNetto", "sKfz", "NichtErfasst", "WertMaxLogisch", "vKfz", "qLkw", "vLkw", "qPkw", "vPkw");
			}

			@Override
			public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
				if(fullRow.get(22).equals("X")) return "[DUA-PP-VV01]";
				return null;
			}

		});
	}

	@Test
	public void testDua14B() throws Exception {
		for(SystemObject obj : _testFs) {
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch2",
			                          "{" +
					                          "ErzeugeBetriebsmeldungPrüfungTLS:'Ja'," +
					                          "ErzeugeBetriebsmeldungPrüfungVerkehr:'Ja'," +
					                          "VerhaltenGrenzwertPrüfung:'Keine Prüfung'," +
					                          "PrüfintervallAusfallHäufigkeit:'15 Minuten'," +
					                          "MaxAusfallZeitProTag:'2 Stunden'," +
					                          "BezugszeitraumVertrauensbereich:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichEin:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichAus:'0 Minuten'," +
					                          "PrüfintervallVertrauensbereich:'0 Minuten'," +
					                          "vKfzGrenz:'100 km/h'," +
					                          "bGrenz:'80 %'," +
					                          "qKfzMax:'4000'," +
					                          "qLkwMax:'4000'," +
					                          "vKfzMax:'254'," +
					                          "vLkwMax:'254'," +
					                          "vPkwMax:'254'," +
					                          "vgKfzMax:'254'," +
					                          "bMax:'100'" +
					                          "}"
			);
		}
		startTestCase("DUA14-2.csv", _testFs, _testFs, _ddIn, _ddOut, new PlColumnLayout() {
			@Override
			public Collection<String> getIgnored() {
				return ImmutableSet.of("T", "tNetto", "sKfz", "vgKfz", "NichtErfasst", "WertMaxLogisch", "qKfz", "qLkw", "vLkw", "qPkw", "vPkw");
			}

			@Override
			public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
				if(fullRow.get(22).equals("X")) return "[DUA-PP-VV02]";
				return null;
			}
		});
	}

	@Test
	public void testDua14NoMessage() throws Exception {
		for(SystemObject obj : _testFs) {
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch2",
			                          "{" +
					                          "ErzeugeBetriebsmeldungPrüfungTLS:'Ja'," +
					                          "ErzeugeBetriebsmeldungPrüfungVerkehr:'Nein'," +
					                          "VerhaltenGrenzwertPrüfung:'Keine Prüfung'," +
					                          "PrüfintervallAusfallHäufigkeit:'15 Minuten'," +
					                          "MaxAusfallZeitProTag:'2 Stunden'," +
					                          "BezugszeitraumVertrauensbereich:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichEin:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichAus:'0 Minuten'," +
					                          "PrüfintervallVertrauensbereich:'0 Minuten'," +
					                          "vKfzGrenz:'100 km/h'," +
					                          "bGrenz:'80 %'," +
					                          "qKfzMax:'4000'," +
					                          "qLkwMax:'4000'," +
					                          "vKfzMax:'254'," +
					                          "vLkwMax:'254'," +
					                          "vPkwMax:'254'," +
					                          "vgKfzMax:'254'," +
					                          "bMax:'100'" +
					                          "}"
			);
		}
		startTestCase("DUA14-1.csv", _testFs, _testFs, _ddIn, _ddOut, new PlColumnLayout() {
			@Override
			public Collection<String> getIgnored() {
				return ImmutableSet.of("T", "b", "tNetto", "sKfz", "NichtErfasst", "WertMaxLogisch", "vKfz", "qLkw", "vLkw", "qPkw", "vPkw");
			}

			@Override
			public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
				return null;
			}

		});
	}

	@Test
	public void testDua14NoMessageB() throws Exception {
		for(SystemObject obj : _testFs) {
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch2",
			                          "{" +
					                          "ErzeugeBetriebsmeldungPrüfungTLS:'Ja'," +
					                          "ErzeugeBetriebsmeldungPrüfungVerkehr:'Nein'," +
					                          "VerhaltenGrenzwertPrüfung:'Keine Prüfung'," +
					                          "PrüfintervallAusfallHäufigkeit:'15 Minuten'," +
					                          "MaxAusfallZeitProTag:'2 Stunden'," +
					                          "BezugszeitraumVertrauensbereich:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichEin:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichAus:'0 Minuten'," +
					                          "PrüfintervallVertrauensbereich:'0 Minuten'," +
					                          "vKfzGrenz:'100 km/h'," +
					                          "bGrenz:'80 %'," +
					                          "qKfzMax:'4000'," +
					                          "qLkwMax:'4000'," +
					                          "vKfzMax:'254'," +
					                          "vLkwMax:'254'," +
					                          "vPkwMax:'254'," +
					                          "vgKfzMax:'254'," +
					                          "bMax:'100'" +
					                          "}"
			);
		}
		startTestCase("DUA14-2.csv", _testFs, _testFs, _ddIn, _ddOut, new PlColumnLayout() {
			@Override
			public Collection<String> getIgnored() {
				return ImmutableSet.of("T", "tNetto", "sKfz", "vgKfz", "NichtErfasst", "WertMaxLogisch", "qKfz", "qLkw", "vLkw", "qPkw", "vPkw", "Wert", "Implausibel");
			}

			@Override
			public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
				return null;
			}
		});
	}

	@Test
	public void testDua1516a() throws Exception {
		for(SystemObject obj : _testFs) {
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch2",
			                          "{" +
					                          "ErzeugeBetriebsmeldungPrüfungTLS:'Nein'," +
					                          "ErzeugeBetriebsmeldungPrüfungVerkehr:'Nein'," +
					                          "VerhaltenGrenzwertPrüfung:'Wert reduzieren'," +
					                          "PrüfintervallAusfallHäufigkeit:'15 Minuten'," +
					                          "MaxAusfallZeitProTag:'2 Stunden'," +
					                          "BezugszeitraumVertrauensbereich:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichEin:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichAus:'0 Minuten'," +
					                          "PrüfintervallVertrauensbereich:'0 Minuten'," +
					                          "vKfzGrenz:'100 km/h'," +
					                          "bGrenz:'100 %'," +
					                          "qKfzMax:'1800'," +
					                          "qLkwMax:'180'," +
					                          "vKfzMax:'115'," +
					                          "vLkwMax:'90'," +
					                          "vPkwMax:'120'," +
					                          "vgKfzMax:'113'," +
					                          "bMax:'80'" +
					                          "}"
			);
		}
		startTestCase("DUA15-1.csv", _testFs, _testFs, _ddIn, _ddOut, new PlLongColumnLayout() {
			@Override
			public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
				return null;
			}
		});
	}

	@Test
	public void testDua1516b() throws Exception {
		for(SystemObject obj : _testFs) {
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch2",
			                          "{" +
					                          "ErzeugeBetriebsmeldungPrüfungTLS:'Nein'," +
					                          "ErzeugeBetriebsmeldungPrüfungVerkehr:'Nein'," +
					                          "VerhaltenGrenzwertPrüfung:'Auf fehlerhaft setzen'," +
					                          "PrüfintervallAusfallHäufigkeit:'15 Minuten'," +
					                          "MaxAusfallZeitProTag:'2 Stunden'," +
					                          "BezugszeitraumVertrauensbereich:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichEin:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichAus:'0 Minuten'," +
					                          "PrüfintervallVertrauensbereich:'0 Minuten'," +
					                          "vKfzGrenz:'100 km/h'," +
					                          "bGrenz:'100 %'," +
					                          "qKfzMax:'1800'," +
					                          "qLkwMax:'180'," +
					                          "vKfzMax:'115'," +
					                          "vLkwMax:'90'," +
					                          "vPkwMax:'120'," +
					                          "vgKfzMax:'113'," +
					                          "bMax:'80'" +
					                          "}"
			);
		}
		startTestCase("DUA15-2.csv", _testFs, _testFs, _ddIn, _ddOut, new PlLongColumnLayout());
	}

	@Test
	public void testDua1516c() throws Exception {
		for(SystemObject obj : _testFs) {
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch2",
			                          "{" +
					                          "ErzeugeBetriebsmeldungPrüfungTLS:'Nein'," +
					                          "ErzeugeBetriebsmeldungPrüfungVerkehr:'Nein'," +
					                          "VerhaltenGrenzwertPrüfung:'Keine Prüfung'," +
					                          "PrüfintervallAusfallHäufigkeit:'15 Minuten'," +
					                          "MaxAusfallZeitProTag:'2 Stunden'," +
					                          "BezugszeitraumVertrauensbereich:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichEin:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichAus:'0 Minuten'," +
					                          "PrüfintervallVertrauensbereich:'0 Minuten'," +
					                          "vKfzGrenz:'100 km/h'," +
					                          "bGrenz:'100 %'," +
					                          "qKfzMax:'30'," +
					                          "qLkwMax:'3'," +
					                          "vKfzMax:'115'," +
					                          "vLkwMax:'90'," +
					                          "vPkwMax:'120'," +
					                          "vgKfzMax:'113'," +
					                          "bMax:'80'" +
					                          "}"
			);
		}
		startTestCase("DUA15-3.csv", _testFs, _testFs, _ddIn, _ddOut, new PlLongColumnLayout() {
			@Override
			public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
				return null;
			}
		});
	}

	@Test
	public void testDua51() throws Exception {
		for(SystemObject obj : _testFs) {
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch2",
			                          "{" +
					                          "ErzeugeBetriebsmeldungPrüfungTLS:'Nein'," +
					                          "ErzeugeBetriebsmeldungPrüfungVerkehr:'Nein'," +
					                          "VerhaltenGrenzwertPrüfung:'Keine Prüfung'," +
					                          "PrüfintervallAusfallHäufigkeit:'15 Minuten'," +
					                          "MaxAusfallZeitProTag:'2 Stunden'," +
					                          "BezugszeitraumVertrauensbereich:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichEin:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichAus:'0 Minuten'," +
					                          "PrüfintervallVertrauensbereich:'0 Minuten'," +
					                          "vKfzGrenz:'100 km/h'," +
					                          "bGrenz:'80 %'," +
					                          "qKfzMax:'4000'," +
					                          "qLkwMax:'4000'," +
					                          "vKfzMax:'254'," +
					                          "vLkwMax:'254'," +
					                          "vPkwMax:'254'," +
					                          "vgKfzMax:'254'," +
					                          "bMax:'100'" +
					                          "}"
			);
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenDifferenzialKontrolleFs2",
			                          "{" +
					                          "maxAnzKonstanzVerkehrsmenge:'6'," +
					                          "maxAnzKonstanzGeschwindigkeit:'4'," +
					                          "maxAnzKonstanzStreung:'7'," +
					                          "maxAnzKonstanzBelegung:'4'" +
					                          "}"
			);
		}
		startTestCase("DUA51.csv", _testFs, _testFs, _ddIn, _ddOut, new PlLongColumnLayout2());
	}

	@Test
	public void testDua51B() throws Exception {
		for(SystemObject obj : _testFs) {
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch2",
			                          "{" +
					                          "ErzeugeBetriebsmeldungPrüfungTLS:'Nein'," +
					                          "ErzeugeBetriebsmeldungPrüfungVerkehr:'Nein'," +
					                          "VerhaltenGrenzwertPrüfung:'Keine Prüfung'," +
					                          "PrüfintervallAusfallHäufigkeit:'15 Minuten'," +
					                          "MaxAusfallZeitProTag:'2 Stunden'," +
					                          "BezugszeitraumVertrauensbereich:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichEin:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichAus:'0 Minuten'," +
					                          "PrüfintervallVertrauensbereich:'0 Minuten'," +
					                          "vKfzGrenz:'100 km/h'," +
					                          "bGrenz:'80 %'," +
					                          "qKfzMax:'4000'," +
					                          "qLkwMax:'4000'," +
					                          "vKfzMax:'254'," +
					                          "vLkwMax:'254'," +
					                          "vPkwMax:'254'," +
					                          "vgKfzMax:'254'," +
					                          "bMax:'100'" +
					                          "}"
			);
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenDifferenzialKontrolleFs2",
			                          "{" +
					                          "maxAnzKonstanzVerkehrsmenge:'0'," +
					                          "maxAnzKonstanzGeschwindigkeit:'4'," +
					                          "maxAnzKonstanzStreung:'0'," +
					                          "maxAnzKonstanzBelegung:'4'" +
					                          "}"
			);
		}
		startTestCase("DUA51.csv", _testFs, _testFs, _ddIn, _ddOut, new PlLongColumnLayout2(){
			@Override
			public LinkedHashSet<String> adjustAttributes(final LinkedHashSet<String> set) {
				set.remove("s");
				set.remove("q");
				return set;
			}

			@Override
			public Collection<String> getIgnored() {
				// Soll-Werte stimmen nicht mehr aufgrund anderer Parametrierung
				return ImmutableSet.of("T", "Wert", "Implausibel", "NichtErfasst", "WertMaxLogisch");
			}
		});
	}

	@Test
	public void testDua51C() throws Exception {
		for(SystemObject obj : _testFs) {
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch2",
			                          "{" +
					                          "ErzeugeBetriebsmeldungPrüfungTLS:'Nein'," +
					                          "ErzeugeBetriebsmeldungPrüfungVerkehr:'Nein'," +
					                          "VerhaltenGrenzwertPrüfung:'Keine Prüfung'," +
					                          "PrüfintervallAusfallHäufigkeit:'15 Minuten'," +
					                          "MaxAusfallZeitProTag:'2 Stunden'," +
					                          "BezugszeitraumVertrauensbereich:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichEin:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichAus:'0 Minuten'," +
					                          "PrüfintervallVertrauensbereich:'0 Minuten'," +
					                          "vKfzGrenz:'100 km/h'," +
					                          "bGrenz:'80 %'," +
					                          "qKfzMax:'4000'," +
					                          "qLkwMax:'4000'," +
					                          "vKfzMax:'254'," +
					                          "vLkwMax:'254'," +
					                          "vPkwMax:'254'," +
					                          "vgKfzMax:'254'," +
					                          "bMax:'100'" +
					                          "}"
			);
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenDifferenzialKontrolleFs2",
			                          "{" +
					                          "maxAnzKonstanzVerkehrsmenge:'6'," +
					                          "maxAnzKonstanzGeschwindigkeit:'0'," +
					                          "maxAnzKonstanzStreung:'7'," +
					                          "maxAnzKonstanzBelegung:'0'" +
					                          "}"
			);
		}
		startTestCase("DUA51.csv", _testFs, _testFs, _ddIn, _ddOut, new PlLongColumnLayout2(){
			@Override
			public LinkedHashSet<String> adjustAttributes(final LinkedHashSet<String> set) {
				set.remove("v");
				set.remove("b");
				return set;
			}

			@Override
			public Collection<String> getIgnored() {
				// Soll-Werte stimmen nicht mehr aufgrund anderer Parametrierung
				return ImmutableSet.of("T", "Wert", "Implausibel", "NichtErfasst", "WertMaxLogisch");
			}
		});
	}

	@Test
	public void testDua51D() throws Exception {
		for(SystemObject obj : _testFs) {
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch2",
			                          "{" +
					                          "ErzeugeBetriebsmeldungPrüfungTLS:'Nein'," +
					                          "ErzeugeBetriebsmeldungPrüfungVerkehr:'Nein'," +
					                          "VerhaltenGrenzwertPrüfung:'Keine Prüfung'," +
					                          "PrüfintervallAusfallHäufigkeit:'15 Minuten'," +
					                          "MaxAusfallZeitProTag:'2 Stunden'," +
					                          "BezugszeitraumVertrauensbereich:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichEin:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichAus:'0 Minuten'," +
					                          "PrüfintervallVertrauensbereich:'0 Minuten'," +
					                          "vKfzGrenz:'100 km/h'," +
					                          "bGrenz:'80 %'," +
					                          "qKfzMax:'4000'," +
					                          "qLkwMax:'4000'," +
					                          "vKfzMax:'254'," +
					                          "vLkwMax:'254'," +
					                          "vPkwMax:'254'," +
					                          "vgKfzMax:'254'," +
					                          "bMax:'100'" +
					                          "}"
			);
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenDifferenzialKontrolleFs2",
			                          "{" +
					                          "maxAnzKonstanzVerkehrsmenge:'0'," +
					                          "maxAnzKonstanzGeschwindigkeit:'0'," +
					                          "maxAnzKonstanzStreung:'0'," +
					                          "maxAnzKonstanzBelegung:'0'" +
					                          "}"
			);
		}
		startTestCase("DUA51.csv", _testFs, _testFs, _ddIn, _ddOut, new PlLongColumnLayout2(){
			@Override
			public LinkedHashSet<String> adjustAttributes(final LinkedHashSet<String> set) {
				set.remove("q");
				set.remove("v");
				set.remove("s");
				set.remove("b");
				return set;
			}

			@Override
			public Collection<String> getIgnored() {
				// Soll-Werte stimmen nicht mehr aufgrund anderer Parametrierung
				return ImmutableSet.of("T", "Wert", "Implausibel", "NichtErfasst", "WertMaxLogisch");
			}
		});
	}

	@Test
	public void testDua52() throws Exception {
		for(SystemObject obj : _testFs) {
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch2",
			                          "{" +
					                          "ErzeugeBetriebsmeldungPrüfungTLS:'Nein'," +
					                          "ErzeugeBetriebsmeldungPrüfungVerkehr:'Nein'," +
					                          "VerhaltenGrenzwertPrüfung:'Keine Prüfung'," +
					                          "PrüfintervallAusfallHäufigkeit:'10 Minuten'," +
					                          "MaxAusfallZeitProTag:'93 Minuten'," +
					                          "BezugszeitraumVertrauensbereich:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichEin:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichAus:'0 Minuten'," +
					                          "PrüfintervallVertrauensbereich:'0 Minuten'," +
					                          "vKfzGrenz:'100 km/h'," +
					                          "bGrenz:'80 %'," +
					                          "qKfzMax:'4000'," +
					                          "qLkwMax:'4000'," +
					                          "vKfzMax:'254'," +
					                          "vLkwMax:'254'," +
					                          "vPkwMax:'254'," +
					                          "vgKfzMax:'254'," +
					                          "bMax:'100'" +
					                          "}"
			);
		}
		startTestCase("DUA52.csv", _testFs, _testFs, _ddIn, _ddOut, new Pl52Layout());
	}
	
	@Test
	public void testDua52B() throws Exception {
		for(SystemObject obj : _testFs) {
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch2",
			                          "{" +
					                          "ErzeugeBetriebsmeldungPrüfungTLS:'Nein'," +
					                          "ErzeugeBetriebsmeldungPrüfungVerkehr:'Nein'," +
					                          "VerhaltenGrenzwertPrüfung:'Keine Prüfung'," +
					                          "PrüfintervallAusfallHäufigkeit:'10 Minuten'," +
					                          "MaxAusfallZeitProTag:'0 Minuten'," +
					                          "BezugszeitraumVertrauensbereich:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichEin:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichAus:'0 Minuten'," +
					                          "PrüfintervallVertrauensbereich:'0 Minuten'," +
					                          "vKfzGrenz:'100 km/h'," +
					                          "bGrenz:'80 %'," +
					                          "qKfzMax:'4000'," +
					                          "qLkwMax:'4000'," +
					                          "vKfzMax:'254'," +
					                          "vLkwMax:'254'," +
					                          "vPkwMax:'254'," +
					                          "vgKfzMax:'254'," +
					                          "bMax:'100'" +
					                          "}"
			);
		}
		startTestCase("DUA52.csv", _testFs, _testFs, _ddIn, _ddOut, new Pl52Layout(){
			@Override
			public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
				return null;
			}


			@Override
			public Collection<String> getIgnored() {
				// Soll-Werte stimmen nicht mehr aufgrund anderer Parametrierung
				return ImmutableSet.of("T", "Wert", "Implausibel", "Index", "NichtErfasst", "WertMaxLogisch");
			}
		});
	}

	@Test
	public void testDua53() throws Exception {
		for(SystemObject obj : _testFs) {
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch2",
			                          "{" +
					                          "ErzeugeBetriebsmeldungPrüfungTLS:'Nein'," +
					                          "ErzeugeBetriebsmeldungPrüfungVerkehr:'Nein'," +
					                          "VerhaltenGrenzwertPrüfung:'Keine Prüfung'," +
					                          "PrüfintervallAusfallHäufigkeit:'10 Minuten'," +
					                          "MaxAusfallZeitProTag:'95 Minuten'," +
					                          "BezugszeitraumVertrauensbereich:'50 Minuten'," +
					                          "PrüfintervallVertrauensbereich:'10 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichEin:'15 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichAus:'13 Minuten'," +
					                          "vKfzGrenz:'100 km/h'," +
					                          "bGrenz:'80 %'," +
					                          "qKfzMax:'4000'," +
					                          "qLkwMax:'4000'," +
					                          "vKfzMax:'254'," +
					                          "vLkwMax:'254'," +
					                          "vPkwMax:'254'," +
					                          "vgKfzMax:'254'," +
					                          "bMax:'100'" +
					                          "}"
			);
		}
		startTestCase("DUA53.csv", _testFs, _testFs, _ddIn, _ddOut, new PlLongColumnLayout3());
	}
	
	@Test
	public void testDua53B() throws Exception {
		for(SystemObject obj : _testFs) {
			fakeParamApp.publishParam(obj.getPid(), "atg.verkehrsDatenKurzZeitIntervallPlausibilitätsPrüfungLogisch2",
			                          "{" +
					                          "ErzeugeBetriebsmeldungPrüfungTLS:'Nein'," +
					                          "ErzeugeBetriebsmeldungPrüfungVerkehr:'Nein'," +
					                          "VerhaltenGrenzwertPrüfung:'Keine Prüfung'," +
					                          "PrüfintervallAusfallHäufigkeit:'10 Minuten'," +
					                          "MaxAusfallZeitProTag:'95 Minuten'," +
					                          "BezugszeitraumVertrauensbereich:'50 Minuten'," +
					                          "PrüfintervallVertrauensbereich:'10 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichEin:'0 Minuten'," +
					                          "MaxAusfallzeitVertrauensbereichAus:'0 Minuten'," +
					                          "vKfzGrenz:'100 km/h'," +
					                          "bGrenz:'80 %'," +
					                          "qKfzMax:'4000'," +
					                          "qLkwMax:'4000'," +
					                          "vKfzMax:'254'," +
					                          "vLkwMax:'254'," +
					                          "vPkwMax:'254'," +
					                          "vgKfzMax:'254'," +
					                          "bMax:'100'" +
					                          "}"
			);
		}
		startTestCase("DUA53.csv", _testFs, _testFs, _ddIn, _ddOut, new PlLongColumnLayout3(){
			@Override
			public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
				return null;
			}


			@Override
			public Collection<String> getIgnored() {
				// Soll-Werte stimmen nicht mehr aufgrund anderer Parametrierung
				return ImmutableSet.of("T", "Wert", "Implausibel", "NichtErfasst", "WertMaxLogisch");
			}
		});
	}


	@Override
	public void sendData(final ResultData... resultDatas) throws SendSubscriptionNotConfirmed {
		_pruefungLogischLVE.update(resultDatas);
	}

	private static class PlColumnLayout extends ColumnLayout {
		@Override
		public int getColumnCount(final boolean in) {
			return 1;
		}

		@Override
		public void setValues(final SystemObject testObject, final Data item, final List<String> row, final int realCol, final String type, final boolean in) {
			item.getTextValue("Wert").setText(row.get(realCol));
			if(!in) {
				item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol + 7));
				String percent = row.get(realCol + 14);
				if(percent.endsWith("%")) {
					percent = percent.substring(0, percent.length() - 1);
				}
				percent = percent.replace(',', '.');
				item.getItem("Güte").getUnscaledValue("Index").set(Double.parseDouble(percent) * 100);
			}
			else {
				item.getItem("Güte").getUnscaledValue("Index").set(10000);
			}
		}

		@Override
		public Collection<String> getIgnored() {
			return ImmutableSet.of("T", "b", "tNetto", "sKfz", "vgKfz", "NichtErfasst", "WertMaxLogisch");
		}

		@Override
		public boolean groupingEnabled() {
			return false;
		}
	}

	private static class PlLongColumnLayout extends PlColumnLayout {
		@Override
		public Collection<String> getIgnored() {
			return ImmutableSet.of("T", "NichtErfasst", "WertMaxLogisch");
		}

		@Override
		public void setValues(final SystemObject testObject, final Data item, final List<String> row, final int realCol, final String type, final boolean in) {
			item.getTextValue("Wert").setText(row.get(realCol));
			if(!in) {
				item.getItem("Status").getItem("PlLogisch").getTextValue("WertMaxLogisch").setText(row.get(realCol + 13));
				item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol + 24));
				String percent = row.get(realCol + 35);
				if(percent.endsWith("%")) {
					percent = percent.substring(0, percent.length() - 1);
				}
				percent = percent.replace(',', '.');
				item.getItem("Güte").getUnscaledValue("Index").set(Double.parseDouble(percent) * 100);
			}
			else {
				item.getItem("Güte").getUnscaledValue("Index").set(10000);
				item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol).equals("-2") ? "Ja" : "Nein");
			}
		}

		@Override
		public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
			if(fullRow.get(15).equals("X")) return "[DUA-PP-VGW01]";
			if(fullRow.get(16).equals("X")) return "[DUA-PP-VGW02]";
			if(fullRow.get(17).equals("X")) return "[DUA-PP-VGW03]";
			if(fullRow.get(18).equals("X")) return "[DUA-PP-VGW04]";
			if(fullRow.get(19).equals("X")) return "[DUA-PP-VGW05]";
			if(fullRow.get(20).equals("X")) return "[DUA-PP-VGW06]";
			if(fullRow.get(21).equals("X")) return "[DUA-PP-VGW07]";
			if(fullRow.get(22).equals("X")) return "[DUA-PP-VGW08]";
			return null;
		}

		@Override
		public boolean groupingEnabled() {
			return false;
		}
	}

	private static class PlLongColumnLayout2 extends PlColumnLayout {
		@Override
		public Collection<String> getIgnored() {
			return ImmutableSet.of("T", "NichtErfasst", "WertMaxLogisch");
		}

		@Override
		public void setValues(final SystemObject testObject, final Data item, final List<String> row, final int realCol, final String type, final boolean in) {
			item.getTextValue("Wert").setText(row.get(realCol));
			if(!in) {
				item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol + 11));
				String percent = row.get(realCol + 22);
				if(percent.endsWith("%")) {
					percent = percent.substring(0, percent.length() - 1);
				}
				percent = percent.replace(',', '.');
				item.getItem("Güte").getUnscaledValue("Index").set(Double.parseDouble(percent) * 100);
			}
			else {
				item.getItem("Güte").getUnscaledValue("Index").set(10000);
				item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol).equals("-2") ? "Ja" : "Nein");
			}
		}

		@Override
		public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
			String s = fullRow.get(15);
			if(s.equals("keine Betriebsmeldung") || s.isEmpty()) return null;
			LinkedHashSet<String> set = new LinkedHashSet<>(Splitter.on(",").trimResults().splitToList(s.replaceAll("^Betriebsmeldung(en)? ", "")));
			set = adjustAttributes(set);
			if(set.isEmpty()) return null;
			return Joiner.on(" und ").join(set) + " überschritten";
		}

		public LinkedHashSet<String> adjustAttributes(final LinkedHashSet<String> set) {
			return set;
		}

		@Override
		public boolean groupingEnabled() {
			return false;
		}
	}


	private static class PlLongColumnLayout3 extends PlColumnLayout {
		@Override
		public Collection<String> getIgnored() {
			return ImmutableSet.of("T", "NichtErfasst", "WertMaxLogisch");
		}

		@Override
		public void setValues(final SystemObject testObject, final Data item, final List<String> row, final int realCol, final String type, final boolean in) {
			item.getTextValue("Wert").setText(row.get(realCol));
			if(!in) {
				item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol + 11));
				if(row.get(realCol).equals("-2")) {
					// Hack
					item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText("Ja");
				}
				String percent = row.get(realCol + 22);
				if(percent.endsWith("%")) {
					percent = percent.substring(0, percent.length() - 1);
				}
				percent = percent.replace(',', '.');
				item.getItem("Güte").getUnscaledValue("Index").set(Double.parseDouble(percent) * 100);
			}
			else {
				item.getItem("Güte").getUnscaledValue("Index").set(10000);
				item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol).equals("-2") ? "Ja" : "Nein");
			}
		}

		@Override
		public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
			String s = fullRow.get(18);
			if(s.equals("-") || s.isEmpty()) return null;
			if(s.equals("Gutmeldung")) return "[DUA-PP-VB02]";
			return "[DUA-PP-VB01]";
		}

		@Override
		public boolean groupingEnabled() {
			return false;
		}
	}

	private static class Pl52Layout extends PlColumnLayout {
		@Override
		public Collection<String> getIgnored() {
			return ImmutableSet.of("T", "b", "tNetto", "sKfz", "NichtErfasst", "WertMaxLogisch", "qKfz", "vKfz", "qLkw", "vLkw", "qPkw", "vPkw", "vgKfz");
		}

		@Override
		public void setValues(final SystemObject testObject, final Data item, final List<String> row, final int realCol, final String type, final boolean in) {
			if(in) {
				item.getTextValue("Wert").setText(row.get(realCol));
				item.getItem("Güte").getUnscaledValue("Index").set(10000);
				item.getItem("Status").getItem("MessWertErsetzung").getTextValue("Implausibel").setText(row.get(realCol).equals("-2") ? "Ja" : "Nein");
			}
		}

		@Override
		public boolean groupingEnabled() {
			return false;
		}


		@Override
		public String getExpectedMessageText(final List<String> fullRow, final DataDescription dataDescription, final List<List<String>> header, final long timestamp) {
			String s = fullRow.get(15);
			if(s.equals("-") || s.isEmpty()) return null;
			return "[DUA-PP-AH01]";
		}
	}
}
