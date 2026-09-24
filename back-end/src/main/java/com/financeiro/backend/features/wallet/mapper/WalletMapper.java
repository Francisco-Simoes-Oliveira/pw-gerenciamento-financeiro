package com.financeiro.backend.features.wallet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.financeiro.backend.features.wallet.dto.request.CreateWalletRequest;
import com.financeiro.backend.features.wallet.dto.request.UpdateWalletRequest;
import com.financeiro.backend.features.wallet.dto.response.WalletResponse;
import com.financeiro.backend.features.wallet.entity.Wallet;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface WalletMapper {
    Wallet toEntity(CreateWalletRequest request);

    @Mapping(target = "ownerId", source = "owner.id")
    WalletResponse toResponse(Wallet entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "lastBalanceUpdate", ignore = true)
    void updateEntityFromDto(UpdateWalletRequest dto, @MappingTarget Wallet entity);
}
