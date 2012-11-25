/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation;

import desmoj.core.simulator.Model;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;
import railwaystation.infrastructure.CashDeskRegion;
import railwaystation.infrastructure.Infrastructure;
import railwaystation.infrastructure.Region;
import railwaystation.infrastructure.ServingRegion;
import railwaystation.visualisation.MainFrame;

/**
 *
 * @author artur
 */
public class RailwayStation extends Model {
    protected Infrastructure infrastructure;

    public RailwayStation() {
        super(null, "railway-station", true, true);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        RailwayStation station = new RailwayStation();
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame frame = new MainFrame();
                JLabel label = frame.addTextLabel("Testowy napis", Color.BLACK, Color.RED);
                frame.animateTextLabel(label);
                frame.setVisible(true);
                JPanel platform1 = frame.addPlatform(1);
            }
        });
    }

    protected void buildInfrastructure() {
        infrastructure = new Infrastructure(this);
        infrastructure.createPlatforms(Infrastructure.PLATFORM_COUNT);

        Region hall = infrastructure.createRegion("hall", Infrastructure.HALL_CAPACITY);
        infrastructure.setEntryRegion(hall);

        ServingRegion informationDeskRegion = infrastructure.createServingRegion("information", Infrastructure.INFO_DESKS_CAPACITY, Infrastructure.INFO_DESK_COUNT);
        infrastructure.setInformationDeskRegion(informationDeskRegion);

        CashDeskRegion cashDeskRegion = infrastructure.createCashDeskRegion("cash-desks", Infrastructure.CASH_DESKS_CAPACITY, Infrastructure.CASH_DESK_COUNT);
        infrastructure.setCashDeskRegion(cashDeskRegion);

        Region waitingRoom = infrastructure.createRegion("waiting-room", Infrastructure.WAITING_ROOM_CAPACITY);
        infrastructure.setWaitingRoom(waitingRoom);

        // polaczenia miedzy elementami infrastruktury dworcowej
        infrastructure.bindRegions(hall, waitingRoom);
        infrastructure.bindWithPlatforms(hall);
        infrastructure.bindWithPlatforms(waitingRoom);
        infrastructure.bindRegions(hall, informationDeskRegion);
        infrastructure.bindRegions(hall, cashDeskRegion);
    }

    @Override
    public String description() {
        return "TODO! description of the simulation.";
    }

    @Override
    public void doInitialSchedules() {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init() {
        buildInfrastructure();
        // throw new UnsupportedOperationException("Not supported yet.");
    }
}