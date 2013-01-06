/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

import java.util.LinkedList;
import railwaystation.infrastructure.Infrastructure;
import railwaystation.infrastructure.Path;
import railwaystation.infrastructure.Platform;
import railwaystation.infrastructure.Region;
import railwaystation.infrastructure.ServingRegion;

/**
 *
 * @author artur
 */
public class Activity {

    public static enum Type {

        WAIT_IN_WAITING_ROOM,
        WAIT_IN_HALL,
        WAIT_ON_PLATFORM,
        ENTER_TRAIN,
        LEAVE_TRAIN,
        ENTER_STATION,
        LEAVE_STATION,
        BUY_TICKET,
        COMPLAIN,
        GET_INFO,
        FOLLOW_PASSENGER,
        BIND_COMPANIONS,
        UNBIND_COMPANIONS
    };

    public static enum State {

        WALKING,
        DOING,
        CANCELLED
    };
    protected Type type;
    protected State state;
    protected Person person;
    protected Region destination;

    public Activity(Person person, Type type) {
        this.person = person;
        this.type = type;
    }

    public void goToDestination() {
        resolveDestination();
        state = State.WALKING;
        // person.station.d(person, "going activity: " + type + " current: " + person.currentRegion);
        if (person.currentRegion != null && destination != null) {
            person.path = Path.findBetween(person.currentRegion, destination);
            person.reachDestination();
        }
    }

    public void cancel() {
        state = State.CANCELLED;
        if (person.path != null) {
            person.path.cancel();
        }
    }

    public boolean isCancelled() {
        return (state == State.CANCELLED);
    }

    public void start() {
        if (!isCancelled()) {
            state = State.DOING;
            // person.station.d(person, "doing activity: " + type + " current: " + person.currentRegion);
            Passenger passenger;
            Companion companion;
            Platform platform;
            ServingRegion region;

            switch (type) {
                case FOLLOW_PASSENGER:
                    person.passivate(); // passenger should take care of its companions
                    break;
                case BUY_TICKET:
                    region = (ServingRegion)person.currentRegion;
                    region.addPersonToShortestQueue(person);
                    person.waitInQueue();
                    break;
                case WAIT_IN_WAITING_ROOM:
                    if (person instanceof TrainOrientedPerson) {
                        ((TrainOrientedPerson)person).waitInWaitingRoom();
                    }
                    break;
                case WAIT_IN_HALL:
                    if (person instanceof TrainOrientedPerson) {
                        ((TrainOrientedPerson)person).waitInHall();
                    }
                    break;
                case WAIT_ON_PLATFORM:
                    if (person instanceof Passenger) {
                        passenger = (Passenger)person;
                        passenger.train.addPassengerReadyToGetIn(passenger);
                    } else if (person.type == Person.Type.ARRIVING_COMPANION) {
                        companion = (Companion)person;
                        companion.train.addCompanionReadyForArrival(companion);
                    }
                    person.passivate();
                    break;
                case ENTER_TRAIN:
                    platform = (Platform)person.currentRegion;
                    platform.personLeaves(person);
                    // tu mozna wygenerowac followerom dalsze aktywnosci.
                    break;
                case LEAVE_TRAIN:
                    passenger = (Passenger) person;
                    platform = passenger.train.getRealPlatform();
                    passenger.currentRegion = platform;
                    passenger.currentRegion.personEnters(person);
                    for (Companion compan : passenger.companions) {
                        if (passenger.train.getCompanionsReadyForArrival().contains(compan)) {
                            passenger.train.getCompanionsReadyForArrival().remove(compan);
                            compan.activate(); // niech followuje pasazera
                        } else {
                            compan.missTrain();
                            passenger.train.removeNotifyListener(compan);
                        }
                    }
                    passenger.hold(platform.getWalkingTime());
                    break;
                case BIND_COMPANIONS:
                    passenger = (Passenger) person;
                    passenger.leadCompanions = true;
                    break;
                case UNBIND_COMPANIONS:
                    passenger = (Passenger) person;
                    LinkedList<Companion> followers = passenger.getFollowers();
                    passenger.leadCompanions = false;
                    for (Companion follower : followers) {
                        follower.currentRegion = passenger.currentRegion;
                        follower.setPassenger(null);
                        follower.activate();
                    }
                    break;
                case ENTER_STATION:
                    Region hall = person.station.structure.getEntryRegion();
                    person.currentRegion = hall;
                    person.currentRegion.personEnters(person);
                    if (person.futureActivities.getFirst() != Activity.Type.FOLLOW_PASSENGER) {
                        person.hold(person.currentRegion.getWalkingTime());
                    }
                    break;
                case LEAVE_STATION:
                    destination.personLeaves(person);
                    break;
                case GET_INFO:
                    region = (ServingRegion)person.currentRegion;
                    region.addPersonToShortestQueue(person);
                    person.waitInQueue();
                    break;
                case COMPLAIN:
                    region = (ServingRegion)person.currentRegion;
                    region.addPersonToShortestQueue(person);
                    person.waitInQueue();
                    break;
                default:

            }
        }
    }

    private void resolveDestination() {
        Infrastructure structure = person.station.structure;

        switch (type) {
            case WAIT_IN_WAITING_ROOM:
                destination = structure.getWaitingRoom();
                break;
            case WAIT_ON_PLATFORM:
            case ENTER_TRAIN:
            case LEAVE_TRAIN:
                TrainOrientedPerson orientedPerson = (TrainOrientedPerson)person;
                destination = orientedPerson.getTrainRealPlatform() != null ? orientedPerson.getTrainRealPlatform() : orientedPerson.train.getPlatform();
                break;
            case WAIT_IN_HALL:
            case ENTER_STATION:
            case LEAVE_STATION:
                destination = structure.getEntryRegion();
                break;
            case BUY_TICKET:
                destination = structure.getCashDeskRegion();
                break;
            case GET_INFO:
            case COMPLAIN:
                destination = structure.getInformationDeskRegion();
                break;
            default:
                destination = null;
        }
    }
    
    public void setType(Type type) {
        this.type = type;
    }
    
    public Type getType(){
        return type;
    }
}
