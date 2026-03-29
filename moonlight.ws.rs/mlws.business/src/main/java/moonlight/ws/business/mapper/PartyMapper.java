package moonlight.ws.business.mapper;

import static moonlight.ws.base.util.FetchUtil.*;
import static moonlight.ws.business.util.TimeUtil.*;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import moonlight.ws.api.party.PartyDto;
import moonlight.ws.api.party.SupplierDto;
import moonlight.ws.persistence.party.PartyEntity;
import moonlight.ws.persistence.party.SupplierEntity;

@RequestScoped
public class PartyMapper extends AbstractMapper<PartyEntity, PartyDto> {

	@Inject
	private ConsigneeMapper consigneeMapper;

	@Inject
	private SupplierMapper supplierMapper;

	@Getter
	@Setter
	private String fetch;

	@Override
	protected void copyPropertiesToEntity(@NonNull PartyEntity entity, @NonNull PartyDto dto) {
		// id, created*, changed*, deleted cannot be written by client!
		entity.setCode(dto.getCode());
		entity.setActive(dto.getActive() == null ? true : dto.getActive());
		entity.setName(dto.getName());
		entity.setEmail(dto.getEmail());
		entity.setPhone(dto.getPhone());
		entity.setStreet1(dto.getStreet1());
		entity.setStreet2(dto.getStreet2());
		entity.setStreet3(dto.getStreet3());
		entity.setZip(dto.getZip());
		entity.setCity(dto.getCity());
		entity.setRegionCode(dto.getRegionCode());
		entity.setCountryIsoCode(dto.getCountryIsoCode());
		entity.setTaxNo(dto.getTaxNo());
		entity.setWebsite(dto.getWebsite());
		entity.setDescription(dto.getDescription());
		// The relations consignees and supplier are not written to the DB! They must be
		// written via the consignee- or the supplier-REST-API.
	}

	@Override
	protected void copyPropertiesToDto(@NonNull PartyDto dto, @NonNull PartyEntity entity) {
		dto.setId(entity.getId());
		dto.setChanged(entity.getChanged());
		dto.setChangedByUserId(entity.getChangedByUserId());
		dto.setCreated(entity.getCreated());
		dto.setCreatedByUserId(entity.getCreatedByUserId());
		dto.setDeleted(instantFromMillis(entity.getDeleted()));
		dto.setDeletedByUserId(entity.getDeletedByUserId());
		dto.setCode(entity.getCode());
		dto.setActive(entity.isActive());
		dto.setName(entity.getName());
		dto.setEmail(entity.getEmail());
		dto.setPhone(entity.getPhone());
		dto.setStreet1(entity.getStreet1());
		dto.setStreet2(entity.getStreet2());
		dto.setStreet3(entity.getStreet3());
		dto.setZip(entity.getZip());
		dto.setCity(entity.getCity());
		dto.setRegionCode(entity.getRegionCode());
		dto.setCountryIsoCode(entity.getCountryIsoCode());
		dto.setTaxNo(entity.getTaxNo());
		dto.setWebsite(entity.getWebsite());
		dto.setDescription(entity.getDescription());

		Set<String> fetchSet = getFetchSet(fetch);
		if (fetchSet.contains("consignees")) {
			dto.setConsignees(entity.getConsignees().stream() //
					.filter(e -> e.getDeleted() == 0) //
					.map(e -> consigneeMapper.toDto(e)) //
					.collect(Collectors.toSet()));
		}

		// We must always look this up, because it is a 1-1-relation in the DTO.
		SupplierEntity supplierEntity = entity.getSuppliers().stream() //
				.filter(e -> e.getDeleted() == 0) //
				.findAny().orElse(null);
		if (supplierEntity == null) {
			dto.setSupplier(null);
		} else {
			if (fetchSet.contains("supplier")) {
				dto.setSupplier(supplierMapper.toDto(supplierEntity));
			} else {
				SupplierDto supplierDto = new SupplierDto();
				supplierDto.setId(supplierEntity.getId());
				dto.setSupplier(supplierDto);
			}
		}
	}
}
