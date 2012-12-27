/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.infrastructure;

import java.util.LinkedList;
import railwaystation.person.Person;

/**
 *
 * @author artur
 */
public interface Visitable {
    // metoda wywoływana, gdy dany obiekt jest opuszczany przez człowieka
    public void personLeaves(Person person);
    public void peopleLeave(LinkedList<Person> people);
    // metoda wywoływana, gdy człowiek wchodzi do danego obiektu (zakładając, że może wejść)
    public void personEnters(Person person);
    public void peopleEnter(LinkedList<Person> people);
    public boolean canPersonEnter();
    public boolean canPeopleEnter(int count);
}
