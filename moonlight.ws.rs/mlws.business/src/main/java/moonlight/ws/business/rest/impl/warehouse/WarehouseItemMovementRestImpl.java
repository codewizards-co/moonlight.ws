package moonlight.ws.business.rest.impl.warehouse;

import static java.util.Objects.*;
import static moonlight.ws.api.RestConst.*;
import static moonlight.ws.base.util.FetchUtil.*;
import static moonlight.ws.base.util.StringUtil.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.Warehouse;
import com.liferay.headless.commerce.admin.inventory.client.dto.v1_0.WarehouseItem;
import com.liferay.headless.commerce.admin.inventory.client.resource.v1_0.WarehouseItemResource;
import com.liferay.headless.commerce.admin.inventory.client.resource.v1_0.WarehouseResource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.QueryParam;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moonlight.ws.api.PriceDto;
import moonlight.ws.api.party.SupplierDto;
import moonlight.ws.api.warehouse.WarehouseItemMovementDto;
import moonlight.ws.api.warehouse.WarehouseItemMovementDtoPage;
import moonlight.ws.api.warehouse.WarehouseItemMovementFilter;
import moonlight.ws.api.warehouse.WarehouseItemMovementRest;
import moonlight.ws.api.warehouse.WarehouseItemMovementType;
import moonlight.ws.api.warehouse.WarehouseItemProductDto;
import moonlight.ws.business.mapper.PriceMapper;
import moonlight.ws.business.mapper.SupplierMapper;
import moonlight.ws.business.mapper.WarehouseItemMovementMapper;
import moonlight.ws.business.rest.impl.liferay.SkuCache;
import moonlight.ws.business.rest.impl.liferay.WarehouseItemCache;
import moonlight.ws.liferay.LiferayResourceFactory;
import moonlight.ws.persistence.UserDao;
import moonlight.ws.persistence.UserEntity;
import moonlight.ws.persistence.party.SupplierDao;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementDao;
import moonlight.ws.persistence.warehouse.WarehouseItemMovementEntity;

@RequestScoped
@Transactional(rollbackOn = Throwable.class)
@Slf4j
public class WarehouseItemMovementRestImpl implements WarehouseItemMovementRest {

	@Inject
	private WarehouseItemMovementMapper mapper;

	@Inject
	private WarehouseItemMovementDao dao;

	@Inject
	private SupplierMapper supplierMapper;

	@Inject
	private SupplierDao supplierDao;

	@Inject
	private WarehouseItemCache warehouseItemCache;

	@Inject
	private LiferayResourceFactory liferayResourceFactory;

	@Inject
	private UserDao userDao;

	@Inject
	private SkuCache skuCache;

	@Inject
	private PriceMapper priceMapper;

	@QueryParam(QUERY_FETCH)
	protected String fetch;

	private Map<Long, SupplierDto> supplierId2SupplierDto = new HashMap<>();

	@Override
	public WarehouseItemMovementDto getWarehouseItemMovement(@NonNull Long id) throws Exception {
		WarehouseItemMovementEntity entity = dao.getEntity(id);
		if (entity == null) {
			throw new NotFoundException();
		}
		return fetchRelations(mapper.toDto(entity));
	}

	@Override
	public WarehouseItemMovementDtoPage getWarehouseItemMovements(WarehouseItemMovementFilter filter) throws Exception {
		filter = filter == null ? new WarehouseItemMovementFilter() : filter;
		var searchResult = dao.searchEntities(filter);
		var page = new WarehouseItemMovementDtoPage();
		page.copyFromFilter(filter);
		page.setItems(mapper.toDtos(searchResult.getEntities()));
		page.setTotalSize(searchResult.getTotalSize());
		return fetchRelations(page);
	}

