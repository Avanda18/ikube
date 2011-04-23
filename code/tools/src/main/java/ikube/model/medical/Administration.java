package ikube.model.medical;

import java.sql.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

/**
 * This class represents a medication that was administered to a patient.
 * 
 * @author Michael Couck
 * @since 05.03.2011
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Administration extends Persistable {

	private Date dt;
	private Integer quantity;

	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER, targetEntity = Medication.class)
	private Medication medication;

	public Date getDt() {
		return dt;
	}

	public void setDt(Date date) {
		this.dt = date;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Medication getMedication() {
		return medication;
	}

	public void setMedication(Medication medication) {
		this.medication = medication;
	}

}
