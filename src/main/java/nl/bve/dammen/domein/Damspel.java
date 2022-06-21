package nl.bve.dammen.domein;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * @author Bart v. Eijkelenburg 
 * 
 * Klasse Damspel is de representatie van een Dambord. De klasse houdt
 * voor alle velden de status bij en bepaalt of een zet of slag uitgevoerd
 * kan worden.
 * 
 * Een Damspel-object kan via de constructor gemaakt worden en is dan direct
 * speelklaar. Vervolgens zijn de volgende methoden nodig om het spel te 
 * be�nvloeden:
 * 
 *  public boolean isVeldSpeelbaar(int veldId)
 *  public boolean isZetMogelijk(int vanVeldId, int naarVeldId)
 *  public boolean doeZet(int vanVeldId, int naarVeldId)
 *  
 * De methode isVeldSpeelbaar kan gebruikt worden om bijvoorbeeld te bepalen
 * of op een GUI een veld geselecteerd kan worden om daarvandaan een zet of 
 * slag te beginnen. Het resultaat is true of false; 
 *  
 * De methode isZetMogelijk kan gebruikt worden om te controleren of een zet
 * of slag is toegestaan zonder de zet of slag ook daadwerkelijk uit te voeren
 * Zodoende kan een gebruiker zijn keuze nog heroverwegen. Het resultaat is true
 * of false.
 *  
 * Methode doeZet is nodig om een zet of slag daadwerkelijk uit te voeren. Het
 * spel zal als deze slag of zet is toegestaan, de interne status van het speel-
 * bord bijwerken.  
 *  
 *  
 * Naast deze methoden zijn er nog:
 *  
 *  public void reset()
 *  public Veld[] getVeldStatus(int veldId) 
 *  public String getMelding() 
 *  public StringProperty getMeldingProperty()
 *  public String getSpeler()
 *  public StringProperty getSpelerProperty()
 *  public Damspel clone() 
 *  public String toString()
 * 
 * De reset() methode kan ten allen tijde worden aangeroepen en zal het bord
 * volledig reseten.
 * 
 * Methode getVeldStatus levert de status voor het veld met id == veldId op als string.
 * Deze status kan zijn: LEEG, WIT, ZWART, ZWARTDAM, WITDAM of NIETSPEELBAAR. Hiermee 
 * kan de GUI geupdate worden.
 * 
 * Methode getMelding kan gebruikt worden om na een zet eventuele berichten in de
 * GUI te tonen. Bijvoorbeeld met informatie waarom een zet niet lukt.
 * 
 * Methode getMeldingProperty levert een property waaraan in JavaFX een listener
 * gekoppeld kan worden. Zodoende hoeft niet steeds gecontroleerd te worden of er
 * een nieuw bericht is, maar kan het bericht automatisch in een Label verschijnen.
 * 
 * Methode getSpeler levert de naam van de HUIDIGE speler op (WIT of ZWART);
 * 
 * Methode getSpelerProperty levert een property waaraan in JavaFX een listener
 * gekoppeld kan worden. Op die manier kan het speelbord bijvoorbeeld geroteerd
 * worden als de beurt wijzigt, of kan de naam van de speler die aan de beurt is
 * automatisch in een Label geplaatst worden (middels een listener).
 * 
 * Methode clone kan gebruikt worden om een deep-copy van het damspel op te vragen.
 * Een eventuele computerspeler kan deze methode gebruiken om zetten op het bord 
 * uit te proberen en mogelijke zetten te overwegen.
 * 
 * Methode toString levert een tekstuele representatie van het dambord op. Deze
 * kan gebruikt worden voor testdoeleinden.
 *  
 */
public class Damspel {
	private enum Veld { WIT, ZWART, WITDAM, ZWARTDAM, LEEG, NIETSPEELBAAR }
	private enum Richting { NOORDWEST, NOORDOOST, ZUIDOOST, ZUIDWEST }
	private enum Speler { WIT, ZWART, WIT_WINT, ZWART_WINT }

	private Veld[] bord = new Veld[100];
	private Speler speler = Speler.ZWART;
	private final StringProperty meldingProperty = new SimpleStringProperty();
	private final StringProperty spelerProperty = new SimpleStringProperty();
	
	/**
	 * Constructor maakt een volledig ingesteld nieuw dambord.
	 */
	public Damspel() {
		reset();
	}
	
	@Override
	public Damspel clone() {
		Damspel clone = new Damspel();
	
		clone.bord = new Veld[bord.length];
		for (int i=0; i < bord.length; i++) {
			clone.bord[i] = bord[i];
		}
		
		clone.speler = speler;
		clone.meldingProperty.setValue(meldingProperty.getValue());
		clone.spelerProperty.setValue(spelerProperty.getValue());
		
		return clone;
	}
	
