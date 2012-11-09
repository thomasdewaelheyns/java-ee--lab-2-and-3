package session;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.ejb.Remote;
import rental.CarData;
import rental.CarType;
import rental.Reservation;

@Remote
public interface ManagerSessionRemote {
    
    public Set<CarType> getCarTypes(String company);
    
    public Set<Integer> getCars(String company,String type);
    
    public Set<Reservation> getReservations(String company, String type, int id);
    
    public Set<Reservation> getReservations(String company, String type);
      
    public Set<Reservation> getReservationsBy(String renter);
    
    public void initializeServer(HashMap<String, List<CarData>> fileContent) throws Exception;
}