	@Override
	public WarehouseItemMovementDto createWarehouseItemMovement(@NonNull WarehouseItemMovementDto dto)
			throws Exception {
		validate(dto);
		WarehouseItemMovementEntity entity = mapper.toEntity(dto, null);

		UserEntity user = userDao.currentUser();
		entity.setCreatedByUserId(user.getId());
		entity.setChangedByUserId(user.getId());
		entity.setBooked(0);
		boolean draft = isDraft(dto, entity);
		entity.setFinalized(draft ? 0 : System.currentTimeMillis());

		WarehouseItem warehouseItem = getWarehouseItem(entity);
		entity.setWarehouseItemId(warehouseItem.getId());
		entity.setWarehouseItemErc(warehouseItem.getExternalReferenceCode());
		entity.setWarehouseId(warehouseItem.getWarehouseId());
		entity.setWarehouseErc(warehouseItem.getWarehouseExternalReferenceCode());
		entity.setSku(warehouseItem.getSku());
		entity.setUnitOfMeasureKey(warehouseItem.getUnitOfMeasureKey());

		WarehouseItem otherWarehouseItem = getOtherWarehouseItem(entity);
		entity.setOtherWarehouseId(otherWarehouseItem == null ? null : otherWarehouseItem.getWarehouseId());
		entity.setOtherWarehouseItemId(otherWarehouseItem == null ? null : otherWarehouseItem.getId());

		if (entity.getWarehouseItemId().equals(entity.getOtherWarehouseItemId())) {
			String msg = "warehouseItemId=%d and otherWarehouseItemId=%d are equal! They must be different!"//
					.formatted(entity.getWarehouseItemId(), entity.getOtherWarehouseItemId());
			log.error(msg);
			throw new BadRequestException(msg);
		}

		dao.persistEntity(entity);
		return fetchRelations(mapper.toDto(entity));
	}

	@Override
	public void admin_syncSku() throws Exception {
		if (true) {
			log.warn(
					"admin_syncSku: This did not help as renaming an SKU seems to create a new warehouse-item with a new ID.");
			// TODO still need to clarify what to do about this.
			throw new NotFoundException();
		}
		Set<Long> warehouseIds = dao.getWarehouseIds();
		for (Long warehouseId : warehouseIds) {
			List<WarehouseItem> warehouseItems = warehouseItemCache.getWarehouseItems(warehouseId);
			Set<Long> warehouseItemIds = dao.getWarehouseItemIds(warehouseId);
			for (Long warehouseItemId : warehouseItemIds) {
				WarehouseItem warehouseItem = warehouseItems.stream().filter(wi -> warehouseItemId.equals(wi.getId()))
						.findAny().orElse(null);
				if (warehouseItem == null) {
					log.warn("admin_syncSku: No WarehouseItem found for warehouseItemId={} and warehouseId={}!",
							warehouseItemId, warehouseId);
					continue;
				}
				if (isEmpty(warehouseItem.getSku())) {
					log.warn("admin_syncSku: WarehouseItem.sku is empty for warehouseItemId={} and warehouseId={}!",
							warehouseItemId, warehouseId);
					continue;
				}
				dao.updateSku(warehouseItemId, warehouseItem.getSku());
			}
		}
	}

	protected @NonNull WarehouseItemMovementDtoPage fetchRelations(@NonNull WarehouseItemMovementDtoPage dtoPage)
			throws Exception {
		if (!isEmpty(fetch)) {
			for (var dto : dtoPage.getItems()) {
				fetchRelations(dto);
			}
		}
		return dtoPage;
	}

	protected @NonNull WarehouseItemMovementDto fetchRelations(@NonNull WarehouseItemMovementDto dto) throws Exception {
		if (!isEmpty(fetch)) {
			Set<String> fetchSet = getFetchSet(fetch);
			if (fetchSet.contains("products")) {
				fetchProducts(dto);
			}
			if (fetchSet.contains("supplier") || fetchSet.contains("supplier.party")) {
				fetchSupplier(dto, fetchSet);
			}
		}
		return dto;
	}

	protected void fetchSupplier(@NonNull WarehouseItemMovementDto dto, @NonNull Set<String> fetchSet) {
		final Long supplierId = dto.getSupplierId();
		if (supplierId == null) {
			return;
		}
		supplierMapper.setFetch(fetchSet.contains("supplier.party") ? "party" : null);
		SupplierDto supplier = supplierId2SupplierDto //
				.computeIfAbsent(supplierId, id -> supplierMapper
						.toDto(requireNonNull(supplierDao.getEntity(id), "supplierDao.getEntity(" + id + ")")));
		dto.setSupplier(requireNonNull(supplier, "supplier"));
	}

	protected void fetchProducts(@NonNull WarehouseItemMovementDto dto) throws Exception {
		final String sku = requireNonNull(dto.getSku(), "dto.sku");
		final List<WarehouseItemProductDto> products = new ArrayList<>();
		skuCache.getSkus().stream().filter(s -> sku.equals(s.getSku())).forEach(skuObj -> {
			products.add(new WarehouseItemProductDto(skuObj.getProductId(), skuObj.getProductName()));
		});
		dto.setProducts(products);
	}

