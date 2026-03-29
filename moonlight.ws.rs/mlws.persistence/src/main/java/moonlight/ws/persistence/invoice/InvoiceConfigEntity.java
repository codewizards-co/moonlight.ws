package moonlight.ws.persistence.invoice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;
import moonlight.ws.persistence.AbstractEntity;

@Getter
@Setter
@Entity(name = "InvoiceConfig")
public class InvoiceConfigEntity extends AbstractEntity {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "InvoiceConfigIdSequence")
	@SequenceGenerator(name = "InvoiceConfigIdSequence", sequenceName = "InvoiceConfigIdSequence", allocationSize = 1)
	private Long id;

	private Long accountId;

	private Long channelId;

	private String consignmentSaleSkuPrefix;

	private String supplyPurchaseSkuPrefix;
}