	/**
	 * De reset() methode kan ten allen tijde worden aangeroepen en zal het bord
	 * volledig reseten.
	 */
	public void reset() {
		speler = Speler.WIT;
		spelerProperty.setValue(speler.name());
		
		// 4 rijen met witte stenen (rij 0 t/m 3)
		for (int i=0; i <= 3; i++) {
			for (int j=i%2; j <= 9; j+=2) {
				bord[i*10+j] = Veld.ZWART;
			}
		}
		
		// 2 rijen zonder stenen (rij 4 en 5)
		for (int i=4; i <= 5; i++) {
			for (int j=i%2; j <= 9; j+=2) {
				bord[i*10+j] = Veld.LEEG;
			}
		}
		
		// 4 rijen met zwarte stenen (rij 6 t/m 9)
		for (int i=6; i <= 9; i++) {
			for (int j=i%2; j <= 9; j+=2) {
				bord[i*10+j] = Veld.WIT;
			}
		}
		
		// 50 onspeelbare posities
		for (int i=0; i <= 9; i++) {
			for (int j=(i+1)%2; j <= 9; j+=2) {
				bord[i*10+j] = Veld.NIETSPEELBAAR;
			}
		}		
	}

	/**
	 * Methode doeZet controleert of een gegeven zet mogelijk is van 'vanVeldId'
	 * naar 'naarVeldId'. Hiervoor wordt de publieke methode isZetMogelijk gebruikt.
	 * 
	 * Als het een slag is zullen stenen van de tegenstander van het bord gehaald 
	 * worden. Het betreffende veld wordt dan 'LEEG'. 
	 * 
	 * Wanneer het een slag is, kan het zijn dat de speler nog meer stenen kan slaan vanaf
	 * het zojuist verkregen veld op positie 'naarVeldId'. In dat geval wijzigt de beurt 
	 * NIET. In alle andere gevallen wel. Daarbij zal de StringProperty 'spelerProperty'
	 * geupdatet worden.
	 * 
	 * @param vanVeldId - het veld waar de zet of slag begint
	 * @param naarVeldId - het veld waar de zet of slag eindigt
	 * @return {@link Boolean}
	 */
	public boolean doeZet(int vanVeldId, int naarVeldId) {
		if (isZetMogelijk(vanVeldId, naarVeldId)) {
			boolean isSlag = !bepaalMogelijkeSlagenVoorVeld(vanVeldId).isEmpty();
			boolean spelersWissel = true;
			
			bord[naarVeldId] = bord[vanVeldId];
			bord[vanVeldId] = Veld.LEEG;
			
			if (isSlag) {
				verwijderTegenstanderTussen(vanVeldId, naarVeldId);
				
				if (!bepaalMogelijkeSlagenVoorVeld(naarVeldId).isEmpty()) {
					spelersWissel = false;
				}
			}
			
			int veldRij = naarVeldId / 10;
			if (veldRij == 9 && speler == Speler.ZWART) bord[naarVeldId] = Veld.ZWARTDAM;
			if (veldRij == 0 && speler == Speler.WIT) bord[naarVeldId] = Veld.WITDAM;
			
			if (spelersWissel) {
				wisselVanSpeler();
			}
			
			return true;
		}
		
		return false;
	}
	
	private void wisselVanSpeler() {
		speler = (speler == Speler.WIT) ? Speler.ZWART : Speler.WIT;
		
		if (getAlleSlagPositiesVoorSpeler().isEmpty() && getAlleZetPositiesVoorSpeler().isEmpty()) {
			if (speler == Speler.WIT) speler = Speler.ZWART_WINT;
			if (speler == Speler.ZWART) speler = Speler.WIT_WINT;
		}
		
		spelerProperty.setValue(speler.name());
	}
	