	protected void validate(@NonNull WarehouseItemMovementDto dto) throws Exception {
		// TODO switch to proper bean-validation
		if (dto.getQuantity() == null) {
			String msg = "quantity is required!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
		if (dto.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
			String msg = "quantity must not be 0!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
		if (dto.getType() == null) {
			String msg = "type is required!";
			log.error(msg);
			throw new BadRequestException(msg);
		}
		if (dto.getType() == WarehouseItemMovementType.TRANSFER) {
			if (dto.getOtherWarehouseId() == null) {
				String msg = "otherWarehouseId is required when type is 'TRANSFER'!";
				log.error(msg);
				throw new BadRequestException(msg);
			}
		}
		if (dto.getType() == WarehouseItemMovementType.SUPPLY) {
			if (dto.getSupplierId() == null) {
				String msg = "supplierId is required when type is 'SUPPLY'!";
				log.error(msg);
				throw new BadRequestException(msg);
			}
		}
		if (dto.getPrice() != null) {
			PriceDto price = dto.getPrice();
			if (price.getQuantity() == null) {
				price.setQuantity(dto.getQuantity());
			} else if (price.getQuantity().compareTo(dto.getQuantity()) != 0) {
				String msg = "quantity=%s differs from price.quantity=%s!".formatted(dto.getQuantity().toPlainString(),
						price.getQuantity().toPlainString());
				log.error(msg);
				throw new BadRequestException(msg);
			}
			try {
				priceMapper.calculateMissingProperties(price);
			} catch (Exception x) {
				String msg = "price is too incomplete to calculate missing properties!";
				log.error(msg, x);
				throw new BadRequestException(msg);
			}
		}
	}

	protected boolean isDraft(@NonNull WarehouseItemMovementDto dto, @NonNull WarehouseItemMovementEntity entity) {
		Boolean draft = null;
		if (dto.getDraft() != null) {
			draft = dto.getDraft(); // may be null => default value then depends on group
		}
		if (entity.getGroup() != null) {
			// if there is a group, we reject modification, if the group is not a draft
			// anymore
			if (entity.getGroup().getFinalized() != 0) {
				String msg = "Group with groupId=%d is already finalized! Cannot add an additional movement to this group."
						.formatted(entity.getGroup().getId());
				log.error(msg);
				throw new BadRequestException(msg);
			}
			if (draft == null) {
				draft = true; // if there is a group, the new movement is automatically a draft
			}
			if (!draft.booleanValue()) {
				String msg = "Group with groupId=%d is still a draft, but movement.draft=false. To add a movement to a group, it must be a draft."
						.formatted(entity.getGroup().getId());
				log.error(msg);
				throw new BadRequestException(msg);
			}
			// if there is a group, the draft-status must be determined by the group =>
			// final sanity-check.
			requireNonNull(draft, "draft");
		} else {
			// There is no group and we currently do not (yet) have API to finalize an
			// individual movement (only the group)
			// Hence, we enforce the draft to be false or null here.
			if (draft != null && draft.booleanValue()) {
				String msg = "There is no group, but draft=true. A movement without group must not be a draft. There is no API to ever finalize it, yet.";
				log.error(msg);
				throw new BadRequestException(msg);
			}
		}
		if (draft == null) {
			// we come here only, if there is no group => immediately finalize!
			return false;
		}
		return draft;
	}

	protected WarehouseItem getWarehouseItem(@NonNull WarehouseItemMovementEntity entity) throws Exception {
		WarehouseItemResource warehouseItemResource = liferayResourceFactory.getResource(WarehouseItemResource.class);
		WarehouseResource warehouseResource = liferayResourceFactory.getResource(WarehouseResource.class);

		WarehouseItem warehouseItem = null;
		if (entity.getWarehouseItemId() != null) {
			warehouseItem = warehouseItemResource.getWarehouseItem(entity.getWarehouseItemId());
			if (warehouseItem == null) {
				throw new BadRequestException("warehouseItemId=%d unknown!".formatted(entity.getWarehouseItemId()));
			}
		}
		if (warehouseItem == null && !isEmpty(entity.getWarehouseItemErc())) {
			warehouseItem = warehouseItemResource.getWarehouseItemByExternalReferenceCode(entity.getWarehouseItemErc());
			if (warehouseItem == null) {
				throw new BadRequestException("warehouseItemErc='%s' unknown!".formatted(entity.getWarehouseItemErc()));
			}
		}
		if (warehouseItem != null && entity.getWarehouseId() != null
				&& !entity.getWarehouseId().equals(warehouseItem.getWarehouseId())) {
			String msg = "warehouseId=%d and warehouseItem.warehouseId=%d (from Liferay) do not match for warehouseItemId=%d!"
					.formatted(entity.getWarehouseId(), warehouseItem.getWarehouseId(), entity.getWarehouseItemId());
			log.error(msg);
			throw new BadRequestException(msg);
		}
		if (warehouseItem == null) {
			if (entity.getWarehouseId() == null) {
				if (isEmpty(entity.getWarehouseErc())) {
					String msg = "warehouseItemId, warehouseItemErc, warehouseId and warehouseErc are all empty! Cannot determine warehouse!";
					log.error(msg);
					throw new BadRequestException(msg);
				}
				Warehouse warehouse = warehouseResource.getWarehouseByExternalReferenceCode(entity.getWarehouseErc());
				entity.setWarehouseId(warehouse.getId());
			} else {
				Warehouse warehouse = warehouseResource.getWarehouseId(entity.getWarehouseId());
				if (isEmpty(entity.getWarehouseErc())) {
					entity.setWarehouseErc(warehouse.getExternalReferenceCode());
				} else if (!entity.getWarehouseErc().equals(warehouse.getExternalReferenceCode())) {
					String msg = "warehouseId=%d and warehouseErc='%s' do not match! Liferay returned the externalReferenceCode='%s' instead!"
							.formatted(entity.getWarehouseId(), entity.getWarehouseErc(),
									warehouse.getExternalReferenceCode());
					log.error(msg);
					throw new BadRequestException(msg);
				}
			}
			if (isEmpty(entity.getSku())) {
				String msg = "warehouseItemId, warehouseItemErc and sku are all empty! Cannot determine warehouse-item!";
				log.error(msg);
				throw new BadRequestException(msg);
			}
			warehouseItem = getWarehouseItem(requireNonNull(entity.getWarehouseId(), "entity.warehouseId"),
					entity.getSku());
			if (warehouseItem == null) {
				String msg = "No warehouse-item found with sku='%s' (warehouseId=%d, warehouseErc='%s')!"
						.formatted(entity.getSku(), entity.getWarehouseId(), entity.getWarehouseErc());
				log.error(msg);
				throw new BadRequestException(msg);
			}
		}
		return warehouseItem;
	}

	protected WarehouseItem getOtherWarehouseItem(@NonNull WarehouseItemMovementEntity entity) throws Exception {
		if (isEmpty(entity.getSku())) {
			throw new IllegalArgumentException("entity.sku must not be empty!");
		}
		WarehouseItemResource warehouseItemResource = liferayResourceFactory.getResource(WarehouseItemResource.class);

		WarehouseItem warehouseItem = null;
		if (entity.getOtherWarehouseItemId() != null) {
			warehouseItem = warehouseItemResource.getWarehouseItem(entity.getOtherWarehouseItemId());
			if (warehouseItem == null) {
				throw new BadRequestException(
						"otherWarehouseItemId=%d unknown!".formatted(entity.getOtherWarehouseItemId()));
			}
		}
		if (warehouseItem != null && entity.getOtherWarehouseId() != null
				&& !entity.getOtherWarehouseId().equals(warehouseItem.getWarehouseId())) {
			String msg = "otherWarehouseId=%d and otherWarehouseItem.warehouseId=%d (from Liferay) do not match for otherWarehouseItemId=%d!"
					.formatted(entity.getOtherWarehouseId(), warehouseItem.getWarehouseId(),
							entity.getOtherWarehouseItemId());
			log.error(msg);
			throw new BadRequestException(msg);
		}
		if (warehouseItem == null && entity.getOtherWarehouseId() != null) {
			warehouseItem = getWarehouseItem(entity.getOtherWarehouseId(), entity.getSku());
			if (warehouseItem == null) {
				String msg = "No warehouse-item found with sku='%s' and otherWarehouseId=%d!"//
						.formatted(entity.getSku(), entity.getOtherWarehouseId());
				log.error(msg);
				throw new BadRequestException(msg);
			}
		}
		return warehouseItem;
	}

	protected WarehouseItem getWarehouseItem(@NonNull Long warehouseId, @NonNull String sku) throws Exception {
		if (isEmpty(sku)) {
			throw new IllegalArgumentException("sku must not be empty!");
		}
		return warehouseItemCache.getWarehouseItems(requireNonNull(warehouseId, "warehouseId")).stream()
				.filter(wi -> sku.equals(wi.getSku())).findAny().orElse(null);
	}
}
