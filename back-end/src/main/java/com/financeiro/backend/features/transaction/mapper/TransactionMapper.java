package com.financeiro.backend.features.transaction.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.financeiro.backend.features.transaction.dto.request.CreateTransactionRequest;
import com.financeiro.backend.features.transaction.dto.request.UpdateTransactionRequest;
import com.financeiro.backend.features.transaction.dto.response.TransactionResponse;
import com.financeiro.backend.features.transaction.entity.Transaction;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TransactionMapper {
    @Mapping(target = "wallet", ignore = true)
    @Mapping(target = "destinationWallet", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Transaction toEntity(CreateTransactionRequest request);

    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "destinationWalletId", source = "destinationWallet.id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    TransactionResponse toResponse(Transaction entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "wallet", ignore = true)
    @Mapping(target = "destinationWallet", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateTransactionRequest dto, @MappingTarget Transaction entity);
}
