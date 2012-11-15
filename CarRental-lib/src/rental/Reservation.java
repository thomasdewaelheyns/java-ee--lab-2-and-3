package rental;

import javax.persistence.*;

@Entity
public class Reservation extends Quote {

    @Id @GeneratedValue private int uniqueIdentifier;
    private int carId;

    public Reservation() {
    }
    
    /***************
     * CONSTRUCTOR *
     ***************/

    public Reservation(Quote quote, int carId) {
    	super(quote.getCarRenter(), quote.getStartDate(), quote.getEndDate(), 
    		quote.getRentalCompany(), quote.getCarType(), quote.getRentalPrice());
        this.carId = carId;
    }
    
    /******
     * ID *
     ******/
    
    public int getCarId() {
    	return carId;
    }
    
    /*************
     * TO STRING *
     *************/
    
    @Override
    public String toString() {
        return String.format("Reservation for %s from %s to %s at %s\nCar type: %s\tCar: %s\nTotal price: %.2f", 
                getCarRenter(), getStartDate(), getEndDate(), getRentalCompany(), getCarType(), getCarId(), getRentalPrice());
    }
    
    public boolean equals(Reservation other){
    return (
            this.getCarRenter().equals(other.getCarRenter()) &&
            this.getStartDate().equals(other.getStartDate()) &&
            this.getEndDate().equals(other.getEndDate()) &&
            this.getRentalCompany().equals(other.getRentalCompany()) &&
            this.getCarType().equals(other.getCarType()) &&
            this.getCarId() == other.getCarId() &&
            this.getRentalPrice() == other.getRentalPrice()
            );
    }
}