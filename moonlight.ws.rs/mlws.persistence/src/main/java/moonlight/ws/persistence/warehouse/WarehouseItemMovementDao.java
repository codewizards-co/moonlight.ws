package moonlight.ws.persistence.warehouse;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.TypedQuery;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.warehouse.WarehouseItemMovementFilter;
import moonlight.ws.api.warehouse.WarehouseItemMovementType;
import moonlight.ws.persistence.AbstractDao;
import moonlight.ws.persistence.SearchResult;

@RequestScoped
@Slf4j
public class WarehouseItemMovementDao extends AbstractDao<WarehouseItemMovementEntity> {

	public SearchResult<WarehouseItemMovementEntity> searchEntities(WarehouseItemMovementFilter filter) {
		var params = new HashMap<String, Object>();
		var jpqlCriteria = "";
		if (filter != null) {
			if (filter.getFilterWarehouseItemId() != null) {
				jpqlCriteria += " and e.warehouseItemId = :warehouseItemId";
				params.put("warehouseItemId", filter.getFilterWarehouseItemId());
			}
			if (filter.getFilterWarehouseItemErc() != null) {
				jpqlCriteria += " and e.warehouseItemErc = :warehouseItemErc";
				params.put("warehouseItemErc", filter.getFilterWarehouseItemErc());
			}
			if (filter.getFilterSku() != null) {
				jpqlCriteria += " and lower(e.sku) like lower(:sku)";
				params.put("sku", prepareLikeCriterion(filter.getFilterSku()));
			}
			if (filter.getFilterWarehouseErc() != null) {
				jpqlCriteria += " and e.warehouseErc = :warehouseErc";
				params.put("warehouseErc", filter.getFilterWarehouseErc());
			}
			if (filter.getFilterWarehouseId() != null) {
				jpqlCriteria += " and e.warehouseId = :warehouseId";
				params.put("warehouseId", filter.getFilterWarehouseId());
			}
			if (filter.getFilterType() != null) {
				jpqlCriteria += " and e.type = :type";
				params.put("type", filter.getFilterType());
			}

			if (filter.getFilterCreatedFromIncl() != null) {
				jpqlCriteria += " and e.created >= :createdFromIncl";
				params.put("createdFromIncl", filter.getFilterCreatedFromIncl());
			}
			if (filter.getFilterCreatedToExcl() != null) {
				jpqlCriteria += " and e.created < :createdToExcl";
				params.put("createdToExcl", filter.getFilterCreatedToExcl());
			}

			if (filter.getFilterChangedFromIncl() != null) {
				jpqlCriteria += " and e.changed >= :changedFromIncl";
				params.put("changedFromIncl", filter.getFilterChangedFromIncl());
			}
			if (filter.getFilterChangedToExcl() != null) {
				jpqlCriteria += " and e.changed < :changedToExcl";
				params.put("changedToExcl", filter.getFilterChangedToExcl());
			}

			if (filter.getFilterBookedFromIncl() != null) {
				jpqlCriteria += " and e.booked >= :bookedFromIncl";
				params.put("bookedFromIncl", filter.getFilterBookedFromIncl().toEpochMilli());
			}
			if (filter.getFilterBookedToExcl() != null) {
				jpqlCriteria += " and e.booked < :bookedToExcl";
				params.put("bookedToExcl", filter.getFilterBookedToExcl().toEpochMilli());
			}
			if (filter.getFilterBooked() != null) {
				if (filter.getFilterBooked()) {
					jpqlCriteria += " and e.booked <> 0";
				} else {
					jpqlCriteria += " and e.booked = 0";
				}
			}
			if (filter.getFilterDraft() != null) {
				if (filter.getFilterDraft()) {
					jpqlCriteria += " and e.finalized = 0";
				} else {
					jpqlCriteria += " and e.finalized <> 0";
				}
			}
			if (filter.getFilterGroupId() != null) {
				jpqlCriteria += " and e.group.id = :groupId";
				params.put("groupId", filter.getFilterGroupId());
			}
		}
		return searchEntities(jpqlCriteria, params, filter);
	}