	private void verwijderTegenstanderTussen(int vanVeldId, int naarVeldId) {
		int verschil = Math.abs(vanVeldId - naarVeldId);
				
		if (verschil >= 18 && verschil <= 22) /* normale slag */ {
			int tegenstanderPositie = (vanVeldId + naarVeldId) / 2;
			bord[tegenstanderPositie] = Veld.LEEG;
		} else if (verschil > 22) /* damslag */ {
			boolean isNoordSlag = (vanVeldId - naarVeldId) < 0;
			
			boolean isNoordWestSlag = isNoordSlag && (verschil % 11 == 0);  
			boolean isNoordOostSlag = isNoordSlag && (verschil % 9 == 0);
			boolean isZuidOostSlag = !isNoordSlag && (verschil % 11 == 0);
			boolean isZuidWestSlag = !isNoordSlag && (verschil % 9 == 0); 
			
			int tegenstanderPositie = -1;
			if (isNoordWestSlag) tegenstanderPositie = naarVeldId-11;
			if (isNoordOostSlag) tegenstanderPositie = naarVeldId-9;
			if (isZuidOostSlag) tegenstanderPositie = naarVeldId+11;
			if (isZuidWestSlag) tegenstanderPositie = naarVeldId+9;
			
			if (tegenstanderPositie != -1) {
				bord[tegenstanderPositie] = Veld.LEEG;
			}
		}
	}
	
	
	/**
	 * De methode isVeldSpeelbaar kan gebruikt worden om bijvoorbeeld te bepalen
	 * of op een GUI een veld geselecteerd kan worden om daarvandaan een zet of
	 * slag te beginnen. Het resultaat is true of false; 
	 * 
	 * @param veldId
	 * @return {@link Boolean}
	 */
	public boolean isVeldSpeelbaar(int veldId) {
		if (speler == Speler.WIT_WINT || speler == Speler.ZWART_WINT) {
			meldingProperty.setValue("Het spel is al afgelopen!"); 
			return false; 
		}
		
		if (bord[veldId] == Veld.NIETSPEELBAAR) { 
			meldingProperty.setValue("Alleen op de donkere vlakken kan gespeeld worden!"); 
			return false; 
		} 
		
		if (bord[veldId] == Veld.LEEG) {
			meldingProperty.setValue("U kunt niet vanaf een leeg veld spelen!"); 
			return false; 
		}
		
		boolean isGeenEigenVeld;
		if (speler == Speler.WIT) isGeenEigenVeld = bord[veldId] == Veld.ZWART|| bord[veldId] == Veld.ZWARTDAM;
		else isGeenEigenVeld = bord[veldId] == Veld.WIT|| bord[veldId] == Veld.WITDAM;
		
		if (isGeenEigenVeld) {
			meldingProperty.setValue("U mag geen stenen van de tegenstander verplaatsen!");
			return false;
		}
		
		List<Integer> alleSlagPosities = getAlleSlagPositiesVoorSpeler();
		if (!alleSlagPosities.isEmpty() && !alleSlagPosities.contains(veldId)) {
			meldingProperty.setValue("Slaan gaat voor een zet!");
			return false;
		}
		
		List<Integer> slagenVoorVeld = bepaalMogelijkeSlagenVoorVeld(veldId);
		List<Integer> zettenVoorVeld = bepaalMogelijkeZettenVoorVeld(veldId);

		if (slagenVoorVeld.isEmpty() && zettenVoorVeld.isEmpty()) {
			meldingProperty.setValue("Deze steen kan niet verplaatsen!");
			return false;
		} else {
			meldingProperty.setValue("Ok!");
			return true;
		}
	}
	
	/**
	 * De methode isZetMogelijk kan gebruikt worden om te controleren of een zet
	 * of slag is toegestaan zonder de zet of slag ook daadwerkelijk uit te voeren
	 * Zodoende kan een gebruiker zijn keuze nog heroverwegen. Het resultaat is true
	 * of false.
	 * 
	 * @param vanVeldId
	 * @param naarVeldId
	 * @return {@link Boolean}
	 */
	public boolean isZetMogelijk(int vanVeldId, int naarVeldId) {
		if (!isVeldSpeelbaar(vanVeldId)) {
			// bericht is al ge-set in andere methode
			return false;
		}
		
		List<Integer> slagPosities = bepaalMogelijkeSlagenVoorVeld(vanVeldId);
		if (!slagPosities.isEmpty() && !slagPosities.contains(naarVeldId)) {
			meldingProperty.setValue("Slaan gaat voor een zet!");
			return false;
		}
		
		List<Integer> zetPosities = bepaalMogelijkeZettenVoorVeld(vanVeldId);
		if (slagPosities.contains(naarVeldId) || zetPosities.contains(naarVeldId)) {
			meldingProperty.setValue("Ok!");
			return true;
		}
		
		meldingProperty.setValue("U kunt niet spelen naar dit veld!");
		return false;
	}
	
	public List<Integer> getAlleSlagPositiesVoorSpeler() {
		List<Integer> alleSlagPosities = new ArrayList<Integer>();
		
		for (int i=0; i < bord.length; i++) {
			if (!isVeldVanTegenstander(i)) {
				List<Integer> slagenVoorVeld = bepaalMogelijkeSlagenVoorVeld(i);
				if (!slagenVoorVeld.isEmpty()) {
					alleSlagPosities.add(i);
				}
			}
		}
		
		return alleSlagPosities;
	}
	
