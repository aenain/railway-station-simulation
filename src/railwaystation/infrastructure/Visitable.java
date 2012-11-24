/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import railwaystation.person.Person;

/**
 *
 * @author artur
 */
public interface Visitable {
    // metoda wywoływana, gdy dany obiekt jest opuszczany przez człowieka
    public void personLeaves(Person person);
    // metoda wywoływana, gdy człowiek wchodzi do danego obiektu (sprawdza, czy może wejść)
    public boolean personEnters(Person person);
}
