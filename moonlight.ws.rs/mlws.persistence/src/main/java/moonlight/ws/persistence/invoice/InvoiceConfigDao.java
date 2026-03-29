package moonlight.ws.persistence.invoice;

import jakarta.enterprise.context.RequestScoped;

// TODO implement this as a real DAO with real DB-data!
@RequestScoped
public class InvoiceConfigDao {

	private InvoiceConfigEntity invoiceConfig;

	public InvoiceConfigEntity getInvoiceConfig() {
		if (invoiceConfig == null) {
			var c = new InvoiceConfigEntity();
			c.setAccountId(45085L);
			c.setChannelId(183451L);
			c.setConsignmentSaleSkuPrefix("CS");
			c.setSupplyPurchaseSkuPrefix("SP");
			invoiceConfig = c;
		}
		return invoiceConfig;
	}

}
