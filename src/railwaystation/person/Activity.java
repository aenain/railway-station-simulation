/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package railwaystation.person;

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
    
    void checkGoToPlatform() {
        if(person instanceof Passenger) {
            ((Passenger)person).checkGoToPlatform();
        }
        else if(person instanceof Companion) {
            ((Companion)person).checkGoToPlatform();
        }
    }
    
    void checkGoOutFromPlatform() {
        if(person instanceof Passenger ) {
            ((Passenger)person).checkGoOutFromPlatform();
        }
        else if(person instanceof Companion) {
            ((Companion)person).checkGoOutFromPlatform();
        }
    }

    public void start() {
        if (!isCancelled()) {
            state = State.DOING;
            Passenger passenger;
            Platform platform;
            ServingRegion region;

            switch (type) {
                case FOLLOW_PASSENGER:
                    person.passivate(); // passenger should take care of its companions
                    break;
                case BUY_TICKET:
                    checkGoToPlatform();
                    
                    region = (ServingRegion)person.currentRegion;
                    region.addPersonToShortestQueue(person);
                    person.waitInQueue();
                    break;
                case WAIT_IN_WAITING_ROOM:
                    checkGoToPlatform();
                    break;
                case WAIT_IN_HALL:
                    checkGoToPlatform();
                    break;
                case WAIT_ON_PLATFORM:
                    checkGoOutFromPlatform();
                    
                    if (person instanceof Passenger) {
                        person.train.addPassengerReadyToGetIn((Passenger) person);
                    } else if (person.type == Person.Type.ARRIVING_COMPANION) {
                        person.train.addCompanionReadyForArrival((Companion) person);
                    }
                    person.passivate();
                    break;
                case ENTER_TRAIN:
                    platform = (person.getTrainRealPlatform() == null ? person.train.getPlatform() : person.getTrainRealPlatform()); // TODO! change to the real platform
                    platform.personLeaves(person);
                    break;
                case LEAVE_TRAIN:
                    passenger = (Passenger) person;
                    platform = passenger.train.getRealPlatform();
                    passenger.currentRegion = platform;
                    passenger.currentRegion.personEnters(person);
                    for (Companion companion : passenger.companions) {
                        if (passenger.train.getCompanionsReadyForArrival().contains(companion)) {
                            passenger.train.getCompanionsReadyForArrival().remove(companion);
                            companion.activate(); // niech followuje pasazera
                        } else {
                            companion.missTrain();
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
                    for (Person companion : passenger.getFollowers()) {
                        companion.currentRegion = passenger.currentRegion;
                        ((Companion)companion).setPassenger(null);
                        companion.activate();
                    }
                    passenger.leadCompanions = false;
                    break;
                case ENTER_STATION:
                    Region hall = person.station.structure.getEntryRegion();
                    person.currentRegion = hall;
                    person.currentRegion.personEnters(person);
                    person.hold(person.currentRegion.getWalkingTime());
                    break;
                case LEAVE_STATION:
                    destination.personLeaves(person);
                    break;
                case GET_INFO:
                    checkGoToPlatform();
                    
                    break;
                case COMPLAIN: 
                    checkGoToPlatform();
                    
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
                destination = person.train.getPlatform(); // TODO! change to the real platform
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
