package pharmacy;

import javax.swing.SwingUtilities;
public class PharmacyInventory {

    
    public static void main(String[] args) {
       SwingUtilities.invokeLater(() -> {
            new Login().setVisible(true);
        }); 
    }
    
}
