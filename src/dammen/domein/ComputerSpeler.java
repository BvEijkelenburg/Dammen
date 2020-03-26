package dammen.domein;

public class ComputerSpeler {
	private boolean enabled = false;
	
	public ComputerSpeler(Damspel spel) {
		spel.getSpelerProperty().addListener(e -> {
			if (enabled) {
				System.out.println("change!");
				if (spel.getSpeler().equals("ZWART")) {
					while (spel.getSpeler().equals("ZWART")) {
						
						System.out.println("playing");
						Damspel clone = ((Damspel)spel.clone());
						
						if (!spel.getAlleSlagPositiesVoorSpeler().isEmpty()) {
							int vanVeld = spel.getAlleSlagPositiesVoorSpeler().get(0);
							int naarVeld = spel.bepaalMogelijkeSlagenVoorVeld(vanVeld).get(0);
							
							clone.doeZet(vanVeld, naarVeld);
							spel.doeZet(vanVeld, naarVeld);
							
						} else {
							int vanVeld = spel.getAlleZetPositiesVoorSpeler().get(0);
							int naarVeld = spel.bepaalMogelijkeZettenVoorVeld(vanVeld).get(0);
							
							clone.doeZet(vanVeld, naarVeld);
							spel.doeZet(vanVeld, naarVeld);
						} 
						
						if (clone.getAlleSlagPositiesVoorSpeler().isEmpty()) {
							System.out.println("HAHA, jij kan niet slaan!");
						} else {
							System.out.println("TSS, verkeerde zet...");
						}
					}
				} else if (spel.getSpeler().equals("WIT_WINT")) {
					System.out.println("Gefeliciteerd!");
				} else if (spel.getSpeler().equals("ZWART_WINT")) {
					System.out.println("GNA GNA GNA!");
				}
			}
		});
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
