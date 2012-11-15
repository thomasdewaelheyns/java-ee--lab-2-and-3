package session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.jboss.logging.Logger;
import rental.Car;
import rental.CarData;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Reservation;

@Stateless
public class ManagerSession implements ManagerSessionRemote {
    
    @PersistenceContext EntityManager em;

    @Override
    public Set<Integer> getCars(String company, CarData data) throws Exception{
        CarRentalCompany comp = (CarRentalCompany) em.createQuery("SELECT c FROM CarRentalCompany c WHERE c.name LIKE :custName")
                    .setParameter("custName", company).getSingleResult();
        
        if(comp == null){
            throw new Exception("This company does not exist");
        } else {
            CarType carType = (CarType) em.createQuery("SELECT carType FROM CarType carType WHERE carType.name LIKE :custName "
                        + "AND carType.nbOfSeats = :custSeats AND carType.rentalPricePerDay = :custPrice "
                        + "AND carType.trunkSpace = :custSpace AND carType.smokingAllowed = :custSmoking ")
                        .setParameter("custName", data.getName())
                        .setParameter("custSeats", data.getNbOfSeats())
                        .setParameter("custPrice", data.getRentalPricePerDay())
                        .setParameter("custSpace", data.getTrunkSpace())
                        .setParameter("custSmoking", data.getSmokingAllowed())
                        .getSingleResult();

            if(carType == null){
                throw new Exception("This type does not exist");
            } else {
                return comp.getCarsUidByType(carType);
            }
        }
    }
    

    @Override
    public Set<Reservation> getReservations(String company, CarData data, int id) throws Exception{
        CarRentalCompany comp = (CarRentalCompany) em.createQuery("SELECT c FROM CarRentalCompany c WHERE c.name LIKE :custName")
                    .setParameter("custName", company).getSingleResult();
        
        if(comp == null){
            throw new Exception("This company does not exist");
        } else {
                return comp.getCar(id).getReservations();
        }
    }

    @Override
    public Set<Reservation> getReservations(String company, String carType) throws Exception{
        CarRentalCompany comp = (CarRentalCompany) em.createQuery("SELECT c FROM CarRentalCompany c WHERE c.name LIKE :custName")
                    .setParameter("custName", company).getSingleResult();
        
        if(comp == null){
            throw new Exception("This company does not exist");
        } else {
            CarType type = (CarType) em.createQuery("SELECT carType FROM CarType carType WHERE carType.name LIKE :custName ")
                        .setParameter("custName", carType)
                        .getSingleResult();

            if(type == null){
                throw new Exception("This type does not exist");
            } else {
                Set<Reservation> reservations = new HashSet<Reservation>();
                for(Car car: comp.getCars(type)){
                reservations.addAll(car.getReservations());
                }
                return reservations;
            }
        }
    }
    
    @Override
    public List<Reservation> getReservationsBy(String renter){
        return em.createQuery("SELECT reservation FROM Reservation reservation WHERE reservation.carRenter LIKE :carRenter")
                .setParameter("carRenter", renter)
                .getResultList();
    }

    
    /*
     * Add companies with their cars in one time
     */
    @Override
    public void initializeServer(HashMap<String, List<CarData>> fileContent) throws Exception{
        boolean skipped = false;
        for(String comp:fileContent.keySet()){
            List<String> companies = new ArrayList<String>();
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
        if(skipped){
            throw new Exception("One or more companies already existed. There were not added and no cars were added for them.");
        }
    } 
    
    
    private List<Car> createCarList(List<CarData> cardata){
        List<Car> returnList = new ArrayList<Car>();
        for(CarData data: cardata){
            List<CarType> types = em.createQuery("SELECT carType FROM CarType carType WHERE carType.name LIKE :custName "
                    + "AND carType.nbOfSeats = :custSeats AND carType.rentalPricePerDay = :custPrice "
                    + "AND carType.trunkSpace = :custSpace AND carType.smokingAllowed = :custSmoking ")
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
                Car car = new Car(carType);
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
                    + "AND carType.nbOfSeats = :custSeats AND carType.rentalPricePerDay = :custPrice "
                    + "AND carType.trunkSpace = :custSpace AND carType.smokingAllowed = :custSmoking ")
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
                Car car = new Car(carType);
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
                    + "AND carType.nbOfSeats = :custSeats AND carType.rentalPricePerDay = :custPrice "
                    + "AND carType.trunkSpace = :custSpace AND carType.smokingAllowed = :custSmoking ")
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
    
    
    public List<String> getAllCompanies(){
        return em.createQuery("SELECT c.name FROM CarRentalCompany c").getResultList();
    }
    
    @Override
    public List<String> getCarTypes(String companyName){
        /*List<CarType> carTypes = em.createQuery("SELECT car.type FROM CarRentalCompany company, Car car JOIN CarRentalCompany.cars Car WHERE company LIKE :companyname")
                                    .setParameter("companyname", companyName)
                                    .getResultList();*/
        CarRentalCompany company = (CarRentalCompany) em.createQuery("SELECT company FROM CarRentalCompany company WHERE company.name LIKE :companyname")
                                            .setParameter("companyname", companyName)
                                            .getSingleResult();
        List<String> carTypes = new ArrayList<String>();
        for(CarType cartype: company.getCarTypes()){
            carTypes.add(cartype.getName() + ", " + cartype.getNbOfSeats() + ", " + cartype.getRentalPricePerDay() + ", " + cartype.getTrunkSpace() + ", " + cartype.isSmokingAllowed());
        }    
    return carTypes;    
    }
}