package session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NamedQuery;
import javax.persistence.PersistenceContext;
import rental.Car;
import rental.CarData;
import rental.CarRentalCompany;
import rental.CarType;
import rental.RentalStore;
import rental.Reservation;
import rental.ReservationException;

@Stateless
public class ManagerSession implements ManagerSessionRemote {
    
    @PersistenceContext EntityManager em;
    
    @Override
    public Set<CarType> getCarTypes(String company) {
        try {
            return new HashSet<CarType>(RentalStore.getRental(company).getAllTypes());
        } catch (ReservationException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public Set<Integer> getCars(String company, String type) {
        Set<Integer> out = new HashSet<Integer>();
        try {
            for(Car c: RentalStore.getRental(company).getCars(type)){
                out.add(c.getId());
            }
        } catch (ReservationException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return out;
    }

    @Override
    public Set<Reservation> getReservations(String company, String type, int id) {
        try {
            return RentalStore.getRental(company).getCar(id).getReservations();
        } catch (ReservationException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public Set<Reservation> getReservations(String company, String type) {
        Set<Reservation> out = new HashSet<Reservation>();
        try {
            for(Car c: RentalStore.getRental(company).getCars(type)){
                out.addAll(c.getReservations());
            }
        } catch (ReservationException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return out;
    }

    @Override
    public Set<Reservation> getReservationsBy(String renter){
        Set<Reservation> out = new HashSet<Reservation>();
        for(CarRentalCompany crc : RentalStore.getRentals().values()) {
            out.addAll(crc.getReservationsBy(renter));
        }
        return out;
    }

    
    /*
     * Add companies with their cars in one time
     */
    @Override
    public void initializeServer(HashMap<String, List<CarData>> fileContent) throws Exception{
        List<String> companies = new ArrayList<String>();
        boolean skipped = false;
        for(String comp:fileContent.keySet()){
            companies.addAll(em.createQuery("SELECT c.uniqueIdentifier FROM CarRentalCompany c WHERE c.name LIKE :custName")
                    .setParameter("custName", comp)
                    .getResultList());
            if(companies.isEmpty()){
                List<Car> carlist = createCarList(fileContent.get(comp));
                CarRentalCompany newCompany = new CarRentalCompany(comp, carlist);
                em.persist(newCompany);
            }
            else{
                skipped = true;
            }
        }
        if(!skipped){
            throw new Exception("One or more companies already existed. They were not added and no cars were added for them.");
        }
    } 
    
    
    private List<Car> createCarList(List<CarData> cardata){
        List<Car> returnList = new ArrayList<Car>();
        for(CarData data: cardata){
            List<CarType> types = em.createQuery("SELECT carType FROM CarType carType WHERE carType.name LIKE :custName "
                    + "AND carType.nbOfSeats LIKE :custSeats AND carType.rentalPricePerDay LIKE :custPrice "
                    + "AND carType.trunkSpace LIKE :custSpace AND carType.smokingAllowed LIKE :custSmoking ")
                    .setParameter("custName", data.getName())
                    .setParameter("custSeats", data.getNbOfSeats())
                    .setParameter("custPrice", data.getRentalPricePerDay())
                    .setParameter("custSpace", data.getTrunkSpace())
                    .setParameter("custSmoking", data.getSmokingAllowed())
                    .getResultList();
            if(types.isEmpty()){
                //create new type and create the car
                CarType carType = new CarType(data.getName(), data.getNbOfSeats(), data.getTrunkSpace(), data.getRentalPricePerDay(), data.getSmokingAllowed());
                em.persist(carType);
                Car car = new Car(types.get(0));
               em.persist(car);
               returnList.add(car);
                
            }
            else{
               Car car = new Car(types.get(0));
               em.persist(car);
               returnList.add(car);
            }
        }
        return returnList;
    }
    
    /*
     * Add companies without any cars
     */
    
    @Override
    public void addCompany(String name) throws Exception{
        List<CarRentalCompany> companies = em.createQuery("SELECT c FROM CarRentalCompany c WHERE c.name LIKE :custName").setParameter("custName", name).getResultList();
        if(companies.isEmpty()){
            CarRentalCompany comp = new CarRentalCompany(name);
            em.persist(comp);
        }
        else{
            throw new Exception("A company with that name already exists.");
        }
    }
    
    
    @Override
    public void addCarToCompany(String name, CarData data) throws Exception{
        List<CarRentalCompany> companies = em.createQuery("SELECT c FROM CarRentalCompany c WHERE c.name LIKE :custName").setParameter("custName", name).getResultList();
        if(!companies.isEmpty()){
            List<CarType> types = em.createQuery("SELECT carType FROM CarType carType WHERE carType.name LIKE :custName "
                    + "AND carType.nbOfSeats LIKE :custSeats AND carType.rentalPricePerDay LIKE :custPrice "
                    + "AND carType.trunkSpace LIKE :custSpace AND carType.smokingAllowed LIKE :custSmoking ")
                    .setParameter("custName", data.getName())
                    .setParameter("custSeats", data.getNbOfSeats())
                    .setParameter("custPrice", data.getRentalPricePerDay())
                    .setParameter("custSpace", data.getTrunkSpace())
                    .setParameter("custSmoking", data.getSmokingAllowed())
                    .getResultList();
            if(types.isEmpty()){
                //create new type and create the car
                CarType carType = new CarType(data.getName(), data.getNbOfSeats(), data.getTrunkSpace(), data.getRentalPricePerDay(), data.getSmokingAllowed());
                em.persist(carType);
                Car car = new Car(types.get(0));
                em.persist(car);
                companies.get(0).addCar(car);
                
            }
            else{
                //The type already exists. Use it to create a new Car.
                Car car = new Car(types.get(0));
                em.persist(car);
                companies.get(0).addCar(car);
            }
        }
        else{
            throw new Exception("A company with that name does not exists.");
        }
        
    }
    
    
    
    
    
    @Override
    public void addCarType(CarData data) throws Exception{
        List<CarType> types = em.createQuery("SELECT carType FROM CarType carType WHERE carType.name LIKE :custName "
                    + "AND carType.nbOfSeats LIKE :custSeats AND carType.rentalPricePerDay LIKE :custPrice "
                    + "AND carType.trunkSpace LIKE :custSpace AND carType.smokingAllowed LIKE :custSmoking ")
                    .setParameter("custName", data.getName())
                    .setParameter("custSeats", data.getNbOfSeats())
                    .setParameter("custPrice", data.getRentalPricePerDay())
                    .setParameter("custSpace", data.getTrunkSpace())
                    .setParameter("custSmoking", data.getSmokingAllowed())
                    .getResultList();
        if(types.isEmpty()){
            //create new type
            CarType carType = new CarType(data.getName(), data.getNbOfSeats(), data.getTrunkSpace(), data.getRentalPricePerDay(), data.getSmokingAllowed());
            em.persist(carType);
        }
        else{
            throw new Exception("This CarType already exists.");
        }
    }
}