/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

/**
 *
 * @author artur
 * lista możliwych scenariuszy "użycia" dworca
 */
public enum Scenario {
    LEAVING_PASSENGER_WITH_TICKET, LEAVING_PASSENGER_WITHOUT_TICKET,
    ARRIVING_PASSENGER, ARRIVING_PASSENGER_WITH_COMPLAINTS,
    COMPANION_FOR_LEAVING_PASSENGER, COMPANION_FOR_ARRIVING_PASSENGER,
    PERSON_BUYING_TICKETS, CURIOUS_PERSON, CURIOUS_PERSON_BUYING_TICKETS
};