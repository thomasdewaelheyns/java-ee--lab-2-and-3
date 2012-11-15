/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rental;

import java.io.Serializable;

/**
 *
 * @author s0202397
 */
public class CarData implements Serializable{
    
    String name;
    int nbOfSeats;
    float trunkSpace;
    double rentalPricePerDay;
    boolean smokingAllowed;
    
    public CarData(String name, int nbOfSeats, float trunkSpace, double rentalPricePerDay, boolean smokingAllowed){
        this.name = name;
        this.nbOfSeats = nbOfSeats;
        this.trunkSpace = trunkSpace;
        this.rentalPricePerDay = rentalPricePerDay;
        this.smokingAllowed = smokingAllowed;
    }
    
    public String getName(){
        return this.name;
    }
    
    public int getNbOfSeats(){
        return this.nbOfSeats;
    }
    
    public float getTrunkSpace(){
        return this.trunkSpace;
    }
    
    public double getRentalPricePerDay(){
        return this.rentalPricePerDay;
    }
    
    public boolean getSmokingAllowed(){
        return this.smokingAllowed;
    }
    
    
}