	public List<Integer> getAlleZetPositiesVoorSpeler() {
		List<Integer> alleZetPosities = new ArrayList<Integer>();
		
		for (int i=0; i < bord.length; i++) {
			if (!isVeldVanTegenstander(i)) {
				List<Integer> zettenVoorVeld = bepaalMogelijkeZettenVoorVeld(i);
				if (!zettenVoorVeld.isEmpty()) {
					alleZetPosities.add(i);
				}
			}
		}
		
		return alleZetPosities;
	}

	public List<Integer> bepaalMogelijkeZettenVoorVeld(int veldId) {
		List<Integer> mogelijkeZetten = new ArrayList<Integer>();

		if (bord[veldId] != Veld.LEEG && bord[veldId] != Veld.NIETSPEELBAAR) {
			for (Richting richting : Richting.values()) {
				mogelijkeZetten.addAll(zijnZettenMogelijkInRichting(richting, veldId));
			}
		}
		
		return mogelijkeZetten;
	}
	
	private List<Integer> zijnZettenMogelijkInRichting(Richting richting, final int veldId) {
		List<Integer> mogelijkeZetten = new ArrayList<Integer>();
		
		if (isEigenVeld(veldId)) {
			BiPredicate<Integer, Integer> zijnRijEnKolomVerGenoegVanBordrand = null;
			
			int VELDDT = 0, KOLOMDT = 0, RIJDT = 0, veldKolom = veldId % 10, veldRij = veldId / 10;
			if (richting == Richting.NOORDWEST) { KOLOMDT = -1; RIJDT = -1; VELDDT = -11; zijnRijEnKolomVerGenoegVanBordrand = (kolom, rij) -> kolom > 0 && rij > 0; } 
			if (richting == Richting.NOORDOOST) { KOLOMDT = +1; RIJDT = -1; VELDDT = -9;  zijnRijEnKolomVerGenoegVanBordrand = (kolom, rij) -> kolom < 9 && rij > 0; }
			if (richting == Richting.ZUIDOOST)  { KOLOMDT = +1; RIJDT = +1; VELDDT = +11; zijnRijEnKolomVerGenoegVanBordrand = (kolom, rij) -> kolom < 9 && rij < 9; }
			if (richting == Richting.ZUIDWEST)  { KOLOMDT = -1; RIJDT = +1; VELDDT = +9;  zijnRijEnKolomVerGenoegVanBordrand = (kolom, rij) -> kolom > 0 && rij < 9; }
			
			int zetNaarPos = veldId + VELDDT;
			
			while (zijnRijEnKolomVerGenoegVanBordrand.test(veldKolom, veldRij) && bord[zetNaarPos] == Veld.LEEG) {
				if (bord[veldId] == Veld.WIT || bord[veldId] == Veld.ZWART) {
					if (speler == Speler.WIT)
						if (richting == Richting.NOORDWEST || richting == Richting.NOORDOOST) mogelijkeZetten.add(zetNaarPos);
					
					if (speler == Speler.ZWART)
						if (richting == Richting.ZUIDOOST || richting == Richting.ZUIDWEST) mogelijkeZetten.add(zetNaarPos);
	
					break;
				}
				
				mogelijkeZetten.add(zetNaarPos);
				
				veldKolom += KOLOMDT;
				veldRij += RIJDT;
				zetNaarPos += VELDDT;
			}
		}
		
		return mogelijkeZetten;
	}

	public List<Integer> bepaalMogelijkeSlagenVoorVeld(int veldId) {
		List<Integer> mogelijkeSlagen = new ArrayList<Integer>();

		if (bord[veldId] != Veld.LEEG && bord[veldId] != Veld.NIETSPEELBAAR) {
			for (Richting richting : Richting.values()) {
				Integer slag = isSlagMogelijkInRichting(richting, veldId);
				if (slag != null) mogelijkeSlagen.add(slag);
			}
		}
		
		return mogelijkeSlagen;
	}
	
