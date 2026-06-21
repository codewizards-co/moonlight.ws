package moonlight.ws.persistence.party;

import static moonlight.ws.api.party.PartyDefaultDto.*;

import java.math.BigDecimal;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import lombok.NonNull;
import moonlight.ws.api.UserConst;
import moonlight.ws.persistence.AbstractDao;

@RequestScoped
public class PartyDefaultDao extends AbstractDao<PartyDefaultEntity> {

	@Override
	public PartyDefaultEntity getEntity(@NonNull Long id) {
		if (PARTY_DEFAULT_ID.equals(id)) {
			return getEntity();
		}
		return super.getEntity(id);
	}

	public PartyDefaultEntity getEntity() {
		EntityManager em = getEntityManager();
		PartyDefaultEntity entity = em.find(PartyDefaultEntity.class, PARTY_DEFAULT_ID);
		if (entity == null) {
			entity = createEntity();
			em.persist(entity);
		}
		return entity;
	}

	protected PartyDefaultEntity createEntity() {
		var entity = new PartyDefaultEntity();
		entity.setId(PARTY_DEFAULT_ID);
		entity.setCreatedByUserId(UserConst.SYSTEM_USERID);
		entity.setChangedByUserId(UserConst.SYSTEM_USERID);
		entity.setTradeDiscountPercent(BigDecimal.valueOf(20));
		entity.setTaxPercent(BigDecimal.ZERO);
		entity.setCatalogName("MUST_CHANGE");
		return entity;
	}
}