	public Set<WarehouseItemMovementEntity> getWarehouseItemMovementsWithoutInvoiceItemForSale(
			@NonNull Long warehouseId) {
		String sql = """
				select wim.id
				from "WarehouseItemMovement" wim
				left join "InvoiceItem" ii on
					ii."warehouseItemMovementId" = wim.id
					and ii."deleted" = 0
				where
					ii.id is null
					and wim."type" = :warehouseItemMovementType
					and wim."warehouseId" = :warehouseId
					and wim."finalized" <> 0
					and wim."booked" <> 0
				""";
		TypedQuery<Long> query = (TypedQuery<Long>) entityManager.createNativeQuery(sql, Long.class);
		query.setParameter("warehouseItemMovementType", WarehouseItemMovementType.SALE.getDbCode());
		query.setParameter("warehouseId", warehouseId);
		List<Long> warehouseItemMovementIds = query.getResultList();
		return getEntities(warehouseItemMovementIds);
	}

	public Set<WarehouseItemMovementEntity> getWarehouseItemMovementsWithoutInvoiceItemForSupply(
			@NonNull Long supplierId) {
		String sql = """
				select wim.id
				from "WarehouseItemMovement" wim
				left join "InvoiceItem" ii on
					ii."warehouseItemMovementId" = wim.id
					and ii."deleted" = 0
				where
					ii.id is null
					and wim."type" = :warehouseItemMovementType
					and wim."supplierId" = :supplierId
					and wim."finalized" <> 0
					and wim."booked" <> 0
				""";
		TypedQuery<Long> query = (TypedQuery<Long>) entityManager.createNativeQuery(sql, Long.class);
		query.setParameter("warehouseItemMovementType", WarehouseItemMovementType.SUPPLY.getDbCode());
		query.setParameter("supplierId", supplierId);
		List<Long> warehouseItemMovementIds = query.getResultList();
		return getEntities(warehouseItemMovementIds);
	}

	public Set<Long> getWarehouseIds() {
		TypedQuery<Long> query = entityManager.createQuery( //
				"SELECT DISTINCT e.warehouseId FROM %s e".formatted(getEntityName()), Long.class);
		return new TreeSet<Long>(query.getResultList());
	}

	public Set<Long> getWarehouseItemIds(@NonNull Long warehouseId) {
		TypedQuery<Long> query = entityManager.createQuery( //
				"SELECT DISTINCT e.warehouseItemId FROM %s e WHERE e.warehouseId = :warehouseId"
						.formatted(getEntityName()),
				Long.class);
		query.setParameter("warehouseId", warehouseId);
		return new TreeSet<Long>(query.getResultList());
	}

	public void updateSku(@NonNull Long warehouseItemId, @NonNull String sku) {
		TypedQuery<WarehouseItemMovementEntity> query = entityManager
				.createQuery("SELECT e FROM %s e WHERE e.warehouseItemId = :warehouseItemId AND e.sku <> :sku"
						.formatted(getEntityName()), WarehouseItemMovementEntity.class);
		query.setParameter("warehouseItemId", warehouseItemId);
		query.setParameter("sku", sku);
		List<WarehouseItemMovementEntity> movements = query.getResultList();
		if (movements.isEmpty()) {
			log.info("updateSku: warehouseItemId={} sku='{}': All OK. No movements to update.", warehouseItemId, sku);
			return;
		}
		log.warn("updateSku: warehouseItemId={} sku='{}': Found mismatches. {} movements to update.", warehouseItemId,
				sku, movements.size());
		for (WarehouseItemMovementEntity movement : movements) {
			log.warn("updateSku: id={}: Changed sku: '{}' => '{}'", movement.getId(), movement.getSku(), sku);
			movement.setSku(sku); // IMPORTANT: We do not change the changed-timestamp, because the actual data
									// wasn't changed, but just the configuration of the warehouse-item.
		}
	}
}