	private Integer isSlagMogelijkInRichting(Richting richting, final int veldId) {
		if (isEigenVeld(veldId)) {
			BiPredicate<Integer, Integer> zijnRijEnKolomVerGenoegVanBordrand = null; 
			
			int VELDDT = 0, KOLOMDT = 0, RIJDT = 0, veldKolom = veldId % 10, veldRij = veldId / 10; 
			if (richting == Richting.NOORDWEST) { KOLOMDT = -1; RIJDT = -1; VELDDT = -11; zijnRijEnKolomVerGenoegVanBordrand = (kolom, rij) -> kolom > 1 && rij > 1; } 
			if (richting == Richting.NOORDOOST) { KOLOMDT = +1; RIJDT = -1; VELDDT = -9;  zijnRijEnKolomVerGenoegVanBordrand = (kolom, rij) -> kolom < 8 && rij > 1; }
			if (richting == Richting.ZUIDOOST)  { KOLOMDT = +1; RIJDT = +1; VELDDT = +11; zijnRijEnKolomVerGenoegVanBordrand = (kolom, rij) -> kolom < 8 && rij < 8; }
			if (richting == Richting.ZUIDWEST)  { KOLOMDT = -1; RIJDT = +1; VELDDT = +9;  zijnRijEnKolomVerGenoegVanBordrand = (kolom, rij) -> kolom > 1 && rij < 8; }
			
			int geslagenPos = veldId + VELDDT*1;
			int slagNaarPos = veldId + VELDDT*2; 

			while (zijnRijEnKolomVerGenoegVanBordrand.test(veldKolom, veldRij)) {
				if (isVeldVanTegenstander(geslagenPos) && bord[slagNaarPos] == Veld.LEEG) {
					return slagNaarPos;
				} else {
					if (bord[veldId] == Veld.WIT || bord[veldId] == Veld.ZWART) break;
					if (bord[geslagenPos] != Veld.LEEG && bord[slagNaarPos] != Veld.LEEG) break;
				}
				
				slagNaarPos += VELDDT;
				geslagenPos += VELDDT;
				veldKolom += KOLOMDT;
				veldRij += RIJDT;
			}
		}

		return null;
	}
	
	private boolean isVeldVanTegenstander(int veldId) {
		if (speler == Speler.WIT) {
			return bord[veldId] == Veld.ZWART || bord[veldId] == Veld.ZWARTDAM;
		} else {
			return bord[veldId] == Veld.WIT || bord[veldId] == Veld.WITDAM;
		}
	}
	
	private boolean isEigenVeld(int veldId) {
		if (speler == Speler.WIT) {
			return bord[veldId] == Veld.WIT || bord[veldId] == Veld.WITDAM;
		} else {
			return bord[veldId] == Veld.ZWART || bord[veldId] == Veld.ZWARTDAM;
		}
	}
	
	/**
	 * Methode getVeldStatus levert voor het gegeven veldId de status op van dat veld.
	 * De status is een String, en kan zijn: "LEEG", "WIT", "ZWART", "ZWARTDAM", "WITDAM" 
	 * of "NIETSPEELBAAR". Hiermee kan de GUI geupdate worden.
	 * 
	 * @return {@link String}
	 */
	public String getVeldStatus(int veldId) {
		return bord[veldId].name();
	}
	
	/**
	 * Methode getMelding kan gebruikt worden om na een zet eventuele berichten in de
	 * GUI te tonen. Bijvoorbeeld met informatie waarom een zet niet lukt.
	 * 
	 * @return {@link String}
	 */
	public String getMelding() {
		return meldingProperty.getValue();
	}
	
	/**
	 * Methode getMeldingProperty levert een property waaraan in JavaFX een listener
	 * gekoppeld kan worden. Zodoende hoeft niet steeds gecontroleerd te worden of er
	 * een nieuw bericht is, maar kan het bericht automatisch in een Label verschijnen.
	 * 
	 * @return {@link StringProperty}
	 */
	public StringProperty getMeldingProperty() {
		return meldingProperty;
	}
	
	/**
	 * Methode getSpeler levert de naam van de HUIDIGE speler op (WIT of ZWART);
	 * 
	 * @return {@link String}
	 */
	public String getSpeler() {
		return spelerProperty.getValue();
	}
	
	/**
	 * Methode getSpelerProperty levert een property waaraan in JavaFX een listener
	 * gekoppeld kan worden. Op die manier kan het speelbord bijvoorbeeld geroteerd
	 * worden als de beurt wijzigt, of kan de naam van de speler die aan de beurt is
	 * automatisch in een Label geplaatst worden (middels een listener).
	 * 
	 * @return {@link StringProperty}
	 */
	public StringProperty getSpelerProperty() {
		return spelerProperty;
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int i=0; i < bord.length; i++) {
			if (i % 10 == 0) result.append("\n");

			switch (bord[i]) {
			case ZWART: result.append(" Z "); break;
			case WIT: result.append(" W "); break;
			case WITDAM: result.append("WD "); break;
			case ZWARTDAM: result.append("ZD "); break;
			case LEEG: result.append(" . "); break;
			case NIETSPEELBAAR: result.append("   "); break;
			}
		}	
		
		return result.toString();
	}
		
}