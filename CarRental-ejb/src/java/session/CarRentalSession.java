package session;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.MessageDrivenContext;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Quote;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateful
public class CarRentalSession implements CarRentalSessionRemote {
    
    @PersistenceContext EntityManager em;

    private String renter;
    private List<Quote> quotes = new LinkedList<Quote>();

    @Override
    public List<String> getAllRentalCompanies() {
        return em.createQuery("SELECT c.name FROM CarRentalCompany c").getResultList();
    }
    
    @Override
    public List<CarType> getAvailableCarTypes(Date start, Date end) {
        List<CarRentalCompany> companies = em.createQuery("SELECT c FROM CarRentalCompany c").getResultList();
        List<CarType> availableCarTypes = new LinkedList<CarType>();
        for(CarRentalCompany crc : companies) {
            for(CarType ct : crc.getAvailableCarTypes(start, end)) {
                if(!availableCarTypes.contains(ct)) {
                    availableCarTypes.add(ct);
                }
            }
        }
        return availableCarTypes;
    }

    @Override
    public Quote createQuote(String company, ReservationConstraints constraints) throws ReservationException {
        CarRentalCompany companyObject = (CarRentalCompany) em.createQuery("SELECT c FROM CarRentalCompany c WHERE c.name LIKE :custName").setParameter("custName", company).getSingleResult();
        Quote out = companyObject.createQuote(constraints, renter);
        quotes.add(out);
        return out;
    }

    @Override
    public List<Quote> getCurrentQuotes() {
        return quotes;
    }
    
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Override
    public List<Reservation> confirmQuotes() throws ReservationException {
        List<Reservation> done = new LinkedList<Reservation>();
        InitialContext ic;
        SessionContext sctxLookup;
        try {
            ic = new InitialContext();
             sctxLookup = (SessionContext) ic.lookup("java:comp/EJBContext");
        } catch (NamingException ex) {
            Logger.getLogger(CarRentalSession.class.getName()).log(Level.SEVERE, null, ex);
            throw new ReservationException("Could not start session.");
        }
        try{
            for (Quote quote : quotes) {
                CarRentalCompany comp = (CarRentalCompany) em.createQuery("SELECT c FROM CarRentalCompany c WHERE c.name LIKE :custName").setParameter("custName", quote.getRentalCompany()).getSingleResult();
                done.add(comp.confirmQuote(quote));
            }
        }catch(Exception e){
            sctxLookup.setRollbackOnly();
            throw new ReservationException("Reservation failed, all made reservation are cancelled.");
        }
        return done;
    }

    @Override
    public void setRenterName(String name) {
        if (renter != null) {
            throw new IllegalStateException("name already set");
        }
        renter = name;
    }
}